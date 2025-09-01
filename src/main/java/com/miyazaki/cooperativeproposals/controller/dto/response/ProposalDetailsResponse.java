package com.miyazaki.cooperativeproposals.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.miyazaki.cooperativeproposals.domain.enums.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Proposal details")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProposalDetailsResponse {
    @Schema(description = "ID of the proposal")
    private UUID proposalId;
    @Schema(description = "Title of the proposal")
    private String title;
    @Schema(description = "Description of the proposal")
    private String description;
    @Schema(description = "Current status of the proposal")
    private ProposalStatusEnum status;
    private ProposalResultResponse result;


}
