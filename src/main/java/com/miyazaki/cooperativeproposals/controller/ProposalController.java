package com.miyazaki.cooperativeproposals.controller;

import com.miyazaki.cooperativeproposals.controller.dto.request.CreateProposalRequest;
import com.miyazaki.cooperativeproposals.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
