package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.GameMode;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.Vector;

import java.util.Random;

public class CreeperJr implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private static final String TAG = "MSC_CreeperJr";

    public CreeperJr(MultiverseCreatures plugin) {
        this.plugin = plugin;
        if (!plugin.isEnabled("entities.creeper-jr")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Creeper creeper : world.getEntitiesByClass(Creeper.class)) {
                if (creeper.getScoreboardTags().contains(TAG)) {
                    applyAttributes(creeper);
                }
            }
        }
    }

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.creeper-jr")) return false;
        for (int i = 0; i < 3; i++) {
            Location spawnLoc = i == 0 ? location : location.clone().add(
                    (random.nextDouble() - 0.5) * 3, 0, (random.nextDouble() - 0.5) * 3
            );
            Creeper creeper = (Creeper) location.getWorld().spawnEntity(spawnLoc, EntityType.CREEPER);
            if (creeper == null) continue;

            creeper.addScoreboardTag(TAG);
            applyAttributes(creeper);
            creeper.setCustomName(ChatColor.GREEN + "" + ChatColor.BOLD + "Creeper Jr.");
            creeper.setCustomNameVisible(true);
            creeper.setRemoveWhenFarAway(false);
            creeper.setPersistent(true);
            creeper.setCollidable(true);
            creeper.setAI(true);
            creeper.setCanPickupItems(false);
        }
        return true;
    }

    private void applyAttributes(Creeper creeper) {
        double scale = plugin.getConfig().getDouble("entities.creeper-jr.scale", 0.6);
        double speed = plugin.getConfig().getDouble("entities.creeper-jr.speed", 0.5);
        int radius = plugin.getConfig().getInt("entities.creeper-jr.explosion-radius", 2);

        if (creeper.getAttribute(Attribute.SCALE) != null) {
            creeper.getAttribute(Attribute.SCALE).setBaseValue(scale);
        }
        if (creeper.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            creeper.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
        }
        creeper.setExplosionRadius(radius);
        creeper.setMaxFuseTicks(25);
    }


    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (event.getEntity() instanceof Creeper creeper && creeper.getScoreboardTags().contains(TAG)) {
            event.setYield(0);

            pushEntitiesAway(creeper);

            int maxRadius = creeper.getExplosionRadius();
            double maxDamage = plugin.getConfig().getDouble("entities.creeper-jr.true-damage", 12.0);
            Location explosionLoc = event.getLocation();

            for (Player player : explosionLoc.getWorld().getPlayers()) {
                if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) continue;

                double distance = player.getLocation().distance(explosionLoc);
                if (distance > maxRadius) continue;

                double multiplier = 1.0 - (distance / maxRadius);
                double damage = maxDamage * multiplier;
                if (damage < 1) damage = 1;

                player.damage(damage, DamageSource.builder(DamageType.OUT_OF_WORLD)
                        .withDirectEntity(creeper)
                        .withCausingEntity(creeper)
                        .withDamageLocation(explosionLoc)
                        .build());
            }
        }
    }

    private void pushEntitiesAway(Creeper source) {
        for (Entity entity : source.getNearbyEntities(8, 8, 8)) {
            if (entity instanceof Player) continue;
            if (entity instanceof LivingEntity living && !entity.equals(source)) {
                Vector away = living.getLocation().toVector().subtract(source.getLocation().toVector());
                if (away.lengthSquared() > 0) {
                    living.setVelocity(living.getVelocity().add(away.normalize().multiply(1.5).setY(0.5)));
                }
                if (living instanceof Creeper other && other.getScoreboardTags().contains(TAG)) {
                    other.setTarget(null);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Creeper creeper)) return;
        if (!creeper.getScoreboardTags().contains(TAG)) return;
        java.util.List<String> messages = plugin.getConfig().getStringList("entities.creeper-jr.death-messages");
        if (messages.isEmpty()) return;
        String raw = messages.get(random.nextInt(messages.size()));
        event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', raw.replace("%player%", event.getEntity().getName())));
    }
}
