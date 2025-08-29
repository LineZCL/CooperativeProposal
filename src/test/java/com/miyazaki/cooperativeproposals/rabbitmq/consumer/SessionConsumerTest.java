package com.miyazaki.cooperativeproposals.rabbitmq.consumer;

import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import com.miyazaki.cooperativeproposals.filter.RequestTraceFilter;
import com.miyazaki.cooperativeproposals.rabbitmq.message.SessionMessage;
import com.miyazaki.cooperativeproposals.service.VotingSessionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionConsumerTest {

    @Mock
    private VotingSessionService votingSessionService;

    @InjectMocks
    private SessionConsumer sessionConsumer;

    private MockedStatic<MDC> mdcMockedStatic;

    @BeforeEach
    void setUp() {
        mdcMockedStatic = mockStatic(MDC.class);
    }

    @AfterEach
    void tearDown() {
        mdcMockedStatic.close();
    }

    @Test
    void onMessage_ShouldProcessMessageSuccessfully_WhenValidSessionMessageProvided() {
        final UUID sessionId = UUID.randomUUID();
        final SessionMessage sessionMessage = new SessionMessage(sessionId);
        final String traceId = "test-trace-123";
        final VotingSession mockSession = VotingSession.builder()
                .id(sessionId)
                .build();

        when(votingSessionService.closeSession(sessionMessage)).thenReturn(mockSession);

        sessionConsumer.onMessage(sessionMessage, traceId, null);

        verify(votingSessionService, times(1)).closeSession(sessionMessage);
        mdcMockedStatic.verify(() -> MDC.put(RequestTraceFilter.TRACE_KEY, traceId), times(1));
        mdcMockedStatic.verify(() -> MDC.remove(RequestTraceFilter.TRACE_KEY), times(1));
    }

    @Test
    void onMessage_ShouldGenerateTraceId_WhenTraceIdIsNull() {
        final UUID sessionId = UUID.randomUUID();
        final SessionMessage sessionMessage = new SessionMessage(sessionId);
        final VotingSession mockSession = VotingSession.builder()
                .id(sessionId)
                .build();

        when(votingSessionService.closeSession(sessionMessage)).thenReturn(mockSession);

        sessionConsumer.onMessage(sessionMessage, null, null);

        verify(votingSessionService, times(1)).closeSession(sessionMessage);
        mdcMockedStatic.verify(() -> MDC.put(eq(RequestTraceFilter.TRACE_KEY), any(String.class)), times(1));
        mdcMockedStatic.verify(() -> MDC.remove(RequestTraceFilter.TRACE_KEY), times(1));
    }


    @Test
    void onMessage_ShouldThrowNotFoundException_WhenSessionNotFound() {
        final UUID sessionId = UUID.randomUUID();
        final SessionMessage sessionMessage = new SessionMessage(sessionId);
        final String traceId = "test-trace-123";
        final NotFoundException expectedException = new NotFoundException("Voting session not found!");

        doThrow(expectedException).when(votingSessionService).closeSession(sessionMessage);

        assertThrows(NotFoundException.class, () -> 
            sessionConsumer.onMessage(sessionMessage, traceId, null));

        verify(votingSessionService, times(1)).closeSession(sessionMessage);
        mdcMockedStatic.verify(() -> MDC.put(RequestTraceFilter.TRACE_KEY, traceId), times(1));
        mdcMockedStatic.verify(() -> MDC.remove(RequestTraceFilter.TRACE_KEY), times(1));
    }

    @Test
    void onMessage_ShouldThrowRuntimeException_WhenUnexpectedErrorOccurs() {
        final UUID sessionId = UUID.randomUUID();
        final SessionMessage sessionMessage = new SessionMessage(sessionId);
        final String traceId = "test-trace-123";
        final RuntimeException expectedException = new RuntimeException("Database connection failed");

        doThrow(expectedException).when(votingSessionService).closeSession(sessionMessage);

        assertThrows(RuntimeException.class, () -> 
            sessionConsumer.onMessage(sessionMessage, traceId, null));

        verify(votingSessionService, times(1)).closeSession(sessionMessage);
        mdcMockedStatic.verify(() -> MDC.put(RequestTraceFilter.TRACE_KEY, traceId), times(1));
        mdcMockedStatic.verify(() -> MDC.remove(RequestTraceFilter.TRACE_KEY), times(1));
    }


    @Test
    void onMessage_ShouldLogRetryAttempt_WhenXDeathHeaderPresent() {
        // Given
        final UUID sessionId = UUID.randomUUID();
        final SessionMessage sessionMessage = new SessionMessage(sessionId);
        final String traceId = "test-trace-123";
        final Object xDeathHeader = "retry-info";
        final VotingSession mockSession = VotingSession.builder()
                .id(sessionId)
                .build();

        when(votingSessionService.closeSession(sessionMessage)).thenReturn(mockSession);

        sessionConsumer.onMessage(sessionMessage, traceId, xDeathHeader);

        verify(votingSessionService, times(1)).closeSession(sessionMessage);
        mdcMockedStatic.verify(() -> MDC.put(RequestTraceFilter.TRACE_KEY, traceId), times(1));
        mdcMockedStatic.verify(() -> MDC.remove(RequestTraceFilter.TRACE_KEY), times(1));
    }

    @Test
    void onMessage_ShouldHandleMultipleConsecutiveCalls_WhenCalledMultipleTimes() {
        final UUID sessionId1 = UUID.randomUUID();
        final UUID sessionId2 = UUID.randomUUID();
        final SessionMessage sessionMessage1 = new SessionMessage(sessionId1);
        final SessionMessage sessionMessage2 = new SessionMessage(sessionId2);
        final String traceId1 = "trace-1";
        final String traceId2 = "trace-2";
        
        final VotingSession mockSession1 = VotingSession.builder().id(sessionId1).build();
        final VotingSession mockSession2 = VotingSession.builder().id(sessionId2).build();

        when(votingSessionService.closeSession(sessionMessage1)).thenReturn(mockSession1);
        when(votingSessionService.closeSession(sessionMessage2)).thenReturn(mockSession2);

        sessionConsumer.onMessage(sessionMessage1, traceId1, null);
        sessionConsumer.onMessage(sessionMessage2, traceId2, null);

        verify(votingSessionService, times(1)).closeSession(sessionMessage1);
        verify(votingSessionService, times(1)).closeSession(sessionMessage2);
        mdcMockedStatic.verify(() -> MDC.put(RequestTraceFilter.TRACE_KEY, traceId1), times(1));
        mdcMockedStatic.verify(() -> MDC.put(RequestTraceFilter.TRACE_KEY, traceId2), times(1));
        mdcMockedStatic.verify(() -> MDC.remove(RequestTraceFilter.TRACE_KEY), times(2));
    }

}
