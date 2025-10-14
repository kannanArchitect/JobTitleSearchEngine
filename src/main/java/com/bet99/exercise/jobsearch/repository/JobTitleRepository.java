package com.bet99.exercise.jobsearch.repository;

import com.bet99.exercise.jobsearch.model.JobTitle;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class JobTitleRepository {

    private static final int MAX_BATCH_SIZE = 500;
    private final SolrClient solrClient;

    public JobTitleRepository(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    /**
     * Search for job titles with language-specific query optimization.
     */
    public QueryResponse search(String query, String language, int start, int rows)
            throws SolrServerException, IOException {

        var solrQuery = new SolrQuery();
        solrQuery.setQuery(query.trim());
        solrQuery.set("defType", "edismax");
        solrQuery.set("mm", "2<-1 5<-2 6<90%");


        String qf = switch (language) {
            case "fr" -> "title_fr^3 description_fr";
            case "en" -> "title_en^3 description_en";
            default -> "title_en^3 title_fr^3 description_en description_fr";
        };

        solrQuery.set("qf", qf);
        solrQuery.setFields("id", "noc_code", "title_en", "title_fr", "category", "skill_level");
        solrQuery.setStart(start);
        solrQuery.setRows(rows);

        return solrClient.query(solrQuery);
    }

    /**
     * Index a single job title document.
     */
    public void index(JobTitle jobTitle) throws SolrServerException, IOException {
        solrClient.add(createDocument(jobTitle));
        solrClient.commit();
    }

    public void indexBatch(Collection<JobTitle> jobTitles) throws SolrServerException, IOException {
        if (jobTitles.isEmpty()) return;

        int batchSize = Math.min(MAX_BATCH_SIZE, jobTitles.size());
        List<SolrInputDocument> docs;

        // Create all documents first (parallel if large)
        if (jobTitles.size() > 1000) {
            docs = jobTitles.parallelStream()
                    .map(this::createDocument)
                    .collect(Collectors.toCollection(() -> new ArrayList<>(jobTitles.size())));
        } else {
            docs = new ArrayList<>(jobTitles.size());
            jobTitles.forEach(jt -> docs.add(createDocument(jt)));
        }

        // Batch add with single commit
        for (int i = 0; i < docs.size(); i += batchSize) {
            solrClient.add(docs.subList(i, Math.min(i + batchSize, docs.size())));
        }
        solrClient.commit();
    }

    /**
     * Convert Solr document to JobTitle record.
     */
    public JobTitle documentToJobTitle(SolrDocument doc) {
        return new JobTitle(
                getField(doc, "id"),
                getField(doc, "noc_code"),
                getField(doc, "title_en"),
                getField(doc, "title_fr"),
                getField(doc, "description_en"),
                getField(doc, "description_fr"),
                getField(doc, "category"),
                getField(doc, "skill_level")
        );
    }

    public List<JobTitle> documentsToJobTitles(SolrDocumentList docs) {
        return docs.stream()
                .map(this::documentToJobTitle)
                .toList();
    }

    /**
     * Create Solr document from JobTitle record.
     */
    private SolrInputDocument createDocument(JobTitle jobTitle) {
        var doc = new SolrInputDocument();
        doc.addField("id", jobTitle.id());
        doc.addField("noc_code", jobTitle.nocCode());
        doc.addField("title_en", jobTitle.titleEn());
        doc.addField("title_fr", jobTitle.titleFr());
        doc.addField("description_en", jobTitle.descriptionEn());
        doc.addField("description_fr", jobTitle.descriptionFr());
        doc.addField("category", jobTitle.category());
        doc.addField("skill_level", jobTitle.skillLevel());
        return doc;
    }

    /**
     * Null-safe field extraction.
     */
    private String getField(SolrDocument doc, String fieldName) {
        var value = doc.getFieldValue(fieldName);
        return value != null ? value.toString() : "";
    }

    /**
     * Delete all documents efficiently.
     */
    public void deleteAll() throws SolrServerException, IOException {
        solrClient.deleteByQuery("*:*");
        solrClient.commit();
    }

}
