package ru.practicum.workshop.eventservice.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.workshop.eventservice.dto.EventRequest;
import ru.practicum.workshop.eventservice.dto.EventResponse;
import ru.practicum.workshop.eventservice.error.ForbiddenException;
import ru.practicum.workshop.eventservice.error.NotFoundException;
import ru.practicum.workshop.eventservice.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventServiceTest {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private EventRequest validEventRequest;

    @BeforeEach
    void setup() {
        validEventRequest = new EventRequest(
                "Test Event",
                "Description",
                LocalDateTime.of(2024, 12, 1, 10, 0),
                LocalDateTime.of(2024, 12, 1, 12, 0),
                "Online"
        );
    }

    @Test
    void createEvent_shouldSaveEvent() {
        EventResponse event = eventService.createEvent(validEventRequest, 1L);

        assertNotNull(event.getId());
        assertEquals("Test Event", event.getName());
        assertEquals("Description", event.getDescription());
        assertEquals(1L, event.getOwnerId());
    }

    @Test
    void updateEvent_shouldUpdateEventDetails() {
        EventResponse event = eventService.createEvent(validEventRequest, 1L);

        EventRequest updatedRequest = new EventRequest(
                "Updated Event",
                "New Description",
                LocalDateTime.of(2024, 12, 1, 13, 0),
                LocalDateTime.of(2024, 12, 1, 15, 0),
                "New Location"
        );

        EventResponse updatedEvent = eventService.updateEvent(event.getId(), updatedRequest, 1L);

        assertEquals("Updated Event", updatedEvent.getName());
        assertEquals("New Description", updatedEvent.getDescription());
        assertEquals("New Location", updatedEvent.getLocation());
    }

    @Test
    void updateEvent_shouldThrowForbiddenException() {
        EventResponse event = eventService.createEvent(validEventRequest, 1L);

        EventRequest updatedRequest = new EventRequest(
                "Updated Event",
                "New Description",
                LocalDateTime.of(2024, 12, 1, 13, 0),
                LocalDateTime.of(2024, 12, 1, 15, 0),
                "New Location"
        );

        assertThrows(ForbiddenException.class, () -> {
            eventService.updateEvent(event.getId(), updatedRequest, 2L);
        });
    }

    @Test
    void getEvent_shouldReturnEvent() {
        EventResponse event = eventService.createEvent(validEventRequest, 1L);

        EventResponse fetchedEvent = eventService.getEvent(event.getId(), 1L);

        assertEquals(event.getId(), fetchedEvent.getId());
        assertEquals("Test Event", fetchedEvent.getName());
        assertEquals("Description", fetchedEvent.getDescription());
    }

    @Test
    void getEvent_shouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> {
            eventService.getEvent(999L, 1L);
        });
    }

    @Test
    void getEvent_shouldHideCreatedDateTimeForNonOwner() {
        EventResponse event = eventService.createEvent(validEventRequest, 1L);

        EventResponse fetchedEvent = eventService.getEvent(event.getId(), null);

        assertNull(fetchedEvent.getCreatedDateTime());
    }

    @Test
    void deleteEvent_shouldRemoveEvent() {
        EventResponse event = eventService.createEvent(validEventRequest, 1L);

        eventService.deleteEvent(event.getId(), 1L);

        assertFalse(eventRepository.findById(event.getId()).isPresent());
    }

    @Test
    void deleteEvent_shouldThrowForbiddenException() {
        EventResponse event = eventService.createEvent(validEventRequest, 1L);

        assertThrows(ForbiddenException.class, () -> {
            eventService.deleteEvent(event.getId(), 2L);
        });
    }

    @Test
    void getEventsWithPagination_shouldReturnCorrectResults() {
        EventResponse event1 = eventService.createEvent(validEventRequest, 1L);
        EventRequest anotherRequest = new EventRequest(
                "Another Event",
                "Another Description",
                LocalDateTime.of(2024, 12, 2, 10, 0),
                LocalDateTime.of(2024, 12, 2, 12, 0),
                "Offline"
        );
        EventResponse event2 = eventService.createEvent(anotherRequest, 2L);

        List<EventResponse> events = eventService.getEvents(0, 2, 1L, 1L);

        assertEquals(1, events.size());
        assertEquals(event1.getId(), events.get(0).getId());
    }
}
