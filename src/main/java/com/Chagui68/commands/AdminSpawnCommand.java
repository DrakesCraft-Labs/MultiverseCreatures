package com.Chagui68.commands;

import com.Chagui68.entities.MobHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;

public class AdminSpawnCommand implements CommandExecutor {

    private final MobHandler mobHandler;

    public AdminSpawnCommand(MobHandler mobHandler) {
        this.mobHandler = mobHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("multiversecreatures.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("spawn")) {
            player.sendMessage(ChatColor.RED + "Usage: /msc spawn <type>");
            return true;
        }

        String type = args[1].toLowerCase();
        if (type.equals("shaggy")) {
            WanderingTrader shaggy = (WanderingTrader) player.getWorld().spawnEntity(player.getLocation(),
                    EntityType.WANDERING_TRADER);
            mobHandler.transformIntoShaggy(shaggy);
            player.sendMessage(ChatColor.GREEN + "Shaggy has been spawned!");
        } else {
            player.sendMessage(ChatColor.RED + "Unknown type. Available types: shaggy");
        }

        return true;
    }
}
