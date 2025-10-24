package com.greenloop.event_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.greenloop.event_service.dtos.RegisterRequest;

@Entity
@NoArgsConstructor
@Data
@Table(name = "event_attendees")
public class EventAttendee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;
    private String username;
    private String userEmail;
    private boolean attended; 

    @ManyToOne
    @JoinColumn(name="event_id", nullable=false)
    @JsonIgnore
    private Event event;


    /* ======== CONSTRUCTORS ======== */
    public EventAttendee(RegisterRequest request) {
        this.userId = request.getUserId();
        this.username = request.getUsername();
        this.userEmail = request.getUserEmail();
    }

    public EventAttendee(UUID userId, String username, String userEmail, Event event) {
        this.userId = userId;
        this.username = username;
        this.userEmail = userEmail;
        this.event = event;
    }
    
}
