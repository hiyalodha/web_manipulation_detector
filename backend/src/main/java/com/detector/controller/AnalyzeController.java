package com.detector.controller;

import com.detector.model.AnalyzeRequest;
import com.detector.model.AnalyzeResponse;
import com.detector.service.DetectionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") 
public class AnalyzeController {

    private final DetectionService detectionService;

    // Spring injects DetectionService automatically
    public AnalyzeController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

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
