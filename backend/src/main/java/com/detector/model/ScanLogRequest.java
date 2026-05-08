package com.detector.model;
import java.util.List;

public class ScanLogRequest {
    private String url, domain, pageTitle, browser, categoriesFound;
    private Integer totalPatternsFound, pageScore;
    private Long scanDurationMs;
    private List<PatternDetail> patterns;

    public static class PatternDetail {
        private String patternText, category, elementTag, regexUsed, description;
        private Integer score, weight;
        private Double confidence;
        public String getPatternText() { return patternText; } public void setPatternText(String t) { this.patternText = t; }
        public String getCategory() { return category; } public void setCategory(String c) { this.category = c; }
        public Integer getScore() { return score; } public void setScore(Integer s) { this.score = s; }
        public String getElementTag() { return elementTag; } public void setElementTag(String t) { this.elementTag = t; }
        public String getRegexUsed() { return regexUsed; } public void setRegexUsed(String r) { this.regexUsed = r; }
        public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
        public Integer getWeight() { return weight; } public void setWeight(Integer w) { this.weight = w; }
        public Double getConfidence() { return confidence; } public void setConfidence(Double c) { this.confidence = c; }
    }
    public String getUrl() { return url; } public void setUrl(String u) { this.url = u; }
    public String getDomain() { return domain; } public void setDomain(String d) { this.domain = d; }
    public String getPageTitle() { return pageTitle; } public void setPageTitle(String t) { this.pageTitle = t; }
    public Integer getTotalPatternsFound() { return totalPatternsFound; } public void setTotalPatternsFound(Integer t) { this.totalPatternsFound = t; }
    public Long getScanDurationMs() { return scanDurationMs; } public void setScanDurationMs(Long ms) { this.scanDurationMs = ms; }
    public String getBrowser() { return browser; } public void setBrowser(String b) { this.browser = b; }
    public Integer getPageScore() { return pageScore; } public void setPageScore(Integer s) { this.pageScore = s; }
    public String getCategoriesFound() { return categoriesFound; } public void setCategoriesFound(String c) { this.categoriesFound = c; }
    public List<PatternDetail> getPatterns() { return patterns; } public void setPatterns(List<PatternDetail> p) { this.patterns = p; }
}
