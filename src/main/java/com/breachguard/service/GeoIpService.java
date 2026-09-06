package com.breachguard.service;

import com.breachguard.dto.GeoInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Free IP geolocation via ipwho.is — exact port of Python geoip.py. */
@Service
public class GeoIpService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8)).build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public GeoInfo lookup(String ip) {
        GeoInfo def = new GeoInfo();
        if (ip == null || ip.isBlank()) return def;
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://ipwho.is/" + ip))
                    .timeout(Duration.ofSeconds(8))
                    .GET().build();
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode d = MAPPER.readTree(resp.body());
            if (!d.path("success").asBoolean(true)) return def;
            return new GeoInfo(
                    d.path("country").asText("Unknown"),
                    d.path("city").asText("Unknown"),
                    d.has("latitude") ? d.get("latitude").asDouble() : null,
                    d.has("longitude") ? d.get("longitude").asDouble() : null
            );
        } catch (Exception e) {
            return def;
        }
    }
}
