package com.bet99.exercise.jobsearch.service;

import com.bet99.exercise.jobsearch.dto.SearchRequest;
import com.bet99.exercise.jobsearch.dto.SearchResponse;
import com.bet99.exercise.jobsearch.model.JobTitle;
import com.bet99.exercise.jobsearch.repository.JobTitleRepository;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobTitleServiceTest {

    @Mock
    private JobTitleRepository repository;

    @InjectMocks
    private JobTitleService service;

    private JobTitle testJobTitle;

    @BeforeEach
    void setUp() {
        testJobTitle = new JobTitle(
                "1", "00010", "Legislators", "Législateurs",
                "Legislators participate in...", "Les législateurs participent...",
                "Management", "0"
        );
    }

    @Test
    void testSearch_WithEmptyResults_ReturnsEmptyList() throws Exception {
        // Arrange
        SearchRequest request = new SearchRequest("nonexistent", "en", 0, 10);
        QueryResponse mockQueryResponse = mock(QueryResponse.class);
        SolrDocumentList mockDocList = new SolrDocumentList();
        mockDocList.setNumFound(0);

        when(repository.search(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(mockQueryResponse);
        when(mockQueryResponse.getResults()).thenReturn(mockDocList);
        when(repository.documentsToJobTitles(mockDocList)).thenReturn(List.of());

        // Act
        SearchResponse response = service.search(request);

        // Assert
        assertEquals(0, response.totalCount());
        assertTrue(response.results().isEmpty());
    }

    @Test
    void testIndexJobTitle_Success() throws Exception {
        // Arrange
        doNothing().when(repository).index(any(JobTitle.class));

        // Act & Assert - should not throw
        assertDoesNotThrow(() -> service.indexJobTitle(testJobTitle));
        verify(repository, times(1)).index(testJobTitle);
    }

    @Test
    void testIndexJobTitles_WithMultipleJobs_Success() throws Exception {
        // Arrange
        List<JobTitle> jobs = List.of(testJobTitle);
        doNothing().when(repository).indexBatch(anyList());

        // Act & Assert
        assertDoesNotThrow(() -> service.indexJobTitles(jobs));
        verify(repository, times(1)).indexBatch(jobs);
    }

    @Test
    void testClearIndex_Success() throws Exception {
        // Arrange
        doNothing().when(repository).deleteAll();

        // Act & Assert
        assertDoesNotThrow(() -> service.clearIndex());
        verify(repository, times(1)).deleteAll();
    }
}