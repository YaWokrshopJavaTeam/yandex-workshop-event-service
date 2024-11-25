package ru.practicum.workshop.eventservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.workshop.eventservice.dto.NewOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.dto.PublicOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.dto.UpdateOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.error.ForbiddenException;
import ru.practicum.workshop.eventservice.mapper.OrgTeamMemberMapper;
import ru.practicum.workshop.eventservice.model.Event;
import ru.practicum.workshop.eventservice.model.OrgTeamMember;
import ru.practicum.workshop.eventservice.repository.OrgTeamMemberRepository;
import ru.practicum.workshop.eventservice.service.EventService;
import ru.practicum.workshop.eventservice.service.OrgTeamMemberService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrgTeamMemberServiceImpl implements OrgTeamMemberService {

    private final EventService eventService;

    private final OrgTeamMemberRepository orgTeamMemberRepository;

    private final OrgTeamMemberMapper orgTeamMemberMapper;

    @Override
    @Transactional
    public PublicOrgTeamMemberDto addTeamMember(Long requesterId, NewOrgTeamMemberDto newOrgTeamMemberDto) {
        Event event = eventService.getEventInternal(newOrgTeamMemberDto.getEventId());

        checkRightsForOrgTeamModification(requesterId, event);

        OrgTeamMember newOrgTeamMember = orgTeamMemberMapper.toOrgTeamMember(event, newOrgTeamMemberDto);
        orgTeamMemberRepository.save(newOrgTeamMember);

        log.info("Added team member: {}", newOrgTeamMember);

        return orgTeamMemberMapper.toPublicOrgTeamMemberDto(newOrgTeamMember);
    }

    @Override
    @Transactional
    public PublicOrgTeamMemberDto updateTeamMemberData(Long requesterId, UpdateOrgTeamMemberDto updateOrgTeamMemberDto) {
        Event event = eventService.getEventInternal(updateOrgTeamMemberDto.getEventId());

        checkRightsForOrgTeamModification(requesterId, event);

        OrgTeamMember orgTeamMember = getOrgTeamMemberInternal(updateOrgTeamMemberDto.getEventId(),
                                                               updateOrgTeamMemberDto.getUserId());

        orgTeamMemberMapper.updateOrgTeamMemberData(orgTeamMember, updateOrgTeamMemberDto);

        log.info("Updated team member: {}", orgTeamMember);

        return orgTeamMemberMapper.toPublicOrgTeamMemberDto(orgTeamMember);
    }

    @Override
    @Transactional
    public void deleteTeamMember(Long requesterId, Long eventId, Long userId) {
        Event event = eventService.getEventInternal(eventId);

        checkRightsForOrgTeamModification(requesterId, event);

        OrgTeamMember orgTeamMember = getOrgTeamMemberInternal(eventId, userId);

        orgTeamMemberRepository.deleteById(orgTeamMember.getId());

        log.info("Deleted team member: {}", orgTeamMember);
    }

    @Override
    public List<PublicOrgTeamMemberDto> getTeamMembers(Long eventId) {
        Event event = eventService.getEventInternal(eventId);

        List<OrgTeamMember> members = orgTeamMemberRepository.findAllByEventId(eventId);

        log.info("Sent members: {}", members);

        return orgTeamMemberMapper.toPublicOrgTeamMemberDto(members);
    }

    private OrgTeamMember getOrgTeamMemberInternal(Long eventId, Long userId) {
        return orgTeamMemberRepository.findByEventIdAndUserId(eventId, userId).orElseThrow(
                () -> new EntityNotFoundException(
                        String.format("Not found team member for event(id=%d) with current user(id=%d).",
                                eventId, userId)));
    }

    private void checkRightsForOrgTeamModification(Long requesterId, Event event) {
        if (event.getOwnerId().equals(requesterId)) {
            return;
        }

        if (orgTeamMemberRepository.findByEventIdAndUserIdAndRole(
                event.getId(),
                requesterId,
                OrgTeamMember.Role.MANAGER).isPresent()) {
            return;
        }

        throw new ForbiddenException(String.format("User(id=%d) has not rights to modify org group of event(id=%d).",
                requesterId,
                event.getId()));
    }
}
