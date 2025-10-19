package com.greenloop.event_service.services;

import com.greenloop.event_service.dtos.RegisterRequest;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.models.EventAttendee;
import com.greenloop.event_service.repos.EventAttendeeRepository;
import com.greenloop.event_service.repos.EventRepository;

import java.util.*;

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
        event.addAttendeeToEvent(newAttendee);

        return newAttendee;
    }

    public List<EventAttendee> getAllEventAttendees(UUID id) {
        Event event = eventRepo.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
        return event.getAttendees();
    }

    public EventAttendee getEventAttendee(UUID id, UUID userId) {
        return attendeeRepo.findByUserIdAndEventId(userId, id).orElseThrow(() -> new RuntimeException("Attendee for this event is not found"));
    }

    public void deregisterAttendee(UUID id, UUID userId) {
        // need to check if event will update
        EventAttendee attendee = attendeeRepo.findByUserIdAndEventId(userId, id).orElseThrow(() -> new RuntimeException("Attendee for this event is not found"));
        attendeeRepo.delete(attendee);
    }
}
