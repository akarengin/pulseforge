package com.akarengin.pulseforge.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.akarengin.pulseforge.IntegrationTestBase;
import com.akarengin.pulseforge.ingestion.dto.EventRequest;
import com.akarengin.pulseforge.ingestion.entity.Event;
import com.akarengin.pulseforge.ingestion.repository.EventRepository;
import com.akarengin.pulseforge.project.entity.Project;
import com.akarengin.pulseforge.project.entity.ProjectMembership;
import com.akarengin.pulseforge.project.entity.ProjectRole;
import com.akarengin.pulseforge.project.repository.ProjectMembershipRepository;
import com.akarengin.pulseforge.project.repository.ProjectRepository;
import com.akarengin.pulseforge.common.entity.User;
import com.akarengin.pulseforge.user.repository.UserRepository;
import com.akarengin.pulseforge.workspace.entity.Workspace;
import com.akarengin.pulseforge.workspace.entity.WorkspaceMembership;
import com.akarengin.pulseforge.workspace.entity.WorkspaceRole;
import com.akarengin.pulseforge.workspace.repository.ApiKeyRepository;
import com.akarengin.pulseforge.workspace.repository.WorkspaceMembershipRepository;
import com.akarengin.pulseforge.workspace.repository.WorkspaceRepository;
import com.akarengin.pulseforge.ingestion.service.EventPersistenceService;
import com.akarengin.pulseforge.workspace.service.ApiKeyService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventIntegrationTest extends IntegrationTestBase {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMembershipRepository projectMembershipRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private EventPersistenceService eventPersistenceService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void cleanup() {
        eventRepository.deleteAll();
        projectMembershipRepository.deleteAll();
        workspaceMembershipRepository.deleteAll();
        projectRepository.deleteAll();
        workspaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test @Order(1)
    void happyPath_endToEnd() {
        Workspace workspace = workspaceRepository.save(Workspace.builder().name("WS-happy").build());
        Project project = projectRepository.save(Project.builder().workspace(workspace).name("P-happy").build());
        String apiKey = apiKeyService.generateApiKey(workspace.getId());

        EventRequest request = new EventRequest("purchase", Map.of("amount", 49.99), "idem-happy-1");
        ResponseEntity<Void> response = postEvent(workspace.getId(), project.getId(), apiKey, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        await().atMost(5, TimeUnit.SECONDS).until(() ->
            eventRepository.findByWorkspace_IdAndProject_Id(workspace.getId(), project.getId()).size() == 1
        );

        List<Event> events = eventRepository.findByWorkspace_IdAndProject_Id(workspace.getId(), project.getId());
        Event saved = events.getFirst();
        assertThat(saved.getType()).isEqualTo("purchase");
        assertThat(saved.getIdempotencyKey()).isEqualTo("idem-happy-1");
        assertThat(saved.getWorkspace().getId()).isEqualTo(workspace.getId());
        assertThat(saved.getProject().getId()).isEqualTo(project.getId());
    }

    @Test @Order(2)
    void clientRetryDedup_redisPath() {
        Workspace workspace = workspaceRepository.save(Workspace.builder().name("WS-retry").build());
        Project project = projectRepository.save(Project.builder().workspace(workspace).name("P-retry").build());
        String apiKey = apiKeyService.generateApiKey(workspace.getId());
        String idempotencyKey = "idem-retry-" + UUID.randomUUID();

        EventRequest request = new EventRequest("click", Map.of("btn", "submit"), idempotencyKey);

        ResponseEntity<Void> first = postEvent(workspace.getId(), project.getId(), apiKey, request);
        ResponseEntity<Void> second = postEvent(workspace.getId(), project.getId(), apiKey, request);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        await().atMost(5, TimeUnit.SECONDS).until(() ->
            eventRepository.findByWorkspace_IdAndProject_Id(workspace.getId(), project.getId()).size() == 1
        );

        assertThat(eventRepository.findByWorkspace_IdAndProject_Id(workspace.getId(), project.getId())).hasSize(1);

        String redisKey = "idempotency:" + workspace.getId() + ":" + idempotencyKey;
        assertThat(redisTemplate.hasKey(redisKey)).isTrue();
    }

    @Test @Order(3)
    void brokerRedeliveryDedup_dbConstraintPath() throws Exception {
        Workspace workspace = workspaceRepository.save(Workspace.builder().name("WS-redelivery").build());
        Project project = projectRepository.save(Project.builder().workspace(workspace).name("P-redelivery").build());
        String idempotencyKey = "idem-redeliver-" + UUID.randomUUID();

        EventRequest request = new EventRequest("click", Map.of("x", "1"), idempotencyKey);

        // First persist succeeds
        eventPersistenceService.persist(workspace.getId(), project.getId(), request);
        assertThat(eventRepository.findByWorkspace_IdAndProject_Id(workspace.getId(), project.getId())).hasSize(1);

        // Second persist with same key throws DataIntegrityViolationException (broker redelivery scenario)
        assertThrows(DataIntegrityViolationException.class,
            () -> eventPersistenceService.persist(workspace.getId(), project.getId(), request)
        );

        // Only one row — constraint is the safety net
        assertThat(eventRepository.findByWorkspace_IdAndProject_Id(workspace.getId(), project.getId())).hasSize(1);
    }

    @Test @Order(6)
    void redisFailOpen() {
        Workspace workspace = workspaceRepository.save(Workspace.builder().name("WS-failopen").build());
        Project project = projectRepository.save(Project.builder().workspace(workspace).name("P-failopen").build());
        String apiKey = apiKeyService.generateApiKey(workspace.getId());
        String idempotencyKey = "idem-failopen-" + UUID.randomUUID();

        redis.stop();
        try {
            EventRequest request = new EventRequest("click", Map.of("x", "1"), idempotencyKey);
            ResponseEntity<Void> response = postEvent(workspace.getId(), project.getId(), apiKey, request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

            await().atMost(10, TimeUnit.SECONDS).until(() ->
                eventRepository.findByWorkspace_IdAndProject_Id(workspace.getId(), project.getId()).size() == 1
            );
        } finally {
            redis.start();
        }
    }

    @Test @Order(4)
    void workspaceIsolation() {
        Workspace wsA = workspaceRepository.save(Workspace.builder().name("WS-A").build());
        Workspace wsB = workspaceRepository.save(Workspace.builder().name("WS-B").build());
        Project projectA = projectRepository.save(Project.builder().workspace(wsA).name("P-A").build());
        Project projectB = projectRepository.save(Project.builder().workspace(wsB).name("P-B").build());
        String keyA = apiKeyService.generateApiKey(wsA.getId());
        String keyB = apiKeyService.generateApiKey(wsB.getId());

        postEvent(wsA.getId(), projectA.getId(), keyA, new EventRequest("ev", Map.of("t", "a"), "idem-iso-a"));
        postEvent(wsB.getId(), projectB.getId(), keyB, new EventRequest("ev", Map.of("t", "b"), "idem-iso-b"));

        await().atMost(5, TimeUnit.SECONDS).until(() ->
            eventRepository.findByWorkspace_IdAndProject_Id(wsA.getId(), projectA.getId()).size() == 1 &&
            eventRepository.findByWorkspace_IdAndProject_Id(wsB.getId(), projectB.getId()).size() == 1
        );

        List<Event> wsAEvents = eventRepository.findByWorkspace_IdAndProject_Id(wsA.getId(), projectA.getId());
        assertThat(wsAEvents).hasSize(1);
        assertThat(wsAEvents.getFirst().getWorkspace().getId()).isEqualTo(wsA.getId());

        // Workspace A's project returns no events when queried with workspace B's ID
        List<Event> crossQuery = eventRepository.findByWorkspace_IdAndProject_Id(wsB.getId(), projectA.getId());
        assertThat(crossQuery).isEmpty();
    }

    @Test @Order(5)
    void authFailures() {
        Workspace wsA = workspaceRepository.save(Workspace.builder().name("WS-auth").build());
        Workspace wsB = workspaceRepository.save(Workspace.builder().name("WS-auth-B").build());
        Project project = projectRepository.save(Project.builder().workspace(wsA).name("P-auth").build());
        String keyB = apiKeyService.generateApiKey(wsB.getId());

        EventRequest request = new EventRequest("ev", Map.of("k", "v"), "idem-auth-1");
        String url = "/api/workspaces/" + wsA.getId() + "/projects/" + project.getId() + "/events";

        // No API key → 401
        HttpHeaders noKeyHeaders = new HttpHeaders();
        noKeyHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Void> noKey = restTemplate.postForEntity(url, new HttpEntity<>(request, noKeyHeaders), Void.class);
        assertThat(noKey.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Invalid API key → 401
        ResponseEntity<Void> invalidKey = postEvent(wsA.getId(), project.getId(), "not-a-real-key", request);
        assertThat(invalidKey.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Valid key but wrong workspace → 403
        ResponseEntity<Void> wrongWorkspace = postEvent(wsA.getId(), project.getId(), keyB, request);
        assertThat(wrongWorkspace.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test @Order(7)
    @Disabled("Enable after JWT auth is implemented — project membership enforcement requires userId from JWT principal")
    void projectAccessEnforcement() {
        Workspace workspace = workspaceRepository.save(Workspace.builder().name("WS-access").build());
        Project project = projectRepository.save(Project.builder().workspace(workspace).name("P-access").build());

        User member = userRepository.save(User.builder().email("member@test.com").passwordHash("hash").build());
        User nonMember = userRepository.save(User.builder().email("nonmember@test.com").passwordHash("hash").build());

        workspaceMembershipRepository.save(WorkspaceMembership.builder().workspace(workspace).user(member).role(WorkspaceRole.MEMBER).build());
        workspaceMembershipRepository.save(WorkspaceMembership.builder().workspace(workspace).user(nonMember).role(WorkspaceRole.MEMBER).build());

        projectMembershipRepository.save(ProjectMembership.builder().project(project).user(member).role(ProjectRole.CONTRIBUTOR).build());
        // nonMember has workspace membership but NO project membership

        // TODO: POST with member JWT → 202; POST with nonMember JWT → 403
    }

    private ResponseEntity<Void> postEvent(UUID workspaceId, UUID projectId, String apiKey, EventRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = "/api/workspaces/" + workspaceId + "/projects/" + projectId + "/events";
        return restTemplate.postForEntity(url, new HttpEntity<>(request, headers), Void.class);
    }
}
