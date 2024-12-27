package ru.practicum.workshop.eventservice.mapper;

import org.mapstruct.*;
import ru.practicum.workshop.eventservice.dto.EventRequest;
import ru.practicum.workshop.eventservice.dto.EventResponse;
import ru.practicum.workshop.eventservice.error.BadRequest;
import ru.practicum.workshop.eventservice.model.Event;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {LocalDateTime.class})
public interface EventMapper {

    @Mapping(target = "ownerId", source = "requesterId")
    Event toModel(EventRequest eventRequest, Long requesterId);

    @Mapping(target = "ownerId", source = "requesterId")
    @Mapping(target = "createdDateTime", expression = "java(LocalDateTime.now())")
    Event toCreatingModel(EventRequest eventRequest, Long requesterId);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "limited", expression = "java(updateIsLimited(eventRequest, event))")
    @Mapping(target = "participantLimit", expression = "java(updateParticipantLimit(eventRequest, event))")
    Event updateEvent(EventRequest eventRequest, @MappingTarget Event event);

    EventResponse toDtoWithCreateDateTime(Event event);

    @Named("toEventDtoPublic")
    @Mapping(target = "createdDateTime", ignore = true)
    EventResponse toDtoWithoutCreateDateTime(Event event);

    @IterableMapping(qualifiedByName = "toEventDtoPublic")
    List<EventResponse> toEventsDtoPublic(List<Event> events);

    default boolean updateIsLimited(EventRequest eventRequest, Event event) {
        if (!event.isLimited() && eventRequest.isLimited()) {
            throw new BadRequest("The event participant limit cannot be reduced");
        } else return eventRequest.isLimited();
    }

    default Integer updateParticipantLimit(EventRequest eventRequest, Event event) {
        if (event.isLimited() && eventRequest.getParticipantLimit() != null
                && eventRequest.getParticipantLimit() < event.getParticipantLimit()) {
            throw new BadRequest("The event participant limit cannot be reduced");
        } else return eventRequest.getParticipantLimit();
    }
}
