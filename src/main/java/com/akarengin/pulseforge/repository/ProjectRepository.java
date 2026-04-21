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
    
    // The method uses Spring Data JPA's query derivation to automatically generate the SQL query
    // The underscore notation (Workspace_Id) navigates the relationship to the workspace entity's id field
    Optional<Project> findByWorkspace_IdAndId(UUID workspaceId, UUID id);

    boolean existsByWorkspace_IdAndName(UUID workspaceId, String name);
}
