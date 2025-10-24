package com.greenloop.event_service.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.services.EventService;


@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // ----- event CRUD -----
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/")
    public Event createEvent(@RequestBody Event event) {
        return eventService.createEvent(event);
    }

    @GetMapping("/")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public Event getEvent(@PathVariable UUID id) {
        return eventService.getEventById(id);
    }

    @GetMapping(value = "/{id}/qr", produces = "image/png")
    public @ResponseBody byte[] getEventQr(@PathVariable UUID id) {
        // default size 300x300
        return eventService.getQrCodeImage(id, 300, 300);
    }

    @PutMapping("/{id}")
    public Event updateEvent(@PathVariable UUID id, @RequestBody Event event) {
        return eventService.updateEvent(id, event);
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable UUID id) {
        eventService.deleteEvent(id);
    }

}
