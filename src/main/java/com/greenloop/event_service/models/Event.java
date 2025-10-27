package com.greenloop.event_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.dtos.UpdateEventRequest;
import com.greenloop.event_service.enums.EventStatus;

@Entity
@Data
@Table(name = "events")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private EventType type;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private String location;

    private String imageUrl;

    private String organizer;

    private int capacity;

    private int coins;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    @JsonIgnore
    @Column(unique = true)
    private String qrToken;

    private LocalDateTime qrGeneratedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonProperty("attendees")
    @Builder.Default
    private List<EventAttendee> attendees = new ArrayList<>();

    public EventAttendee addAttendeeToEvent(EventAttendee attendee) {
        attendees.add(attendee);
        attendee.setEvent(this);
        return attendee;
    }

    public EventAttendee removeAttendeeFromEvent(EventAttendee attendee) {
        attendees.remove(attendee);
        return attendee;
    }

    public int getAttendeeCount() {
        return attendees.size();
    }

    public Event updateFromRequest(UpdateEventRequest request) {
        Optional.ofNullable(request.getName()).ifPresent(this::setName);
        Optional.ofNullable(request.getDescription()).ifPresent(this::setDescription);
        Optional.ofNullable(request.getType())
                .map(v -> EventType.valueOf(v.toUpperCase()))
                .ifPresent(this::setType);
        Optional.ofNullable(request.getStatus())
                .map(v -> EventStatus.valueOf(v.toUpperCase()))
                .ifPresent(this::setStatus);
        Optional.ofNullable(request.getLocation()).ifPresent(this::setLocation);
        Optional.ofNullable(request.getImageUrl()).ifPresent(this::setImageUrl);
        Optional.ofNullable(request.getOrganizer()).ifPresent(this::setOrganizer);
        Optional.ofNullable(request.getCapacity()).ifPresent(this::setCapacity);
        Optional.ofNullable(request.getCoins()).ifPresent(this::setCoins);
        Optional.ofNullable(request.getStartDateTime()).ifPresent(this::setStartDateTime);
        Optional.ofNullable(request.getEndDateTime()).ifPresent(this::setEndDateTime);

        return this;
    }

}
