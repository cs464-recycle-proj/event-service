package com.greenloop.event_service.services;

import org.springframework.stereotype.Service;

import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.repos.*;

import java.util.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

@Service
public class EventService {

    private final EventRepository eventRepo;

    public EventService(EventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    // ----- event CRUD -----
    public Event createEvent(Event event) {
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
