package com.bet99.exercise.jobsearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record JobTitle(
        @JsonProperty("id")
        String id,

        @JsonProperty("noc_code")
        String nocCode,

        @JsonProperty("title_en")
        String titleEn,

        @JsonProperty("title_fr")
        String titleFr,

        @JsonProperty("description_en")
        String descriptionEn,

        @JsonProperty("description_fr")
        String descriptionFr,

        @JsonProperty("category")
        String category,

        @JsonProperty("skill_level")
        String skillLevel
) implements Serializable {
    /**
     * Compact constructor with validation and defaults.
     */
    public JobTitle {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID cannot be null or blank");
        }
        if (nocCode == null || nocCode.isBlank()) {
            throw new IllegalArgumentException("NOC code cannot be null or blank");
        }

        // Provide defaults for optional fields
        titleEn = titleEn != null ? titleEn : "";
        titleFr = titleFr != null ? titleFr : "";
        descriptionEn = descriptionEn != null ? descriptionEn : "";
        descriptionFr = descriptionFr != null ? descriptionFr : "";
        category = category != null ? category : "General";
        skillLevel = skillLevel != null ? skillLevel : "Unknown";
    }
}
