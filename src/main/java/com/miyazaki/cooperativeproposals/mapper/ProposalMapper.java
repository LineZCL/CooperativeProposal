package com.miyazaki.cooperativeproposals.mapper;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.entity.Proposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProposalMapper {
    @Mapping(target = "id", ignore = true)
    Proposal toEntity(CreateProposalRequest createProposalRequest);
}
