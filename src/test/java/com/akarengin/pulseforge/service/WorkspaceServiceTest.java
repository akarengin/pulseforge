package com.akarengin.pulseforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.repository.WorkspaceRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @InjectMocks
    private WorkspaceService workspaceService;

    @Captor
    private ArgumentCaptor<Workspace> workspaceCaptor;

    @Test
    void createWorkspace_buildsAndSavesWorkspace() {
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Workspace result = workspaceService.createWorkspace("acme");

        verify(workspaceRepository).save(workspaceCaptor.capture());
        Workspace saved = workspaceCaptor.getValue();

        assertThat(saved.getName()).isEqualTo("acme");
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void getWorkspaceById_delegatesToRepository() {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var workspace = Workspace.builder().id(workspaceId).name("acme").build();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        Optional<Workspace> result = workspaceService.getWorkspaceById(workspaceId);

        assertThat(result).contains(workspace);
    }

    @Test
    void getWorkspaceByName_delegatesToRepository() {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var workspace = Workspace.builder().id(workspaceId).name("acme").build();
        when(workspaceRepository.findByName("acme")).thenReturn(Optional.of(workspace));

        Optional<Workspace> result = workspaceService.getWorkspaceByName("acme");

        assertThat(result).contains(workspace);
    }
}
