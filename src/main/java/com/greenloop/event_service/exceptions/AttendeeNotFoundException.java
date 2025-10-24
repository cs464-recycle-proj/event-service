package com.greenloop.event_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.*;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AttendeeNotFoundException extends RuntimeException {

    public AttendeeNotFoundException(UUID attendeeId) {
        super("Could not find attendee " + attendeeId);
    }
    
}
