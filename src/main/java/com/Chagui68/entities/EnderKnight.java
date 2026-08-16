package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.EnderFragment;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class EnderKnight implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, EnderKnightInstance> active = new HashMap<>();
    private static final String TAG = "MSC_EnderKnight";

    public EnderKnight(MultiverseCreatures plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Enderman em : world.getEntitiesByClass(Enderman.class)) {
                if (em.getScoreboardTags().contains(TAG)) {
                    active.put(em.getUniqueId(), new EnderKnightInstance(em));
                }
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : new HashMap<>(active).entrySet()) {
                    EnderKnightInstance inst = entry.getValue();
                    if (inst.enderman.isDead() || !inst.enderman.isValid()) {
                        active.remove(entry.getKey());
                        continue;
                    }
                    tickEnder(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public boolean trySpawn(Location location) {
        Enderman em = (Enderman) location.getWorld().spawnEntity(location, EntityType.ENDERMAN);
        if (em == null) return false;
        em.addScoreboardTag(TAG);
        em.setCustomName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Ender Knight");
        em.setCustomNameVisible(true);
        em.setPersistent(true);
        em.setRemoveWhenFarAway(false);
        MscEntityUtils.setAttribute(em, Attribute.MAX_HEALTH, 120.0);
        em.setHealth(120.0);
        MscEntityUtils.setAttribute(em, Attribute.ATTACK_DAMAGE, 14.0);
        MscEntityUtils.setAttribute(em, Attribute.MOVEMENT_SPEED, 0.3);
        em.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        EntityEquipment eq = em.getEquipment();
        if (eq != null) {
            ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
            ItemMeta meta = sword.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.SHARPNESS, 5, true);
                meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
                meta.setItemName("Ender Blade");
                sword.setItemMeta(meta);
            }
            eq.setItemInMainHand(sword);
            eq.setItemInMainHandDropChance(0);
        }
        em.setAI(true);
        active.put(em.getUniqueId(), new EnderKnightInstance(em));
        return true;
    }

    private void tickEnder(EnderKnightInstance inst) {
        Enderman em = inst.enderman;
        if (!(em.getTarget() instanceof Player target)) return;
        if (target.isDead() || !target.isOnline()) return;
        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            em.setTarget(null);
            return;
        }

        Location eLoc = em.getLocation();
        Location tLoc = target.getLocation();
        double dist = eLoc.distance(tLoc);
        inst.enderPullCooldown++;
        inst.enderRushCooldown++;

        if (dist > 5 && dist < 25 && inst.enderPullCooldown > 60) {
            target.setVelocity(eLoc.toVector().subtract(tLoc.toVector()).normalize().multiply(0.8));
            MscEntityUtils.damageBy(em, target, 4.0);
            for (int i = 0; i < 8; i++) {
                Location pl = tLoc.clone().add((random.nextDouble() - 0.5) * 3, random.nextDouble() * 2, (random.nextDouble() - 0.5) * 3);
                eLoc.getWorld().spawnParticle(Particle.PORTAL, pl, 3, 0.1, 0.1, 0.1, 0.02);
                eLoc.getWorld().spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
            }
            eLoc.getWorld().playSound(tLoc, Sound.ENTITY_ENDERMAN_SCREAM, 0.8f, 0.7f);
            inst.enderPullCooldown = 0;
        }

        if (dist > 8 && inst.enderRushCooldown > 40) {
            Location behind = tLoc.clone().add(tLoc.getDirection().multiply(-2));
            behind.setY(tLoc.getY());
            em.teleport(behind);
            eLoc.getWorld().spawnParticle(Particle.PORTAL, eLoc, 20, 0.5, 1, 0.5, 0.05);
            eLoc.getWorld().spawnParticle(Particle.PORTAL, behind, 20, 0.5, 1, 0.5, 0.05);
            eLoc.getWorld().playSound(behind, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            MscEntityUtils.damageBy(em, target, 8.0);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
            inst.enderRushCooldown = 0;
        }

        if (dist < 4) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 0));
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Enderman em)) return;
        if (!em.getScoreboardTags().contains(TAG)) return;
        if (event.getEntity() instanceof Player p) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 30, 0));
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Enderman em)) return;
        if (!em.getScoreboardTags().contains(TAG)) return;
        active.remove(em.getUniqueId());
        event.getDrops().clear();
        if (Math.random() < 0.55) {
            em.getWorld().dropItemNaturally(em.getLocation(), EnderFragment.ENDER_FRAGMENT.clone());
        }
        event.setDroppedExp(70);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MscEntityUtils.applyDeathMessage(plugin, event, TAG, "ender-knight.death-messages");
    }

    private static class EnderKnightInstance {
        final Enderman enderman;
        int enderPullCooldown = 0;
        int enderRushCooldown = 0;

        EnderKnightInstance(Enderman em) {
            this.enderman = em;
        }
    }
}