package com.Chagui68.ritual.terrain;

import org.bukkit.Material;

import static com.Chagui68.ritual.terrain.TerrainNoise.fbm;
import static com.Chagui68.ritual.terrain.TerrainNoise.hash;
import static com.Chagui68.ritual.terrain.TerrainNoise.lerp;
import static com.Chagui68.ritual.terrain.TerrainNoise.ridge;
import static com.Chagui68.ritual.terrain.TerrainNoise.smoothstep;
import static com.Chagui68.ritual.terrain.TerrainNoise.unit;

/**
 * The ritual dimension: a scorched battlefield about 1500 blocks across.
 *
 * <pre>
 * r &lt;= 48      the arena: perfectly flat paved floor with a crying-obsidian sigil
 * r &lt;= 92      the approach: rubble fading into the wasteland
 * beyond       the wasteland: soul valleys with blue fire, scorched blackstone with magma
 *              patches, burning netherrack fields; lava rivers, lava lakes, craters,
 *              obsidian spikes, ruined colonnades and giant ribcages
 * edge &gt;= 630  a jagged obsidian range that closes the world before its border
 * </pre>
 *
 * <p>Every column is a pure function of its coordinates and the world seed: a chunk never asks
 * another chunk anything, so chunk borders always match and generation can run in parallel. Lava
 * only ever fills a column up to {@link #LAVA_Y}, and every column below that level is filled the
 * same way, so a lava surface always meets either more lava or rock and never runs anywhere.
 */
public final class Wasteland {

    /** Receives one column's blocks, bottom to top; later writes replace earlier ones. */
    public interface ColumnWriter {
        /** Fills {@code fromY..toY} (both inclusive); an empty range writes nothing. */
        void fill(int fromY, int toY, Material material);
    }

    public static final int BEDROCK_Y = 0;
    /** Top block of the arena floor: bosses and players stand at {@code FLOOR_Y + 1}. */
    public static final int FLOOR_Y = 40;
    /** Surface of every lava river, lake and flooded crater. */
    public static final int LAVA_Y = 34;
    /** The arena: flat, paved, and nothing ever stands on it. */
    public static final double ARENA_RADIUS = 48;
    /** Width of the band where the arena floor blends into the wasteland. */
    public static final double APPROACH_WIDTH = 44;
    /** No lava, crater or structure closer to the centre than this. */
    public static final double HAZARD_RADIUS = 110;
    /** Half the side of the battlefield; the world border sits here by default. */
    public static final double HALF_SIZE = 750;
    /** Where the boundary range starts rising, measured as a rounded square like the border. */
    public static final double MOUNTAIN_START = 630;

    private static final int RIVER = 3;
    private static final int LAKE = 4;
    private static final int ZONE = 6;
    private static final int DETAIL = 7;
    private static final int FOOTHILLS = 8;
    private static final int PEAKS = 5;

    private final int salt;

    public Wasteland(long seed) {
        this.salt = TerrainNoise.saltOf(seed);
    }

    // ------------------------------------------------------------------ shape

    /** Distance to the centre measured as a rounded square, so the boundary range follows the square border. */
    public static double edgeDistance(double x, double z) {
        double ax = Math.abs(x);
        double az = Math.abs(z);
        return Math.sqrt(Math.sqrt(ax * ax * ax * ax + az * az * az * az));
    }

    /** 0 on the battlefield, rising to 1 in the boundary range. */
    double mountainMask(int x, int z) {
        double jitter = (fbm(x, z, salt + FOOTHILLS, 1 / 60.0, 2) - 0.5) * 70;
        return smoothstep(MOUNTAIN_START, HALF_SIZE - 25, edgeDistance(x, z) + jitter);
    }

    /** How strongly lava and craters may appear here: none near the arena or in the mountains. */
    double hazard(double r, double mountain) {
        return smoothstep(HAZARD_RADIUS, HAZARD_RADIUS + 90, r) * (1.0 - mountain);
    }

    /** Top solid block of the column. */
    public int surfaceY(int x, int z) {
        double r = Math.hypot(x, z);
        return (int) Math.round(height(x, z, r, mountainMask(x, z)));
    }

