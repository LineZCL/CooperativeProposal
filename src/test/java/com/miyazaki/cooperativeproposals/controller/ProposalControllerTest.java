package com.miyazaki.cooperativeproposals.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.controller.dto.request.OpenSessionRequest;
import com.miyazaki.cooperativeproposals.controller.dto.request.VoteRequest;
import com.miyazaki.cooperativeproposals.controller.dto.response.PagedResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalStatusEnum;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalSummary;
import com.miyazaki.cooperativeproposals.controller.dto.response.SessionResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.VoteResponse;
import com.miyazaki.cooperativeproposals.service.ProposalService;
import com.miyazaki.cooperativeproposals.service.VoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProposalControllerTest {

    @Mock
    private ProposalService proposalService;

    @Mock
    private VoteService voteService;

    @InjectMocks
    private ProposalController proposalController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID proposalId;
    private UUID sessionId;
    private UUID associateId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(proposalController).build();
        objectMapper = new ObjectMapper();
        proposalId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        associateId = UUID.randomUUID();
    }

    @Test
    void create_ShouldReturnNoContent_WhenValidProposalProvided() {
        final CreateProposalRequest request = new CreateProposalRequest("Valid Title", "Valid Description");
        doNothing().when(proposalService).create(any(CreateProposalRequest.class));

        final ResponseEntity<Void> response = proposalController.create(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(proposalService, times(1)).create(request);
    }

    @Test
    void create_ShouldCallServiceWithCorrectParameters() {
        final CreateProposalRequest request = new CreateProposalRequest("Test Title", "Test Description");
        doNothing().when(proposalService).create(request);

        proposalController.create(request);

        verify(proposalService, times(1)).create(request);
    }

    @Test
    void openSession_ShouldReturnOk_WhenValidProposalIdAndRequestProvided() {
        final OpenSessionRequest request = new OpenSessionRequest(60);
        final SessionResponse sessionResponse = SessionResponse.builder()
                .id(sessionId)
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now().plusMinutes(1))
                .build();

        when(proposalService.openVotingSession(eq(proposalId), eq(request)))
                .thenReturn(sessionResponse);

        final ResponseEntity<?> response = proposalController.openSession(proposalId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sessionResponse, response.getBody());
        verify(proposalService, times(1)).openVotingSession(proposalId, request);
    }

    @Test
    void openSession_ShouldReturnOk_WhenValidProposalIdAndNoRequestBodyProvided() {
        final SessionResponse sessionResponse = SessionResponse.builder()
                .id(sessionId)
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now().plusMinutes(1))
                .build();

        when(proposalService.openVotingSession(eq(proposalId), eq(null)))
                .thenReturn(sessionResponse);

        final ResponseEntity<?> response = proposalController.openSession(proposalId, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sessionResponse, response.getBody());
        verify(proposalService, times(1)).openVotingSession(proposalId, null);
    }

    @Test
    void getAllProposals_ShouldReturnOk_WhenDefaultParametersUsed() {
        final ProposalSummary proposal1 = ProposalSummary.builder()
                .id(UUID.randomUUID())
                .title("Proposal 1")
                .description("Description 1")
                .status(ProposalStatusEnum.OPENED)
                .build();

        final PagedResponse<ProposalSummary> pagedResponse = PagedResponse.<ProposalSummary>builder()
                .content(Arrays.asList(proposal1))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        final Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title"));
        when(proposalService.getAllProposals(expectedPageable)).thenReturn(pagedResponse);

        final ResponseEntity<PagedResponse<ProposalSummary>> response = 
                proposalController.getAllProposals(0, 10, "title", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Proposal 1", response.getBody().getContent().get(0).getTitle());
        verify(proposalService, times(1)).getAllProposals(expectedPageable);
    }

    @Test
    void getAllProposals_ShouldReturnOk_WhenCustomParametersProvided() {
        final PagedResponse<ProposalSummary> pagedResponse = PagedResponse.<ProposalSummary>builder()
                .content(Collections.emptyList())
                .page(1)
                .size(5)
                .totalElements(0L)
                .totalPages(0)
                .build();

        final Pageable expectedPageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(proposalService.getAllProposals(expectedPageable)).thenReturn(pagedResponse);

        final ResponseEntity<PagedResponse<ProposalSummary>> response = 
                proposalController.getAllProposals(1, 5, "createdAt", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getPage());
        assertEquals(5, response.getBody().getSize());
        verify(proposalService, times(1)).getAllProposals(expectedPageable);
    }

    @Test
    void castVote_ShouldReturnCreated_WhenValidVoteProvided() {
        final VoteRequest request = new VoteRequest(associateId, "Sim", true);
        final VoteResponse voteResponse = VoteResponse.builder()
                .voteId(UUID.randomUUID())
                .proposalId(proposalId)
                .associateId(associateId)
                .vote(true)
                .votedAt(LocalDateTime.now())
                .build();

        when(voteService.castVote(eq(proposalId), eq(request))).thenReturn(voteResponse);

        final ResponseEntity<VoteResponse> response = proposalController.castVote(proposalId, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(voteResponse, response.getBody());
        assertNotNull(response.getBody());
        assertEquals(proposalId, response.getBody().getProposalId());
        assertEquals(associateId, response.getBody().getAssociateId());
        assertEquals(true, response.getBody().getVote());
        verify(voteService, times(1)).castVote(proposalId, request);
    }

}
