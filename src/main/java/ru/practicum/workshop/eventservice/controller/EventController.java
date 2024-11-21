package ru.practicum.workshop.eventservice.controller;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import ru.practicum.workshop.eventservice.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.workshop.eventservice.service.EventService;
import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
public class EventController {
    @Autowired
    private EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse createEvent(@RequestBody EventRequest request,
                                     @RequestHeader("X-User-Id") Long requesterId) {
        return eventService.createEvent(request, requesterId);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventResponse updateEvent(@PathVariable Long id, @RequestBody EventRequest request,
                                     @RequestHeader("X-User-Id") Long requesterId) {
        return eventService.updateEvent(id, request, requesterId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventResponse getEvent(@PathVariable Long id,
                                  @RequestHeader(value = "X-User-Id", required = false) Long requesterId) {
        return eventService.getEvent(id, requesterId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventResponse> getEvents(@RequestParam @Positive int page, @RequestParam @Positive int size,
                                         @RequestHeader(value = "X-User-Id", required = false) Long requesterId) {
        return eventService.getEvents(page, size, requesterId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id,
                            @RequestHeader("X-User-Id") Long requesterId) {
        eventService.deleteEvent(id, requesterId);
    }
}

