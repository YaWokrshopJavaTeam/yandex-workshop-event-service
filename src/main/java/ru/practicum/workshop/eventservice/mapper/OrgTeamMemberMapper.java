package ru.practicum.workshop.eventservice.mapper;

import org.mapstruct.*;
import ru.practicum.workshop.eventservice.dto.NewOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.dto.PublicOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.dto.UpdateOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.model.Event;
import ru.practicum.workshop.eventservice.model.OrgTeamMember;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrgTeamMemberMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", source = "event")
    OrgTeamMember toOrgTeamMember(Event event, NewOrgTeamMemberDto newOrgTeamMemberDto);

    @Mapping(target = "userId", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    OrgTeamMember updateOrgTeamMemberData(@MappingTarget OrgTeamMember orgTeamMember,
                                          UpdateOrgTeamMemberDto updateOrgTeamMemberDto);

    PublicOrgTeamMemberDto toPublicOrgTeamMemberDto(OrgTeamMember orgTeamMember);

    List<PublicOrgTeamMemberDto> toPublicOrgTeamMemberDto(List<OrgTeamMember> orgTeamMembers);

}
