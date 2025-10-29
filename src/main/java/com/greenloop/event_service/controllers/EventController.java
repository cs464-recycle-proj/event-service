package com.greenloop.event_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import com.greenloop.event_service.dtos.ApiResponse;
import com.greenloop.event_service.dtos.CreateEventRequest;
import com.greenloop.event_service.dtos.EventAttendeeResponse;
import com.greenloop.event_service.dtos.EventResponse;
import com.greenloop.event_service.dtos.ScanRequest;
import com.greenloop.event_service.dtos.UpdateEventRequest;
import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.exceptions.RoleNotAllowedException;
import com.greenloop.event_service.services.EventService;
import com.greenloop.event_service.services.EventAttendeeService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

/**
 * REST controller for event management and querying.
 * <p>
 * Handles both CRUD operations (admin-only) and query operations (public/user).
 * Merges command and query responsibilities for a simpler, unified API.
 * </p>
 *
 * <p>
 * <strong>Base Path:</strong> /api/events
 * </p>
 *
 * @see EventService
 */
@RestController
@RequestMapping("/api/events")
@AllArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventAttendeeService attendeeService;

    // ==================== QUERY ENDPOINTS (PUBLIC/USER) ==================== //

    /**
     * Retrieves all events.
     *
     * @return list of all events wrapped in ApiResponse
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        List<EventResponse> response = eventService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", response));
    }

    /**
     * Retrieves a single event by ID.
     *
     * @param id event UUID
     * @return event details wrapped in ApiResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable UUID id) {
        EventResponse response = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success("Event retrieved successfully", response));
    }

    /**
     * Retrieves upcoming events that the authenticated user has joined.
     *
     * @param userId authenticated user ID from gateway (X-User-ID header)
     * @return list of upcoming events user is registered for
     */
    @GetMapping("/upcoming/joined")
    public ResponseEntity<ApiResponse<List<EventResponse>>> upcomingEventsForUser(
            @RequestHeader("X-User-ID") String userId) {
        List<EventResponse> response = eventService.upcomingEventForUser(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", response));
    }

    /**
     * Retrieves past events that the authenticated user attended.
     *
     * @param userId authenticated user ID from gateway (X-User-ID header)
     * @return list of past events user attended
     */
    @GetMapping("/past")
    public ResponseEntity<ApiResponse<List<EventResponse>>> pastEventsForUser(
            @RequestHeader("X-User-ID") String userId) {
        List<EventResponse> response = eventService.pastEventsForUser(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", response));
    }

    /**
     * Retrieves all available event types (enum values).
     *
     * @return array of EventType enum values
     */
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<EventType[]>> getAllEventTypes() {
        EventType[] response = eventService.getAllEventTypes();
        return ResponseEntity.ok(ApiResponse.success("Event types retrieved successfully", response));
    }

    // ==================== ANALYTICS ENDPOINTS ==================== //

    /**
     * Gets the total count of open events.
     *
     * @return total count of open events
     */
    @GetMapping("/stats/open/total")
    public ResponseEntity<ApiResponse<Long>> getTotalOpenEvents() {
        long response = eventService.getTotalOpenEvents();
        return ResponseEntity.ok(ApiResponse.success("Total event count retrieved successfully", response));
    }

    /**
     * Gets the count of upcoming events in the next 30 days.
     *
     * @return count of events starting within 30 days
     */
    @GetMapping("/stats/upcoming/30days")
    public ResponseEntity<ApiResponse<Long>> getUpcomingEventsNext30Days() {
        long response = eventService.getUpcomingEventsNext30Days();
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", response));
    }

    /**
     * Gets the total participant count across all open events.
     *
     * @return sum of participants in open events
     */
    @GetMapping("/stats/open/participants")
    public ResponseEntity<ApiResponse<Long>> getTotalParticipantsInOpenEvents() {
        long response = eventService.getTotalParticipantsInOpenEvents();
        return ResponseEntity.ok(ApiResponse.success("Total participant count retrieved successfully", response));
    }

    // ==================== COMMAND ENDPOINTS (ADMIN-ONLY) ==================== //

    /**
     * Creates a new event (admin-only).
     *
     * @param request  event details
     * @param userRole authenticated user role from gateway (X-User-Role header)
     * @return newly created event wrapped in ApiResponse
     * @throws RoleNotAllowedException if user is not an admin
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        EventResponse response = eventService.createEvent(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Event created successfully", response));
    }

    /**
     * Updates an existing event (admin-only).
     *
     * @param id       event UUID
     * @param request  updated event details
     * @param userRole authenticated user role from gateway (X-User-Role header)
     * @return updated event wrapped in ApiResponse
     * @throws RoleNotAllowedException if user is not an admin
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request,
            @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        EventResponse response = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", response));
    }

    /**
     * Deletes an event permanently (admin-only).
     *
     * @param id       event UUID
     * @param userRole authenticated user role from gateway (X-User-Role header)
     * @return 204 No Content on successful deletion
     * @throws RoleNotAllowedException if user is not an admin
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable UUID id,
            @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== ATTENDANCE (SCAN) ENDPOINT ==================== //

    /**
     * Marks attendance for the authenticated user by scanning a QR token.
     * <p>
     * The QR token uniquely identifies the event; therefore, no eventId is required
     * in the path.
     * </p>
     *
     * @param req       the scan request containing the QR token
     * @param userId    authenticated user ID from gateway (X-User-ID header)
     * @param userEmail authenticated user email from gateway (X-User-Email header)
     * @return attendance status wrapped in ApiResponse
     */
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<EventAttendeeResponse>> scanAndMarkAttendance(
            @RequestBody ScanRequest req,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-User-Email") String userEmail) {
        EventAttendeeResponse response = attendeeService.markAttendanceByToken(
                req, UUID.fromString(userId), userEmail);
        return ResponseEntity.ok(ApiResponse.success("Attendance marked successfully", response));
    }
}
