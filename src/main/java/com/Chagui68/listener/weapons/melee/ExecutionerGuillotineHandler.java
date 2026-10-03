package com.Chagui68.listener.weapons.melee;

import com.Chagui68.items.weapons.melee.ExecutionerGuillotine;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The Executioner's Guillotine: NIX's chains and leap in a player's hands.
 */
public class ExecutionerGuillotineHandler implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Long> chainCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> slamCooldowns = new ConcurrentHashMap<>();

    public ExecutionerGuillotineHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private double setting(String key, double fallback) {
        return plugin.getConfig().getDouble(key, fallback);
    }

    private static boolean isGuillotine(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer()
                .has(ExecutionerGuillotine.KEY, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !event.getAction().isRightClick()) {
            return;
        }
        Player player = event.getPlayer();
        if (!isGuillotine(player.getInventory().getItemInMainHand())) {
            return;
        }
        long now = System.currentTimeMillis();
        if (player.isSneaking()) {
            long until = slamCooldowns.getOrDefault(player.getUniqueId(), 0L);
            if (until > now) {
                player.sendActionBar(Component.text("Guillotine Drop on cooldown: "
                        + (until - now + 999) / 1000 + "s", NamedTextColor.RED));
                return;
            }
            slamCooldowns.put(player.getUniqueId(),
                    now + (long) setting("items.executioner-guillotine.slam-cooldown-ms", 25000));
            leap(player);
            return;
        }
        long until = chainCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (until > now) {
            player.sendActionBar(Component.text("Chains of Judgment on cooldown: "
                    + (until - now + 999) / 1000 + "s", NamedTextColor.RED));
            return;
        }
        double range = setting("items.executioner-guillotine.chains-range", 18.0);
        RayTraceResult ray = player.getWorld().rayTraceEntities(player.getEyeLocation(),
                player.getEyeLocation().getDirection(), range, 0.6,
                entity -> entity instanceof LivingEntity && !entity.equals(player));
        if (ray == null || !(ray.getHitEntity() instanceof LivingEntity target)) {
            return;
        }
        chainCooldowns.put(player.getUniqueId(),
                now + (long) setting("items.executioner-guillotine.chains-cooldown-ms", 15000));
        chain(player, target);
    }

    /** Chains of Judgment: spectral chains drag the target to the executioner. */
    private void chain(Player player, LivingEntity target) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHAIN_PLACE, 1.2f, 0.6f);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
        Location from = player.getEyeLocation();
        Vector line = target.getLocation().add(0, 1, 0).toVector().subtract(from.toVector());
        double length = line.length();
        line.normalize();
        for (double d = 0; d < length; d += 0.4) {
            player.getWorld().spawnParticle(Particle.DUST, from.clone().add(line.clone().multiply(d)), 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.fromRGB(0x5A5A5A), 1.1f));
        }
        Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector());
        target.setVelocity(pull.multiply(0.16).setY(0.45));
    }

    /** The Guillotine falls: a leap and a slam that throws everybody round the landing up. */
    private void leap(Player player) {
        Vector forward = player.getLocation().getDirection().setY(0).normalize();
        player.setVelocity(forward.multiply(0.9).setY(1.05));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 0.8f, 1.4f);
        double damage = setting("items.executioner-guillotine.slam-damage", 14.0);
        double radius = setting("items.executioner-guillotine.slam-radius", 4.0);
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                if (!player.isOnline() || player.isDead() || ticks > 60) {
                    cancel();
                    return;
                }
                if (ticks < 6 || !player.isOnGround()) {
                    return;
                }
                Location at = player.getLocation();
                at.getWorld().spawnParticle(Particle.EXPLOSION, at, 3, 1, 0.2, 1, 0);
                at.getWorld().spawnParticle(Particle.DUST, at.clone().add(0, 0.2, 0), 60, radius / 2, 0.1, radius / 2, 0,
                        new Particle.DustOptions(Color.fromRGB(0x880000), 1.6f));
                at.getWorld().playSound(at, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.7f);
                for (Entity entity : at.getWorld().getNearbyEntities(at, radius, 2, radius)) {
                    if (entity.equals(player) || !(entity instanceof LivingEntity victim)) {
                        continue;
                    }
                    victim.damage(damage, DamageSource.builder(DamageType.PLAYER_ATTACK)
                            .withCausingEntity(player).withDirectEntity(player).build());
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                    victim.setVelocity(victim.getVelocity().setY(0.8));
                }
                cancel();
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    /** Sentence and Bleed: more damage to the dying, and wounds that keep bleeding. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player) || !(event.getEntity() instanceof LivingEntity target)
                || event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || !isGuillotine(player.getInventory().getItemInMainHand())) {
            return;
        }
        AttributeInstance max = target.getAttribute(Attribute.MAX_HEALTH);
        double top = max == null ? target.getHealth() : max.getValue();
        if (top > 0 && target.getHealth() / top < setting("items.executioner-guillotine.execute-threshold", 0.3)) {
            event.setDamage(event.getDamage() * (1 + setting("items.executioner-guillotine.execute-bonus", 0.4)));
            target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1.2, 0), 6,
                    0.3, 0.3, 0.3, 0.1);
        }
        if (ThreadLocalRandom.current().nextDouble() < setting("items.executioner-guillotine.bleed-chance", 0.25)) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0));
        }
    }
}
