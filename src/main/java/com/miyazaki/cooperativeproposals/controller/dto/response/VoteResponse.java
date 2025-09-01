package com.miyazaki.cooperativeproposals.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Response after casting a vote")
public class VoteResponse {
    
    @Schema(description = "Unique identifier of the vote")
    private UUID voteId;
    
    @Schema(description = "ID of the proposal that was voted on")
    private UUID proposalId;
    
    @Schema(description = "ID of the associate who cast the vote")
    private UUID associateId;
    
    @Schema(description = "The vote value - true for YES, false for NO")
    private Boolean vote;
    
    @Schema(description = "Timestamp when the vote was cast")
    private LocalDateTime votedAt;
}
