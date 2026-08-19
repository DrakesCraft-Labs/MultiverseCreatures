package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.ReaperEssence;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
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
import org.bukkit.util.Vector;

import java.util.*;

public class SoulReaper implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, SoulReaperInstance> active = new HashMap<>();
    private static final String TAG = "MSC_SoulReaper";
    private double dropChance;

    public SoulReaper(MultiverseCreatures plugin) {
        this.plugin = plugin;
        dropChance = plugin.getConfig().getDouble("entities.soul-reaper.drop-chance", 0.6);
        if (!plugin.isEnabled("entities.soul-reaper")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTicker();
        reloadExisting();
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (WitherSkeleton ws : world.getEntitiesByClass(WitherSkeleton.class)) {
                if (ws.getScoreboardTags().contains(TAG)) {
                    active.put(ws.getUniqueId(), new SoulReaperInstance(ws));
                }
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : new HashMap<>(active).entrySet()) {
                    SoulReaperInstance inst = entry.getValue();
                    if (inst.skeleton.isDead() || !inst.skeleton.isValid()) {
                        active.remove(entry.getKey());
                        continue;
                    }
                    tickReaper(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.soul-reaper")) return false;
        WitherSkeleton ws = (WitherSkeleton) location.getWorld().spawnEntity(location, EntityType.WITHER_SKELETON);
        if (ws == null) return false;
        ws.addScoreboardTag(TAG);
        ws.setCustomName(ChatColor.BLACK + "" + ChatColor.BOLD + "Soul Reaper");
        ws.setCustomNameVisible(true);
        ws.setPersistent(true);
        ws.setRemoveWhenFarAway(false);
        MscEntityUtils.setAttribute(ws, Attribute.MAX_HEALTH, 100.0);
        ws.setHealth(100.0);
        MscEntityUtils.setAttribute(ws, Attribute.MOVEMENT_SPEED, 0.28);
        MscEntityUtils.setAttribute(ws, Attribute.FOLLOW_RANGE, 30.0);
        ws.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        EntityEquipment eq = ws.getEquipment();
        if (eq != null) {
            ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
            ItemMeta meta = axe.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.SHARPNESS, 5, true);
                meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
                meta.setItemName("Soul Reaper's Scythe");
                axe.setItemMeta(meta);
            }
            eq.setItemInMainHand(axe);
            eq.setItemInMainHandDropChance(0);
            eq.setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
            eq.setHelmetDropChance(0);

            ItemStack chest = coloredLeather(Material.LEATHER_CHESTPLATE, Color.fromRGB(0x1A1A1A));
            ItemStack legs = coloredLeather(Material.LEATHER_LEGGINGS, Color.fromRGB(0x1A1A1A));
            ItemStack boots = coloredLeather(Material.LEATHER_BOOTS, Color.fromRGB(0x1A1A1A));
            eq.setChestplate(chest);
            eq.setChestplateDropChance(0);
            eq.setLeggings(legs);
            eq.setLeggingsDropChance(0);
            eq.setBoots(boots);
            eq.setBootsDropChance(0);
        }
        ws.setAI(true);
        active.put(ws.getUniqueId(), new SoulReaperInstance(ws));
        return true;
    }

    private void tickReaper(SoulReaperInstance inst) {
        WitherSkeleton ws = inst.skeleton;
        if (!(ws.getTarget() instanceof Player target)) return;
        if (target.isDead() || !target.isOnline()) return;
        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            ws.setTarget(null);
            return;
        }

        Location wLoc = ws.getLocation();
        double dist = wLoc.distance(target.getLocation());
        inst.soulDrainCooldown++;

        if (dist < 8 && inst.soulDrainCooldown > 60) {
            double dmg = 10.0;
            MscEntityUtils.damageBy(ws, target, dmg);
            double heal = dmg * 0.5;
            ws.setHealth(Math.min(ws.getAttribute(Attribute.MAX_HEALTH).getValue(), ws.getHealth() + heal));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 2));
            target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 80, 2));
            Location tLoc = target.getLocation().add(0, 1, 0);
            for (int i = 0; i < 10; i++) {
                Location pl = tLoc.clone().add((random.nextDouble() - 0.5) * 1.5, random.nextDouble() * 2, (random.nextDouble() - 0.5) * 1.5);
                wLoc.getWorld().spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(0x440000), 2.0f));
            }
            Vector soul = wLoc.toVector().subtract(tLoc.toVector());
            for (double d = 0; d < 1; d += 0.1) {
                Location pl = tLoc.clone().add(soul.clone().multiply(d));
                wLoc.getWorld().spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                wLoc.getWorld().spawnParticle(Particle.PORTAL, pl, 1, 0, 0, 0, 0);
            }
            wLoc.getWorld().playSound(tLoc, Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.6f);
            inst.soulDrainCooldown = 0;
        }
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

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof WitherSkeleton ws)) return;
        if (!ws.getScoreboardTags().contains(TAG)) return;
        if (event.getEntity() instanceof Player p) {
            double heal = event.getFinalDamage() * 0.3;
            ws.setHealth(Math.min(ws.getAttribute(Attribute.MAX_HEALTH).getValue(), ws.getHealth() + heal));
            p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MscEntityUtils.applyDeathMessage(plugin, event, TAG, "entities.soul-reaper.death-messages");
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof WitherSkeleton ws)) return;
        if (!ws.getScoreboardTags().contains(TAG)) return;
        active.remove(ws.getUniqueId());
        event.getDrops().clear();
        if (Math.random() < dropChance) {
            ws.getWorld().dropItemNaturally(ws.getLocation(), ReaperEssence.REAPER_ESSENCE.clone());
        }
        event.setDroppedExp(60);
    }

    private static class SoulReaperInstance {
        final WitherSkeleton skeleton;
        int soulDrainCooldown = 0;

        SoulReaperInstance(WitherSkeleton ws) {
            this.skeleton = ws;
        }
    }
}