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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("msc.admin.bypass")) {
            return;
        }

        com.Chagui68.ritual.BossDimensionManager dimensionManager = plugin.getBossDimensionManager();
        if (dimensionManager == null || dimensionManager.getBossWorld() == null) {
            return;
        }

        if (player.getWorld().equals(dimensionManager.getBossWorld())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot place blocks in this dimension.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("msc.admin.bypass")) {
            return;
        }

        com.Chagui68.ritual.BossDimensionManager dimensionManager = plugin.getBossDimensionManager();
        if (dimensionManager == null || dimensionManager.getBossWorld() == null) {
            return;
        }

        if (player.getWorld().equals(dimensionManager.getBossWorld())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot break blocks in this dimension.");
        }
    }
}