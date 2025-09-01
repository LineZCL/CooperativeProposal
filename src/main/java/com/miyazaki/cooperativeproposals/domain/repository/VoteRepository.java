package com.miyazaki.cooperativeproposals.domain.repository;

import com.miyazaki.cooperativeproposals.domain.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {
    
    boolean existsByProposalIdAndAssociateId(UUID proposalId, UUID associateId);
}
