package com.greenloop.event_service.dtos;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScanRequest {
    private String qrToken;
    private UUID userId;
    private String username;
    private String userEmail;
}
