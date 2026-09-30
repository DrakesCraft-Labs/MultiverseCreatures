package com.Chagui68.ritual;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import com.Chagui68.MultiverseCreatures;
import com.Chagui68.ritual.terrain.ArenaShape;
import com.Chagui68.ritual.terrain.BossArenaGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class BossDimensionManager {

    /** Name of the dimension the plugin owns; also the folder under the world container. */
    public static final String WORLD_NAME = "boss_dimension";

    /** Config knob: delete the world once on startup so a regenerated dimension is visible. */
    private static final String RESET_ON_LOAD_KEY = "boss-dimension.reset-on-load";

    private final MultiverseCreatures plugin;
    private World bossWorld;

    public BossDimensionManager(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    public void createBossDimension() {
        if (plugin.getConfig().getBoolean("boss-dimension.red-sky", true)) {
            BossDimensionSky.apply(plugin);
        }
        resetDimensionIfRequested();

        if (bossWorld != null) {
            plugin.getLogger().info("Boss dimension already exists: " + WORLD_NAME);
            return;
        }

        World existingWorld = Bukkit.getWorld(WORLD_NAME);
        if (existingWorld != null) {
            this.bossWorld = existingWorld;
            plugin.getLogger().info("Loaded existing boss dimension: " + WORLD_NAME);
            configureBossWorld(existingWorld);
            return;
        }

        plugin.getLogger().info("Creating boss dimension: " + WORLD_NAME);

        WorldCreator creator = new WorldCreator(WORLD_NAME);
        creator.environment(World.Environment.NORMAL);
        creator.generator(new BossArenaGenerator());
        creator.generateStructures(false);

        bossWorld = creator.createWorld();

        if (bossWorld != null) {
            configureBossWorld(bossWorld);
            plugin.getLogger().info("Boss dimension created successfully!");
        } else {
            plugin.getLogger().severe("Failed to create boss dimension!");
        }
    }

    /**
     * Deletes the dimension folder once, when an operator asks for it.
     *
     * <p>A world that already exists is never regenerated: {@code createBossDimension} loads it as
     * it is, so terrain changes only reach the chunks nobody has visited yet. Setting {@code
     * boss-dimension.reset-on-load} to true throws the old world away on the next start, which is
     * the only way to see a new generator at the spawn — it is off by default and documented as a
     * one-shot switch because it destroys everything players built there.
     */
    private void resetDimensionIfRequested() {
        if (!plugin.getConfig().getBoolean(RESET_ON_LOAD_KEY, false)) return;

        World loaded = Bukkit.getWorld(WORLD_NAME);
        if (loaded != null) {
            plugin.getLogger().warning("Boss dimension reset requested, but " + WORLD_NAME
                    + " is already loaded: unload it first (nothing was deleted).");
            return;
        }
        Path folder = new File(Bukkit.getWorldContainer(), WORLD_NAME).toPath();
        if (!Files.isDirectory(folder)) return;

        plugin.getLogger().warning("Boss dimension reset requested: deleting " + folder);
        try (Stream<Path> paths = Files.walk(folder)) {
            paths.sorted(Comparator.reverseOrder()).forEach(BossDimensionManager::deleteQuietly);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not walk the boss dimension folder: " + e.getMessage());
        }
        plugin.getLogger().warning("Boss dimension deleted: it will be regenerated on this start. "
                + "Set " + RESET_ON_LOAD_KEY + " back to false.");
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            Bukkit.getLogger().warning("Could not delete " + path + ": " + e.getMessage());
        }
    }

    private void configureBossWorld(World world) {
        applyDeprecatedGameRules(world);
        world.setSpawnLocation(0, ArenaShape.spawnY(), 0);
    }

    // GameRule API is marked for removal in Bukkit 1.21;
    // using string-based setGameRuleValue pending Paper replacement
    @SuppressWarnings("removal")
    private void applyDeprecatedGameRules(World world) {
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doImmediateRespawn", "true");
        world.setGameRuleValue("announceAdvancements", "false");

        world.setTime(14000);
        world.setStorm(false);
        world.setThundering(false);

        for (Chunk chunk : world.getLoadedChunks()) {
            for (Entity entity : chunk.getEntities()) {
                if (entity instanceof Player) continue;
                entity.remove();
            }
        }
    }

    public void teleportPlayerToBossDimension(Player player) {
        if (bossWorld == null) {
            createBossDimension();
        }

        if (bossWorld == null) {
            player.sendMessage(ChatColor.RED + "Error: Could not create the boss dimension");
            return;
        }

        Player finalPlayer = player;
        new BukkitRunnable() {
            @Override
            public void run() {                    Location spawnLocation = new Location(bossWorld, 0.5, ArenaShape.spawnY(), 0.5);

                finalPlayer.teleportAsync(spawnLocation).thenAccept(success -> {
                    if (success) {
                        finalPlayer.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "You have been transported to the ritual dimension...");
                        finalPlayer.sendMessage(ChatColor.RED + "There is no escape.");

                        finalPlayer.setFlying(false);
                        finalPlayer.setAllowFlight(false);

                        finalPlayer.addPotionEffect(new PotionEffect(
                                PotionEffectType.BLINDNESS,
                                100,
                                0,
                                false,
                                false
                        ));

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                finalPlayer.removePotionEffect(PotionEffectType.BLINDNESS);
                                finalPlayer.playSound(finalPlayer.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.5f);
                            }
                        }.runTaskLater(plugin, 100L);
                    } else {
                        finalPlayer.sendMessage(ChatColor.RED + "Error teleporting to the boss dimension");
                    }
                });
            }
        }.runTask(plugin);
    }

    public void teleportPlayerBack(Player player, Location origin) {
        if (player.getWorld() != bossWorld) {
            return;
        }

        Location returnLocation = RitualStructure.getCenterLocation(origin).add(0, 2, 0);

        player.teleportAsync(returnLocation).thenAccept(success -> {
            if (success) {
                player.sendMessage(ChatColor.GREEN + "You have escaped from the ritual dimension.");
                player.setAllowFlight(true);
            }
        });
    }

    public World getBossWorld() {
        return bossWorld;
    }

    public boolean isInBossDimension(Player player) {
        return bossWorld != null && player.getWorld().equals(bossWorld);
    }

    public void unloadBossDimension() {
        if (bossWorld != null) {
            for (Player player : bossWorld.getPlayers()) {
                player.teleportAsync(Bukkit.getWorlds().get(0).getSpawnLocation());
            }

            Bukkit.unloadWorld(bossWorld, false);
            bossWorld = null;
            plugin.getLogger().info("Boss dimension unloaded.");
        }
    }

}