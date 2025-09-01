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
@Schema(description = "Form screen for mobile app")
public class MobileFormScreen extends MobileScreenResponse {
    
    @Schema(description = "Form title")
    private String titulo;
    
    @Schema(description = "Form description")
    private String descricao;
    
    @Schema(description = "List of form fields")
    private List<FormField> itens;
    
    @Schema(description = "Action buttons")
    private List<ActionButton> botoes;

}