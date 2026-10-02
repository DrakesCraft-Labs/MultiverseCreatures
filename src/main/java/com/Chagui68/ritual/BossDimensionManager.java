package com.Chagui68.ritual;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import com.Chagui68.MultiverseCreatures;
import com.Chagui68.ritual.terrain.WastelandGenerator;

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

    /** Config knob: side of the square the world border closes the dimension into, in blocks. */
    private static final String SIZE_KEY = "boss-dimension.size";

    /** The dimension's default size: the 1500 x 1500 battlefield the terrain is designed for. */
    public static final int DEFAULT_SIZE = 1500;

    /** Smallest border allowed: room for a boss fight and the invocation structures. */
    public static final int MIN_SIZE = 100;

    /** Largest border allowed, so the dimension never grows into a full-size world. */
    public static final int MAX_SIZE = 1500;

    /**
     * File left in the world folder naming the generator that built it. A world without it (or with
     * another name in it) was made by an older generator and is rebuilt once, on the next start: a
     * world that already exists is otherwise loaded as it is, and its old terrain would never go away.
     */
    static final String GENERATOR_MARKER = "msc-generator.txt";
    static final String GENERATOR_ID = "scorched-wasteland-1";

    private final MultiverseCreatures plugin;
    private World bossWorld;

    public BossDimensionManager(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    public void createBossDimension() {
        if (plugin.getConfig().getBoolean("boss-dimension.red-sky", true)) {
            BossDimensionSky.apply(plugin);
        }
        resetDimensionIfNeeded();

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
        creator.generator(new WastelandGenerator());
        creator.generateStructures(false);

        bossWorld = creator.createWorld();

        if (bossWorld != null) {
            writeGeneratorMarker(bossWorld.getWorldFolder().toPath());
            configureBossWorld(bossWorld);
            plugin.getLogger().info("Boss dimension created successfully!");
        } else {
            plugin.getLogger().severe("Failed to create boss dimension!");
        }
    }

    /** Whether a world folder was built by an older generator and has to be rebuilt. */
    static boolean builtByAnotherGenerator(Path folder) {
        if (!Files.isDirectory(folder)) return false;
        try {
            Path marker = folder.resolve(GENERATOR_MARKER);
            return !Files.isRegularFile(marker) || !GENERATOR_ID.equals(Files.readString(marker).trim());
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * Deletes the dimension folder when an operator asks for it, or when an older generator built it.
     *
     * <p>Either way it destroys whatever players built in that world, which is why it only happens
     * once: the rebuilt world carries the current {@link #GENERATOR_MARKER}.
     */
    private void resetDimensionIfNeeded() {
        Path folder = new File(Bukkit.getWorldContainer(), WORLD_NAME).toPath();
        boolean requested = plugin.getConfig().getBoolean(RESET_ON_LOAD_KEY, false);
        boolean outdated = builtByAnotherGenerator(folder);
        if (!requested && !outdated) return;
        if (!Files.isDirectory(folder)) return;

        if (Bukkit.getWorld(WORLD_NAME) != null) {
            plugin.getLogger().warning("The boss dimension needs to be rebuilt, but " + WORLD_NAME
                    + " is already loaded by another plugin: unload it first (nothing was deleted).");
            return;
        }

        plugin.getLogger().warning((requested ? "Boss dimension reset requested" : "The boss dimension "
                + "was built by an older generator") + ": deleting " + folder);
        try (Stream<Path> paths = Files.walk(folder)) {
            paths.sorted(Comparator.reverseOrder()).forEach(BossDimensionManager::deleteQuietly);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not walk the boss dimension folder: " + e.getMessage());
        }
        plugin.getLogger().warning("Boss dimension deleted: it will be regenerated on this start."
                + (requested ? " Set " + RESET_ON_LOAD_KEY + " back to false." : ""));
    }

    private void writeGeneratorMarker(Path folder) {
        try {
            Files.writeString(folder.resolve(GENERATOR_MARKER), GENERATOR_ID);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not mark the boss dimension's generator: " + e.getMessage());
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            Bukkit.getLogger().warning("Could not delete " + path + ": " + e.getMessage());
        }
    }

    /** The configured border size, kept inside [{@link #MIN_SIZE}, {@link #MAX_SIZE}]. */
    static int borderSize(int configured) {
        return Math.max(MIN_SIZE, Math.min(MAX_SIZE, configured));
    }

    private void configureBossWorld(World world) {
        applyDeprecatedGameRules(world);
        world.setSpawnLocation(WastelandGenerator.spawn(world));

        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(borderSize(plugin.getConfig().getInt(SIZE_KEY, DEFAULT_SIZE)));
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
            public void run() {
                Location spawnLocation = WastelandGenerator.spawn(bossWorld);

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