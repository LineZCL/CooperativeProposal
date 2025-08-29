package com.miyazaki.cooperativeproposals.rabbitmq.consumer;

import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import com.miyazaki.cooperativeproposals.filter.RequestTraceFilter;
import com.miyazaki.cooperativeproposals.rabbitmq.config.RabbitMQConfig;
import com.miyazaki.cooperativeproposals.rabbitmq.message.SessionMessage;
import com.miyazaki.cooperativeproposals.service.VotingSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionConsumer {
    private final VotingSessionService votingSessionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CLOSE, containerFactory = "listenerFactory")
    public void onMessage(SessionMessage payload,
                          @Header(name = RequestTraceFilter.TRACE_KEY, required = false) String traceId,
                          @Header(name = "x-death", required = false) Object xDeath) {
        if (traceId == null || traceId.isBlank()) traceId = "amqp-" + UUID.randomUUID();
        MDC.put(RequestTraceFilter.TRACE_KEY, traceId);
        
        try {
            log.info("Processing session closure  for SessionId: {}", payload.votingSessionId());
            votingSessionService.closeSession(payload);
            log.info("Successfully closed session {}", payload.votingSessionId());
        } catch (NotFoundException e) {
            log.error("Session not found for closure. SessionId: {}. Message will be retried.",
                    payload.votingSessionId(), e);
            throw e;
        } finally {
            MDC.remove(RequestTraceFilter.TRACE_KEY);
        }
    }
}
