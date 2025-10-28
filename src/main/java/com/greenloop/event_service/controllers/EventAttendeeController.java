package com.greenloop.event_service.controllers;

import com.greenloop.event_service.dtos.ApiResponse;
import com.greenloop.event_service.dtos.EventAttendeeResponse;
import com.greenloop.event_service.dtos.ScanRequest;
import com.greenloop.event_service.exceptions.RoleNotAllowedException;
import com.greenloop.event_service.services.EventAttendeeService;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/events/{eventId}")
@AllArgsConstructor
public class EventAttendeeController {

    private final EventAttendeeService attendeeService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<EventAttendeeResponse>> registerAttendee(@PathVariable UUID eventId,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-User-Email") String userEmail) {
        EventAttendeeResponse response = attendeeService.registerAttendee(eventId, UUID.fromString(userId), userEmail);
        return ResponseEntity.ok(ApiResponse.success("Attendee registered successfully", response));

    }

    @GetMapping("/participants")
    public ResponseEntity<ApiResponse<List<EventAttendeeResponse>>> getAllEventAttendees(@PathVariable UUID eventId,
            @RequestHeader("X-User-Role") String userRole) {
        if (!userRole.equals("ADMIN")) {
            throw new RoleNotAllowedException();
        }
        List<EventAttendeeResponse> response = attendeeService.getAllEventAttendees(eventId);
        return ResponseEntity.ok(ApiResponse.success("Participants retrieved successfully", response));

    }

    @GetMapping("/is-registered")
    public boolean isRegistered(@PathVariable UUID eventId, @RequestHeader("X-User-ID") String userId) {
        return attendeeService.isUserRegistered(eventId, UUID.fromString(userId));
    }

    @DeleteMapping("/participants/{userId}")
    public ResponseEntity<Void> deregisterAttendee(@PathVariable UUID eventId, @PathVariable UUID userId) {
        attendeeService.deregisterAttendee(eventId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<EventAttendeeResponse>> scanAndMarkAttendance(@RequestBody ScanRequest req,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-User-Email") String userEmail) {
        EventAttendeeResponse response = attendeeService.markAttendanceByToken(req, UUID.fromString(userId), userEmail);
        return ResponseEntity.ok(ApiResponse.success("Attendance marked successfully", response));

    }
}
