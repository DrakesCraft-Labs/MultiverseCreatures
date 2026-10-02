package com.Chagui68.stand;

/**
 * How Sheer Heart Attack rolls towards its target, free of Bukkit so it can be tested.
 *
 * <p>It drives along the ground like a small tank, climbing and dropping a block at a time.
 * When there is no ground within reach ahead (a wall, a cliff, a cave ceiling between it and
 * the target) it does not stop: it phases straight through the blocks, drifting towards the
 * height of the target, and lands again as soon as there is ground to roll on.</p>
 */
public final class SheerHeartAttackPath {

    /** Whether the block at those coordinates stops movement. */
    @FunctionalInterface
    public interface Blocks {
        boolean solid(int x, int y, int z);
    }

    /**
     * The next position.
     *
     * @param phasing true when the step went through blocks instead of rolling on the ground
     */
    public record Step(double x, double y, double z, boolean phasing) {
    }

    /** Highest climb or drop it rolls over without phasing. */
    static final int REACH = 1;

    private SheerHeartAttackPath() {
    }

    /**
     * One step of {@code speed} blocks from {@code (x, y, z)} towards {@code (tx, ty, tz)}; the
     * y values are the height of the feet.
     */
    public static Step step(double x, double y, double z, double tx, double ty, double tz, double speed,
                            Blocks blocks) {
        double dx = tx - x;
        double dz = tz - z;
        double flat = Math.hypot(dx, dz);
        double nx = x;
        double nz = z;
        if (flat > 0.05) {
            double move = Math.min(speed, flat);
            nx = x + dx / flat * move;
            nz = z + dz / flat * move;
        }
        int bx = (int) Math.floor(nx);
        int bz = (int) Math.floor(nz);
        int by = (int) Math.floor(y + 1e-6);

        // Ground to roll on: a free block for the body with a solid one under it, within one
        // block up or down, preferring the level it is already on.
        if (flat > 0.05 || Math.abs(ty - y) < 0.6) {
            for (int dy : new int[]{0, 1, -1}) {
                int feet = by + dy;
                if (Math.abs(dy) > REACH) {
                    continue;
                }
                if (!blocks.solid(bx, feet, bz) && blocks.solid(bx, feet - 1, bz)) {
                    return new Step(nx, feet, nz, false);
                }
            }
        }
        // No ground within reach: go through the blocks, towards the height of the target.
        double climb = Math.max(-speed, Math.min(speed, ty - y));
        return new Step(nx, y + climb, nz, true);
    }

    /** True when it is close enough to the target to go off. */
    public static boolean arrived(double x, double y, double z, double tx, double ty, double tz, double reach) {
        double dx = tx - x;
        double dy = (ty + 0.9) - (y + 0.3);
        double dz = tz - z;
        return dx * dx + dy * dy + dz * dz <= reach * reach;
    }
}
