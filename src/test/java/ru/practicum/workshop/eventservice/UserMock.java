package ru.practicum.workshop.eventservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import ru.practicum.workshop.eventservice.client.dto.UserDto;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class UserMock {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void setupMockGetUserById(WireMockServer mockService, Long userId, UserDto userDto) throws IOException {
        mockService.stubFor(get(urlEqualTo("/users/" + userId))
                .willReturn(
                        aResponse()
                                .withStatus(HttpStatus.OK.value())
                                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .withBody(objectMapper.writeValueAsString(userDto))));
    }
}
