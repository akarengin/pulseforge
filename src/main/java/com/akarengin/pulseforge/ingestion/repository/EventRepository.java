package com.akarengin.pulseforge.ingestion.repository;

import com.akarengin.pulseforge.ingestion.entity.Event;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for Event entity. All queries MUST include workspace_id AND project_id filtering
 * to prevent cross-tenant and cross-project data leakage.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByWorkspace_IdAndProject_Id(UUID workspaceId, UUID projectId);

    @Query("SELECT e FROM Event e WHERE e.workspace.id = :workspaceId AND e.project.id = :projectId AND e.timestamp BETWEEN :startTime AND :endTime ORDER BY e.timestamp DESC")
    List<Event> findByWorkspaceAndProjectAndTimeRange(UUID workspaceId, UUID projectId,
        Instant startTime, Instant endTime);

    @Query("SELECT e FROM Event e WHERE e.workspace.id = :workspaceId AND e.project.id = :projectId AND e.type = :type")
    List<Event> findByWorkspace_IdAndProject_IdAndType(UUID workspaceId, UUID projectId, String type);

}
