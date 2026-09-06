package com.breachguard.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "honeypot_events")
public class HoneypotEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canary_id", nullable = false)
    private Canary canary;

    @Column(length = 45)
    private String ip = "";

    @Column(name = "user_agent", length = 500)
    private String userAgent = "";

    @Column(length = 120)
    private String country = "Unknown";

    @Column(length = 120)
    private String city = "Unknown";

    private Double lat;
    private Double lng;

    @Column(name = "is_anomaly")
    private boolean isAnomaly = false;

    @Column(name = "anomaly_reason", length = 255)
    private String anomalyReason = "";

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public HoneypotEvent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Canary getCanary() { return canary; }
    public void setCanary(Canary canary) { this.canary = canary; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public boolean isAnomaly() { return isAnomaly; }
    public void setAnomaly(boolean anomaly) { isAnomaly = anomaly; }

    public String getAnomalyReason() { return anomalyReason; }
    public void setAnomalyReason(String anomalyReason) { this.anomalyReason = anomalyReason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
