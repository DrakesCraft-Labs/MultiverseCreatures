package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class Warlord implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private static final String TAG = "MSC_Warlord";
    private static final double TRUE_DAMAGE = 4.0;
    private boolean debug;

    public Warlord(MultiverseCreatures plugin) {
        this.plugin = plugin;
        reloadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        reloadExisting();
    }

    public void reloadConfig() {
        debug = plugin.getConfig().getBoolean("warlord.debug", false);
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Pillager pillager : world.getEntitiesByClass(Pillager.class)) {
                if (pillager.getScoreboardTags().contains(TAG)) {
                    customize(pillager);
                }
            }
        }
    }

    public boolean trySpawn(Location location) {
        Pillager pillager = (Pillager) location.getWorld().spawnEntity(location, EntityType.PILLAGER);
        if (pillager == null) return false;
        customize(pillager);
        if (debug) plugin.getLogger().info("[Warlord] Spawned Warlord at " + location);
        return true;
    }

    public boolean convertExisting(Pillager pillager) {
        if (pillager == null || pillager.isDead() || !pillager.isValid()) return false;
        customize(pillager);
        if (debug) plugin.getLogger().info("[Warlord] Converted raider to Warlord at " + pillager.getLocation());
        return true;
    }

    private void customize(Pillager pillager) {
        pillager.addScoreboardTag(TAG);
        pillager.setCustomName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Warlord");
        pillager.setCustomNameVisible(true);
        pillager.setPersistent(true);
        pillager.setRemoveWhenFarAway(false);
        if (pillager.getAttribute(Attribute.MAX_HEALTH) != null) {
            pillager.getAttribute(Attribute.MAX_HEALTH).setBaseValue(50.0);
        }
        pillager.setHealth(50.0);
        if (pillager.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            pillager.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.32);
        }
        if (pillager.getEquipment() != null) {
            ItemStack crossbow = new ItemStack(Material.CROSSBOW);
            crossbow.addUnsafeEnchantment(Enchantment.PIERCING, 4);
            crossbow.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 3);
            pillager.getEquipment().setItemInMainHand(crossbow);
            pillager.getEquipment().setItemInMainHandDropChance(0);
        }
        pillager.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 999999, 0, false, false));
        pillager.setAI(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Pillager warlord = null;
        if (event.getDamager() instanceof Pillager pillager && pillager.getScoreboardTags().contains(TAG)) {
            warlord = pillager;
        } else if (event.getDamager() instanceof AbstractArrow arrow
                && arrow.getShooter() instanceof Pillager pillager && pillager.getScoreboardTags().contains(TAG)) {
            warlord = pillager;
        }
        if (warlord == null) return;

        event.setCancelled(true);
        player.damage(TRUE_DAMAGE, DamageSource.builder(DamageType.OUT_OF_WORLD)
                .withDirectEntity(warlord)
                .withCausingEntity(warlord)
                .build());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Pillager pillager)) return;
        if (!pillager.getScoreboardTags().contains(TAG)) return;
        List<String> messages = plugin.getConfig().getStringList("warlord.death-messages");
        if (messages.isEmpty()) return;
        String raw = messages.get(random.nextInt(messages.size()));
        event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', raw.replace("%player%", event.getEntity().getName())));
    }
}