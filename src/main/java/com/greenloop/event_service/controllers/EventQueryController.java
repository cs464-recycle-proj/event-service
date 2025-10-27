package com.greenloop.event_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import com.greenloop.event_service.dtos.ApiResponse;
import com.greenloop.event_service.dtos.EventResponse;
import com.greenloop.event_service.enums.EventType;
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
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        List<EventResponse> response = queryService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable UUID id) {
        EventResponse response = queryService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success("Event retrieved successfully", response));
    }

    @GetMapping("/upcoming/joined")
    public ResponseEntity<ApiResponse<List<EventResponse>>> upcomingEventsForUser(
            @RequestHeader("X-User-ID") String userId) {
        List<EventResponse> response = queryService.upcomingEventForUser(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", response));
    }

    @GetMapping("/past")
    public ResponseEntity<ApiResponse<List<EventResponse>>> pastEventsForUser(@PathVariable UUID eventId,
            @RequestHeader("X-User-ID") String userId) {
        List<EventResponse> response = queryService.pastEventsForUser(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", response));
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<EventType[]>> getAllEventTypes() {
        EventType[] response = queryService.getAllEventTypes();
        return ResponseEntity.ok(ApiResponse.success("Event types retrieved successfully", response));
    }

    // ---------------- ANALYTICS ---------------- //

    @GetMapping("/stats/open/total")
    public ResponseEntity<ApiResponse<Long>> getTotalOpenEvents() {
        long response = queryService.getTotalOpenEvents();
        return ResponseEntity.ok(ApiResponse.success("Total event count retrieved successfully", response));
    }

    @GetMapping("/stats/upcoming/30days")
    public ResponseEntity<ApiResponse<Long>> getUpcomingEventsNext30Days() {
        long response = queryService.getUpcomingEventsNext30Days();
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", response));
    }

    @GetMapping("/stats/open/participants")
    public ResponseEntity<ApiResponse<Long>> getTotalParticipantsInOpenEvents() {
        long response = queryService.getTotalParticipantsInOpenEvents();
        return ResponseEntity.ok(ApiResponse.success("Total participant count retrieved successfully", response));
    }

    
}