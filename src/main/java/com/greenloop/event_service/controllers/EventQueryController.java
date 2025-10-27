package com.greenloop.event_service.controllers;

import org.springframework.web.bind.annotation.*;

import java.util.*;

import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.services.EventQueryService;

@RestController
@RequestMapping("/api/events")
public class EventQueryController {

    private final EventQueryService queryService;

    public EventQueryController(EventQueryService queryService) {
        this.queryService = queryService;
    }

    // ---------------- PUBLIC / USER ---------------- //

    @GetMapping
    public List<Event> getAllEvents() {
        return queryService.getAllEvents();
    }

    @GetMapping("/{id}")
    public Event getEvent(@PathVariable UUID id) {
        return queryService.getEventById(id);
    }

    @GetMapping("/upcoming/joined")
    public List<Event> upcomingEventsForUser(@RequestHeader("X-User-ID") String userId) {
        return queryService.upcomingEventForUser(UUID.fromString(userId));
    }

    @GetMapping("/upcoming/unjoined")
    public List<Event> upcomingNotJoinedEvents(@RequestHeader("X-User-ID") String userId) {
        return queryService.upcomingNotJoinedEventsForUser(UUID.fromString(userId));
    }

    @GetMapping("/past")
    public List<Event> pastEventsForUser(@RequestHeader("X-User-ID") String userId) {
        return queryService.pastEventsForUser(UUID.fromString(userId));
    }

    // ---------------- ANALYTICS ---------------- //

    @GetMapping("/open/total")
    public long getTotalOpenEvents() {
        return queryService.getTotalOpenEvents();
    }

    @GetMapping("/upcoming/30days")
    public List<Event> getUpcomingEventsNext30Days() {
        return queryService.getUpcomingEventsNext30Days();
    }

    @GetMapping("/open/participants")
    public long getTotalParticipantsInOpenEvents() {
        return queryService.getTotalParticipantsInOpenEvents();
    }

    @GetMapping("/types")
    public EventType[] getAllEventTypes() {
        return queryService.getAllEventTypes();
    }
}