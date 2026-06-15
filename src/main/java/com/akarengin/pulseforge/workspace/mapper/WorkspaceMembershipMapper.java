package com.akarengin.pulseforge.workspace.mapper;

import com.akarengin.pulseforge.workspace.dto.WorkspaceMembershipResponse;
import com.akarengin.pulseforge.workspace.entity.WorkspaceMembership;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkspaceMembershipMapper {

    public WorkspaceMembershipResponse toResponse(WorkspaceMembership membership) {
        return new WorkspaceMembershipResponse(
                membership.getId(),
                membership.getWorkspace().getId(),
                membership.getUser().getId(),
                membership.getUser().getEmail(),
                membership.getRole().name(),
                membership.getCreatedAt()
        );
    }

    public List<WorkspaceMembershipResponse> toResponseList(List<WorkspaceMembership> memberships) {
        return memberships.stream()
                .map(this::toResponse)
                .toList();
    }
}
