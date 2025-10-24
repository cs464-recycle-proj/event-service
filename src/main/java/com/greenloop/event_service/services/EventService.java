package com.greenloop.event_service.services;

import org.springframework.stereotype.Service;

import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.models.Tag;
import com.greenloop.event_service.repos.*;

import java.util.*;

@Service
public class EventService {

    private final EventRepository eventRepo;
    private final TagRepository tagRepo;

    public EventService(EventRepository eventRepo, TagRepository tagRepo) {
        this.eventRepo = eventRepo;
        this.tagRepo = tagRepo;
    }

    // ----- event CRUD -----
    public Event createEvent(Event event) {

        for (Tag tag : event.getTags()) {
            Tag existingTag = tagRepo.findByTagName(tag.getTagName());
            if (existingTag == null) {
                // Save the new tag if not found
                existingTag = tagRepo.save(tag);
            }
            event.addTagToEvent(existingTag);
        }

        // Save event
        return eventRepo.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepo.findAll();
    }

    public Event getEventById(UUID id) {
        return eventRepo.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    public Event updateEvent(UUID id, Event event) {
        Event existingEvent = eventRepo.findById(id).orElseThrow(() -> new EventNotFoundException(id));
        existingEvent.setName(event.getName());
        existingEvent.setDescription(event.getDescription());
        existingEvent.setStartDT(event.getStartDT());
        existingEvent.setEndDT(event.getEndDT());
        existingEvent.setLocation(event.getLocation());
        existingEvent.setCapacity(event.getCapacity());
        existingEvent.setCoins(event.getCoins());
        existingEvent.setRegStartDT(event.getRegStartDT());
        existingEvent.setRegEndDT(event.getRegEndDT());
        return eventRepo.save(existingEvent);
    }

    public void deleteEvent(UUID id) {
        eventRepo.deleteById(id);
    }

}
