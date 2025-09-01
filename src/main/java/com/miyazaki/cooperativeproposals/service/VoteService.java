package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.controller.dto.request.VoteRequest;
import com.miyazaki.cooperativeproposals.controller.dto.response.VoteResponse;
import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import com.miyazaki.cooperativeproposals.domain.entity.Vote;
import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import com.miyazaki.cooperativeproposals.domain.mapper.VoteMapper;
import com.miyazaki.cooperativeproposals.domain.repository.VoteRepository;
import com.miyazaki.cooperativeproposals.exception.AssociatePermissionVoteException;
import com.miyazaki.cooperativeproposals.exception.DuplicateVoteException;
import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteService {
    
    private final VoteRepository voteRepository;
    private final ProposalService proposalService;
    private final VotingSessionService votingSessionService;
    private final VoteMapper voteMapper;
    private final AssociateValidationService associateValidationService;
    
    @Transactional
    public VoteResponse castVote(UUID proposalId, VoteRequest voteRequest) {
        log.info("Processing vote for proposal: {}, associate: {}, vote: {}", 
                proposalId, voteRequest.associateId(), voteRequest.vote());

        if(!associateValidationService.isValidCpf(voteRequest.associateCpf())){
            throw new AssociatePermissionVoteException("Associado sem permissão para voltar");
        }

        final Proposal proposal = proposalService.getProposal(proposalId);
        
        final VotingSession votingSession = getActiveVotingSession(proposalId);
        
        validateNoDuplicateVote(proposalId, voteRequest.associateId());
        
        Vote vote = createVote(proposal, votingSession, voteRequest);
        vote = voteRepository.save(vote);
        
        log.info("Vote successfully cast - ID: {}, Proposal: {}, Associate: {}, Vote: {}", 
                vote.getId(), proposalId, voteRequest.associateId(), voteRequest.vote());
        
        return voteMapper.toVoteResponse(vote);
    }
    
    private VotingSession getActiveVotingSession(UUID proposalId) {
        
        if (!votingSessionService.hasVotingSessionOpened(proposalId)) {
            log.warn("No active voting session found for proposal: {}", proposalId);
            throw new NotFoundException("No active voting session found for this proposal");
        }
        
        return votingSessionService.getSessionActiveByProposalId(proposalId);
    }
    
    private void validateNoDuplicateVote(UUID proposalId, UUID associateId) {
        if (voteRepository.existsByProposalIdAndAssociateId(proposalId, associateId)) {
            log.warn("Associate {} has already voted on proposal {}", associateId, proposalId);
            throw new DuplicateVoteException("Associate has already voted on this proposal");
        }
    }
    
    private Vote createVote(Proposal proposal, VotingSession votingSession, VoteRequest voteRequest) {
        return Vote.builder()
                .proposal(proposal)
                .votingSession(votingSession)
                .associateId(voteRequest.associateId())
                .vote(voteRequest.vote())
                .votedAt(LocalDateTime.now())
                .build();
    }
    

}
