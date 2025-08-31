package com.miyazaki.cooperativeproposals.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Schema(description = "Paginated response wrapper")
public class PagedResponse<T> {
    
    @Schema(description = "List of items for current page")
    private List<T> content;
    
    @Schema(description = "Current page number (0-based)")
    private int page;
    
    @Schema(description = "Number of items per page")
    private int size;
    
    @Schema(description = "Total number of elements across all pages")
    private long totalElements;
    
    @Schema(description = "Total number of pages")
    private int totalPages;
    
}
