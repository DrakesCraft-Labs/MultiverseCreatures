package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.ReinforcedBone;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BoneShield implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, BoneShieldInstance> active = new HashMap<>();
    private static final String TAG = "MSC_BoneShield";
    private double shieldEquipChance;
    private double dropChance;

    public BoneShield(MultiverseCreatures plugin) {
        this.plugin = plugin;
        shieldEquipChance = plugin.getConfig().getDouble("entities.bone-shield.shield-equip-chance", 0.05);
        dropChance = plugin.getConfig().getDouble("entities.bone-shield.drop-chance", 0.8);
        if (!plugin.isEnabled("entities.bone-shield")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Skeleton sk : world.getEntitiesByClass(Skeleton.class)) {
                if (sk.getScoreboardTags().contains(TAG)) {
                    active.put(sk.getUniqueId(), new BoneShieldInstance(sk));
                }
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : new HashMap<>(active).entrySet()) {
                    BoneShieldInstance inst = entry.getValue();
                    if (inst.skeleton.isDead() || !inst.skeleton.isValid()) {
                        active.remove(entry.getKey());
                        continue;
                    }
                    tickBone(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.bone-shield")) return false;
        Skeleton sk = (Skeleton) location.getWorld().spawnEntity(location, EntityType.SKELETON);
        if (sk == null) return false;
        sk.addScoreboardTag(TAG);
        sk.setCustomName(ChatColor.WHITE + "" + ChatColor.BOLD + "Bone Shield");
        sk.setCustomNameVisible(true);
        sk.setPersistent(true);
        sk.setRemoveWhenFarAway(false);
        MscEntityUtils.setAttribute(sk, Attribute.MAX_HEALTH, 120.0);
        sk.setHealth(120.0);
        MscEntityUtils.setAttribute(sk, Attribute.MOVEMENT_SPEED, 0.2);
        sk.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        EntityEquipment eq = sk.getEquipment();
        if (eq != null) {
            ItemStack shield = new ItemStack(Material.SHIELD);
            ItemMeta shieldMeta = shield.getItemMeta();
            if (shieldMeta != null) {
                shieldMeta.setUnbreakable(true);
                shieldMeta.setItemName("Bone Wall");
                shield.setItemMeta(shieldMeta);
            }
            eq.setItemInOffHand(shield);
            eq.setItemInOffHandDropChance(0);

            ItemStack helmet = new ItemStack(Material.BONE_BLOCK);
            eq.setHelmet(helmet);
            eq.setHelmetDropChance(0);

            Color boneColor = Color.fromRGB(0xE8E0D0);
            ItemStack chest = coloredLeather(Material.LEATHER_CHESTPLATE, boneColor);
            ItemStack legs = coloredLeather(Material.LEATHER_LEGGINGS, boneColor);
            ItemStack boots = coloredLeather(Material.LEATHER_BOOTS, boneColor);
            eq.setChestplate(chest);
            eq.setChestplateDropChance(0);
            eq.setLeggings(legs);
            eq.setLeggingsDropChance(0);
            eq.setBoots(boots);
            eq.setBootsDropChance(0);
        }
        sk.setAI(true);
        active.put(sk.getUniqueId(), new BoneShieldInstance(sk));
        return true;
    }

    private void tickBone(BoneShieldInstance inst) {
        Skeleton sk = inst.skeleton;
        if (!(sk.getTarget() instanceof Player target)) return;
        if (target.isDead() || !target.isOnline()) return;
        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            sk.setTarget(null);
            return;
        }

        Location sLoc = sk.getLocation();
        inst.shieldRecharge++;

        if (inst.boneShieldHealth <= 0 && inst.shieldRecharge > 100) {
            inst.boneShieldHealth = 30;
            inst.shieldRecharge = 0;
            sLoc.getWorld().spawnParticle(Particle.EXPLOSION, sLoc.clone().add(0, 1, 0), 5, 1, 1, 1, 0);
            sLoc.getWorld().playSound(sLoc, Sound.ITEM_SHIELD_BLOCK, 1.5f, 1.8f);
        }

        if (inst.boneShieldHealth > 0) {
            for (int a = 0; a < 8; a++) {
                double angle = (2 * Math.PI * a / 8) + inst.shieldRecharge * 0.02;
                double r = 2.0;
                double x = sLoc.getX() + Math.cos(angle) * r;
                double z = sLoc.getZ() + Math.sin(angle) * r;
                Location pl = new Location(sLoc.getWorld(), x, sLoc.getY() + 1 + Math.sin(angle * 2) * 0.5, z);
                sLoc.getWorld().spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(0xEEEEEE), 1.5f));
            }
        }

        if (random.nextDouble() < shieldEquipChance && inst.boneShieldHealth > 0) {
            EntityEquipment eq = sk.getEquipment();
            if (eq != null && eq.getItemInOffHand().getType() == Material.AIR) {
                ItemStack shield = new ItemStack(Material.SHIELD);
                ItemMeta sm = shield.getItemMeta();
                if (sm != null) {
                    sm.setUnbreakable(true);
                    shield.setItemMeta(sm);
                }
                eq.setItemInOffHand(shield);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Skeleton sk)) return;
        if (!sk.getScoreboardTags().contains(TAG)) return;
        BoneShieldInstance inst = active.get(sk.getUniqueId());
        if (inst == null) return;

        if (inst.boneShieldHealth > 0) {
            double reduction = Math.min(event.getFinalDamage() * 0.6, inst.boneShieldHealth);
            inst.boneShieldHealth -= reduction;
            event.setDamage(event.getDamage() * 0.4);
            sk.getWorld().spawnParticle(Particle.BLOCK, sk.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.05, Material.BONE_BLOCK.createBlockData());
            sk.getWorld().playSound(sk.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.8f, 1.2f);
            if (event.getDamager() instanceof Player p) {
                MscEntityUtils.damageBy(sk, p, 3.0);
                p.getWorld().spawnParticle(Particle.CRIT, p.getLocation().add(0, 1, 0), 5, 0.2, 0.3, 0.2, 0.02);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MscEntityUtils.applyDeathMessage(plugin, event, TAG, "entities.bone-shield.death-messages");
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Skeleton sk)) return;
        if (!sk.getScoreboardTags().contains(TAG)) return;
        active.remove(sk.getUniqueId());
        event.getDrops().clear();
        if (Math.random() < dropChance) {
            sk.getWorld().dropItemNaturally(sk.getLocation(), ReinforcedBone.REINFORCED_BONE.clone());
        }
        event.setDroppedExp(40);
    }

    private ItemStack coloredLeather(Material mat, Color color) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static class BoneShieldInstance {
        final Skeleton skeleton;
        double boneShieldHealth = 30;
        int shieldRecharge = 0;

        BoneShieldInstance(Skeleton sk) {
            this.skeleton = sk;
        }
    }
}