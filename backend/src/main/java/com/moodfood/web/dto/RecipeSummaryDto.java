package com.moodfood.web.dto;

public record RecipeSummaryDto(
    Long id,
    String title,
    String imageUrl,
    Integer totalTimeMin,
    String difficulty,
    String qualityStatus
) {
}
