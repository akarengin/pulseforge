package com.akarengin.pulseforge.repository;

import com.akarengin.pulseforge.entity.Workspace;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {

    @Query("SELECT w FROM Workspace w WHERE w.name = :name")
    Optional<Workspace> findByName(String name);

}

