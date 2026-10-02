package com.Chagui68.listener.weapons.ranged;

import com.Chagui68.items.weapons.ranged.ArchitectDeployer;
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
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Architect's Deployer: guided packets, the {@code sudo rm -rf} beam and Failover.
 */
public class ArchitectDeployerHandler implements Listener {

    private static final String PACKET = "MSC_DeployerPacket";

    private final Plugin plugin;
    private final Map<UUID, Long> beamCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> failoverCooldowns = new ConcurrentHashMap<>();

    public ArchitectDeployerHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private double setting(String key, double fallback) {
        return plugin.getConfig().getDouble(key, fallback);
    }

    private static boolean isDeployer(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer()
                .has(ArchitectDeployer.KEY, PersistentDataType.INTEGER);
    }

    /** Guided Packets: every arrow homes in on the nearest living thing ahead of it. */
    @EventHandler(ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player) || !isDeployer(event.getBow())
                || !(event.getProjectile() instanceof AbstractArrow arrow)) {
            return;
        }
        arrow.addScoreboardTag(PACKET);
        double radius = setting("items.architect-deployer.homing-radius", 8.0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.6f, 1.8f);
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                if (!arrow.isValid() || arrow.isInBlock() || arrow.isOnGround() || ticks > 100) {
                    cancel();
                    return;
                }
                arrow.getWorld().spawnParticle(Particle.DUST, arrow.getLocation(), 2, 0.02, 0.02, 0.02, 0,
                        new Particle.DustOptions(Color.fromRGB(0x2FE6F2), 0.8f));
                if (ticks < 4) {
                    return;
                }
                LivingEntity target = nearest(arrow, player, radius);
                if (target == null) {
                    return;
                }
                Vector velocity = arrow.getVelocity();
                double speed = velocity.length();
                Vector wanted = target.getLocation().add(0, target.getHeight() * 0.6, 0).toVector()
                        .subtract(arrow.getLocation().toVector()).normalize().multiply(speed);
                arrow.setVelocity(velocity.multiply(0.7).add(wanted.multiply(0.3)));
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private static LivingEntity nearest(AbstractArrow arrow, Player shooter, double radius) {
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        Vector heading = arrow.getVelocity().clone().normalize();
        for (Entity entity : arrow.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living) || entity.equals(shooter) || living.isDead()) {
                continue;
            }
            Vector to = living.getLocation().toVector().subtract(arrow.getLocation().toVector());
            if (to.clone().normalize().dot(heading) < 0.3) {
                continue;
            }
            double distance = to.lengthSquared();
            if (distance < bestDistance) {
                bestDistance = distance;
                best = living;
            }
        }
        return best;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPacketHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile projectile && projectile.getScoreboardTags().contains(PACKET)) {
            event.setDamage(event.getDamage() * (1 + setting("items.architect-deployer.damage-bonus", 0.2)));
        }
    }

    /** sudo rm -rf: a beam that deletes everything in a straight line. */
    @EventHandler
    public void onSwing(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking() || !isDeployer(player.getInventory().getItemInMainHand())) {
            return;
        }
        long now = System.currentTimeMillis();
        long until = beamCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (until > now) {
            player.sendActionBar(Component.text("sudo rm -rf en enfriamiento: " + (until - now + 999) / 1000 + "s",
                    NamedTextColor.RED));
            return;
        }
        beamCooldowns.put(player.getUniqueId(), now + (long) setting("items.architect-deployer.beam-cooldown-ms", 18000));
        double range = setting("items.architect-deployer.beam-range", 40.0);
        double damage = setting("items.architect-deployer.beam-damage", 12.0);
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        RayTraceResult wall = player.getWorld().rayTraceBlocks(eye, direction, range);
        double length = wall == null ? range : wall.getHitPosition().distance(eye.toVector());
        Set<UUID> hit = new HashSet<>();
        for (double d = 0.5; d < length; d += 0.5) {
            Location point = eye.clone().add(direction.clone().multiply(d));
            player.getWorld().spawnParticle(Particle.END_ROD, point, 1, 0, 0, 0, 0);
            if (((int) (d * 2)) % 3 == 0) {
                player.getWorld().spawnParticle(Particle.DUST, point, 1, 0.05, 0.05, 0.05, 0,
                        new Particle.DustOptions(Color.fromRGB(0x2FE6F2), 1.2f));
            }
            for (Entity entity : player.getWorld().getNearbyEntities(point, 0.6, 0.6, 0.6)) {
                if (entity.equals(player) || !(entity instanceof LivingEntity victim) || !hit.add(entity.getUniqueId())) {
                    continue;
                }
                victim.damage(damage, DamageSource.builder(DamageType.MAGIC).withCausingEntity(player)
                        .withDirectEntity(player).build());
            }
        }
        player.getWorld().playSound(eye, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f);
        player.getWorld().playSound(eye, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 1.8f);
        player.sendActionBar(Component.text("$ sudo rm -rf --no-preserve-root  ✔ " + hit.size() + " eliminado(s)",
                NamedTextColor.AQUA));
    }

    /** Failover: about to fall, the holder switches to behind the attacker. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHurt(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!isDeployer(player.getInventory().getItemInMainHand()) && !isDeployer(player.getInventory().getItemInOffHand())) {
            return;
        }
        AttributeInstance max = player.getAttribute(Attribute.MAX_HEALTH);
        double top = max == null ? 20.0 : max.getValue();
        double after = player.getHealth() - event.getFinalDamage();
        if (after <= 0 || after / top >= setting("items.architect-deployer.failover-threshold", 0.3)) {
            return;
        }
        long now = System.currentTimeMillis();
        if (failoverCooldowns.getOrDefault(player.getUniqueId(), 0L) > now) {
            return;
        }
        Entity attacker = event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Entity shooter ? shooter : event.getDamager();
        failoverCooldowns.put(player.getUniqueId(),
                now + (long) setting("items.architect-deployer.failover-cooldown-ms", 12000));
        Location behind = attacker.getLocation().clone()
                .add(attacker.getLocation().getDirection().setY(0).normalize().multiply(-2));
        behind.setYaw(attacker.getLocation().getYaw());
        if (!behind.getBlock().isPassable() || !behind.clone().add(0, 1, 0).getBlock().isPassable()) {
            behind = player.getLocation();
        }
        player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.4, 0.8, 0.4, 0.2);
        player.teleport(behind);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0));
        player.getWorld().playSound(behind, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.6f);
        player.sendActionBar(Component.text("Failover: servicio conmutado", NamedTextColor.AQUA));
    }
}
