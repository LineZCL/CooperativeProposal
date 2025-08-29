package com.miyazaki.cooperativeproposals.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "CreateProposalRequest")
public record CreateProposalRequest(
    @Schema(minLength = 1, example = "Prestação de contas") @NotBlank String title,
    @Schema(example = "Descrição da pauta") String description
) {
}
