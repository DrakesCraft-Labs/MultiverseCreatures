package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.StormCrystal;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class StormCaller implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, StormCallerInstance> active = new HashMap<>();
    private static final String TAG = "MSC_StormCaller";
    private double dropChance;
    private double health;

    public StormCaller(MultiverseCreatures plugin) {
        this.plugin = plugin;
        dropChance = plugin.getConfig().getDouble("entities.storm-caller.drop-chance", 0.6);
        health = plugin.getConfig().getDouble("entities.storm-caller.health", 60.0);
        if (!plugin.isEnabled("entities.storm-caller")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Witch w : world.getEntitiesByClass(Witch.class)) {
                if (w.getScoreboardTags().contains(TAG)) {
                    active.put(w.getUniqueId(), new StormCallerInstance(w));
                }
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : new HashMap<>(active).entrySet()) {
                    StormCallerInstance inst = entry.getValue();
                    if (inst.witch.isDead() || !inst.witch.isValid()) {
                        active.remove(entry.getKey());
                        continue;
                    }
                    tickStorm(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.storm-caller")) return false;
        Witch witch = (Witch) location.getWorld().spawnEntity(location, EntityType.WITCH);
        if (witch == null) return false;
        MscEntityUtils.applyAmbientPersistence(plugin, witch);
        customize(witch);
        return true;
    }

    public boolean convertExisting(Witch witch) {
        if (!plugin.isEnabled("entities.storm-caller")) return false;
        if (witch == null || witch.isDead() || !witch.isValid()) return false;
        customize(witch);
        return true;
    }

    private void customize(Witch witch) {
        witch.addScoreboardTag(TAG);
        witch.setCustomName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Storm Caller");
        witch.setCustomNameVisible(true);
        MscEntityUtils.setAttribute(witch, Attribute.MAX_HEALTH, health);
        witch.setHealth(60.0);
        MscEntityUtils.setAttribute(witch, Attribute.MOVEMENT_SPEED, 0.28);
        witch.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        witch.setAI(true);
        active.put(witch.getUniqueId(), new StormCallerInstance(witch));
    }

    private void tickStorm(StormCallerInstance inst) {
        Witch witch = inst.witch;
        if (!(witch.getTarget() instanceof Player target)) return;
        if (target.isDead() || !target.isOnline()) return;
        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            witch.setTarget(null);
            return;
        }

        Location wLoc = witch.getLocation();
        Location tLoc = target.getLocation();
        inst.cloudCooldown++;

        if (inst.cloudCooldown > 100) {
            for (int a = 0; a < 20; a++) {
                double angle = (2 * Math.PI * a / 20);
                double r = 5.0;
                double x = tLoc.getX() + Math.cos(angle) * r;
                double z = tLoc.getZ() + Math.sin(angle) * r;
                Location pl = new Location(wLoc.getWorld(), x, tLoc.getY() + 5 + Math.sin(angle * 2) * 1, z);
                wLoc.getWorld().spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(0x666688), 2.5f));
                wLoc.getWorld().spawnParticle(Particle.CLOUD, pl, 2, 0.5, 0.2, 0.5, 0.02);
            }
            MscEntityUtils.damageBy(witch, target, 8.0);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            wLoc.getWorld().playSound(wLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.5f);
            inst.cloudCooldown = 0;
        }
    }

    @EventHandler
    public void onPotionThrow(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof ThrownPotion potion)) return;
        if (!(potion.getShooter() instanceof Witch witch)) return;
        if (!witch.getScoreboardTags().contains(TAG)) return;
        if (!(witch.getTarget() instanceof Player target)) return;
        if (target.isDead() || !target.isOnline()) return;

        Location wLoc = witch.getLocation();
        Location tLoc = target.getLocation();
        for (int i = 0; i < 2; i++) {
            double ox = (random.nextDouble() - 0.5) * 4;
            double oz = (random.nextDouble() - 0.5) * 4;
            Location strikeLoc = tLoc.clone().add(ox, 0, oz);
            strikeLoc.setY(wLoc.getWorld().getHighestBlockYAt(strikeLoc));
            wLoc.getWorld().strikeLightningEffect(strikeLoc);
            wLoc.getWorld().playSound(strikeLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.9f);
            for (Player p : wLoc.getWorld().getPlayers()) {
                if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
                if (p.getLocation().distanceSquared(strikeLoc) < 16) {
                    MscEntityUtils.damageBy(witch, p, 10.0);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MscEntityUtils.applyDeathMessage(plugin, event, TAG, "entities.storm-caller.death-messages");
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Witch witch)) return;
        if (!witch.getScoreboardTags().contains(TAG)) return;
        active.remove(witch.getUniqueId());
        event.getDrops().clear();
        if (Math.random() < dropChance) {
            witch.getWorld().dropItemNaturally(witch.getLocation(), StormCrystal.STORM_CRYSTAL.clone());
        }
        event.setDroppedExp(50);
    }

    private static class StormCallerInstance {
        final Witch witch;
        int cloudCooldown = 0;

        StormCallerInstance(Witch w) {
            this.witch = w;
        }
    }
}