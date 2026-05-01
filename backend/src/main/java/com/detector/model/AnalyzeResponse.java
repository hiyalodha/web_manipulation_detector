package com.detector.model;

import java.util.List;

/**
 * Response body for POST /analyze.
 *
 * Example:
 * {
 *   "flags":      ["urgency", "scarcity"],
 *   "confidence": 0.92,
 *   "score":      92,
 *   "clean":      false
 * }
 */
public class AnalyzeResponse {

    /** Categories of dark patterns detected (urgency / scarcity / pressure). */
    private List<String> flags;

    /**
     * Confidence as a 0.0 – 1.0 float, for compatibility with ML-style APIs.
     * Derived from score / 100.
     */
    private double confidence;

    /** Manipulation score 0 – 100. Higher = more manipulative. */
    private int score;

    /** True when no patterns were found. */
    private boolean clean;

    // ── Constructor ───────────────────────────────────────────
    public AnalyzeResponse(List<String> flags, int score) {
        this.flags      = flags;
        this.score      = Math.min(score, 100);          // cap at 100
        this.confidence = Math.round(this.score) / 100.0; // 0.0 – 1.0
        this.clean      = flags.isEmpty();
    }

    // ── Getters ───────────────────────────────────────────────
    public List<String>  getFlags()      { return flags; }
    public double        getConfidence() { return confidence; }
    public int           getScore()      { return score; }
    public boolean       isClean()       { return clean; }
}
