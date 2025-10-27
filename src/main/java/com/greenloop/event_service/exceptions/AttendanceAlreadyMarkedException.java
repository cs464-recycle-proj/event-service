package com.greenloop.event_service.exceptions;

public class AttendanceAlreadyMarkedException extends RuntimeException {
    public AttendanceAlreadyMarkedException(String message) {
        super(message);
    }
}