    private double height(int x, int z, double r, double mountain) {
        double h = FLOOR_Y
                + (fbm(x, z, salt + 1, 1 / 96.0, 4) - 0.5) * 16
                + (fbm(x, z, salt + 2, 1 / 21.0, 2) - 0.5) * 3;

        double hazard = hazard(r, mountain);
        if (hazard > 0) {
            // Rivers follow the crest line of a ridged field: thin, winding, and continuous.
            double river = smoothstep(0.955, 0.99, ridge(x, z, salt + RIVER, 1 / 280.0, 3)) * hazard;
            h = lerp(h, LAVA_Y - 4, river);
            double lake = smoothstep(0.66, 0.73, fbm(x, z, salt + LAKE, 1 / 330.0, 3)) * hazard;
            h = lerp(h, LAVA_Y - 3, lake);
            h += craters(x, z);
        }

        if (mountain > 0) {
            double crag = ridge(x, z, salt + PEAKS, 1 / 70.0, 4);
            h += mountain * (24 + 80 * Math.pow(crag, 1.6));
        }

        double flat = 1.0 - smoothstep(ARENA_RADIUS, ARENA_RADIUS + APPROACH_WIDTH, r);
        return lerp(h, FLOOR_Y, flat);
    }

    private static final int CRATER_CELL = 110;

    /** Height change from the craters around a column: a bowl with a raised lip. */
    private double craters(int x, int z) {
        double total = 0;
        int cx = Math.floorDiv(x, CRATER_CELL);
        int cz = Math.floorDiv(z, CRATER_CELL);
        for (int gx = cx - 1; gx <= cx + 1; gx++) {
            for (int gz = cz - 1; gz <= cz + 1; gz++) {
                if (unit(gx, gz, salt + 20) > 0.35) continue;
                double px = gx * CRATER_CELL + 25 + unit(gx, gz, salt + 21) * (CRATER_CELL - 50);
                double pz = gz * CRATER_CELL + 25 + unit(gx, gz, salt + 22) * (CRATER_CELL - 50);
                if (Math.hypot(px, pz) < HAZARD_RADIUS + 60 || edgeDistance(px, pz) > MOUNTAIN_START - 40) continue;
                double radius = 10 + unit(gx, gz, salt + 23) * 14;
                double depth = 5 + unit(gx, gz, salt + 24) * 9;
                double d = Math.hypot(x - px, z - pz) / radius;
                if (d < 1.0) total -= depth * (1.0 - d * d);
                if (d > 0.75 && d < 1.45) total += 2.5 * Math.max(0.0, 1.0 - Math.abs(d - 1.1) / 0.35);
            }
        }
        return total;
    }

    // ------------------------------------------------------------------ materials

    /** The three kinds of ground the wasteland is made of. */
    public enum Ground { SOUL, SCORCHED, BURNING }

    Ground ground(int x, int z) {
        double zone = fbm(x, z, salt + ZONE, 1 / 170.0, 3) + (unit(x, z, salt + 61) - 0.5) * 0.03;
        if (zone < 0.43) return Ground.SOUL;
        if (zone < 0.59) return Ground.SCORCHED;
        return Ground.BURNING;
    }

    /** The paved arena: brick checker, a crying-obsidian sigil of rings and spokes, a chiselled rim. */
    static Material arenaFloor(int x, int z, double r, int salt) {
        if (r < 3.5) return Material.CRYING_OBSIDIAN;
        if (r >= ARENA_RADIUS - 2.5) {
            double degrees = Math.toDegrees(Math.atan2(z, x)) + 360.0;
            return degrees % 22.5 < 2.0 ? Material.GILDED_BLACKSTONE : Material.CHISELED_POLISHED_BLACKSTONE;
        }
        if (Math.abs(r - 12) < 0.5 || Math.abs(r - 30) < 0.5 || Math.abs(r - 42) < 0.5) {
            return Material.CRYING_OBSIDIAN;
        }
        if (r < 30) {
            for (int spoke = 0; spoke < 8; spoke++) {
                double angle = spoke * Math.PI / 4;
                double along = x * Math.cos(angle) + z * Math.sin(angle);
                double across = -x * Math.sin(angle) + z * Math.cos(angle);
                if (along > 0 && Math.abs(across) < 0.55) return Material.CRYING_OBSIDIAN;
            }
        }
        if (unit(x, z, salt + 40) < 0.08) return Material.CRACKED_POLISHED_BLACKSTONE_BRICKS;
        return ((x >> 1) + (z >> 1)) % 2 == 0 ? Material.POLISHED_BLACKSTONE_BRICKS : Material.DEEPSLATE_BRICKS;
    }

