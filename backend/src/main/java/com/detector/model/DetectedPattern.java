package com.detector.model;
import jakarta.persistence.*;

@Entity
@Table(name = "detected_pattern")
public class DetectedPattern {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id", nullable = false)
    private ScanLog scanLog;
    @Column(nullable = false, length = 1000) private String patternText;
    @Column(nullable = false, length = 50) private String category;
    @Column(nullable = false) private Integer score;
    @Column(length = 50) private String elementTag;
    @Column(length = 500) private String regexUsed;
    @Column(length = 500) private String description;
    @Column private Integer weight;
    @Column private Double confidence;
    @Column(nullable = false) private Boolean isFalsePositive = false;

    public DetectedPattern() {}
    public DetectedPattern(ScanLog scanLog, String patternText, String category,
                           Integer score, String elementTag, String regexUsed,
                           String description, Integer weight, Double confidence) {
        this.scanLog = scanLog; this.patternText = patternText; this.category = category;
        this.score = score; this.elementTag = elementTag; this.regexUsed = regexUsed;
        this.description = description; this.weight = weight; this.confidence = confidence;
        this.isFalsePositive = false;
    }
    public Long getId() { return id; }
    public ScanLog getScanLog() { return scanLog; } public void setScanLog(ScanLog s) { this.scanLog = s; }
    public String getPatternText() { return patternText; } public void setPatternText(String t) { this.patternText = t; }
    public String getCategory() { return category; } public void setCategory(String c) { this.category = c; }
    public Integer getScore() { return score; } public void setScore(Integer s) { this.score = s; }
    public String getElementTag() { return elementTag; } public void setElementTag(String t) { this.elementTag = t; }
    public String getRegexUsed() { return regexUsed; } public void setRegexUsed(String r) { this.regexUsed = r; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
    public Integer getWeight() { return weight; } public void setWeight(Integer w) { this.weight = w; }
    public Double getConfidence() { return confidence; } public void setConfidence(Double c) { this.confidence = c; }
    public Boolean getIsFalsePositive() { return isFalsePositive; } public void setIsFalsePositive(Boolean fp) { this.isFalsePositive = fp; }
}
