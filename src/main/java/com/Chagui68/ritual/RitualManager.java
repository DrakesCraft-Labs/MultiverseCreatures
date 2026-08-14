package com.Chagui68.ritual;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.Chagui68.MultiverseCreatures;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RitualManager {

    private final MultiverseCreatures plugin;
    private final Map<UUID, RitualParticles> activeRituals;

    public RitualManager(MultiverseCreatures plugin) {
        this.plugin = plugin;
        this.activeRituals = new HashMap<>();
    }

    public void startRitual(Location origin) {
        World world = origin.getWorld();
        if (world == null) return;

        UUID worldUuid = world.getUID();

        if (activeRituals.containsKey(worldUuid)) {
            plugin.getLogger().warning("A ritual is already active in this world");
            return;
        }

        RitualParticles ritual = new RitualParticles(plugin, origin);
        activeRituals.put(worldUuid, ritual);
        ritual.start();

        plugin.getLogger().info("Ritual started at: " + origin);
    }

    public void stopRitual(World world) {
        if (world == null) return;

        RitualParticles ritual = activeRituals.remove(world.getUID());
        if (ritual != null) {
            ritual.stop();
            plugin.getLogger().info("Ritual stopped in: " + world.getName());
        }
    }

    public void stopAllRituals() {
        for (RitualParticles ritual : activeRituals.values()) {
            ritual.stop();
        }
        activeRituals.clear();
        plugin.getLogger().info("All rituals have been stopped");
    }

    public RitualParticles getRitual(World world) {
        if (world == null) return null;
        return activeRituals.get(world.getUID());
    }

    public boolean isRitualActive(World world) {
        if (world == null) return false;
        return activeRituals.containsKey(world.getUID());
    }

    public void checkAndStartRitual(Location origin) {
        if (RitualStructure.isStructureComplete(origin)) {
            if (!isRitualActive(origin.getWorld())) {
                startRitual(origin);
            }
        }
    }
}