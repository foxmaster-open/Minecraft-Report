package com.example.report;

import java.util.UUID;

public class ReportData {
    
    private final int id;
    private final UUID reporterUUID;
    private final String reporterName;
    private final UUID targetUUID;
    private final String targetName;
    private final String reason;
    private final long timestamp;
    private boolean handled;
    
    public ReportData(int id, UUID reporterUUID, String reporterName, 
                      UUID targetUUID, String targetName, String reason, long timestamp) {
        this.id = id;
        this.reporterUUID = reporterUUID;
        this.reporterName = reporterName;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.reason = reason;
        this.timestamp = timestamp;
        this.handled = false;
    }
    
    public ReportData(int id, UUID reporterUUID, String reporterName, 
                      UUID targetUUID, String targetName, String reason, 
                      long timestamp, boolean handled) {
        this.id = id;
        this.reporterUUID = reporterUUID;
        this.reporterName = reporterName;
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        this.reason = reason;
        this.timestamp = timestamp;
        this.handled = handled;
    }
    
    public int getId() {
        return id;
    }
    
    public UUID getReporterUUID() {
        return reporterUUID;
    }
    
    public String getReporterName() {
        return reporterName;
    }
    
    public UUID getTargetUUID() {
        return targetUUID;
    }
    
    public String getTargetName() {
        return targetName;
    }
    
    public String getReason() {
        return reason;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public boolean isHandled() {
        return handled;
    }
    
    public void setHandled(boolean handled) {
        this.handled = handled;
    }
    
    public String getFormattedTime() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));
    }
}
