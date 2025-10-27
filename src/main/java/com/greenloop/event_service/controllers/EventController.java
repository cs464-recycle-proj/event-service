package com.greenloop.event_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

import com.greenloop.event_service.dtos.ApiResponse;
import com.greenloop.event_service.dtos.CreateEventRequest;
import com.greenloop.event_service.dtos.EventResponse;
import com.greenloop.event_service.dtos.UpdateEventRequest;
import com.greenloop.event_service.exceptions.RoleNotAllowedException;
import com.greenloop.event_service.services.EventService;

import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/events")
@AllArgsConstructor
public class EventController {

    private final EventService eventService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(@RequestBody CreateEventRequest request,
            @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        List<EventResponse> response = eventService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable UUID id) {
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success("Event retrieved successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(@PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest event,
            @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        EventResponse response = eventService.updateEvent(id, event);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(@PathVariable UUID id,
            @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        eventService.deleteEvent(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/upcoming/joined")
    public ResponseEntity<ApiResponse<List<EventResponse>>> upcomingEventsForUser(
            @RequestHeader("X-User-ID") String userId) {
        List<EventResponse> response = eventService.upcomingEventForUser(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", response));
    }

    @GetMapping("/past")
    public ResponseEntity<ApiResponse<List<EventResponse>>> pastEventsForUser(@PathVariable UUID eventId,
            @RequestHeader("X-User-ID") String userId) {
        List<EventResponse> response = eventService.pastEventsForUser(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", response));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        Map<String, String> response = Collections.singletonMap("status", "Event Service is Up and Running!");
        return ResponseEntity.ok(response);
    }

}
