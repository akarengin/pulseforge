package com.akarengin.pulseforge;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.akarengin.pulseforge.common.entity.User;
import com.akarengin.pulseforge.project.entity.Project;
import com.akarengin.pulseforge.project.entity.ProjectMembership;
import com.akarengin.pulseforge.project.entity.ProjectRole;
import com.akarengin.pulseforge.project.repository.ProjectMembershipRepository;
import com.akarengin.pulseforge.project.repository.ProjectRepository;
import com.akarengin.pulseforge.user.dto.auth.LoginRequest;
import com.akarengin.pulseforge.user.dto.auth.LoginResponse;
import com.akarengin.pulseforge.user.repository.UserRepository;
import com.akarengin.pulseforge.workspace.entity.Workspace;
import com.akarengin.pulseforge.workspace.repository.WorkspaceRepository;

public class JwtAuthIntegrationTest extends IntegrationTestBase{

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMembershipRepository projectMembershipRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @AfterEach
    void cleanup() {
        projectMembershipRepository.deleteAll();
        projectRepository.deleteAll();
        workspaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void login_validCredentials_returnsToken() {
        // Arrange: create a user with a known password
        User user = userRepository.save(User.builder()
                .email("alice@example.com")
                .passwordHash(encoder.encode("password123"))
                .build());

        // Act: POST /auth/login
        LoginRequest request = new LoginRequest("alice@example.com", "password123");
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                "/auth/login", request, LoginResponse.class);

        // Assert: 200 with a non-blank token
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
    }

    @Test
    void login_wrongPassword_returns401() {
        userRepository.save(User.builder()
                .email("bob@example.com")
                .passwordHash(encoder.encode("correct"))
                .build());

        LoginRequest request = new LoginRequest("bob@example.com", "wrong");
        ResponseEntity<Void> response = restTemplate.postForEntity("/auth/login", request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getProject_withValidToken_member_returns200() {
        // Arrange
        User user = userRepository.save(User.builder()
                .email("carol@example.com")
                .passwordHash(encoder.encode("pass"))
                .build());
        Workspace ws = workspaceRepository.save(Workspace.builder().name("WS-jwt-1").build());
        Project project = projectRepository.save(Project.builder().workspace(ws).name("P-jwt-1").build());
        projectMembershipRepository.save(ProjectMembership.builder()
                .project(project).user(user).role(ProjectRole.VIEWER).build());

        String token = login("carol@example.com", "pass");

        // Act
        ResponseEntity<Void> response = getProject(ws.getId(), project.getId(), token);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getProject_withValidToken_nonMember_returns403() {
        User user = userRepository.save(User.builder()
                .email("dave@example.com")
                .passwordHash(encoder.encode("pass"))
                .build());
        Workspace ws = workspaceRepository.save(Workspace.builder().name("WS-jwt-2").build());
        Project project = projectRepository.save(Project.builder().workspace(ws).name("P-jwt-2").build());
        // user has NO project membership

        String token = login("dave@example.com", "pass");

        ResponseEntity<Void> response = getProject(ws.getId(), project.getId(), token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN); // AccessDenied Exception
    }

    @Test
    void getProject_withInvalidToken_returns401() {
        Workspace ws = workspaceRepository.save(Workspace.builder().name("WS-jwt-3").build());
        Project project = projectRepository.save(Project.builder().workspace(ws).name("P-jwt-3").build());

        ResponseEntity<Void> response = getProject(ws.getId(), project.getId(), "not.a.valid.token");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void apiKeyEndpoint_rejectsJwtToken() {
        // Ingestion endpoint requires X-API-Key, not Bearer token
        User user = userRepository.save(User.builder()
                .email("eve@example.com")
                .passwordHash(encoder.encode("pass"))
                .build());
        Workspace ws = workspaceRepository.save(Workspace.builder().name("WS-jwt-4").build());
        Project project = projectRepository.save(Project.builder().workspace(ws).name("P-jwt-4").build());

        String token = login("eve@example.com", "pass");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
            {
            "type":"ev",
            "payload":{"k":"v"},
            "idempotencyKey":"k1"
            }
            """;
        String url = "/api/workspaces/" + ws.getId() + "/projects/" + project.getId() + "/events";
        ResponseEntity<Void> response = restTemplate.postForEntity(
                url, new HttpEntity<>(body, headers), Void.class);

        // JWT is not a valid API key — should be 401
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String login(String email, String password) {
        LoginResponse body = restTemplate
                .postForEntity("/auth/login", new LoginRequest(email, password), LoginResponse.class)
                .getBody();
        assertThat(body).isNotNull();
        return body.token();
    }

    private ResponseEntity<Void> getProject(UUID workspaceId, UUID projectId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        String url = "/api/workspaces/" + workspaceId + "/projects/" + projectId;
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Void.class);
    }
}
