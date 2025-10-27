package com.greenloop.event_service.exceptions;

public class QrCodeGenerationException extends RuntimeException {
    public QrCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}