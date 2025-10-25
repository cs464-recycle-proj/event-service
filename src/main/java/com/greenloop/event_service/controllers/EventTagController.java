package com.greenloop.event_service.controllers;


import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.greenloop.event_service.models.Tag;
import com.greenloop.event_service.services.EventTagService;

import java.util.*;


@RestController
@RequestMapping("/api/events")
public class EventTagController {

    private final EventTagService tagService;
    public EventTagController(EventTagService tagService) {
        this.tagService = tagService;
    }
    
    @GetMapping("/tags")
    public List<Tag> getAllTags() {
        return tagService.getAllTags();
    }
    
    @GetMapping("/{eventId}/tags")
    public List<Tag> getTagByEventId(@PathVariable UUID eventId) {
        return tagService.getAllTagsByEventId(eventId);
    }

    @PutMapping("/{eventId}/tags")
    public Tag addTagToEvent(@PathVariable UUID eventId, @RequestBody String tagName) {
        return tagService.addTagToEvent(tagName, eventId);
    }

    @DeleteMapping("/{eventId}/tags")
    public Tag removeTagFromEvent(@PathVariable UUID eventId, @RequestBody String tagName) {
        return tagService.removeTagFromEvent(tagName, eventId);
    }
    
}