    private Material topBlock(int x, int z, int surface, double r, double mountain, Ground ground) {
        if (r <= ARENA_RADIUS) return arenaFloor(x, z, r, salt);
        if (surface < LAVA_Y) return unit(x, z, salt + 41) < 0.35 ? Material.MAGMA_BLOCK : Material.BASALT;
        if (surface <= LAVA_Y + 1) return unit(x, z, salt + 42) < 0.45 ? Material.MAGMA_BLOCK : Material.BLACKSTONE;
        if (mountain > 0.35) {
            if (surface > FLOOR_Y + 72) {
                return unit(x, z, salt + 43) < 0.03 ? Material.CRYING_OBSIDIAN : Material.OBSIDIAN;
            }
            // The slopes show the strata they are cut from, which draws contour lines up the range.
            return stratum(surface, strataShift(x, z));
        }
        // The approach: paving rubble thinning out into the wasteland.
        double wild = smoothstep(ARENA_RADIUS, ARENA_RADIUS + APPROACH_WIDTH, r);
        if (unit(x, z, salt + 45) > wild) {
            double pick = unit(x, z, salt + 46);
            if (pick < 0.3) return Material.CRACKED_POLISHED_BLACKSTONE_BRICKS;
            if (pick < 0.55) return Material.POLISHED_BLACKSTONE;
            return Material.BLACKSTONE;
        }
        double detail = fbm(x, z, salt + DETAIL, 1 / 13.0, 2);
        return switch (ground) {
            case SOUL -> detail > 0.5 ? Material.SOUL_SOIL : Material.SOUL_SAND;
            case SCORCHED -> {
                if (detail > 0.68) yield Material.MAGMA_BLOCK;
                if (detail < 0.3) yield Material.BASALT;
                yield unit(x, z, salt + 47) < 0.01 ? Material.GILDED_BLACKSTONE : Material.BLACKSTONE;
            }
            case BURNING -> detail > 0.72 ? Material.MAGMA_BLOCK : Material.NETHERRACK;
        };
    }

    private static Material subSurface(Ground ground, double r) {
        if (r <= ARENA_RADIUS + APPROACH_WIDTH) return Material.BLACKSTONE;
        return switch (ground) {
            case SOUL -> Material.SOUL_SOIL;
            case SCORCHED -> Material.BLACKSTONE;
            case BURNING -> Material.NETHERRACK;
        };
    }

    /** Rock of a cliff: banded blackstone with obsidian seams, so mountain faces read as strata. */
    /** How far the strata are pushed up or down here, so the bands undulate instead of lying flat. */
    private int strataShift(int x, int z) {
        return (int) (fbm(x, z, salt + 48, 1 / 40.0, 1) * 6);
    }

    private static Material stratum(int y, int shift) {
        int band = Math.floorMod(y + shift, 11);
        if (band <= 1) return Material.OBSIDIAN;
        if (band == 5) return Material.BASALT;
        return Material.BLACKSTONE;
    }

    // ------------------------------------------------------------------ the column

    /** Writes one column, bedrock to the top of whatever stands on it. */
    public void write(int x, int z, ColumnWriter out) {
        double r = Math.hypot(x, z);
        double mountain = mountainMask(x, z);
        int surface = (int) Math.round(height(x, z, r, mountain));
        Ground ground = ground(x, z);

        out.fill(BEDROCK_Y, BEDROCK_Y, Material.BEDROCK);
        int deep = Math.min(surface - 13, FLOOR_Y - 10);
        out.fill(BEDROCK_Y + 1, deep, Material.DEEPSLATE);
        if (mountain > 0.15) {
            int shift = strataShift(x, z);
            for (int y = Math.max(deep + 1, BEDROCK_Y + 1); y <= surface - 4; y++) {
                out.fill(y, y, stratum(y, shift));
            }
        } else {
            out.fill(Math.max(deep + 1, BEDROCK_Y + 1), surface - 4, Material.BLACKSTONE);
        }
        out.fill(Math.max(surface - 3, BEDROCK_Y + 1), surface - 1, subSurface(ground, r));
        Material top = topBlock(x, z, surface, r, mountain, ground);
        out.fill(surface, surface, top);

        if (surface < LAVA_Y) {
            out.fill(surface + 1, LAVA_Y, Material.LAVA);
            return;
        }
        if (r <= ARENA_RADIUS + 40 || mountain > 0.05) return;

        boolean built = spikes(x, z, surface, out) | ruins(x, z, surface, out) | ribcages(x, z, surface, out);
        if (built) return;

        if ((top == Material.SOUL_SOIL || top == Material.SOUL_SAND) && hash(x, z, salt + 50) % 61 == 0) {
            out.fill(surface + 1, surface + 1, Material.SOUL_FIRE);
        } else if (top == Material.NETHERRACK && hash(x, z, salt + 51) % 47 == 0) {
            out.fill(surface + 1, surface + 1, Material.FIRE);
        }
    }

