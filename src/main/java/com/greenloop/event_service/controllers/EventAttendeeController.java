package com.greenloop.event_service.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.greenloop.event_service.dtos.RegisterRequest;
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

    // ----- event user management -----
    @PostMapping("/register")
    public EventAttendee registerAttendee(@PathVariable UUID eventId, @RequestBody RegisterRequest request) {
        return attendeeService.registerAttendee(eventId, request);
    }

    @GetMapping("/{id}/users")
    public List<EventAttendee> getAllEventAttendees(@PathVariable UUID eventId) {
        return attendeeService.getAllEventAttendees(eventId);
    }

    @GetMapping("/{id}/users/{userId}")
    public EventAttendee getEventAttendee(@PathVariable UUID eventId, @PathVariable UUID userId) {
        return attendeeService.getEventAttendee(eventId, userId);
    }

    @PatchMapping("/{id}/users/{userId}")
    public EventAttendee markAttendee(@PathVariable UUID eventId, @PathVariable UUID userId) {
        return attendeeService.markedAttendee(eventId, userId);
    }

    @DeleteMapping("/{id}/users/{userId}")
    public void deregisterAttendee(@PathVariable UUID eventId, @PathVariable UUID userId) {
        attendeeService.deregisterAttendee(eventId, userId);
    }
}
