package com.miyazaki.cooperativeproposals.domain.mapper;

import com.miyazaki.cooperativeproposals.controller.dto.response.SessionResponse;
import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VotingSessionMapper {
    SessionResponse toSessionResponse(VotingSession votingSession);
}
