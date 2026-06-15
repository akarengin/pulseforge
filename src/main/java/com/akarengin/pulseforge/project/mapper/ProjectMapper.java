package com.akarengin.pulseforge.project.mapper;

import com.akarengin.pulseforge.project.dto.ProjectResponse;
import com.akarengin.pulseforge.project.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(source = "workspace.id", target = "workspaceId")
    ProjectResponse toResponse(Project project);

    List<ProjectResponse> toResponseList(List<Project> projects);
}
