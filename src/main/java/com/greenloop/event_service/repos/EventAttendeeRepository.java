package com.greenloop.event_service.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greenloop.event_service.models.EventAttendee;
import java.util.*;

@Repository
public interface EventAttendeeRepository extends JpaRepository<EventAttendee, UUID> {

    Optional<EventAttendee> findByUserIdAndEventId(UUID userId, UUID eventId);

    Optional<EventAttendee> findByUserId(UUID userId);

    boolean existsByUserIdAndEventId(UUID userId, UUID eventId);
}
