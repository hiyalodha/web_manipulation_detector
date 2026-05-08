package com.detector.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnalyzeRequest {

    @NotBlank(message = "text must not be blank")
    @Size(max = 5000, message = "text must be under 5000 characters")
    private String text;
  public AnalyzeRequest() {}

    public AnalyzeRequest(String text) {
        this.text = text;
    }
  public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
