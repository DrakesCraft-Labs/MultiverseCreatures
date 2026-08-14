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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("msc.admin.bypass")) {
            return;
        }

        com.Chagui68.ritual.BossDimensionManager dimensionManager = plugin.getBossDimensionManager();
        if (dimensionManager == null || dimensionManager.getBossWorld() == null) {
            return;
        }

        if (player.getWorld().equals(dimensionManager.getBossWorld())) {
            String command = event.getMessage().toLowerCase();

            if (command.startsWith("/msc") ||
                    command.startsWith("/multiversecreatures") ||
                    command.startsWith("/return") ||
                    command.startsWith("/back") ||
                    command.startsWith("/tp") ||
                    command.startsWith("/teleport") ||
                    command.startsWith("/gamemode") ||
                    command.startsWith("/gm") ||
                    command.startsWith("/give") ||
                    command.startsWith("/ban") ||
                    command.startsWith("/pardon") ||
                    command.startsWith("/kick") ||
                    command.startsWith("/difficulty") ||
                    command.startsWith("/whitelist") ||
                    command.startsWith("/stop") ||
                    command.startsWith("/reload") ||
                    command.startsWith("/tpa") ||
                    command.startsWith("/rl")) {

                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot use commands in this dimension.");
                return;
            }

            if (!command.startsWith("/say") &&
                    !command.startsWith("/me") &&
                    !command.startsWith("/help") &&
                    !command.startsWith("/?") &&
                    !command.startsWith("/dimtp")) {

                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot use commands in this dimension.");
            }
        }
    }
}