package com.detector.service;

import com.detector.model.AnalyzeResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class DetectionService {
 private static final Map<String, List<Pattern>> PATTERN_MAP;

    static {
        PATTERN_MAP = new LinkedHashMap<>(); 
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
    private static final Map<String, Integer> SCORE_WEIGHTS = Map.of(
        "urgency",  35,
        "scarcity", 30,
        "pressure", 25
    );

    public AnalyzeResponse analyze(String text) {
        if (text == null || text.isBlank()) {
            return new AnalyzeResponse(Collections.emptyList(), 0);
        }

        List<String> flags = new ArrayList<>();
        int totalScore = 0;

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
