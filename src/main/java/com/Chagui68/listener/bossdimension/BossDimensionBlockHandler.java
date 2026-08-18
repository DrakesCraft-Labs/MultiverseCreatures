package com.Chagui68.listener.bossdimension;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import com.Chagui68.MultiverseCreatures;

public class BossDimensionBlockHandler implements Listener {

    private final MultiverseCreatures plugin;

    public BossDimensionBlockHandler(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    private boolean isInBossWorld(Player player) {
        com.Chagui68.ritual.BossDimensionManager dimensionManager = plugin.getBossDimensionManager();
        return dimensionManager != null
                && dimensionManager.getBossWorld() != null
                && player.getWorld().equals(dimensionManager.getBossWorld());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
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

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You cannot place blocks while the boss is active.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
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

        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You cannot break blocks while the boss is active.");
    }
}