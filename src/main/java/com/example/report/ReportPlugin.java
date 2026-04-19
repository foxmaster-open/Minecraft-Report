package com.example.report;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ReportPlugin extends JavaPlugin {
    
    private static ReportPlugin instance;
    private ReportManager reportManager;
    private MessageUtils messageUtils;
    private FileConfiguration config;
    private List<String> reportReasons;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // 保存默认配置
        saveDefaultConfig();
        reloadConfig();
        this.config = getConfig();
        
        // 加载举报原因
        loadReportReasons();
        
        // 初始化管理器
        this.reportManager = new ReportManager(this);
        this.messageUtils = new MessageUtils(this);
        
        // 注册命令执行器
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("reportlist").setExecutor(new ReportListCommand(this));
        
        // 注册GUI监听器
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        
        getLogger().info("Report举报插件 v1.0.0 已启用！");
        getLogger().info("已加载 " + reportReasons.size() + " 个举报原因");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Report举报插件已禁用！");
    }
    
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.config = getConfig();
        loadReportReasons();
    }
    
    private void loadReportReasons() {
        this.reportReasons = config.getStringList("report-reasons");
        if (reportReasons.isEmpty()) {
            // 默认原因
            reportReasons.add("&c作弊/外挂");
            reportReasons.add("&6恶意骚扰");
            reportReasons.add("&e刷屏/广告");
            reportReasons.add("&2恶意破坏");
        }
    }
    
    public static ReportPlugin getInstance() {
        return instance;
    }
    
    public ReportManager getReportManager() {
        return reportManager;
    }
    
    public MessageUtils getMessageUtils() {
        return messageUtils;
    }
    
    public List<String> getReportReasons() {
        return reportReasons;
    }
    
    public String getMessage(String key) {
        return config.getString("messages." + key, "&c消息未配置: " + key);
    }
    
    public String getPrefix() {
        return config.getString("messages.prefix", "&8[&c举报&8] &r");
    }
    
    public boolean isCooldownEnabled() {
        return config.getBoolean("cooldown.enabled", true);
    }
    
    public int getCooldownTime() {
        return config.getInt("cooldown.time", 60);
    }
    
    public boolean isNotifyOps() {
        return config.getBoolean("notify-ops", true);
    }
    
    public boolean isSaveToFile() {
        return config.getBoolean("save-to-file", true);
    }
}
