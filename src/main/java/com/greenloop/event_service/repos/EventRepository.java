package com.greenloop.event_service.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.greenloop.event_service.models.Event;
import java.util.*;

public interface EventRepository extends JpaRepository<Event, UUID>{
	Optional<Event> findByQrToken(String qrToken);
}

