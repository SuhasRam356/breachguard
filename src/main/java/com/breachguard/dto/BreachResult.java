package com.breachguard.dto;

import java.util.List;

/** Full result of an email breach check. */
public class BreachResult {
    private List<BreachInfo> breaches;
    private int count;
    private String provider;
    private String providerLabel;
    private boolean demo;
    private String error;
    private int score;
    private String level;
    private String levelLabel;
    private String levelColor;
    private List<String> genericAdvice;
    private String twofaDirectory;
    private double marketValue;
    private List<AttackChain> attackChains;
    private int resolvedCount;

    // --- Getters & Setters ---
    public List<BreachInfo> getBreaches() { return breaches; }
    public void setBreaches(List<BreachInfo> breaches) { this.breaches = breaches; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getProviderLabel() { return providerLabel; }
    public void setProviderLabel(String providerLabel) { this.providerLabel = providerLabel; }
    public boolean isDemo() { return demo; }
    public void setDemo(boolean demo) { this.demo = demo; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getLevelLabel() { return levelLabel; }
    public void setLevelLabel(String levelLabel) { this.levelLabel = levelLabel; }
    public String getLevelColor() { return levelColor; }
    public void setLevelColor(String levelColor) { this.levelColor = levelColor; }
    public List<String> getGenericAdvice() { return genericAdvice; }
    public void setGenericAdvice(List<String> genericAdvice) { this.genericAdvice = genericAdvice; }
    public String getTwofaDirectory() { return twofaDirectory; }
    public void setTwofaDirectory(String twofaDirectory) { this.twofaDirectory = twofaDirectory; }
    public double getMarketValue() { return marketValue; }
    public void setMarketValue(double marketValue) { this.marketValue = marketValue; }
    public List<AttackChain> getAttackChains() { return attackChains; }
    public void setAttackChains(List<AttackChain> attackChains) { this.attackChains = attackChains; }
    public int getResolvedCount() { return resolvedCount; }
    public void setResolvedCount(int resolvedCount) { this.resolvedCount = resolvedCount; }
}
