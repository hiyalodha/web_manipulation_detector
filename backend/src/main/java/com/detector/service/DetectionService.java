package com.detector.service;

import com.detector.model.AnalyzeResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * DetectionService — the brain of the system.
 *
 * Uses rule-based regex matching to classify text into three
 * dark pattern categories:
 *
 *   urgency  — time-based pressure ("act now", "hurry!", "limited time")
 *   scarcity — quantity-based pressure ("only 2 left", "almost gone")
 *   pressure — social proof manipulation ("selling fast", "100 watching")
 *
 * Each matched rule adds points to the manipulation score (0–100).
 */
@Service
public class DetectionService {

    // ─────────────────────────────────────────────────────────
    // Pattern definitions
    // Each entry: category → list of compiled regex patterns
    // All patterns are CASE-INSENSITIVE.
    // ─────────────────────────────────────────────────────────
    private static final Map<String, List<Pattern>> PATTERN_MAP;

    static {
        PATTERN_MAP = new LinkedHashMap<>(); // preserves insertion order

        // ── Urgency patterns ────────────────────────────────
        // Time-based manipulation: creates fear of missing out.
        PATTERN_MAP.put("urgency", List.of(
            Pattern.compile("hurry[\\s!?]",          Pattern.CASE_INSENSITIVE),
            Pattern.compile("act\\s+now",             Pattern.CASE_INSENSITIVE),
            Pattern.compile("limited\\s+time",        Pattern.CASE_INSENSITIVE),
            Pattern.compile("offer\\s+ends",          Pattern.CASE_INSENSITIVE),
            Pattern.compile("expires?\\s+soon",       Pattern.CASE_INSENSITIVE),
            Pattern.compile("don'?t\\s+miss",         Pattern.CASE_INSENSITIVE),
            Pattern.compile("last\\s+chance",         Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\d+\\s*(hours?|mins?|minutes?)\\s+(left|remaining|only)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ends?\\s+(today|tonight|midnight)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("flash\\s+sale",          Pattern.CASE_INSENSITIVE),
            Pattern.compile("today\\s+only",          Pattern.CASE_INSENSITIVE)
        ));

        // ── Scarcity patterns ───────────────────────────────
        // Quantity-based manipulation: makes item feel rare.
        PATTERN_MAP.put("scarcity", List.of(
            Pattern.compile("only\\s+\\d+\\s+left",         Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\d+\\s+left\\s+in\\s+stock",  Pattern.CASE_INSENSITIVE),
            Pattern.compile("limited\\s+stock",              Pattern.CASE_INSENSITIVE),
            Pattern.compile("almost\\s+gone",                Pattern.CASE_INSENSITIVE),
            Pattern.compile("(very\\s+)?few\\s+left",        Pattern.CASE_INSENSITIVE),
            Pattern.compile("low\\s+stock",                  Pattern.CASE_INSENSITIVE),
            Pattern.compile("selling\\s+out\\s+fast",        Pattern.CASE_INSENSITIVE),
            Pattern.compile("only\\s+\\d+\\s+remaining",     Pattern.CASE_INSENSITIVE),
            Pattern.compile("limited\\s+(availability|supply|edition)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\d+\\s+(rooms?|seats?|tickets?)\\s+left", Pattern.CASE_INSENSITIVE)
        ));

        // ── Pressure patterns ────────────────────────────────
        // Social proof / bandwagon manipulation.
        PATTERN_MAP.put("pressure", List.of(
            Pattern.compile("selling\\s+fast",                Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\d+\\s+people\\s+(are\\s+)?watching", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\d+\\s+people\\s+(are\\s+)?viewing",  Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\d+\\s+in\\s+(your\\s+)?cart",        Pattern.CASE_INSENSITIVE),
            Pattern.compile("most\\s+popular",                       Pattern.CASE_INSENSITIVE),
            Pattern.compile("best\\s+seller",                        Pattern.CASE_INSENSITIVE),
            Pattern.compile("trending\\s+(now|today)",               Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\d+\\s+people\\s+bought",              Pattern.CASE_INSENSITIVE),
            Pattern.compile("everyone('?s|\\s+is)\\s+(buying|loving)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("recommended\\s+for\\s+you",             Pattern.CASE_INSENSITIVE)
        ));
    }

    // ── Score weights per matched pattern ────────────────────
    // Urgency is weighted higher as it's the most aggressive tactic.
    private static final Map<String, Integer> SCORE_WEIGHTS = Map.of(
        "urgency",  35,
        "scarcity", 30,
        "pressure", 25
    );

    // ─────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────

    /**
     * Analyze a piece of text for dark patterns.
     *
     * @param text raw text from a webpage DOM element
     * @return AnalyzeResponse with flags, score, and confidence
     */
    public AnalyzeResponse analyze(String text) {
        if (text == null || text.isBlank()) {
            return new AnalyzeResponse(Collections.emptyList(), 0);
        }

        List<String> flags = new ArrayList<>();
        int totalScore = 0;

        // Test each category's patterns against the input
        for (Map.Entry<String, List<Pattern>> entry : PATTERN_MAP.entrySet()) {
            String category = entry.getKey();
            List<Pattern> patterns = entry.getValue();

            boolean categoryMatched = false;
            int categoryScore = 0;

            for (Pattern pattern : patterns) {
                if (pattern.matcher(text).find()) {
                    categoryMatched = true;
                    categoryScore += SCORE_WEIGHTS.getOrDefault(category, 25);
                }
            }

            if (categoryMatched) {
                flags.add(category);
                totalScore += categoryScore;
            }
        }

        return new AnalyzeResponse(flags, totalScore);
    }
}
