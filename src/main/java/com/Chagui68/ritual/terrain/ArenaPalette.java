package com.Chagui68.ritual.terrain;

import org.bukkit.Material;

import java.util.List;

/**
 * Block choices for the ritual dimension: the strata, the glowing inlays and the pavement.
 *
 * <p>The pavement rule is the important one. Players build the invocation structures on the floor
 * of the arena, and each structure validates the blocks it stands on ({@code
 * JackInvocationStructure#isValidBase} / {@code NixInvocationStructure#isValidBase}). A pavement
 * the rituals refuse would break the whole dimension, so {@link #PLAZA_PAVEMENT} is exactly the
 * intersection of both lists and {@code BossArenaTerrainTest} proves every material used inside
 * {@link ArenaShape#PLAZA_RADIUS} is in it.
 *
 * <p>Light is handled with the same trick: the arena is at midnight forever ({@code
 * doDaylightCycle} is off at time 14000), so the glow comes from luminous <em>full</em> blocks set
 * flush with the floor — crying obsidian (light 10, and accepted by both structures) inside the
 * plaza, shroomlight and ochre froglight studs outside it. Nothing is ever placed above the floor
 * inside the arena, so the bosses' ground queries keep working exactly as they did on the flat
 * plain this dimension used to be.
 */
public final class ArenaPalette {

    /**
     * The only materials the paved plaza is made of: the intersection of the valid bases of both
     * invocation structures. Anything else here and a player could not build a ritual at spawn.
     */
    public static final List<Material> PLAZA_PAVEMENT = List.of(
            Material.POLISHED_BLACKSTONE_BRICKS,
            Material.DEEPSLATE_BRICKS,
            Material.CRYING_OBSIDIAN);

    /** Glow of the plaza: light 10 and accepted by every structure. */
    public static final Material PLAZA_GLOW = Material.CRYING_OBSIDIAN;

    private ArenaPalette() {
    }

    // ------------------------------------------------------------------ rock column

    /**
     * The block at {@code y} inside a solid column whose top is {@code surfaceY}.
     *
     * <p>{@code bedrockY} is the seal at the bottom of that column: {@link ArenaShape#BEDROCK_Y}
     * everywhere, and lower under a canyon, where the rock reaches deeper.
     */
    public static Material fill(int x, int y, int z, int surfaceY, int bedrockY, long seed,
                                ArenaShape.Zone zone) {
        if (y <= bedrockY) return Material.BEDROCK;
        int depth = surfaceY - y;
        int salt = ArenaNoise.saltOf(seed);

        // Glowing veins: what a canyon wall shows when it cuts through the rock.
        if (depth > 2 && ArenaNoise.hash(x, y, z + salt) % 191 == 0) {
            return Material.CRYING_OBSIDIAN;
        }
        if (depth <= 1) return subSurface(zone);
        if (depth <= 5) return Material.DEEPSLATE;
        if (depth <= 18) return Material.DEEPSLATE_TILES;
        if (y < -6 && ArenaNoise.fbm(x, z, salt + 31, 0.05, 2) > 0.62) return Material.SCULK;
        return Material.DEEPSLATE;
    }

    /** One block under the surface, per zone, so a cut through the floor still looks built. */
    public static Material subSurface(ArenaShape.Zone zone) {
        return switch (zone) {
            case ARENA -> Material.POLISHED_BLACKSTONE;
            case RIM -> Material.BLACKSTONE;
            case WASTE -> Material.DEEPSLATE_TILES;
        };
    }

    // ------------------------------------------------------------------ arena floor

    /** The block players stand on inside the arena: the plaza, or the decorated outer ring. */
    public static Material arenaFloor(int x, int z, long seed) {
        double r = ArenaShape.distance(x, z);
        if (r <= ArenaShape.PLAZA_RADIUS) {
            return plazaFloor(x, z, r);
        }
        return arenaRingFloor(x, z, r, seed);
    }

    /**
     * The plaza: an eight-pointed sigil at the centre, glowing rings and spokes, and a checker of
     * the two accepted brick types in between. Only {@link #PLAZA_PAVEMENT} is ever returned.
     */
    public static Material plazaFloor(int x, int z, double r) {
        if (r <= 3.5) return PLAZA_GLOW;
        if (r <= 15.0) {
            double degrees = ((Math.toDegrees(Math.atan2(z, x)) % 45.0) + 45.0) % 45.0;
            if (ring(r, 8.0, 1.0)) return PLAZA_GLOW;
            if (Math.min(degrees, 45.0 - degrees) <= 3.0) return PLAZA_GLOW;
            return Material.DEEPSLATE_BRICKS;
        }
        if (ring(r, 9.0, 1.0)) return PLAZA_GLOW;
        if (isAxisLine(x, z)) return PLAZA_GLOW;
        return ((x >> 1) + (z >> 1)) % 2 == 0
                ? Material.POLISHED_BLACKSTONE_BRICKS
                : Material.DEEPSLATE_BRICKS;
    }

