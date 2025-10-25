package com.greenloop.event_service.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenloop.event_service.exceptions.AttendeeNotFoundException;
import com.greenloop.event_service.exceptions.EventFullException;
import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.dtos.ScanRequest;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.models.EventAttendee;
import com.greenloop.event_service.repos.EventAttendeeRepository;
import com.greenloop.event_service.repos.EventRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class EventAttendeeService {

    private final EventRepository eventRepo;
    private final EventAttendeeRepository attendeeRepo;

    public EventAttendeeService(EventRepository eventRepo, EventAttendeeRepository attendeeRepo) {
        this.eventRepo = eventRepo;
        this.attendeeRepo = attendeeRepo;
    }

    // register attendee to event
    public EventAttendee registerAttendee(UUID eventId, UUID userid, String userEmail) {
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.getAttendeeCount() >= event.getCapacity()) {
            throw new EventFullException(event.getId());
        }

        EventAttendee newAttendee = new EventAttendee(userid, userEmail);
        event.addAttendeeToEvent(newAttendee);

        return newAttendee;
    }

    // get all attendees of event
    public List<EventAttendee> getAllEventAttendees(UUID eventId) {
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        return event.getAttendees();
    }

    // get an attendee of event
    public EventAttendee getEventAttendee(UUID eventId, UUID userId) {
        return attendeeRepo.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new AttendeeNotFoundException(eventId));
    }

    // mark attendee attendance
    public boolean markedAttendee(UUID eventId, UUID userId) {
        EventAttendee attendee = attendeeRepo.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new AttendeeNotFoundException(eventId));

        attendee.setAttended(true);
        attendeeRepo.save(attendee);
        return true;
    }

    // check if user is registered for event
    public boolean isUserRegistered(UUID eventId, UUID userId) {
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        EventAttendee attendee = attendeeRepo.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new AttendeeNotFoundException(eventId));

        return event.getAttendees().contains(attendee);
    }

    // deregister attendee from event
    public void deregisterAttendee(UUID eventId, UUID userId) {
        // TODO need to check if event will update
        EventAttendee attendee = attendeeRepo.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new AttendeeNotFoundException(eventId));
        attendeeRepo.delete(attendee);
    }

    /**
     * Mark attendance using a QR token. If attendee doesn't exist, create a
     * registration entry.
     */
    public EventAttendee markAttendanceByToken(ScanRequest req) {
        Event event = eventRepo.findByQrToken(req.getQrToken())
                .orElseThrow(() -> new RuntimeException("Event not found for provided QR token"));

        UUID userId = req.getUserId();
        EventAttendee attendee = attendeeRepo.findByUserIdAndEventId(userId, event.getId()).orElse(null);
        if (attendee == null) {
            // create a new attendee record
            EventAttendee newAtt = new EventAttendee(req.getUserId(), req.getUserEmail());
            newAtt.setEvent(event);
            attendee = attendeeRepo.save(newAtt);
            event.addAttendeeToEvent(attendee);
            eventRepo.save(event);
        }

        attendee.setAttended(true);
        attendee.setAttendedAt(LocalDateTime.now());
        return attendeeRepo.save(attendee);
    }
}
