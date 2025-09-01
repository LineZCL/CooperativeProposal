package com.miyazaki.cooperativeproposals.domain.repository;

import com.miyazaki.cooperativeproposals.domain.entity.Vote;
import com.miyazaki.cooperativeproposals.domain.repository.projection.VoteSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {
    
    boolean existsByProposalIdAndAssociateId(UUID proposalId, UUID associateId);

    @Query(value = """
      SELECT
        COUNT(*) FILTER (WHERE vote = true)  AS countYes,
        COUNT(*) FILTER (WHERE vote = false) AS countNo
      FROM vote
      WHERE (:proposalId IS NULL OR proposal_id = :proposalId)
      """, nativeQuery = true)
    VoteSummaryProjection countVoteResults(@Param("proposalId") UUID proposalId);
}
