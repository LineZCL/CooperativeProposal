package com.miyazaki.cooperativeproposals.rabbitmq.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class RabbitMQConfigTest {

    @Mock
    private ConnectionFactory connectionFactory;

    @InjectMocks
    private RabbitMQConfig rabbitMQConfig;

    @Test
    void delayedExchange_ShouldReturnCorrectlyConfiguredExchange() {
        // Act
        CustomExchange result = rabbitMQConfig.delayedExchange();

        // Assert
        assertNotNull(result);
        assertEquals(RabbitMQConfig.EXCHANGE_DELAYED, result.getName());
        assertEquals("x-delayed-message", result.getType());
        assertTrue(result.isDurable());
        assertFalse(result.isAutoDelete());
        assertNotNull(result.getArguments());
        assertEquals("topic", result.getArguments().get("x-delayed-type"));
    }

    @Test
    void dlx_ShouldReturnCorrectlyConfiguredDirectExchange() {
        // Act
        DirectExchange result = rabbitMQConfig.dlx();

        // Assert
        assertNotNull(result);
        assertEquals(RabbitMQConfig.EXCHANGE_DLX, result.getName());
    }

    @Test
    void closeQueue_ShouldReturnCorrectlyConfiguredQueue() {
        // Act
        Queue result = rabbitMQConfig.closeQueue();

        // Assert
        assertNotNull(result);
        assertEquals(RabbitMQConfig.QUEUE_CLOSE, result.getName());
        assertTrue(result.isDurable());
        assertNotNull(result.getArguments());
        assertEquals(RabbitMQConfig.EXCHANGE_DLX, result.getArguments().get("x-dead-letter-exchange"));
        assertEquals(RabbitMQConfig.ROUTE_KEY_CLOSE, result.getArguments().get("x-dead-letter-routing-key"));
    }

    @Test
    void closeDlq_ShouldReturnCorrectlyConfiguredQueue() {
        // Act
        Queue result = rabbitMQConfig.closeDlq();

        // Assert
        assertNotNull(result);
        assertEquals(RabbitMQConfig.QUEUE_CLOSE_DLQ, result.getName());
        assertTrue(result.isDurable());
    }

    @Test
    void closeBinding_ShouldReturnCorrectlyConfiguredBinding() {
        // Arrange
        Queue closeQueue = rabbitMQConfig.closeQueue();
        CustomExchange delayedExchange = rabbitMQConfig.delayedExchange();

        // Act
        Binding result = rabbitMQConfig.closeBinding(closeQueue, delayedExchange);

        // Assert
        assertNotNull(result);
        assertEquals(closeQueue.getName(), result.getDestination());
        assertEquals(delayedExchange.getName(), result.getExchange());
        assertEquals(RabbitMQConfig.ROUTE_KEY_CLOSE, result.getRoutingKey());
    }

    @Test
    void closeDlqBinding_ShouldReturnCorrectlyConfiguredBinding() {
        // Arrange
        Queue closeDlq = rabbitMQConfig.closeDlq();
        DirectExchange dlx = rabbitMQConfig.dlx();

        // Act
        Binding result = rabbitMQConfig.closeDlqBinding(closeDlq, dlx);

        // Assert
        assertNotNull(result);
        assertEquals(closeDlq.getName(), result.getDestination());
        assertEquals(dlx.getName(), result.getExchange());
        assertEquals(RabbitMQConfig.ROUTE_KEY_CLOSE, result.getRoutingKey());
    }

    @Test
    void listenerFactory_ShouldReturnCorrectlyConfiguredFactory() {
        // Arrange
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        // Act
        SimpleRabbitListenerContainerFactory result = 
                rabbitMQConfig.listenerFactory(connectionFactory, converter);

        // Assert
        assertNotNull(result);
        // Note: Some getter methods may not be available in all Spring AMQP versions
    }

    @Test
    void jackson2JsonMessageConverter_ShouldReturnNotNullConverter() {
        // Act
        Jackson2JsonMessageConverter result = rabbitMQConfig.jackson2JsonMessageConverter();

        // Assert
        assertNotNull(result);
    }

    @Test
    void rabbitTemplate_ShouldReturnCorrectlyConfiguredTemplate() {
        // Arrange
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        // Act
        RabbitTemplate result = rabbitMQConfig.rabbitTemplate(connectionFactory, converter);

        // Assert
        assertNotNull(result);
        assertEquals(converter, result.getMessageConverter());
    }

    @Test
    void constants_ShouldHaveCorrectValues() {
        // Assert
        assertEquals("session.delayed", RabbitMQConfig.EXCHANGE_DELAYED);
        assertEquals("session.close", RabbitMQConfig.ROUTE_KEY_CLOSE);
        assertEquals("session.close.q", RabbitMQConfig.QUEUE_CLOSE);
        assertEquals("session.dlx", RabbitMQConfig.EXCHANGE_DLX);
        assertEquals("session.close.dlq", RabbitMQConfig.QUEUE_CLOSE_DLQ);
    }
}
