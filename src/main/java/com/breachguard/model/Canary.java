package com.breachguard.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "canaries")
public class Canary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(name = "decoy_email", nullable = false, length = 255)
    private String decoyEmail;

    @Column(name = "decoy_password", nullable = false, length = 255)
    private String decoyPassword;

    @Column(name = "decoy_note", length = 255)
    private String decoyNote = "";

    @Column(name = "trap_token", unique = true, nullable = false, length = 32)
    private String trapToken;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "canary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<HoneypotEvent> events = new ArrayList<>();

    public Canary() {}

    public Canary(User user, String label, String decoyEmail, String decoyPassword,
                  String decoyNote, String trapToken) {
        this.user = user;
        this.label = label;
        this.decoyEmail = decoyEmail;
        this.decoyPassword = decoyPassword;
        this.decoyNote = decoyNote;
        this.trapToken = trapToken;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getDecoyEmail() { return decoyEmail; }
    public void setDecoyEmail(String decoyEmail) { this.decoyEmail = decoyEmail; }

    public String getDecoyPassword() { return decoyPassword; }
    public void setDecoyPassword(String decoyPassword) { this.decoyPassword = decoyPassword; }

    public String getDecoyNote() { return decoyNote; }
    public void setDecoyNote(String decoyNote) { this.decoyNote = decoyNote; }

    public String getTrapToken() { return trapToken; }
    public void setTrapToken(String trapToken) { this.trapToken = trapToken; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<HoneypotEvent> getEvents() { return events; }
    public void setEvents(List<HoneypotEvent> events) { this.events = events; }
}
