package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.controller.dto.request.VoteRequest;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalResultResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.VoteResponse;
import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import com.miyazaki.cooperativeproposals.domain.entity.Vote;
import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import com.miyazaki.cooperativeproposals.domain.enums.SessionStatus;
import com.miyazaki.cooperativeproposals.domain.mapper.VoteMapper;
import com.miyazaki.cooperativeproposals.domain.repository.ProposalRepository;
import com.miyazaki.cooperativeproposals.domain.repository.VoteRepository;
import com.miyazaki.cooperativeproposals.domain.repository.projection.VoteSummaryProjection;
import com.miyazaki.cooperativeproposals.exception.AssociatePermissionVoteException;
import com.miyazaki.cooperativeproposals.exception.DuplicateVoteException;
import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private VotingSessionService votingSessionService;

    @Mock
    private VoteMapper voteMapper;

    @Mock
    private AssociateValidationService associateValidationService;

    @Mock
    private VoteSummaryProjection voteSummaryProjection;

    @InjectMocks
    private VoteService voteService;

    private UUID proposalId;
    private UUID associateId;
    private UUID voteId;
    private Proposal proposal;
    private VotingSession votingSession;
    private Vote vote;
    private VoteRequest voteRequest;
    private VoteResponse voteResponse;

    @BeforeEach
    void setUp() {
        proposalId = UUID.randomUUID();
        associateId = UUID.randomUUID();
        voteId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();

        proposal = Proposal.builder()
                .id(proposalId)
                .title("Test Proposal")
                .description("Test Description")
                .build();

        votingSession = VotingSession.builder()
                .id(sessionId)
                .proposal(proposal)
                .status(SessionStatus.OPENED)
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now().plusMinutes(1))
                .build();

        vote = Vote.builder()
                .id(voteId)
                .proposal(proposal)
                .votingSession(votingSession)
                .associateId(associateId)
                .vote(true)
                .votedAt(LocalDateTime.now())
                .build();

        voteRequest = new VoteRequest(associateId, "19839091069", true);

        voteResponse = VoteResponse.builder()
                .voteId(voteId)
                .proposalId(proposalId)
                .associateId(associateId)
                .vote(true)
                .votedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void castVote_ShouldReturnVoteResponse_WhenValidVoteProvided() {
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(votingSessionService.hasVotingSessionOpened(proposalId)).thenReturn(true);
        when(votingSessionService.getSessionActiveByProposalId(proposalId)).thenReturn(votingSession);
        when(voteRepository.existsByProposalIdAndAssociateId(proposalId, associateId)).thenReturn(false);
        when(voteRepository.save(any(Vote.class))).thenReturn(vote);
        when(voteMapper.toVoteResponse(vote)).thenReturn(voteResponse);
        when(associateValidationService.isValidCpf(voteRequest.associateCpf())).thenReturn(true);

        final VoteResponse result = voteService.castVote(proposalId, voteRequest);

        assertNotNull(result);
        assertEquals(voteId, result.getVoteId());
        assertEquals(proposalId, result.getProposalId());
        assertEquals(associateId, result.getAssociateId());
        assertEquals(true, result.getVote());

        verify(voteRepository, times(1)).save(any(Vote.class));
        verify(voteMapper, times(1)).toVoteResponse(vote);
    }

    @Test
    void castVote_ShouldThrowNotFoundException_WhenNoActiveVotingSession() {
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(votingSessionService.hasVotingSessionOpened(proposalId)).thenReturn(false);
        when(associateValidationService.isValidCpf(voteRequest.associateCpf())).thenReturn(true);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> voteService.castVote(proposalId, voteRequest));

        assertEquals("No active voting session found for this proposal", exception.getMessage());

        verify(voteRepository, never()).save(any());
    }

    @Test
    void castVote_ShouldThrowNotFoundException_WhenProposalNotFound() {
        when(associateValidationService.isValidCpf(voteRequest.associateCpf())).thenReturn(true);
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> voteService.castVote(proposalId, voteRequest));

        assertEquals("Proposal not found!", exception.getMessage());

        verify(voteRepository, never()).save(any());
    }

    @Test
    void castVote_ShouldThrowDuplicateVoteException_WhenAssociateAlreadyVoted() {
        when(proposalRepository.findById(proposalId)).thenReturn(Optional.of(proposal));
        when(votingSessionService.hasVotingSessionOpened(proposalId)).thenReturn(true);
        when(votingSessionService.getSessionActiveByProposalId(proposalId)).thenReturn(votingSession);
        when(voteRepository.existsByProposalIdAndAssociateId(proposalId, associateId)).thenReturn(true);
        when(associateValidationService.isValidCpf(voteRequest.associateCpf())).thenReturn(true);

        final DuplicateVoteException exception = assertThrows(DuplicateVoteException.class,
                () -> voteService.castVote(proposalId, voteRequest));

        assertEquals("Associate has already voted on this proposal", exception.getMessage());

        verify(voteRepository, never()).save(any());
    }

    @Test
    void castVote_ShouldThrowAssociatePermissionVoteException_WhenAssociateCPFNotValid() {
        when(associateValidationService.isValidCpf(voteRequest.associateCpf())).thenReturn(false);

        final AssociatePermissionVoteException exception = assertThrows(AssociatePermissionVoteException.class,
                () -> voteService.castVote(proposalId, voteRequest));

        assertEquals("Associado sem permissão para voltar", exception.getMessage());

        verify(voteRepository, never()).save(any());
    }

    @Test
    void getVoteResult_ShouldReturnCorrectResult_WhenVotesExist() {
        final Integer countYes = 5;
        final Integer countNo = 3;
        final Integer totalVotes = 8;
        
        when(voteSummaryProjection.getCountYes()).thenReturn(countYes);
        when(voteSummaryProjection.getCountNo()).thenReturn(countNo);
        when(voteRepository.countVoteResults(proposalId)).thenReturn(voteSummaryProjection);

        final ProposalResultResponse result = voteService.getVoteResult(proposalId);

        assertNotNull(result);
        assertEquals(countYes, result.getCountYes());
        assertEquals(countNo, result.getCountNo());
        assertEquals(totalVotes, result.getTotalVotes());
        
        verify(voteRepository, times(1)).countVoteResults(proposalId);
    }

    @Test
    void getVoteResult_ShouldReturnZeroVotes_WhenNoVotesExist() {
        final Integer countYes = 0;
        final Integer countNo = 0;
        final Integer totalVotes = 0;
        
        when(voteSummaryProjection.getCountYes()).thenReturn(countYes);
        when(voteSummaryProjection.getCountNo()).thenReturn(countNo);
        when(voteRepository.countVoteResults(proposalId)).thenReturn(voteSummaryProjection);

        final ProposalResultResponse result = voteService.getVoteResult(proposalId);

        assertNotNull(result);
        assertEquals(countYes, result.getCountYes());
        assertEquals(countNo, result.getCountNo());
        assertEquals(totalVotes, result.getTotalVotes());
        
        verify(voteRepository, times(1)).countVoteResults(proposalId);
    }

}
