package com.detector.model;

import java.util.List;

public class AnalyzeResponse {
  private List<String> flags;
  private double confidence;
  private int score;
  private boolean clean;
  public AnalyzeResponse(List<String> flags, int score) {
        this.flags      = flags;
        this.score      = Math.min(score, 100);          // cap at 100
        this.confidence = Math.round(this.score) / 100.0; // 0.0 – 1.0
        this.clean      = flags.isEmpty();
    }   public List<String>  getFlags()      { return flags; }
    public double        getConfidence() { return confidence; }
    public int           getScore()      { return score; }
    public boolean       isClean()       { return clean; }
}
