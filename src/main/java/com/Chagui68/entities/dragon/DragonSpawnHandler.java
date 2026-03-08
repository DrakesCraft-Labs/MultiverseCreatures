package com.Chagui68.entities.dragon;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class DragonSpawnHandler implements Listener {

    private final DragonCombatHandler combatHandler;

    public DragonSpawnHandler(DragonCombatHandler combatHandler) {
        this.combatHandler = combatHandler;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDragonSpawn(CreatureSpawnEvent event) {
        // Only target the End dimension
        if (event.getLocation().getWorld().getEnvironment() != World.Environment.THE_END) {
            return;
        }

        // Logic for replacing/modifying the Dragon
        if (event.getEntityType() == EntityType.ENDER_DRAGON) {
            EnderDragon dragon = (EnderDragon) event.getEntity();
            combatHandler.configureDragon(dragon);
        }
    }
}
