package com.akarengin.pulseforge.mapper;

import com.akarengin.pulseforge.dto.ProjectResponse;
import com.akarengin.pulseforge.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(source = "workspace.id", target = "workspaceId")
    ProjectResponse toResponse(Project project);

    List<ProjectResponse> toResponseList(List<Project> projects);
}
