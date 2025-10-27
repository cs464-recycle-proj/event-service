package com.greenloop.event_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import com.greenloop.event_service.exceptions.RoleNotAllowedException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.services.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth() {
        Map<String, String> response = Collections.singletonMap("status", "Event Service is Up and Running!");
        return ResponseEntity.ok(response);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Event createEvent(@RequestBody Event event, @RequestHeader("X-User-Role") String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) throw new RoleNotAllowedException();
        return eventService.createEvent(event);
    }

    @PutMapping("/{id}")
    public Event updateEvent(@PathVariable UUID id, @Valid @RequestBody Event event, @RequestHeader("X-User-Role") String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) throw new RoleNotAllowedException();
        return eventService.updateEvent(id, event);
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable UUID id, @RequestHeader("X-User-Role") String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) throw new RoleNotAllowedException();
        eventService.deleteEvent(id);
    }

    @GetMapping(value = "/{id}/qr", produces = "image/png")
    public @ResponseBody byte[] getEventQr(@PathVariable UUID id, @RequestHeader("X-User-Role") String userRole) {
        if (!"ADMIN".equalsIgnoreCase(userRole)) throw new RoleNotAllowedException();
        return eventService.getQrCodeImage(id, 300, 300);
    }

}