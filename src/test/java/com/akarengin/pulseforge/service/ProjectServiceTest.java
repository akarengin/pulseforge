package com.akarengin.pulseforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.akarengin.pulseforge.entity.Project;
import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.repository.ProjectRepository;
import com.akarengin.pulseforge.repository.WorkspaceRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProject_success() {
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = Workspace.builder().id(workspaceId).name("W").build();
        
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(projectRepository.existsByWorkspace_IdAndName(workspaceId, "P")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(i -> i.getArgument(0));

        Project project = projectService.createProject(workspaceId, "P");

        assertNotNull(project);
        assertEquals("P", project.getName());
        assertEquals(workspace, project.getWorkspace());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void createProject_workspaceNotFound_throwsException() {
        UUID workspaceId = UUID.randomUUID();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(workspaceId, "P"));
    }

    @Test
    void createProject_duplicateName_throwsException() {
        UUID workspaceId = UUID.randomUUID();
        Workspace workspace = Workspace.builder().id(workspaceId).name("W").build();
        
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(projectRepository.existsByWorkspace_IdAndName(workspaceId, "P")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(workspaceId, "P"));
    }
}
