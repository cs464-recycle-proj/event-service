package com.greenloop.event_service.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {
    private UUID id;
    private int capacity;
    private int coins;
    private String description;
    private String imageUrl;
    private String location;
    private String name;
    private String organizer;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String type;
    private String status;
    private LocalDateTime qrGeneratedAt;
    private String qrToken;
    private int attendeeCount;
}
