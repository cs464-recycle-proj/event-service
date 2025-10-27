package com.greenloop.event_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "event_attendees", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "event_id" })
})
@Builder
public class EventAttendee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    private UUID userId;

    private String username;

    @JsonProperty("email")
    private String userEmail;

    @Builder.Default
    private boolean attended = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    private java.time.LocalDateTime attendedAt;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;

    @PrePersist
    public void prePersist() {
        this.registeredAt = LocalDateTime.now();
    }
}
