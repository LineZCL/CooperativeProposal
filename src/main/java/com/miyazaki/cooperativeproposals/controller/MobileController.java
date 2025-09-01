package com.miyazaki.cooperativeproposals.controller;

import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.FormField;
import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.MobileFormScreen;
import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.MobileScreenResponse;
import com.miyazaki.cooperativeproposals.controller.dto.response.mobile.MobileSelectionScreen;
import com.miyazaki.cooperativeproposals.domain.entity.Proposal;
import com.miyazaki.cooperativeproposals.domain.entity.VotingSession;
import com.miyazaki.cooperativeproposals.exception.NotFoundException;
import com.miyazaki.cooperativeproposals.service.MobileScreenService;
import com.miyazaki.cooperativeproposals.service.ProposalService;
import com.miyazaki.cooperativeproposals.service.VotingSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/mobile")
@Tag(name = "Mobile", description = "Mobile app specific endpoints with custom JSON format")
public final class MobileController {
    
    private final MobileScreenService mobileScreenService;
    private final ProposalService proposalService;
    private final VotingSessionService votingSessionService;
    
    @Operation(summary = "Get proposals list as mobile selection screen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mobile selection screen with proposals list")
    })
    @GetMapping("/proposals")
    public ResponseEntity<MobileSelectionScreen> getProposalsList(
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") final int page,
            @Parameter(description = "Page size", example = "20") 
            @RequestParam(defaultValue = "20") final int size) {
        
        log.info("Getting proposals list for mobile - page: {}, size: {}", page, size);
        
        Page<Proposal> proposals = proposalService.getAllProposalsPage(PageRequest.of(page, size));
        MobileSelectionScreen screen = mobileScreenService.createProposalList(proposals.getContent());
        
        return ResponseEntity.ok(screen);
    }
    
    @Operation(summary = "Get proposal details and voting options")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mobile screen for proposal voting"),
            @ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @GetMapping("/proposal/{proposalId}")
    public ResponseEntity<MobileScreenResponse> getProposalDetails(
            @Parameter(description = "Proposal ID") 
            @PathVariable final UUID proposalId) {
        
        log.info("Getting proposal details for mobile: {}", proposalId);
        
        Proposal proposal = proposalService.getProposal(proposalId);
        
        if (votingSessionService.hasVotingSessionOpened(proposalId)) {
            MobileSelectionScreen screen = mobileScreenService.createVotingOptions(proposal);
            return ResponseEntity.ok(screen);
        } else {
            throw new NotFoundException("No active voting session for this proposal");
        }
    }
    
    @Operation(summary = "Get voting form for specific vote choice")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mobile form screen for voting"),
            @ApiResponse(responseCode = "404", description = "Proposal not found")
    })
    @GetMapping("/vote-form/{proposalId}/{voteChoice}")
    public ResponseEntity<MobileFormScreen> getVoteForm(
            @Parameter(description = "Proposal ID") 
            @PathVariable final UUID proposalId,
            @Parameter(description = "Vote choice") 
            @PathVariable final Boolean voteChoice) {
        
        log.info("Getting vote form for proposal: {}, choice: {}", proposalId, voteChoice);
        
        Proposal proposal = proposalService.getProposal(proposalId);
        VotingSession session = votingSessionService.getSessionActiveByProposalId(proposalId);
        
        MobileFormScreen screen = mobileScreenService.createVotingForm(proposal, session);
        
        screen.getItens().add(
                FormField.builder()
                        .id("vote")
                        .tipo("HIDDEN")
                        .valor(voteChoice)
                        .build()
        );
        
        return ResponseEntity.ok(screen);
    }
    
    @Operation(summary = "Get new proposal creation form")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mobile form screen for creating new proposal")
    })
    @GetMapping("/new-proposal")
    public ResponseEntity<MobileFormScreen> getNewProposalForm() {
        log.info("Getting new proposal form for mobile");
        
        MobileFormScreen screen = mobileScreenService.createProposalForm();
        return ResponseEntity.ok(screen);
    }
}