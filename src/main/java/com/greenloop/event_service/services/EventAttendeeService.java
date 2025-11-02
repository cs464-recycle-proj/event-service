package com.greenloop.event_service.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenloop.event_service.exceptions.AlreadyRegisteredException;
import com.greenloop.event_service.exceptions.AttendanceAlreadyMarkedException;
import com.greenloop.event_service.exceptions.AttendeeNotRegisteredException;
import com.greenloop.event_service.exceptions.EventFullException;
import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.exceptions.InvalidEventStateException;
import com.greenloop.event_service.exceptions.ResourceNotFoundException;
import com.greenloop.event_service.dtos.EventAttendeeResponse;
import com.greenloop.event_service.dtos.ScanRequest;
import com.greenloop.event_service.enums.EventStatus;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.models.EventAttendee;
import com.greenloop.event_service.repos.EventAttendeeRepository;
import com.greenloop.event_service.repos.EventRepository;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for managing event attendee operations.
 * <p>
 * Handles attendee registration, deregistration, attendance tracking via QR
 * codes,
 * and attendee status queries. All operations are transactional to ensure data
 * consistency.
 * </p>
 * 
 * @author GreenLoop Team
 * @version 1.0
 */
@Service
@Transactional
@AllArgsConstructor
public class EventAttendeeService {

    private final EventRepository eventRepository;
    private final EventAttendeeRepository attendeeRepository;
    private final EventMessagePublisher messagePublisher;
    private final NotificationPublisher notificationPublisher;

    /**
     * Registers a user as an attendee for an event
     * <p>
     * Validates that the user is not already registered and that the event has
     * available capacity.
     * </p>
     *
     * @param eventId   the UUID of the event
     * @param userid    the UUID of the user to register
     * @param userEmail the email of the user to register
     * @return EventAttendeeResponse containing the registration details
     * @throws EventNotFoundException     if the event does not exist
     * @throws AlreadyRegisteredException if the user is already registered for this
     *                                    event
     * @throws EventFullException         if the event is at full capacity
     */
    public EventAttendeeResponse registerAttendee(UUID eventId, UUID userid, String userEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event with id " + eventId + " is not found"));
    
        if (attendeeRepository.existsByUserIdAndEventId(userid, eventId)) {
            throw new AlreadyRegisteredException("User already registered for this event");
        }

        if (event.getCapacity() != -1 && event.getAttendeeCount() >= event.getCapacity()) {
            throw new EventFullException("Event with id " + eventId + " is full");
        }

        EventAttendee newAttendee = EventAttendee.builder()
                .userId(userid)
                .userEmail(userEmail)
                .event(event)
                .build();

        event.addAttendeeToEvent(newAttendee);
        
        // Send event confirmation email to the attendee
        notificationPublisher.publishEventConfirmation(userEmail, event);

        return mapToResponse(newAttendee);
    }

    /**
     * Retrieves all attendees registered for a specific event.
     *
     * @param eventId the UUID of the event
     * @return list of all attendees for the event
     * @throws EventNotFoundException if the event does not exist
     */
    public List<EventAttendeeResponse> getAllEventAttendees(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event with id " + eventId + " is not found"));
        return event.getAttendees()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a user is registered for a specific event.
     *
     * @param eventId the UUID of the event
     * @param userId  the UUID of the user
     * @return true if user is registered, false otherwise
     */
    public boolean isUserRegistered(UUID eventId, UUID userId) {
        return attendeeRepository.existsByUserIdAndEventId(userId, eventId);
    }

    /**
     * Deregisters a user from an event.
     *
     * @param eventId the UUID of the event
     * @param userId  the UUID of the user to deregister
     * @throws ResourceNotFoundException if the attendee record does not exist
     */
    public void deregisterAttendee(UUID eventId, UUID userId) {
        EventAttendee attendee = attendeeRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendee with id " + userId + " is not found"));
        attendeeRepository.delete(attendee);
    }

    /**
     * Marks a user's attendance at an event using a QR code token.
     * <p>
     * Validates that:
     * <ul>
     * <li>The event is in ONGOING status</li>
     * <li>The user is registered for the event</li>
     * <li>The user has not already marked attendance</li>
     * </ul>
     * </p>
     *
     * @param req       the scan request containing the QR token
     * @param userId    the UUID of the user marking attendance
     * @param userEmail the email of the user marking attendance
     * @return EventAttendeeResponse with updated attendance details
     * @throws ResourceNotFoundException        if the event or attendee record is
     *                                          not found
     * @throws InvalidEventStateException       if the event is not in ONGOING
     *                                          status
     * @throws AttendeeNotRegisteredException   if the user did not register for the
     *                                          event
     * @throws AttendanceAlreadyMarkedException if the user already marked
     *                                          attendance
     */
    public EventAttendeeResponse markAttendanceByToken(ScanRequest req, UUID userId, String userEmail) {
        Event event = eventRepository.findByQrToken(req.getQrToken())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found for provided QR token"));

        if (event.getStatus() != EventStatus.ONGOING) {
            throw new InvalidEventStateException("Attendance can only be marked for ongoing events");
        }

        EventAttendee attendee = attendeeRepository.findByUserIdAndEventId(userId, event.getId())
                .orElseThrow(() -> new AttendeeNotRegisteredException(
                        "User with ID " + userId + " did not register for this event"));

        if (attendee.isAttended()) {
            throw new AttendanceAlreadyMarkedException(
                    "User with ID " + userId + " has already marked attendance for this event");
        }

        attendee.setAttended(true);
        attendee.setAttendedAt(LocalDateTime.now());
        EventAttendee savedAttendee = attendeeRepository.save(attendee);
        
        // Publish event participation message to gamification service
        // User earns coins from the event
        messagePublisher.publishEventParticipation(event, userId, event.getCoins());
        
        // Send attendance confirmation email
        notificationPublisher.publishEventAttendance(userEmail, event.getName(), event.getCoins());
        
        return mapToResponse(savedAttendee);
    }

    /**
     * Retrieves a specific attendee record for an event.
     *
     * @param eventId the UUID of the event
     * @param userId  the UUID of the user/attendee
     * @return EventAttendeeResponse containing the attendee details
     * @throws ResourceNotFoundException if the attendee record does not exist
     */
    public EventAttendeeResponse getEventAttendeeById(UUID eventId, UUID userId) {
        EventAttendee attendee = attendeeRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendee did not sign up for this event"));
        return mapToResponse(attendee);
    }

    /**
     * Maps an EventAttendee entity to an EventAttendeeResponse DTO.
     *
     * @param attendee the EventAttendee entity to map
     * @return EventAttendeeResponse DTO
     */
    private EventAttendeeResponse mapToResponse(EventAttendee attendee) {
        return EventAttendeeResponse.builder()
                .id(attendee.getId())
                .userId(attendee.getUserId())
                .userEmail(attendee.getUserEmail())
                .registeredAt(attendee.getRegisteredAt())
                .attended(attendee.isAttended())
                .attendedAt(attendee.getAttendedAt())
                .build();
    }
}
