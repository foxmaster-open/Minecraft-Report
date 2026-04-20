package com.example.report;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportListCommand implements CommandExecutor {
    
    private final ReportPlugin plugin;
    private final MessageUtils messageUtils;
    private final ReportGUI reportGUI;
    
    public ReportListCommand(ReportPlugin plugin) {
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
        
        if (!player.hasPermission("report.list")) {
            messageUtils.sendMessage(player, "no-permission");
            return true;
        }
        
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                // 不是数字，忽略
            }
        }
        
        // 打开举报列表GUI（支持分页）
        reportGUI.openReportList(player, page);
        
        return true;
    }
}
