package com.greenloop.event_service.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.greenloop.event_service.models.Event;

import java.time.LocalDateTime;
import java.util.*;

import com.greenloop.event_service.enums.EventStatus;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("SELECT e FROM Event e JOIN e.attendees a WHERE a.userId = :userId")
    List<Event> findAllEventsByAttendeeId(@Param("userId") UUID userId);

    List<Event> findByStatus(EventStatus status);

    Optional<Event> findByQrToken(String qrToken);

    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.status = :newStatus WHERE (e.status = :regStatus OR e.status = :fullStatus) AND e.startDateTime <= :currentTime")
    int updateStatusToOngoing(
            EventStatus regStatus, 
            EventStatus fullStatus, 
            EventStatus newStatus,
            LocalDateTime currentTime);

    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.status = :newStatus WHERE e.status = :oldStatus AND e.endDateTime <= :currentTime")
    int updateStatusToClosed(EventStatus oldStatus, EventStatus newStatus, LocalDateTime currentTime);
}
