package com.miyazaki.cooperativeproposals.domain.mapper;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalDetailsResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalStatusEnum;
import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProposalMapper {
    @Mapping(target = "id", ignore = true)
    Proposal toEntity(CreateProposalRequest createProposalRequest);

    @Mapping(target = "proposalId", source = "proposal.id")
    @Mapping(target = "title", source = "proposal.title")
    @Mapping(target = "description", source = "proposal.description")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "result", ignore = true)
    ProposalDetailsResponse toProposalDetailsResponse(Proposal proposal, ProposalStatusEnum status);
}
