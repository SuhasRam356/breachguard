package com.breachguard.service;

import com.breachguard.dto.BreachInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Breach-data providers — adapter pattern (LeakCheck / HIBP / demo).
 * Exact port of Python breach_providers.py.
 */
@Service
public class BreachProviderService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15)).build();
    private static final String UA = "BreachGuard-StudentProject/1.0";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${breachguard.hibp-api-key:}")
    private String hibpApiKey;

    @Value("${breachguard.leakcheck-enabled:true}")
    private boolean leakcheckEnabled;

    // Field normalization map (LeakCheck field -> friendly label)
    private static final Map<String, String> FIELD_MAP = Map.ofEntries(
            Map.entry("password", "Passwords"), Map.entry("email", "Email addresses"),
            Map.entry("username", "Usernames"), Map.entry("name", "Names"),
            Map.entry("first_name", "First names"), Map.entry("last_name", "Last names"),
            Map.entry("phone", "Phone numbers"), Map.entry("address", "Physical addresses"),
            Map.entry("city", "Physical addresses"), Map.entry("state", "Physical addresses"),
            Map.entry("zip", "Physical addresses"), Map.entry("country", "Physical addresses"),
            Map.entry("dob", "Dates of birth"), Map.entry("gender", "Genders"),
            Map.entry("ip", "IP addresses"), Map.entry("ssn", "Government IDs"),
            Map.entry("company_name", "Employers")
    );

    // ── Public API ──

    public Map<String, Object> checkEmail(String email) {
        email = email.trim().toLowerCase();

        // Demo emails → built-in sample data
        if (email.endsWith("@example.com") || email.endsWith("@example.org")) {
            return Map.of("breaches", demoBreaches(), "provider", "demo",
                    "demo", true, "error", "");
        }

        // 1) Try HIBP if key configured
        if (hibpApiKey != null && !hibpApiKey.isBlank()) {
            var result = fromHibp(email);
            if (result.get("error") == null || ((String) result.get("error")).isEmpty()) {
                return result;
            }
        }

        // 2) Fall back to LeakCheck
        if (leakcheckEnabled) {
            var result = fromLeakCheck(email);
            String err = (String) result.get("error");
            if (err == null || err.isEmpty()) {
                return result;
            }
            if ("leakcheck-rate-limit".equals(err) || "leakcheck-network".equals(err)) {
                return Map.of("breaches", demoBreaches(), "provider", "demo",
                        "demo", true, "error", err);
            }
        }

        // 3) Offline fallback
        return Map.of("breaches", demoBreaches(), "provider", "demo",
                "demo", true, "error", "offline");
    }

    public String providerLabel(String provider) {
        return switch (provider) {
            case "hibp" -> "Have I Been Pwned";
            case "leakcheck" -> "LeakCheck public breach database";
            case "demo" -> "Built-in demo data (sample breaches)";
            default -> provider;
        };
    }

    // ── LeakCheck (free public API) ──

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromLeakCheck(String email) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://leakcheck.io/api/public?check=" + email))
                    .header("User-Agent", UA)
                    .timeout(Duration.ofSeconds(15))
                    .GET().build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 429)
                return Map.of("breaches", List.of(), "provider", "leakcheck",
                        "demo", false, "error", "leakcheck-rate-limit");
            if (resp.statusCode() != 200)
                return Map.of("breaches", List.of(), "provider", "leakcheck",
                        "demo", false, "error", "leakcheck-http-" + resp.statusCode());

            JsonNode data = MAPPER.readTree(resp.body());
            List<BreachInfo> breaches = new ArrayList<>();

            if (data.path("success").asBoolean() && data.path("found").asInt() > 0) {
                List<String> fields = new ArrayList<>();
                if (data.has("fields")) {
                    data.get("fields").forEach(f -> fields.add(f.asText()));
                }
                List<String> normalizedClasses = normalizeClasses(fields);

                for (JsonNode src : data.path("sources")) {
                    String name = src.path("name").asText("Unknown breach");
                    String dateStr = src.path("date").asText("");
                    String breachDate = dateStr + (dateStr.length() == 7 ? "-01" : "");

                    breaches.add(new BreachInfo(name, domainFromName(name), dateStr,
                            breachDate, normalizedClasses,
                            "Exposed in the " + name + " data breach.", "leakcheck"));
                }
            }
            return Map.of("breaches", breaches, "provider", "leakcheck",
                    "demo", false, "error", "");
        } catch (Exception e) {
            return Map.of("breaches", List.of(), "provider", "leakcheck",
                    "demo", false, "error", "leakcheck-network");
        }
    }

    // ── HIBP (paid API key) ──

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromHibp(String email) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://haveibeenpwned.com/api/v3/breachedaccount/"
                            + email + "?truncateResponse=false"))
                    .header("User-Agent", UA)
                    .header("hibp-api-key", hibpApiKey)
                    .timeout(Duration.ofSeconds(15))
                    .GET().build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 404)
                return Map.of("breaches", List.of(), "provider", "hibp",
                        "demo", false, "error", "");
            if (resp.statusCode() != 200)
                return Map.of("breaches", List.of(), "provider", "hibp",
                        "demo", false, "error", "hibp-http-" + resp.statusCode());

            JsonNode data = MAPPER.readTree(resp.body());
            List<BreachInfo> breaches = new ArrayList<>();
            for (JsonNode b : data) {
                String name = b.path("Name").asText("Unknown");
                String domain = b.path("Domain").asText("").toLowerCase();
                if (domain.isEmpty()) domain = domainFromName(name);
                String breachDate = b.path("BreachDate").asText("");
                String dateShort = breachDate.length() >= 7 ? breachDate.substring(0, 7) : breachDate;

                List<String> dc = new ArrayList<>();
                b.path("DataClasses").forEach(c -> dc.add(c.asText()));
                if (dc.isEmpty()) dc.add("Email addresses");

                String desc = b.path("Description").asText("").replace("<p>", "").replace("</p>", "");

                breaches.add(new BreachInfo(name, domain, dateShort, breachDate, dc, desc, "hibp"));
            }
            return Map.of("breaches", breaches, "provider", "hibp",
                    "demo", false, "error", "");
        } catch (Exception e) {
            return Map.of("breaches", List.of(), "provider", "hibp",
                    "demo", false, "error", "hibp-network");
        }
    }

    // ── Demo data ──

    public List<BreachInfo> demoBreaches() {
        return List.of(
                new BreachInfo("DemoLinkedIn", "linkedin.com", "2021-06", "2021-06-22",
                        List.of("Email addresses", "Passwords", "Names", "Usernames", "Social media profiles"),
                        "DEMO DATA: 700M LinkedIn records circulated online. (Real breach — shown here as sample output.)", "demo"),
                new BreachInfo("DemoCanva", "canva.com", "2019-05", "2019-05-24",
                        List.of("Email addresses", "Names", "Usernames", "Geographic locations"),
                        "DEMO DATA: 137M Canva user records exposed. (Real breach — shown here as sample output.)", "demo"),
                new BreachInfo("DemoDropbox", "dropbox.com", "2012-07", "2012-07-01",
                        List.of("Email addresses", "Passwords"),
                        "DEMO DATA: 68M Dropbox credentials leaked in 2012 and traded publicly in 2016. (Real breach — sample output.)", "demo")
        );
    }

    // ── Helpers ──

    private String domainFromName(String name) {
        Matcher m = Pattern.compile("([a-z0-9-]+\\.[a-z]{2,})").matcher(name.toLowerCase());
        if (m.find()) return m.group(1);
        String[] parts = name.toLowerCase().split("\\s+");
        return parts[0] + ".com";
    }

    private List<String> normalizeClasses(List<String> rawFields) {
        List<String> out = new ArrayList<>();
        for (String f : rawFields) {
            String label = FIELD_MAP.get(f.toLowerCase().trim());
            if (label != null && !out.contains(label)) {
                out.add(label);
            }
        }
        if (out.isEmpty()) out.add("Email addresses");
        return out;
    }
}
