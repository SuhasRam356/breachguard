package com.breachguard.service;

import com.breachguard.dto.BreachInfo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Personal risk-scoring engine — exact port of Python risk_score.py.
 */
@Service
public class RiskScoreService {

    private static final List<Map.Entry<Pattern, Integer>> CLASS_WEIGHTS = List.of(
            Map.entry(Pattern.compile("password", Pattern.CASE_INSENSITIVE), 40),
            Map.entry(Pattern.compile("credit|card|bank|payment", Pattern.CASE_INSENSITIVE), 25),
            Map.entry(Pattern.compile("security question", Pattern.CASE_INSENSITIVE), 20),
            Map.entry(Pattern.compile("government|ssn|national", Pattern.CASE_INSENSITIVE), 20),
            Map.entry(Pattern.compile("phone", Pattern.CASE_INSENSITIVE), 15),
            Map.entry(Pattern.compile("address|geographic|location", Pattern.CASE_INSENSITIVE), 12),
            Map.entry(Pattern.compile("birth|dob", Pattern.CASE_INSENSITIVE), 10),
            Map.entry(Pattern.compile("social|chat|browsing|message", Pattern.CASE_INSENSITIVE), 10),
            Map.entry(Pattern.compile("ip", Pattern.CASE_INSENSITIVE), 8),
            Map.entry(Pattern.compile("username", Pattern.CASE_INSENSITIVE), 8),
            Map.entry(Pattern.compile("name", Pattern.CASE_INSENSITIVE), 6),
            Map.entry(Pattern.compile("email", Pattern.CASE_INSENSITIVE), 5),
            Map.entry(Pattern.compile("gender", Pattern.CASE_INSENSITIVE), 4),
            Map.entry(Pattern.compile("employer|company", Pattern.CASE_INSENSITIVE), 4),
            Map.entry(Pattern.compile("ethnic", Pattern.CASE_INSENSITIVE), 10)
    );

    public static final Map<String, String> LEVEL_LABELS = Map.of(
            "critical", "Critical — act now",
            "high", "High risk",
            "medium", "Medium risk",
            "low", "Low risk"
    );

    public static final Map<String, String> LEVEL_COLORS = Map.of(
            "critical", "#ff4d4f",
            "high", "#ff7a45",
            "medium", "#ffc53d",
            "low", "#52c41a"
    );

    /**
     * Score breaches and return [score, level, scored-breaches-list].
     */
    public Object[] scoreBreaches(List<BreachInfo> breaches) {
        LocalDate today = LocalDate.now();
        double total = 0.0;
        List<BreachInfo> perBreach = new ArrayList<>();

        for (BreachInfo b : breaches) {
            int points = 0;
            if (b.getDataClasses() != null) {
                for (String dc : b.getDataClasses()) {
                    points += classWeight(dc);
                }
            }
            points = Math.min(points, 70);

            double multiplier = 1.0;
            LocalDate d = parseDate(b);
            if (d != null) {
                long days = Period.between(d, today).getDays()
                        + Period.between(d, today).getMonths() * 30L
                        + Period.between(d, today).getYears() * 365L;
                double years = days / 365.25;
                if (years <= 1) multiplier = 1.5;
                else if (years <= 2) multiplier = 1.3;
                else if (years <= 5) multiplier = 1.15;
            }

            double weighted = Math.round(points * multiplier * 10.0) / 10.0;
            total += weighted;

            BreachInfo scored = new BreachInfo(b.getName(), b.getDomain(), b.getDate(),
                    b.getBreachDate(), b.getDataClasses(), b.getDescription(), b.getSource());
            scored.setPoints(weighted);
            perBreach.add(scored);
        }

        total += Math.max(0, breaches.size() - 1) * 8;
        int score = (int) Math.min(Math.round(total), 100);

        String level;
        if (score >= 70) level = "critical";
        else if (score >= 45) level = "high";
        else if (score >= 20) level = "medium";
        else level = "low";

        perBreach.sort((a, b2) -> Double.compare(b2.getPoints(), a.getPoints()));
        return new Object[]{score, level, perBreach};
    }

    private int classWeight(String label) {
        for (var entry : CLASS_WEIGHTS) {
            if (entry.getKey().matcher(label).find()) {
                return entry.getValue();
            }
        }
        return 4;
    }

    private LocalDate parseDate(BreachInfo breach) {
        for (String val : new String[]{breach.getBreachDate(), breach.getDate()}) {
            if (val == null || val.isEmpty()) continue;
            for (String fmt : new String[]{"yyyy-MM-dd", "yyyy-MM"}) {
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(fmt);
                    if (fmt.equals("yyyy-MM")) {
                        return LocalDate.parse(val + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }
                    return LocalDate.parse(val, dtf);
                } catch (DateTimeParseException ignored) {}
            }
        }
        return null;
    }
}
