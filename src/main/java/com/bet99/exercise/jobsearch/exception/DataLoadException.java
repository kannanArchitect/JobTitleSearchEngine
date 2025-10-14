package com.bet99.exercise.jobsearch.exception;

public class DataLoadException extends JobSearchException {
    public DataLoadException(String message, Throwable cause) {
        super("DATA_LOAD_ERROR", message, cause);
    }
}
