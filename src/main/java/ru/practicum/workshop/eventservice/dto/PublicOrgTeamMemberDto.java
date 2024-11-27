package ru.practicum.workshop.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.workshop.eventservice.model.OrgTeamMember;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicOrgTeamMemberDto {

    private Long userId;

    private OrgTeamMember.Role role;

}
