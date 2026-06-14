package com.interviewprep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin("*")
@SpringBootApplication
public class InterviewPrepApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewPrepApplication.class, args);
    }
}
