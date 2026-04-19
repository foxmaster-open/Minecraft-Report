package com.example.report;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ReportGUI {
    
    private final ReportPlugin plugin;
    private final MessageUtils messageUtils;
    
    public ReportGUI(ReportPlugin plugin) {
        this.plugin = plugin;
        this.messageUtils = plugin.getMessageUtils();
    }
    
    /**
     * 打开玩家选择GUI
     */
    public void openPlayerSelector(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, messageUtils.colorize(plugin.getMessage("gui-title")));
        
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue; // 不能举报自己
            
            if (slot >= 54) break;
            
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(online);
            meta.setDisplayName(messageUtils.colorize("&e" + online.getName()));
            meta.setLore(Arrays.asList(
                    messageUtils.colorize("&7点击举报该玩家")
            ));
            head.setItemMeta(meta);
            
            gui.setItem(slot, head);
            slot++;
        }
        
        // 如果没有可举报的玩家
        if (slot == 0) {
            ItemStack empty = new ItemStack(Material.BARRIER);
            ItemMeta meta = empty.getItemMeta();
            meta.setDisplayName(messageUtils.colorize("&c没有可举报的玩家"));
            empty.setItemMeta(meta);
            gui.setItem(22, empty);
        }
        
        player.openInventory(gui);
    }
    
    /**
     * 打开原因选择GUI
     */
    public void openReasonSelector(Player player, String targetName) {
        Inventory gui = Bukkit.createInventory(null, 27, 
                messageUtils.colorize(plugin.getMessage("gui-reason-title")));
        
        List<String> reasons = plugin.getReportReasons();
        int slot = 0;
        for (String reason : reasons) {
            if (slot >= 27) break;
            
            ItemStack item = createReasonItem(reason);
            gui.setItem(slot, item);
            slot++;
        }
        
        // 存储目标玩家名称到元数据（用于回调）
        player.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "report_target"),
                org.bukkit.persistence.PersistentDataType.STRING, targetName);
        
        player.openInventory(gui);
    }
    
    /**
     * 创建原因物品
     */
    private ItemStack createReasonItem(String reason) {
        ItemStack item;
        String displayName;
        
        if (reason.contains("作弊")) {
            item = new ItemStack(Material.DIAMOND_SWORD);
            displayName = reason;
        } else if (reason.contains("骚扰")) {
            item = new ItemStack(Material.TOTEM_OF_UNDYING);
            displayName = reason;
        } else if (reason.contains("刷屏") || reason.contains("广告")) {
            item = new ItemStack(Material.BOOK);
            displayName = reason;
        } else if (reason.contains("破坏")) {
            item = new ItemStack(Material.TNT);
            displayName = reason;
        } else if (reason.contains("偷窃") || reason.contains("抢夺")) {
            item = new ItemStack(Material.CHEST);
            displayName = reason;
        } else if (reason.contains("辱骂")) {
            item = new ItemStack(Material.PAPER);
            displayName = reason;
        } else if (reason.contains("BUG")) {
            item = new ItemStack(Material.REDSTONE);
            displayName = reason;
        } else {
            item = new ItemStack(Material.BOOKSHELF);
            displayName = reason;
        }
        
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(messageUtils.colorize(displayName));
        meta.setLore(Arrays.asList(
                messageUtils.colorize("&7点击选择此原因"),
                messageUtils.colorize("&8将以此原因举报该玩家")
        ));
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * 打开举报列表GUI（仅OP）
     */
    public void openReportList(Player player) {
        if (!player.hasPermission("report.list")) {
            messageUtils.sendMessage(player, "no-permission");
            return;
        }
        
        Collection<ReportData> reports = plugin.getReportManager().getAllReports();
        
        if (reports.isEmpty()) {
            messageUtils.sendMessage(player, "report-list-empty");
            return;
        }
        
        // 计算需要的格子大小
        int size = ((reports.size() - 1) / 9 + 1) * 9;
        if (size < 9) size = 9;
        if (size > 54) size = 54;
        
        Inventory gui = Bukkit.createInventory(null, size, 
                messageUtils.colorize("&c举报列表 - 点击删除"));
        
        int slot = 0;
        for (ReportData report : reports) {
            if (slot >= size) break;
            
            ItemStack item;
            if (report.isHandled()) {
                item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            } else {
                item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            }
            
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(messageUtils.colorize("&e#" + report.getId() + " &7| &c" + report.getTargetName()));
            meta.setLore(Arrays.asList(
                    messageUtils.colorize("&7举报人: &f" + report.getReporterName()),
                    messageUtils.colorize("&7原因: &f" + report.getReason()),
                    messageUtils.colorize("&7时间: &f" + report.getFormattedTime()),
                    messageUtils.colorize("&7状态: " + (report.isHandled() ? "&a已处理" : "&c未处理")),
                    "",
                    messageUtils.colorize("&c点击删除此举报")
            ));
            item.setItemMeta(meta);
            
            gui.setItem(slot, item);
            slot++;
        }
        
        player.openInventory(gui);
    }
}
