package com.greenloop.event_service.services;

import org.springframework.stereotype.Service;

import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.models.Tag;
import com.greenloop.event_service.repos.*;

import java.time.LocalDateTime;
import java.util.*;
import java.io.ByteArrayOutputStream;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

@Service
public class EventService {

    private final EventRepository eventRepo;
    private final TagRepository tagRepo;

    public EventService(EventRepository eventRepo, TagRepository tagRepo) {
        this.eventRepo = eventRepo;
        this.tagRepo = tagRepo;
    }

    // create event
    public Event createEvent(Event event) {

        for (Tag tag : event.getTags()) {
            Tag existingTag = tagRepo.findByTagName(tag.getTagName());
            // Save the tag if not found
            if (existingTag == null) {
                existingTag = tagRepo.save(tag);
            }
            event.addTagToEvent(existingTag);
        }

        // Save event
        // Ensure qrToken exists so admins and users can access the QR immediately after
        // creation
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
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        String token = event.getQrToken();
        if (token == null)
            throw new RuntimeException("QR token not found for event");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            QRCodeWriter qrWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrWriter.encode(token, BarcodeFormat.QR_CODE, width, height);
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | java.io.IOException e) {
            throw new RuntimeException("Failed to create QR image", e);
        }
    }

    

    // update event
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

    

}
