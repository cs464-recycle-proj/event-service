package com.greenloop.event_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.enums.EventStatus;

@Entity
@NoArgsConstructor
@Data
@Table(name = "events")
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
    private int views_count;

    private LocalDateTime startDT;
    private LocalDateTime endDT;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<EventAttendee> attendees = new ArrayList<>();

    @OneToMany(mappedBy = "events")
    @JsonProperty("attendees")
    private List<Tag> tags = new ArrayList<>();

    /* ======== CONSTRUCTORS ======== */
    public Event(String name, String description, EventType type, LocalDateTime startDT, LocalDateTime endDT,
            String location, int capacity, int coins, String imageUrl, String organizer) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.location = location;
        this.imageUrl = imageUrl;
        this.organizer = organizer;
        this.capacity = capacity;
        this.coins = coins;
        this.startDT = startDT;
        this.endDT = endDT;
        this.status = EventStatus.REGISTRATION;
    }

    /* ======== METHODS ======== */
    @JsonProperty("tags")
    public List<String> getTagNames() {
        List<String> tagNames = new ArrayList<>();
        for (Tag tag : tags) {
            tagNames.add(tag.getTagName());
        }
        return tagNames;
    }

    public EventAttendee addAttendeeToEvent(EventAttendee attendee) {
        attendees.add(attendee);
        attendee.setEvent(this);
        return attendee;
    }

    public EventAttendee removeAttendeeFromEvent(EventAttendee attendee) {
        attendees.remove(attendee);
        return attendee;
    }

    public Tag addTagToEvent(Tag tag) {
        tags.add(tag);
        tag.getEvents().add(this);
        return tag;
    }

    public Tag removeTagFromEvent(Tag tag) {
        tags.remove(tag);
        tag.getEvents().remove(this);
        return tag;
    }

    public int getAttendeeCount() {
        return attendees.size();
    }

}
