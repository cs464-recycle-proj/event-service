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
    private EventType type;
    private EventStatus status;

    private String location;
    private String organizer;
    
    private int capacity;
    private int points_reward;
    private int views_count;

    private LocalDateTime startDT;
    private LocalDateTime endDT;
    private LocalDateTime regStartDT;
    private LocalDateTime regEndDT;
    
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<EventAttendee> attendees = new ArrayList<>();

    @OneToMany(mappedBy = "events")
    @JsonIgnore
    private List<Tag> tags = new ArrayList<>();

    
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