    /** The ring between the plaza and the wall: masonry, gilded courses and glowing studs. */
    public static Material arenaRingFloor(int x, int z, double r, long seed) {
        int salt = ArenaNoise.saltOf(seed);
        double degrees = ((Math.toDegrees(Math.atan2(z, x)) % 360.0) + 360.0) % 360.0;

        if (r >= 60.0) {
            return degrees % 20.0 < 2.0 ? Material.SHROOMLIGHT : PLAZA_GLOW;
        }
        if (r >= 56.0) {
            if (degrees % 45.0 < 2.0) return Material.SHROOMLIGHT;
            if (degrees % 15.0 < 1.0) return Material.OCHRE_FROGLIGHT;
            return degrees % 3.0 < 1.5
                    ? Material.CHISELED_POLISHED_BLACKSTONE
                    : Material.POLISHED_BLACKSTONE_BRICKS;
        }
        if (r >= 40.0 && ring(r, 12.0, 1.0)) return Material.GILDED_BLACKSTONE;
        if (ArenaNoise.hash(x, z, salt + 5) % 89 == 0) return Material.SHROOMLIGHT;
        if (ArenaNoise.hash(x, z, salt + 5) % 89 == 1) return Material.OCHRE_FROGLIGHT;
        return ((x / 3) + (z / 3) + ((x / 3) ^ (z / 3))) % 2 == 0
                ? Material.POLISHED_BLACKSTONE
                : Material.CRACKED_POLISHED_BLACKSTONE_BRICKS;
    }

    // ------------------------------------------------------------------ rim

    /**
     * The rim's surface.
     *
     * <p>A column whose outward neighbours share its height is a terrace floor and gets masonry
     * with a glowing stud now and then; one that steps down is the rock face holding the terrace
     * above it and stays dark stone, which is what makes the galleries read as separate levels
     * from the arena floor.
     */
    public static Material rimSurface(int x, int z, boolean terraceFloor, long seed) {
        double r = ArenaShape.distance(x, z);
        double degrees = ((Math.toDegrees(Math.atan2(z, x)) % 360.0) + 360.0) % 360.0;
        if (!terraceFloor) {
            if (ArenaNoise.hash(x, z, ArenaNoise.saltOf(seed) + 41) % 23 == 0) return PLAZA_GLOW;
            return r >= ArenaShape.RIM_RADIUS - 8.0
                    ? Material.OBSIDIAN
                    : Material.BLACKSTONE;
        }
        if (degrees % 11.25 < 0.9) return Material.SHROOMLIGHT;
        return ((x >> 1) + (z >> 1)) % 2 == 0
                ? Material.DEEPSLATE_BRICKS
                : Material.POLISHED_BLACKSTONE_BRICKS;
    }

    // ------------------------------------------------------------------ wasteland

    /**
     * The wilderness surface, coloured by altitude so every shelf of a peak is visible.
     *
     * <p>"Flat" is a low frequency field rather than a slope measurement: the generator writes one
     * column at a time and asking for four neighbour heights per block would multiply the cost of
     * every chunk by five. The field is broad enough that the soul sand flats and the sculk basins
     * always land on ground that is already gentle.
     */
    public static Material wasteSurface(int x, int z, int surfaceY, long seed) {
        int salt = ArenaNoise.saltOf(seed);
        if (surfaceY <= ArenaShape.CHASM_FLOOR_Y + 1) return Material.LAVA;

        if (surfaceY >= 44) {
            if (ArenaNoise.hash(x, z, salt + 61) % 37 == 0) return Material.MAGMA_BLOCK;
            return Material.OBSIDIAN;
        }
        if (surfaceY >= 30) {
            return ArenaNoise.fbm(x, z, salt + 67, 0.06, 2) > 0.55
                    ? Material.BASALT
                    : Material.BLACKSTONE;
        }
        if (surfaceY >= 12) {
            if (ArenaNoise.fbm(x, z, salt + 71, 0.02, 2) > 0.58) return Material.SOUL_SAND;
            if (ArenaNoise.hash(x, z, salt + 73) % 53 == 0) return Material.CRYING_OBSIDIAN;
            return Material.DEEPSLATE;
        }
        if (ArenaNoise.fbm(x, z, salt + 79, 0.02, 2) > 0.62) return Material.SCULK;
        return Material.DEEPSLATE_TILES;
    }

    /** Soul fire standing on a soul sand flat: the blue flames that light the wilderness. */
    public static boolean soulFireAt(int x, int z, Material surface, long seed) {
        if (surface != Material.SOUL_SAND) return false;
        return ArenaNoise.hash(x, z, ArenaNoise.saltOf(seed) + 83) % 17 == 0;
    }

    private static boolean isAxisLine(int x, int z) {
        return x == 0 || z == 0 || x == z || x == -z;
    }

    /** A one-block-wide ring every {@code period} blocks of radius. */
    private static boolean ring(double radius, double period, double width) {
        double offset = ((radius % period) + period) % period;
        return offset < width;
    }
}
