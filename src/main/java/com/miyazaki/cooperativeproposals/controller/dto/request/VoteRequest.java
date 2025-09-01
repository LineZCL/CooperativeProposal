package com.miyazaki.cooperativeproposals.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to cast a vote on a proposal")
public record VoteRequest(
        @Schema(description = "ID of the associate casting the vote", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "Associate ID is required")
        UUID associateId,

        @Schema(description = "CPF of the associate casting the vote", example = "85490387092")
        @NotNull(message = "Associate CPF is required")
        String associateCpf,
        
        @Schema(description = "The vote value - true for YES, false for NO", example = "true")
        @NotNull(message = "Vote value is required")
        Boolean vote
) {
}
