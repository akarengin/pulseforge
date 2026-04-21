package com.akarengin.pulseforge.repository;

import com.akarengin.pulseforge.entity.WorkspaceMembership;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceMembershipRepository extends JpaRepository<WorkspaceMembership, UUID> {

    @Query("SELECT m FROM WorkspaceMembership m WHERE m.workspace.id = :workspaceId")
    List<WorkspaceMembership> findByWorkspace_Id(UUID workspaceId);

    Optional<WorkspaceMembership> findByWorkspace_IdAndUser_Id(UUID workspaceId, UUID userId);

}
