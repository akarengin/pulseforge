package com.akarengin.pulseforge.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.akarengin.pulseforge.dto.EventRequest;
import com.akarengin.pulseforge.dto.EventResponse;
import com.akarengin.pulseforge.entity.Event;
import com.akarengin.pulseforge.entity.Workspace;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EventMapper {

    @Mapping(target = "workspaceId", source = "workspace.id")
    EventResponse toResponse(Event event);

    List<EventResponse> toResponseList(List<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    Event toEntity(EventRequest request, Workspace workspace);

}
