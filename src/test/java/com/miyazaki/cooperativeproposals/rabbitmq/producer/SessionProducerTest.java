package com.miyazaki.cooperativeproposals.rabbitmq.producer;

import com.miyazaki.cooperativeproposals.rabbitmq.config.RabbitMQConfig;
import com.miyazaki.cooperativeproposals.rabbitmq.message.SessionMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SessionProducer sessionProducer;

    @Test
    void schedulerSessionClosure_ShouldSendMessageWithCorrectParameters_WhenValidInputProvided() {
        final UUID sessionId = UUID.randomUUID();
        final long delayMs = 5000L;

        sessionProducer.schedulerSessionClosure(sessionId, delayMs);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_DELAYED),
                eq(RabbitMQConfig.ROUTE_KEY_CLOSE),
                any(SessionMessage.class),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    void schedulerSessionClosure_ShouldCreateCorrectSessionMessage_WhenValidSessionIdProvided() {
        final UUID sessionId = UUID.randomUUID();
        final long delayMs = 10000L;

        final ArgumentCaptor<SessionMessage> messageCaptor = ArgumentCaptor.forClass(SessionMessage.class);

        sessionProducer.schedulerSessionClosure(sessionId, delayMs);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_DELAYED),
                eq(RabbitMQConfig.ROUTE_KEY_CLOSE),
                messageCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        final SessionMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertEquals(sessionId, capturedMessage.votingSessionId());
    }

    @Test
    void schedulerSessionClosure_ShouldSetCorrectDelayHeader_WhenDelayMsProvided() {
        final UUID sessionId = UUID.randomUUID();
        final long delayMs = 15000L;

        final ArgumentCaptor<MessagePostProcessor> processorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        final Message mockMessage = mock(Message.class);
        final MessageProperties mockProperties = mock(MessageProperties.class);
        when(mockMessage.getMessageProperties()).thenReturn(mockProperties);

        sessionProducer.schedulerSessionClosure(sessionId, delayMs);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_DELAYED),
                eq(RabbitMQConfig.ROUTE_KEY_CLOSE),
                any(SessionMessage.class),
                processorCaptor.capture()
        );

        final MessagePostProcessor capturedProcessor = processorCaptor.getValue();
        final Message processedMessage = capturedProcessor.postProcessMessage(mockMessage);

        assertEquals(mockMessage, processedMessage);
        verify(mockProperties, times(1)).setHeader("x-delay", delayMs);
    }

    @Test
    void schedulerSessionClosure_ShouldHandleLargeDelay_WhenDelayMsIsVeryLarge() {
        final UUID sessionId = UUID.randomUUID();
        final long delayMs = Long.MAX_VALUE;

        final ArgumentCaptor<MessagePostProcessor> processorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);

        final Message mockMessage = mock(Message.class);
        final MessageProperties mockProperties = mock(MessageProperties.class);
        when(mockMessage.getMessageProperties()).thenReturn(mockProperties);

        sessionProducer.schedulerSessionClosure(sessionId, delayMs);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_DELAYED),
                eq(RabbitMQConfig.ROUTE_KEY_CLOSE),
                any(SessionMessage.class),
                processorCaptor.capture()
        );

        final MessagePostProcessor capturedProcessor = processorCaptor.getValue();
        capturedProcessor.postProcessMessage(mockMessage);

        verify(mockProperties, times(1)).setHeader("x-delay", Long.MAX_VALUE);
    }

    @Test
    void schedulerSessionClosure_ShouldUseCorrectExchangeAndRoutingKey_WhenCalled() {
        UUID sessionId = UUID.randomUUID();
        long delayMs = 30000L;

        sessionProducer.schedulerSessionClosure(sessionId, delayMs);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("session.delayed"),  // Verify exact exchange name
                eq("session.close"),    // Verify exact routing key
                any(SessionMessage.class),
                any(MessagePostProcessor.class)
        );
    }

    @Test
    void schedulerSessionClosure_ShouldCreateUniqueMessages_WhenCalledMultipleTimes() {
        final UUID sessionId1 = UUID.randomUUID();
        final UUID sessionId2 = UUID.randomUUID();
        final long delayMs1 = 5000L;
        final long delayMs2 = 10000L;

        final ArgumentCaptor<SessionMessage> messageCaptor = ArgumentCaptor.forClass(SessionMessage.class);

        sessionProducer.schedulerSessionClosure(sessionId1, delayMs1);
        sessionProducer.schedulerSessionClosure(sessionId2, delayMs2);

        verify(rabbitTemplate, times(2)).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_DELAYED),
                eq(RabbitMQConfig.ROUTE_KEY_CLOSE),
                messageCaptor.capture(),
                any(MessagePostProcessor.class)
        );

        assertEquals(sessionId1, messageCaptor.getAllValues().get(0).votingSessionId());
        assertEquals(sessionId2, messageCaptor.getAllValues().get(1).votingSessionId());
    }
}
