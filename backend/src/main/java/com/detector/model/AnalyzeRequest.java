package com.detector.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /analyze.
 * The extension sends the raw text from a DOM element here.
 */
public class AnalyzeRequest {

    @NotBlank(message = "text must not be blank")
    @Size(max = 5000, message = "text must be under 5000 characters")
    private String text;

    // ── Constructors ──────────────────────────────────────────
    public AnalyzeRequest() {}

    public AnalyzeRequest(String text) {
        this.text = text;
    }

    // ── Getters / Setters ─────────────────────────────────────
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
