package com.greenloop.event_service.services;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.greenloop.event_service.enums.EventStatus;
import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.exceptions.AttendeeNotFoundException;
import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.repos.EventAttendeeRepository;
import com.greenloop.event_service.repos.EventRepository;

import java.util.*;

@Service
public class EventQueryService {
    private final EventRepository eventRepo;
        private final EventAttendeeRepository attendeeRepo;


    public EventQueryService(EventRepository eventRepo, EventAttendeeRepository attendeeRepo) {
        this.eventRepo = eventRepo;
        this.attendeeRepo = attendeeRepo;
    }

    // get all events 
    public List<Event> getAllEvents() {
        return eventRepo.findAll();
    }

    // get event by id
    public Event getEventById(UUID id) {
        return eventRepo.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    // ---------- USER-EVENT RELATIONS ----------

    // get upcoming events for user
    public List<Event> upcomingEventForUser(UUID userId) {

        attendeeRepo.findByUserId(userId)
                .orElseThrow(() -> new AttendeeNotFoundException(userId));

        List<Event> events = eventRepo.findAllEventsByAttendeeId(userId);
        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .filter(e -> e.getStartDT().isAfter(now)) // upcoming only
                .collect(Collectors.toList());
    }

    // get past events for user
    public List<Event> pastEventsForUser(UUID userId) {

        attendeeRepo.findByUserId(userId)
                .orElseThrow(() -> new AttendeeNotFoundException(userId));

        List<Event> events = eventRepo.findAllEventsByAttendeeId(userId);
        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .filter(e -> e.getEndDT().isBefore(now)) // past only
                .collect(Collectors.toList());
    }

    // get upcoming events that user can join
    public List<Event> upcomingNotJoinedEventsForUser(UUID userId) {
        LocalDateTime now = LocalDateTime.now();

        // get all upcoming events (not closed)
        List<Event> upcomingEvents = eventRepo.findAll().stream()
                .filter(e -> e.getEndDT().isAfter(now)) // only events not ended
                .filter(e -> e.getAttendeeCount() < e.getCapacity()) // only events with available slots
                .filter(e -> e.getAttendees().stream()
                        .noneMatch(a -> a.getUserId().equals(userId))) // exclude events the user has joined
                .collect(Collectors.toList());

        return upcomingEvents;
    }

    // ---------- EVENT ANALYTICS ----------

    // Total events (not closed)
    public long getTotalOpenEvents() {
        return eventRepo.findAll().stream()
                .filter(e -> EventStatus.CLOSED != e.getStatus())
                .count();
    }

    // Upcoming events in next 30 days
    public List<Event> getUpcomingEventsNext30Days() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next30Days = now.plusDays(30);
        return eventRepo.findAll().stream()
                .filter(e -> e.getStartDT() != null && e.getStartDT().isAfter(now)
                        && e.getStartDT().isBefore(next30Days))
                .collect(Collectors.toList());
    }

    // Total participants in open events
    public long getTotalParticipantsInOpenEvents() {
        return eventRepo.findAll().stream()
                .filter(e -> EventStatus.CLOSED != e.getStatus())
                .mapToLong(e -> e.getAttendees() != null ? e.getAttendees().size() : 0)
                .sum();
    }

    // Get all event types
    public EventType[] getAllEventTypes() {
        return EventType.values();
    }
}
