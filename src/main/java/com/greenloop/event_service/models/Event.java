package com.greenloop.event_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

import com.greenloop.event_service.enums.EventType;

@Entity
@Getter
@Setter
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String eventName;
    private String eventDescription;
    private EventType eventType;

    private String location;
    
    private int capacity;
    private int points_reward;

    private LocalDateTime eventStartDT;
    private LocalDateTime eventEndDT;
    private LocalDateTime regStartDT;
    private LocalDateTime regEndDT;

    // QR token used to identify the event when scanned. Generated on create if missing.
    @Column(unique = true)
    private String qrToken;

    private LocalDateTime qrGeneratedAt;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "organizer_id", referencedColumnName = "id")
    private EventOrganzier organzier;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<EventAttendee> attendees = new ArrayList<>();

    @OneToMany(mappedBy = "events")
    private List<Tag> tags = new ArrayList<>();

    // Methods
    public void addAttendeeToEvent(EventAttendee attendee) {
        attendees.add(attendee);
        attendee.setEvent(this);
    }
    
}
