package com.breachguard.dto;

import java.util.List;
import java.util.Map;

/** A single breach record (normalized across providers). */
public class BreachInfo {
    private String name;
    private String domain;
    private String date;
    private String breachDate;
    private List<String> dataClasses;
    private String description;
    private String source;
    private double points;
    private boolean resolved;
    private Map<String, Object> remediation;

    public BreachInfo() {}

    public BreachInfo(String name, String domain, String date, String breachDate,
                      List<String> dataClasses, String description, String source) {
        this.name = name;
        this.domain = domain;
        this.date = date;
        this.breachDate = breachDate;
        this.dataClasses = dataClasses;
        this.description = description;
        this.source = source;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getBreachDate() { return breachDate; }
    public void setBreachDate(String breachDate) { this.breachDate = breachDate; }
    public List<String> getDataClasses() { return dataClasses; }
    public void setDataClasses(List<String> dataClasses) { this.dataClasses = dataClasses; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
    public Map<String, Object> getRemediation() { return remediation; }
    public void setRemediation(Map<String, Object> remediation) { this.remediation = remediation; }
}
