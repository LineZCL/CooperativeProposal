package com.miyazaki.cooperativeproposals.mapper;

import com.miyazaki.cooperativeproposals.controller.dto.response.VoteResponse;
import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import com.miyazaki.cooperativeproposals.domain.entity.Vote;
import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import com.miyazaki.cooperativeproposals.domain.enums.SessionStatus;
import com.miyazaki.cooperativeproposals.domain.mapper.VoteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class VoteMapperTest {

    @Autowired
    private VoteMapper voteMapper;

    private UUID proposalId;
    private UUID voteId;
    private UUID sessionId;
    private UUID associateId;
    private LocalDateTime votedAt;
    private Proposal proposal;
    private VotingSession votingSession;
    private Vote vote;

    @BeforeEach
    void setUp() {
        proposalId = UUID.randomUUID();
        voteId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        associateId = UUID.randomUUID();
        votedAt = LocalDateTime.now();

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
                .votedAt(votedAt)
                .build();
    }

    @Test
    void toVoteResponse_ShouldMapCorrectly_WhenVoteIsTrue() {
        VoteResponse result = voteMapper.toVoteResponse(vote);

        assertNotNull(result);
        assertEquals(voteId, result.getVoteId());
        assertEquals(proposalId, result.getProposalId());
        assertEquals(associateId, result.getAssociateId());
        assertEquals(true, result.getVote());
        assertEquals(votedAt, result.getVotedAt());
    }

}
