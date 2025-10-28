package com.greenloop.event_service.controllers;

import com.greenloop.event_service.dtos.ApiResponse;
import com.greenloop.event_service.dtos.EventAttendeeResponse;
import com.greenloop.event_service.dtos.ScanRequest;
import com.greenloop.event_service.services.EventAttendeeService;

import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/events")
@AllArgsConstructor
public class ScanController {

    private final EventAttendeeService attendeeService;

    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<EventAttendeeResponse>> scanAndMarkAttendance(
            @RequestBody ScanRequest req,
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-User-Email") String userEmail) {
        EventAttendeeResponse response = attendeeService.markAttendanceByToken(req, UUID.fromString(userId), userEmail);
        return ResponseEntity.ok(ApiResponse.success("Attendance marked successfully", response));
    }
}