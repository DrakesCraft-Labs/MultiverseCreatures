package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.ShadowCloak;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ShadowRogue implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, ShadowRogueInstance> activeRogues = new HashMap<>();
    private static final String TAG = "MSC_ShadowRogue";
    private double dropChance;

    public ShadowRogue(MultiverseCreatures plugin) {
        this.plugin = plugin;
        dropChance = plugin.getConfig().getDouble("entities.shadow-rogue.drop-chance", 0.5);
        if (!plugin.isEnabled("entities.shadow-rogue")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Skeleton sk : world.getEntitiesByClass(Skeleton.class)) {
                if (sk.getScoreboardTags().contains(TAG)) {
                    activeRogues.put(sk.getUniqueId(), new ShadowRogueInstance(sk));
                }
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : new HashMap<>(activeRogues).entrySet()) {
                    ShadowRogueInstance inst = entry.getValue();
                    if (inst.skeleton.isDead() || !inst.skeleton.isValid()) {
                        activeRogues.remove(entry.getKey());
                        continue;
                    }
                    tickRogue(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.shadow-rogue")) return false;
        Skeleton sk = (Skeleton) location.getWorld().spawnEntity(location, EntityType.SKELETON);
        if (sk == null) return false;
        sk.addScoreboardTag(TAG);
        sk.setCustomName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Shadow Rogue");
        sk.setCustomNameVisible(true);
        sk.setPersistent(true);
        sk.setRemoveWhenFarAway(false);
        MscEntityUtils.setAttribute(sk, Attribute.MAX_HEALTH, 60.0);
        sk.setHealth(60.0);
        MscEntityUtils.setAttribute(sk, Attribute.MOVEMENT_SPEED, 0.35);
        sk.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        EntityEquipment eq = sk.getEquipment();
        if (eq != null) {
            ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
            ItemMeta meta = sword.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.SHARPNESS, 4, true);
                meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
                meta.setItemName("Shadow Blade");
                sword.setItemMeta(meta);
            }
            eq.setItemInMainHand(sword);
            eq.setItemInMainHandDropChance(0);
            eq.setHelmet(new ItemStack(Material.BLACK_STAINED_GLASS));
            eq.setHelmetDropChance(0);
        }
        sk.setAI(true);
        activeRogues.put(sk.getUniqueId(), new ShadowRogueInstance(sk));
        return true;
    }

    private void tickRogue(ShadowRogueInstance inst) {
        Skeleton sk = inst.skeleton;
        if (!(sk.getTarget() instanceof Player target)) return;
        if (target.isDead() || !target.isOnline()) return;
        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            sk.setTarget(null);
            return;
        }

        Location skLoc = sk.getLocation();
        Location tLoc = target.getLocation();
        double distSq = skLoc.distanceSquared(tLoc);
        inst.teleportCooldown++;

        if (distSq > 25 && inst.teleportCooldown > 60) {
            Location behind = tLoc.clone().add(tLoc.getDirection().multiply(-2));
            behind.setY(tLoc.getY());
            sk.teleport(behind);
            skLoc.getWorld().spawnParticle(Particle.PORTAL, skLoc, 15, 0.5, 1, 0.5, 0.05);
            skLoc.getWorld().spawnParticle(Particle.PORTAL, behind, 15, 0.5, 1, 0.5, 0.05);
            skLoc.getWorld().playSound(behind, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.3f);
            inst.teleportCooldown = 0;
            sk.setTarget(target);
        }

        if (distSq < 9 && inst.backstabCooldown > 40) {
            Vector dirToTarget = tLoc.toVector().subtract(skLoc.toVector());
            Vector targetDir = tLoc.getDirection();
            double dot = dirToTarget.normalize().dot(targetDir);
            if (dot > 0.7) {
                double dmg = 18.0;
                MscEntityUtils.damageBy(sk, target, dmg);
                target.setVelocity(targetDir.multiply(-1.5).setY(0.5));
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                tLoc.getWorld().spawnParticle(Particle.CRIT, tLoc.add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                tLoc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, tLoc, 5, 0, 0, 0, 0);
                tLoc.getWorld().playSound(tLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.2f, 0.8f);
                inst.backstabCooldown = 0;
            }
        }
        inst.backstabCooldown++;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MscEntityUtils.applyDeathMessage(plugin, event, TAG, "entities.shadow-rogue.death-messages");
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Skeleton sk)) return;
        if (!sk.getScoreboardTags().contains(TAG)) return;
        activeRogues.remove(sk.getUniqueId());
        event.getDrops().clear();
        if (Math.random() < dropChance) {
            sk.getWorld().dropItemNaturally(sk.getLocation(), ShadowCloak.SHADOW_CLOAK.clone());
        }
        event.setDroppedExp(30);
    }

    private static class ShadowRogueInstance {
        final Skeleton skeleton;
        int teleportCooldown = 0;
        int backstabCooldown = 0;

        ShadowRogueInstance(Skeleton sk) {
            this.skeleton = sk;
        }
    }
}