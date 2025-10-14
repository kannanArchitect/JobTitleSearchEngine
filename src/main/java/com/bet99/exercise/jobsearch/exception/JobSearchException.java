package com.bet99.exercise.jobsearch.exception;

public class JobSearchException extends RuntimeException {

    private final String errorCode;
    private final transient Object[] args;

    public JobSearchException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }

    public JobSearchException(String errorCode, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    public String getErrorCode() {
        return errorCode;
    }

}