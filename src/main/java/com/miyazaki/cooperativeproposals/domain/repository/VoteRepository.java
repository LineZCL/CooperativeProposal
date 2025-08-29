package com.miyazaki.cooperativeproposals.domain.repository;

import com.miyazaki.cooperativeproposals.domain.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
}
