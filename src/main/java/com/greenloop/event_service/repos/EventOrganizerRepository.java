package com.greenloop.event_service.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.greenloop.event_service.models.EventOrganzier;
import java.util.*;

public interface EventOrganizerRepository extends JpaRepository<EventOrganzier, UUID>{
    
}

