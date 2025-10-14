package com.bet99.exercise.jobsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JobSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobSearchApplication.class, args);
    }
}