    // ------------------------------------------------------------------ structures

    private static final int SPIKE_CELL = 30;
    private static final int RUIN_CELL = 72;
    private static final int RIB_CELL = 120;

    /** Whether a structure may stand at this centre: away from the arena and out of the mountains. */
    private static boolean structureMayStandAt(double px, double pz) {
        return Math.hypot(px, pz) >= HAZARD_RADIUS && edgeDistance(px, pz) <= MOUNTAIN_START - 30;
    }

    private double cellX(int gx, int gz, int cell, int key) {
        return gx * (double) cell + 4 + unit(gx, gz, salt + key) * (cell - 8);
    }

    private double cellZ(int gx, int gz, int cell, int key) {
        return gz * (double) cell + 4 + unit(gx, gz, salt + key + 1) * (cell - 8);
    }

    /** Obsidian and basalt spikes: tapering needles, some of them twenty-odd blocks tall. */
    private boolean spikes(int x, int z, int surface, ColumnWriter out) {
        boolean built = false;
        int cx = Math.floorDiv(x, SPIKE_CELL);
        int cz = Math.floorDiv(z, SPIKE_CELL);
        for (int gx = cx - 1; gx <= cx + 1; gx++) {
            for (int gz = cz - 1; gz <= cz + 1; gz++) {
                if (unit(gx, gz, salt + 30) > 0.4) continue;
                double px = cellX(gx, gz, SPIKE_CELL, 31);
                double pz = cellZ(gx, gz, SPIKE_CELL, 31);
                if (!structureMayStandAt(px, pz)) continue;
                double radius = 1.8 + unit(gx, gz, salt + 33) * 2.6;
                double d = Math.hypot(x + 0.5 - px, z + 0.5 - pz);
                if (d >= radius) continue;
                double tall = 9 + Math.pow(unit(gx, gz, salt + 34), 1.5) * 30;
                int top = (int) Math.round(tall * Math.pow(1.0 - d / radius, 1.25));
                if (top < 1) continue;
                boolean basalt = unit(gx, gz, salt + 35) < 0.4;
                for (int y = surface + 1; y <= surface + top; y++) {
                    Material block;
                    if (y == surface + top && top > 4) block = basalt ? Material.MAGMA_BLOCK : Material.CRYING_OBSIDIAN;
                    else if (basalt) block = Material.BASALT;
                    else block = (y % 6 == 0) ? Material.CRYING_OBSIDIAN : Material.OBSIDIAN;
                    out.fill(y, y, block);
                }
                built = true;
            }
        }
        return built;
    }

