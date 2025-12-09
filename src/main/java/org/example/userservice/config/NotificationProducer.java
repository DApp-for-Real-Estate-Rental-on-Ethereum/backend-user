package org.example.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.example.userservice.dto.requests.NotificationRequestDTO;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationProducer {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(NotificationRequestDTO message)
    {
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
            log.info("Notification sent successfully via RabbitMQ for channel: {}", message.getChannel());
        } catch (AmqpException e) {
            log.error("Failed to send notification via RabbitMQ. RabbitMQ may not be available. Error: {}", e.getMessage());
            log.warn("Notification request will be skipped. Please ensure RabbitMQ is running for email notifications to work.");
        } catch (Exception e) {
            log.error("Unexpected error while sending notification via RabbitMQ: {}", e.getMessage(), e);
        }
    }
}