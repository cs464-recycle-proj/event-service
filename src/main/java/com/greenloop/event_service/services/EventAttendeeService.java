package com.greenloop.event_service.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.greenloop.event_service.dtos.RegisterRequest;
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
    
    // ----- event user management -----
    public EventAttendee registerAttendee(UUID id, RegisterRequest request) {
        Event event = eventRepo.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));

        EventAttendee newAttendee = new EventAttendee(request); 
        newAttendee.setEvent(event);
        EventAttendee saved = attendeeRepo.save(newAttendee);
        // ensure event relation maintained
        event.addAttendeeToEvent(saved);
        eventRepo.save(event);
        return saved;
    }

    public List<EventAttendee> getAllEventAttendees(UUID id) {
        Event event = eventRepo.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
        return event.getAttendees();
    }

    public EventAttendee getEventAttendee(UUID id, UUID userId) {
        return attendeeRepo.findByUserIdAndEventId(userId, id).orElseThrow(() -> new RuntimeException("Attendee for this event is not found"));
    }

    public void deregisterAttendee(UUID id, UUID userId) {
        EventAttendee attendee = attendeeRepo.findByUserIdAndEventId(userId, id).orElseThrow(() -> new RuntimeException("Attendee for this event is not found"));
        attendeeRepo.delete(attendee);
    }

    /**
     * Mark attendance using a QR token. If attendee doesn't exist, create a registration entry.
     */
    public EventAttendee markAttendanceByToken(ScanRequest req) {
        Event event = eventRepo.findByQrToken(req.getQrToken()).orElseThrow(() -> new RuntimeException("Event not found for provided QR token"));

        UUID userId = req.getUserId();
        EventAttendee attendee = attendeeRepo.findByUserIdAndEventId(userId, event.getId()).orElse(null);
        if (attendee == null) {
            // create a new attendee record
            RegisterRequest r = new RegisterRequest();
            r.setUserId(req.getUserId());
            r.setUsername(req.getUsername());
            r.setUserEmail(req.getUserEmail());
            EventAttendee newAtt = new EventAttendee(r);
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
