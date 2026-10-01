package com.Chagui68.entities.boss;

import java.util.ArrayList;
import java.util.List;

import com.Chagui68.utils.MscLog;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
                } catch (IllegalArgumentException e) {
                    MscLog.debug("skipped a player whose location is in another world", e);
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
                } catch (IllegalArgumentException e) {
                    MscLog.debug("skipped a player whose location is in another world", e);
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

    /** Probes one vertical column of blocks by the height of their top face. */
    @FunctionalInterface
    public interface SurfaceProbe {
        /** Absolute Y of the top of block {@code y} (e.g. {@code y + 0.5} for a slab), or NaN when it is not solid. */
        double surfaceAt(int y);
    }

    /**
     * How far a body may sit off the floor and still count as standing on it.
     *
     * Wider than float noise on purpose: a stand left a few centimetres up by a teleport would
     * otherwise read as airborne, and an airborne Sentinel does not attack.
     */
    public static final double GROUND_TOLERANCE = 0.2;

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
        return findFloorY(loc.getY(), maxScan, (SurfaceProbe) y -> {
            Block block = world.getBlockAt(x, y, z);
            if (block == null || !block.getType().isSolid()) return Double.NaN;
            // Slabs, stairs and the like: stand on the shape, not on the block cell.
            double top = block.getBoundingBox().getMaxY();
            return top > y ? top : y + 1.0;
        });
    }

    /**
     * Pure version of {@link #findFloorY(Location, double)} over whole blocks: no server required, so
     * the rule can be tested directly.
     */
    public static double findFloorY(double startY, double maxScan, VerticalProbe probe) {
        if (probe == null) {
            return Double.NaN;
        }
        return findFloorY(startY, maxScan, (SurfaceProbe) y -> probe.isSolidAt(y) ? y + 1.0 : Double.NaN);
    }

    /**
     * The top face of the first solid block at or below {@code startY}, or NaN within {@code maxScan}.
     *
     * The answer is always a block's own top face. The old scan answered {@code startY - dy + 1},
     * which is the boss's own Y whenever it hovers less than a block up: a stand left at 64.5 over a
     * floor at 64 was told the floor was at 64.5, the descent never moved it, {@code isOnGround}
     * stayed false and the Sentinel hovered half a block up, not attacking, for the rest of the
     * fight. The block containing {@code startY} is scanned too, so a body sunk into a slab or a
     * step is pushed back on top of it instead of searching below it.
     */
    public static double findFloorY(double startY, double maxScan, SurfaceProbe probe) {
        if (probe == null || Double.isNaN(startY)) {
            return Double.NaN;
        }
        int top = (int) Math.floor(startY);
        int bottom = (int) Math.floor(startY - maxScan);
        for (int y = top; y >= bottom; y--) {
            double surface = probe.surfaceAt(y);
            if (!Double.isNaN(surface)) {
                return surface;
            }
        }
        return Double.NaN;
    }

    /** Whether a body at {@code y} stands on a floor at {@code floorY}; NaN means there is no floor. */
    public static boolean restsOn(double y, double floorY) {
        return !Double.isNaN(floorY) && Math.abs(y - floorY) <= GROUND_TOLERANCE;
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

    /**
     * Checks whether the entity is standing on a solid block.
     *
     * Measured against the same floor {@link #findFloorY} reports, so "on the ground" and "where the
     * descent snaps to" can never disagree; the old block-under-the-feet test said "airborne" for a
     * stand hovering a fraction of a block up, while the floor scan said it had already landed.
     */
    public static boolean isOnGround(BossPuppet stand) {
        Location loc = stand.getLocation();
        return restsOn(loc.getY(), findFloorY(loc, 2.0));
    }

    /** Highest step a walking boss climbs; anything taller is a wall. */
    public static final double STEP_HEIGHT = 1.0;

    /** How fast a walking boss drops off a ledge, in blocks per tick. */
    public static final double FALL_PER_TICK = 0.6;

    /** How far below its feet a walker looks for a floor before treating the drop as bottomless. */
    private static final double WALK_SCAN = 12.0;

    /**
     * Where a walker's feet go next given the floor under the destination, or NaN when that floor
     * is a wall.
     *
     * <p>{@code floorY} is the top of the first solid block at or below {@code fromY + STEP_HEIGHT}
     * (NaN when there is none within the scan): one block up is a step, level ground is level, and
     * a lower floor is fallen towards rather than teleported onto. No floor at all still falls,
     * which is what keeps a walker from hanging in the air over a deep drop.
     */
    public static double nextFeetY(double fromY, double floorY) {
        if (Double.isNaN(floorY)) return fromY - FALL_PER_TICK;
        if (floorY > fromY + STEP_HEIGHT + 1e-6) return Double.NaN;
        if (floorY >= fromY) return floorY;
        return Math.max(floorY, fromY - FALL_PER_TICK);
    }

    /**
     * Moves {@code loc} by the horizontal {@code step} as a walker would: up one block at most, down
     * by falling, and never into a wall.
     *
     * <p>The dressed bosses used to add the step and then snap to the first solid block under their
     * feet. A wall is solid under the feet too, so they walked into it and climbed it a block per
     * tick; and the snap only looked eight blocks down, so a longer drop left them standing on air.
     *
     * @param slide when the full step is blocked, try its two axes alone so a wall met at an angle
     *              is followed instead of stopping the walker dead
     * @return whether {@code loc} moved
     */
    public static boolean walk(Location loc, Vector step, boolean slide) {
        Vector[] attempts = slide
                ? new Vector[]{step, new Vector(step.getX(), 0, 0), new Vector(0, 0, step.getZ())}
                : new Vector[]{step};
        for (Vector attempt : attempts) {
            if (attempt.lengthSquared() < 1e-6) continue;
            Location next = loc.clone().add(attempt.getX(), 0, attempt.getZ());
            Location probe = next.clone();
            probe.setY(loc.getY() + STEP_HEIGHT);
            double feet = nextFeetY(loc.getY(), findFloorY(probe, WALK_SCAN));
            if (Double.isNaN(feet)) continue;
            loc.setX(next.getX());
            loc.setZ(next.getZ());
            loc.setY(feet);
            return true;
        }
        return false;
    }

    /**
     * Drops {@code loc} towards the floor under it, or lifts it a block at a time out of terrain it
     * ended up inside. Scanned from the feet, so the block a body is buried in is the floor to climb.
     */
    public static void settle(Location loc) {
        double feet = nextFeetY(loc.getY(), findFloorY(loc, WALK_SCAN));
        if (!Double.isNaN(feet)) loc.setY(feet);
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
