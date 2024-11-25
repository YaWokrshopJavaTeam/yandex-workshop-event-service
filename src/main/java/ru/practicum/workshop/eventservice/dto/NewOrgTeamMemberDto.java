package ru.practicum.workshop.eventservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.workshop.eventservice.model.OrgTeamMember;

import static ru.practicum.workshop.eventservice.dto.constants.OrgTeamMemberDtoConstants.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewOrgTeamMemberDto {

    @NotNull(message = EVENT_ID_NOT_NULL_ERROR_MESSAGE)
    @Positive(message = EVENT_ID_POSITIVE_ERROR_MESSAGE)
    private Long eventId;

    @NotNull(message = USER_ID_NOT_NULL_ERROR_MESSAGE)
    @Positive(message = USER_ID_POSITIVE_ERROR_MESSAGE)
    private Long userId;

    @NotNull(message = ROLE_NOT_NULL_ERROR_MESSAGE)
    private OrgTeamMember.Role role;

}
