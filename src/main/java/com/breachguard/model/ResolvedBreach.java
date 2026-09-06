package com.breachguard.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resolved_breaches",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "account", "breach_name"}))
public class ResolvedBreach {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String account;

    @Column(name = "breach_name", nullable = false, length = 255)
    private String breachName;

    @Column(name = "resolved_at")
    private Instant resolvedAt = Instant.now();

    public ResolvedBreach() {}

    public ResolvedBreach(User user, String account, String breachName) {
        this.user = user;
        this.account = account;
        this.breachName = breachName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }

    public String getBreachName() { return breachName; }
    public void setBreachName(String breachName) { this.breachName = breachName; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
