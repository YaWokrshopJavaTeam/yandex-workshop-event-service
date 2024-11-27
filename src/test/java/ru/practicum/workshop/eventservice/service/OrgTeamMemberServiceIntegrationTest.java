package ru.practicum.workshop.eventservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.workshop.eventservice.dto.EventRequest;
import ru.practicum.workshop.eventservice.dto.NewOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.dto.PublicOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.dto.UpdateOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.error.ForbiddenException;
import ru.practicum.workshop.eventservice.model.OrgTeamMember;
import ru.practicum.workshop.eventservice.repository.EventRepository;
import ru.practicum.workshop.eventservice.repository.OrgTeamMemberRepository;
import ru.practicum.workshop.eventservice.service.impl.EventServiceImpl;
import ru.practicum.workshop.eventservice.service.impl.OrgTeamMemberServiceImpl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OrgTeamMemberServiceIntegrationTest {

    private final OrgTeamMemberServiceImpl orgTeamMemberService;
    private final EventServiceImpl eventService;

    private final OrgTeamMemberRepository orgTeamMemberRepository;
    private final EventRepository eventRepository;

    private int nextUserId = 1;
    private int nextEventSuffix = 1;

    @AfterEach
    void clearDataBase() {
        orgTeamMemberRepository.deleteAll();
        eventRepository.deleteAll();
    }

    // Method "addTeamMember" tests.
    @Test
    public void addTeamMember_byOwner_thenSave() {
        var eventResponse = eventService.createEvent(getNextEventRequest(), 777L);

        var newOrgTeamMemberDto = NewOrgTeamMemberDto.builder()
                .eventId(eventResponse.getId())
                .userId(101L)
                .role(OrgTeamMember.Role.EXECUTOR).build();

        var expectedPublicOrgTeamMemberDto = PublicOrgTeamMemberDto.builder()
                .userId(101L).role(OrgTeamMember.Role.EXECUTOR).build();

        var actualPublicOrgTeamMemberDot = orgTeamMemberService.addTeamMember(777L, newOrgTeamMemberDto);

        assertThat(actualPublicOrgTeamMemberDot, equalTo(expectedPublicOrgTeamMemberDto));
    }

    @Test
    public void addTeamMember_byManager_thenSave() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.MANAGER).build());

        var memberDto = new NewOrgTeamMemberDto(eventId, 102L, OrgTeamMember.Role.EXECUTOR);

        var expectedDto = PublicOrgTeamMemberDto.builder().userId(102L).role(OrgTeamMember.Role.EXECUTOR).build();

        var actualDto = orgTeamMemberService.addTeamMember(101L, memberDto);

        assertThat(actualDto, equalTo(expectedDto));
    }

    @Test
    public void addTeamMember_byExecutor_thenThrowException() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var memberDto = new NewOrgTeamMemberDto(eventId, 102L, OrgTeamMember.Role.EXECUTOR);

        assertThrows(ForbiddenException.class, () -> orgTeamMemberService.addTeamMember(101L, memberDto));
    }

    @Test
    public void addOwner_thenThrowException() {
        var eventResponse = eventService.createEvent(getNextEventRequest(), 777L);

        var memberDto = NewOrgTeamMemberDto.builder()
                .eventId(eventResponse.getId())
                .userId(777L)
                .role(OrgTeamMember.Role.MANAGER).build();

        assertThrows(ForbiddenException.class, () -> orgTeamMemberService.addTeamMember(777L, memberDto));
    }

    @Test
    public void addTeamMember_whenAlreadyExists_thenThrowException() {
        var eventResponse = eventService.createEvent(getNextEventRequest(), 777L);

        var memberDto = NewOrgTeamMemberDto.builder()
                .eventId(eventResponse.getId())
                .userId(101L)
                .role(OrgTeamMember.Role.EXECUTOR).build();

        orgTeamMemberService.addTeamMember(777L, memberDto);

        assertThrows(DataIntegrityViolationException.class, () -> orgTeamMemberService.addTeamMember(777L, memberDto));
    }

    @Test
    public void addTeamMember_whenEventNotExists_thenThrowException() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        var memberDto = NewOrgTeamMemberDto.builder()
                .eventId(eventId + 1)
                .userId(101L)
                .role(OrgTeamMember.Role.EXECUTOR).build();

        assertThrows(EntityNotFoundException.class, () -> orgTeamMemberService.addTeamMember(777L, memberDto));
    }

    // Method "updateTeamMemberData" tests.
    @Test
    public void updateTeamMemberData_byOwner_thenUpdate() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var updateMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(eventId).userId(101L).role(OrgTeamMember.Role.MANAGER).build();

        var expectedMemberDto = PublicOrgTeamMemberDto.builder()
                .userId(101L).role(OrgTeamMember.Role.MANAGER).build();

        var actualMemberDto = orgTeamMemberService.updateTeamMemberData(777L, updateMemberDto);

        assertThat(actualMemberDto, equalTo(expectedMemberDto));
    }

    @Test
    public void updateTeamMemberData_byManager_thenUpdate() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.MANAGER).build());

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(102L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var updateMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(eventId).userId(102L).role(OrgTeamMember.Role.MANAGER).build();

        var expectedMemberDto = PublicOrgTeamMemberDto.builder()
                .userId(102L).role(OrgTeamMember.Role.MANAGER).build();

        var actualMemberDto = orgTeamMemberService.updateTeamMemberData(101L, updateMemberDto);

        assertThat(actualMemberDto, equalTo(expectedMemberDto));
    }

    @Test
    public void updateTeamMemberData_byExecutor_thenThrowException() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(102L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var updateMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(eventId).userId(102L).role(OrgTeamMember.Role.MANAGER).build();

        assertThrows(ForbiddenException.class, () -> orgTeamMemberService.updateTeamMemberData(101L, updateMemberDto));
    }

    @Test
    public void updateTeamMemberData_whenMemberNotExists_thenThrowException() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var updateMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(eventId).userId(102L).role(OrgTeamMember.Role.MANAGER).build();

        assertThrows(EntityNotFoundException.class, () -> orgTeamMemberService.updateTeamMemberData(777L, updateMemberDto));
    }

    @Test
    public void updateTeamMemberData_whenEventNotExists_thenThrowException() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var updateMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(eventId + 1).userId(101L).role(OrgTeamMember.Role.MANAGER).build();

        assertThrows(EntityNotFoundException.class, () -> orgTeamMemberService.updateTeamMemberData(777L, updateMemberDto));
    }

    // Method "deleteTeamMember" tests.
    @Test
    public void deleteTeamMember_byOwner_thenDelete() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        orgTeamMemberService.deleteTeamMember(777L, eventId, 101L);

        assertTrue(orgTeamMemberRepository.findByEventIdAndUserId(eventId, 101L).isEmpty());
    }

    @Test
    public void deleteTeamMember_byManager_thenDelete() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.MANAGER).build());

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(102L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        orgTeamMemberService.deleteTeamMember(101L, eventId, 102L);

        assertTrue(orgTeamMemberRepository.findByEventIdAndUserId(eventId, 102L).isEmpty());
    }

    @Test
    public void deleteTeamMember_byExecutor_thenThrowException() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(102L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        assertThrows(ForbiddenException.class, () -> orgTeamMemberService.deleteTeamMember(101L, eventId, 102L));
    }

    @Test
    public void deleteTeamMember_whenMemberNotExists_thenThrowException() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        assertThrows(EntityNotFoundException.class, () -> orgTeamMemberService.deleteTeamMember(777L, eventId, 102L));
    }

    @Test
    public void deleteTeamMember_whenEventNotExists_thenThrowException() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        assertThrows(EntityNotFoundException.class, () -> orgTeamMemberService.deleteTeamMember(777L, eventId + 1, 102L));
    }

    // Method "getTeamMembers" tests.
    @Test
    public void getTeamMembers_whenInputValid_thenReturn() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.MANAGER).build());

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(102L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        var expectedMemberDtos = Set.of(
                new PublicOrgTeamMemberDto(101L, OrgTeamMember.Role.MANAGER),
                new PublicOrgTeamMemberDto(102L, OrgTeamMember.Role.EXECUTOR));

        var actualMembersDtos = new HashSet<>(orgTeamMemberService.getTeamMembers(eventId));

        assertThat(actualMembersDtos, equalTo(expectedMemberDtos));
    }

    @Test
    public void getTeamMembers_whenEventNotExists_thenThrowException() {
        var eventId = eventService.createEvent(getNextEventRequest(), 777L).getId();

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(101L)
                .role(OrgTeamMember.Role.MANAGER).build());

        orgTeamMemberService.addTeamMember(777L, NewOrgTeamMemberDto.builder()
                .eventId(eventId)
                .userId(102L)
                .role(OrgTeamMember.Role.EXECUTOR).build());

        assertThrows(EntityNotFoundException.class, () -> orgTeamMemberService.getTeamMembers(eventId + 1));
    }

    // Utilities methods.
    private int getNextUserId() {
        return nextUserId++;
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

}
