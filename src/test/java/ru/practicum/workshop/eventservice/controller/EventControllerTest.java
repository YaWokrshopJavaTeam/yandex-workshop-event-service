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
import ru.practicum.workshop.eventservice.model.EventRegistrationStatus;
import ru.practicum.workshop.eventservice.params.EventSearchParam;
import ru.practicum.workshop.eventservice.service.EventService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
                "Online",
                EventRegistrationStatus.OPEN,
                false,
                null
        );

        EventResponse response = new EventResponse(
                1L,
                "Test Event",
                "Description",
                startDateTime,
                endDateTime,
                "Online", 1L, LocalDateTime.now(),
                EventRegistrationStatus.OPEN,
                false,
                null
        );

        Mockito.when(eventService.createEvent(any(EventRequest.class), anyLong())).thenReturn(response);

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
                "New Location",
                EventRegistrationStatus.OPEN,
                false,
                null
        );

        EventResponse updateResponse = new EventResponse(1L, "Updated Event",
                "Updated Description",
                startDateTime,
                endDateTime,
                "New Location",1L, LocalDateTime.of(2024, 12, 2, 10, 0),
                EventRegistrationStatus.OPEN, false, null);

        Mockito.when(eventService.updateEvent(anyLong(), any(EventRequest.class), anyLong()))
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
                LocalDateTime.now(),
                EventRegistrationStatus.OPEN,
                false,
                null
        );

        Mockito.when(eventService.getEvent(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/events/1")
                        .header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Event"))
                .andExpect(jsonPath("$.createdDateTime").exists());
    }

    @Test
    void testGetEvents() throws Exception {
        EventResponse event1 = new EventResponse(
                1L,
                "Test Event",
                "Description",
                LocalDateTime.of(2024, 12, 1, 10, 0),
                LocalDateTime.of(2024, 12, 1, 12, 0),
                "Online",
                1L,
                LocalDateTime.now(),
                EventRegistrationStatus.OPEN,
                false,
                null
        );
        List<EventResponse> response = new ArrayList<>(List.of(event1));

        Mockito.when(eventService.getEvents(any(EventSearchParam.class))).thenReturn(response);

        mockMvc.perform(get("/events")
                        .param("page", String.valueOf(0))
                        .param("size", String.valueOf(2))
                        .param("ownerId", String.valueOf(1L))
                        .param("status", EventRegistrationStatus.OPEN.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath(".name").value("Test Event"));
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
                "Online",
                EventRegistrationStatus.OPEN,
                false,
                null
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
                "Online",
                EventRegistrationStatus.OPEN,
                false,
                null
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
                null,
                EventRegistrationStatus.OPEN,
                false,
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
                "Online",
                EventRegistrationStatus.OPEN,
                false,
                null
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
                "Online",
                EventRegistrationStatus.OPEN,
                false,
                null
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenCreateEvent_IsLimitNullParticipant_thenReturns400() throws Exception {
        EventRequest invalidRequest = new EventRequest(
                "Event",
                "Description",
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusDays(2),
                "Online",
                EventRegistrationStatus.OPEN,
                true,
                null
        );

        mockMvc.perform(post("/events").header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenCreateEvent_IsNotLimitParticipantNotNull_thenReturns400() throws Exception {
        EventRequest invalidRequest = new EventRequest(
                "Event",
                "Description",
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusDays(2),
                "Online",
                EventRegistrationStatus.OPEN,
                false,
                0
        );

        mockMvc.perform(post("/events").header("X-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}