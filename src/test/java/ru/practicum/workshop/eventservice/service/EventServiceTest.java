package ru.practicum.workshop.eventservice.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.workshop.eventservice.dto.EventRequest;
import ru.practicum.workshop.eventservice.dto.EventResponse;
import ru.practicum.workshop.eventservice.dto.UserDto;
import ru.practicum.workshop.eventservice.error.ForbiddenException;
import ru.practicum.workshop.eventservice.error.NotFoundException;
import ru.practicum.workshop.eventservice.repository.EventRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.workshop.eventservice.UserMock.setupMockGetUserById;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventServiceTest {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private EventRequest validEventRequest;
    private static WireMockServer mockUserServer;

    private UserDto userDto;
    private long userId = 1L;

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

    @BeforeEach
    void stubMockUser() throws IOException {
        userDto = createUserDto(userId);
        setupMockGetUserById(mockUserServer, userId, userDto);
    }

    @BeforeAll
    static void beforeAll() {
        mockUserServer = new WireMockServer(8081);
        configureFor("localhost", 8081);
        mockUserServer.start();
    }

    private UserDto createUserDto(Long userId) {
        return UserDto.builder()
                .id(userId)
                .email("email@email.com")
                .name("name")
                .aboutMe("about me")
                .build();
    }

    @Test
    void createEvent_shouldSaveEvent() {
        EventResponse event = eventService.createEvent(validEventRequest, userId);

        assertNotNull(event.getId());
        assertEquals("Test Event", event.getName());
        assertEquals("Description", event.getDescription());
        assertEquals(userId, event.getOwnerId());
    }

    @Test
    void updateEvent_shouldUpdateEventDetails() {
        EventResponse event = eventService.createEvent(validEventRequest, userId);

        EventRequest updatedRequest = new EventRequest(
                "Updated Event",
                "New Description",
                LocalDateTime.of(2024, 12, 1, 13, 0),
                LocalDateTime.of(2024, 12, 1, 15, 0),
                "New Location"
        );

        EventResponse updatedEvent = eventService.updateEvent(event.getId(), updatedRequest, userId);

        assertEquals("Updated Event", updatedEvent.getName());
        assertEquals("New Description", updatedEvent.getDescription());
        assertEquals("New Location", updatedEvent.getLocation());
    }

    @Test
    void updateEvent_shouldThrowForbiddenException() {
        EventResponse event = eventService.createEvent(validEventRequest, userId);

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
        EventResponse event = eventService.createEvent(validEventRequest, userId);

        EventResponse fetchedEvent = eventService.getEvent(event.getId(), userId);

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
        EventResponse event = eventService.createEvent(validEventRequest, userId);

        EventResponse fetchedEvent = eventService.getEvent(event.getId(), null);

        assertNull(fetchedEvent.getCreatedDateTime());
    }

    @Test
    void deleteEvent_shouldRemoveEvent() {
        EventResponse event = eventService.createEvent(validEventRequest, userId);

        eventService.deleteEvent(event.getId(), userId);

        assertFalse(eventRepository.findById(event.getId()).isPresent());
    }

    @Test
    void deleteEvent_shouldThrowForbiddenException() {
        EventResponse event = eventService.createEvent(validEventRequest, userId);

        assertThrows(ForbiddenException.class, () -> {
            eventService.deleteEvent(event.getId(), 2L);
        });
    }

    @Test
    void getEventsWithPagination_shouldReturnCorrectResults() throws IOException {
        EventResponse event1 = eventService.createEvent(validEventRequest, userId);
        EventRequest anotherRequest = new EventRequest(
                "Another Event",
                "Another Description",
                LocalDateTime.of(2024, 12, 2, 10, 0),
                LocalDateTime.of(2024, 12, 2, 12, 0),
                "Offline"
        );

        userDto = createUserDto(++userId);
        setupMockGetUserById(mockUserServer, userId, userDto);
        EventResponse event2 = eventService.createEvent(anotherRequest, userId);

        List<EventResponse> events = eventService.getEvents(0, 2, 1L);

        assertEquals(1, events.size());
        assertEquals(event1.getId(), events.get(0).getId());
    }

    @AfterAll
    static void tearDown() {
        mockUserServer.stop();
    }
}
