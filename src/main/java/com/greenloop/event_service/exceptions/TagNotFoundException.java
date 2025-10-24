package com.greenloop.event_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.*;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TagNotFoundException extends RuntimeException {

    public TagNotFoundException(UUID tagId) {
        super("Could not find tag id " + tagId);
    }

    public TagNotFoundException(String tagName) {
        super("Could not find tag name " + tagName);
    }

}
