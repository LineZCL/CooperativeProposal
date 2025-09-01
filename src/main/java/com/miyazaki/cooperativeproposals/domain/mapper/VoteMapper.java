package com.miyazaki.cooperativeproposals.domain.mapper;

import com.miyazaki.cooperativeproposals.controller.dto.response.VoteResponse;
import com.miyazaki.cooperativeproposals.domain.entity.Vote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VoteMapper {
    
    @Mapping(target = "voteId", source = "id")
    @Mapping(target = "proposalId", source = "proposal.id")
    VoteResponse toVoteResponse(Vote vote);

}
