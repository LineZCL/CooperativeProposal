package com.miyazaki.cooperativeproposals.repository;

import com.miyazaki.cooperativeproposals.entity.VotingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VotingSessionRepository extends JpaRepository<VotingSession, UUID> {
}
