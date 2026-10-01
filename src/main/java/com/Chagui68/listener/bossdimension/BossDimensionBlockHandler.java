package com.Chagui68.listener.bossdimension;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BossDimensionBlockHandler implements Listener {

    private final MultiverseCreatures plugin;

    public BossDimensionBlockHandler(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!BossFightGuard.restricts(plugin, event.getPlayer())) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "You cannot place blocks while the boss is active.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!BossFightGuard.restricts(plugin, event.getPlayer())) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "You cannot break blocks while the boss is active.");
    }
}
