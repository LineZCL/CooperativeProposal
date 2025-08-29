package com.miyazaki.cooperativeproposals.rabbitmq.producer;

import com.miyazaki.cooperativeproposals.rabbitmq.config.RabbitMQConfig;
import com.miyazaki.cooperativeproposals.rabbitmq.message.SessionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionProducer {
    private final RabbitTemplate rabbitTemplate;
    public void schedulerSessionClosure(UUID sessionId, long delayMs) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_DELAYED,
                RabbitMQConfig.ROUTE_KEY_CLOSE,
                new SessionMessage(sessionId),
                msg -> { msg.getMessageProperties().setHeader("x-delay", delayMs); return msg; }
        );
    }
}
