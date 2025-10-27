package com.greenloop.event_service.exceptions;

public class InvalidEventStateException extends RuntimeException {
    public InvalidEventStateException(String message){
        super(message);
    }
}
