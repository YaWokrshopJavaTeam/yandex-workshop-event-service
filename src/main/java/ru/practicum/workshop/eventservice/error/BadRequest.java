package ru.practicum.workshop.eventservice.error;

public class BadRequest extends RuntimeException {
    public BadRequest(String message) {
        super(message);
    }
}
