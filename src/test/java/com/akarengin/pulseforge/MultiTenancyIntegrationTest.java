package com.akarengin.pulseforge;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.akarengin.pulseforge.ingestion.entity.Event;
import com.akarengin.pulseforge.ingestion.repository.EventRepository;
import com.akarengin.pulseforge.project.entity.Project;
import com.akarengin.pulseforge.project.repository.ProjectRepository;
import com.akarengin.pulseforge.workspace.entity.Workspace;
import com.akarengin.pulseforge.workspace.repository.WorkspaceRepository;

/**
 * Integration tests for multi-tenancy isolation at repository and service layers.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class MultiTenancyIntegrationTest {

        @Container
        static final PostgreSQLContainer<?> postgres =
                        new PostgreSQLContainer<>("postgres:16-alpine");

        @DynamicPropertySource
        static void configurePostgres(DynamicPropertyRegistry registry) {
                registry.add("spring.datasource.url", postgres::getJdbcUrl);
                registry.add("spring.datasource.username", postgres::getUsername);
                registry.add("spring.datasource.password", postgres::getPassword);
                registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
                registry.add("spring.flyway.enabled", () -> true);
                registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
                registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        }

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EventRepository eventRepository;

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

        eventRepository.save(Event.builder()
                .workspace(ws1)
                .project(project1)
                .type("click")
                .payload(new HashMap<>())
                .idempotencyKey("key1")
                .build());
        eventRepository.save(Event.builder()
                .workspace(ws2)
                .project(project2)
                .type("click")
                .payload(new HashMap<>())       
                .idempotencyKey("key2")
                .build());

        List<Event> ws1Events = eventRepository.findByWorkspace_IdAndProject_Id(
                ws1.getId(), project1.getId());
        List<Event> ws2Events = eventRepository.findByWorkspace_IdAndProject_Id(
                ws2.getId(), project2.getId());

        // Assert: each workspace sees only its own event
        assertThat(ws1Events)
                .extracting(event -> event.getWorkspace().getId())
                .containsOnly(ws1.getId());
        assertThat(ws2Events)
                .extracting(event -> event.getWorkspace().getId())
                .containsOnly(ws2.getId());
    }

}
