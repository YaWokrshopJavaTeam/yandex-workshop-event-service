package ru.practicum.workshop.eventservice.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import ru.practicum.workshop.eventservice.dto.EventRequest;
import ru.practicum.workshop.eventservice.dto.EventResponse;
import ru.practicum.workshop.eventservice.model.Event;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {LocalDateTime.class})
public interface EventMapper {

    @Mapping(target = "ownerId", source = "requesterId")
    Event toModel(EventRequest eventRequest, Long requesterId);

    @Mapping(target = "ownerId", source = "requesterId")
    @Mapping(target = "createdDateTime", expression = "java(LocalDateTime.now())")
    Event toCreatingModel(EventRequest eventRequest, Long requesterId);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Event updateEvent(EventRequest eventRequest, @MappingTarget Event event);

    EventResponse toDto(Event event);
}
