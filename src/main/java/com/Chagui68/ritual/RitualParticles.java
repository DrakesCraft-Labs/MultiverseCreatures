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
        Location home = findPlayerHome(player);
        if (home != null) {
            player.teleportAsync(home).thenAccept(success -> {
                if (success) {
                    player.sendMessage(org.bukkit.ChatColor.GREEN + "You have returned to your home.");
                    player.setAllowFlight(true);
                } else {
                    teleportToOverworldSpawn(player);
                }
            });
            return;
        }
        teleportToOverworldSpawn(player);
    }

    private void teleportToOverworldSpawn(Player player) {
        org.bukkit.World overworld = org.bukkit.Bukkit.getWorlds().get(0);
        if (overworld == null) return;

        org.bukkit.Location destination = findSafeLocation(overworld, overworld.getSpawnLocation());
        if (destination == null) {
            destination = overworld.getSpawnLocation();
        }
        final org.bukkit.Location target = destination;
        player.teleportAsync(target).thenAccept(success -> {
            if (success) {
                player.sendMessage(org.bukkit.ChatColor.GREEN + "You have returned to the overworld.");
                player.setAllowFlight(true);
            } else {
                player.sendMessage(org.bukkit.ChatColor.RED + "Failed to teleport to the overworld.");
            }
        });
    }

    /**
     * Busca, alrededor del centro, el primer punto seguro para aparecer:
     * suelo sólido que no sea líquido ni bloque peligroso y al menos dos
     * bloques libres encima. Devuelve null si no encuentra ninguno.
     */
    private org.bukkit.Location findSafeLocation(org.bukkit.World world, org.bukkit.Location center) {
        int searchRadius = 32;
        int cx = center.getBlockX();
        int cz = center.getBlockZ();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        for (int dx = 0; dx <= searchRadius; dx++) {
            for (int dz = 0; dz <= searchRadius; dz++) {
                int[][] offsets = {{dx, dz}, {dx, -dz}, {-dx, dz}, {-dx, -dz}};
                for (int[] off : offsets) {
                    int x = cx + off[0];
                    int z = cz + off[1];
                    for (int y = maxY - 1; y > minY; y--) {
                        org.bukkit.block.Block ground = world.getBlockAt(x, y, z);
                        if (!isSafeGround(ground)) continue;
                        if (!world.getBlockAt(x, y + 1, z).isPassable()) continue;
                        if (!world.getBlockAt(x, y + 2, z).isPassable()) continue;
                        return new org.bukkit.Location(world, x + 0.5, y + 1, z + 0.5,
                                center.getYaw(), center.getPitch());
                    }
                }
            }
        }
        return null;
    }

    private boolean isSafeGround(org.bukkit.block.Block block) {
        if (block.isPassable()) return false;
        if (block.isLiquid()) return false;
        org.bukkit.Material type = block.getType();
        return type != org.bukkit.Material.CACTUS
                && type != org.bukkit.Material.MAGMA_BLOCK
                && type != org.bukkit.Material.FIRE
                && type != org.bukkit.Material.SOUL_FIRE
                && type != org.bukkit.Material.CAMPFIRE
                && type != org.bukkit.Material.SOUL_CAMPFIRE
                && type != org.bukkit.Material.SWEET_BERRY_BUSH
                && type != org.bukkit.Material.POWDER_SNOW
                && type != org.bukkit.Material.WITHER_ROSE
                && type != org.bukkit.Material.POINTED_DRIPSTONE;
    }

    /**
     * Lee los homes del jugador desde los datos de EssentialsX
     * (plugins/Essentials/userdata/&lt;uuid&gt;.yml) y devuelve el preferido
     * ("home" o el primero definido). Devuelve null si no hay datos, si el
     * mundo no está cargado o si el home cae en la dimensión del boss
     * (evitando bucles de teleport).
     */
    private Location findPlayerHome(Player player) {
        try {
            java.io.File userdata = new java.io.File(plugin.getDataFolder().getParentFile(),
                    "Essentials/userdata/" + player.getUniqueId() + ".yml");
            if (!userdata.isFile()) return null;

            org.bukkit.configuration.ConfigurationSection homes =
                    org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(userdata)
                            .getConfigurationSection("homes");
            if (homes == null) return null;

            org.bukkit.World bossWorld = plugin.getBossDimensionManager() != null
                    ? plugin.getBossDimensionManager().getBossWorld() : null;

            String preferred = homes.contains("home") ? "home" : null;
            if (preferred == null) {
                for (String name : homes.getKeys(false)) {
                    preferred = name;
                    break;
                }
            }
            if (preferred == null) return null;

            org.bukkit.configuration.ConfigurationSection home = homes.getConfigurationSection(preferred);
            if (home == null) return null;

            String worldName = home.getString("world");
            if (worldName == null) return null;
            org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
            if (world == null) return null;
            if (bossWorld != null && world.equals(bossWorld)) return null;

            return new Location(world,
                    home.getDouble("x", 0),
                    home.getDouble("y", world.getMaxHeight()),
                    home.getDouble("z", 0),
                    (float) home.getDouble("yaw", 0),
                    (float) home.getDouble("pitch", 0));
        } catch (Exception e) {
            plugin.getLogger().warning("Could not read homes for " + player.getName() + ": " + e.getMessage());
            return null;
        }
    }

    public RitualState getCurrentState() {
        return currentState;
    }
}