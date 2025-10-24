package com.greenloop.event_service.services;

import org.springframework.stereotype.Service;

import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.exceptions.TagNotFoundException;
import com.greenloop.event_service.models.Tag;
import com.greenloop.event_service.repos.EventRepository;
import com.greenloop.event_service.repos.TagRepository;

import java.util.*;

@Service
public class EventTagService {
    private final EventRepository eventRepo;
    private final TagRepository tagRepo;

    public EventTagService(EventRepository eventRepo, TagRepository tagRepo) {
        this.eventRepo = eventRepo;
        this.tagRepo = tagRepo;
    }

    // List all tags
    public List<Tag> getAllTags() {
        return tagRepo.findAll();
    }

    // List all tags by event
    public List<Tag> getAllTagsByEventId(UUID eventId) {
        return eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId))
                .getTags();
    }

    // Add Tag to event
    public Tag addTagToEvent(String tagName, UUID eventId) {
        Tag tag = tagRepo.findByTagName(tagName);

        if (tag == null) {
            tag = new Tag(tagName);
            tagRepo.save(tag);
        }

        tag = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId))
                .addTagToEvent(tag);

        return tagRepo.save(tag);
    }

    // Remove Tag from event
    public Tag removeTagFromEvent(String tagName, UUID eventId) {
        Tag tag = tagRepo.findByTagName(tagName);

        if (tag == null) {
            throw new TagNotFoundException(tagName);
        }

        tag = eventRepo.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId))
                .removeTagFromEvent(tag);

        return tagRepo.save(tag);
    }
    
}
