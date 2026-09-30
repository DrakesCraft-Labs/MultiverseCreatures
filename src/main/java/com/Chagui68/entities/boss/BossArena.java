package com.Chagui68.entities.boss;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
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

    /** Probes one vertical column of blocks. */
    @FunctionalInterface
    public interface VerticalProbe {
        boolean isSolidAt(int y);
    }

    /** Probes whole columns of terrain, used to find somewhere safe to stand. */
    @FunctionalInterface
    public interface ColumnProbe {
        /** Y of the highest solid block with headroom in this column, or {@link Double#NaN}. */
        double floorY(int x, int z);
    }

    /**
     * The Y altitude of the first solid block below, or {@link Double#NaN} when none was found.
     *
     * The NaN is the whole point of this method existing next to {@link #getGroundY}: the
     * convenience version cannot tell "standing on the floor" apart from "nothing underneath",
     * and a grounded boss that mistakes the second for the first stops attacking forever.
     */
    public static double findFloorY(Location loc, double maxScan) {
        if (loc == null || loc.getWorld() == null) {
            return Double.NaN;
        }
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        return findFloorY(loc.getY(), maxScan, y -> {
            Block block = world.getBlockAt(x, y, z);
            return block != null && block.getType().isSolid();
        });
    }

    /**
     * Pure version of {@link #findFloorY(Location, double)}: no server required, so the rule can be
     * tested directly.
     */
    public static double findFloorY(double startY, double maxScan, VerticalProbe probe) {
        if (probe == null) {
            return Double.NaN;
        }
        for (double dy = 1; dy <= maxScan; dy++) {
            if (probe.isSolidAt((int) Math.floor(startY - dy))) {
                return startY - dy + 1;
            }
        }
        return Double.NaN;
    }

    /** The Y altitude of the first solid block below, or the current Y if none found. */
    public static double getGroundY(Location loc, double maxScan) {
        double floorY = findFloorY(loc, maxScan);
        if (Double.isNaN(floorY)) {
            return loc == null ? 0 : loc.getY();
        }
        return floorY;
    }

    /** Whether a solid block exists below the location within the scan range. */
    public static boolean hasFloorBelow(Location loc, double maxScan) {
        return !Double.isNaN(findFloorY(loc, maxScan));
    }

    /** Checks whether the entity is standing on a solid block. */
    public static boolean isOnGround(BossPuppet stand) {
        return stand.getLocation().subtract(0, 0.1, 0).getBlock().getType().isSolid();
    }

    /**
     * Column offsets ordered by distance from the origin: the origin first, then expanding rings.
     *
     * Deterministic order is the contract: a boss looking for a place to stand always picks the
     * nearest usable column, so the same terrain produces the same landing spot on every run.
     * Within a ring the offsets are sorted by distance too, otherwise the boss could walk past a
     * closer column just because it was iterated later.
     */
    public static List<int[]> ringOffsets(int maxRadius) {
        List<int[]> offsets = new ArrayList<>();
        offsets.add(new int[]{0, 0});
        for (int radius = 1; radius <= maxRadius; radius++) {
            int outerSq = radius * radius;
            int innerSq = (radius - 1) * (radius - 1);
            List<int[]> ring = new ArrayList<>();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int distSq = dx * dx + dz * dz;
                    if (distSq > innerSq && distSq <= outerSq) {
                        ring.add(new int[]{dx, dz});
                    }
                }
            }
            ring.sort((a, b) -> {
                int byDistance = Integer.compare(a[0] * a[0] + a[1] * a[1], b[0] * b[0] + b[1] * b[1]);
                if (byDistance != 0) return byDistance;
                int byX = Integer.compare(a[0], b[0]);
                return byX != 0 ? byX : Integer.compare(a[1], b[1]);
            });
            offsets.addAll(ring);
        }
        return offsets;
    }

    /**
     * Nearest standing spot with solid ground, for a boss that ended up with no floor under it.
     *
     * Probes columns outward from {@code from} and returns the first block that is solid and has
     * two free blocks above it, so the boss does not land inside terrain. Returns null when no
     * column within the radius qualifies; the caller decides what to try next (usually the target
     * column and then the world spawn).
     */
    public static Location findGroundRestingPlace(Location from, int horizontalRadius, double maxScan) {
        if (from == null || from.getWorld() == null) {
            return null;
        }
        World world = from.getWorld();
        int topY = (int) Math.floor(from.getY());
        int scan = (int) Math.max(1, maxScan);
        int[] column = findUsableColumn(from.getBlockX(), from.getBlockZ(), horizontalRadius,
                (x, z) -> usableFloorY(world, x, z, topY, scan));
        if (column == null) {
            return null;
        }
        return new Location(world, column[0] + 0.5, column[1] + 1.0, column[2] + 0.5);
    }

    /**
     * Pure column search over {@link #ringOffsets}: the first usable column as
     * {@code {x, floorY, z}}, or null when every probed column is unusable.
     */
    public static int[] findUsableColumn(int originX, int originZ, int horizontalRadius, ColumnProbe probe) {
        if (probe == null) {
            return null;
        }
        for (int[] offset : ringOffsets(horizontalRadius)) {
            int x = originX + offset[0];
            int z = originZ + offset[1];
            double floorY = probe.floorY(x, z);
            if (!Double.isNaN(floorY)) {
                return new int[]{x, (int) Math.floor(floorY), z};
            }
        }
        return null;
    }

    /**
     * Y of the highest solid block with two free blocks above it in one column, or NaN.
     *
     * A solid block without headroom makes the whole column unusable: anything below it is covered
     * by it, so there is no point scanning deeper.
     */
    private static double usableFloorY(World world, int x, int z, int topY, int scan) {
        for (int y = topY; y > topY - scan; y--) {
            Block block = world.getBlockAt(x, y, z);
            if (block == null || !block.getType().isSolid()) {
                continue;
            }
            Block above = world.getBlockAt(x, y + 1, z);
            Block above2 = world.getBlockAt(x, y + 2, z);
            if (above != null && above2 != null
                    && !above.getType().isSolid() && !above2.getType().isSolid()) {
                return y;
            }
            return Double.NaN;
        }
        return Double.NaN;
    }

    /** Returns the nearest valid player within the boss aggro range, or null. */
    public static Player detectTarget(BossPuppet stand, double aggroRange) {
        return findNearestPlayer(stand.getLocation(), aggroRange);
    }
}
