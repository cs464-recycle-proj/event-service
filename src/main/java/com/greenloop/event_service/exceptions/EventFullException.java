package com.greenloop.event_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.*;

@ResponseStatus(HttpStatus.CONFLICT)
public class EventFullException extends RuntimeException{

    public EventFullException(UUID eventId) {
        super("Event "+ eventId.toString() +" is full. Cannot Register.");
    }
}
