package com.bet99.exercise.jobsearch.service;

import com.bet99.exercise.jobsearch.dto.SearchRequest;
import com.bet99.exercise.jobsearch.dto.SearchResponse;
import com.bet99.exercise.jobsearch.exception.IndexingException;
import com.bet99.exercise.jobsearch.exception.SearchException;
import com.bet99.exercise.jobsearch.model.JobTitle;
import com.bet99.exercise.jobsearch.repository.JobTitleRepository;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;

@Service
public class JobTitleService {

    private static final Logger logger = LoggerFactory.getLogger(JobTitleService.class);

    private final JobTitleRepository repository;

    public JobTitleService(JobTitleRepository repository) {
        this.repository = repository;
    }

    @Cacheable(
            value = "searchResults",
            key = "#request.query() + '_' + #request.language() + '_' + #request.page() + '_' + #request.size()"
    )
    public SearchResponse search(SearchRequest request) {
        try {
            logger.debug("Executing search: query={}, language={}, page={}, size={}",
                    request.query(), request.language(), request.page(), request.size());

            var response = repository.search(
                    request.query(),
                    request.language(),
                    request.getOffset(),
                    request.size()
            );

            var docs = response.getResults();
            var jobTitles = repository.documentsToJobTitles(docs);
            var totalCount = docs.getNumFound();

            logger.info("Search completed: query='{}', results={}", request.query(), totalCount);

            return SearchResponse.of(jobTitles, totalCount, request.page(), request.size());

        } catch (SolrServerException | IOException e) {
            logger.error("Search failed: {}", e.getMessage(), e);
            throw new SearchException("Failed to search job titles", e);
        }
    }

    /**
     * Index a single job title and evict relevant caches.
     */
    @CacheEvict(value = "searchResults", allEntries = true)
    public void indexJobTitle(JobTitle jobTitle) {
        try {
            repository.index(jobTitle);
            logger.info("Indexed job title: id={}, nocCode={}", jobTitle.id(), jobTitle.nocCode());
        } catch (SolrServerException | IOException e) {
            logger.error("Indexing failed for job title: {}", jobTitle.id(), e);
            throw new IndexingException("Failed to index job title", e);
        }
    }


    @CacheEvict(value = "searchResults", allEntries = true)
    public void indexJobTitles(Collection<JobTitle> jobTitles) {
        if (jobTitles.isEmpty()) {
            logger.warn("Attempted to index empty collection");
            return;
        }

        try {
            var startTime = System.currentTimeMillis();
            repository.indexBatch(jobTitles);
            var duration = System.currentTimeMillis() - startTime;

            logger.info("Batch indexed {} job titles in {}ms ({} docs/sec)",
                    jobTitles.size(), duration,
                    (int) (jobTitles.size() / (duration / 1000.0)));
        } catch (SolrServerException | IOException e) {
            logger.error("Batch indexing failed: {}", e.getMessage(), e);
            throw new IndexingException("Failed to batch index job titles", e);
        }
    }

    /**
     * Clear all indexed data and caches.
     */
    @CacheEvict(value = "searchResults", allEntries = true)
    public void clearIndex() {
        try {
            repository.deleteAll();
            logger.info("Cleared all job titles from index and cache");
        } catch (SolrServerException | IOException e) {
            logger.error("Failed to clear index: {}", e.getMessage(), e);
            throw new IndexingException("Failed to clear index", e);
        }
    }
}
