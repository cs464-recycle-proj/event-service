package com.ticketsystem.event.service.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String type;
    private String email;
    
    @JsonProperty("event_name")
    private String eventName;
    
    private String details;
    
    @JsonProperty("coins_earned")
    private Integer coinsEarned;
    
    private String location;
    
    @JsonProperty("start_date")
    private String startDate;
    
    @JsonProperty("end_date")
    private String endDate;
    
    private String organizer;
    private Integer coins;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
