package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.ObsidianShard;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
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

public class ObsidianGuard implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, ObsidianGuardInstance> active = new HashMap<>();
    private static final String TAG = "MSC_ObsidianGuard";

    public ObsidianGuard(MultiverseCreatures plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Zombie z : world.getEntitiesByClass(Zombie.class)) {
                if (z.getScoreboardTags().contains(TAG)) {
                    active.put(z.getUniqueId(), new ObsidianGuardInstance(z));
                }
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : new HashMap<>(active).entrySet()) {
                    ObsidianGuardInstance inst = entry.getValue();
                    if (inst.zombie.isDead() || !inst.zombie.isValid()) {
                        active.remove(entry.getKey());
                        continue;
                    }
                    tickGuard(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public boolean trySpawn(Location location) {
        Zombie zombie = (Zombie) location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
        if (zombie == null) return false;
        zombie.setBaby(false);
        zombie.addScoreboardTag(TAG);
        zombie.setCustomName(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Obsidian Guard");
        zombie.setCustomNameVisible(true);
        zombie.setPersistent(true);
        zombie.setRemoveWhenFarAway(false);
        MscEntityUtils.setAttribute(zombie, Attribute.MAX_HEALTH, 300.0);
        zombie.setHealth(300.0);
        MscEntityUtils.setAttribute(zombie, Attribute.ATTACK_DAMAGE, 8.0);
        MscEntityUtils.setAttribute(zombie, Attribute.MOVEMENT_SPEED, 0.15);
        MscEntityUtils.setAttribute(zombie, Attribute.KNOCKBACK_RESISTANCE, 1.0);
        MscEntityUtils.setAttribute(zombie, Attribute.SCALE, 1.8);
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        zombie.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 999999, 1, false, false));
        EntityEquipment eq = zombie.getEquipment();
        if (eq != null) {
            eq.setHelmet(new ItemStack(Material.OBSIDIAN));
            eq.setHelmetDropChance(0);
            eq.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
            eq.setChestplateDropChance(0);
            eq.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
            eq.setLeggingsDropChance(0);
            eq.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
            eq.setBootsDropChance(0);
            ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
            ItemMeta meta = sword.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.SHARPNESS, 3, true);
                meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
                meta.setItemName("Obsidian Blade");
                sword.setItemMeta(meta);
            }
            eq.setItemInMainHand(sword);
            eq.setItemInMainHandDropChance(0);
        }
        zombie.setAI(true);
        active.put(zombie.getUniqueId(), new ObsidianGuardInstance(zombie));
        return true;
    }

    private void tickGuard(ObsidianGuardInstance inst) {
        Zombie zombie = inst.zombie;
        if (!(zombie.getTarget() instanceof Player target)) return;
        if (target.isDead() || !target.isOnline()) return;
        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            zombie.setTarget(null);
            return;
        }

        Location zLoc = zombie.getLocation();
        inst.tauntCooldown++;

        if (inst.tauntCooldown > 100) {
            for (Player p : zLoc.getWorld().getPlayers()) {
                if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
                if (p.getLocation().distanceSquared(zLoc) < 400) {
                    zombie.setTarget(p);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
                    p.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Obsidian Guard: " + ChatColor.GRAY + "Face me!");
                }
            }
            zLoc.getWorld().spawnParticle(Particle.EXPLOSION, zLoc.clone().add(0, 2, 0), 8, 2, 1, 2, 0);
            zLoc.getWorld().playSound(zLoc, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.5f, 0.5f);
            inst.tauntCooldown = 0;
        }

        if (zombie.getHealth() < 100 && inst.healCooldown > 200) {
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 2));
            zLoc.getWorld().spawnParticle(Particle.HEART, zLoc.clone().add(0, 2, 0), 10, 1, 0.5, 1, 0);
            zLoc.getWorld().playSound(zLoc, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 0.8f);
            inst.healCooldown = 0;
        }
        inst.healCooldown++;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Zombie zombie)) return;
        if (!zombie.getScoreboardTags().contains(TAG)) return;
        if (event.getEntity() instanceof Player p) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MscEntityUtils.applyDeathMessage(plugin, event, TAG, "obsidian-guard.death-messages");
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie)) return;
        if (!zombie.getScoreboardTags().contains(TAG)) return;
        active.remove(zombie.getUniqueId());
        event.getDrops().clear();
        if (Math.random() < 0.85) {
            zombie.getWorld().dropItemNaturally(zombie.getLocation(), ObsidianShard.OBSIDIAN_SHARD.clone());
        }
        event.setDroppedExp(100);
    }

    private static class ObsidianGuardInstance {
        final Zombie zombie;
        int tauntCooldown = 0;
        int healCooldown = 0;

        ObsidianGuardInstance(Zombie z) {
            this.zombie = z;
        }
    }
}