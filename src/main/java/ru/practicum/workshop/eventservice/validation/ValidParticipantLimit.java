package ru.practicum.workshop.eventservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ParticipantLimitValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidParticipantLimit {
    String message() default "The number of participants in an event with a limit must be specified," +
            " without a limit of participants - not specified.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
