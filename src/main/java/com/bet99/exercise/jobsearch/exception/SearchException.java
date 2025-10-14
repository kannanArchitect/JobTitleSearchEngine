package com.bet99.exercise.jobsearch.exception;

public class SearchException extends JobSearchException {
    public SearchException(String message, Throwable cause) {
        super("SEARCH_ERROR", message, cause);
    }
}
