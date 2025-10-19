package com.greenloop.event_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
@Table(name = "tag")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToMany
    @JoinTable(name = "event_tags", 
                joinColumns = @JoinColumn(name = "tag_id"), 
                inverseJoinColumns = @JoinColumn(name = "event_id"), 
                uniqueConstraints = @UniqueConstraint(columnNames = {"tag_id", "event_id" }))
    @JsonIgnore
    private List<Event> events = new ArrayList<>();

}
