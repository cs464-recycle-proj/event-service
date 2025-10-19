package com.greenloop.event_service.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import com.greenloop.event_service.models.Tag;
import java.util.*;

public interface TagRepository extends JpaRepository<Tag, UUID>{
    
}
