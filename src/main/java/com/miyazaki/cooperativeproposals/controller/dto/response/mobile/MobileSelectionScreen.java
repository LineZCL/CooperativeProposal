package com.miyazaki.cooperativeproposals.controller.dto.response.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(NON_NULL)
@Schema(description = "Selection screen for mobile app")
public class MobileSelectionScreen extends MobileScreenResponse {
    
    @Schema(description = "Screen title")
    private String titulo;
    
    @Schema(description = "Screen description")  
    private String descricao;
    
    @Schema(description = "List of selection options")
    private List<SelectionOption> opcoes;
    
}