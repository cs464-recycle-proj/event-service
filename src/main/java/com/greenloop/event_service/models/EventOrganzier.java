package com.greenloop.event_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "event_organizers")
public class EventOrganzier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;
    private String username;
    private String userEmail;

    @OneToMany(mappedBy = "organzier")
    private List<Event> events = new ArrayList<>();
}
