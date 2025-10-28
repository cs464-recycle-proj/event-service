package com.greenloop.event_service.integration;

import com.greenloop.event_service.controllers.EventQueryController;
import com.greenloop.event_service.dtos.EventResponse;
import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.services.EventQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventQueryController.class)
@ActiveProfiles("test")
class EventQueryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventQueryService eventQueryService;

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void getAllEvents_Success() throws Exception {
        List<EventResponse> responses = List.of(
                EventResponse.builder()
                        .id(EVENT_ID)
                        .name("Community Cleanup")
                        .capacity(50)
                        .build());

        when(eventQueryService.getAllEvents()).thenReturn(responses);

        mockMvc.perform(get("/api/events")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Events retrieved successfully"))
                .andExpect(jsonPath("$.data[0].name").value("Community Cleanup"));

        verify(eventQueryService).getAllEvents();
    }

    @Test
    void getEventById_Success() throws Exception {
        EventResponse response = EventResponse.builder()
                .id(EVENT_ID)
                .name("Beach Cleanup")
                .build();

        when(eventQueryService.getEventById(EVENT_ID)).thenReturn(response);

        mockMvc.perform(get("/api/events/{id}", EVENT_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Event retrieved successfully"))
                .andExpect(jsonPath("$.data.name").value("Beach Cleanup"));

        verify(eventQueryService).getEventById(EVENT_ID);
    }

    @Test
    void upcomingEventsForUser_Success() throws Exception {
        List<EventResponse> responses = Arrays.asList(
                EventResponse.builder()
                        .id(EVENT_ID)
                        .name("Upcoming Event")
                        .startDateTime(LocalDateTime.now().plusDays(5))
                        .build());

        when(eventQueryService.upcomingEventForUser(USER_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/events/upcoming/joined")
                .header("X-User-ID", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Upcoming Event"));

        verify(eventQueryService).upcomingEventForUser(USER_ID);
    }

    @Test
    void pastEventsForUser_Success() throws Exception {
        List<EventResponse> responses = Arrays.asList(
                EventResponse.builder()
                        .id(EVENT_ID)
                        .name("Past Event")
                        .endDateTime(LocalDateTime.now().minusDays(1))
                        .build());

        when(eventQueryService.pastEventsForUser(USER_ID)).thenReturn(responses);

        mockMvc.perform(get("/api/events/past")
                .header("X-User-ID", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Past Event"));

        verify(eventQueryService).pastEventsForUser(USER_ID);
    }

    @Test
    void getAllEventTypes_Success() throws Exception {
        when(eventQueryService.getAllEventTypes()).thenReturn(EventType.values());

        mockMvc.perform(get("/api/events/types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Event types retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray());

        verify(eventQueryService).getAllEventTypes();
    }

    @Test
    void getTotalOpenEvents_Success() throws Exception {
        when(eventQueryService.getTotalOpenEvents()).thenReturn(12L);

        mockMvc.perform(get("/api/events/stats/open/total"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Total event count retrieved successfully"))
                .andExpect(jsonPath("$.data").value(12));

        verify(eventQueryService).getTotalOpenEvents();
    }

    @Test
    void getUpcomingEventsNext30Days_Success() throws Exception {
        when(eventQueryService.getUpcomingEventsNext30Days()).thenReturn(5L);

        mockMvc.perform(get("/api/events/stats/upcoming/30days"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Events retrieved successfully"))
                .andExpect(jsonPath("$.data").value(5));

        verify(eventQueryService).getUpcomingEventsNext30Days();
    }

    @Test
    void getTotalParticipantsInOpenEvents_Success() throws Exception {
        when(eventQueryService.getTotalParticipantsInOpenEvents()).thenReturn(150L);

        mockMvc.perform(get("/api/events/stats/open/participants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Total participant count retrieved successfully"))
                .andExpect(jsonPath("$.data").value(150));

        verify(eventQueryService).getTotalParticipantsInOpenEvents();
    }
}