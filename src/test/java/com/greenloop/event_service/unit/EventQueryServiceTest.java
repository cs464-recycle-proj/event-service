package com.greenloop.event_service.unit;

import com.greenloop.event_service.dtos.EventResponse;
import com.greenloop.event_service.enums.EventStatus;
import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.models.EventAttendee;
import com.greenloop.event_service.repos.EventRepository;
import com.greenloop.event_service.services.EventQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventQueryServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventQueryService eventQueryService;

    private Event testEvent;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();

        testEvent = Event.builder()
                .id(eventId)
                .name("Test Event")
                .description("Description")
                .capacity(100)
                .coins(50)
                .organizer("Organizer")
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(2))
                .imageUrl("http://example.com/image.jpg")
                .type(EventType.WORKSHOP)
                .status(EventStatus.REGISTRATION)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllEvents_Success() {
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll()).thenReturn(events);

        List<EventResponse> responses = eventQueryService.getAllEvents();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(eventId);
        assertThat(responses.get(0).getName()).isEqualTo(testEvent.getName());
        verify(eventRepository).findAll();
    }

    @Test
    void getAllEvents_EmptyList() {
        when(eventRepository.findAll()).thenReturn(Collections.emptyList());

        List<EventResponse> responses = eventQueryService.getAllEvents();

        assertThat(responses).isEmpty();
        verify(eventRepository).findAll();
    }

    @Test
    void getEventById_Success() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        EventResponse response = eventQueryService.getEventById(eventId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(eventId);
        assertThat(response.getName()).isEqualTo(testEvent.getName());
        verify(eventRepository).findById(eventId);
    }

    @Test
    void getEventById_NotFound_ThrowsException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventQueryService.getEventById(eventId))
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
                .type(EventType.WORKSHOP)
                .status(EventStatus.REGISTRATION)
                .build();

        Event pastEvent = Event.builder()
                .id(UUID.randomUUID())
                .name("Past Event")
                .startDateTime(now.minusDays(2))
                .endDateTime(now.minusDays(1))
                .type(EventType.WORKSHOP)
                .status(EventStatus.CLOSED)
                .build();

        when(eventRepository.findAllEventsByAttendeeId(userId))
                .thenReturn(Arrays.asList(futureEvent, pastEvent));

        List<EventResponse> responses = eventQueryService.upcomingEventForUser(userId);

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
                .type(EventType.WORKSHOP)
                .status(EventStatus.CLOSED)
                .build();

        when(eventRepository.findAllEventsByAttendeeId(userId))
                .thenReturn(Collections.singletonList(pastEvent));

        List<EventResponse> responses = eventQueryService.upcomingEventForUser(userId);

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
                .type(EventType.TREE_PLANTING)
                .status(EventStatus.CLOSED)
                .build();

        Event futureEvent = Event.builder()
                .id(UUID.randomUUID())
                .name("Future Event")
                .startDateTime(now.plusDays(5))
                .endDateTime(now.plusDays(6))
                .type(EventType.WORKSHOP)
                .status(EventStatus.REGISTRATION)
                .build();

        when(eventRepository.findAllEventsByAttendeeId(userId))
                .thenReturn(Arrays.asList(pastEvent, futureEvent));

        List<EventResponse> responses = eventQueryService.pastEventsForUser(userId);

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
                .type(EventType.WORKSHOP)
                .status(EventStatus.REGISTRATION)
                .build();

        when(eventRepository.findAllEventsByAttendeeId(userId))
                .thenReturn(Collections.singletonList(futureEvent));

        List<EventResponse> responses = eventQueryService.pastEventsForUser(userId);

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
                .startDateTime(LocalDateTime.now().plusDays(3))
                .endDateTime(LocalDateTime.now().plusDays(4))
                .type(EventType.WORKSHOP)
                .status(EventStatus.REGISTRATION)
                .attendees(new ArrayList<>())
                .build();

        Event joinedEvent = Event.builder()
                .id(UUID.randomUUID())
                .name("Joined Event")
                .capacity(10)
                .startDateTime(LocalDateTime.now().plusDays(1))
                .endDateTime(LocalDateTime.now().plusDays(2))
                .type(EventType.WORKSHOP)
                .status(EventStatus.REGISTRATION)
                .attendees(List.of(joinedAttendee))
                .build();

        when(eventRepository.findAll()).thenReturn(List.of(availableEvent, joinedEvent));

        List<EventResponse> responses = eventQueryService.upcomingNotJoinedEventsForUser(userId);

        System.out.println(responses);
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("Available Event");
        verify(eventRepository).findAll();
    }

    @Test
    void getTotalOpenEvents_Success() {
        Event closed = Event.builder().status(EventStatus.CLOSED).build();
        Event open = Event.builder().status(EventStatus.REGISTRATION).build();

        when(eventRepository.findAll()).thenReturn(List.of(closed, open));

        long total = eventQueryService.getTotalOpenEvents();

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

        long total = eventQueryService.getUpcomingEventsNext30Days();

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

        long total = eventQueryService.getTotalParticipantsInOpenEvents();

        assertThat(total).isEqualTo(3);
        verify(eventRepository).findAll();
    }

    @Test
    void getAllEventTypes_ReturnsAllEnums() {
        EventType[] types = eventQueryService.getAllEventTypes();

        assertThat(types).isNotEmpty();
        assertThat(types).contains(EventType.WORKSHOP, EventType.TREE_PLANTING);
    }
}