package com.greenloop.event_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenloop.event_service.controllers.EventController;
import com.greenloop.event_service.dtos.CreateEventRequest;
import com.greenloop.event_service.dtos.EventResponse;
import com.greenloop.event_service.dtos.UpdateEventRequest;
import com.greenloop.event_service.services.EventAttendeeService;
import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.services.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for EventController.
 * <p>
 * Tests both command operations (admin-only) and query operations
 * (public/user).
 * Merged from EventControllerIntegrationTest and
 * EventQueryControllerIntegrationTest.
 * </p>
 */
@WebMvcTest(EventController.class)
@Import(EventControllerIntegrationTest.TestConfig.class)
@ActiveProfiles("test")
class EventControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private EventService eventService;

        @Autowired
        private EventAttendeeService attendeeService;

        private static final UUID EVENT_ID = UUID.randomUUID();

        @BeforeEach
        void resetMocks() {
                org.mockito.Mockito.reset(eventService, attendeeService);
        }

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

        // ==================== QUERY ENDPOINT TESTS ==================== //

        @Test
        void getAllEvents_Success() throws Exception {
                List<EventResponse> responses = List.of(
                                EventResponse.builder()
                                                .id(EVENT_ID)
                                                .name("Community Cleanup")
                                                .capacity(50)
                                                .build());

                when(eventService.getAllEvents()).thenReturn(responses);

                mockMvc.perform(get("/api/events")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Events retrieved successfully"))
                                .andExpect(jsonPath("$.data[0].name").value("Community Cleanup"));

                verify(eventService).getAllEvents();
        }

        @Test
        void getEventById_Success() throws Exception {
                EventResponse response = EventResponse.builder()
                                .id(EVENT_ID)
                                .name("Beach Cleanup")
                                .build();

                when(eventService.getEventById(EVENT_ID)).thenReturn(response);

                mockMvc.perform(get("/api/events/{id}", EVENT_ID)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Event retrieved successfully"))
                                .andExpect(jsonPath("$.data.name").value("Beach Cleanup"));

                verify(eventService).getEventById(EVENT_ID);
        }

        @Test
        void upcomingEventsForUser_Success() throws Exception {
                UUID userId = UUID.randomUUID();
                List<EventResponse> responses = Arrays.asList(
                                EventResponse.builder()
                                                .id(EVENT_ID)
                                                .name("Upcoming Event")
                                                .startDateTime(LocalDateTime.now().plusDays(5))
                                                .build());

                when(eventService.upcomingEventForUser(userId)).thenReturn(responses);

                mockMvc.perform(get("/api/events/upcoming/joined")
                                .header("X-User-ID", userId.toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].name").value("Upcoming Event"));

                verify(eventService).upcomingEventForUser(userId);
        }

        @Test
        void pastEventsForUser_Success() throws Exception {
                UUID userId = UUID.randomUUID();
                List<EventResponse> responses = Arrays.asList(
                                EventResponse.builder()
                                                .id(EVENT_ID)
                                                .name("Past Event")
                                                .endDateTime(LocalDateTime.now().minusDays(1))
                                                .build());

                when(eventService.pastEventsForUser(userId)).thenReturn(responses);

                mockMvc.perform(get("/api/events/past")
                                .header("X-User-ID", userId.toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data[0].name").value("Past Event"));

                verify(eventService).pastEventsForUser(userId);
        }

        @Test
        void getAllEventTypes_Success() throws Exception {
                when(eventService.getAllEventTypes()).thenReturn(EventType.values());

                mockMvc.perform(get("/api/events/types")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Event types retrieved successfully"))
                                .andExpect(jsonPath("$.data").isArray());

                verify(eventService).getAllEventTypes();
        }

        // ==================== ANALYTICS ENDPOINT TESTS ==================== //

        @Test
        void getTotalOpenEvents_Success() throws Exception {
                when(eventService.getTotalOpenEvents()).thenReturn(12L);

                mockMvc.perform(get("/api/events/stats/open/total"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Total event count retrieved successfully"))
                                .andExpect(jsonPath("$.data").value(12));

                verify(eventService).getTotalOpenEvents();
        }

        @Test
        void getUpcomingEventsNext30Days_Success() throws Exception {
                when(eventService.getUpcomingEventsNext30Days()).thenReturn(5L);

                mockMvc.perform(get("/api/events/stats/upcoming/30days"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message").value("Events retrieved successfully"))
                                .andExpect(jsonPath("$.data").value(5));

                verify(eventService).getUpcomingEventsNext30Days();
        }

        @Test
        void getTotalParticipantsInOpenEvents_Success() throws Exception {
                when(eventService.getTotalParticipantsInOpenEvents()).thenReturn(250L);

                mockMvc.perform(get("/api/events/stats/open/participants"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.message")
                                                .value("Total participant count retrieved successfully"))
                                .andExpect(jsonPath("$.data").value(250));

                verify(eventService).getTotalParticipantsInOpenEvents();
        }

        @TestConfiguration
        static class TestConfig {
                @Bean
                EventService eventService() {
                        return org.mockito.Mockito.mock(EventService.class);
                }

                @Bean
                EventAttendeeService attendeeService() {
                        return org.mockito.Mockito.mock(EventAttendeeService.class);
                }
        }
}