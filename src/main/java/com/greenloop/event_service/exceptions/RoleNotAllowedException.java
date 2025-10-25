package com.greenloop.event_service.exceptions;

public class RoleNotAllowedException extends RuntimeException {

    public RoleNotAllowedException() {
        super("Your role does not have permission to perform this action.");
    }

}