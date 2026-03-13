package com.akarengin.pulseforge.repository;

import com.akarengin.pulseforge.entity.Membership;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    @Query("SELECT m FROM Membership m WHERE m.workspace.id = :workspaceId")
    List<Membership> findByWorkspace_Id(UUID workspaceId);

    Optional<Membership> findByWorkspace_IdAndUser_Id(UUID workspaceId, UUID userId);

}
