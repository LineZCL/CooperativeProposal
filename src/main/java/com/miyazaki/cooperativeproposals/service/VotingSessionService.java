package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.controller.dto.request.OpenSessionRequest;
import com.miyazaki.cooperativeproposals.entity.Proposal;
import com.miyazaki.cooperativeproposals.entity.VotingSession;
import com.miyazaki.cooperativeproposals.enums.SessionStatus;
import com.miyazaki.cooperativeproposals.rabbitmq.producer.SessionProducer;
import com.miyazaki.cooperativeproposals.repository.VotingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VotingSessionService {
    private final VotingSessionRepository votingSessionRepository;
    private final SessionProducer sessionProducer;

    public boolean hasVotingSessionOpened(UUID proposalId){
        final Optional<VotingSession> optSession = votingSessionRepository.findByProposalId(proposalId);
        return optSession.isPresent();
    }

    public VotingSession create(Proposal proposal, Integer duration){
        final LocalDateTime now = LocalDateTime.now();

        final VotingSession session = VotingSession.builder().proposal(proposal)
                .openedAt(now)
                .closesAt(now.plusSeconds(duration))
                .status(SessionStatus.OPENED)
                .build();

        return votingSessionRepository.save(session);
    }

    public void schedulerSessionClosure(UUID sessionId, Long duration){
        sessionProducer.schedulerSessionClosure(sessionId, duration * 1000L);
    }
}
