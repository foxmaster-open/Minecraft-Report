package com.example.report;

import java.time.LocalDateTime;
import java.util.UUID;

public class Report {
    
    private final int id;
    private final UUID reporter;
    private final UUID reported;
    private final String reason;
    private final LocalDateTime timestamp;
    
    public Report(int id, UUID reporter, UUID reported, String reason, LocalDateTime timestamp) {
        this.id = id;
        this.reporter = reporter;
        this.reported = reported;
        this.reason = reason;
        this.timestamp = timestamp;
    }
    
    public int getId() {
        return id;
    }
    
    public UUID getReporter() {
        return reporter;
    }
    
    public UUID getReported() {
        return reported;
    }
    
    public String getReason() {
        return reason;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
