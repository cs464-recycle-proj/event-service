package com.greenloop.event_service.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScanRequest {
    private String qrToken;
}
