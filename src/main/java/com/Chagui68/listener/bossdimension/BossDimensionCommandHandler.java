package com.Chagui68.listener.bossdimension;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class BossDimensionCommandHandler implements Listener {

    private final MultiverseCreatures plugin;

    public BossDimensionCommandHandler(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!BossFightGuard.restricts(plugin, event.getPlayer())) return;
        if (BossFightGuard.isAllowedCommand(event.getMessage())) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "You cannot use commands while the boss is active.");
    }
}
