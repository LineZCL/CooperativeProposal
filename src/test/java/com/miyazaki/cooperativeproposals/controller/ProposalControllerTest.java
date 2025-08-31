package com.miyazaki.cooperativeproposals.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.controller.dto.request.OpenSessionRequest;
import com.miyazaki.cooperativeproposals.controller.dto.response.PagedResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalStatusEnum;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalSummary;
import com.miyazaki.cooperativeproposals.controller.dto.response.SessionResponse;
import com.miyazaki.cooperativeproposals.domain.enums.SessionStatus;
import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import com.miyazaki.cooperativeproposals.exception.SessionOpenedException;
import com.miyazaki.cooperativeproposals.service.ProposalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProposalController.class)
class ProposalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProposalService proposalService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ProposalService proposalService() {
            return mock(ProposalService.class);
        }
    }

    @BeforeEach
    void setUp() {
        reset(proposalService);
    }

    @Test
    void create_ShouldReturnNoContent_WhenValidProposalProvided() throws Exception {
        CreateProposalRequest request = new CreateProposalRequest("Valid Title", "Valid Description");
        doNothing().when(proposalService).create(any(CreateProposalRequest.class));

        mockMvc.perform(post("/proposal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(proposalService, times(1)).create(any(CreateProposalRequest.class));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenTitleIsBlank() throws Exception {
        CreateProposalRequest request = new CreateProposalRequest("", "Valid Description");

        mockMvc.perform(post("/proposal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetros inválidos"))
                .andExpect(jsonPath("$.details").value("title: must not be blank"));

        verify(proposalService, never()).create(any());
    }

    @Test
    void create_ShouldReturnBadRequest_WhenTitleIsNull() throws Exception {
        CreateProposalRequest request = new CreateProposalRequest(null, "Valid Description");

        mockMvc.perform(post("/proposal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetros inválidos"))
                .andExpect(jsonPath("$.details").value("title: must not be blank"));

        verify(proposalService, never()).create(any());
    }

    @Test
    void openSession_ShouldReturnOk_WhenValidProposalIdAndRequestProvided() throws Exception {
        final UUID proposalId = UUID.randomUUID();
        final OpenSessionRequest request = new OpenSessionRequest(120);
        final SessionResponse expectedResponse = SessionResponse.builder()
                .id(UUID.randomUUID())
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now().plusSeconds(120))
                .status(SessionStatus.OPENED)
                .build();

        when(proposalService.openVotingSession(eq(proposalId), any(OpenSessionRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/proposal/{proposalId}/open", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.getId().toString()))
                .andExpect(jsonPath("$.status").value("OPENED"));

        verify(proposalService, times(1)).openVotingSession(eq(proposalId), any(OpenSessionRequest.class));
    }

    @Test
    void openSession_ShouldReturnOk_WhenValidProposalIdAndNoRequestBodyProvided() throws Exception {
        final UUID proposalId = UUID.randomUUID();
        final SessionResponse expectedResponse = SessionResponse.builder()
                .id(UUID.randomUUID())
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now().plusSeconds(60))
                .status(SessionStatus.OPENED)
                .build();

        when(proposalService.openVotingSession(eq(proposalId), eq(null)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/proposal/{proposalId}/open", proposalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.getId().toString()))
                .andExpect(jsonPath("$.status").value("OPENED"));

        verify(proposalService, times(1)).openVotingSession(eq(proposalId), eq(null));
    }

    @Test
    void openSession_ShouldReturnOk_WhenValidProposalIdAndEmptyRequestBodyProvided() throws Exception {
        final UUID proposalId = UUID.randomUUID();
        final SessionResponse expectedResponse = SessionResponse.builder()
                .id(UUID.randomUUID())
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now().plusSeconds(60))
                .status(SessionStatus.OPENED)
                .build();

        when(proposalService.openVotingSession(eq(proposalId), any(OpenSessionRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/proposal/{proposalId}/open", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.getId().toString()))
                .andExpect(jsonPath("$.status").value("OPENED"));

        verify(proposalService, times(1)).openVotingSession(eq(proposalId), any(OpenSessionRequest.class));
    }

    @Test
    void openSession_ShouldReturnNotFound_WhenProposalNotFound() throws Exception {
        final UUID proposalId = UUID.randomUUID();
        final OpenSessionRequest request = new OpenSessionRequest(60);

        final String errorMessage = "Proposal not found!";

        when(proposalService.openVotingSession(eq(proposalId), any(OpenSessionRequest.class)))
                .thenThrow(new NotFoundException(errorMessage));

        mockMvc.perform(post("/proposal/{proposalId}/open", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(errorMessage));

        verify(proposalService, times(1)).openVotingSession(eq(proposalId), any(OpenSessionRequest.class));
    }

    @Test
    void openSession_ShouldReturnConflict_WhenSessionAlreadyOpened() throws Exception {
        final UUID proposalId = UUID.randomUUID();
        final OpenSessionRequest request = new OpenSessionRequest(60);
        final String errorMessage = "Session voting to proposal already opened";

        when(proposalService.openVotingSession(eq(proposalId), any(OpenSessionRequest.class)))
                .thenThrow(new SessionOpenedException(errorMessage));

        mockMvc.perform(post("/proposal/{proposalId}/open", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(errorMessage));

        verify(proposalService, times(1)).openVotingSession(eq(proposalId), any(OpenSessionRequest.class));
    }

    @Test
    void openSession_ShouldReturnBadRequest_WhenDurationSecondsIsNegative() throws Exception {
        final UUID proposalId = UUID.randomUUID();
        final OpenSessionRequest request = new OpenSessionRequest(-1);

        mockMvc.perform(post("/proposal/{proposalId}/open", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetros inválidos"))
                .andExpect(jsonPath("$.details").value("durationSeconds: must be greater than or equal to 0"));

        verify(proposalService, never()).openVotingSession(any(UUID.class), any(OpenSessionRequest.class));
    }

    @Test
    void openSession_ShouldReturnBadRequest_WhenDurationSecondsExceedsMaximum() throws Exception {
        final UUID proposalId = UUID.randomUUID();
        final OpenSessionRequest request = new OpenSessionRequest(3601);

        mockMvc.perform(post("/proposal/{proposalId}/open", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parâmetros inválidos"))
                .andExpect(jsonPath("$.details").value("durationSeconds: must be less than or equal to 3600"));

        verify(proposalService, never()).openVotingSession(any(UUID.class), any(OpenSessionRequest.class));
    }

    @Test
    void openSession_ShouldReturnOk_WhenDurationSecondsIsZero() throws Exception {
        final UUID proposalId = UUID.randomUUID();
        final OpenSessionRequest request = new OpenSessionRequest(0);
        final SessionResponse expectedResponse = SessionResponse.builder()
                .id(UUID.randomUUID())
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now())
                .status(SessionStatus.OPENED)
                .build();

        when(proposalService.openVotingSession(eq(proposalId), any(OpenSessionRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/proposal/{proposalId}/open", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.getId().toString()))
                .andExpect(jsonPath("$.status").value("OPENED"));

        verify(proposalService, times(1)).openVotingSession(eq(proposalId), any(OpenSessionRequest.class));
    }

    @Test
    void openSession_ShouldReturnOk_WhenDurationSecondsIsMaximumAllowed() throws Exception {
        final UUID proposalId = UUID.randomUUID();
        final OpenSessionRequest request = new OpenSessionRequest(3600);
        final SessionResponse expectedResponse = SessionResponse.builder()
                .id(UUID.randomUUID())
                .openedAt(LocalDateTime.now())
                .closesAt(LocalDateTime.now().plusSeconds(3600))
                .status(SessionStatus.OPENED)
                .build();

        when(proposalService.openVotingSession(eq(proposalId), any(OpenSessionRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/proposal/{proposalId}/open", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expectedResponse.getId().toString()))
                .andExpect(jsonPath("$.status").value("OPENED"));

        verify(proposalService, times(1)).openVotingSession(eq(proposalId), any(OpenSessionRequest.class));
    }

    @Test
    void openSession_ShouldReturnBadRequest_WhenInvalidProposalIdFormat() throws Exception {
        final String invalidProposalId = "invalid-uuid";
        final OpenSessionRequest request = new OpenSessionRequest(60);

        mockMvc.perform(post("/proposal/{proposalId}/open", invalidProposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(proposalService, never()).openVotingSession(any(UUID.class), any(OpenSessionRequest.class));
    }

    @Test
    void openSession_ShouldReturnBadRequest_WhenInvalidJsonProvided() throws Exception {
        final UUID proposalId = UUID.randomUUID();

        mockMvc.perform(post("/proposal/{proposalId}/open", proposalId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verify(proposalService, never()).openVotingSession(any(UUID.class), any(OpenSessionRequest.class));
    }

    @Test
    void getAllProposals_ShouldReturnOk_WhenDefaultParametersUsed() throws Exception {
        final PagedResponse<ProposalSummary> mockResponse = createMockPagedResponse();
        final Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title"));

        when(proposalService.getAllProposals(expectedPageable)).thenReturn(mockResponse);

        mockMvc.perform(get("/proposal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Proposal 1"))
                .andExpect(jsonPath("$.content[0].status").value("WAITING"))
                .andExpect(jsonPath("$.content[1].title").value("Proposal 2"))
                .andExpect(jsonPath("$.content[1].status").value("OPENED"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(proposalService, times(1)).getAllProposals(expectedPageable);
    }

    @Test
    void getAllProposals_ShouldReturnOk_WhenCustomParametersProvided() throws Exception {
        final PagedResponse<ProposalSummary> mockResponse = createMockPagedResponse();
        final Pageable expectedPageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "description"));

        when(proposalService.getAllProposals(expectedPageable)).thenReturn(mockResponse);

        mockMvc.perform(get("/proposal")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "description")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(proposalService, times(1)).getAllProposals(expectedPageable);
    }

    @Test
    void getAllProposals_ShouldReturnOk_WhenEmptyResultReturned() throws Exception {
        // Arrange
        final PagedResponse<ProposalSummary> emptyResponse = PagedResponse.<ProposalSummary>builder()
                .content(Collections.emptyList())
                .page(0)
                .size(10)
                .totalElements(0)
                .totalPages(0)
                .build();

        final Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "title"));

        when(proposalService.getAllProposals(expectedPageable)).thenReturn(emptyResponse);

        // Act & Assert
        mockMvc.perform(get("/proposal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(proposalService, times(1)).getAllProposals(expectedPageable);
    }

    private PagedResponse<ProposalSummary> createMockPagedResponse() {
        final ProposalSummary summary1 = ProposalSummary.builder()
                .id(UUID.randomUUID())
                .title("Proposal 1")
                .description("Description 1")
                .status(ProposalStatusEnum.WAITING)
                .build();

        final ProposalSummary summary2 = ProposalSummary.builder()
                .id(UUID.randomUUID())
                .title("Proposal 2")
                .description("Description 2")
                .status(ProposalStatusEnum.OPENED)
                .build();

        return PagedResponse.<ProposalSummary>builder()
                .content(Arrays.asList(summary1, summary2))
                .page(0)
                .size(10)
                .totalElements(2)
                .totalPages(1)
                .build();
    }
}
