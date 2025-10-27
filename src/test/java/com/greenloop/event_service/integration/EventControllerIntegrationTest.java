package com.greenloop.event_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenloop.event_service.controllers.EventController;
import com.greenloop.event_service.dtos.CreateEventRequest;
import com.greenloop.event_service.dtos.EventResponse;
import com.greenloop.event_service.dtos.UpdateEventRequest;
import com.greenloop.event_service.services.EventService;
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

@WebMvcTest(EventController.class)
@ActiveProfiles("test")
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void createEvent_AsAdmin_Success() throws Exception {
        CreateEventRequest request = new CreateEventRequest();
        request.setName("Test Event");
        request.setDescription("Test Description");
        request.setCapacity(100);
        request.setCoins(50);
        request.setOrganizer("Test Organizer");
        request.setStartDateTime(LocalDateTime.now().plusDays(1));
        request.setEndDateTime(LocalDateTime.now().plusDays(2));
        request.setType("WORKSHOP");

        EventResponse response = EventResponse.builder()
                .id(EVENT_ID)
                .name("Test Event")
                .capacity(100)
                .build();

        when(eventService.createEvent(any(CreateEventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/events")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Event created successfully"))
                .andExpect(jsonPath("$.data.name").value("Test Event"));

        verify(eventService).createEvent(any(CreateEventRequest.class));
    }

    @Test
    void createEvent_AsNonAdmin_ThrowsException() throws Exception {
        CreateEventRequest request = new CreateEventRequest();
        request.setName("Test Event");

        mockMvc.perform(post("/api/events")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(eventService, never()).createEvent(any());
    }

    @Test
    void getAllEvents_Success() throws Exception {
        List<EventResponse> responses = Arrays.asList(
                EventResponse.builder().id(EVENT_ID).name("Event 1").build(),
                EventResponse.builder().id(UUID.randomUUID()).name("Event 2").build()
        );

        when(eventService.getAllEvents()).thenReturn(responses);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(eventService).getAllEvents();
    }

    @Test
    void getEvent_Success() throws Exception {
        EventResponse response = EventResponse.builder()
                .id(EVENT_ID)
                .name("Test Event")
                .build();

        when(eventService.getEventById(EVENT_ID)).thenReturn(response);

        mockMvc.perform(get("/api/events/{id}", EVENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.data.name").value("Test Event"));

        verify(eventService).getEventById(EVENT_ID);
    }

    @Test
    void updateEvent_AsAdmin_Success() throws Exception {
        UpdateEventRequest request = new UpdateEventRequest();
        request.setName("Updated Event");
        request.setDescription("Updated Description");

        EventResponse response = EventResponse.builder()
                .id(EVENT_ID)
                .name("Updated Event")
                .build();

        when(eventService.updateEvent(eq(EVENT_ID), any(UpdateEventRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/events/{id}", EVENT_ID)
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated Event"));

        verify(eventService).updateEvent(eq(EVENT_ID), any(UpdateEventRequest.class));
    }

    @Test
    void updateEvent_AsNonAdmin_ThrowsException() throws Exception {
        UpdateEventRequest request = new UpdateEventRequest();
        request.setName("Updated Event");

        mockMvc.perform(put("/api/events/{id}", EVENT_ID)
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(eventService, never()).updateEvent(any(), any());
    }

    @Test
    void deleteEvent_AsAdmin_Success() throws Exception {
        doNothing().when(eventService).deleteEvent(EVENT_ID);

        mockMvc.perform(delete("/api/events/{id}", EVENT_ID)
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());

        verify(eventService).deleteEvent(EVENT_ID);
    }

    @Test
    void deleteEvent_AsNonAdmin_ThrowsException() throws Exception {
        mockMvc.perform(delete("/api/events/{id}", EVENT_ID)
                        .header("X-User-Role", "USER"))
                .andExpect(status().isForbidden());

        verify(eventService, never()).deleteEvent(any());
    }

    @Test
    void upcomingEventsForUser_Success() throws Exception {
        List<EventResponse> responses = Arrays.asList(
                EventResponse.builder()
                        .id(EVENT_ID)
                        .name("Upcoming Event")
                        .startDateTime(LocalDateTime.now().plusDays(5))
                        .build()
        );

        when(eventService.upcomingEventForUser(USER_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/events/upcoming/joined")
                        .header("X-User-ID", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Upcoming Event"));

        verify(eventService).upcomingEventForUser(USER_ID);
    }

    @Test
    void pastEventsForUser_Success() throws Exception {
        List<EventResponse> responses = Arrays.asList(
                EventResponse.builder()
                        .id(EVENT_ID)
                        .name("Past Event")
                        .endDateTime(LocalDateTime.now().minusDays(1))
                        .build()
        );

        when(eventService.pastEventsForUser(USER_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/events/past")
                        .header("X-User-ID", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Past Event"));

        verify(eventService).pastEventsForUser(USER_ID);
    }

    @Test
    void checkHealth_Success() throws Exception {
        mockMvc.perform(get("/api/events/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Event Service is Up and Running!"));

        verifyNoInteractions(eventService);
    }
}