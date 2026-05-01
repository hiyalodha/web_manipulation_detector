package com.detector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Web Manipulation Detector backend.
 * Starts an embedded Tomcat server on port 8080.
 */
@SpringBootApplication
public class ManipulationDetectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManipulationDetectorApplication.class, args);
    }
}
