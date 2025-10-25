package com.greenloop.event_service.services;

import com.greenloop.event_service.dtos.RegisterRequestDTO;
import com.greenloop.event_service.exceptions.AttendeeNotFoundException;
import com.greenloop.event_service.exceptions.EventFullException;
import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.models.EventAttendee;
import com.greenloop.event_service.repos.EventAttendeeRepository;
import com.greenloop.event_service.repos.EventRepository;

import java.util.*;

import org.springframework.stereotype.Service;

@Service
public class EventAttendeeService {

    private final EventRepository eventRepo;
    private final EventAttendeeRepository attendeeRepo;

    public EventAttendeeService(EventRepository eventRepo, EventAttendeeRepository attendeeRepo) {
        this.eventRepo = eventRepo;
        this.attendeeRepo = attendeeRepo;
    }

    // register attendee to event
    public EventAttendee registerAttendee(UUID eventId, RegisterRequestDTO request) {
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (event.getAttendeeCount() >= event.getCapacity()) {
            throw new EventFullException(event.getId());
        }

        EventAttendee newAttendee = new EventAttendee(request);
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
}
