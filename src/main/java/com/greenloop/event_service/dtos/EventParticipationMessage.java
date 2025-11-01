package com.greenloop.event_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventParticipationMessage {
    private Integer eventId;
    private Integer userId;
    private String eventType;
    private String participationType;
    private Integer coinsEarned;
    private LocalDateTime timestamp;
}
