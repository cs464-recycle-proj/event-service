package com.greenloop.event_service.controllers;

import com.greenloop.event_service.dtos.RegisterRequest;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.models.EventAttendee;
import com.greenloop.event_service.services.EventAttendeeService;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/events/{eventId}")
public class EventAttendeeController {

    private final EventAttendeeService attendeeService;
    public EventAttendeeController(EventAttendeeService attendeeService) {
        this.attendeeService = attendeeService;
    }

    // register for event
    @PostMapping("/register")
    public EventAttendee registerAttendee(@PathVariable UUID eventId, @RequestBody RegisterRequest request) {
        return attendeeService.registerAttendee(eventId, request);
    }
    // get all event attendees
    @GetMapping("/{id}/participants")
    public List<EventAttendee> getAllEventAttendees(@PathVariable UUID eventId) {
        return attendeeService.getAllEventAttendees(eventId);
    }
    // get one event attendee
    @GetMapping("/{id}/participants/{userId}")
    public EventAttendee getEventAttendee(@PathVariable UUID eventId, @PathVariable UUID userId) {
        return attendeeService.getEventAttendee(eventId, userId);
    }
    // mark attendances of attendee
    @PostMapping("/{id}/attendance")
    public boolean markAttendee(@PathVariable UUID eventId, @RequestHeader UUID userId) {
        return attendeeService.markedAttendee(eventId, userId);
    }
    // check attendance of attendee
    @GetMapping("/{id}/is-registered")
    public boolean isRegistered(@PathVariable UUID eventId, @RequestHeader UUID userId) {
        return attendeeService.isUserRegistered(eventId, userId);
    }
    // get upcoming events for user
    @GetMapping("/upcoming-for-user")
    public List<Event> upcomingEventsForUser(@RequestHeader UUID userId) {
        return attendeeService.upcomingEventForUser(userId);
    }
    // get upcoming events for unjoined user
    @GetMapping("/upcoming")
    public List<Event> upcomingNotJoinedEvents(@RequestHeader UUID userId) {
        return attendeeService.upcomingNotJoinedEvents(userId);
    }
    // get past events for user
    @GetMapping("/past")
    public List<Event> pastEventsForUser(@PathVariable UUID eventId, @RequestHeader UUID userId) {
        return attendeeService.pastEventsForUser(userId);
    }
    // remove attendee from event
    @DeleteMapping("/{id}/participants/{userId}")
    public void deregisterAttendee(@PathVariable UUID eventId, @PathVariable UUID userId) {
        attendeeService.deregisterAttendee(eventId, userId);
    }
}
