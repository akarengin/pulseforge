package com.akarengin.pulseforge.ingestion.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.akarengin.pulseforge.ingestion.dto.EventRequest;
import com.akarengin.pulseforge.ingestion.dto.EventResponse;
import com.akarengin.pulseforge.ingestion.entity.Event;
import com.akarengin.pulseforge.project.entity.Project;
import com.akarengin.pulseforge.workspace.entity.Workspace;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EventMapper {

    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "projectId", source = "project.id")
    EventResponse toResponse(Event event);

    List<EventResponse> toResponseList(List<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "project", source = "project")
    @Mapping(target = "idempotencyKey", source = "request.idempotencyKey")
    Event toEntity(EventRequest request, Workspace workspace, Project project);

}