package com.miyazaki.cooperativeproposals.controller.dto.response.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Base class for all mobile screen responses")
public abstract class MobileScreenResponse {
    
   protected MobileScreenResponse() {
    }

   @Schema(description = "Type of the screen", allowableValues = {"FORMULARIO", "SELECAO"})
   protected String tipo;
}
