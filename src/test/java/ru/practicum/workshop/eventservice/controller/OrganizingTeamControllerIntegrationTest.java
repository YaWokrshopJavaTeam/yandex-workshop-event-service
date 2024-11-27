package ru.practicum.workshop.eventservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.workshop.eventservice.dto.NewOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.dto.PublicOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.dto.UpdateOrgTeamMemberDto;
import ru.practicum.workshop.eventservice.model.OrgTeamMember;
import ru.practicum.workshop.eventservice.service.OrgTeamMemberService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrganizingTeamController.class)
public class OrganizingTeamControllerIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrgTeamMemberService orgTeamMemberService;

    // Method "addTeamMember" tests.
    @Test
    public void addTeamMember_whenInputValid_thenSave() throws Exception {
        NewOrgTeamMemberDto newOrgTeamMemberDto = NewOrgTeamMemberDto.builder()
                .eventId(1L).userId(101L).role(OrgTeamMember.Role.EXECUTOR).build();

        PublicOrgTeamMemberDto publicOrgTeamMemberDto = PublicOrgTeamMemberDto.builder()
                .userId(101L).role(OrgTeamMember.Role.EXECUTOR).build();

        when(orgTeamMemberService.addTeamMember(any(Long.class), any(NewOrgTeamMemberDto.class)))
                .thenReturn(publicOrgTeamMemberDto);

        mockMvc.perform(post("/events/orgs")
                        .header("X-User-Id", 77)
                        .content(objectMapper.writeValueAsString(newOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.userId", is(newOrgTeamMemberDto.getUserId()), Long.class))
                .andExpect(jsonPath("$.role").exists())
                .andExpect(jsonPath("$.role", is(newOrgTeamMemberDto.getRole().toString())));
    }

    @Test
    public void addTeamMember_whenNoRequesterId_thenThrowException() throws Exception {
        NewOrgTeamMemberDto newOrgTeamMemberDto = NewOrgTeamMemberDto.builder()
                .eventId(1L).userId(101L).role(OrgTeamMember.Role.EXECUTOR).build();

        mockMvc.perform(post("/events/orgs")
                        .content(objectMapper.writeValueAsString(newOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addTeamMember_whenInvalidRequesterId_thenThrowException() throws Exception {
        NewOrgTeamMemberDto newOrgTeamMemberDto = NewOrgTeamMemberDto.builder()
                .eventId(1L).userId(101L).role(OrgTeamMember.Role.EXECUTOR).build();

        mockMvc.perform(post("/events/orgs")
                        .header("X-User-Id", 0)
                        .content(objectMapper.writeValueAsString(newOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addTeamMember_whenNoEventId_thenThrowException() throws Exception {
        NewOrgTeamMemberDto newOrgTeamMemberDto = NewOrgTeamMemberDto.builder()
                .userId(101L).role(OrgTeamMember.Role.EXECUTOR).build();

        mockMvc.perform(post("/events/orgs")
                        .header("X-User-Id", 77)
                        .content(objectMapper.writeValueAsString(newOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addTeamMember_whenInvalidEventId_thenThrowException() throws Exception {
        NewOrgTeamMemberDto newOrgTeamMemberDto = NewOrgTeamMemberDto.builder()
                .eventId(0L).userId(101L).role(OrgTeamMember.Role.EXECUTOR).build();

        mockMvc.perform(post("/events/orgs")
                        .header("X-User-Id", 77)
                        .content(objectMapper.writeValueAsString(newOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addTeamMember_whenNoUserId_thenThrowException() throws Exception {
        NewOrgTeamMemberDto newOrgTeamMemberDto = NewOrgTeamMemberDto.builder()
                .eventId(1L).role(OrgTeamMember.Role.EXECUTOR).build();

        mockMvc.perform(post("/events/orgs")
                        .header("X-User-Id", 77)
                        .content(objectMapper.writeValueAsString(newOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addTeamMember_whenInvalidUserId_thenThrowException() throws Exception {
        NewOrgTeamMemberDto newOrgTeamMemberDto = NewOrgTeamMemberDto.builder()
                .eventId(1L).userId(0L).role(OrgTeamMember.Role.EXECUTOR).build();

        mockMvc.perform(post("/events/orgs")
                        .header("X-User-Id", 77)
                        .content(objectMapper.writeValueAsString(newOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void addTeamMember_whenNoRole_thenThrowException() throws Exception {
        NewOrgTeamMemberDto newOrgTeamMemberDto = NewOrgTeamMemberDto.builder()
                .eventId(0L).userId(101L).build();

        mockMvc.perform(post("/events/orgs")
                        .header("X-User-Id", 77)
                        .content(objectMapper.writeValueAsString(newOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Method "updateTeamMemberData" tests.
    @Test
    public void updateTeamMemberData_whenInputValid_thenUpdate() throws Exception {
        UpdateOrgTeamMemberDto updateOrgTeamMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(1L).userId(101L).role(OrgTeamMember.Role.MANAGER).build();

        PublicOrgTeamMemberDto publicOrgTeamMemberDto = PublicOrgTeamMemberDto.builder()
                .userId(101L).role(OrgTeamMember.Role.MANAGER).build();

        when(orgTeamMemberService.updateTeamMemberData(any(Long.class), any(UpdateOrgTeamMemberDto.class)))
                .thenReturn(publicOrgTeamMemberDto);

        mockMvc.perform(patch("/events/orgs")
                        .header("X-User-Id", 77)
                        .content(objectMapper.writeValueAsString(updateOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.userId", is(updateOrgTeamMemberDto.getUserId()), Long.class))
                .andExpect(jsonPath("$.role").exists())
                .andExpect(jsonPath("$.role", is(updateOrgTeamMemberDto.getRole().toString())));
    }

    @Test
    public void updateTeamMemberData_whenNoRequesterId_thenThrowException() throws Exception {
        UpdateOrgTeamMemberDto updateOrgTeamMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(1L).userId(101L).role(OrgTeamMember.Role.MANAGER).build();

        mockMvc.perform(patch("/events/orgs")
                        .content(objectMapper.writeValueAsString(updateOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateTeamMemberData_whenInvalidRequesterId_thenThrowException() throws Exception {
        UpdateOrgTeamMemberDto updateOrgTeamMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(1L).userId(101L).role(OrgTeamMember.Role.MANAGER).build();

        mockMvc.perform(patch("/events/orgs")
                        .header("X-User-Id", 0)
                        .content(objectMapper.writeValueAsString(updateOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateTeamMemberData_whenNoEventId_thenThrowException() throws Exception {
        UpdateOrgTeamMemberDto updateOrgTeamMemberDto = UpdateOrgTeamMemberDto.builder()
                .userId(101L).role(OrgTeamMember.Role.MANAGER).build();

        mockMvc.perform(patch("/events/orgs")
                        .header("X-User-Id", 77)
                        .content(objectMapper.writeValueAsString(updateOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateTeamMemberData_whenInvalidEventId_thenThrowException() throws Exception {
        UpdateOrgTeamMemberDto updateOrgTeamMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(0L).userId(101L).role(OrgTeamMember.Role.MANAGER).build();

        mockMvc.perform(patch("/events/orgs")
                        .header("X-User-Id", 77)
                        .content(objectMapper.writeValueAsString(updateOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateTeamMemberData_whenNoUserId_thenThrowException() throws Exception {
        UpdateOrgTeamMemberDto updateOrgTeamMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(1L).role(OrgTeamMember.Role.MANAGER).build();

        mockMvc.perform(patch("/events/orgs")
                        .header("X-User-Id", 77)
                        .content(objectMapper.writeValueAsString(updateOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateTeamMemberData_whenInvalidUserId_thenThrowException() throws Exception {
        UpdateOrgTeamMemberDto updateOrgTeamMemberDto = UpdateOrgTeamMemberDto.builder()
                .eventId(1L).userId(0L).role(OrgTeamMember.Role.MANAGER).build();

        mockMvc.perform(patch("/events/orgs")
                        .header("X-User-Id", 77)
                        .content(objectMapper.writeValueAsString(updateOrgTeamMemberDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Method "deleteTeamMember" tests.
    @Test
    public void deleteTeamMember_whenInputValid_thenDelete() throws Exception {
        mockMvc.perform(delete("/events/{eventId}/orgs/{userId}", 1, 101)
                        .header("X-User-Id", 77)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteTeamMember_whenNoRequesterId_thenThrowException() throws Exception {
        mockMvc.perform(delete("/events/{eventId}/orgs/{userId}", 1, 101)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteTeamMember_whenInvalidRequesterId_thenThrowException() throws Exception {
        mockMvc.perform(delete("/events/{eventId}/orgs/{userId}", 1, 101)
                        .header("X-User-Id", 0)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteTeamMember_whenInvalidEventId_thenThrowException() throws Exception {
        mockMvc.perform(delete("/events/{eventId}/orgs/{userId}", 0, 101)
                        .header("X-User-Id", 77)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteTeamMember_whenInvalidUserId_thenThrowException() throws Exception {
        mockMvc.perform(delete("/events/{eventId}/orgs/{userId}", 1, 0)
                        .header("X-User-Id", 77)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Method "getTeamMembers" tests.
    @Test
    public void getTeamMembers_whenInputValid_thenReturn() throws Exception {
        List<PublicOrgTeamMemberDto> publicOrgTeamMemberDtos = List.of(
                PublicOrgTeamMemberDto.builder()
                        .userId(101L).role(OrgTeamMember.Role.EXECUTOR).build(),
                PublicOrgTeamMemberDto.builder()
                        .userId(102L).role(OrgTeamMember.Role.MANAGER).build()
        );

        when(orgTeamMemberService.getTeamMembers(any(Long.class))).thenReturn(publicOrgTeamMemberDtos);

        mockMvc.perform(get("/events/orgs/{eventId}", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").exists())
                .andExpect(jsonPath("$[0].userId", is(publicOrgTeamMemberDtos.get(0).getUserId()), Long.class))
                .andExpect(jsonPath("$[0].role").exists())
                .andExpect(jsonPath("$[0].role", is(publicOrgTeamMemberDtos.get(0).getRole().toString())))
                .andExpect(jsonPath("$[1].userId").exists())
                .andExpect(jsonPath("$[1].userId", is(publicOrgTeamMemberDtos.get(1).getUserId()), Long.class))
                .andExpect(jsonPath("$[1].role").exists())
                .andExpect(jsonPath("$[1].role", is(publicOrgTeamMemberDtos.get(1).getRole().toString())));

    }

    @Test
    public void getTeamMembers_whenInvalidEventId_thenThrowException() throws Exception {
        mockMvc.perform(get("/events/orgs/{eventId}", 0)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

}
