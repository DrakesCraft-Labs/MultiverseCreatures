package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.MagmaCore;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class FlameElemental implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, FlameElementalInstance> active = new HashMap<>();
    private static final String TAG = "MSC_FlameElemental";
    private double dropChance;
    private double health;

    public FlameElemental(MultiverseCreatures plugin) {
        this.plugin = plugin;
        dropChance = plugin.getConfig().getDouble("entities.flame-elemental.drop-chance", 0.6);
        health = plugin.getConfig().getDouble("entities.flame-elemental.health", 80.0);
        if (!plugin.isEnabled("entities.flame-elemental")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Blaze b : world.getEntitiesByClass(Blaze.class)) {
                if (b.getScoreboardTags().contains(TAG)) {
                    active.put(b.getUniqueId(), new FlameElementalInstance(b));
                }
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : new HashMap<>(active).entrySet()) {
                    FlameElementalInstance inst = entry.getValue();
                    if (inst.blaze.isDead() || !inst.blaze.isValid()) {
                        active.remove(entry.getKey());
                        continue;
                    }
                    tickFlame(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.flame-elemental")) return false;
        Blaze blaze = (Blaze) location.getWorld().spawnEntity(location, EntityType.BLAZE);
        if (blaze == null) return false;
        blaze.addScoreboardTag(TAG);
        blaze.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + "Flame Elemental");
        blaze.setCustomNameVisible(true);
        blaze.setPersistent(true);
        blaze.setRemoveWhenFarAway(false);
        MscEntityUtils.setAttribute(blaze, Attribute.MAX_HEALTH, health);
        blaze.setHealth(80.0);
        MscEntityUtils.setAttribute(blaze, Attribute.MOVEMENT_SPEED, 0.25);
        blaze.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        blaze.setAI(true);
        active.put(blaze.getUniqueId(), new FlameElementalInstance(blaze));
        return true;
    }

    private void tickFlame(FlameElementalInstance inst) {
        Blaze blaze = inst.blaze;
        if (!(blaze.getTarget() instanceof Player target)) return;
        if (target.isDead() || !target.isOnline()) return;
        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            blaze.setTarget(null);
            return;
        }

        Location bLoc = blaze.getLocation();
        Location tLoc = target.getLocation();
        double dist = bLoc.distance(tLoc);
        inst.meteorCooldown++;

        if (dist > 3 && dist < 20 && inst.meteorCooldown > 60) {
            inst.activeMeteor = new MeteorProjectile(bLoc.clone().add(0, 1.5, 0), target);
            inst.meteorCooldown = 0;
        }

        if (inst.activeMeteor != null) {
            MeteorProjectile meteor = inst.activeMeteor;
            meteor.targetLoc = meteor.target.getLocation();
            Location tTarget = meteor.targetLoc;

            if (meteor.origin.getWorld() != tTarget.getWorld()) {
                inst.activeMeteor = null;
                return;
            }

            Vector toTarget = tTarget.toVector().subtract(meteor.pos.toVector());
            double remaining = toTarget.length();
            double step = 0.6;
            double speed = step / remaining;

            if (remaining > 0.1) {
                meteor.pos.add(toTarget.normalize().multiply(step));
                meteor.traveled++;

                Location pLoc = meteor.pos.clone();
                for (int i = 0; i < 3; i++) {
                    Location pl = pLoc.clone().add(
                            (random.nextDouble() - 0.5) * 0.6,
                            (random.nextDouble() - 0.5) * 0.6,
                            (random.nextDouble() - 0.5) * 0.6);
                    meteor.world.spawnParticle(Particle.FLAME, pl, 2, 0, 0, 0, 0.02);
                    meteor.world.spawnParticle(Particle.SMOKE, pl, 1, 0, 0, 0, 0.02);
                }
                meteor.world.spawnParticle(Particle.LAVA, pLoc, 1, 0.2, 0.1, 0.2, 0);
                meteor.world.spawnParticle(Particle.DUST, pLoc, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.ORANGE, 2.0f));
                meteor.world.playSound(pLoc, Sound.BLOCK_FIRE_AMBIENT, 0.4f, 1.5f);

                for (Player p : meteor.world.getPlayers()) {
                    if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
                    if (p.getLocation().distanceSquared(meteor.pos) < 4) {
                        p.setFireTicks(100);
                        MscEntityUtils.damageBy(blaze, p, 12.0);
                        meteor.world.spawnParticle(Particle.EXPLOSION, pLoc, 10, 1, 1, 1, 0.1);
                        meteor.world.playSound(pLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.6f);
                        inst.activeMeteor = null;
                        return;
                    }
                }
            }

            if (meteor.pos.distanceSquared(tTarget) < 1.5 || meteor.traveled > 40) {
                meteor.world.spawnParticle(Particle.EXPLOSION, meteor.pos, 15, 1, 1, 1, 0.1);
                meteor.world.playSound(meteor.pos, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.6f);
                for (Player p : meteor.world.getPlayers()) {
                    if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
                    if (p.getLocation().distanceSquared(meteor.pos) < 9) {
                        p.setFireTicks(80);
                        MscEntityUtils.damageBy(blaze, p, 6.0);
                    }
                }
                inst.activeMeteor = null;
            }
        }

        if (dist < 5 && blaze.getFireTicks() > 0) {
            target.setFireTicks(target.getFireTicks() + 20);
            MscEntityUtils.damageBy(blaze, target, 3.0);
            tLoc.getWorld().spawnParticle(Particle.FLAME, tLoc.add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.03);
        }

        blaze.setVisualFire(true);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Blaze blaze)) return;
        if (!blaze.getScoreboardTags().contains(TAG)) return;
        if (event.getEntity() instanceof Player p) {
            p.setFireTicks(80);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Blaze blaze)) return;
        if (!blaze.getScoreboardTags().contains(TAG)) return;
        active.remove(blaze.getUniqueId());
        event.getDrops().clear();
        if (Math.random() < dropChance) {
            blaze.getWorld().dropItemNaturally(blaze.getLocation(), MagmaCore.MAGMA_CORE.clone());
        }
        event.setDroppedExp(40);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MscEntityUtils.applyDeathMessage(plugin, event, TAG, "entities.flame-elemental.death-messages");
    }

    private static class FlameElementalInstance {
        final Blaze blaze;
        int meteorCooldown = 0;
        MeteorProjectile activeMeteor;

        FlameElementalInstance(Blaze b) {
            this.blaze = b;
        }
    }

    private static class MeteorProjectile {
        final World world;
        final Player target;
        final Location origin;
        Location pos;
        Location targetLoc;
        int traveled = 0;

        MeteorProjectile(Location origin, Player target) {
            this.world = origin.getWorld();
            this.target = target;
            this.origin = origin.clone();
            this.pos = origin.clone();
            this.targetLoc = target.getLocation();
        }
    }
}