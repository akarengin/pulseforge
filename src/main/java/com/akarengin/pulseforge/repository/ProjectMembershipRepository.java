package com.akarengin.pulseforge.repository;

import com.akarengin.pulseforge.entity.ProjectMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, UUID> {
    
    Optional<ProjectMembership> findByProject_IdAndUser_Id(UUID projectId, UUID userId);
    
    List<ProjectMembership> findByProject_Id(UUID projectId);
}
