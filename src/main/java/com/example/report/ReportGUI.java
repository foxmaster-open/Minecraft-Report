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
import java.util.ArrayList;

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
            if (online.equals(player)) continue;
            
            if (slot >= 54) break;
            
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(online);
            meta.setDisplayName(messageUtils.colorize("&e" + online.getName()));
            meta.setLore(Arrays.asList(
                    messageUtils.colorize("&7左键举报该玩家")
            ));
            head.setItemMeta(meta);
            
            gui.setItem(slot, head);
            slot++;
        }
        
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
        
        // 存储目标玩家名称到元数据
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
     * 打开举报列表GUI（支持分页和左右键操作）
     */
    public void openReportList(Player player, int page) {
        if (!player.hasPermission("report.list")) {
            messageUtils.sendMessage(player, "no-permission");
            return;
        }
        
        Collection<ReportData> allReports = plugin.getReportManager().getAllReports();
        List<ReportData> reports = new ArrayList<>(allReports);
        
        if (reports.isEmpty()) {
            messageUtils.sendMessage(player, "report-list-empty");
            return;
        }
        
        // 分页设置
        int itemsPerPage = 45;  // 每页显示45个
        int totalPages = (int) Math.ceil((double) reports.size() / itemsPerPage);
        if (page > totalPages) page = totalPages;
        if (page < 1) page = 1;
        
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, reports.size());
        
        // GUI大小：45个物品 + 9个控制按钮 = 54
        Inventory gui = Bukkit.createInventory(null, 54, 
                messageUtils.colorize("&c举报列表 - 第 " + page + "/" + totalPages + " 页"));
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            ReportData report = reports.get(i);
            
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
                    messageUtils.colorize("&e左键 &7- 标记为已处理"),
                    messageUtils.colorize("&c右键 &7- 删除举报")
            ));
            item.setItemMeta(meta);
            
            gui.setItem(slot, item);
            slot++;
        }
        
        // 添加控制按钮
        // 上一页按钮 (45号槽位)
        if (page > 1) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            prevMeta.setDisplayName(messageUtils.colorize("&a◀ 上一页"));
            prevPage.setItemMeta(prevMeta);
            gui.setItem(45, prevPage);
        }
        
        // 下一页按钮 (53号槽位)
        if (page < totalPages) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            nextMeta.setDisplayName(messageUtils.colorize("&a下一页 ▶"));
            nextPage.setItemMeta(nextMeta);
            gui.setItem(53, nextPage);
        }
        
        // 刷新按钮 (49号槽位)
        ItemStack refresh = new ItemStack(Material.SPECTRAL_ARROW);
        ItemMeta refreshMeta = refresh.getItemMeta();
        refreshMeta.setDisplayName(messageUtils.colorize("&a刷新"));
        refresh.setItemMeta(refreshMeta);
        gui.setItem(49, refresh);
        
        // 关闭按钮 (50号槽位)
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(messageUtils.colorize("&c关闭"));
        close.setItemMeta(closeMeta);
        gui.setItem(50, close);
        
        player.openInventory(gui);
    }
}
