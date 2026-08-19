package com.Chagui68.entities.miniboss;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Garou - Cazador de Héroes y Guerrero Cósmico.
 * Miniboss de alta agilidad, artes marciales y contraataques rápidos.
 */
public class GarouBoss implements Listener {

    private final MultiverseCreatures plugin;
    private final Map<UUID, Long> lastCounterTime = new HashMap<>();
    private final Map<UUID, Long> lastSkillTime = new HashMap<>();
    private final Random random = new Random();

    public GarouBoss(MultiverseCreatures plugin) {
        this.plugin = plugin;
        if (plugin.isEnabled("entities.garou")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }
    }

    public boolean trySpawn(Location loc) {
        if (!plugin.isEnabled("entities.garou")) return false;
        if (loc == null || loc.getWorld() == null) return false;

        // Comprobar densidad de Garou en un radio de 64 bloques
        boolean hasNearby = !loc.getWorld().getNearbyEntities(loc, 64, 32, 64, 
                e -> e.getScoreboardTags().contains("MSC_Garou")).isEmpty();
        if (hasNearby) return false;

        WitherSkeleton garou = (WitherSkeleton) loc.getWorld().spawnEntity(loc, EntityType.WITHER_SKELETON);
        garou.addScoreboardTag("MSC_Garou");
        garou.setCustomName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Garou " + ChatColor.LIGHT_PURPLE + "[Cazador de Héroes]");
        garou.setCustomNameVisible(true);
        garou.setRemoveWhenFarAway(true);
        garou.setCanPickupItems(false);

        // Atributos de jefe
        AttributeInstance maxHealth = garou.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(1800.0);
            garou.setHealth(1800.0);
        }

