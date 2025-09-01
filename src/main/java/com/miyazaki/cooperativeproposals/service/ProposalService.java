package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.controller.dto.request.OpenSessionRequest;
import com.miyazaki.cooperativeproposals.controller.dto.response.PagedResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalDetailsResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalResultResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalStatusEnum;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalSummary;
import com.miyazaki.cooperativeproposals.controller.dto.response.SessionResponse;
import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import com.miyazaki.cooperativeproposals.exception.SessionOpenedException;
import com.miyazaki.cooperativeproposals.domain.mapper.ProposalMapper;
import com.miyazaki.cooperativeproposals.domain.mapper.VotingSessionMapper;
import com.miyazaki.cooperativeproposals.domain.repository.ProposalRepository;
import com.miyazaki.cooperativeproposals.domain.repository.VoteRepository;
import com.miyazaki.cooperativeproposals.domain.repository.projection.VoteSummaryProjection;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final VoteService voteService;
    private static final Integer DEFAULT_DURATION = 60;


    @Transactional
    public void create(CreateProposalRequest proposalRequest){
        final var proposal = proposalMapper.toEntity(proposalRequest);
        proposalRepository.save(proposal);
    }

    @Transactional
    public SessionResponse openVotingSession(UUID proposalId, OpenSessionRequest request) throws NotFoundException, SessionOpenedException {
        log.info("Starting voting session opening process for proposal: {}", proposalId);
        
        final var proposal = getProposal(proposalId);
        log.info("Proposal found: {}", proposal.getTitle());
        
        if(votingSessionService.hasVotingSessionOpened(proposalId)){
            log.warn("Attempted to open session for proposal {} but session already exists", proposalId);
            throw new SessionOpenedException("Session voting to proposal already opened");
        }

        final Integer duration = Objects.nonNull(request) && Objects.nonNull(request.durationSeconds()) ? request.durationSeconds() : DEFAULT_DURATION;

        final var session = votingSessionService.create(proposal, duration);
        log.info("Session created with ID: {} for proposal: {}", session.getId(), proposalId);
        
        votingSessionService.schedulerSessionClosure(session.getId(), Long.valueOf(duration));
        log.info("Session closure scheduled for session: {} in {} seconds", session.getId(), duration);
        
        return votingSessionMapper.toSessionResponse(session);
    }

    public Proposal getProposal(UUID proposalId) throws NotFoundException {
        final Optional<Proposal> proposalOptional = proposalRepository.findById(proposalId);
        if(proposalOptional.isEmpty())
            throw new NotFoundException("Proposal not found!");
        return proposalOptional.get();
    }

    public PagedResponse<ProposalSummary> getAllProposals(Pageable pageable) {
        log.info("Retrieving proposals with pagination - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        
        final Page<Proposal> proposalPage = proposalRepository.findAll(pageable);
        
        final var proposalSummaries = proposalPage.getContent().stream()
                .map(this::mapToProposalSummary)
                .toList();
        
        log.info("Retrieved {} proposals out of {} total", proposalSummaries.size(), proposalPage.getTotalElements());
        
        return PagedResponse.<ProposalSummary>builder()
                .content(proposalSummaries)
                .page(proposalPage.getNumber())
                .size(proposalPage.getSize())
                .totalElements(proposalPage.getTotalElements())
                .totalPages(proposalPage.getTotalPages())
                .build();
    }

    private ProposalSummary mapToProposalSummary(Proposal proposal) {
        final ProposalStatusEnum status = determineProposalStatus(proposal);
        
        return ProposalSummary.builder()
                .id(proposal.getId())
                .title(proposal.getTitle())
                .description(proposal.getDescription())
                .status(status)
                .build();
    }

    private ProposalStatusEnum determineProposalStatus(Proposal proposal) {
        if (proposal.getVotingSession() == null) {
            return ProposalStatusEnum.WAITING;
        }
        
        return switch (proposal.getVotingSession().getStatus()) {
            case OPENED -> ProposalStatusEnum.OPENED;
            case CLOSED -> ProposalStatusEnum.CLOSED;
        };
    }

    public ProposalDetailsResponse getProposalDetail(final UUID proposalId){
        final var proposal = getProposal(proposalId);
        final var proposalStatus = determineProposalStatus(proposal);

        final var details = proposalMapper.toProposalDetailsResponse(proposal, proposalStatus);

        if(proposalStatus.equals(ProposalStatusEnum.CLOSED)){
            final var result = voteService.getVoteResult(proposalId);
            details.setResult(result);
        }
        return details;
    }

    public Page<Proposal> getAllProposalsPage(Pageable pageable) {
        log.info("Retrieving proposals page: {}", pageable);
        return proposalRepository.findAll(pageable);
    }
    
}
