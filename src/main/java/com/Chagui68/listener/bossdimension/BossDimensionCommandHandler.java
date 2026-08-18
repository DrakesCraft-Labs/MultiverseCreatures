package com.Chagui68.listener.bossdimension;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import com.Chagui68.MultiverseCreatures;

public class BossDimensionCommandHandler implements Listener {

    private final MultiverseCreatures plugin;

    public BossDimensionCommandHandler(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    private boolean isInBossWorld(Player player) {
        com.Chagui68.ritual.BossDimensionManager dimensionManager = plugin.getBossDimensionManager();
        return dimensionManager != null
                && dimensionManager.getBossWorld() != null
                && player.getWorld().equals(dimensionManager.getBossWorld());
    }

    private boolean isAllowedCommand(String command) {
        return command.startsWith("/say") ||
                command.startsWith("/me") ||
                command.startsWith("/help") ||
                command.startsWith("/?") ||
                command.startsWith("/dimtp");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("msc.admin.bypass")) {
            return;
        }

        if (!isInBossWorld(player)) {
            return;
        }

        com.Chagui68.ritual.BossDimensionManager dimensionManager = plugin.getBossDimensionManager();
        if (dimensionManager == null || dimensionManager.getBossWorld() == null) return;
        com.Chagui68.entities.boss.ArmorStandBoss boss = plugin.getArmorStandBoss();
        if (boss == null || !boss.isBossActiveInWorld(dimensionManager.getBossWorld())) {
            return;
        }

        String command = event.getMessage().toLowerCase();

        if (isAllowedCommand(command)) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You cannot use commands while the boss is active.");
    }
}