        AttributeInstance speed = garou.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(0.38);
        }

        AttributeInstance attack = garou.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attack != null) {
            attack.setBaseValue(28.0);
        }

        AttributeInstance knockbackResist = garou.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (knockbackResist != null) {
            knockbackResist.setBaseValue(0.9);
        }

        // Equipamiento visual distintivo (Túnica oscura de artes marciales)
        EntityEquipment eq = garou.getEquipment();
        if (eq != null) {
            ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
            LeatherArmorMeta cm = (LeatherArmorMeta) chest.getItemMeta();
            if (cm != null) {
                cm.setColor(Color.fromRGB(30, 20, 45)); // Púrpura oscuro cósmico
                chest.setItemMeta(cm);
            }
            eq.setChestplate(chest);

            ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS);
            LeatherArmorMeta lm = (LeatherArmorMeta) legs.getItemMeta();
            if (lm != null) {
                lm.setColor(Color.fromRGB(20, 20, 25));
                legs.setItemMeta(lm);
            }
            eq.setLeggings(legs);

            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            LeatherArmorMeta bm = (LeatherArmorMeta) boots.getItemMeta();
            if (bm != null) {
                bm.setColor(Color.fromRGB(120, 40, 200));
                boots.setItemMeta(bm);
            }
            eq.setBoots(boots);

            eq.setItemInMainHand(new ItemStack(Material.AIR));
            eq.setItemInOffHand(new ItemStack(Material.AIR));
        }

        garou.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60 * 60, 0, false, false));
        garou.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 60, 1, false, false));

        startGarouAITask(garou);
        return true;
    }

    private void startGarouAITask(WitherSkeleton garou) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (garou == null || !garou.isValid() || garou.isDead()) {
                    cancel();
                    return;
                }

                Location gLoc = garou.getLocation();
                World w = gLoc.getWorld();
                if (w == null) return;

                // Efecto de aura cósmica constante
                w.spawnParticle(Particle.PORTAL, gLoc.clone().add(0, 1.0, 0), 6, 0.3, 0.6, 0.3, 0.05);

                LivingEntity target = garou.getTarget();
                if (target == null || !target.isValid() || (target instanceof Player p && !MscEntityUtils.isValidTarget(p))) {
                    Player nearest = findNearestValidPlayer(garou, 28.0);
                    if (nearest != null) {
                        garou.setTarget(nearest);
                    }
                    return;
                }

                double distanceSq = gLoc.distanceSquared(target.getLocation());
                long now = System.currentTimeMillis();
                long lastSkill = lastSkillTime.getOrDefault(garou.getUniqueId(), 0L);

                // Habilidad 1: Ráfaga de Acercamiento / Teletransporte Rápido si el objetivo huye
                if (distanceSq > 64.0 && now - lastSkill > 7000L) {
                    lastSkillTime.put(garou.getUniqueId(), now);
                    Location tLoc = target.getLocation();
                    w.spawnParticle(Particle.REVERSE_PORTAL, gLoc.clone().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                    w.playSound(gLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 0.8f);

                    Vector dir = tLoc.toVector().subtract(gLoc.toVector()).normalize().multiply(1.5);
                    dir.setY(0.35);
                    garou.setVelocity(dir);

                    w.spawnParticle(Particle.SWEEP_ATTACK, tLoc.clone().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.1);
                    return;
                }

                // Habilidad 2: Corriente de Puño de Agua Rompedor de Rocas (Water Stream Rock Smashing Fist)
                if (distanceSq <= 25.0 && now - lastSkill > 5000L) {
                    lastSkillTime.put(garou.getUniqueId(), now);
                    w.playSound(gLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.4f);
                    w.spawnParticle(Particle.SPLASH, gLoc.clone().add(0, 1.0, 0), 30, 0.6, 0.4, 0.6, 0.2);
                    w.spawnParticle(Particle.SOUL_FIRE_FLAME, gLoc.clone().add(0, 1.0, 0), 15, 0.4, 0.4, 0.4, 0.05);

                    for (Entity nearby : garou.getNearbyEntities(4.0, 3.0, 4.0)) {
                        if (nearby instanceof LivingEntity le && !(nearby instanceof WitherSkeleton)) {
                            le.damage(16.0, garou);
                            le.setVelocity(le.getLocation().toVector().subtract(gLoc.toVector()).normalize().multiply(0.8).setY(0.3));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private Player findNearestValidPlayer(WitherSkeleton garou, double radius) {
        Player nearest = null;
        double nearestDistSq = radius * radius;
        Location loc = garou.getLocation();

        for (Entity e : garou.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Player p && MscEntityUtils.isValidTarget(p)) {
                double distSq = loc.distanceSquared(p.getLocation());
                if (distSq < nearestDistSq) {
                    nearestDistSq = distSq;
                    nearest = p;
                }
            }
        }
        return nearest;
    }

    @EventHandler
    public void onGarouDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof WitherSkeleton garou)) return;
        if (!garou.getScoreboardTags().contains("MSC_Garou")) return;

        // Contraataque Marcial (Martial Counter): 25% de probabilidad de desviar y contraatacar
        long now = System.currentTimeMillis();
        long lastCounter = lastCounterTime.getOrDefault(garou.getUniqueId(), 0L);

        if (now - lastCounter > 3000L && random.nextDouble() < 0.25) {
            lastCounterTime.put(garou.getUniqueId(), now);
            event.setCancelled(true);

            Location loc = garou.getLocation();
            World w = loc.getWorld();
            if (w != null) {
                w.playSound(loc, Sound.ITEM_SHIELD_BLOCK, 1.5f, 1.2f);
                w.spawnParticle(Particle.CRIT, loc.clone().add(0, 1.2, 0), 20, 0.4, 0.4, 0.4, 0.2);
            }

            if (event.getDamager() instanceof LivingEntity damager) {
                damager.damage(14.0, garou);
                damager.sendMessage(ChatColor.DARK_PURPLE + "[Garou]" + ChatColor.GRAY + " ¡Tu golpe fue desviado por el Puño de Agua!");
                damager.setVelocity(damager.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(0.9).setY(0.3));
            }
        }
    }

    @EventHandler
    public void onGarouDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof WitherSkeleton garou)) return;
        if (!garou.getScoreboardTags().contains("MSC_Garou")) return;

        event.getDrops().clear();
        event.setDroppedExp(500);

        Location loc = garou.getLocation();
        World w = loc.getWorld();
        if (w != null) {
            w.spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 1, 0), 80, 0.8, 0.8, 0.8, 0.4);
            w.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 0.8f);

            // Recompensa: Núcleo Cósmico de Garou / Esencia Marcial
            ItemStack core = new ItemStack(Material.NETHER_STAR);
            var meta = core.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ Núcleo Cósmico de Garou");
                meta.setLore(List.of(
                        ChatColor.GRAY + "Fragmento de poder primordial de las artes marciales.",
                        ChatColor.LIGHT_PURPLE + "Reliquia de los Dioses de DrakesCraft."
                ));
                core.setItemMeta(meta);
            }
            w.dropItemNaturally(loc, core);
        }

        // Anuncio a jugadores cercanos
        for (Entity e : garou.getNearbyEntities(40, 20, 40)) {
            if (e instanceof Player p) {
                p.sendMessage(ChatColor.GOLD + "✦ " + ChatColor.DARK_PURPLE + "¡Garou ha sido derrotado!");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MscEntityUtils.applyDeathMessage(plugin, event, "MSC_Garou", "entities.garou.death-messages");
    }
}
