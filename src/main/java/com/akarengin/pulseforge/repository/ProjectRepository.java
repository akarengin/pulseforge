package com.akarengin.pulseforge.repository;

import com.akarengin.pulseforge.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    
    List<Project> findByWorkspace_Id(UUID workspaceId);
    
    Optional<Project> findByWorkspace_IdAndId(UUID workspaceId, UUID id);
    
    boolean existsByWorkspace_IdAndName(UUID workspaceId, String name);
}
