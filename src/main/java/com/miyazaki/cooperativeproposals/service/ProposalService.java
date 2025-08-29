package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.mapper.ProposalMapper;
import com.miyazaki.cooperativeproposals.repository.ProposalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProposalService {
    private final ProposalRepository proposalRepository;
    private final ProposalMapper proposalMapper;

    public void create(CreateProposalRequest proposalRequest){
        final var proposal = proposalMapper.toEntity(proposalRequest);
        proposalRepository.save(proposal);
    }


}
