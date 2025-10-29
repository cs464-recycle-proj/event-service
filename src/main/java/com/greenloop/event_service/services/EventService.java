package com.greenloop.event_service.services;

import org.springframework.stereotype.Service;

import com.greenloop.event_service.dtos.CreateEventRequest;
import com.greenloop.event_service.dtos.EventResponse;
import com.greenloop.event_service.dtos.UpdateEventRequest;
import com.greenloop.event_service.enums.EventStatus;
import com.greenloop.event_service.enums.EventType;
import com.greenloop.event_service.exceptions.EventNotFoundException;
import com.greenloop.event_service.models.Event;
import com.greenloop.event_service.repos.*;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service layer handling all event-related business logic.
 * <p>
 * This service provides comprehensive event management capabilities including:
 * <ul>
 * <li>CRUD operations (create, read, update, delete events)</li>
 * <li>Event queries (get all events, get by ID, filter by status)</li>
 * <li>User-event relationships (upcoming/past events for users)</li>
 * <li>Analytics (event statistics and participation metrics)</li>
 * </ul>
 * 
 * All event responses include attendee counts and QR token information for
 * event check-in.
 * </p>
 * 
 * @author GreenLoop Team
 * @version 1.0
 */
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    // ---------- COMMAND OPERATIONS ----------

    /**
     * Creates a new event with the provided details.
     * <p>
     * Automatically generates a unique QR token and sets initial status to
     * REGISTRATION.
     * </p>
     *
     * @param request the event creation request containing all event details
     * @return EventResponse containing the created event details with generated ID
     *         and QR token
     */
    public EventResponse createEvent(CreateEventRequest request) {
        Event event = Event.builder()
                .capacity(request.getCapacity())
                .name(request.getName())
                .description(request.getDescription())
                .coins(request.getCoins())
                .organizer(request.getOrganizer())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .imageUrl(request.getImageUrl())
                .location(request.getLocation())
                .type(EventType.valueOf(request.getType().toUpperCase()))
                .status(EventStatus.REGISTRATION)
                .qrToken(UUID.randomUUID().toString())
                .qrGeneratedAt(LocalDateTime.now())
                .build();

        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    /**
     * Updates an existing event with new details.
     *
     * @param id      the UUID of the event to update
     * @param request the update request containing modified event details
     * @return EventResponse containing the updated event details
     * @throws EventNotFoundException if no event exists with the given ID
     */
    public EventResponse updateEvent(UUID id, UpdateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event with id " + id + " is not found"));
        event.updateFromRequest(request);
        return mapToResponse(eventRepository.save(event));
    }

    /**
     * Deletes an event by ID.
     *
     * @param id the UUID of the event to delete
     * @throws EventNotFoundException if no event exists with the given ID
     */
    public void deleteEvent(UUID id) {
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException("Event with id " + id + " is not found");
        }
        eventRepository.deleteById(id);
    }

    // ---------- QUERY OPERATIONS ----------

    /**
     * Retrieves all events in the system.
     *
     * @return list of all events as EventResponse objects
     */
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves a specific event by its ID.
     *
     * @param id the UUID of the event to retrieve
     * @return EventResponse containing the event details
     * @throws EventNotFoundException if no event exists with the given ID
     */
    public EventResponse getEventById(UUID id) {
        return eventRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EventNotFoundException("Event with id " + id + " is not found"));
    }

    /**
     * Retrieves all upcoming events that a user has joined.
     * <p>
     * Only returns events where the start date is after the current time.
     * </p>
     *
     * @param userId the UUID of the user
     * @return list of upcoming events the user has joined
     */
    public List<EventResponse> upcomingEventForUser(UUID userId) {
        List<Event> events = eventRepository.findAllEventsByAttendeeId(userId);
        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .filter(e -> e.getStartDateTime().isAfter(now))
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves all past events that a user has attended.
     * <p>
     * Only returns events where the end date is before the current time.
     * </p>
     *
     * @param userId the UUID of the user
     * @return list of past events the user has attended
     */
    public List<EventResponse> pastEventsForUser(UUID userId) {
        List<Event> events = eventRepository.findAllEventsByAttendeeId(userId);
        LocalDateTime now = LocalDateTime.now();

        return events.stream()
                .filter(e -> e.getEndDateTime().isBefore(now))
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves all upcoming events that a user has NOT joined yet and can still
     * join.
     * <p>
     * Filters events based on:
     * <ul>
     * <li>End date is after current time (event not ended)</li>
     * <li>Event has available capacity</li>
     * <li>User has not already joined the event</li>
     * </ul>
     * </p>
     *
     * @param userId the UUID of the user
     * @return list of joinable upcoming events
     */
    public List<EventResponse> upcomingNotJoinedEventsForUser(UUID userId) {
        LocalDateTime now = LocalDateTime.now();

        return eventRepository.findAll().stream()
                .filter(e -> e.getEndDateTime().isAfter(now)) // only events not ended
                .filter(e -> e.getAttendeeCount() < e.getCapacity()) // only events with available slots
                .filter(e -> e.getAttendees().stream()
                        .noneMatch(a -> a.getUserId().equals(userId))) // exclude events the user has joined
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves all available event types.
     *
     * @return array of all EventType enum values
     */
    public EventType[] getAllEventTypes() {
        return EventType.values();
    }

    // ---------- ANALYTICS ----------

    /**
     * Counts the total number of open (non-closed) events.
     *
     * @return count of events that are not in CLOSED status
     */
    public long getTotalOpenEvents() {
        return eventRepository.findAll().stream()
                .filter(e -> EventStatus.CLOSED != e.getStatus())
                .count();
    }

    /**
     * Counts upcoming events starting within the next 30 days.
     *
     * @return count of events starting between now and 30 days from now
     */
    public long getUpcomingEventsNext30Days() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next30Days = now.plusDays(30);

        return eventRepository.findAll().stream()
                .filter(e -> e.getStartDateTime() != null && e.getStartDateTime().isAfter(now)
                        && e.getStartDateTime().isBefore(next30Days))
                .count();
    }

    /**
     * Calculates the total number of participants across all open events.
     *
     * @return sum of attendee counts for all non-closed events
     */
    public long getTotalParticipantsInOpenEvents() {
        return eventRepository.findAll().stream()
                .filter(e -> EventStatus.CLOSED != e.getStatus())
                .mapToLong(e -> e.getAttendees() != null ? e.getAttendees().size() : 0)
                .sum();
    }

    // ---------- HELPER METHODS ----------

    /**
     * Maps an Event entity to an EventResponse DTO.
     *
     * @param event the Event entity to map
     * @return EventResponse DTO containing event details
     */
    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .capacity(event.getCapacity())
                .coins(event.getCoins())
                .description(event.getDescription())
                .imageUrl(event.getImageUrl())
                .location(event.getLocation())
                .name(event.getName())
                .organizer(event.getOrganizer())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .type(event.getType().name())
                .status(event.getStatus().name())
                .qrToken(event.getQrToken())
                .qrGeneratedAt(event.getQrGeneratedAt())
                .attendeeCount(event.getAttendeeCount())
                .build();
    }

    // @Scheduled(fixedRate = 300000)
    // public void updateEventStatuses() {
    // LocalDateTime now = LocalDateTime.now();

    // // Transition events from REGISTRATION OR FULL to ONGOING
    // eventRepository.updateStatusToOngoing(
    // EventStatus.REGISTRATION,
    // EventStatus.FULL,
    // EventStatus.ONGOING,
    // now);

    // // Transition events from ONGOING to CLOSED
    // eventRepository.updateStatusToClosed(
    // EventStatus.ONGOING,
    // EventStatus.CLOSED,
    // now);
    // }
}
