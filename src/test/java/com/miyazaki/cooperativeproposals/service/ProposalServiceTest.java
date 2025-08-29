package com.miyazaki.cooperativeproposals.service;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.entity.Proposal;
import com.miyazaki.cooperativeproposals.mapper.ProposalMapper;
import com.miyazaki.cooperativeproposals.repository.ProposalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private ProposalMapper proposalMapper;

    @InjectMocks
    private ProposalService proposalService;

    @Test
    void create_ShouldMapAndSaveProposal_WhenValidRequestProvided() {
        CreateProposalRequest request = new CreateProposalRequest("Test Title", "Test Description");
        Proposal mappedProposal = Proposal.builder()
                .title("Test Title")
                .description("Test Description")
                .build();

        when(proposalMapper.toEntity(request)).thenReturn(mappedProposal);
        when(proposalRepository.save(any(Proposal.class))).thenReturn(mappedProposal);

        proposalService.create(request);

        verify(proposalMapper, times(1)).toEntity(request);
        verify(proposalRepository, times(1)).save(mappedProposal);
    }

    @Test
    void create_ShouldCallRepositorySave_WhenMapperReturnsEntity() {
        CreateProposalRequest request = new CreateProposalRequest("Another Title", "Another Description");
        Proposal proposal = Proposal.builder()
                .title("Another Title")
                .description("Another Description")
                .build();

        when(proposalMapper.toEntity(request)).thenReturn(proposal);

        proposalService.create(request);

        verify(proposalRepository).save(proposal);
    }
}
