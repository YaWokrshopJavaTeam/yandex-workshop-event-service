package ru.practicum.workshop.eventservice.params;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Pageable;
import ru.practicum.workshop.eventservice.model.EventRegistrationStatus;

@Data
@Builder
public class EventSearchParam {
    private Pageable pageable;
    private Long ownerId;
    private EventRegistrationStatus status;
}
