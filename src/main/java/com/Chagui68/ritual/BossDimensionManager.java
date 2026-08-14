package com.Chagui68.ritual;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import com.Chagui68.MultiverseCreatures;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BossDimensionManager {

    private final MultiverseCreatures plugin;
    private World bossWorld;
    private final String WORLD_NAME = "boss_dimension";

    public BossDimensionManager(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    public void createBossDimension() {
        BossDimensionSky.apply(plugin);

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
        creator.generator(new CryingObsidianChunkGenerator());
        creator.type(WorldType.FLAT);
        creator.generateStructures(false);

        bossWorld = creator.createWorld();

        if (bossWorld != null) {
            configureBossWorld(bossWorld);
            plugin.getLogger().info("Boss dimension created successfully!");
        } else {
            plugin.getLogger().severe("Failed to create boss dimension!");
        }
    }

    private void configureBossWorld(World world) {
        applyDeprecatedGameRules(world);
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
                Location spawnLocation = new Location(bossWorld, 0.5, 10, 0.5);

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

    private static class CryingObsidianChunkGenerator extends ChunkGenerator {

        @Override
        public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    chunkData.setBlock(x, 0, z, Material.BEDROCK);

                    for (int y = 1; y <= 5; y++) {
                        chunkData.setBlock(x, y, z, Material.CRYING_OBSIDIAN);
                    }
                }
            }
        }

        @Override
        public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        }

        @Override
        public List<BlockPopulator> getDefaultPopulators(World world) {
            return Collections.emptyList();
        }

        @Override
        public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
            return new BiomeProvider() {
                @Override
                public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
                    return Biome.PLAINS;
                }

                @Override
                public List<Biome> getBiomes(WorldInfo worldInfo) {
                    return Collections.singletonList(Biome.PLAINS);
                }
            };
        }
    }
}