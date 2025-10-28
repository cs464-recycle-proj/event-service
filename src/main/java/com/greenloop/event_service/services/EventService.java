package com.greenloop.event_service.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.greenloop.event_service.dtos.CreateEventRequest;
import com.greenloop.event_service.dtos.EventResponse;
import com.greenloop.event_service.dtos.UpdateEventRequest;
import com.greenloop.event_service.enums.EventStatus;
import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.repos.*;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public EventResponse createEvent(CreateEventRequest request) {
        Event event = Event.builder()
                .capacity(request.getCapacity())
                .name(request.getName())
                .description(request.getDescription())
                .coins(request.getCoins())
                .organizer(request.getOrganizer())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .imageUrl(request.getImageUrl())
                .location(request.getLocation())
                .type(EventType.valueOf(request.getType().toUpperCase()))
                .status(EventStatus.REGISTRATION)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(LocalDateTime.now())
                .build();

        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public EventResponse getEventById(UUID id) {
        return eventRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EventNotFoundException("Event with id " + id + " is not found"));
    }

    public EventResponse updateEvent(UUID id, UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event with id " + id + " is not found"));
        event.updateFromRequest(request);
        return mapToResponse(eventRepository.save(event));
    }

    public void deleteEvent(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException("Event with id " + id + " is not found");
        }
        eventRepository.deleteById(id);
    }

    public List<EventResponse> upcomingEventForUser(UUID userId) {
        List<Event> events = eventRepository.findAllEventsByAttendeeId(userId);
        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .filter(e -> e.getStartDateTime().isAfter(now))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<EventResponse> pastEventsForUser(UUID userId) {
        List<Event> events = eventRepository.findAllEventsByAttendeeId(userId);
        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .filter(e -> e.getEndDateTime().isBefore(now))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

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

    // @Scheduled(fixedRate = 300000)
    // public void updateEventStatuses() {
    //     LocalDateTime now = LocalDateTime.now();

    //     // Transition events from REGISTRATION OR FULL to ONGOING
    //     eventRepository.updateStatusToOngoing(
    //             EventStatus.REGISTRATION,
    //             EventStatus.FULL,
    //             EventStatus.ONGOING,
    //             now);

    //     // Transition events from ONGOING to CLOSED
    //     eventRepository.updateStatusToClosed(
    //             EventStatus.ONGOING,
    //             EventStatus.CLOSED,
    //             now);
    // }
}
