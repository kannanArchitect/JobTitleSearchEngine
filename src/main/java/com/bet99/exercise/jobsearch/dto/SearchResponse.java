package com.bet99.exercise.jobsearch.dto;


import com.bet99.exercise.jobsearch.model.JobTitle;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public record SearchResponse(
        @JsonProperty("results")
        List<JobTitle> results,

        @JsonProperty("total_count")
        long totalCount,

        @JsonProperty("page")
        int page,

        @JsonProperty("page_size")
        int pageSize,

        @JsonProperty("total_pages")
        int totalPages
) implements Serializable {
    /**
     * Compact constructor that calculates total pages and ensures immutability.
     */
    public SearchResponse {
        results = results != null ? List.copyOf(results) : List.of();
        totalPages = pageSize > 0 ? (int) Math.ceil((double) totalCount / pageSize) : 0;
    }

    /**
     * Factory method for creating response from search results.
     */
    public static SearchResponse of(List<JobTitle> results, long totalCount, int page, int pageSize) {
        return new SearchResponse(results, totalCount, page, pageSize, 0);
    }

}
