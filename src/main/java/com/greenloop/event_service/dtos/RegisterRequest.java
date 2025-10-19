package com.greenloop.event_service.dtos;

import java.util.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private UUID userId;
    private String username;
    private String userEmail;
}
