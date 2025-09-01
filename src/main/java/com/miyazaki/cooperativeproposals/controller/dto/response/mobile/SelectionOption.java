package com.miyazaki.cooperativeproposals.controller.dto.response.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
@Schema(description = "Selection option for mobile screen")
public class SelectionOption {
    
    @Schema(description = "Option text")
    private String texto;
    
    @Schema(description = "Option value")
    private String valor;
    
    @Schema(description = "Option description")
    private String descricao;
    
    @Schema(description = "URL for POST request when option is selected")
    private String url;
    
    @Schema(description = "HTTP method", allowableValues = {"POST", "GET"})
    private String metodo;
    
    @Schema(description = "Body data to send with request")
    private Map<String, Object> body;
}