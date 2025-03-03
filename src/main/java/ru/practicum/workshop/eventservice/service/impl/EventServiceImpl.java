package ru.practicum.workshop.eventservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.workshop.eventservice.client.UserClient;
import ru.practicum.workshop.eventservice.dto.EventRequest;
import ru.practicum.workshop.eventservice.dto.EventResponse;
import ru.practicum.workshop.eventservice.error.ForbiddenException;
import ru.practicum.workshop.eventservice.error.NotFoundException;
import ru.practicum.workshop.eventservice.mapper.EventMapper;
import ru.practicum.workshop.eventservice.params.EventSearchParam;
import ru.practicum.workshop.eventservice.repository.EventRepository;
import ru.practicum.workshop.eventservice.service.EventService;
import ru.practicum.workshop.eventservice.model.Event;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserClient userClient;

    private void checkUserForbidden(Long userId) {
        log.info("Getting user from User Service by id={}", userId);
        try {
            userClient.getUserById(userId);
        } catch (EntityNotFoundException ex) {
            throw new ForbiddenException("You can't create an event. Please log in to your account");
        }
    }

    @Override
    public EventResponse createEvent(EventRequest request, Long requesterId) {
        checkUserForbidden(requesterId);
        Event event = eventMapper.toCreatingModel(request, requesterId);
        Event savedEvent = eventRepository.save(event);

        log.info("Event created: {}", event);

        return eventMapper.toDtoWithCreateDateTime(savedEvent);
    }

    @Override
    public EventResponse updateEvent(Long id, EventRequest request, Long requesterId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!event.getOwnerId().equals(requesterId)) {
            throw new ForbiddenException("Not authorized to update this event");
        }
        Event newEvent = eventMapper.updateEvent(request, event);
        Event updatedEvent = eventRepository.save(newEvent);
        return eventMapper.toDtoWithCreateDateTime(updatedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEvent(Long id, Long requesterId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!event.getOwnerId().equals(requesterId)) {
            return eventMapper.toDtoWithoutCreateDateTime(event);
        }
        return eventMapper.toDtoWithCreateDateTime(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEvents(EventSearchParam param) {
        List<Event> events = eventRepository.getEvents(param);
        return eventMapper.toEventsDtoPublic(events);
    }

    @Override
    public void deleteEvent(Long id, Long requesterId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!event.getOwnerId().equals(requesterId)) {
            throw new ForbiddenException("Not authorized to delete this event");
        }
        eventRepository.delete(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Event getEventInternal(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Event with id=%d not found.", eventId)));
    }
}
