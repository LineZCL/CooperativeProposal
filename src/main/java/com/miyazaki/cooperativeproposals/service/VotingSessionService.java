package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import com.miyazaki.cooperativeproposals.domain.enums.SessionStatus;
import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import com.miyazaki.cooperativeproposals.rabbitmq.message.SessionMessage;
import com.miyazaki.cooperativeproposals.rabbitmq.producer.SessionProducer;
import com.miyazaki.cooperativeproposals.domain.repository.VotingSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VotingSessionService {
    private final VotingSessionRepository votingSessionRepository;
    private final SessionProducer sessionProducer;

    private final static String SESSION_NOT_FOUND = "Sessão de voto nao encontrada";

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

    @Transactional
    public VotingSession closeSession(final SessionMessage sessionMessage){
        if(Objects.nonNull(sessionMessage)) {
            final var session = getSession(sessionMessage.votingSessionId());
            session.setStatus(SessionStatus.CLOSED);
            return votingSessionRepository.save(session);
        }else{
            log.error("Session is null");
            throw new NotFoundException(SESSION_NOT_FOUND);
        }
    }

    public VotingSession getSession(UUID votingSessionId){
        final var sessionOpt = votingSessionRepository.findById(votingSessionId);
        if(sessionOpt.isEmpty()){
            throw new NotFoundException(SESSION_NOT_FOUND);
        }
        return sessionOpt.get();
    }

    public VotingSession getSessionActiveByProposalId(UUID proposalId){
        final var votingSessionOpt = votingSessionRepository.findByProposalIdAndStatus(proposalId, SessionStatus.OPENED);
        if(votingSessionOpt.isEmpty()){
            throw new NotFoundException(SESSION_NOT_FOUND);
        }
        return votingSessionOpt.get();
    }
}
