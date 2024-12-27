package ru.practicum.workshop.eventservice.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.workshop.eventservice.model.EventRegistrationStatus;
import ru.practicum.workshop.eventservice.validation.ValidDateRange;
import ru.practicum.workshop.eventservice.validation.ValidParticipantLimit;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidDateRange
@ValidParticipantLimit
public class EventRequest {
    @NotNull(message = "Название не должно быть пустым")
    private String name;
    @NotNull(message = "Описание не должно быть пустым")
    private String description;
    @NotNull(message = "Дата начала события не должна быть пустой")
    @Future(message = "Дата начала события должна быть в будущем времени")
    private LocalDateTime startDateTime;
    @NotNull(message = "Дата окончания события не должна быть пустой")
    @Future(message = "Дата начала события должна быть в будущем времени")
    private LocalDateTime endDateTime;
    @NotNull(message = "Локация не должна быть пустой")
    private String location;
    @JsonSetter(nulls = Nulls.SKIP)
    private EventRegistrationStatus registrationStatus = EventRegistrationStatus.OPEN;
    @JsonSetter(nulls = Nulls.SKIP)
    private boolean isLimited = false;
    @PositiveOrZero(message = "Количество участников для мероприятия с лимитом участников должно быть больше либо равно 0")
    private Integer participantLimit;
}
