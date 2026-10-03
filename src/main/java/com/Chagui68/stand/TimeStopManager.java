package com.Chagui68.stand;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Stopped time: for a few seconds everything round a Stand user stands still. Players cannot
 * move, hit, use items or shoot; mobs lose their minds; arrows hang in the air. When the time
 * runs out (or the plugin stops) everything is given back exactly as it was.
 */
public final class TimeStopManager implements Listener {

    private record FrozenMob(long until, boolean hadAi) {
    }

    private record FrozenProjectile(long until, Vector velocity, boolean hadGravity) {
    }

    private final Plugin plugin;
    private final Map<UUID, Long> frozenPlayers = new HashMap<>();
    private final Map<UUID, FrozenMob> frozenMobs = new HashMap<>();
    private final Map<UUID, FrozenProjectile> frozenProjectiles = new HashMap<>();
    private BukkitTask ticker;

    public TimeStopManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Stops time round {@code user} for that many ticks.
     *
     * @param immune entities that keep moving: other Stands that stop time, bosses...
     * @return how many entities were frozen
     */
    public int stop(Player user, double radius, int ticks, Predicate<Entity> immune) {
        long until = System.currentTimeMillis() + ticks * 50L;
        int frozen = 0;
        Location center = user.getLocation();
        for (Entity entity : user.getWorld().getNearbyEntities(center, radius, radius, radius)) {
            if (entity.equals(user) || immune.test(entity)) {
                continue;
            }
            if (entity instanceof Player player) {
                frozenPlayers.put(player.getUniqueId(), until);
                player.sendActionBar(Component.text("⏸ Time has stopped", NamedTextColor.GOLD));
                frozen++;
            } else if (entity instanceof Mob mob) {
                FrozenMob previous = frozenMobs.get(mob.getUniqueId());
                frozenMobs.put(mob.getUniqueId(), new FrozenMob(until, previous != null ? previous.hadAi() : mob.hasAI()));
                mob.setAI(false);
                mob.setVelocity(new Vector());
                frozen++;
            } else if (entity instanceof Projectile projectile) {
                FrozenProjectile previous = frozenProjectiles.get(projectile.getUniqueId());
                frozenProjectiles.put(projectile.getUniqueId(), new FrozenProjectile(until,
                        previous != null ? previous.velocity() : projectile.getVelocity(),
                        previous != null ? previous.hadGravity() : projectile.hasGravity()));
                projectile.setGravity(false);
                projectile.setVelocity(new Vector());
                frozen++;
            }
        }
        if (ticker == null) {
            ticker = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
        }
        return frozen;
    }

    /** True while that player is caught in a stopped time. */
    public boolean frozen(Player player) {
        Long until = frozenPlayers.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (Iterator<Map.Entry<UUID, Long>> it = frozenPlayers.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Long> entry = it.next();
            if (entry.getValue() <= now) {
                it.remove();
                Player player = plugin.getServer().getPlayer(entry.getKey());
                if (player != null) {
                    player.sendActionBar(Component.text("▶ Time flows again", NamedTextColor.YELLOW));
                }
            }
        }
        for (Iterator<Map.Entry<UUID, FrozenMob>> it = frozenMobs.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, FrozenMob> entry = it.next();
            Entity entity = plugin.getServer().getEntity(entry.getKey());
            if (!(entity instanceof Mob mob) || !mob.isValid()) {
                it.remove();
                continue;
            }
            if (entry.getValue().until() <= now) {
                mob.setAI(entry.getValue().hadAi());
                it.remove();
            } else {
                mob.setVelocity(new Vector());
            }
        }
        for (Iterator<Map.Entry<UUID, FrozenProjectile>> it = frozenProjectiles.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, FrozenProjectile> entry = it.next();
            Entity entity = plugin.getServer().getEntity(entry.getKey());
            if (!(entity instanceof Projectile projectile) || !projectile.isValid()) {
                it.remove();
                continue;
            }
            if (entry.getValue().until() <= now) {
                projectile.setGravity(entry.getValue().hadGravity());
                projectile.setVelocity(entry.getValue().velocity());
                it.remove();
            } else {
                projectile.setVelocity(new Vector());
            }
        }
        if (frozenPlayers.isEmpty() && frozenMobs.isEmpty() && frozenProjectiles.isEmpty() && ticker != null) {
            ticker.cancel();
            ticker = null;
        }
    }

    /** Lets time flow again everywhere: used when the plugin stops. */
    public void releaseAll() {
        long past = 0L;
        frozenPlayers.replaceAll((id, until) -> past);
        frozenMobs.replaceAll((id, mob) -> new FrozenMob(past, mob.hadAi()));
        frozenProjectiles.replaceAll((id, p) -> new FrozenProjectile(past, p.velocity(), p.hadGravity()));
        tick();
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!frozen(event.getPlayer())) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            Location still = from.clone();
            still.setYaw(to.getYaw());
            still.setPitch(to.getPitch());
            event.setTo(still);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (frozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            damager = shooter;
        }
        if (damager instanceof Player player && frozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player && frozen(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (frozen(event.getPlayer()) && event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        frozenPlayers.remove(event.getPlayer().getUniqueId());
    }
}
