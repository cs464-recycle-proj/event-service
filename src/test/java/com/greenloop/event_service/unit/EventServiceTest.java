package com.greenloop.event_service.unit;

import com.greenloop.event_service.dtos.CreateEventRequest;
import com.greenloop.event_service.dtos.EventResponse;
import com.greenloop.event_service.dtos.UpdateEventRequest;
import com.greenloop.event_service.enums.EventStatus;
import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.repos.EventRepository;
import com.greenloop.event_service.services.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private UUID eventId;
    private CreateEventRequest createRequest;
    private UpdateEventRequest updateRequest;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();

        testEvent = Event.builder()
                .id(eventId)
                .name("Test Event")
                .description("Test Description")
                .capacity(100)
                .coins(50)
                .organizer("Test Organizer")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(2))
                .imageUrl("http://example.com/image.jpg")
                .type(EventType.WORKSHOP)
                .status(EventStatus.REGISTRATION)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(LocalDateTime.now())
                .build();

        createRequest = new CreateEventRequest();
        createRequest.setName("New Event");
        createRequest.setDescription("New Description");
        createRequest.setCapacity(200);
        createRequest.setCoins(100);
        createRequest.setOrganizer("New Organizer");
        createRequest.setStartDateTime(LocalDateTime.now().plusDays(3));
        createRequest.setEndDateTime(LocalDateTime.now().plusDays(4));
        createRequest.setImageUrl("http://example.com/new.jpg");
        createRequest.setType("TREE_PLANTING");

        updateRequest = new UpdateEventRequest();
        updateRequest.setName("Updated Event");
        updateRequest.setDescription("Updated Description");
    }

    @Test
    void createEvent_Success() {
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        EventResponse response = eventService.createEvent(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(testEvent.getName());
        assertThat(response.getDescription()).isEqualTo(testEvent.getDescription());
        assertThat(response.getCapacity()).isEqualTo(testEvent.getCapacity());
        assertThat(response.getCoins()).isEqualTo(testEvent.getCoins());
        assertThat(response.getStatus()).isEqualTo(EventStatus.REGISTRATION.name());
        assertThat(response.getQrToken()).isNotNull();
        verify(eventRepository).save(any(Event.class));
    }

    

    @Test
    void updateEvent_Success() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        EventResponse response = eventService.updateEvent(eventId, updateRequest);

        assertThat(response).isNotNull();
        verify(eventRepository).findById(eventId);
        verify(eventRepository).save(testEvent);
    }

    @Test
    void updateEvent_NotFound_ThrowsException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(eventId, updateRequest))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("Event with id " + eventId + " is not found");

        verify(eventRepository).findById(eventId);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void deleteEvent_Success() {
        when(eventRepository.existsById(eventId)).thenReturn(true);

        eventService.deleteEvent(eventId);

        verify(eventRepository).existsById(eventId);
        verify(eventRepository).deleteById(eventId);
    }

    @Test
    void deleteEvent_NotFound_ThrowsException() {
        when(eventRepository.existsById(eventId)).thenReturn(false);

        assertThatThrownBy(() -> eventService.deleteEvent(eventId))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("Event with id " + eventId + " is not found");

        verify(eventRepository).existsById(eventId);
        verify(eventRepository, never()).deleteById(any());
    }



    // @Test
    // void updateEventStatuses_TransitionsCorrectly() {
    //     eventService.updateEventStatuses();

    //     verify(eventRepository).updateStatusToOngoing(
    //             eq(EventStatus.REGISTRATION),
    //             eq(EventStatus.FULL),
    //             eq(EventStatus.ONGOING),
    //             any(LocalDateTime.class));

    //     verify(eventRepository).updateStatusToClosed(
    //             eq(EventStatus.ONGOING),
    //             eq(EventStatus.CLOSED),
    //             any(LocalDateTime.class));
    // }
}