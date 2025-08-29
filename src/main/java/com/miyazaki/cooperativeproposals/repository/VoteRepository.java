package com.miyazaki.cooperativeproposals.repository;

import com.miyazaki.cooperativeproposals.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VoteRepository extends JpaRepository<Vote, UUID> {
}
