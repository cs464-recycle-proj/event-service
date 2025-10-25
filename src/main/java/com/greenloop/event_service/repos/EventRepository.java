package com.greenloop.event_service.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.greenloop.event_service.models.Event;

import java.util.*;

import com.greenloop.event_service.enums.EventStatus;
import java.util.List;



public interface EventRepository extends JpaRepository<Event, UUID>{

    @Query("SELECT e FROM Event e JOIN EventAttendee a ON e.id = a.event.id WHERE a.userId = :userId")
    List<Event> findAllEventsByAttendeeId(@Param("userId") UUID userId);


    List<Event> findByStatus(EventStatus status);

    
}

