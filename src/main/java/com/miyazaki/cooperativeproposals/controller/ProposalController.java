package com.miyazaki.cooperativeproposals.controller;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.controller.dto.request.OpenSessionRequest;
import com.miyazaki.cooperativeproposals.controller.dto.request.VoteRequest;
import com.miyazaki.cooperativeproposals.controller.dto.response.PagedResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalDetailsResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.ProposalSummary;
import com.miyazaki.cooperativeproposals.controller.dto.response.SessionResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.VoteResponse;
import com.miyazaki.cooperativeproposals.service.ProposalService;
import com.miyazaki.cooperativeproposals.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/proposal")
@Tag(name = "Proposal")
public class ProposalController {

    private final ProposalService proposalService;
    private final VoteService voteService;

    @Operation(summary = "Create a new proposal")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proposal criado."),
    })
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody final CreateProposalRequest createProposalRequest){
        log.info("Create a proposal. Title: {}", createProposalRequest.title());
        proposalService.create(createProposalRequest);
        log.info("Proposal has been created. Title: {}", createProposalRequest.title());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Open voting session to proposal")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessão aberta",
                    content = @Content(schema = @Schema(implementation = SessionResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe sessão aberta"),
            @ApiResponse(responseCode = "404", description = "Pauta não encontrada")
    })
    @PostMapping("/{proposalId}/open")
    public ResponseEntity<?> openSession(
            @PathVariable UUID proposalId,
            @Valid @RequestBody(required = false) OpenSessionRequest req) {
            log.info("Opening voting session for proposal: {}, duration: {}", 
                    proposalId, req != null ? req.durationSeconds() : "default");
            SessionResponse resp = proposalService.openVotingSession(proposalId, req);
            log.info("Voting session opened successfully for proposal: {}, sessionId: {}", 
                    proposalId, resp.getId());
            return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Get a paginated list of all proposals")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of proposals",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PagedResponse<ProposalSummary>> getAllProposals(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sort field", example = "title")
            @RequestParam(defaultValue = "title") String sortBy,
            
            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        log.info("Retrieving proposals - page: {}, size: {}, sortBy: {}, sortDirection: {}", 
                page, size, sortBy, sortDirection);
        
        final Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        final Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        final PagedResponse<ProposalSummary> response = proposalService.getAllProposals(pageable);
        
        log.info("Retrieved proposals for page {}", page);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cast a vote on a proposal")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Vote successfully cast",
                    content = @Content(schema = @Schema(implementation = VoteResponse.class))),
            @ApiResponse(responseCode = "404", description = "Proposal not found or no active voting session"),
            @ApiResponse(responseCode = "409", description = "Associate has already voted or session is not active"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "409", description = "Associate has not permission to vote")
    })
    @PostMapping("/{proposalId}/vote")
    public ResponseEntity<VoteResponse> castVote(
            @Parameter(description = "ID of the proposal to vote on", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID proposalId,
            @Valid @RequestBody VoteRequest voteRequest) {
        
        log.info("Processing vote for proposal: {}, associate: {}, vote: {}", 
                proposalId, voteRequest.associateId(), voteRequest.vote());
        
        final VoteResponse response = voteService.castVote(proposalId, voteRequest);
        
        log.info("Vote successfully processed - ID: {}, Proposal: {}, Associate: {}", 
                response.getVoteId(), proposalId, voteRequest.associateId());
        
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Get proposal details with result")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proposal details",
            content = @Content(schema = @Schema(implementation = ProposalDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @GetMapping("/{proposalId}")
    public ResponseEntity<ProposalDetailsResponse> getProposalDetail(
            @Parameter(description = "ID of the proposal to vote on", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID proposalId){
        log.info("Get proposal details for ID: {}", proposalId);
        final var proposalDetails = proposalService.getProposalDetail(proposalId);
        return ResponseEntity.ok(proposalDetails);
    }
}
