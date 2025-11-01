package com.greenloop.event_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.event-participation}")
    private String eventParticipationQueue;

    @Value("${rabbitmq.exchange.gamification}")
    private String gamificationExchange;

    /**
     * Creates the queue for event participation messages
     */
    @Bean
    public Queue eventParticipationQueue() {
        return new Queue(eventParticipationQueue, true); // durable = true
    }

    /**
     * Creates the direct exchange for gamification messages
     */
    @Bean
    public DirectExchange gamificationExchange() {
        return new DirectExchange(gamificationExchange);
    }

    /**
     * Binds the event participation queue to the gamification exchange
     */
    @Bean
    public Binding eventParticipationBinding(Queue eventParticipationQueue, DirectExchange gamificationExchange) {
        return BindingBuilder.bind(eventParticipationQueue)
                .to(gamificationExchange)
                .with("event.participation");
    }

    /**
     * Configures Jackson JSON message converter for RabbitMQ
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configures RabbitTemplate with JSON message converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
