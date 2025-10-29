package com.greenloop.event_service.unit;

import com.greenloop.event_service.dtos.CreateEventRequest;
import com.greenloop.event_service.dtos.EventResponse;
import com.greenloop.event_service.dtos.UpdateEventRequest;
import com.greenloop.event_service.enums.EventStatus;
import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.models.EventAttendee;
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

/**
 * Comprehensive unit tests for EventService.
 * <p>
 * Tests both command operations (create, update, delete) and query operations
 * (get all, get by ID, user event relationships, analytics).
 * </p>
 */
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

    // ==================== QUERY OPERATION TESTS ==================== //

    @Test
    void getAllEvents_Success() {
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll()).thenReturn(events);

        List<EventResponse> responses = eventService.getAllEvents();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(eventId);
        assertThat(responses.get(0).getName()).isEqualTo(testEvent.getName());
        verify(eventRepository).findAll();
    }

    @Test
    void getAllEvents_EmptyList() {
        when(eventRepository.findAll()).thenReturn(Collections.emptyList());

        List<EventResponse> responses = eventService.getAllEvents();

        assertThat(responses).isEmpty();
        verify(eventRepository).findAll();
    }

    @Test
    void getEventById_Success() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        EventResponse response = eventService.getEventById(eventId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(eventId);
        assertThat(response.getName()).isEqualTo(testEvent.getName());
        verify(eventRepository).findById(eventId);
    }

    @Test
    void getEventById_NotFound_ThrowsException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(eventId))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("Event with id " + eventId + " is not found");

        verify(eventRepository).findById(eventId);
    }

    @Test
    void upcomingEventForUser_Success() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Event futureEvent = Event.builder()
                .id(UUID.randomUUID())
                .name("Future Event")
                .startDateTime(now.plusDays(5))
                .endDateTime(now.plusDays(6))
                .capacity(100)
                .coins(50)
                .organizer("Organizer")
                .imageUrl("http://example.com/image.jpg")
                .type(EventType.WORKSHOP)
                .status(EventStatus.REGISTRATION)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(now)
                .build();

        Event pastEvent = Event.builder()
                .id(UUID.randomUUID())
                .name("Past Event")
                .startDateTime(now.minusDays(2))
                .endDateTime(now.minusDays(1))
                .capacity(100)
                .coins(50)
                .organizer("Organizer")
                .imageUrl("http://example.com/image.jpg")
                .type(EventType.WORKSHOP)
                .status(EventStatus.CLOSED)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(now)
                .build();

        when(eventRepository.findAllEventsByAttendeeId(userId))
                .thenReturn(Arrays.asList(futureEvent, pastEvent));

        List<EventResponse> responses = eventService.upcomingEventForUser(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Future Event");
        verify(eventRepository).findAllEventsByAttendeeId(userId);
    }

    @Test
    void upcomingEventForUser_NoUpcomingEvents() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Event pastEvent = Event.builder()
                .id(UUID.randomUUID())
                .name("Past Event")
                .startDateTime(now.minusDays(2))
                .endDateTime(now.minusDays(1))
                .capacity(100)
                .coins(50)
                .organizer("Organizer")
                .imageUrl("http://example.com/image.jpg")
                .type(EventType.WORKSHOP)
                .status(EventStatus.CLOSED)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(now)
                .build();

        when(eventRepository.findAllEventsByAttendeeId(userId))
                .thenReturn(Collections.singletonList(pastEvent));

        List<EventResponse> responses = eventService.upcomingEventForUser(userId);

        assertThat(responses).isEmpty();
        verify(eventRepository).findAllEventsByAttendeeId(userId);
    }

    @Test
    void pastEventsForUser_Success() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Event pastEvent = Event.builder()
                .id(UUID.randomUUID())
                .name("Past Event")
                .startDateTime(now.minusDays(2))
                .endDateTime(now.minusDays(1))
                .capacity(100)
                .coins(50)
                .organizer("Organizer")
                .imageUrl("http://example.com/image.jpg")
                .type(EventType.TREE_PLANTING)
                .status(EventStatus.CLOSED)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(now)
                .build();

        Event futureEvent = Event.builder()
                .id(UUID.randomUUID())
                .name("Future Event")
                .startDateTime(now.plusDays(5))
                .endDateTime(now.plusDays(6))
                .capacity(100)
                .coins(50)
                .organizer("Organizer")
                .imageUrl("http://example.com/image.jpg")
                .type(EventType.WORKSHOP)
                .status(EventStatus.REGISTRATION)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(now)
                .build();

        when(eventRepository.findAllEventsByAttendeeId(userId))
                .thenReturn(Arrays.asList(pastEvent, futureEvent));

        List<EventResponse> responses = eventService.pastEventsForUser(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Past Event");
        verify(eventRepository).findAllEventsByAttendeeId(userId);
    }

    @Test
    void pastEventsForUser_NoPastEvents() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Event futureEvent = Event.builder()
                .id(UUID.randomUUID())
                .name("Future Event")
                .startDateTime(now.plusDays(5))
                .endDateTime(now.plusDays(6))
                .capacity(100)
                .coins(50)
                .organizer("Organizer")
                .imageUrl("http://example.com/image.jpg")
                .type(EventType.WORKSHOP)
                .status(EventStatus.REGISTRATION)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(now)
                .build();

        when(eventRepository.findAllEventsByAttendeeId(userId))
                .thenReturn(Collections.singletonList(futureEvent));

        List<EventResponse> responses = eventService.pastEventsForUser(userId);

        assertThat(responses).isEmpty();
        verify(eventRepository).findAllEventsByAttendeeId(userId);
    }

    @Test
    void upcomingNotJoinedEventsForUser_Success() {
        UUID userId = UUID.randomUUID();
        EventAttendee joinedAttendee = new EventAttendee();
        joinedAttendee.setUserId(userId);

        Event availableEvent = Event.builder()
                .id(UUID.randomUUID())
                .name("Available Event")
                .capacity(10)
                .coins(50)
                .organizer("Organizer")
                .imageUrl("http://example.com/image.jpg")
                .startDateTime(LocalDateTime.now().plusDays(3))
                .endDateTime(LocalDateTime.now().plusDays(4))
                .type(EventType.WORKSHOP)
                .status(EventStatus.REGISTRATION)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(LocalDateTime.now())
                .attendees(new ArrayList<>())
                .build();

        Event joinedEvent = Event.builder()
                .id(UUID.randomUUID())
                .name("Joined Event")
                .capacity(10)
                .coins(50)
                .organizer("Organizer")
                .imageUrl("http://example.com/image.jpg")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(2))
                .type(EventType.WORKSHOP)
                .status(EventStatus.REGISTRATION)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(LocalDateTime.now())
                .attendees(List.of(joinedAttendee))
                .build();

        when(eventRepository.findAll()).thenReturn(List.of(availableEvent, joinedEvent));

        List<EventResponse> responses = eventService.upcomingNotJoinedEventsForUser(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Available Event");
        verify(eventRepository).findAll();
    }

    // ==================== ANALYTICS TESTS ==================== //

    @Test
    void getTotalOpenEvents_Success() {
        Event closed = Event.builder().status(EventStatus.CLOSED).build();
        Event open = Event.builder().status(EventStatus.REGISTRATION).build();

        when(eventRepository.findAll()).thenReturn(List.of(closed, open));

        long total = eventService.getTotalOpenEvents();

        assertThat(total).isEqualTo(1);
        verify(eventRepository).findAll();
    }

    @Test
    void getUpcomingEventsNext30Days_Success() {
        LocalDateTime now = LocalDateTime.now();

        Event within30 = Event.builder()
                .startDateTime(now.plusDays(10))
                .build();

        Event outside30 = Event.builder()
                .startDateTime(now.plusDays(40))
                .build();

        when(eventRepository.findAll()).thenReturn(List.of(within30, outside30));

        long total = eventService.getUpcomingEventsNext30Days();

        assertThat(total).isEqualTo(1);
        verify(eventRepository).findAll();
    }

    @Test
    void getTotalParticipantsInOpenEvents_Success() {
        Event closed = Event.builder()
                .status(EventStatus.CLOSED)
                .attendees(List.of(new EventAttendee(), new EventAttendee()))
                .build();

        Event open = Event.builder()
                .status(EventStatus.REGISTRATION)
                .attendees(List.of(new EventAttendee(), new EventAttendee(), new EventAttendee()))
                .build();

        when(eventRepository.findAll()).thenReturn(List.of(closed, open));

        long total = eventService.getTotalParticipantsInOpenEvents();

        assertThat(total).isEqualTo(3);
        verify(eventRepository).findAll();
    }

    @Test
    void getAllEventTypes_ReturnsAllEnums() {
        EventType[] types = eventService.getAllEventTypes();

        assertThat(types).isNotEmpty();
        assertThat(types).contains(EventType.WORKSHOP, EventType.TREE_PLANTING);
    }

    // ==================== SCHEDULED TASKS (COMMENTED) ==================== //

    // @Test
    // void updateEventStatuses_TransitionsCorrectly() {
    // eventService.updateEventStatuses();

    // verify(eventRepository).updateStatusToOngoing(
    // eq(EventStatus.REGISTRATION),
    // eq(EventStatus.FULL),
    // eq(EventStatus.ONGOING),
    // any(LocalDateTime.class));

    // verify(eventRepository).updateStatusToClosed(
    // eq(EventStatus.ONGOING),
    // eq(EventStatus.CLOSED),
    // any(LocalDateTime.class));
    // }
}