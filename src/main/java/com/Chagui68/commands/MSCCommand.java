package com.Chagui68.commands;

import com.Chagui68.entities.MobHandler;
import com.Chagui68.entities.dragon.DragonCombatHandler;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class MSCCommand implements CommandExecutor {

    private final MobHandler mobHandler;
    private final DragonCombatHandler dragonCombatHandler;

    public MSCCommand(MobHandler mobHandler, DragonCombatHandler dragonCombatHandler) {
        this.mobHandler = mobHandler;
        this.dragonCombatHandler = dragonCombatHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spawn":
                handleSpawn(sender, args);
                break;
            case "damage":
                handleDamage(sender, args);
                break;
            case "ability":
                handleAbility(sender, args);
                break;
            case "chests":
                if (sender instanceof Player) {
                    dragonCombatHandler.spawnRewardChests(((Player) sender).getWorld());
                    sender.sendMessage(ChatColor.GREEN + "Spawned reward chests for testing.");
                }
                break;
            case "phase":
                handlePhase(sender, args);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /msc for help.");
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can spawn entities.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /msc spawn <shaggy|dragon>");
            return;
        }

        Player p = (Player) sender;
        String type = args[1].toLowerCase();

        if (type.equals("shaggy")) {
            mobHandler.spawnShaggy(p.getLocation());
            sender.sendMessage(ChatColor.GREEN + "Spawned Shaggy!");
        } else if (type.equals("dragon")) {
            EnderDragon dragon = (EnderDragon) p.getWorld().spawnEntity(p.getLocation(), EntityType.ENDER_DRAGON);
            dragonCombatHandler.configureDragon(dragon);
            sender.sendMessage(ChatColor.GREEN + "Spawned Custom Dragon!");
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown entity type.");
        }
    }

    private void handleDamage(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /msc damage <amount>");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid number.");
            return;
        }

        EnderDragon target = getNearestDragon(sender);
        if (target != null) {
            if (sender instanceof Player) {
                target.damage(amount, (Player) sender);
            } else {
                target.damage(amount);
            }
            sender.sendMessage(
                    ChatColor.GREEN + "Applied " + amount + " damage to the dragon (" + target.getCustomName() + ").");
        } else {
            sender.sendMessage(
                    ChatColor.RED + "No custom dragon found in this world. Make sure it has been configured.");
        }
    }

    private void handleAbility(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(
                    ChatColor.RED + "Usage: /msc ability <slam|storm|floor|scream|meteors|mist|shadow|beam|roulette>");
            return;
        }

        EnderDragon dragon = getNearestDragon(sender);
        if (dragon == null) {
            sender.sendMessage(ChatColor.RED + "No custom dragon found nearby.");
            return;
        }

        String ability = args[1].toLowerCase();
        switch (ability) {
            case "slam":
                dragonCombatHandler.performGroundSlam(dragon);
                break;
            case "storm":
                dragonCombatHandler.performLightningStorm(dragon);
                break;
            case "floor":
                dragonCombatHandler.performVoidBreath(dragon);
                break;
            case "scream":
                dragonCombatHandler.performAbyssalScream(dragon);
                break;
            case "meteors":
                dragonCombatHandler.performMeteorShower(dragon);
                break;
            case "mist":
                dragonCombatHandler.performAbyssalMist(dragon);
                break;
            case "shadow":
                dragonCombatHandler.performShadowEnemies(dragon);
                break;
            case "beam":
                dragonCombatHandler.performVoidBeam(dragon);
                break;
            case "roulette":
                dragonCombatHandler.startAbilityRoulette(dragon);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown ability.");
                break;
        }
    }

    private EnderDragon getNearestDragon(CommandSender sender) {
        if (!(sender instanceof Player))
            return null;
        Player p = (Player) sender;
        EnderDragon nearest = null;
        double minDist = 40000; // 200 blocks

        // Primary search: nearby tagged dragons
        for (EnderDragon d : p.getWorld().getEntitiesByClass(EnderDragon.class)) {
            if (d.getScoreboardTags().contains("MSC_CustomDragon")) {
                double dist = d.getLocation().distanceSquared(p.getLocation());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = d;
                }
            }
        }

        // Secondary search: any tagged dragon in the world (if none nearby)
        if (nearest == null) {
            for (EnderDragon d : p.getWorld().getEntitiesByClass(EnderDragon.class)) {
                if (d.getScoreboardTags().contains("MSC_CustomDragon")) {
                    return d;
                }
            }
        }

        return nearest;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- MultiverseCreatures Commands ---");
        sender.sendMessage(ChatColor.YELLOW + "/msc spawn <type> " + ChatColor.WHITE + "- Spawn custom mobs");
        sender.sendMessage(ChatColor.YELLOW + "/msc damage <amount> " + ChatColor.WHITE + "- Damage nearest dragon");
        sender.sendMessage(ChatColor.YELLOW + "/msc ability <name> " + ChatColor.WHITE + "- Force a dragon ability");
        sender.sendMessage(
                ChatColor.YELLOW + "/msc phase <color> " + ChatColor.WHITE + "- Force a specific phase color");
        sender.sendMessage(ChatColor.YELLOW + "/msc chests " + ChatColor.WHITE + "- Spawn reward chests (Test)");
    }

    private void handlePhase(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /msc phase <white|green|blue|yellow|pink|purple|red>");
            return;
        }

        EnderDragon dragon = getNearestDragon(sender);
        if (dragon == null) {
            sender.sendMessage(ChatColor.RED + "No custom dragon found nearby.");
            return;
        }

        try {
            BarColor color = BarColor.valueOf(args[1].toUpperCase());
            dragonCombatHandler.forcePhaseColor(dragon, color);
            sender.sendMessage(ChatColor.GREEN + "Dragon phase forced to: " + ChatColor.BOLD + color.name());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid color. Use: white, green, blue, yellow, pink, purple, red");
        }
    }
}
