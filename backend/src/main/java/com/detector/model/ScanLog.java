package com.detector.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "scan_log")
public class ScanLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 2048) private String url;
    @Column(length = 255) private String domain;
    @Column(length = 512) private String pageTitle;
    @Column(nullable = false) private LocalDateTime scannedAt;
    @Column(nullable = false) private Integer totalPatternsFound;
    @Column private Long scanDurationMs;
    @Column(length = 100) private String browser;
    @Column(length = 50) private String status;
    @Column private Integer pageScore;
    @Column(length = 255) private String categoriesFound;
    @OneToMany(mappedBy = "scanLog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetectedPattern> detectedPatterns;

    public ScanLog() {}
    public ScanLog(String url, String domain, String pageTitle, LocalDateTime scannedAt,
                   Integer totalPatternsFound, Long scanDurationMs, String browser,
                   String status, Integer pageScore, String categoriesFound) {
        this.url = url; this.domain = domain; this.pageTitle = pageTitle;
        this.scannedAt = scannedAt; this.totalPatternsFound = totalPatternsFound;
        this.scanDurationMs = scanDurationMs; this.browser = browser;
        this.status = status; this.pageScore = pageScore; this.categoriesFound = categoriesFound;
    }
    public Long getId() { return id; }
    public String getUrl() { return url; } public void setUrl(String u) { this.url = u; }
    public String getDomain() { return domain; } public void setDomain(String d) { this.domain = d; }
    public String getPageTitle() { return pageTitle; } public void setPageTitle(String t) { this.pageTitle = t; }
    public LocalDateTime getScannedAt() { return scannedAt; } public void setScannedAt(LocalDateTime t) { this.scannedAt = t; }
    public Integer getTotalPatternsFound() { return totalPatternsFound; } public void setTotalPatternsFound(Integer t) { this.totalPatternsFound = t; }
    public Long getScanDurationMs() { return scanDurationMs; } public void setScanDurationMs(Long ms) { this.scanDurationMs = ms; }
    public String getBrowser() { return browser; } public void setBrowser(String b) { this.browser = b; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public Integer getPageScore() { return pageScore; } public void setPageScore(Integer s) { this.pageScore = s; }
    public String getCategoriesFound() { return categoriesFound; } public void setCategoriesFound(String c) { this.categoriesFound = c; }
    public List<DetectedPattern> getDetectedPatterns() { return detectedPatterns; }
}
