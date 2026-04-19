package com.example.report;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {
    
    private final ReportPlugin plugin;
    
    public MessageUtils(ReportPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void sendMessage(CommandSender sender, String key, String... replacements) {
        String message = plugin.getMessage(key);
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        
        sender.sendMessage(colorize(plugin.getPrefix() + message));
    }
    
    public void sendRawMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }
    
    public void sendMessageToPlayer(Player player, String key, String... replacements) {
        if (player != null && player.isOnline()) {
            sendMessage(player, key, replacements);
        }
    }
    
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
