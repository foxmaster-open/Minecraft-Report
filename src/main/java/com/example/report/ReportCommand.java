package com.example.report;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportCommand implements CommandExecutor {
    
    private final ReportPlugin plugin;
    private final MessageUtils messageUtils;
    private final ReportGUI reportGUI;
    
    public ReportCommand(ReportPlugin plugin) {
        this.plugin = plugin;
        this.messageUtils = plugin.getMessageUtils();
        this.reportGUI = new ReportGUI(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (!(sender instanceof Player)) {
            messageUtils.sendMessage(sender, "player-only");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("report.use")) {
            messageUtils.sendMessage(player, "no-permission");
            return true;
        }
        
        // 无参数：打开玩家选择GUI
        if (args.length == 0) {
            reportGUI.openPlayerSelector(player);
            return true;
        }
        
        // 获取目标玩家
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            messageUtils.sendMessage(player, "player-not-found");
            return true;
        }
        
        // 不能举报自己
        if (target.equals(player)) {
            messageUtils.sendMessage(player, "cannot-report-self");
            return true;
        }
        
        // 检查冷却
        if (plugin.getReportManager().isInCooldown(player.getUniqueId())) {
            int remaining = plugin.getReportManager().getRemainingCooldown(player.getUniqueId());
            messageUtils.sendMessage(player, "report-cooldown", "time", String.valueOf(remaining));
            return true;
        }
        
        // 有理由参数：直接举报
        if (args.length >= 2) {
            // 拼接理由（支持空格）
            StringBuilder reasonBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) reasonBuilder.append(" ");
                reasonBuilder.append(args[i]);
            }
            String reason = reasonBuilder.toString();
            
            // 创建举报
            boolean success = plugin.getReportManager().createReport(player, target, reason);
            
            if (success) {
                messageUtils.sendMessage(player, "report-sent");
                
                // 通知OP
                if (plugin.isNotifyOps()) {
                    String msg = plugin.getMessage("report-received")
                            .replace("{player}", player.getName())
                            .replace("{target}", target.getName())
                            .replace("{reason}", reason);
                    
                    for (Player op : Bukkit.getOnlinePlayers()) {
                        if (op.hasPermission("report.list")) {
                            messageUtils.sendRawMessage(op, msg);
                        }
                    }
                    Bukkit.getConsoleSender().sendMessage(messageUtils.colorize(msg));
                }
            }
            return true;
        }
        
        // 只有玩家名：打开原因选择GUI
        reportGUI.openReasonSelector(player, targetName);
        
        return true;
    }
}
