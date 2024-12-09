package ru.practicum.workshop.eventservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.workshop.eventservice.dto.EventRequest;
import ru.practicum.workshop.eventservice.dto.EventResponse;
import ru.practicum.workshop.eventservice.service.EventService;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventController.class)
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateEvent() throws Exception {
        LocalDateTime startDateTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endDateTime = startDateTime.plusDays(2);

        EventRequest request = new EventRequest(
                "Test Event",
                "Description",
                startDateTime,
                endDateTime,
                "Online"
        );

        EventResponse response = new EventResponse(
                1L,
                "Test Event",
                "Description",
                startDateTime,
                endDateTime,
                "Online", 1L, LocalDateTime.now()
        );

        Mockito.when(eventService.createEvent(Mockito.any(EventRequest.class), Mockito.anyLong())).thenReturn(response);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Event"));
    }

    @Test
    void testUpdateEvent() throws Exception {
        LocalDateTime startDateTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endDateTime = startDateTime.plusDays(2);

        EventRequest updateRequest = new EventRequest(
                "Updated Event",
                "Updated Description",
                startDateTime,
                endDateTime,
                "New Location"
        );

        EventResponse updateResponse = new EventResponse(1L, "Updated Event",
                "Updated Description",
                startDateTime,
                endDateTime,
                "New Location",1L, LocalDateTime.of(2024, 12, 2, 10, 0));

        Mockito.when(eventService.updateEvent(Mockito.anyLong(), Mockito.any(EventRequest.class), Mockito.anyLong()))
                .thenReturn(updateResponse);

        mockMvc.perform(patch("/events/1")
                        .header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetEventById() throws Exception {
        EventResponse response = new EventResponse(
                1L,
                "Test Event",
                "Description",
                LocalDateTime.of(2024, 12, 1, 10, 0),
                LocalDateTime.of(2024, 12, 1, 12, 0),
                "Online",
                1L,
                LocalDateTime.now()
        );

        Mockito.when(eventService.getEvent(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/events/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Event"))
                .andExpect(jsonPath("$.createdDateTime").exists());
    }

    @Test
    void testDeleteEvent() throws Exception {
        Mockito.doNothing().when(eventService).deleteEvent(1L, 1L);

        mockMvc.perform(delete("/events/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void whenNameIsEmpty_thenReturns400() throws Exception {
        EventRequest invalidRequest = new EventRequest(
                null,
                "Description",
                LocalDateTime.of(2024, 12, 1, 10, 0),
                LocalDateTime.of(2024, 12, 1, 12, 0),
                "Online"
        );

        mockMvc.perform(post("/events").header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenStartDateAfterEndDate_thenReturns400() throws Exception {
        EventRequest invalidRequest = new EventRequest(
                "Invalid Event",
                "Description",
                LocalDateTime.of(2024, 12, 1, 12, 0),
                LocalDateTime.of(2024, 12, 1, 10, 0),
                "Online"
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenLocationIsMissing_thenReturns400() throws Exception {
        EventRequest invalidRequest = new EventRequest(
                "Valid Event",
                "Description",
                LocalDateTime.of(2024, 12, 1, 10, 0),
                LocalDateTime.of(2024, 12, 1, 12, 0),
                null
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenStartDateIsMissing_thenReturns400() throws Exception {
        EventRequest invalidRequest = new EventRequest(
                "Valid Event",
                "Description",
                null,
                LocalDateTime.of(2024, 12, 1, 12, 0),
                "Online"
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenEndDateIsMissing_thenReturns400() throws Exception {
        EventRequest invalidRequest = new EventRequest(
                "Valid Event",
                "Description",
                LocalDateTime.of(2024, 12, 1, 10, 0),
                null,
                "Online"
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}