package com.greenloop.event_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenloop.event_service.controllers.EventAttendeeController;
import com.greenloop.event_service.dtos.EventAttendeeResponse;
import com.greenloop.event_service.dtos.ScanRequest;
import com.greenloop.event_service.services.EventAttendeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventAttendeeController.class)
@ActiveProfiles("test")
class EventAttendeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventAttendeeService attendeeService;

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USER_EMAIL = "test@example.com";

    @Test
    void registerAttendee_Success() throws Exception {
        EventAttendeeResponse response = EventAttendeeResponse.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .userEmail(USER_EMAIL)
                .registeredAt(LocalDateTime.now())
                .attended(false)
                .build();

        when(attendeeService.registerAttendee(any(UUID.class), any(UUID.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/events/{eventId}/register", EVENT_ID)
                        .header("X-User-ID", USER_ID.toString())
                        .header("X-User-Email", USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Attendee registered successfully"))
                .andExpect(jsonPath("$.data.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.data.userEmail").value(USER_EMAIL));

        verify(attendeeService).registerAttendee(EVENT_ID, USER_ID, USER_EMAIL);
    }

    @Test
    void getAllEventAttendees_AsAdmin_Success() throws Exception {
        List<EventAttendeeResponse> responses = Arrays.asList(
                EventAttendeeResponse.builder()
                        .id(UUID.randomUUID())
                        .userId(USER_ID)
                        .userEmail(USER_EMAIL)
                        .build()
        );

        when(attendeeService.getAllEventAttendees(EVENT_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/events/{eventId}/participants", EVENT_ID)
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].userId").value(USER_ID.toString()));

        verify(attendeeService).getAllEventAttendees(EVENT_ID);
    }

    @Test
    void getAllEventAttendees_AsNonAdmin_ThrowsException() throws Exception {
        mockMvc.perform(get("/api/events/{eventId}/participants", EVENT_ID)
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden());

        verify(attendeeService, never()).getAllEventAttendees(any());
    }

    @Test
    void isRegistered_ReturnsTrue() throws Exception {
        when(attendeeService.isUserRegistered(EVENT_ID, USER_ID)).thenReturn(true);

        mockMvc.perform(get("/api/events/{eventId}/is-registered", EVENT_ID)
                        .header("X-User-ID", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(attendeeService).isUserRegistered(EVENT_ID, USER_ID);
    }

    @Test
    void isRegistered_ReturnsFalse() throws Exception {
        when(attendeeService.isUserRegistered(EVENT_ID, USER_ID)).thenReturn(false);

        mockMvc.perform(get("/api/events/{eventId}/is-registered", EVENT_ID)
                        .header("X-User-ID", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(attendeeService).isUserRegistered(EVENT_ID, USER_ID);
    }

    @Test
    void deregisterAttendee_Success() throws Exception {
        doNothing().when(attendeeService).deregisterAttendee(EVENT_ID, USER_ID);

        mockMvc.perform(delete("/api/events/{eventId}/participants/{userId}", EVENT_ID, USER_ID))
                .andExpect(status().isNoContent());

        verify(attendeeService).deregisterAttendee(EVENT_ID, USER_ID);
    }

    @Test
    void scanAndMarkAttendance_Success() throws Exception {
        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrToken("test-qr-token");

        EventAttendeeResponse response = EventAttendeeResponse.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .userEmail(USER_EMAIL)
                .attended(true)
                .attendedAt(LocalDateTime.now())
                .build();

        when(attendeeService.markAttendanceByToken(any(ScanRequest.class), any(UUID.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/events/{eventId}/scan", EVENT_ID)
                        .header("X-User-ID", USER_ID.toString())
                        .header("X-User-Email", USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Attendance marked successfully"))
                .andExpect(jsonPath("$.data.attended").value(true));

        verify(attendeeService).markAttendanceByToken(any(ScanRequest.class), eq(USER_ID), eq(USER_EMAIL));
    }
}