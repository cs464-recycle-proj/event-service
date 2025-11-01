package com.greenloop.event_service.services;

import com.greenloop.event_service.dtos.EventParticipationMessage;
import com.greenloop.event_service.models.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.gamification}")
    private String gamificationExchange;

    /**
     * Publishes an event participation message to RabbitMQ for the gamification service to consume
     * 
     * @param event The event
     * @param userId The user who participated
     * @param coinsEarned The coins earned from participation
     */
    public void publishEventParticipation(Event event, UUID userId, int coinsEarned) {
        try {
            // Extract numeric ID from UUID (simplified - you might want to use a mapping table)
            // For now, using hashCode as a simple int conversion
            int eventIdInt = Math.abs(event.getId().hashCode());
            int userIdInt = Math.abs(userId.hashCode());

            EventParticipationMessage message = EventParticipationMessage.builder()
                    .eventId(eventIdInt)
                    .userId(userIdInt)
                    .eventType(event.getType().toString())
                    .participationType("attended")
                    .coinsEarned(coinsEarned)
                    .timestamp(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(
                    gamificationExchange,
                    "event.participation",
                    message
            );

            log.info("Published event participation message for user: {} at event: {}", userId, event.getId());
        } catch (Exception e) {
            // Log the error but don't fail the attendance marking process
            log.error("Failed to publish event participation message for user: {} at event: {}. Error: {}", 
                    userId, event.getId(), e.getMessage(), e);
        }
    }
}
