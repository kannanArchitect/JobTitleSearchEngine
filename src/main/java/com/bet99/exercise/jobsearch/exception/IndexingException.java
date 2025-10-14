package com.bet99.exercise.jobsearch.exception;

public class IndexingException extends JobSearchException {
    public IndexingException(String message, Throwable cause) {
        super("INDEXING_ERROR", message, cause);
    }
}
