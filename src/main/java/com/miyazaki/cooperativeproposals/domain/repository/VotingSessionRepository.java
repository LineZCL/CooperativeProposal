package com.miyazaki.cooperativeproposals.domain.repository;

import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VotingSessionRepository extends JpaRepository<VotingSession, UUID> {
    Optional<VotingSession> findByProposalId(UUID proposalId);
}
