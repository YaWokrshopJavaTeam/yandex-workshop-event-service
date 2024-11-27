package ru.practicum.workshop.eventservice.service;

import ru.practicum.workshop.eventservice.dto.NewOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.dto.PublicOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.dto.UpdateOrgTeamMemberDto;

import java.util.List;

public interface OrgTeamMemberService {

    PublicOrgTeamMemberDto addTeamMember(Long requesterId, NewOrgTeamMemberDto newOrgTeamMemberDto);

    PublicOrgTeamMemberDto updateTeamMemberData(Long requesterId, UpdateOrgTeamMemberDto updateOrgTeamMemberDto);

    void deleteTeamMember(Long requesterId, Long eventId, Long userId);

    List<PublicOrgTeamMemberDto> getTeamMembers(Long eventId);
}
