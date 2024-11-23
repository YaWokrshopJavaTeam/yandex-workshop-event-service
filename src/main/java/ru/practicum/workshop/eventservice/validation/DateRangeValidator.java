package ru.practicum.workshop.eventservice.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.workshop.eventservice.dto.EventRequest;
import ru.practicum.workshop.eventservice.model.Event;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, EventRequest> {

    @Override
    public boolean isValid(EventRequest eventRequest, ConstraintValidatorContext context) {
        return eventRequest.getStartDateTime().isBefore(eventRequest.getEndDateTime());
    }
}
