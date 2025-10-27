package com.greenloop.event_service.unit;

import com.greenloop.event_service.dtos.EventAttendeeResponse;
import com.greenloop.event_service.dtos.ScanRequest;
import com.greenloop.event_service.enums.EventStatus;
import com.greenloop.event_service.exceptions.*;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.models.EventAttendee;
import com.greenloop.event_service.repos.EventAttendeeRepository;
import com.greenloop.event_service.repos.EventRepository;
import com.greenloop.event_service.services.EventAttendeeService;
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
class EventAttendeeServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventAttendeeRepository attendeeRepository;

    @InjectMocks
    private EventAttendeeService eventAttendeeService;

    private Event testEvent;
    private EventAttendee testAttendee;
    private UUID eventId;
    private UUID userId;
    private String userEmail;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        userId = UUID.randomUUID();
        userEmail = "test@example.com";

        testEvent = Event.builder()
                .id(eventId)
                .name("Test Event")
                .capacity(100)
                .status(EventStatus.REGISTRATION)
                .qrToken("test-qr-token")
                .attendees(new ArrayList<>())
                .build();

        testAttendee = EventAttendee.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .userEmail(userEmail)
                .event(testEvent)
                .registeredAt(LocalDateTime.now())
                .attended(false)
                .build();
    }

    @Test
    void registerAttendee_Success() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(attendeeRepository.existsByUserIdAndEventId(userId, eventId)).thenReturn(false);

        EventAttendeeResponse response = eventAttendeeService.registerAttendee(eventId, userId, userEmail);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getUserEmail()).isEqualTo(userEmail);
        assertThat(response.isAttended()).isFalse();
        verify(eventRepository).findById(eventId);
        verify(attendeeRepository).existsByUserIdAndEventId(userId, eventId);
    }

    @Test
    void registerAttendee_EventNotFound_ThrowsException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventAttendeeService.registerAttendee(eventId, userId, userEmail))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("Event with id " + eventId + " is not found");

        verify(eventRepository).findById(eventId);
        verify(attendeeRepository, never()).existsByUserIdAndEventId(any(), any());
    }

    @Test
    void registerAttendee_AlreadyRegistered_ThrowsException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(attendeeRepository.existsByUserIdAndEventId(userId, eventId)).thenReturn(true);

        assertThatThrownBy(() -> eventAttendeeService.registerAttendee(eventId, userId, userEmail))
                .isInstanceOf(AlreadyRegisteredException.class)
                .hasMessageContaining("User already registered for this event");

        verify(eventRepository).findById(eventId);
        verify(attendeeRepository).existsByUserIdAndEventId(userId, eventId);
    }

    @Test
    void registerAttendee_EventFull_ThrowsException() {
        testEvent.setCapacity(1);
        testEvent.getAttendees().add(EventAttendee.builder().id(UUID.randomUUID()).build());

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(attendeeRepository.existsByUserIdAndEventId(userId, eventId)).thenReturn(false);

        assertThatThrownBy(() -> eventAttendeeService.registerAttendee(eventId, userId, userEmail))
                .isInstanceOf(EventFullException.class)
                .hasMessageContaining("Event with id " + eventId + " is full");

        verify(eventRepository).findById(eventId);
        verify(attendeeRepository).existsByUserIdAndEventId(userId, eventId);
    }

    @Test
    void getAllEventAttendees_Success() {
        testEvent.getAttendees().add(testAttendee);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        List<EventAttendeeResponse> responses = eventAttendeeService.getAllEventAttendees(eventId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(userId);
        assertThat(responses.get(0).getUserEmail()).isEqualTo(userEmail);
        verify(eventRepository).findById(eventId);
    }

    @Test
    void getAllEventAttendees_EventNotFound_ThrowsException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventAttendeeService.getAllEventAttendees(eventId))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessageContaining("Event with id " + eventId + " is not found");

        verify(eventRepository).findById(eventId);
    }

    @Test
    void isUserRegistered_ReturnsTrue() {
        when(attendeeRepository.existsByUserIdAndEventId(userId, eventId)).thenReturn(true);

        boolean result = eventAttendeeService.isUserRegistered(eventId, userId);

        assertThat(result).isTrue();
        verify(attendeeRepository).existsByUserIdAndEventId(userId, eventId);
    }

    @Test
    void isUserRegistered_ReturnsFalse() {
        when(attendeeRepository.existsByUserIdAndEventId(userId, eventId)).thenReturn(false);

        boolean result = eventAttendeeService.isUserRegistered(eventId, userId);

        assertThat(result).isFalse();
        verify(attendeeRepository).existsByUserIdAndEventId(userId, eventId);
    }

    @Test
    void deregisterAttendee_Success() {
        when(attendeeRepository.findByUserIdAndEventId(userId, eventId))
                .thenReturn(Optional.of(testAttendee));

        eventAttendeeService.deregisterAttendee(eventId, userId);

        verify(attendeeRepository).findByUserIdAndEventId(userId, eventId);
        verify(attendeeRepository).delete(testAttendee);
    }

    @Test
    void deregisterAttendee_AttendeeNotFound_ThrowsException() {
        when(attendeeRepository.findByUserIdAndEventId(userId, eventId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventAttendeeService.deregisterAttendee(eventId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Attendee with id " + userId + " is not found");

        verify(attendeeRepository).findByUserIdAndEventId(userId, eventId);
        verify(attendeeRepository, never()).delete(any());
    }

    @Test
    void markAttendanceByToken_Success() {
        testEvent.setStatus(EventStatus.ONGOING);
        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrToken("test-qr-token");

        when(eventRepository.findByQrToken("test-qr-token")).thenReturn(Optional.of(testEvent));
        when(attendeeRepository.findByUserIdAndEventId(userId, eventId))
                .thenReturn(Optional.of(testAttendee));
        when(attendeeRepository.save(any(EventAttendee.class))).thenReturn(testAttendee);

        EventAttendeeResponse response = eventAttendeeService.markAttendanceByToken(
                scanRequest, userId, userEmail);

        assertThat(response).isNotNull();
        assertThat(testAttendee.isAttended()).isTrue();
        assertThat(testAttendee.getAttendedAt()).isNotNull();
        verify(eventRepository).findByQrToken("test-qr-token");
        verify(attendeeRepository).findByUserIdAndEventId(userId, eventId);
        verify(attendeeRepository).save(testAttendee);
    }

    @Test
    void markAttendanceByToken_EventNotFound_ThrowsException() {
        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrToken("invalid-token");

        when(eventRepository.findByQrToken("invalid-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventAttendeeService.markAttendanceByToken(
                scanRequest, userId, userEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found for provided QR token");

        verify(eventRepository).findByQrToken("invalid-token");
        verify(attendeeRepository, never()).findByUserIdAndEventId(any(), any());
    }

    @Test
    void markAttendanceByToken_EventNotOngoing_ThrowsException() {
        testEvent.setStatus(EventStatus.REGISTRATION);
        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrToken("test-qr-token");

        when(eventRepository.findByQrToken("test-qr-token")).thenReturn(Optional.of(testEvent));

        assertThatThrownBy(() -> eventAttendeeService.markAttendanceByToken(
                scanRequest, userId, userEmail))
                .isInstanceOf(InvalidEventStateException.class)
                .hasMessageContaining("Attendance can only be marked for ongoing events");

        verify(eventRepository).findByQrToken("test-qr-token");
        verify(attendeeRepository, never()).findByUserIdAndEventId(any(), any());
    }

    @Test
    void markAttendanceByToken_AttendeeNotRegistered_ThrowsException() {
        testEvent.setStatus(EventStatus.ONGOING);
        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrToken("test-qr-token");

        when(eventRepository.findByQrToken("test-qr-token")).thenReturn(Optional.of(testEvent));
        when(attendeeRepository.findByUserIdAndEventId(userId, eventId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventAttendeeService.markAttendanceByToken(
                scanRequest, userId, userEmail))
                .isInstanceOf(AttendeeNotRegisteredException.class)
                .hasMessageContaining("User with ID " + userId + " did not register for this event");

        verify(eventRepository).findByQrToken("test-qr-token");
        verify(attendeeRepository).findByUserIdAndEventId(userId, eventId);
    }

    @Test
    void markAttendanceByToken_AlreadyMarked_ThrowsException() {
        testEvent.setStatus(EventStatus.ONGOING);
        testAttendee.setAttended(true);
        testAttendee.setAttendedAt(LocalDateTime.now());
        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setQrToken("test-qr-token");

        when(eventRepository.findByQrToken("test-qr-token")).thenReturn(Optional.of(testEvent));
        when(attendeeRepository.findByUserIdAndEventId(userId, eventId))
                .thenReturn(Optional.of(testAttendee));

        assertThatThrownBy(() -> eventAttendeeService.markAttendanceByToken(
                scanRequest, userId, userEmail))
                .isInstanceOf(AttendanceAlreadyMarkedException.class)
                .hasMessageContaining("User with ID " + userId + " has already marked attendance");

        verify(eventRepository).findByQrToken("test-qr-token");
        verify(attendeeRepository).findByUserIdAndEventId(userId, eventId);
        verify(attendeeRepository, never()).save(any());
    }
}