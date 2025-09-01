package com.miyazaki.cooperativeproposals.controller.dto.response.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
@Schema(description = "Form field for mobile screen")
public class FormField {

    @Schema(description = "Field identifier")
    private String id;

    @Schema(description = "Field label")
    private String label;

    @Schema(description = "Field type", allowableValues = {"TEXT", "NUMBER", "DATE", "BOOLEAN", "HIDDEN"})
    private String tipo;

    @Schema(description = "Field value")
    private Object valor;

    @Schema(description = "Whether field is required")
    private Boolean obrigatorio;

    @Schema(description = "Field placeholder")
    private String placeholder;
}