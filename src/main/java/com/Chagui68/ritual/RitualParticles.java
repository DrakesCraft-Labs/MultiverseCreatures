package com.Chagui68.ritual;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.Chagui68.MultiverseCreatures;

public class RitualParticles {

    private final MultiverseCreatures plugin;
    private BukkitRunnable particleTask;
    private Location origin;
    private RitualState currentState;

    public enum RitualState {
        INCOMPLETE,
        STRUCTURE_COMPLETE,
        CANDLES_LIT,
        ACTIVATED
    }

    public RitualParticles(MultiverseCreatures plugin, Location origin) {
        this.plugin = plugin;
        this.origin = origin;
        this.currentState = RitualState.INCOMPLETE;
    }

    public void start() {
        if (particleTask != null) {
            particleTask.cancel();
        }

        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateState();
                spawnParticles();
            }
        };
        particleTask.runTaskTimer(plugin, 0L, 1L);
    }

    public void stop() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }

    private void updateState() {
        RitualState newState;

        if (!RitualStructure.isStructureComplete(origin)) {
            newState = RitualState.INCOMPLETE;
        } else if (RitualStructure.areAllCandlesLit(origin)) {
            newState = RitualState.CANDLES_LIT;
        } else {
            newState = RitualState.STRUCTURE_COMPLETE;
        }

        if (newState != currentState) {
            currentState = newState;
            onStateChange(newState);
        }
    }

    private void onStateChange(RitualState newState) {
        switch (newState) {
            case STRUCTURE_COMPLETE:
                plugin.getLogger().info("Ritual structure complete! Red fire particles activated.");
                break;
            case CANDLES_LIT:
                plugin.getLogger().info("All candles lit! Blue fire and activation particles started.");
                teleportPlayersViaPortal();
                break;
            case INCOMPLETE:
                plugin.getLogger().info("Ritual structure incomplete.");
                break;
        }
    }

    private void spawnParticles() {
        World world = origin.getWorld();
        if (world == null) return;

        Location center = RitualStructure.getCenterLocation(origin);
        double radius = RitualStructure.getRadius();

        switch (currentState) {
            case STRUCTURE_COMPLETE:
                spawnRedFireCircle(world, center, radius);
                break;
            case CANDLES_LIT:
                spawnBlueFireCircle(world, center, radius);
                spawnAscendingParticles(world, center, radius);
                break;
            default:
                break;
        }
    }

    private void spawnRedFireCircle(World world, Location center, double radius) {
        int particles = 50;

        for (int i = 0; i < particles; i++) {
            double angle = (2 * Math.PI * i) / particles;
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;

            Location particleLoc = new Location(world, x, center.getY() + 0.5, z);

            world.spawnParticle(Particle.FLAME, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
            world.spawnParticle(Particle.SMOKE, particleLoc, 1, 0.1, 0.1, 0.1, 0.02);
        }
    }

    private void spawnBlueFireCircle(World world, Location center, double radius) {
        int particles = 50;

        for (int i = 0; i < particles; i++) {
            double angle = (2 * Math.PI * i) / particles;
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;

            Location particleLoc = new Location(world, x, center.getY() + 0.5, z);

            world.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
            world.spawnParticle(Particle.SOUL, particleLoc, 1, 0.1, 0.1, 0.1, 0.02);
        }
    }

    private void spawnAscendingParticles(World world, Location center, double radius) {
        int particles = 15;

        for (int i = 0; i < particles; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double x = center.getX() + Math.cos(angle) * radius * (0.5 + Math.random() * 0.5);
            double z = center.getZ() + Math.sin(angle) * radius * (0.5 + Math.random() * 0.5);
            double y = center.getY() + Math.random() * 3;

            Location particleLoc = new Location(world, x, y, z);

            world.spawnParticle(Particle.PORTAL, particleLoc, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0.2, 0.2, 0.2, 0.01);
        }
    }

    private void teleportPlayersViaPortal() {
        if (currentState != RitualState.CANDLES_LIT) return;

        World world = origin.getWorld();
        if (world == null) return;

        Location center = RitualStructure.getCenterLocation(origin);
        double radius = RitualStructure.getRadius();

        BossDimensionManager bossManager = plugin.getBossDimensionManager();
        boolean inBossDimension = bossManager != null
                && bossManager.getBossWorld() != null
                && world.equals(bossManager.getBossWorld());

        new BukkitRunnable() {
            @Override
            public void run() {
                boolean teleported = false;
                for (Player player : world.getPlayers()) {
                    Location playerLoc = player.getLocation();
                    double distance = playerLoc.distance(center);

                    if (distance <= radius) {
                        if (inBossDimension) {
                            teleportPlayerToOverworld(player);
                        } else {
                            if (bossManager != null) {
                                bossManager.teleportPlayerToBossDimension(player);
                            }
                        }
                        teleported = true;
                    }
                }

                if (teleported) {
                    RitualStructure.extinguishAllCandles(origin);
                    RitualManager manager = plugin.getRitualManager();
                    if (manager != null) {
                        manager.stopRitual(world);
                    }
                }
            }
        }.runTaskLater(plugin, 100L);
    }

    private void teleportPlayerToOverworld(Player player) {
        org.bukkit.World overworld = org.bukkit.Bukkit.getWorlds().get(0);
        if (overworld == null) return;

        org.bukkit.Location destination = overworld.getSpawnLocation();
        player.teleportAsync(destination).thenAccept(success -> {
            if (success) {
                player.sendMessage(org.bukkit.ChatColor.GREEN + "You have returned to the overworld.");
                player.setAllowFlight(true);
            } else {
                player.sendMessage(org.bukkit.ChatColor.RED + "Failed to teleport to the overworld.");
            }
        });
    }

    public RitualState getCurrentState() {
        return currentState;
    }
}