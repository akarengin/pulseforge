package com.akarengin.pulseforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.akarengin.pulseforge.entity.Membership;
import com.akarengin.pulseforge.entity.User;
import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.repository.MembershipRepository;
import com.akarengin.pulseforge.repository.UserRepository;
import com.akarengin.pulseforge.repository.WorkspaceRepository;
import java.util.List;
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
class MembershipServiceTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MembershipService membershipService;

    @Captor
    private ArgumentCaptor<Membership> membershipCaptor;

    @Test
    void addUserToWorkspace_happyPath_savesMembership() {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var userId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        var workspace = Workspace.builder().id(workspaceId).name("acme").build();
        var user = User.builder().id(userId).email("a@b.com").build();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(membershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)).thenReturn(Optional.empty());
        when(membershipRepository.save(any(Membership.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Membership result = membershipService.addUserToWorkspace(workspaceId, userId, "ADMIN");

        verify(membershipRepository).save(membershipCaptor.capture());
        Membership saved = membershipCaptor.getValue();

        assertThat(saved.getWorkspace()).isEqualTo(workspace);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getRole()).isEqualTo("ADMIN");
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void addUserToWorkspace_whenWorkspaceMissing_throwsAndDoesNotSave() {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var userId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.addUserToWorkspace(workspaceId, userId, "ADMIN"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Workspace not found for ID " + workspaceId);

        verify(membershipRepository, never()).save(any());
    }

    @Test
    void addUserToWorkspace_whenUserMissing_throwsAndDoesNotSave() {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var userId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(Workspace.builder().id(workspaceId).build()));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> membershipService.addUserToWorkspace(workspaceId, userId, "ADMIN"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User not found for ID " + userId);

        verify(membershipRepository, never()).save(any());
    }

    @Test
    void addUserToWorkspace_whenAlreadyMember_throwsAndDoesNotSave() {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var userId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(Workspace.builder().id(workspaceId).build()));
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(membershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)).thenReturn(Optional.of(Membership.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000099")).build()));

        assertThatThrownBy(() -> membershipService.addUserToWorkspace(workspaceId, userId, "ADMIN"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User already member of workspace");

        verify(membershipRepository, never()).save(any());
    }

    @Test
    void getWorkspaceMembers_delegatesToRepository() {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(membershipRepository.findByWorkspace_Id(workspaceId)).thenReturn(List.of(Membership.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).build()));

        List<Membership> result = membershipService.getWorkspaceMembers(workspaceId);

        assertThat(result).hasSize(1);
    }

    @Test
    void isMember_returnsTrueWhenPresent() {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var userId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(membershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)).thenReturn(Optional.of(Membership.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).build()));

        assertThat(membershipService.isMember(workspaceId, userId)).isTrue();
    }

    @Test
    void isMember_returnsFalseWhenMissing() {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var userId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(membershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)).thenReturn(Optional.empty());

        assertThat(membershipService.isMember(workspaceId, userId)).isFalse();
    }

    @Test
    void getUserRole_returnsRoleWhenPresent() {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var userId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(membershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)).thenReturn(Optional.of(Membership.builder().role("ADMIN").build()));

        assertThat(membershipService.getUserRole(workspaceId, userId)).contains("ADMIN");
    }

    @Test
    void getUserRole_returnsEmptyWhenMissing() {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var userId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(membershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)).thenReturn(Optional.empty());

        assertThat(membershipService.getUserRole(workspaceId, userId)).isEmpty();
    }
}
