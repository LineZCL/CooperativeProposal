package com.miyazaki.cooperativeproposals.mapper;

import com.miyazaki.cooperativeproposals.controller.dto.response.SessionResponse;
import com.miyazaki.cooperativeproposals.entity.VotingSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VotingSessionMapper {
    SessionResponse toSessionResponse(VotingSession votingSession);
}
