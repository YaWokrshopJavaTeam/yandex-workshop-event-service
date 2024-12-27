package ru.practicum.workshop.eventservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.workshop.eventservice.dto.EventRequest;

public class ParticipantLimitValidator implements ConstraintValidator<ValidParticipantLimit, EventRequest> {

    @Override
    public boolean isValid(EventRequest eventRequest, ConstraintValidatorContext context) {
        if (eventRequest.isLimited()) {
            return eventRequest.getParticipantLimit() != null;
        } else return eventRequest.getParticipantLimit() == null;
    }
}
