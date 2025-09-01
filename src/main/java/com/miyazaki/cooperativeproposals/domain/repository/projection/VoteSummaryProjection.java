package com.miyazaki.cooperativeproposals.domain.repository.projection;

public interface VoteSummaryProjection {
    Integer getCountYes();
    Integer getCountNo();
}
