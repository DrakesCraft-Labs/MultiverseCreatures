package com.Chagui68.entities.boss;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

/**
 * Utility queries for terrain and players in a boss arena.
 *
 * Provides reusable static checks for valid player targets (filtering dead, creative,
 * and spectator players) and spatial queries across worlds and arenas.
 */
public final class BossArena {

    private BossArena() {}

    /** Checks whether a player is a valid attack target for a boss. */
    public static boolean isValidTarget(Player p) {
        return p != null
                && !p.isDead()
                && p.getGameMode() != GameMode.CREATIVE
                && p.getGameMode() != GameMode.SPECTATOR;
    }

    /** Compatibility alias for isValidTarget. */
    @Deprecated
    public static boolean esObjetivoValido(Player p) {
        return isValidTarget(p);
    }

    /** Returns all valid players in the world that a boss can target. */
    public static List<Player> getValidPlayers(World world) {
        List<Player> result = new ArrayList<>();
        if (world == null) {
            return result;
        }
        for (Player p : world.getPlayers()) {
            if (isValidTarget(p)) {
                result.add(p);
            }
        }
        return result;
    }

    /**
     * Los jugadores validos dentro de un radio.
     *
     * El radio va al cuadrado a proposito: los ataques comparan distancias muchas veces por tick
     * y la raiz cuadrada es lo caro de la operacion.
     */
    public static List<Player> getValidPlayersNear(Location center, double radiusSq) {
        List<Player> result = new ArrayList<>();
        if (center == null || center.getWorld() == null) {
            return result;
        }
        for (Player p : center.getWorld().getPlayers()) {
            if (p != null && p.getWorld().equals(center.getWorld()) && isValidTarget(p)) {
                try {
                    if (p.getLocation().distanceSquared(center) <= radiusSq) {
                        result.add(p);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return result;
    }

    /** Counts valid players within the given radius. */
    public static int countPlayersInRange(Location center, double radius) {
        return getValidPlayersNear(center, radius * radius).size();
    }

    /** Returns the nearest valid player within range, or null if none found. */
    public static Player findNearestPlayer(Location center, double range) {
        Player nearest = null;
        if (center == null || center.getWorld() == null) {
            return null;
        }
        double nearestDistSq = range * range;
        for (Player p : getValidPlayers(center.getWorld())) {
            if (p != null && p.getWorld().equals(center.getWorld())) {
                try {
                    double distSq = p.getLocation().distanceSquared(center);
                    if (distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                        nearest = p;
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return nearest;
    }

    /** Distance to the nearest valid player, or Double.MAX_VALUE if none found. */
    public static double getNearestPlayerDistance(Location loc) {
        Player nearest = findNearestPlayer(loc, Double.MAX_VALUE);
        if (nearest == null || nearest.getWorld() == null || loc == null || loc.getWorld() == null
                || !nearest.getWorld().equals(loc.getWorld())) {
            return Double.MAX_VALUE;
        }
        try {
            return nearest.getLocation().distance(loc);
        } catch (IllegalArgumentException e) {
            return Double.MAX_VALUE;
        }
    }

    /**
     * Launches the player upward.
     *
     * If the player is already rising upward, no additional vertical boost is applied to prevent
     * runaway vertical compounding.
     */
    public static void launchPlayer(Player p, double y) {
        if (p.getVelocity().getY() > 0.1) {
            return;
        }
        p.setVelocity(p.getVelocity().setY(y));
    }

    /** The Y altitude of the first solid block below, or the current Y if none found. */
    public static double getGroundY(Location loc, double maxScan) {
        for (double dy = 1; dy <= maxScan; dy++) {
            if (loc.clone().subtract(0, dy, 0).getBlock().getType().isSolid()) {
                return loc.getY() - dy + 1;
            }
        }
        return loc.getY();
    }

    /** Checks whether the entity is standing on a solid block. */
    public static boolean isOnGround(BossPuppet stand) {
        return stand.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid();
    }

    /** Returns the nearest valid player within the boss aggro range, or null. */
    public static Player detectTarget(BossPuppet stand, double aggroRange) {
        return findNearestPlayer(stand.getLocation(), aggroRange);
    }
}
