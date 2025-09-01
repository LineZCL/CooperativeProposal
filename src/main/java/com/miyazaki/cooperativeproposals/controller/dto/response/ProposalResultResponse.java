package com.miyazaki.cooperativeproposals.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Proposal vote result")
public class ProposalResultResponse {
    @Schema(description = "Count 'YES' vote")
    private Integer countYes;
    @Schema(description = "Count 'NO' vote")
    private Integer countNo;
    @Schema(description = "Count votes")
    private Integer totalVotes;
}
