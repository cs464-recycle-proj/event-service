package com.greenloop.event_service.services;

// import java.time.LocalDateTime;
// import java.util.*;


// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.transaction.annotation.Transactional;
// import com.greenloop.event_service.enums.EventStatus;
// import com.greenloop.event_service.models.Event;
// import com.greenloop.event_service.repos.*;
import org.springframework.stereotype.Service;



@Service
public class EventAutoSchedulerService {

    // private final EventRepository eventRepo;
    // private final int FIXED_RATE = 60000; // every min

    // public EventAutoSchedulerService(EventRepository eventRepo) {
    //     this.eventRepo = eventRepo;
    // }

    // @Scheduled(fixedRate = FIXED_RATE)
    // @Transactional
    // public void autoStartEvents() {
    //     List<Event> toStartEvents = eventRepo.findByStatus(EventStatus.REGISTRATION);
    //     for (Event event : toStartEvents) {
    //         if (event.getStartDT().isAfter(LocalDateTime.now()) || event.getStartDT().isEqual(LocalDateTime.now()) ) {
    //             event.setStatus(EventStatus.ONGOING);
    //         }
    //     }

    //     eventRepo.saveAll(toStartEvents);
    // }

    // @Scheduled(fixedRate = FIXED_RATE)
    // @Transactional
    // public void autoEndEvents() {
    //     List<Event> toEndEvents = eventRepo.findByStatus(EventStatus.ONGOING);
    //     for (Event event : toEndEvents) {
    //         if (event.getEndDT().isAfter(LocalDateTime.now()) || event.getEndDT().isEqual(LocalDateTime.now()) ) {
    //             event.setStatus(EventStatus.CLOSED);
    //         }
    //     }
    //     eventRepo.saveAll(toEndEvents);
    // }

}
