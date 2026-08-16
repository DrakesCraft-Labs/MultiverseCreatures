package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.VenomGland;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Witch;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class VenomWitch implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, VenomWitchInstance> active = new HashMap<>();
    private static final String TAG = "MSC_VenomWitch";

    public VenomWitch(MultiverseCreatures plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Witch w : world.getEntitiesByClass(Witch.class)) {
                if (w.getScoreboardTags().contains(TAG)) {
                    active.put(w.getUniqueId(), new VenomWitchInstance(w));
                }
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : new HashMap<>(active).entrySet()) {
                    VenomWitchInstance inst = entry.getValue();
                    if (inst.witch.isDead() || !inst.witch.isValid()) {
                        active.remove(entry.getKey());
                        continue;
                    }
                    tickVenom(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public boolean trySpawn(Location location) {
        Witch witch = (Witch) location.getWorld().spawnEntity(location, EntityType.WITCH);
        if (witch == null) return false;
        witch.setPersistent(true);
        witch.setRemoveWhenFarAway(false);
        customize(witch);
        return true;
    }

    public boolean convertExisting(Witch witch) {
        if (witch == null || witch.isDead() || !witch.isValid()) return false;
        customize(witch);
        return true;
    }

    private void customize(Witch witch) {
        witch.addScoreboardTag(TAG);
        witch.setCustomName(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Venom Witch");
        witch.setCustomNameVisible(true);
        MscEntityUtils.setAttribute(witch, Attribute.MAX_HEALTH, 50.0);
        witch.setHealth(50.0);
        MscEntityUtils.setAttribute(witch, Attribute.MOVEMENT_SPEED, 0.25);
        witch.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        witch.setAI(true);
        active.put(witch.getUniqueId(), new VenomWitchInstance(witch));
    }

    private void tickVenom(VenomWitchInstance inst) {
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
        inst.debuffCooldown++;

        if (inst.cloudCooldown > 80) {
            Location cloudLoc = tLoc.clone().add((random.nextDouble() - 0.5) * 4, 0, (random.nextDouble() - 0.5) * 4);
            AreaEffectCloud cloud = (AreaEffectCloud) wLoc.getWorld().spawnEntity(cloudLoc, EntityType.AREA_EFFECT_CLOUD);
            if (cloud != null) {
                cloud.setRadius(3.5f);
                cloud.setDuration(100);
                cloud.setColor(Color.fromRGB(0x66FF00));
                cloud.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 60, 2), true);
                cloud.addCustomEffect(new PotionEffect(PotionEffectType.WITHER, 40, 1), true);
                cloud.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1), true);
                cloud.setSource(witch);
            }
            for (int a = 0; a < 15; a++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double r = random.nextDouble() * 3;
                Location pl = cloudLoc.clone().add(Math.cos(angle) * r, random.nextDouble() * 2, Math.sin(angle) * r);
                wLoc.getWorld().spawnParticle(Particle.DUST, pl, 2, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(0x66FF00), 1.8f));
                wLoc.getWorld().spawnParticle(Particle.WITCH, pl, 1, 0, 0, 0, 0);
            }
            wLoc.getWorld().playSound(wLoc, Sound.ENTITY_WITCH_DRINK, 1.0f, 0.6f);
            inst.cloudCooldown = 0;
        }

        if (inst.debuffCooldown > 50) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
            target.damage(4.0);
            tLoc.getWorld().spawnParticle(Particle.WITCH, tLoc.add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0);
            wLoc.getWorld().playSound(tLoc, Sound.ENTITY_WITCH_THROW, 1.0f, 1.2f);
            inst.debuffCooldown = 0;
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Witch witch)) return;
        if (!witch.getScoreboardTags().contains(TAG)) return;
        active.remove(witch.getUniqueId());
        event.getDrops().clear();
        if (Math.random() < 0.6) {
            witch.getWorld().dropItemNaturally(witch.getLocation(), VenomGland.VENOM_GLAND.clone());
        }
        event.setDroppedExp(30);
    }

    private static class VenomWitchInstance {
        final Witch witch;
        int cloudCooldown = 0;
        int debuffCooldown = 0;

        VenomWitchInstance(Witch w) {
            this.witch = w;
        }
    }
}