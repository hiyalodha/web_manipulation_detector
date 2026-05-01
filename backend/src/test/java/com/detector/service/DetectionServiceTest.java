package com.detector.service;

import com.detector.model.AnalyzeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DetectionServiceTest {

    private DetectionService service;

    @BeforeEach
    void setUp() { service = new DetectionService(); }

    @Test @DisplayName("Detects hurry as urgency")
    void detectsHurry() {
        assertTrue(service.analyze("Hurry! This deal won't last.").getFlags().contains("urgency"));
    }

    @Test @DisplayName("Detects limited time as urgency")
    void detectsLimitedTime() {
        assertTrue(service.analyze("Limited time offer!").getFlags().contains("urgency"));
    }

    @Test @DisplayName("Detects only N left as scarcity")
    void detectsOnlyNLeft() {
        assertTrue(service.analyze("Only 3 left in stock!").getFlags().contains("scarcity"));
    }

    @Test @DisplayName("Detects almost gone as scarcity")
    void detectsAlmostGone() {
        assertTrue(service.analyze("Almost gone!").getFlags().contains("scarcity"));
    }

    @Test @DisplayName("Detects selling fast as pressure")
    void detectsSellingFast() {
        assertTrue(service.analyze("This item is selling fast!").getFlags().contains("pressure"));
    }

    @Test @DisplayName("Detects people watching as pressure")
    void detectsPeopleWatching() {
        assertTrue(service.analyze("47 people are watching this.").getFlags().contains("pressure"));
    }

    @Test @DisplayName("Detects multiple categories")
    void detectsMultiple() {
        AnalyzeResponse r = service.analyze("Only 2 left! Hurry! 30 people watching.");
        assertTrue(r.getFlags().contains("urgency"));
        assertTrue(r.getFlags().contains("scarcity"));
        assertTrue(r.getFlags().contains("pressure"));
    }

    @Test @DisplayName("Score capped at 100")
    void scoreCapped() {
        AnalyzeResponse r = service.analyze(
            "Only 1 left! Hurry! Act now! Limited time! Flash sale! 99 people watching!");
        assertTrue(r.getScore() <= 100);
    }

    @Test @DisplayName("Clean text returns no flags")
    void cleanText() {
        AnalyzeResponse r = service.analyze("A set of notebooks. Ships in 3 days.");
        assertTrue(r.isClean());
        assertEquals(0, r.getScore());
    }

    @Test @DisplayName("Handles null gracefully")
    void handlesNull() {
        assertTrue(service.analyze(null).isClean());
    }

    @Test @DisplayName("Handles blank gracefully")
    void handlesBlank() {
        assertTrue(service.analyze("   ").isClean());
    }

    @Test @DisplayName("Detection is case-insensitive")
    void caseInsensitive() {
        assertEquals(
            service.analyze("HURRY! ACT NOW!").getFlags(),
            service.analyze("hurry! act now!").getFlags()
        );
    }

    @Test @DisplayName("Confidence equals score divided by 100")
    void confidenceMatchesScore() {
        AnalyzeResponse r = service.analyze("Only 2 left! Hurry!");
        assertEquals(r.getScore() / 100.0, r.getConfidence(), 0.001);
    }
}