package com.example.report;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReportManager {
    
    private final ReportPlugin plugin;
    private final Map<Integer, ReportData> reports = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private int nextId = 1;
    private final File dataFile;
    
    public ReportManager(ReportPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "reports.yml");
        loadReports();
    }
    
    /**
     * 创建举报
     */
    public boolean createReport(Player reporter, Player target, String reason) {
        UUID reporterUUID = reporter.getUniqueId();
        
        if (isInCooldown(reporterUUID)) {
            return false;
        }
        
        ReportData report = new ReportData(nextId, reporterUUID, reporter.getName(),
                target.getUniqueId(), target.getName(), reason, System.currentTimeMillis());
        
        reports.put(nextId, report);
        setCooldown(reporterUUID);
        nextId++;
        
        saveReports();
        return true;
    }
    
    /**
     * 获取所有举报
     */
    public Collection<ReportData> getAllReports() {
        return reports.values();
    }
    
    /**
     * 获取未处理的举报
     */
    public Collection<ReportData> getUnhandledReports() {
        List<ReportData> unhandled = new ArrayList<>();
        for (ReportData report : reports.values()) {
            if (!report.isHandled()) {
                unhandled.add(report);
            }
        }
        return unhandled;
    }
    
    /**
     * 删除举报并通知举报人
     */
    public boolean deleteReport(int id) {
        ReportData removed = reports.remove(id);
        if (removed != null) {
            saveReports();
            // 通知举报人已被删除
            notifyReporter(removed, "deleted");
            return true;
        }
        return false;
    }
    
    /**
     * 标记为已处理并通知举报人
     */
    public void markAsHandled(int id) {
        ReportData report = reports.get(id);
        if (report != null) {
            report.setHandled(true);
            saveReports();
            // 通知举报人已处理
            notifyReporter(report, "handled");
        }
    }
    
    /**
     * 通知举报人
     */
    private void notifyReporter(ReportData report, String action) {
        if (!plugin.isNotifyReporter()) return;
        
        Player reporter = Bukkit.getPlayer(report.getReporterUUID());
        if (reporter == null || !reporter.isOnline()) return;
        
        String messageKey = "report-" + action;
        String message = plugin.getMessage(messageKey)
                .replace("{target}", report.getTargetName())
                .replace("{reason}", report.getReason())
                .replace("{id}", String.valueOf(report.getId()));
        
        plugin.getMessageUtils().sendMessage(reporter, messageKey, 
                "target", report.getTargetName(),
                "reason", report.getReason(),
                "id", String.valueOf(report.getId()));
    }
    
    /**
     * 检查冷却
     */
    public boolean isInCooldown(UUID playerUUID) {
        if (!plugin.isCooldownEnabled()) return false;
        
        Long cooldownEnd = cooldowns.get(playerUUID);
        if (cooldownEnd == null) return false;
        
        return System.currentTimeMillis() < cooldownEnd;
    }
    
    /**
     * 获取剩余冷却时间（秒）
     */
    public int getRemainingCooldown(UUID playerUUID) {
        if (!isInCooldown(playerUUID)) return 0;
        
        Long cooldownEnd = cooldowns.get(playerUUID);
        if (cooldownEnd == null) return 0;
        
        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
        return (int) Math.max(0, remaining);
    }
    
    /**
     * 设置冷却
     */
    private void setCooldown(UUID playerUUID) {
        int cooldownTime = plugin.getCooldownTime();
        long cooldownEnd = System.currentTimeMillis() + (cooldownTime * 1000L);
        cooldowns.put(playerUUID, cooldownEnd);
    }
    
    /**
     * 保存举报到文件
     */
    private void saveReports() {
        if (!plugin.isSaveToFile()) return;
        
        YamlConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<Integer, ReportData> entry : reports.entrySet()) {
            ReportData report = entry.getValue();
            String path = String.valueOf(entry.getKey());
            config.set(path + ".reporter-uuid", report.getReporterUUID().toString());
            config.set(path + ".reporter-name", report.getReporterName());
            config.set(path + ".target-uuid", report.getTargetUUID().toString());
            config.set(path + ".target-name", report.getTargetName());
            config.set(path + ".reason", report.getReason());
            config.set(path + ".timestamp", report.getTimestamp());
            config.set(path + ".handled", report.isHandled());
        }
        
        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存举报数据: " + e.getMessage());
        }
    }
    
    /**
     * 加载举报从文件
     */
    private void loadReports() {
        if (!dataFile.exists()) return;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        
        for (String key : config.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                UUID reporterUUID = UUID.fromString(config.getString(key + ".reporter-uuid"));
                String reporterName = config.getString(key + ".reporter-name");
                UUID targetUUID = UUID.fromString(config.getString(key + ".target-uuid"));
                String targetName = config.getString(key + ".target-name");
                String reason = config.getString(key + ".reason");
                long timestamp = config.getLong(key + ".timestamp");
                boolean handled = config.getBoolean(key + ".handled", false);
                
                ReportData report = new ReportData(id, reporterUUID, reporterName,
                        targetUUID, targetName, reason, timestamp, handled);
                reports.put(id, report);
                
                if (id >= nextId) {
                    nextId = id + 1;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("无法加载举报 " + key + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("已加载 " + reports.size() + " 条举报记录");
    }
}
