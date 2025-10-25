package com.greenloop.event_service.services;

import org.springframework.stereotype.Service;

import com.greenloop.event_service.exceptions.AttendeeNotFoundException;
import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.models.Tag;
import com.greenloop.event_service.repos.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

@Service
public class EventService {

    private final EventRepository eventRepo;
    private final EventAttendeeRepository attendeeRepo;
    private final TagRepository tagRepo;

    public EventService(EventRepository eventRepo, EventAttendeeRepository attendeeRepo, TagRepository tagRepo) {
        this.eventRepo = eventRepo;
        this.attendeeRepo = attendeeRepo;
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
        // Ensure qrToken exists so admins and users can access the QR immediately after creation
        if (event.getQrToken() == null || event.getQrToken().isEmpty()) {
            event.setQrToken(UUID.randomUUID().toString());
            event.setQrGeneratedAt(LocalDateTime.now());
        }
        return eventRepo.save(event);
    }

    /**
     * Generate a PNG byte[] for the event's QR token. Encodes the token string.
     */
    public byte[] getQrCodeImage(UUID eventId, int width, int height) {
        Event event = getEventById(eventId);
        String token = event.getQrToken();
        if (token == null) throw new RuntimeException("QR token not found for event");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            QRCodeWriter qrWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrWriter.encode(token, BarcodeFormat.QR_CODE, width, height);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | java.io.IOException e) {
            throw new RuntimeException("Failed to create QR image", e);
        }
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
        existingEvent.setType(event.getType());
        existingEvent.setStatus(event.getStatus());

        existingEvent.setLocation(event.getLocation());
        existingEvent.setImageUrl(event.getImageUrl());
        existingEvent.setOrganizer(event.getOrganizer());
        
        existingEvent.setCapacity(event.getCapacity());
        existingEvent.setCoins(event.getCoins());

        existingEvent.setStartDT(event.getStartDT());
        existingEvent.setEndDT(event.getEndDT());
        
        return eventRepo.save(existingEvent);
    }

    public void deleteEvent(UUID id) {
        eventRepo.deleteById(id);
    }

     // get upcoming events for user
    public List<Event> upcomingEventForUser(UUID userId) {

        attendeeRepo.findByUserId(userId)
                .orElseThrow(() -> new AttendeeNotFoundException(userId));

        List<Event> events = eventRepo.findAllEventsByAttendeeId(userId);
        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .filter(e -> e.getStartDT().isAfter(now)) // upcoming only
                .collect(Collectors.toList());
    }

    // get past events for user
    public List<Event> pastEventsForUser(UUID userId) {

        attendeeRepo.findByUserId(userId)
                .orElseThrow(() -> new AttendeeNotFoundException(userId));

        List<Event> events = eventRepo.findAllEventsByAttendeeId(userId);
        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .filter(e -> e.getEndDT().isBefore(now)) // past only
                .collect(Collectors.toList());
    }

    // get upcoming events that user can join
    public List<Event> upcomingNotJoinedEvents(UUID userId) {
        LocalDateTime now = LocalDateTime.now();

        // get all upcoming events (not closed)
        List<Event> upcomingEvents = eventRepo.findAll().stream()
                .filter(e -> e.getEndDT().isAfter(now)) // only events not ended
                .filter(e -> e.getAttendeeCount() < e.getCapacity()) // only events with available slots
                .filter(e -> e.getAttendees().stream()
                        .noneMatch(a -> a.getUserId().equals(userId))) // exclude events the user has joined
                .collect(Collectors.toList());

        return upcomingEvents;
    }


}
