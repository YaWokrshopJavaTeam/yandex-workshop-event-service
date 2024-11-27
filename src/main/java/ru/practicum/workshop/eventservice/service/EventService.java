package ru.practicum.workshop.eventservice.service;

import ru.practicum.workshop.eventservice.dto.EventRequest;
import ru.practicum.workshop.eventservice.dto.EventResponse;
import ru.practicum.workshop.eventservice.model.Event;

import java.util.List;

public interface EventService {
    EventResponse createEvent(EventRequest request, Long requesterId);
    EventResponse updateEvent(Long id, EventRequest request, Long requesterId);
    EventResponse getEvent(Long id, Long requesterId);
    List<EventResponse> getEvents(int page, int size, Long requesterId, Long ownerId);
    void deleteEvent(Long id, Long requesterId);

    Event getEventInternal(Long eventId);
}
