package ru.practicum.workshop.eventservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.workshop.eventservice.dto.EventRequest;
import ru.practicum.workshop.eventservice.dto.EventResponse;
import ru.practicum.workshop.eventservice.error.ForbiddenException;
import ru.practicum.workshop.eventservice.error.NotFoundException;
import ru.practicum.workshop.eventservice.mapper.EventMapper;
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

    @Override
    public EventResponse createEvent(EventRequest request, Long requesterId) {
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
    public List<EventResponse> getEvents(int page, int size, Long ownerId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDateTime");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        List<Event> events;
        if (ownerId != null) {
            events = eventRepository.findByOwnerId(ownerId, pageRequest).getContent();
        } else {
            events = eventRepository.findAll(pageRequest).getContent();
        }
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
