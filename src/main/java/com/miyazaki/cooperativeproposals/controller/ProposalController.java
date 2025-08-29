package com.miyazaki.cooperativeproposals.controller;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.controller.dto.request.OpenSessionRequest;
import com.miyazaki.cooperativeproposals.controller.dto.response.SessionResponse;
import com.miyazaki.cooperativeproposals.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/proposal")
@Tag(name = "Proposal")
public class ProposalController {

    private final ProposalService proposalService;

    @Operation(summary = "Create a new proposal")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
    })
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody final CreateProposalRequest createProposalRequest){
        proposalService.create(createProposalRequest);
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
            SessionResponse resp = proposalService.openVotingSession(proposalId, req);
            return ResponseEntity.ok(resp);

    }
}
