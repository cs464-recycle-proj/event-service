package com.greenloop.event_service.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenloop.event_service.exceptions.AttendanceAlreadyMarkedException;
import com.greenloop.event_service.exceptions.AttendeeNotRegisteredException;
import com.greenloop.event_service.exceptions.EventFullException;
import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.exceptions.ResourceNotFoundException;
import com.greenloop.event_service.dtos.EventAttendeeResponse;
import com.greenloop.event_service.dtos.ScanRequest;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.models.EventAttendee;
import com.greenloop.event_service.repos.EventAttendeeRepository;
import com.greenloop.event_service.repos.EventRepository;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class EventAttendeeService {

    private final EventRepository eventRepository;
    private final EventAttendeeRepository attendeeRepository;

    public EventAttendeeResponse registerAttendee(UUID eventId, UUID userid, String userEmail) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event with id " + eventId + " is not found"));

        if (event.getAttendeeCount() >= event.getCapacity()) {
            throw new EventFullException(event.getId());
        }

        EventAttendee newAttendee = EventAttendee.builder()
                .userId(userid)
                .userEmail(userEmail)
                .event(event)
                .build();

        event.addAttendeeToEvent(newAttendee);

        return mapToResponse(newAttendee);
    }

    public List<EventAttendeeResponse> getAllEventAttendees(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event with id " + eventId + " is not found"));
        return event.getAttendees()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public boolean isUserRegistered(UUID eventId, UUID userId) {
        return attendeeRepository.existsByUserIdAndEventId(userId, eventId);
    }

    public void deregisterAttendee(UUID eventId, UUID userId) {
        EventAttendee attendee = attendeeRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendee with id " + userId + " is not found"));
        attendeeRepository.delete(attendee);
    }

    public EventAttendeeResponse markAttendanceByToken(ScanRequest req, UUID userId, String userEmail) {
        Event event = eventRepository.findByQrToken(req.getQrToken())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found for provided QR token"));

        EventAttendee attendee = attendeeRepository.findByUserIdAndEventId(userId, event.getId())
                .orElseThrow(() -> new AttendeeNotRegisteredException(
                        "User with ID " + userId + " did not register for this event"));
        
        if (Boolean.TRUE.equals(attendee.isAttended())) {
        throw new AttendanceAlreadyMarkedException(
                "User with ID " + userId + " has already marked attendance for this event");
    }

        attendee.setAttended(true);
        attendee.setAttendedAt(LocalDateTime.now());
        return mapToResponse(attendeeRepository.save(attendee));
    }

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
