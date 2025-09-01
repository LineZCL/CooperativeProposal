package com.miyazaki.cooperativeproposals.mapper;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalDetailsResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalStatusEnum;
import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import com.miyazaki.cooperativeproposals.domain.mapper.ProposalMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProposalMapperTest {

    @Autowired
    private ProposalMapper proposalMapper;

    @Test
    void toEntity_ShouldMapAllFields_WhenValidRequestProvided() {
        CreateProposalRequest request = new CreateProposalRequest("Test Title", "Test Description");

        Proposal result = proposalMapper.toEntity(request);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Description", result.getDescription());
    }

    @Test
    void toProposalDetailsResponse_ShouldMapAllFields_WhenProposalProvided() {
        final UUID proposalId = UUID.randomUUID();
        final Proposal proposal = Proposal.builder()
                .id(proposalId)
                .title("Test Proposal Title")
                .description("Test Proposal Description")
                .build();
        final ProposalStatusEnum status = ProposalStatusEnum.WAITING;

        final ProposalDetailsResponse result = proposalMapper.toProposalDetailsResponse(proposal, status);

        assertNotNull(result);
        assertEquals(proposalId, result.getProposalId());
        assertEquals("Test Proposal Title", result.getTitle());
        assertEquals("Test Proposal Description", result.getDescription());
        assertEquals(ProposalStatusEnum.WAITING, result.getStatus());
        assertNull(result.getResult()); // Should be null as per mapping configuration
    }
}
