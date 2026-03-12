package com.akarengin.pulseforge.repository;

import com.akarengin.pulseforge.entity.Event;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for Event entity. All queries MUST include workspace_id filtering to prevent
 * cross-tenant data leakage.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    List<Event> findByWorkspace_Id(UUID workspaceId);

    @Query("SELECT e FROM Event e WHERE e.workspace.id = :workspaceId AND e.timestamp BETWEEN :startTime AND :endTime ORDER BY e.timestamp DESC")
    List<Event> findByWorkspaceAndTimeRange(UUID workspaceId,
        Instant startTime,
        Instant endTime);

    @Query("SELECT e FROM Event e WHERE e.workspace.id = :workspaceId AND e.type = :type")
    List<Event> findByWorkspace_IdAndType(UUID workspaceId, String type);

}
