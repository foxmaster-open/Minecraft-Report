package com.example.report;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

public class GUIListener implements Listener {
    
    private final ReportPlugin plugin;
    private final MessageUtils messageUtils;
    private final ReportGUI reportGUI;
    
    public GUIListener(ReportPlugin plugin) {
        this.plugin = plugin;
        this.messageUtils = plugin.getMessageUtils();
        this.reportGUI = new ReportGUI(plugin);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // 玩家选择GUI
        if (title.equals(messageUtils.colorize(plugin.getMessage("gui-title")))) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;
            if (!clicked.hasItemMeta()) return;
            
            ItemMeta meta = clicked.getItemMeta();
            if (!(meta instanceof SkullMeta)) return;
            
            SkullMeta skullMeta = (SkullMeta) meta;
            // 兼容性修复：使用 getOwner() 替代 hasOwningPlayer()
            if (skullMeta.getOwner() == null) return;
            String targetName = skullMeta.getOwner();
            if (targetName == null) return;
            
            // 检查不能举报自己
            if (targetName.equals(player.getName())) {
                messageUtils.sendMessage(player, "cannot-report-self");
                player.closeInventory();
                return;
            }
            
            // 打开原因选择GUI
            reportGUI.openReasonSelector(player, targetName);
        }
        
        // 原因选择GUI
        else if (title.equals(messageUtils.colorize(plugin.getMessage("gui-reason-title")))) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;
            if (!clicked.hasItemMeta()) return;
            
            String reason = clicked.getItemMeta().getDisplayName();
            reason = org.bukkit.ChatColor.stripColor(reason);
            
            // 获取目标玩家名称
            String targetName = player.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "report_target"),
                    PersistentDataType.STRING);
            
            if (targetName == null) {
                player.closeInventory();
                return;
            }
            
            // 清除存储的数据
            player.getPersistentDataContainer().remove(new NamespacedKey(plugin, "report_target"));
            
            // 处理举报
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                messageUtils.sendMessage(player, "player-not-found");
                player.closeInventory();
                return;
            }
            
            // 检查冷却
            if (plugin.getReportManager().isInCooldown(player.getUniqueId())) {
                int remaining = plugin.getReportManager().getRemainingCooldown(player.getUniqueId());
                messageUtils.sendMessage(player, "report-cooldown", "time", String.valueOf(remaining));
                player.closeInventory();
                return;
            }
            
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
                    
                    // 控制台也显示
                    Bukkit.getConsoleSender().sendMessage(messageUtils.colorize(msg));
                }
            } else {
                messageUtils.sendMessage(player, "report-cooldown", "time", 
                        String.valueOf(plugin.getCooldownTime()));
            }
            
            player.closeInventory();
        }
        
        // 举报列表GUI
        else if (title.equals(messageUtils.colorize("&c举报列表 - 点击删除"))) {
            event.setCancelled(true);
            
            if (!player.hasPermission("report.list")) {
                messageUtils.sendMessage(player, "no-permission");
                player.closeInventory();
                return;
            }
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;
            if (!clicked.hasItemMeta()) return;
            
            String displayName = clicked.getItemMeta().getDisplayName();
            String idStr = org.bukkit.ChatColor.stripColor(displayName).split("#")[1].split(" ")[0];
            
            try {
                int id = Integer.parseInt(idStr);
                boolean deleted = plugin.getReportManager().deleteReport(id);
                
                if (deleted) {
                    messageUtils.sendMessage(player, "report-deleted", "id", String.valueOf(id));
                    // 刷新GUI
                    reportGUI.openReportList(player);
                } else {
                    messageUtils.sendMessage(player, "report-not-found", "id", String.valueOf(id));
                }
            } catch (Exception e) {
                // 不是有效的举报ID
            }
        }
    }
}
