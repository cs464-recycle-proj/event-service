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
public class EventAttendeeResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private boolean attended;
    private LocalDateTime registeredAt;
    private LocalDateTime attendedAt;
}
