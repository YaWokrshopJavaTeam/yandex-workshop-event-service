package ru.practicum.workshop.eventservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.workshop.eventservice.model.OrgTeamMember;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrgTeamMemberDto {

    @NotNull
    @Positive
    private Long eventId;

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    private OrgTeamMember.Role role;

}
