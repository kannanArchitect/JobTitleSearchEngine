package com.bet99.exercise.jobsearch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record SearchRequest(
        @NotBlank(message = "Query cannot be empty")
        String query,

        @Pattern(regexp = "en|fr", message = "Language must be 'en' or 'fr'")
        String language,

        @PositiveOrZero(message = "Page must be zero or positive")
        int page,

        @Positive(message = "Size must be positive")
        int size
) {

    /**
     * Compact constructor with validation and normalization.
     */
    public SearchRequest {
        language = language != null && !language.isBlank() ? language : "en";
        page = Math.max(0, page);
        size = size > 0 ? Math.min(size, 100) : 10; // Cap at 100 for performance
    }

    /**
     * Calculate offset for pagination.
     */
    public int getOffset() {
        return page * size;
    }
}