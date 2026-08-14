package com.Chagui68.listener.combat;

import com.Chagui68.items.weapons.melee.Excalibur;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ItemCombatHandler implements Listener {

    private final Plugin plugin;
    private final FileConfiguration config;
    private final Map<UUID, Long> solarFlareCooldowns = new ConcurrentHashMap<>();
    private static final long SOLAR_FLARE_COOLDOWN_MS = 15000;

    public ItemCombatHandler(Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        startPassiveEffect();
    }

    private void startPassiveEffect() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();
                    if (itemInHand == null || itemInHand.getType().isAir())
                        continue;

                    ItemMeta meta = itemInHand.getItemMeta();
                    if (meta == null)
                        continue;

                    if (meta.getPersistentDataContainer().has(Excalibur.EXCALIBUR_KEY,
                            PersistentDataType.INTEGER)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 80, 2));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(Excalibur.EXCALIBUR_KEY, PersistentDataType.INTEGER))
            return;

        if (!config.getBoolean("excalibur.solar-flare.enabled", true)) {
            p.sendMessage(ChatColor.RED + "Solar Flare is disabled in config.");
            return;
        }

        long cooldownMs = config.getLong("excalibur.solar-flare.cooldown-ms", SOLAR_FLARE_COOLDOWN_MS);
        UUID id = p.getUniqueId();
        Long last = solarFlareCooldowns.get(id);
        if (last != null && System.currentTimeMillis() - last < cooldownMs) {
            long remaining = (cooldownMs - (System.currentTimeMillis() - last)) / 1000;
            p.sendMessage(ChatColor.RED + "Solar Flare on cooldown (" + remaining + "s)");
            return;
        }

        solarFlareCooldowns.put(id, System.currentTimeMillis());

        performSolarFlare(p);
        e.setCancelled(true);
    }

    private void performSolarFlare(Player p) {
        double range = config.getDouble("excalibur.solar-flare.range", 20.0);
        double beamRadius = config.getDouble("excalibur.solar-flare.radius", 1.5);
        double damage = config.getDouble("excalibur.solar-flare.damage", 12.0);
        int fireTicks = config.getInt("excalibur.solar-flare.fire-ticks", 100);
        int blindnessDuration = config.getInt("excalibur.solar-flare.blindness-duration", 60);
        double knockbackHorizontal = config.getDouble("excalibur.solar-flare.knockback-horizontal", 1.5);
        double knockbackVertical = config.getDouble("excalibur.solar-flare.knockback-vertical", 0.8);

        Location start = p.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        p.getWorld().playSound(start, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.8f);
        p.getWorld().playSound(start, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);

        new BukkitRunnable() {
            int ticks = 0;
            double currentDistance = 0;
            final double step = 1.0;
            final Set<UUID> hitThisCast = new HashSet<>();

            @Override
            public void run() {
                ticks++;
                if (ticks > 20) {
                    cancel();
                    return;
                }

                currentDistance += step;
                if (currentDistance > range) {
                    cancel();
                    return;
                }

                Location beamCenter = start.clone().add(direction.clone().multiply(currentDistance));

                p.getWorld().spawnParticle(Particle.FLAME, beamCenter, 3, beamRadius * 0.5, 0.1, beamRadius * 0.5, 0.01);
                p.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, beamCenter, 2, beamRadius * 0.5, 0.1, beamRadius * 0.5, 0.01);
                p.getWorld().spawnParticle(Particle.END_ROD, beamCenter, 1, beamRadius * 0.3, 0.1, beamRadius * 0.3, 0.0);

                for (Entity entity : p.getWorld().getNearbyEntities(beamCenter, beamRadius, beamRadius, beamRadius)) {
                    if (entity instanceof LivingEntity target && target != p) {
                        if (!hitThisCast.contains(target.getUniqueId())) {
                            hitThisCast.add(target.getUniqueId());
                            target.damage(damage, p);
                            target.setFireTicks(fireTicks);
                            target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindnessDuration, 1));

                            Vector knockback = direction.clone().multiply(knockbackHorizontal);
                            knockback.setY(knockbackVertical);
                            target.setVelocity(target.getVelocity().add(knockback));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                if (ticks > 40) {
                    cancel();
                    return;
                }
                Location beamCenter = start.clone().add(direction.clone().multiply(range / 2.0));
                p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, beamCenter, 5, 1.0, 1.0, 1.0, 0.1);
                p.getWorld().spawnParticle(Particle.FLAME, beamCenter, 2, 1.0, 1.0, 1.0, 0.05);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}