package com.breachguard.dto;

public class AttackChain {
    private String name;
    private String description;

    public AttackChain() {}

    public AttackChain(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
