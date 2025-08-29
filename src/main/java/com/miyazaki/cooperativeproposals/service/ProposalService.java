package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.controller.dto.request.OpenSessionRequest;
import com.miyazaki.cooperativeproposals.controller.dto.response.SessionResponse;
import com.miyazaki.cooperativeproposals.entity.Proposal;
import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import com.miyazaki.cooperativeproposals.exception.SessionOpenedException;
import com.miyazaki.cooperativeproposals.mapper.ProposalMapper;
import com.miyazaki.cooperativeproposals.mapper.VotingSessionMapper;
import com.miyazaki.cooperativeproposals.repository.ProposalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProposalService {
    private final ProposalRepository proposalRepository;
    private final VotingSessionService votingSessionService;
    private final VotingSessionMapper votingSessionMapper;
    private final ProposalMapper proposalMapper;
    private static final Integer DEFAULT_DURATION = 60;


    @Transactional
    public void create(CreateProposalRequest proposalRequest){
        final var proposal = proposalMapper.toEntity(proposalRequest);
        proposalRepository.save(proposal);
    }

    @Transactional
    public SessionResponse openVotingSession(UUID proposalId, OpenSessionRequest request) throws NotFoundException, SessionOpenedException {
        final var proposal = getProposal(proposalId);
        if(votingSessionService.hasVotingSessionOpened(proposalId)){
            throw new SessionOpenedException("Session voting to proposal already opened");
        }

        final Integer duration = Objects.nonNull(request) && Objects.nonNull(request.durationSeconds()) ? request.durationSeconds() : DEFAULT_DURATION;
        final var session = votingSessionService.create(proposal, duration);
        votingSessionService.schedulerSessionClosure(session.getId(), Long.valueOf(duration));
        return votingSessionMapper.toSessionResponse(session);
    }

    public Proposal getProposal(UUID proposalId) throws NotFoundException {
        final Optional<Proposal> proposalOptional = proposalRepository.findById(proposalId);
        if(proposalOptional.isEmpty())
            throw new NotFoundException("Proposal not found!");
        return proposalOptional.get();
    }


}
