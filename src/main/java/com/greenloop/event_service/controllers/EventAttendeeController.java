package com.greenloop.event_service.controllers;

import com.greenloop.event_service.exceptions.RoleNotAllowedException;
import com.greenloop.event_service.models.EventAttendee;
import com.greenloop.event_service.services.EventAttendeeService;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/events/{eventId}")
public class EventAttendeeController {

    private final EventAttendeeService attendeeService;
    public EventAttendeeController(EventAttendeeService attendeeService) {
        this.attendeeService = attendeeService;
    }

    // register for event
    @PostMapping("/register")
    public EventAttendee registerAttendee(@PathVariable UUID eventId, @RequestHeader("X-User-ID") String userId, @RequestHeader("X-User-Email") String userEmail) {
        return attendeeService.registerAttendee(eventId, UUID.fromString(userId), userEmail);
    }
    // get all event attendees
    @GetMapping("/participants")
    public List<EventAttendee> getAllEventAttendees(@PathVariable UUID eventId, @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        return attendeeService.getAllEventAttendees(eventId);
    }
    // get one event attendee
    @GetMapping("/participants/profile")
    public EventAttendee getEventAttendee(@PathVariable UUID eventId, @RequestHeader("X-User-ID") String userId, @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        return attendeeService.getEventAttendee(eventId, UUID.fromString(userId));
    }

    // check attendance of attendee - for btn from user side
    @GetMapping("/is-registered")
    public boolean isRegistered(@PathVariable UUID eventId, @RequestHeader("X-User-ID") String userId) {
        return attendeeService.isUserRegistered(eventId, UUID.fromString(userId));
    }
    
    // remove attendee from event
    @DeleteMapping("/participants/{userId}")
    public void deregisterAttendee(@PathVariable UUID eventId, @PathVariable UUID userId) {
        attendeeService.deregisterAttendee(eventId, userId);
    }
}
