package com.bet99.exercise.jobsearch.controller;


import com.bet99.exercise.jobsearch.dto.SearchResponse;
import com.bet99.exercise.jobsearch.model.JobTitle;
import com.bet99.exercise.jobsearch.service.JobTitleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobTitleController.class)
class JobTitleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobTitleService service;

    @Test
    void testSearch_WithValidParams_ReturnsOk() throws Exception {
        // Arrange
        JobTitle job = new JobTitle("1", "00010", "Legislators", "Législateurs",
                "Test desc", "Test desc FR", "Management", "0");
        SearchResponse response = SearchResponse.of(List.of(job), 1, 0, 10);
        when(service.search(any())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobtitles/search")
                        .param("query", "legislator")
                        .param("language", "en")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_count").value(1))
                .andExpect(jsonPath("$.results[0].title_en").value("Legislators"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.page_size").value(10));
    }

    @Test
    void testHealth_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/jobtitles/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Job Title Search Service is running"));
    }
}