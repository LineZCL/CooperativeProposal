package com.miyazaki.cooperativeproposals.repository;

import com.miyazaki.cooperativeproposals.entity.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, UUID> {
}
