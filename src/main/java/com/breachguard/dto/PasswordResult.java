package com.breachguard.dto;

public class PasswordResult {
    private int timesSeen;
    private boolean pwned;
    private String prefix;
    private String error;

    public PasswordResult() {}

    public PasswordResult(int timesSeen, boolean pwned, String prefix, String error) {
        this.timesSeen = timesSeen;
        this.pwned = pwned;
        this.prefix = prefix;
        this.error = error;
    }

    public int getTimesSeen() { return timesSeen; }
    public void setTimesSeen(int timesSeen) { this.timesSeen = timesSeen; }
    public boolean isPwned() { return pwned; }
    public void setPwned(boolean pwned) { this.pwned = pwned; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