    /** Broken colonnades: a ring of snapped blackstone pillars on a cracked floor. */
    private boolean ruins(int x, int z, int surface, ColumnWriter out) {
        boolean built = false;
        int cx = Math.floorDiv(x, RUIN_CELL);
        int cz = Math.floorDiv(z, RUIN_CELL);
        for (int gx = cx - 1; gx <= cx + 1; gx++) {
            for (int gz = cz - 1; gz <= cz + 1; gz++) {
                if (unit(gx, gz, salt + 70) > 0.3) continue;
                double px = cellX(gx, gz, RUIN_CELL, 71);
                double pz = cellZ(gx, gz, RUIN_CELL, 71);
                if (!structureMayStandAt(px, pz)) continue;
                double ring = 5 + unit(gx, gz, salt + 73) * 5;
                double dx = x + 0.5 - px;
                double dz = z + 0.5 - pz;
                double d = Math.hypot(dx, dz);
                if (d > ring + 1.5) continue;

                // The floor of the ruin, half buried.
                if (unit(x, z, salt + 74) < 0.65) {
                    out.fill(surface, surface, unit(x, z, salt + 75) < 0.5
                            ? Material.CRACKED_POLISHED_BLACKSTONE_BRICKS : Material.POLISHED_BLACKSTONE);
                    built = true;
                }
                int pillars = 6 + (int) (unit(gx, gz, salt + 76) * 4);
                double start = unit(gx, gz, salt + 77) * Math.PI * 2;
                for (int i = 0; i < pillars; i++) {
                    double angle = start + i * Math.PI * 2 / pillars;
                    double ox = Math.cos(angle) * ring;
                    double oz = Math.sin(angle) * ring;
                    if (Math.abs(dx - ox) > 1.0 || Math.abs(dz - oz) > 1.0) continue;
                    int height = (int) (unit(gx * 31 + i, gz, salt + 78) * 11);
                    for (int y = surface + 1; y <= surface + height; y++) {
                        Material block;
                        if (height >= 8 && y == surface + height) block = Material.CHISELED_POLISHED_BLACKSTONE;
                        else block = unit(x, y * 7 + z, salt + 79) < 0.3
                                ? Material.CRACKED_POLISHED_BLACKSTONE_BRICKS : Material.POLISHED_BLACKSTONE_BRICKS;
                        out.fill(y, y, block);
                    }
                    built = true;
                }
            }
        }
        return built;
    }

    /** The bones of something huge: a spine with ribs arching over the ground and a skull. */
    private boolean ribcages(int x, int z, int surface, ColumnWriter out) {
        boolean built = false;
        int cx = Math.floorDiv(x, RIB_CELL);
        int cz = Math.floorDiv(z, RIB_CELL);
        for (int gx = cx - 1; gx <= cx + 1; gx++) {
            for (int gz = cz - 1; gz <= cz + 1; gz++) {
                if (unit(gx, gz, salt + 90) > 0.3) continue;
                double px = cellX(gx, gz, RIB_CELL, 91);
                double pz = cellZ(gx, gz, RIB_CELL, 91);
                if (!structureMayStandAt(px, pz)) continue;
                double heading = unit(gx, gz, salt + 93) * Math.PI * 2;
                double length = 22 + unit(gx, gz, salt + 94) * 14;
                double size = 5 + unit(gx, gz, salt + 95) * 3;
                double dx = x + 0.5 - px;
                double dz = z + 0.5 - pz;
                double along = dx * Math.cos(heading) + dz * Math.sin(heading);
                double across = -dx * Math.sin(heading) + dz * Math.cos(heading);
                if (along < -5 || along > length + 1) continue;

                if (along < 0) {
                    // The skull: a lump of bone at the head end.
                    if (Math.hypot(along + 2.5, across) < 2.6) {
                        out.fill(surface + 1, surface + 3, Material.BONE_BLOCK);
                        built = true;
                    }
                    continue;
                }
                double ribHeight = size * Math.pow(Math.sin(Math.PI * Math.min(1.0, along / length)), 0.6);
                if (Math.abs(across) < 0.6 && ribHeight > 1) {
                    int y = surface + (int) Math.round(ribHeight * 1.15);
                    out.fill(y, y, Material.BONE_BLOCK);
                    built = true;
                }
                int rib = (int) Math.round(along / 3.0);
                if (rib < 1 || rib * 3.0 > length - 2 || Math.abs(along - rib * 3.0) > 0.5) continue;
                double radius = size * Math.pow(Math.sin(Math.PI * rib * 3.0 / length), 0.6);
                if (Math.abs(across) > radius + 0.5) continue;
                double outer = Math.min(1.0, (Math.abs(across) + 0.5) / radius);
                double inner = Math.max(0.0, (Math.abs(across) - 0.5) / radius);
                int low = surface + (int) Math.round(radius * 1.15 * Math.sqrt(1 - outer * outer));
                int high = surface + (int) Math.round(radius * 1.15 * Math.sqrt(1 - inner * inner));
                out.fill(Math.max(surface + 1, low), Math.max(surface + 1, high), Material.BONE_BLOCK);
                built = true;
            }
        }
        return built;
    }
}
