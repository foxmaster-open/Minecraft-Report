package com.example.report;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.ClickType;
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
            if (skullMeta.getOwner() == null) return;
            String targetName = skullMeta.getOwner();
            if (targetName == null) return;
            
            if (targetName.equals(player.getName())) {
                messageUtils.sendMessage(player, "cannot-report-self");
                player.closeInventory();
                return;
            }
            
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
            
            String targetName = player.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, "report_target"),
                    PersistentDataType.STRING);
            
            if (targetName == null) {
                player.closeInventory();
                return;
            }
            
            player.getPersistentDataContainer().remove(new NamespacedKey(plugin, "report_target"));
            
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                messageUtils.sendMessage(player, "player-not-found");
                player.closeInventory();
                return;
            }
            
            if (plugin.getReportManager().isInCooldown(player.getUniqueId())) {
                int remaining = plugin.getReportManager().getRemainingCooldown(player.getUniqueId());
                messageUtils.sendMessage(player, "report-cooldown", "time", String.valueOf(remaining));
                player.closeInventory();
                return;
            }
            
            boolean success = plugin.getReportManager().createReport(player, target, reason);
            
            if (success) {
                messageUtils.sendMessage(player, "report-sent");
                
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
            
            player.closeInventory();
        }
        
        // 举报列表GUI
        else if (title.startsWith(messageUtils.colorize("&c举报列表 - 第"))) {
            event.setCancelled(true);
            
            if (!player.hasPermission("report.list")) {
                messageUtils.sendMessage(player, "no-permission");
                player.closeInventory();
                return;
            }
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType().isAir()) return;
            
            // 处理翻页按钮
            if (clicked.getType() == Material.ARROW) {
                String displayName = clicked.getItemMeta().getDisplayName();
                displayName = org.bukkit.ChatColor.stripColor(displayName);
                
                // 获取当前页码
                int currentPage = 1;
                try {
                    String pageStr = title.split("第")[1].split("/")[0].trim();
                    currentPage = Integer.parseInt(pageStr);
                } catch (Exception e) {}
                
                if (displayName.contains("上一页")) {
                    reportGUI.openReportList(player, currentPage - 1);
                } else if (displayName.contains("下一页")) {
                    reportGUI.openReportList(player, currentPage + 1);
                }
                return;
            }
            
            // 处理刷新按钮
            if (clicked.getType() == Material.SPECTRAL_ARROW) {
                int currentPage = 1;
                try {
                    String pageStr = title.split("第")[1].split("/")[0].trim();
                    currentPage = Integer.parseInt(pageStr);
                } catch (Exception e) {}
                reportGUI.openReportList(player, currentPage);
                return;
            }
            
            // 处理关闭按钮
            if (clicked.getType() == Material.BARRIER) {
                player.closeInventory();
                return;
            }
            
            // 处理举报物品
            if (!clicked.hasItemMeta()) return;
            if (!clicked.getItemMeta().hasDisplayName()) return;
            
            String displayName = clicked.getItemMeta().getDisplayName();
            String idStr = org.bukkit.ChatColor.stripColor(displayName).split("#")[1].split(" ")[0];
            
            try {
                int id = Integer.parseInt(idStr);
                ReportData report = null;
                for (ReportData r : plugin.getReportManager().getAllReports()) {
                    if (r.getId() == id) {
                        report = r;
                        break;
                    }
                }
                
                if (report == null) {
                    messageUtils.sendMessage(player, "report-not-found", "id", String.valueOf(id));
                    // 刷新当前页
                    int currentPage = 1;
                    try {
                        String pageStr = title.split("第")[1].split("/")[0].trim();
                        currentPage = Integer.parseInt(pageStr);
                    } catch (Exception e) {}
                    reportGUI.openReportList(player, currentPage);
                    return;
                }
                
                // 左键：标记为已处理
                if (event.getClick() == ClickType.LEFT) {
                    plugin.getReportManager().markAsHandled(id);
                    messageUtils.sendMessage(player, "report-handled", "id", String.valueOf(id));
                }
                // 右键：删除举报
                else if (event.getClick() == ClickType.RIGHT) {
                    plugin.getReportManager().deleteReport(id);
                    messageUtils.sendMessage(player, "report-deleted", "id", String.valueOf(id));
                }
                
                // 刷新当前页
                int currentPage = 1;
                try {
                    String pageStr = title.split("第")[1].split("/")[0].trim();
                    currentPage = Integer.parseInt(pageStr);
                } catch (Exception e) {}
                reportGUI.openReportList(player, currentPage);
                
            } catch (Exception e) {
                // 不是有效的举报ID
            }
        }
    }
}
