package com.bet99.exercise.jobsearch.loader;

import com.bet99.exercise.jobsearch.exception.DataLoadException;
import com.bet99.exercise.jobsearch.model.JobTitle;
import com.bet99.exercise.jobsearch.service.JobTitleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * NOC data loader
 */
@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private static final int BATCH_SIZE = 500;

    private final JobTitleService jobTitleService;

    @Value("${data.loader.enabled:true}")
    private boolean loaderEnabled;

    @Value("classpath:data/noc_2021_version_1.0_classification_structure.csv")
    private Resource structureResource;

    @Value("classpath:data/noc_2021_version_1.0_elements.csv")
    private Resource elementsResource;

    public DataLoader(JobTitleService jobTitleService) {
        this.jobTitleService = jobTitleService;
    }

/*    @Override
    public void run(String... args) {
        if (!loaderEnabled) {
            logger.info("Data loader is disabled");
            return;
        }

        try {
            logger.info("Starting NOC data loading...");
            var startTime = System.currentTimeMillis();

            // Load classifications first (for descriptions)
            var classifications = loadClassifications();
            logger.info("Loaded {} classifications", classifications.size());

            // Load job title examples from elements.csv
            var jobTitles = loadJobTitlesFromElements(classifications);
            logger.info("Processed {} unique job titles", jobTitles.size());

            indexInParallelBatches(jobTitles);

            var totalTime = System.currentTimeMillis() - startTime;
            logger.info("NOC data loading completed in {}ms", totalTime);

        } catch (Exception e) {
            logger.error("Error loading NOC data: {}", e.getMessage(), e);
            throw new DataLoadException("Failed to load NOC data", e);
        }
    }*/

    @Override
    public void run(String... args) {
        if (!loaderEnabled) {
            logger.info("Data loader is disabled");
            return;
        }

        try {
            logger.info("Starting NOC data loading...");
            var startTime = System.currentTimeMillis();

            // Load classifications
            var classifications = loadClassifications();
            logger.info("Loaded {} classifications", classifications.size());

            // Create job titles list
            var jobTitles = new ArrayList<JobTitle>();

            // 1. Add main occupation titles from classification structure (Level 5 only)
            var classificationTitles = loadClassificationTitles(classifications);
            jobTitles.addAll(classificationTitles);
            logger.info("Added {} classification titles", classificationTitles.size());

            // 2. Add job examples from elements.csv
            var jobExamples = loadJobTitlesFromElements(classifications);
            jobTitles.addAll(jobExamples);
            logger.info("Added {} job examples", jobExamples.size());

            logger.info("Total processed: {} unique job titles", jobTitles.size());

            indexInParallelBatches(jobTitles);

            var totalTime = System.currentTimeMillis() - startTime;
            logger.info("NOC data loading completed in {}ms", totalTime);

        } catch (Exception e) {
            logger.error("Error loading NOC data: {}", e.getMessage(), e);
            throw new DataLoadException("Failed to load NOC data", e);
        }
    }

    private List<JobTitle> loadClassificationTitles(Map<String, ClassificationData> classifications) {
        var idCounter = new java.util.concurrent.atomic.AtomicInteger(1);

        return classifications.values().stream()
                .filter(c -> c.level() == 5)  // Only Unit Group level (detailed occupations)
                .map(c -> new JobTitle(
                        String.valueOf(idCounter.getAndIncrement()),
                        c.code(),
                        c.title(),  // "Legislators", "Cooks", etc.
                        c.title() + " (FR)",
                        c.definition(),
                        c.definition() + " (FR)",
                        c.category(),
                        determineSkillLevel(c.code())
                ))
                .toList();
    }

    /**
     * Load classifications using efficient stream processing.
     */
    private Map<String, ClassificationData> loadClassifications() throws Exception {
        try (var reader = new BufferedReader(
                new InputStreamReader(structureResource.getInputStream(), StandardCharsets.UTF_8))) {

            return reader.lines()
                    .skip(1) // Skip header
                    .parallel() // Parallel processing
                    .map(this::parseClassificationLine)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toConcurrentMap(
                            ClassificationData::code,
                            data -> data,
                            (existing, replacement) -> existing
                    ));
        }
    }

    private List<JobTitle> loadJobTitlesFromElements(Map<String, ClassificationData> classifications) throws Exception {
        var jobTitleSet = new ConcurrentHashMap<String, JobTitle>(28000); // Pre-size for known capacity
        var idCounter = new AtomicInteger(1);

        try (var reader = new BufferedReader(
                new InputStreamReader(elementsResource.getInputStream(), StandardCharsets.UTF_8))) {

            reader.lines()
                    .skip(1)
                    .parallel()
                    .filter(line -> line.contains("example") || line.contains("Job title")) // Early filter
                    .forEach(line -> processElementLine(line, classifications, jobTitleSet, idCounter));
        }

        return new ArrayList<>(jobTitleSet.values());
    }

    private void processElementLine(String line, Map<String, ClassificationData> classifications,
                                    ConcurrentHashMap<String, JobTitle> jobTitleSet, AtomicInteger idCounter) {
        try {
            var fields = parseCsvLine(line);
            if (fields.length < 5) return;

            String elementType = fields[3].trim();
            String jobTitleText = fields[4].trim();

            if (jobTitleText.isEmpty()) return;

            String nocCode = formatNocCode(fields[1].trim());
            String key = nocCode + ":" + jobTitleText;

            jobTitleSet.computeIfAbsent(key, k -> createJobTitle(
                    idCounter.getAndIncrement(), nocCode, jobTitleText, classifications.get(nocCode)));
        } catch (Exception e) {
            // Log and continue
        }
    }

    private String formatNocCode(String rawCode) {
        String cleaned = rawCode.replaceAll("[\\[\\]]", "");
        return String.format("%05d", Integer.parseInt(cleaned));
    }

    private JobTitle createJobTitle(int id, String nocCode, String title, ClassificationData classData) {
        return new JobTitle(
                String.valueOf(id), nocCode, title, title + " (FR)",
                classData != null ? classData.definition() : "",
                classData != null ? classData.definition() + " (FR)" : "",
                classData != null ? classData.category() : "General",
                determineSkillLevel(nocCode)
        );
    }




    /**
     * Index in parallel batches for maximum performance.
     */
    private void indexInParallelBatches(List<JobTitle> jobTitles) {
        var totalBatches = (jobTitles.size() + BATCH_SIZE - 1) / BATCH_SIZE;

        for (int i = 0; i < jobTitles.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, jobTitles.size());
            var batch = jobTitles.subList(i, end);

            try {
                jobTitleService.indexJobTitles(batch);
                int batchNum = (i / BATCH_SIZE) + 1;
                logger.info("Progress: {}/{} batches ({} titles)", batchNum, totalBatches, end);
            } catch (Exception e) {
                logger.error("Error indexing batch starting at {}: {}", i, e.getMessage());
            }
        }
    }



    /**
     * Parse classification line efficiently
     */
    private Optional<ClassificationData> parseClassificationLine(String line) {
        try {
            var fields = parseCsvLine(line);
            if (fields.length >= 5) {
                String rawCode = fields[2].trim();
                rawCode = rawCode.replace("[", "").replace("]", "");
                String formattedCode = String.format("%05d", Integer.parseInt(rawCode));
                String title = fields[3].trim().replace("[", "").replace("]", "");
                String definition = fields[4].trim().replace("[", "").replace("]", "");

                return Optional.of(new ClassificationData(
                        formattedCode,
                        title,
                        definition,
                        Integer.parseInt(fields[0].trim()),
                        categorizeJob(title)
                ));
            }
        } catch (Exception e) {
            // Skip malformed lines
        }
        return Optional.empty();
    }

    /**
     * Efficient CSV parsing handling quoted fields.
     */
    private String[] parseCsvLine(String line) {
        var fields = new ArrayList<String>();
        var field = new StringBuilder();
        var inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }

        fields.add(field.toString());
        return fields.toArray(String[]::new);
    }

    /**
     * Categorize job
     */
    private String categorizeJob(String title) {
        var lower = title.toLowerCase();

        return switch (lower) {
            case String s when s.contains("health") || s.contains("medical") || s.contains("nurse") -> "Health";
            case String s when s.contains("information") || s.contains("computer") || s.contains("software") -> "Information Technology";
            case String s when s.contains("education") || s.contains("teaching") || s.contains("professor") -> "Education";
            case String s when s.contains("business") || s.contains("management") || s.contains("finance") -> "Business and Finance";
            case String s when s.contains("sales") || s.contains("service") -> "Sales and Service";
            case String s when s.contains("trades") || s.contains("construction") || s.contains("manufacturing") -> "Trades and Manufacturing";
            case String s when s.contains("natural") || s.contains("science") || s.contains("engineering") -> "Natural and Applied Sciences";
            case String s when s.contains("art") || s.contains("culture") || s.contains("recreation") -> "Art, Culture and Recreation";
            default -> "General";
        };
    }

    /**
     * Determine skill level from NOC code.
     */
    private String determineSkillLevel(String code) {
        if (code != null && !code.isEmpty()) {
            return switch (code.charAt(0)) {
                case '0' -> "0";  // Management
                case '1', '2', '3' -> "A";  // Professional
                case '4', '5' -> "B";  // Technical
                case '6', '7' -> "C";  // Intermediate
                case '8', '9' -> "D";  // Laborer
                default -> "Unknown";
            };
        }
        return "Unknown";
    }

    private record ClassificationData(String code, String title, String definition, int level, String category) {}
}
