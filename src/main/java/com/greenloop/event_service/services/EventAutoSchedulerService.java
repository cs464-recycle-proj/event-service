package com.greenloop.event_service.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.greenloop.event_service.enums.EventStatus;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.repos.*;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EventAutoSchedulerService {

    private final EventRepository eventRepo;
    private final int FIXED_RATE = 60000; // every min

    public EventAutoSchedulerService(EventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    @Scheduled(fixedRate = FIXED_RATE)
    public void autoStartEvents() {
        List<Event> toStartEvents = eventRepo.findByStatus(EventStatus.REGISTRATION);
        for (Event event : toStartEvents) {
            if (event.getStartDT().isAfter(LocalDateTime.now()) || event.getStartDT().isEqual(LocalDateTime.now()) ) {
                event.setStatus(EventStatus.ONGOING);
            }
        }

        eventRepo.saveAll(toStartEvents);
    }

    @Scheduled(fixedRate = FIXED_RATE)
    public void autoEndEvents() {
        List<Event> toEndEvents = eventRepo.findByStatus(EventStatus.ONGOING);
        for (Event event : toEndEvents) {
            if (event.getEndDT().isAfter(LocalDateTime.now()) || event.getEndDT().isEqual(LocalDateTime.now()) ) {
                event.setStatus(EventStatus.CLOSED);
            }
        }
        eventRepo.saveAll(toEndEvents);
    }

}
