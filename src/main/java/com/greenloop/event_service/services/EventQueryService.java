package com.greenloop.event_service.services;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.greenloop.event_service.dtos.EventResponse;
import com.greenloop.event_service.enums.EventStatus;
import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.repos.EventRepository;

import java.util.*;

@Service
public class EventQueryService {
    private final EventRepository eventRepository;

    public EventQueryService(EventRepository eventRepo) {
        this.eventRepository = eventRepo;
    }

    // get all events
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // get event by id
    public EventResponse getEventById(UUID id) {
        return eventRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EventNotFoundException("Event with id " + id + " is not found"));
    }


    // ---------- USER-EVENT RELATIONS ----------

    // get upcoming events for user
    public List<EventResponse> upcomingEventForUser(UUID userId) {
        List<Event> events = eventRepository.findAllEventsByAttendeeId(userId);
        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .filter(e -> e.getStartDateTime().isAfter(now))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // get past events for user
    public List<EventResponse> pastEventsForUser(UUID userId) {
        List<Event> events = eventRepository.findAllEventsByAttendeeId(userId);
        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .filter(e -> e.getEndDateTime().isBefore(now))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // get upcoming events that user can join
    public List<EventResponse> upcomingNotJoinedEventsForUser(UUID userId) {
        LocalDateTime now = LocalDateTime.now();

        // get all upcoming events (not closed)
        List<EventResponse> upcomingEvents = eventRepository.findAll().stream()
                .filter(e -> e.getEndDateTime().isAfter(now)) // only events not ended
                .filter(e -> e.getAttendeeCount() < e.getCapacity()) // only events with available slots
                .filter(e -> e.getAttendees().stream()
                        .noneMatch(a -> a.getUserId().equals(userId))) // exclude events the user has joined
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return upcomingEvents;
    }

    // ---------- EVENT ANALYTICS ----------

    // Total events (not closed)
    public long getTotalOpenEvents() {
        return eventRepository.findAll().stream()
                .filter(e -> EventStatus.CLOSED != e.getStatus())
                .count();
    }

    // Upcoming events in next 30 days
    public long getUpcomingEventsNext30Days() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next30Days = now.plusDays(30);

        return eventRepository.findAll().stream()
                .filter(e -> e.getStartDateTime() != null && e.getStartDateTime().isAfter(now)
                        && e.getStartDateTime().isBefore(next30Days))
                .count();
    }

    // Total participants in open events
    public long getTotalParticipantsInOpenEvents() {
        return eventRepository.findAll().stream()
                .filter(e -> EventStatus.CLOSED != e.getStatus())
                .mapToLong(e -> e.getAttendees() != null ? e.getAttendees().size() : 0)
                .sum();
    }

    // Get all event types
    public EventType[] getAllEventTypes() {
        return EventType.values();
    }

    // ---------- HELPER ----------
    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .capacity(event.getCapacity())
                .coins(event.getCoins())
                .description(event.getDescription())
                .imageUrl(event.getImageUrl())
                .location(event.getLocation())
                .name(event.getName())
                .organizer(event.getOrganizer())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .type(event.getType().name())
                .status(event.getStatus().name())
                .qrToken(event.getQrToken())
                .qrGeneratedAt(event.getQrGeneratedAt())
                .attendeeCount(event.getAttendeeCount())
                .build();
    }
}
