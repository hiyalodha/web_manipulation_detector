package com.detector.controller;
import com.detector.model.DetectedPattern;
import com.detector.model.ScanLog;
import com.detector.model.ScanLogRequest;
import com.detector.service.ScanLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class ScanLogController {
    private final ScanLogService scanLogService;
    public ScanLogController(ScanLogService scanLogService) { this.scanLogService = scanLogService; }

    @PostMapping("/scan-log")
    public ResponseEntity<ScanLog> saveScan(@RequestBody ScanLogRequest request) {
        return ResponseEntity.ok(scanLogService.saveScan(request));
    }
    @GetMapping("/history")
    public ResponseEntity<List<ScanLog>> getHistory() {
        return ResponseEntity.ok(scanLogService.getAllScans());
    }
    @GetMapping("/history/{domain}")
    public ResponseEntity<List<ScanLog>> getByDomain(@PathVariable String domain) {
        return ResponseEntity.ok(scanLogService.getScansByDomain(domain));
    }
    @GetMapping("/history/scan/{id}/patterns")
    public ResponseEntity<List<DetectedPattern>> getPatterns(@PathVariable Long id) {
        return ResponseEntity.ok(scanLogService.getPatternsForScan(id));
    }
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("topManipulativeDomains", scanLogService.getTopManipulativeDomains().stream().map(row -> {
            Map<String, Object> m = new HashMap<>(); m.put("domain", row[0]); m.put("avgScore", row[1]); m.put("scanCount", row[2]); return m;
        }).toList());
        stats.put("categoryBreakdown", scanLogService.getCategoryBreakdown().stream().map(row -> {
            Map<String, Object> m = new HashMap<>(); m.put("category", row[0]); m.put("total", row[1]); return m;
        }).toList());
        stats.put("mostCommonPatterns", scanLogService.getMostCommonPatterns().stream().limit(10).map(row -> {
            Map<String, Object> m = new HashMap<>(); m.put("text", row[0]); m.put("occurrences", row[1]); return m;
        }).toList());
        return ResponseEntity.ok(stats);
    }
}
