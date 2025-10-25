package com.greenloop.event_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.greenloop.event_service.dtos.RegisterRequestDTO;

@Entity
@NoArgsConstructor
@Data
@Table(name = "event_attendees")
public class EventAttendee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @JsonIgnore
    private UUID userId;
    private String name;
    @JsonProperty("email")
    private String userEmail;
    private boolean attended = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;
    private LocalDateTime attendedAt;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;

    /* ======== CONSTRUCTORS ======== */
    public EventAttendee(RegisterRequestDTO request) {
        this.userId = request.getUserId();
        this.userEmail = request.getUserEmail();
    }

    public EventAttendee(UUID userId, String name, String userEmail, Event event) {
        this.userId = userId;
        this.name = name;
        this.userEmail = userEmail;
        this.event = event;
    }

}
