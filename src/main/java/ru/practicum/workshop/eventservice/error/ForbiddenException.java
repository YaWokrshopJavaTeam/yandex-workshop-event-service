package ru.practicum.workshop.eventservice.error;

public class ForbiddenException extends RuntimeException {
    public <T> ForbiddenException(Class<T> type, long id) {
        super(String.format("Не найден %s с id=%s", type.getSimpleName(), id));
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
