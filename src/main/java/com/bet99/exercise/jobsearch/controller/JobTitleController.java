package com.bet99.exercise.jobsearch.controller;

import com.bet99.exercise.jobsearch.dto.SearchRequest;
import com.bet99.exercise.jobsearch.dto.SearchResponse;
import com.bet99.exercise.jobsearch.model.JobTitle;
import com.bet99.exercise.jobsearch.service.JobTitleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/v1/jobtitles")
public class JobTitleController {

    private final JobTitleService service;

    public JobTitleController(JobTitleService service) {
        this.service = service;
    }

    /**
     * GET search endpoint with query parameters.
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "en") String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        var request = new SearchRequest(query, language, page, size);
        return ResponseEntity.ok(service.search(request));
    }

    /**
     * POST search endpoint for complex queries.
     */
    @PostMapping("/search")
    public ResponseEntity<SearchResponse> searchPost(@Valid @RequestBody SearchRequest request) {
        return ResponseEntity.ok(service.search(request));
    }

    /**
     * Index a single job title (admin).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void indexJobTitle(@Valid @RequestBody JobTitle jobTitle) {
        service.indexJobTitle(jobTitle);
    }

    /**
     * Batch index job titles (admin).
     */
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public void indexJobTitles(@RequestBody Collection<JobTitle> jobTitles) {
        service.indexJobTitles(jobTitles);
    }

    /**
     * Clear all indexed data (admin).
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearIndex() {
        service.clearIndex();
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Job Title Search Service is running");
    }
}
