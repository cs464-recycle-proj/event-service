package com.greenloop.event_service.exceptions;

public class EventFullException extends RuntimeException{

    public EventFullException(String message) {
        super(message);
    }
}
