package ru.practicum.workshop.eventservice.controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import ru.practicum.workshop.eventservice.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.workshop.eventservice.service.EventService;
import java.util.List;

@RestController
@RequestMapping("/events")
@Validated
@Slf4j
public class EventController {
    @Autowired
    private EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@Valid @RequestBody EventRequest request,
                                     @RequestHeader("X-User-Id") Long requesterId) {
        log.info("Request: create event by user(id={}), request={}", requesterId, request);
        return eventService.createEvent(request, requesterId);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventResponse updateEvent(@PathVariable Long id,
                                     @Valid @RequestBody EventRequest request,
                                     @RequestHeader("X-User-Id") Long requesterId) {
        return eventService.updateEvent(id, request, requesterId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventResponse getEvent(@PathVariable Long id,
                                  @RequestHeader(value = "X-User-Id") Long requesterId) {
        return eventService.getEvent(id, requesterId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventResponse> getEvents(@RequestParam @PositiveOrZero int page,
                                         @RequestParam @Positive int size,
                                         @RequestParam(value = "ownerId", required = false) Long ownerId) {
        return eventService.getEvents(page, size, ownerId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id,
                            @RequestHeader("X-User-Id") Long requesterId) {
        eventService.deleteEvent(id, requesterId);
    }
}

