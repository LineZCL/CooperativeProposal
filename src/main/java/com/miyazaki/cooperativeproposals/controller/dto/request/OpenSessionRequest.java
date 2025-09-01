package com.miyazaki.cooperativeproposals.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(name = "OpenSessionRequest")
public record OpenSessionRequest(
        @Schema(description = "Voting Session duration (default=60)", example = "60")
        @PositiveOrZero @Max(3600) Integer durationSeconds) {
}
