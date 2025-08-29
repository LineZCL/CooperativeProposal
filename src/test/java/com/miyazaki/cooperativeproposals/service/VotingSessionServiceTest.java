package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import com.miyazaki.cooperativeproposals.domain.enums.SessionStatus;
import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import com.miyazaki.cooperativeproposals.rabbitmq.message.SessionMessage;
import com.miyazaki.cooperativeproposals.rabbitmq.producer.SessionProducer;
import com.miyazaki.cooperativeproposals.domain.repository.VotingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotingSessionServiceTest {

    @Mock
    private VotingSessionRepository votingSessionRepository;

    @Mock
    private SessionProducer sessionProducer;

    @InjectMocks
    private VotingSessionService votingSessionService;

    @Test
    void hasVotingSessionOpened_ShouldReturnTrue_WhenSessionExistsAndIsOpened() {
        final UUID proposalId = UUID.randomUUID();
        final VotingSession openedSession = VotingSession.builder()
                .id(UUID.randomUUID())
                .status(SessionStatus.OPENED)
                .build();

        when(votingSessionRepository.findByProposalId(proposalId))
                .thenReturn(Optional.of(openedSession));

        final boolean result = votingSessionService.hasVotingSessionOpened(proposalId);

        assertTrue(result);
        verify(votingSessionRepository, times(1)).findByProposalId(proposalId);
    }

    @Test
    void hasVotingSessionOpened_ShouldReturnFalse_WhenNoSessionExists() {
        final UUID proposalId = UUID.randomUUID();

        when(votingSessionRepository.findByProposalId(proposalId))
                .thenReturn(Optional.empty());

        final boolean result = votingSessionService.hasVotingSessionOpened(proposalId);

        assertFalse(result);
        verify(votingSessionRepository, times(1)).findByProposalId(proposalId);
    }

    @Test
    void create_ShouldCreateVotingSessionWithCorrectFields_WhenValidProposalAndDurationProvided() {
        final Proposal proposal = Proposal.builder()
                .id(UUID.randomUUID())
                .title("Test Proposal")
                .description("Test Description")
                .build();
        final Integer duration = 60;

        final VotingSession savedSession = VotingSession.builder()
                .id(UUID.randomUUID())
                .proposal(proposal)
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now().plusSeconds(duration))
                .status(SessionStatus.OPENED)
                .build();

        when(votingSessionRepository.save(any(VotingSession.class))).thenReturn(savedSession);

        final VotingSession result = votingSessionService.create(proposal, duration);

        assertNotNull(result);
        assertEquals(savedSession.getId(), result.getId());
        assertEquals(proposal, result.getProposal());
        assertEquals(SessionStatus.OPENED, result.getStatus());

        final ArgumentCaptor<VotingSession> sessionCaptor = ArgumentCaptor.forClass(VotingSession.class);
        verify(votingSessionRepository, times(1)).save(sessionCaptor.capture());

        final VotingSession capturedSession = sessionCaptor.getValue();
        assertEquals(proposal, capturedSession.getProposal());
        assertEquals(SessionStatus.OPENED, capturedSession.getStatus());
        assertNotNull(capturedSession.getOpenedAt());
        assertNotNull(capturedSession.getClosesAt());
        assertEquals(capturedSession.getOpenedAt().plusSeconds(duration), capturedSession.getClosesAt());
    }

    @Test
    void create_ShouldCreateSessionWithLongDuration_WhenLargeDurationProvided() {
        final Proposal proposal = Proposal.builder()
                .id(UUID.randomUUID())
                .title("Test Proposal")
                .description("Test Description")
                .build();
        final Integer duration = 3600; // 1 hour

        final VotingSession savedSession = VotingSession.builder()
                .id(UUID.randomUUID())
                .proposal(proposal)
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now().plusSeconds(duration))
                .status(SessionStatus.OPENED)
                .build();

        when(votingSessionRepository.save(any(VotingSession.class))).thenReturn(savedSession);

        final VotingSession result = votingSessionService.create(proposal, duration);

        assertNotNull(result);
        verify(votingSessionRepository, times(1)).save(any(VotingSession.class));

        final ArgumentCaptor<VotingSession> sessionCaptor = ArgumentCaptor.forClass(VotingSession.class);
        verify(votingSessionRepository).save(sessionCaptor.capture());

        final VotingSession capturedSession = sessionCaptor.getValue();
        assertEquals(capturedSession.getOpenedAt().plusSeconds(3600), capturedSession.getClosesAt());
    }

    @Test
    void create_ShouldAlwaysSetStatusToOpened_WhenCreatingSession() {
        final Proposal proposal = Proposal.builder()
                .id(UUID.randomUUID())
                .title("Test Proposal")
                .description("Test Description")
                .build();
        final Integer duration = 120;

        final VotingSession savedSession = VotingSession.builder()
                .id(UUID.randomUUID())
                .proposal(proposal)
                .status(SessionStatus.OPENED)
                .build();

        when(votingSessionRepository.save(any(VotingSession.class))).thenReturn(savedSession);

        votingSessionService.create(proposal, duration);

        final ArgumentCaptor<VotingSession> sessionCaptor = ArgumentCaptor.forClass(VotingSession.class);
        verify(votingSessionRepository, times(1)).save(sessionCaptor.capture());

        final VotingSession capturedSession = sessionCaptor.getValue();
        assertEquals(SessionStatus.OPENED, capturedSession.getStatus());
    }

    @Test
    void schedulerSessionClosure_ShouldCallSessionProducer_WhenValidParametersProvided() {
        final UUID sessionId = UUID.randomUUID();
        final Long duration = 60L;
        final long expectedDelayMs = duration * 1000L;

        votingSessionService.schedulerSessionClosure(sessionId, duration);

        verify(sessionProducer, times(1)).schedulerSessionClosure(sessionId, expectedDelayMs);
    }

    @Test
    void schedulerSessionClosure_ShouldConvertSecondsToMilliseconds_WhenCalled() {
        final UUID sessionId = UUID.randomUUID();
        final Long durationInSeconds = 30L;
        final long expectedDelayMs = 30000L; // 30 seconds = 30000 milliseconds

        votingSessionService.schedulerSessionClosure(sessionId, durationInSeconds);

        verify(sessionProducer, times(1)).schedulerSessionClosure(eq(sessionId), eq(expectedDelayMs));
    }


    @Test
    void schedulerSessionClosure_ShouldHandleMaxLongValue_WhenMaxDurationProvided() {
        final UUID sessionId = UUID.randomUUID();
        final Long duration = Long.MAX_VALUE / 1000L; // Max safe value to avoid overflow
        final long expectedDelayMs = duration * 1000L;

        votingSessionService.schedulerSessionClosure(sessionId, duration);

        verify(sessionProducer, times(1)).schedulerSessionClosure(eq(sessionId), eq(expectedDelayMs));
    }

    @Test
    void closeSession_ShouldSetStatusToClosed_WhenSessionIsFound(){
        final var sessionId = UUID.randomUUID();
        final var message = new SessionMessage(sessionId);
        final VotingSession savedSession = VotingSession.builder()
                .id(sessionId)
                .status(SessionStatus.OPENED)
                .build();

        when(votingSessionRepository.findById(eq(sessionId))).thenReturn(Optional.of(savedSession));

        votingSessionService.closeSession(message);

        final ArgumentCaptor<VotingSession> sessionCaptor = ArgumentCaptor.forClass(VotingSession.class);
        verify(votingSessionRepository, times(1)).save(sessionCaptor.capture());

        final VotingSession capturedSession = sessionCaptor.getValue();
        assertEquals(SessionStatus.CLOSED, capturedSession.getStatus());
    }

    @Test
    void closeSession_ShouldThrowNotFoundException_WhenSessionNotFound(){
        final var sessionId = UUID.randomUUID();
        final var message = new SessionMessage(sessionId);

        when(votingSessionRepository.findById(eq(sessionId))).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            votingSessionService.closeSession(message);
        });

        assertEquals("Voting session not found!", exception.getMessage());
    }

    @Test
    void closeSession_ShouldThrowNotFoundException_WhenSessionMessageIsNull(){

        final NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            votingSessionService.closeSession(null);
        });

        assertEquals("Voting session not found!", exception.getMessage());
    }
}
