package com.detector.controller;

import com.detector.model.AnalyzeRequest;
import com.detector.model.AnalyzeResponse;
import com.detector.service.DetectionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * AnalyzeController — exposes the detection API.
 *
 * Endpoints:
 *   POST /analyze   — analyze text for dark patterns
 *   GET  /health    — simple health check (used by extension popup)
 *
 * CORS is enabled for all origins so the Chrome extension
 * (which runs on every domain) can call this local backend.
 */
@RestController
@CrossOrigin(origins = "*")   // Allow extension to call from any page origin
public class AnalyzeController {

    private final DetectionService detectionService;

    // Spring injects DetectionService automatically
    public AnalyzeController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    // ── POST /analyze ─────────────────────────────────────────
    /**
     * Analyze a text string for manipulative dark patterns.
     *
     * Request body:  { "text": "Only 2 rooms left! Hurry!" }
     * Response body: { "flags": ["urgency","scarcity"], "confidence": 0.92, "score": 92, "clean": false }
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(@Valid @RequestBody AnalyzeRequest request) {
        AnalyzeResponse response = detectionService.analyze(request.getText());
        return ResponseEntity.ok(response);
    }

    // ── GET /health ───────────────────────────────────────────
    /**
     * Health check endpoint.
     * The extension popup pings this to show backend status.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Collections.singletonMap("status", "UP"));
    }
}
