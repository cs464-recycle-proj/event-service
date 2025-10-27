package com.greenloop.event_service.exceptions;

public class AttendeeNotRegisteredException extends RuntimeException {
    public AttendeeNotRegisteredException(String message) {
        super(message);
    }
}
