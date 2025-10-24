package com.greenloop.event_service.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.greenloop.event_service.models.EventAttendee;
import java.util.*;

public interface EventAttendeeRepository extends JpaRepository<EventAttendee, UUID>{
    
    Optional<EventAttendee> findByUserIdAndEventId(UUID userId, UUID eventId);
    Optional<EventAttendee> findByUserId(UUID userId);
}
