package ru.practicum.workshop.eventservice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.workshop.eventservice.dto.NewOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.dto.PublicOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.dto.UpdateOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.service.OrgTeamMemberService;

import java.util.List;

@RestController
@RequestMapping("/events/orgs")
@Validated
@RequiredArgsConstructor
@Slf4j
public class OrganizingTeamController {

    private final OrgTeamMemberService orgTeamMemberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublicOrgTeamMemberDto addTeamMember(@RequestHeader(name = "X-User-Id") @Positive Long requesterId,
                                                @RequestBody @Valid NewOrgTeamMemberDto newOrgTeamMemberDto) {
        log.info("Request: add team member, requester id={}, newOrgTeamMemberDto={}", requesterId, newOrgTeamMemberDto);
        return orgTeamMemberService.addTeamMember(requesterId, newOrgTeamMemberDto);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public PublicOrgTeamMemberDto updateTeamMemberData(@RequestHeader(name = "X-User-Id") @Positive Long requesterId,
                                                       @RequestBody @Valid UpdateOrgTeamMemberDto updateOrgTeamMemberDto) {
        log.info("Request: update team member data, requester id={}, updateOrgTeamMemberDto={}",
                requesterId, updateOrgTeamMemberDto);
        return orgTeamMemberService.updateTeamMemberData(requesterId, updateOrgTeamMemberDto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeamMember(@RequestHeader(name = "X-User-Id") @Positive Long requesterId,
                                 @RequestParam(name = "eventId") @Positive Long eventId,
                                 @RequestParam(name = "userId") @Positive Long userId) {
        log.info("Request: delete team member, requester id={}, event id={}, user id={}",
                requesterId, eventId, userId);
        orgTeamMemberService.deleteTeamMember(requesterId, eventId, userId);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public List<PublicOrgTeamMemberDto> getTeamMembers(@PathVariable(name = "eventId") @Positive Long eventId) {
        log.info("Request: get all team members for event with id={}", eventId);
        return orgTeamMemberService.getTeamMembers(eventId);
    }
}
