package ru.practicum.workshop.eventservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.workshop.eventservice.model.EventRegistrationStatus;


import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String location;
    private Long ownerId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime createdDateTime;
    private EventRegistrationStatus registrationStatus;
    private boolean isLimited;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer participantLimit;
}