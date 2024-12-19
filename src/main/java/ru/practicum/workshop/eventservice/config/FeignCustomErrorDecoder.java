package ru.practicum.workshop.eventservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.persistence.EntityNotFoundException;
import ru.practicum.workshop.eventservice.error.BadRequest;

public class FeignCustomErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        return switch (response.status()) {
            case 400 -> new BadRequest("Data is incorrect");
            case 404 -> new EntityNotFoundException("Object not found");
            default -> new Exception("Common Feign Exception");
        };
    }
}
