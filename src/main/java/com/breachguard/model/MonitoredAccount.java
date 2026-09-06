package com.breachguard.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "monitored_accounts",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "account"}))
public class MonitoredAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String account;

    @Column(length = 120)
    private String label = "";

    @Column(name = "last_checked")
    private Instant lastChecked;

    @Column(name = "last_breach_count")
    private int lastBreachCount = 0;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public MonitoredAccount() {}

    public MonitoredAccount(User user, String account, String label) {
        this.user = user;
        this.account = account;
        this.label = label;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Instant getLastChecked() { return lastChecked; }
    public void setLastChecked(Instant lastChecked) { this.lastChecked = lastChecked; }

    public int getLastBreachCount() { return lastBreachCount; }
    public void setLastBreachCount(int lastBreachCount) { this.lastBreachCount = lastBreachCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
