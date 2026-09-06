package com.breachguard.service;

import com.breachguard.dto.GeoInfo;
import com.breachguard.model.Canary;
import com.breachguard.model.HoneypotEvent;
import com.breachguard.model.Notification;
import com.breachguard.repository.HoneypotEventRepository;
import com.breachguard.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Honeypot / canary-token engine — exact port of Python honeypot.py.
 * Includes haversine distance, impossible-travel detection, and attack simulation.
 */
@Service
public class HoneypotService {

    private final HoneypotEventRepository eventRepo;
    private final NotificationRepository notifRepo;
    private final GeoIpService geoIpService;

    private static final List<Map<String, String>> DEMO_ATTACKERS = List.of(
            Map.of("ip", "1.1.1.1", "label", "Cloudflare (Sydney, AU)"),
            Map.of("ip", "8.8.8.8", "label", "Google (California, US)"),
            Map.of("ip", "208.67.222.222", "label", "OpenDNS (San Francisco, US)"),
            Map.of("ip", "9.9.9.9", "label", "Quad9 (Zürich, CH)")
    );

    public HoneypotService(HoneypotEventRepository eventRepo,
                           NotificationRepository notifRepo,
                           GeoIpService geoIpService) {
        this.eventRepo = eventRepo;
        this.notifRepo = notifRepo;
        this.geoIpService = geoIpService;
    }

    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371;
        double p1 = Math.toRadians(lat1), p2 = Math.toRadians(lat2);
        double dphi = Math.toRadians(lat2 - lat1);
        double dlmb = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dphi / 2) * Math.sin(dphi / 2)
                + Math.cos(p1) * Math.cos(p2) * Math.sin(dlmb / 2) * Math.sin(dlmb / 2);
        return 2 * r * Math.asin(Math.sqrt(a));
    }

    public HoneypotEvent recordAttack(Canary canary, String ip, String userAgent, Instant created) {
        if (created == null) created = Instant.now();

        GeoInfo geo = geoIpService.lookup(ip);
        boolean isAnomaly = false;
        String reason = "";

        // Check impossible travel
        var prevOpt = eventRepo.findFirstByCanaryIdOrderByCreatedAtDesc(canary.getId());
        if (prevOpt.isPresent()) {
            HoneypotEvent prev = prevOpt.get();
            if (prev.getLat() != null && prev.getLng() != null
                    && geo.getLat() != null && geo.getLng() != null) {
                double distance = haversineKm(prev.getLat(), prev.getLng(), geo.getLat(), geo.getLng());
                double hours = Duration.between(prev.getCreatedAt(), created).toMillis() / 3_600_000.0;
                if (hours <= 0) hours = 0.001;
                double speed = distance / hours;

                if (distance > 1000 && speed > 900) {
                    isAnomaly = true;
                    reason = String.format("Impossible travel: %,.0f km in %.1f h (~%,.0f km/h) after a login in %s, %s",
                            distance, hours, speed, prev.getCity(), prev.getCountry());
                } else if (distance > 500) {
                    isAnomaly = true;
                    reason = "New distant login location: " + geo.getCity() + ", " + geo.getCountry();
                }
            }
        }

        HoneypotEvent event = new HoneypotEvent();
        event.setCanary(canary);
        event.setIp(ip != null ? ip : "");
        event.setUserAgent(userAgent != null ? userAgent.substring(0, Math.min(userAgent.length(), 500)) : "");
        event.setCountry(geo.getCountry());
        event.setCity(geo.getCity());
        event.setLat(geo.getLat());
        event.setLng(geo.getLng());
        event.setAnomaly(isAnomaly);
        event.setAnomalyReason(reason);
        event.setCreatedAt(created);
        eventRepo.save(event);

        Notification note = new Notification(canary.getUser(), "honeypot",
                "🍯 Canary triggered: " + canary.getLabel(),
                "A decoy credential was used from " + geo.getCity() + ", " + geo.getCountry()
                        + " (" + (ip != null ? ip : "unknown IP") + "). "
                        + (!reason.isEmpty() ? "⚠️ " + reason : ""));
        notifRepo.save(note);

        return event;
    }

    public HoneypotEvent simulateAttack(Canary canary) {
        int idx = canary.getEvents().size() % DEMO_ATTACKERS.size();
        Map<String, String> pick = DEMO_ATTACKERS.get(idx);

        Instant created;
        if (!canary.getEvents().isEmpty()) {
            created = Instant.now();
        } else {
            int hoursBack = ThreadLocalRandom.current().nextInt(3, 11);
            created = Instant.now().minus(Duration.ofHours(hoursBack));
        }

        return recordAttack(canary, pick.get("ip"),
                "SIMULATION-ATTACK · " + pick.get("label") + " · credential-stuffing bot (curl/7.88)",
                created);
    }

    public String newTrapToken() {
        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
