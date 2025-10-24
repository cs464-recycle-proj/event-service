package com.greenloop.event_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.greenloop.event_service.dtos.ScanRequest;
import com.greenloop.event_service.models.EventAttendee;
import com.greenloop.event_service.services.EventAttendeeService;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final EventAttendeeService attendeeService;

    public AttendanceController(EventAttendeeService attendeeService) {
        this.attendeeService = attendeeService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/scan")
    public EventAttendee scanAndMarkAttendance(@RequestBody ScanRequest req) {
        return attendeeService.markAttendanceByToken(req);
    }
}
