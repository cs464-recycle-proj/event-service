package com.greenloop.event_service.services;

import org.springframework.stereotype.Service;

import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.repos.*;

import java.util.*;

@Service
public class EventService {

    private final EventRepository eventRepo;

    public EventService(EventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    // ----- event CRUD -----
    public Event createEvent(Event event) {
        return eventRepo.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepo.findAll();
    }

    public Event getEventById(UUID id) {
        return eventRepo.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public Event updateEvent(UUID id, Event event) {
        Event existingEvent = eventRepo.findById(id).orElseThrow(() -> new RuntimeException("Event not found"));
        existingEvent.setEventName(event.getEventName());
        existingEvent.setEventDescription(event.getEventDescription());
        existingEvent.setEventStartDT(event.getEventStartDT());
        existingEvent.setEventEndDT(event.getEventEndDT());
        existingEvent.setLocation(event.getLocation());
        existingEvent.setCapacity(event.getCapacity());
        existingEvent.setPoints_reward(event.getPoints_reward());
        existingEvent.setRegStartDT(event.getRegStartDT());
        existingEvent.setRegEndDT(event.getRegEndDT());
        existingEvent.setOrganzier(event.getOrganzier());
        // i remove attendees
        return eventRepo.save(existingEvent);
    }

    public void deleteEvent(UUID id) {
        eventRepo.deleteById(id);
    }

}
