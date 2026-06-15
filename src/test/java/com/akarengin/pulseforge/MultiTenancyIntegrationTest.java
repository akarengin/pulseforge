package com.akarengin.pulseforge;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.akarengin.pulseforge.ingestion.service.EventService;
import com.akarengin.pulseforge.workspace.repository.WorkspaceRepository;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for multi-tenancy isolation at repository and service layers.
 * Day 2: Repository-layer workspace isolation
 * Day 6: Service-layer project access enforcement
 */
@SpringBootTest
@Transactional
class MultiTenancyIntegrationTest {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMembershipRepository projectMembershipRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    @Test
    void multiTenancy_workspaceIsolation() {
        // Arrange: two workspaces with events
        Workspace ws1 = workspaceRepository.save(Workspace.builder().name("Tenant A").build());
        Workspace ws2 = workspaceRepository.save(Workspace.builder().name("Tenant B").build());

        Project project1 = projectRepository.save(Project.builder()
                .workspace(ws1)
                .name("Project A")
                .build());
        Project project2 = projectRepository.save(Project.builder()
                .workspace(ws2)
                .name("Project B")
                .build());

        Event event1 = eventRepository.save(Event.builder()
                .workspace(ws1)
                .project(project1)
                .type("click")
                .payload(new HashMap<>())
                .build());
        Event event2 = eventRepository.save(Event.builder()
                .workspace(ws2)
                .project(project2)
                .type("click")
                .payload(new HashMap<>())
                .build());

        // Act: query by workspace and project
        List<Event> ws1Events = eventRepository.findByWorkspace_IdAndProject_Id(
                ws1.getId(), project1.getId());
        List<Event> ws2Events = eventRepository.findByWorkspace_IdAndProject_Id(
                ws2.getId(), project2.getId());

        // Assert: each workspace sees only its own events
        assertThat(ws1Events).hasSize(1).contains(event1);
        assertThat(ws2Events).hasSize(1).contains(event2);
    }

    @Test
    @Disabled("TODO Day 6: Implement ProjectMembershipService.checkAccess() and EventService.getEventsByProject(workspaceId, projectId, userId)")
    void projectAccess_nonMember_cannotSeeProjectEvents() {
        // Arrange
        Workspace ws = workspaceRepository.save(Workspace.builder().name("Acme").build());
        Project project = projectRepository.save(Project.builder()
                .workspace(ws)
                .name("Backend")
                .build());
        
        User member = userRepository.save(User.builder()
                .email("member@acme.com")
                .passwordHash("hash")
                .build());
        User nonMember = userRepository.save(User.builder()
                .email("nonmember@acme.com")
                .passwordHash("hash")
                .build());

        projectMembershipRepository.save(ProjectMembership.builder()
                .project(project)
                .user(member)
                .role(ProjectRole.VIEWER)
                .build());

        Event event = eventRepository.save(Event.builder()
                .workspace(ws)
                .project(project)
                .type("deploy")
                .payload(new HashMap<>())
                .build());

        // Act & Assert: member can access
        // (This will fail until service layer enforcement exists)
        // assertDoesNotThrow(() ->
        //         eventService.getEventsByProject(ws.getId(), project.getId(), member.getId())
        // );

        // Act & Assert: non-member gets denied
        // assertThrows(AccessDeniedException.class, () ->
        //         eventService.getEventsByProject(ws.getId(), project.getId(), nonMember.getId())
        // );
    }
}
