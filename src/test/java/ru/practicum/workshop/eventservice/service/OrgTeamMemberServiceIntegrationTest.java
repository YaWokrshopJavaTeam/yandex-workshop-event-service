package ru.practicum.workshop.eventservice.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.practicum.workshop.eventservice.dto.*;
import ru.practicum.workshop.eventservice.error.ForbiddenException;
import ru.practicum.workshop.eventservice.model.OrgTeamMember;
import ru.practicum.workshop.eventservice.repository.EventRepository;
import ru.practicum.workshop.eventservice.repository.OrgTeamMemberRepository;
import ru.practicum.workshop.eventservice.service.impl.EventServiceImpl;
import ru.practicum.workshop.eventservice.service.impl.OrgTeamMemberServiceImpl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.workshop.eventservice.UserMock.setupMockGetUserById;

@ActiveProfiles("test")
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class OrgTeamMemberServiceIntegrationTest {

    private final OrgTeamMemberServiceImpl orgTeamMemberService;
    private final EventServiceImpl eventService;

    private final OrgTeamMemberRepository orgTeamMemberRepository;
    private final EventRepository eventRepository;
    private static WireMockServer mockUserServer;

    private UserDto userDto;
    private long userId = 1L;
    private int nextEventSuffix = 1;

    @BeforeAll
    static void beforeAll() {
        mockUserServer = new WireMockServer();
        mockUserServer.start();
        log.info("Mock-server started on port {}.", mockUserServer.port());
        configureFor("localhost", mockUserServer.port());
    }

    @DynamicPropertySource
    static void setUserServiceUrl(DynamicPropertyRegistry registry) {
        registry.add("userservice.url", () -> "localhost:" + mockUserServer.port() + "/users");
    }

    @BeforeEach
    void stubMockUser() throws IOException {
        userDto = createUserDto(userId);
        setupMockGetUserById(mockUserServer, userId, userDto);
    }

    @AfterEach
    void clearDataBase() {
        orgTeamMemberRepository.deleteAll();
        eventRepository.deleteAll();
    }

    private UserDto createUserDto(Long userId) {
        return UserDto.builder()
                .id(userId)
                .email("email@email.com")
                .name("name")
                .aboutMe("about me")
                .build();
    }

    // Method "addTeamMember" tests.
    @Test
    public void addTeamMember_byOwner_thenSave() throws IOException {
        var eventResponse = eventService.createEvent(getNextEventRequest(), userId);

        setupMockGetUserById(mockUserServer, getUserId(), createUserDto(userId));
        var newOrgTeamMemberDto = NewOrgTeamMemberDto.builder()
                .eventId(eventResponse.getId())
                .userId(userId)
                .role(OrgTeamMember.Role.EXECUTOR).build();

        var expectedPublicOrgTeamMemberDto = PublicOrgTeamMemberDto.builder()
                .userId(newOrgTeamMemberDto.getUserId()).role(OrgTeamMember.Role.EXECUTOR).build();

        var actualPublicOrgTeamMemberDot = orgTeamMemberService.addTeamMember(eventResponse.getOwnerId(), newOrgTeamMemberDto);

        assertThat(actualPublicOrgTeamMemberDot, equalTo(expectedPublicOrgTeamMemberDto));
    }

    @Test
    public void addTeamMember_byManager_thenSave() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var managerId = getUserId();
        setupMockGetUserById(mockUserServer, managerId, createUserDto(managerId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(managerId)
                .role(OrgTeamMember.Role.MANAGER).build());

        var newMemberId = getUserId();
        var memberDto = new NewOrgTeamMemberDto(eventId, newMemberId, OrgTeamMember.Role.EXECUTOR);

        var expectedDto = PublicOrgTeamMemberDto.builder().userId(newMemberId).role(OrgTeamMember.Role.EXECUTOR).build();

        setupMockGetUserById(mockUserServer, newMemberId, createUserDto(newMemberId));
        var actualDto = orgTeamMemberService.addTeamMember(managerId, memberDto);

        assertThat(actualDto, equalTo(expectedDto));
    }

    @Test
    public void addTeamMember_byExecutor_thenThrowException() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var executorId = getUserId();
        setupMockGetUserById(mockUserServer, executorId, createUserDto(executorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var newMemberId = getUserId();
        var memberDto = new NewOrgTeamMemberDto(eventId, newMemberId, OrgTeamMember.Role.EXECUTOR);

        assertThrows(ForbiddenException.class, () -> orgTeamMemberService.addTeamMember(executorId, memberDto));
    }

    @Test
    public void addOwner_thenThrowException() {
        var ownerId = userId;
        var eventResponse = eventService.createEvent(getNextEventRequest(), ownerId);

        var memberDto = NewOrgTeamMemberDto.builder()
                .eventId(eventResponse.getId())
                .userId(ownerId)
                .role(OrgTeamMember.Role.MANAGER).build();

        assertThrows(ForbiddenException.class, () -> orgTeamMemberService.addTeamMember(ownerId, memberDto));
    }

    @Test
    public void addTeamMember_whenAlreadyExists_thenThrowException() throws IOException {
        var ownerId = userId;
        var eventResponse = eventService.createEvent(getNextEventRequest(), ownerId);

        var executorId = getUserId();
        setupMockGetUserById(mockUserServer, executorId, createUserDto(executorId));
        var memberDto = NewOrgTeamMemberDto.builder()
                .eventId(eventResponse.getId())
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build();

        orgTeamMemberService.addTeamMember(ownerId, memberDto);

        assertThrows(DataIntegrityViolationException.class, () -> orgTeamMemberService.addTeamMember(ownerId, memberDto));
    }

    @Test
    public void addTeamMember_whenEventNotExists_thenThrowException() {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var executorId = getUserId();
        var memberDto = NewOrgTeamMemberDto.builder()
                .eventId(eventId + 1)
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build();

        assertThrows(EntityNotFoundException.class, () -> orgTeamMemberService.addTeamMember(ownerId, memberDto));
    }

    // Method "updateTeamMemberData" tests.
    @Test
    public void updateTeamMemberData_byOwner_thenUpdate() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var executorId = getUserId();
        setupMockGetUserById(mockUserServer, executorId, createUserDto(executorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var updateMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(eventId).userId(executorId).role(OrgTeamMember.Role.MANAGER).build();

        var expectedMemberDto = PublicOrgTeamMemberDto.builder()
                .userId(executorId).role(OrgTeamMember.Role.MANAGER).build();

        var actualMemberDto = orgTeamMemberService.updateTeamMemberData(ownerId, updateMemberDto);

        assertThat(actualMemberDto, equalTo(expectedMemberDto));
    }

    @Test
    public void updateTeamMemberData_byManager_thenUpdate() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var managerId = getUserId();
        setupMockGetUserById(mockUserServer, managerId, createUserDto(managerId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(managerId)
                .role(OrgTeamMember.Role.MANAGER).build());

        var executorId = getUserId();
        setupMockGetUserById(mockUserServer, executorId, createUserDto(executorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var updateMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(eventId).userId(executorId).role(OrgTeamMember.Role.MANAGER).build();

        var expectedMemberDto = PublicOrgTeamMemberDto.builder()
                .userId(executorId).role(OrgTeamMember.Role.MANAGER).build();

        var actualMemberDto = orgTeamMemberService.updateTeamMemberData(managerId, updateMemberDto);

        assertThat(actualMemberDto, equalTo(expectedMemberDto));
    }

    @Test
    public void updateTeamMemberData_byExecutor_thenThrowException() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var firstExecutorId = getUserId();
        setupMockGetUserById(mockUserServer, firstExecutorId, createUserDto(firstExecutorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(firstExecutorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var secondExecutorId = getUserId();
        setupMockGetUserById(mockUserServer, secondExecutorId, createUserDto(secondExecutorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(secondExecutorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var updateMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(eventId).userId(secondExecutorId).role(OrgTeamMember.Role.MANAGER).build();

        assertThrows(ForbiddenException.class, () -> orgTeamMemberService.updateTeamMemberData(firstExecutorId, updateMemberDto));
    }

    @Test
    public void updateTeamMemberData_whenMemberNotExists_thenThrowException() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var executorId = getUserId();
        setupMockGetUserById(mockUserServer, executorId, createUserDto(executorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var incorrectExecutorId = getUserId();
        var updateMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(eventId).userId(incorrectExecutorId).role(OrgTeamMember.Role.MANAGER).build();

        assertThrows(EntityNotFoundException.class, () -> orgTeamMemberService.updateTeamMemberData(ownerId, updateMemberDto));
    }

    @Test
    public void updateTeamMemberData_whenEventNotExists_thenThrowException() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var executorId = getUserId();
        setupMockGetUserById(mockUserServer, executorId, createUserDto(executorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var updateMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(eventId + 1).userId(executorId).role(OrgTeamMember.Role.MANAGER).build();

        assertThrows(EntityNotFoundException.class, () -> orgTeamMemberService.updateTeamMemberData(ownerId, updateMemberDto));
    }

    // Method "deleteTeamMember" tests.
    @Test
    public void deleteTeamMember_byOwner_thenDelete() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var executorId = getUserId();
        setupMockGetUserById(mockUserServer, executorId, createUserDto(executorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        orgTeamMemberService.deleteTeamMember(ownerId, eventId, executorId);

        assertTrue(orgTeamMemberRepository.findByEventIdAndUserId(eventId, executorId).isEmpty());
    }

    @Test
    public void deleteTeamMember_byManager_thenDelete() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var managerId = getUserId();
        setupMockGetUserById(mockUserServer, managerId, createUserDto(managerId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(managerId)
                .role(OrgTeamMember.Role.MANAGER).build());

        var executorId = getUserId();
        setupMockGetUserById(mockUserServer, executorId, createUserDto(executorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        orgTeamMemberService.deleteTeamMember(managerId, eventId, executorId);

        assertTrue(orgTeamMemberRepository.findByEventIdAndUserId(eventId, executorId).isEmpty());
    }

    @Test
    public void deleteTeamMember_byExecutor_thenThrowException() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var firstExecutorId = getUserId();
        setupMockGetUserById(mockUserServer, firstExecutorId, createUserDto(firstExecutorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(firstExecutorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var secondExecutorId = getUserId();
        setupMockGetUserById(mockUserServer, secondExecutorId, createUserDto(secondExecutorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(secondExecutorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        assertThrows(ForbiddenException.class, () -> orgTeamMemberService.deleteTeamMember(firstExecutorId, eventId, secondExecutorId));
    }

    @Test
    public void deleteTeamMember_whenMemberNotExists_thenThrowException() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var executorId = getUserId();
        setupMockGetUserById(mockUserServer, executorId, createUserDto(executorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var incorrectExecutorId = getUserId();
        assertThrows(EntityNotFoundException.class, () -> orgTeamMemberService.deleteTeamMember(ownerId, eventId, incorrectExecutorId));
    }

    @Test
    public void deleteTeamMember_whenEventNotExists_thenThrowException() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var executorId = getUserId();
        setupMockGetUserById(mockUserServer, executorId, createUserDto(executorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        assertThrows(EntityNotFoundException.class, () -> orgTeamMemberService.deleteTeamMember(ownerId, eventId + 1, executorId));
    }

    // Method "getTeamMembers" tests.
    @Test
    public void getTeamMembers_whenInputValid_thenReturn() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var managerId = getUserId();
        setupMockGetUserById(mockUserServer, managerId, createUserDto(managerId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(managerId)
                .role(OrgTeamMember.Role.MANAGER).build());

        var executorId = getUserId();
        setupMockGetUserById(mockUserServer, executorId, createUserDto(executorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var expectedMemberDtos = Set.of(
                new PublicOrgTeamMemberDto(managerId, OrgTeamMember.Role.MANAGER),
                new PublicOrgTeamMemberDto(executorId, OrgTeamMember.Role.EXECUTOR));

        var actualMembersDtos = new HashSet<>(orgTeamMemberService.getTeamMembers(eventId));

        assertThat(actualMembersDtos, equalTo(expectedMemberDtos));
    }

    @Test
    public void getTeamMembers_whenEventNotExists_thenThrowException() throws IOException {
        var ownerId = userId;
        var eventId = eventService.createEvent(getNextEventRequest(), ownerId).getId();

        var managerId = getUserId();
        setupMockGetUserById(mockUserServer, managerId, createUserDto(managerId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(managerId)
                .role(OrgTeamMember.Role.MANAGER).build());

        var executorId = getUserId();
        setupMockGetUserById(mockUserServer, executorId, createUserDto(executorId));
        orgTeamMemberService.addTeamMember(ownerId, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(executorId)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        assertThrows(EntityNotFoundException.class, () -> orgTeamMemberService.getTeamMembers(eventId + 1));
    }

    // Utilities methods.
    private long getUserId() {
        return ++userId;
    }

    private int getNextEventSuffix() {
        return nextEventSuffix++;
    }

    private EventRequest getNextEventRequest() {
        int currentEventSuffix = getNextEventSuffix();

        return EventRequest.builder()
                .name("Event" + currentEventSuffix)
                .description("Description" + currentEventSuffix)
                .location("Location1" + currentEventSuffix)
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(2))
                .build();
    }

    @AfterAll
    static void tearDown() {
        mockUserServer.stop();
    }
}
