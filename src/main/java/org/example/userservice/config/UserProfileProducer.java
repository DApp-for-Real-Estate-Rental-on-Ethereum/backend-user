package org.example.userservice.config;

import org.example.userservice.dto.requests.UserProfileUpdateRequestDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserProfileProducer {
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.user.exchange.name:user-exchange}")
    private String exchangeName;

    @Value("${rabbitmq.user.routing.key:user.routing.key}")
    private String routingKey;

    public UserProfileProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendUserProfileUpdate(UserProfileUpdateRequestDTO message) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }
}

