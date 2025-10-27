package com.greenloop.event_service.services;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

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
                .type(EventType.valueOf(request.getType().toUpperCase()))
                .status(EventStatus.REGISTRATION)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(LocalDateTime.now())
                .build();

        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
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

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        Map<String, String> response = Collections.singletonMap("status", "Event Service is Up and Running!");
        return ResponseEntity.ok(response);
    }

    @Scheduled(fixedRate = 300000)
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // Transition events from REGISTRATION OR FULL to ONGOING
        eventRepository.updateStatusToOngoing(
                EventStatus.REGISTRATION,
                EventStatus.FULL,
                EventStatus.ONGOING,
                now);

        // Transition events from ONGOING to CLOSED
        eventRepository.updateStatusToClosed(
                EventStatus.ONGOING,
                EventStatus.CLOSED,
                now);
    }
}
