package com.akarengin.pulseforge.project.mapper;

import com.akarengin.pulseforge.project.dto.ProjectMembershipResponse;
import com.akarengin.pulseforge.project.entity.ProjectMembership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMembershipMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "userId", source = "user.id")
    ProjectMembershipResponse toResponse(ProjectMembership membership);

    List<ProjectMembershipResponse> toResponseList(List<ProjectMembership> memberships);
}
