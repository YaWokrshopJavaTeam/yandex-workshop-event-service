package ru.practicum.workshop.eventservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.workshop.eventservice.dto.EventRequest;
import ru.practicum.workshop.eventservice.dto.EventResponse;
import ru.practicum.workshop.eventservice.error.ForbiddenException;
import ru.practicum.workshop.eventservice.error.NotFoundException;
import ru.practicum.workshop.eventservice.mapper.EventMapper;
import ru.practicum.workshop.eventservice.model.Event;
import ru.practicum.workshop.eventservice.repository.EventRepository;
import ru.practicum.workshop.eventservice.service.EventService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    public EventResponse createEvent(EventRequest request, Long requesterId) {
        Event event = eventMapper.toCreatingModel(request, requesterId);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDto(savedEvent);
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
        return eventMapper.toDto(updatedEvent);
    }

    @Override
    public EventResponse getEvent(Long id, Long requesterId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        EventResponse response = eventMapper.toDto(event);
        if (!event.getOwnerId().equals(requesterId)) {
            response.setCreatedDateTime(null);
        }
        return response;
    }

    @Override
    public List<EventResponse> getEvents(int page, int size, Long requesterId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDateTime");
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        if (requesterId != null) {
            return eventRepository.findByOwnerId(requesterId, pageRequest).stream()
                    .map(eventMapper::toDto)
                    .collect(Collectors.toList());
        } else {
            return eventRepository.findAll(pageRequest).stream()
                    .peek(event -> event.setCreatedDateTime(null))
                    .map(eventMapper::toDto)
                    .collect(Collectors.toList());
        }

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
}
