package com.detector.service;
import com.detector.model.DetectedPattern;
import com.detector.model.ScanLog;
import com.detector.model.ScanLogRequest;
import com.detector.repository.DetectedPatternRepository;
import com.detector.repository.ScanLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScanLogService {
    private final ScanLogRepository scanLogRepo;
    private final DetectedPatternRepository patternRepo;

    public ScanLogService(ScanLogRepository scanLogRepo, DetectedPatternRepository patternRepo) {
        this.scanLogRepo = scanLogRepo; this.patternRepo = patternRepo;
    }

    @Transactional
    public ScanLog saveScan(ScanLogRequest request) {
        ScanLog log = new ScanLog(
            request.getUrl(), request.getDomain(), request.getPageTitle(), LocalDateTime.now(),
            request.getTotalPatternsFound() != null ? request.getTotalPatternsFound() : 0,
            request.getScanDurationMs(),
            request.getBrowser() != null ? request.getBrowser() : "Chrome",
            "COMPLETED",
            request.getPageScore() != null ? request.getPageScore() : 0,
            request.getCategoriesFound()
        );
        ScanLog savedLog = scanLogRepo.save(log);
        if (request.getPatterns() != null) {
            for (ScanLogRequest.PatternDetail p : request.getPatterns()) {
                patternRepo.save(new DetectedPattern(
                    savedLog, p.getPatternText(), p.getCategory(),
                    p.getScore() != null ? p.getScore() : 0,
                    p.getElementTag() != null ? p.getElementTag() : "unknown",
                    p.getRegexUsed(), p.getDescription(), p.getWeight(), p.getConfidence()
                ));
            }
        }
        return savedLog;
    }

    public List<ScanLog> getAllScans() { return scanLogRepo.findTop10ByOrderByScannedAtDesc(); }
    public List<ScanLog> getScansByDomain(String domain) { return scanLogRepo.findByDomainOrderByScannedAtDesc(domain); }
    public List<DetectedPattern> getPatternsForScan(Long id) { return patternRepo.findByScanLogId(id); }
    public List<Object[]> getTopManipulativeDomains() { return scanLogRepo.findTopManipulativeDomains(); }
    public List<Object[]> getCategoryBreakdown() { return patternRepo.countByCategory(); }
    public List<Object[]> getMostCommonPatterns() { return patternRepo.findMostCommonPatterns(); }
}
