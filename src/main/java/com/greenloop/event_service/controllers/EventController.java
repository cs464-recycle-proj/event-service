package com.greenloop.event_service.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

import com.greenloop.event_service.exceptions.RoleNotAllowedException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.services.EventService;

import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/events")
public class EventController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        Map<String, String> response = Collections.singletonMap("status", "Event Service is Up and Running!");
        return ResponseEntity.ok(response);
    }

    private final EventService eventService;
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Event createEvent(@RequestBody Event event, @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        return eventService.createEvent(event);
    }

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public Event getEvent(@PathVariable UUID id) {
        return eventService.getEventById(id);
    }

    @GetMapping(value = "/{id}/qr", produces = "image/png")
    public @ResponseBody byte[] getEventQr(@PathVariable UUID id, @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        // default size 300x300
        return eventService.getQrCodeImage(id, 300, 300);
    }

    @PutMapping("/{id}")
    public Event updateEvent(@PathVariable UUID id, @Valid @RequestBody Event event, @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        return eventService.updateEvent(id, event);
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable UUID id, @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        eventService.deleteEvent(id);
    }

    // get upcoming events for user
    @GetMapping("/upcoming/joined")
    public List<Event> upcomingEventsForUser(@RequestHeader("X-User-ID") String userId) {
        return eventService.upcomingEventForUser(UUID.fromString(userId));
    }
    // get upcoming events for unjoined user
    @GetMapping("/upcoming/unjoined")
    public List<Event> upcomingNotJoinedEvents(@RequestHeader("X-User-ID") String userId) {
        return eventService.upcomingNotJoinedEvents(UUID.fromString(userId));
    }
    // get past events for user
    @GetMapping("/past")
    public List<Event> pastEventsForUser(@PathVariable UUID eventId, @RequestHeader("X-User-ID") String userId) {
        return eventService.pastEventsForUser(UUID.fromString(userId));
    }

}
