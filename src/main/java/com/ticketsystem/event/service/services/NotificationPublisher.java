package com.ticketsystem.event.service.services;

import com.greenloop.event_service.models.Event;
import com.ticketsystem.event.service.dtos.NotificationMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a");

    @Value("${rabbitmq.notification.exchange:notifications.topic}")
    private String notificationExchange;

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEventConfirmation(String email, Event event) {
        NotificationMessage message = NotificationMessage.builder()
                .type("event_confirmation")
                .email(email)
                .eventName(event.getName())
                .location(event.getLocation())
                .startDate(event.getStartDT().format(DATE_FORMATTER))
                .endDate(event.getEndDT().format(DATE_FORMATTER))
                .organizer(event.getOrganizer())
                .coins(event.getCoins())
                .details("You have successfully registered for this event.")
                .build();
        
        rabbitTemplate.convertAndSend(notificationExchange, "event.confirmation", message);
    }

    public void publishEventAttendance(String email, String eventName, int coinsEarned) {
        NotificationMessage message = NotificationMessage.builder()
                .type("event_attendance")
                .email(email)
                .eventName(eventName)
                .coinsEarned(coinsEarned)
                .details("Thank you for attending!")
                .build();
        
        rabbitTemplate.convertAndSend(notificationExchange, "event.attendance", message);
    }
}
