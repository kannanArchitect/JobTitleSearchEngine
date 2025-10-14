package com.bet99.exercise.jobsearch.loader;

import com.bet99.exercise.jobsearch.service.JobTitleService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DataLoaderTest {

    @Mock
    private JobTitleService jobTitleService;

    private DataLoader dataLoader;

    @Mock
    private Resource structureResource;

    @Mock
    private Resource elementsResource;

    @BeforeEach
    void setUp() {
        dataLoader = new DataLoader(jobTitleService);
    }

    @Test
    void testRun_WhenDisabled_DoesNotLoadData() {
        // Arrange
        ReflectionTestUtils.setField(dataLoader, "loaderEnabled", false);

        // Act
        dataLoader.run();

        // Assert
        verifyNoInteractions(jobTitleService);
    }
}