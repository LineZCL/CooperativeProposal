package com.miyazaki.cooperativeproposals.domain.mapper;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProposalMapper {
    @Mapping(target = "id", ignore = true)
    Proposal toEntity(CreateProposalRequest createProposalRequest);
}
