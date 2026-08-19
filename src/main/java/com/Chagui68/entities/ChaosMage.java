package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.ChaosOrb;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ChaosMage implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, ChaosMageInstance> active = new HashMap<>();
    private static final String TAG = "MSC_ChaosMage";
    private double dropChance;

    private static final List<PotionEffectType> DEBUFFS = List.of(
            PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.SLOWNESS,
            PotionEffectType.WEAKNESS, PotionEffectType.BLINDNESS, PotionEffectType.HUNGER,
            PotionEffectType.LEVITATION, PotionEffectType.DARKNESS
    );

    public ChaosMage(MultiverseCreatures plugin) {
        this.plugin = plugin;
        dropChance = plugin.getConfig().getDouble("entities.chaos-mage.drop-chance", 0.3);
        if (!plugin.isEnabled("entities.chaos-mage")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Evoker ev : world.getEntitiesByClass(Evoker.class)) {
                if (ev.getScoreboardTags().contains(TAG)) {
                    active.put(ev.getUniqueId(), new ChaosMageInstance(ev));
                }
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : new HashMap<>(active).entrySet()) {
                    ChaosMageInstance inst = entry.getValue();
                    if (inst.evoker.isDead() || !inst.evoker.isValid()) {
                        active.remove(entry.getKey());
                        continue;
                    }
                    tickChaos(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.chaos-mage")) return false;
        Evoker evoker = (Evoker) location.getWorld().spawnEntity(location, EntityType.EVOKER);
        if (evoker == null) return false;
        evoker.setPersistent(true);
        evoker.setRemoveWhenFarAway(false);
        customize(evoker);
        return true;
    }

    public boolean convertExisting(Evoker evoker) {
        if (!plugin.isEnabled("entities.chaos-mage")) return false;
        if (evoker == null || evoker.isDead() || !evoker.isValid()) return false;
        customize(evoker);
        return true;
    }

    private void customize(Evoker evoker) {
        evoker.addScoreboardTag(TAG);
        evoker.setCustomName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Chaos Mage");
        evoker.setCustomNameVisible(true);
        MscEntityUtils.setAttribute(evoker, Attribute.MAX_HEALTH, 70.0);
        evoker.setHealth(70.0);
        MscEntityUtils.setAttribute(evoker, Attribute.MOVEMENT_SPEED, 0.25);
        evoker.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        evoker.setAI(true);
        active.put(evoker.getUniqueId(), new ChaosMageInstance(evoker));
    }

    private void tickChaos(ChaosMageInstance inst) {
        Evoker evoker = inst.evoker;
        if (!(evoker.getTarget() instanceof Player target)) return;
        if (target.isDead() || !target.isOnline()) return;
        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            evoker.setTarget(null);
            return;
        }

        Location eLoc = evoker.getLocation();
        inst.chaosCastCooldown++;

        if (inst.chaosCastCooldown > 50) {
            int effect = random.nextInt(6);
            switch (effect) {
                case 0 -> {
                    if (evoker.getPassengers().isEmpty()) {
                        Vex vex = (Vex) eLoc.getWorld().spawnEntity(eLoc.clone().add(0, 2, 0), EntityType.VEX);
                        if (vex != null) {
                            vex.setCustomName(ChatColor.LIGHT_PURPLE + "Chaos Vex");
                            vex.setCustomNameVisible(true);
                            vex.setPersistent(true);
                            vex.setRemoveWhenFarAway(false);
                            vex.addScoreboardTag("MSC_ChaosVex");
                            vex.setTarget(target);
                            evoker.addPassenger(vex);
                        }
                    }
                }
                case 1 -> {
                    for (int a = 0; a < 12; a++) {
                        double angle = (2 * Math.PI * a / 12);
                        double r = 4.0;
                        Location pl = eLoc.clone().add(Math.cos(angle) * r, 0.5, Math.sin(angle) * r);
                        eLoc.getWorld().spawnParticle(Particle.FLAME, pl, 5, 0.5, 0.3, 0.5, 0.02);
                        eLoc.getWorld().spawnParticle(Particle.DUST, pl, 2, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0xFF4400), 2.0f));
                    }
                    target.setFireTicks(80);
                    MscEntityUtils.damageBy(evoker, target, 8.0);
                }
                case 2 -> {
                    for (int i = 0; i < 3; i++) {
                        int r = random.nextInt(5) + 2;
                        Location sl = eLoc.clone().add((random.nextDouble() - 0.5) * 6, 2, (random.nextDouble() - 0.5) * 6);
                        eLoc.getWorld().spawnEntity(sl, EntityType.CREEPER);
                    }
                    eLoc.getWorld().playSound(eLoc, Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 0.8f);
                }
                case 3 -> {
                    PotionEffectType debuff = DEBUFFS.get(random.nextInt(DEBUFFS.size()));
                    target.addPotionEffect(new PotionEffect(debuff, 120, 2));
                    MscEntityUtils.damageBy(evoker, target, 5.0);
                    target.getWorld().spawnParticle(Particle.WITCH, target.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 0);
                }
                case 4 -> {
                    for (int i = 0; i < 4; i++) {
                        double angle = (2 * Math.PI * i / 4);
                        Vector dir = new Vector(Math.cos(angle), 0.2, Math.sin(angle));
                        SmallFireball fb = eLoc.getWorld().spawn(eLoc.clone().add(0, 1.5, 0), SmallFireball.class);
                        if (fb != null) {
                            fb.setDirection(dir);
                            fb.setYield(0);
                        }
                    }
                }
                case 5 -> {
                    Location swapLoc = eLoc.clone();
                    evoker.teleport(target.getLocation().add(0, 1, 0));
                    target.teleport(swapLoc);
                    eLoc.getWorld().spawnParticle(Particle.PORTAL, swapLoc, 30, 0.5, 1, 0.5, 0.05);
                    eLoc.getWorld().spawnParticle(Particle.PORTAL, target.getLocation(), 30, 0.5, 1, 0.5, 0.05);
                    eLoc.getWorld().playSound(swapLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.7f);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0));
                }
            }
            eLoc.getWorld().spawnParticle(Particle.EXPLOSION, eLoc.clone().add(0, 1, 0), 5, 1, 0.5, 1, 0);
            inst.chaosCastCooldown = 0;
            inst.lastEffect = effect;
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Evoker evoker)) return;
        if (!evoker.getScoreboardTags().contains(TAG)) return;
        active.remove(evoker.getUniqueId());
        event.getDrops().clear();
        if (Math.random() < dropChance) {
            evoker.getWorld().dropItemNaturally(evoker.getLocation(), ChaosOrb.CHAOS_ORB.clone());
        }
        event.setDroppedExp(50);
        evoker.getWorld().spawnParticle(Particle.EXPLOSION, evoker.getLocation(), 15, 3, 2, 3, 0);
        evoker.getWorld().playSound(evoker.getLocation(), Sound.ENTITY_EVOKER_DEATH, 1.5f, 0.5f);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!MscEntityUtils.applyDeathMessage(plugin, event, TAG, "entities.chaos-mage.death-messages")) {
            MscEntityUtils.applyDeathMessage(plugin, event, "MSC_ChaosVex", "entities.chaos-mage.death-messages");
        }
    }

    private static class ChaosMageInstance {
        final Evoker evoker;
        int chaosCastCooldown = 0;
        int lastEffect = -1;

        ChaosMageInstance(Evoker e) {
            this.evoker = e;
        }
    }
}