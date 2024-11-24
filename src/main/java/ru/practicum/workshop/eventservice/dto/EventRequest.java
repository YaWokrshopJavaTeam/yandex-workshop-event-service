package ru.practicum.workshop.eventservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.workshop.eventservice.validation.ValidDateRange;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidDateRange
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
}
