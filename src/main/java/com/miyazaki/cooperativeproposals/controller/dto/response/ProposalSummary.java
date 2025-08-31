package com.miyazaki.cooperativeproposals.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Proposal summary information")
public class ProposalSummary {
    
    @Schema(description = "Unique identifier of the proposal")
    private UUID id;
    
    @Schema(description = "Title of the proposal")
    private String title;
    
    @Schema(description = "Description of the proposal")
    private String description;
    
    @Schema(description = "Current status of the proposal")
    private ProposalStatusEnum status;
}
