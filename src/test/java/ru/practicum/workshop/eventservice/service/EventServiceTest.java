package ru.practicum.workshop.eventservice.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.workshop.eventservice.dto.EventRequest;
import ru.practicum.workshop.eventservice.dto.EventResponse;
import ru.practicum.workshop.eventservice.client.dto.UserDto;
import ru.practicum.workshop.eventservice.error.BadRequest;
import ru.practicum.workshop.eventservice.error.ForbiddenException;
import ru.practicum.workshop.eventservice.error.NotFoundException;
import ru.practicum.workshop.eventservice.model.EventRegistrationStatus;
import ru.practicum.workshop.eventservice.params.EventSearchParam;
import ru.practicum.workshop.eventservice.repository.EventRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.workshop.eventservice.UserMock.setupMockGetUserById;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
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
                "Online",
                EventRegistrationStatus.OPEN,
                false,
                null
        );
    }

    @BeforeEach
    void stubMockUser() throws IOException {
        userDto = createUserDto(userId);
        setupMockGetUserById(mockUserServer, userId, userDto);
    }

    @BeforeAll
    static void beforeAll() {
        mockUserServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        mockUserServer.start();
        log.info("Mock-server started on port {}.", mockUserServer.port());
        configureFor("localhost", mockUserServer.port());
    }

    @DynamicPropertySource
    static void setUserServiceUrl(DynamicPropertyRegistry registry) {
        registry.add("userservice.url", () -> "localhost:" + mockUserServer.port());
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
                "New Location",
                EventRegistrationStatus.OPEN,
                false,
                null
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
                "New Location",
                EventRegistrationStatus.OPEN,
                false,
                null
        );

        assertThrows(ForbiddenException.class, () -> {
            eventService.updateEvent(event.getId(), updatedRequest, 2L);
        });
    }

    @Test
    void updateEvent_shouldThrowBadRequestException_WhenIsLimitedWasFalse() {
        EventResponse event = eventService.createEvent(validEventRequest, userId);

        EventRequest updatedRequest = new EventRequest(
                "Updated Event",
                "New Description",
                LocalDateTime.now().plusHours(4),
                LocalDateTime.now().plusDays(4),
                "New Location",
                EventRegistrationStatus.OPEN,
                true,
                1
        );

        assertThrows(BadRequest.class, () -> {
            eventService.updateEvent(event.getId(), updatedRequest, userId);
        });
    }

    @Test
    void updateEvent_shouldThrowBadRequestException_WhenIsLimitedWasTrue() {
        validEventRequest = new EventRequest(
                "Test Event",
                "Description",
                LocalDateTime.of(2024, 12, 1, 10, 0),
                LocalDateTime.of(2024, 12, 1, 12, 0),
                "Online",
                EventRegistrationStatus.OPEN,
                true,
                10
        );
        EventResponse event = eventService.createEvent(validEventRequest, userId);

        EventRequest updatedRequest = new EventRequest(
                "Updated Event",
                "New Description",
                LocalDateTime.now().plusHours(4),
                LocalDateTime.now().plusDays(4),
                "New Location",
                EventRegistrationStatus.OPEN,
                true,
                1
        );

        assertThrows(BadRequest.class, () -> {
            eventService.updateEvent(event.getId(), updatedRequest, userId);
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
                "Offline",
                EventRegistrationStatus.OPEN,
                false,
                null
        );

        userDto = createUserDto(++userId);
        setupMockGetUserById(mockUserServer, userId, userDto);
        EventResponse event2 = eventService.createEvent(anotherRequest, userId);

        EventSearchParam param = EventSearchParam.builder()
                .pageable(PageRequest.of(0, 2))
                .ownerId(1L)
                .status(EventRegistrationStatus.OPEN)
                .build();
        List<EventResponse> events = eventService.getEvents(param);

        assertEquals(1, events.size());
        assertEquals(event1.getId(), events.get(0).getId());
    }

    private EventResponse createEventWithStatus(long userId, UserDto userDto, EventRegistrationStatus status) throws IOException {
        EventRequest request = new EventRequest(
                "Another Event",
                "Another Description",
                LocalDateTime.now().plusHours(4),
                LocalDateTime.now().plusDays(4),
                "Offline",
                status,
                false,
                null
        );
        setupMockGetUserById(mockUserServer, userId, userDto);
        return eventService.createEvent(request, userId);
    }

    @Test
    void getEventsWithPagination_openStatus() throws IOException {
        userDto = createUserDto(++userId);
        setupMockGetUserById(mockUserServer, userId, userDto);
        EventResponse event1 = eventService.createEvent(validEventRequest, userId);
        EventResponse closedRequest = createEventWithStatus(userId, userDto, EventRegistrationStatus.CLOSED);
        EventResponse suspendedRequest = createEventWithStatus(userId, userDto, EventRegistrationStatus.SUSPENDED);

        EventSearchParam param = EventSearchParam.builder()
                .pageable(PageRequest.of(0, 3))
                .ownerId(userId)
                .status(EventRegistrationStatus.OPEN)
                .build();
        List<EventResponse> events = eventService.getEvents(param);

        assertEquals(1, events.size());
        assertEquals(event1.getId(), events.get(0).getId());
    }

    @Test
    void getEventsWithPagination_withoutStatus() throws IOException {
        userDto = createUserDto(++userId);
        setupMockGetUserById(mockUserServer, userId, userDto);
        EventResponse event1 = eventService.createEvent(validEventRequest, userId);
        EventResponse closedEvent = createEventWithStatus(userId, userDto, EventRegistrationStatus.CLOSED);
        EventResponse suspendedEvent = createEventWithStatus(userId, userDto, EventRegistrationStatus.SUSPENDED);
        List<Long> expectedIds = new ArrayList<>(List.of(event1.getId(), closedEvent.getId(), suspendedEvent.getId()));

        EventSearchParam param = EventSearchParam.builder()
                .pageable(PageRequest.of(0, 3))
                .ownerId(userId)
                .build();
        List<EventResponse> events = eventService.getEvents(param);
        List<Long> resultIds = events.stream().map(EventResponse::getId).toList();

        assertEquals(3, events.size());
        assertTrue(expectedIds.containsAll(resultIds));
    }

    @Test
    void getEventsWithPagination_withoutOwnerId() throws IOException {
        userDto = createUserDto(++userId);
        setupMockGetUserById(mockUserServer, userId, userDto);
        EventResponse event1 = eventService.createEvent(validEventRequest, userId);
        EventResponse event2 = createEventWithStatus(++userId, createUserDto(userId), EventRegistrationStatus.OPEN);
        EventResponse event3 = createEventWithStatus(++userId, createUserDto(userId), EventRegistrationStatus.OPEN);
        List<Long> expectedIds = new ArrayList<>(List.of(event1.getId(), event2.getId(), event3.getId()));

        EventSearchParam param = EventSearchParam.builder()
                .pageable(PageRequest.of(0, 3))
                .status(EventRegistrationStatus.OPEN)
                .build();
        List<EventResponse> events = eventService.getEvents(param);
        List<Long> resultIds = events.stream().map(EventResponse::getId).toList();

        assertEquals(3, events.size());
        assertTrue(expectedIds.containsAll(resultIds));
    }

    @AfterAll
    static void tearDown() {
        mockUserServer.stop();
    }
}
