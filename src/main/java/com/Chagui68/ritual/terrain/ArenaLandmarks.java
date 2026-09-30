package com.Chagui68.ritual.terrain;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * The landmarks that break up the wilderness: monoliths, ruined arches, shattered gateways, soul
 * wells, basalt spires and floating shards.
 *
 * <p>Two rules keep this out of the fight's way. Nothing is ever placed inside
 * {@link ArenaShape#LANDMARK_INNER_RADIUS} of the origin, so the view from the arena floor is
 * never blocked by a ruin; and each landmark is a pure function of its cell and the world seed, so
 * a chunk can draw its own slice of a landmark without asking any other chunk what it decided —
 * the same rule the height fields follow.
 *
 * <p>Landmarks are drawn per column: a landmark outside the chunk being generated is filtered out
 * by its bounding box before anything is written, so the cost is bounded by the couple of ruins
 * that can actually reach a chunk.
 */
public final class ArenaLandmarks {

    /** Size of the cell grid landmarks are scattered over. */
    public static final int CELL = 112;

    /** How many quarter turns a landmark can be rotated by. */
    private static final int ROTATIONS = 4;

    /** What a landmark is. */
    public enum Type {
        MONOLITH(6),
        ARCH(16),
        RING(12),
        WELL(7),
        SPIRES(11),
        SKY_SHARD(9);

        /** Radius of the footprint that decides which chunks have to draw it. */
        private final int footprint;

        Type(int footprint) {
            this.footprint = footprint;
        }

        /** Half-width of the bounding box around the landmark's centre. */
        public int footprint() {
            return footprint;
        }
    }

    /**
     * One landmark: its centre, its type, how big it is, which way it faces and how high it
     * floats. {@code y} is only meaningful for {@link Type#SKY_SHARD}, which ignores the ground.
     */
    public record Landmark(Type type, int x, int z, int size, int rotation, int y) {

        /** Whether any column of this landmark could land in the given block window. */
        public boolean overlaps(int minX, int minZ, int maxX, int maxZ) {
            int reach = type.footprint();
            return x + reach >= minX && x - reach <= maxX
                    && z + reach >= minZ && z - reach <= maxZ;
        }
    }

    private ArenaLandmarks() {
    }

    /** Types that stand on the ground. {@link Type#SKY_SHARD} is handled apart, high above it. */
    private static final Type[] GROUND_TYPES = {
            Type.MONOLITH, Type.ARCH, Type.RING, Type.WELL, Type.SPIRES};

    /**
     * The landmark of a cell, or null when the cell was left as wilderness.
     *
     * <p>One cell in four stays empty, and everything within {@link
     * ArenaShape#LANDMARK_INNER_RADIUS} is skipped outright so the ring of peaks the players see
     * over the parapet keeps its silhouette.
     */
    public static Landmark at(int cellX, int cellZ, long seed) {
        int salt = ArenaNoise.saltOf(seed) ^ 0x2C1B_3E4D;
        int hash = ArenaNoise.hash(cellX, cellZ, salt);
        if ((hash & 3) == 0) return null;

        int x = cellX * CELL + 28 + Math.floorMod(hash >>> 4, CELL - 56);
        int z = cellZ * CELL + 28 + Math.floorMod(hash >>> 12, CELL - 56);
        double distance = ArenaShape.distance(x, z);
        if (distance < ArenaShape.LANDMARK_INNER_RADIUS) return null;

        int size = 3 + Math.floorMod(hash >>> 8, 4);
        int rotation = Math.floorMod(hash >>> 16, ROTATIONS);
        Type type = GROUND_TYPES[Math.floorMod(hash >>> 20, GROUND_TYPES.length)];
        return new Landmark(type, x, z, size, rotation, ArenaShape.surfaceY(x, z, seed));
    }

    /**
     * The shards that hang around the coliseum.
     *
     * <p>Eight of them, deliberately placed between the buttress ribs — which sit on multiples of
     * {@code 360 / ArenaShape.RIB_COUNT} degrees — so the crown reads as alternating ribs and
     * floating rock instead of a collision of the two.
     */
    public static List<Landmark> skyShards() {
        List<Landmark> shards = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            double degrees = 22.5 + index * 45.0;
            double radians = Math.toRadians(degrees);
            int x = (int) Math.round(96.0 * Math.cos(radians));
            int z = (int) Math.round(96.0 * Math.sin(radians));
            shards.add(new Landmark(Type.SKY_SHARD, x, z, 3 + index % 3, index % ROTATIONS,
                    44 + (index % 3) * 4));
        }
        return shards;
    }

    /** Every landmark whose bounding box can reach a block window, shards included. */
    public static List<Landmark> touching(int minX, int minZ, int maxX, int maxZ, long seed) {
        List<Landmark> found = new ArrayList<>();
        int minCellX = Math.floorDiv(minX, CELL) - 1;
        int maxCellX = Math.floorDiv(maxX, CELL) + 1;
        int minCellZ = Math.floorDiv(minZ, CELL) - 1;
        int maxCellZ = Math.floorDiv(maxZ, CELL) + 1;
        for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
            for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
                Landmark landmark = at(cellX, cellZ, seed);
                if (landmark != null && landmark.overlaps(minX, minZ, maxX, maxZ)) {
                    found.add(landmark);
                }
            }
        }
        for (Landmark shard : skyShards()) {
            if (shard.overlaps(minX, minZ, maxX, maxZ)) {
                found.add(shard);
            }
        }
        return found;
    }

    /** Draws a landmark. Writes outside the sink's window are dropped by the sink. */
    public static void write(Landmark landmark, long seed, TerrainSink sink) {
        switch (landmark.type()) {
            case MONOLITH -> writeMonolith(landmark, sink);
            case ARCH -> writeArch(landmark, seed, sink);
            case RING -> writeRing(landmark, sink);
            case WELL -> writeWell(landmark, seed, sink);
            case SPIRES -> writeSpires(landmark, seed, sink);
            case SKY_SHARD -> writeShard(landmark, sink);
        }
    }

    // ------------------------------------------------------------------ shapes

    private static void writeMonolith(Landmark landmark, TerrainSink sink) {
        int half = 2;
        int top = landmark.y() + 26 + landmark.size() * 4;
        // Plinth, then the shaft, then a gilded crown with a glowing cap.
        for (int dx = -half - 1; dx <= half + 1; dx++) {
            for (int dz = -half - 1; dz <= half + 1; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3 + half) {
                    sink.set(landmark.x() + dx, landmark.y(), landmark.z() + dz,
                            Material.POLISHED_BLACKSTONE_BRICKS);
                }
            }
        }
        for (int y = landmark.y() + 1; y <= top; y++) {
            Material shaft = y >= top - 1 ? Material.GILDED_BLACKSTONE
                    : y % 7 == 0 ? Material.CRYING_OBSIDIAN
                    : Material.OBSIDIAN;
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    boolean corner = Math.abs(dx) == half && Math.abs(dz) == half;
                    if (corner && landmark.size() < 5) continue;
                    sink.set(landmark.x() + dx, y, landmark.z() + dz, shaft);
                }
            }
        }
        sink.set(landmark.x(), top + 1, landmark.z(), Material.SHROOMLIGHT);
        sink.set(landmark.x() + 1, top, landmark.z(), Material.SHROOMLIGHT);
        sink.set(landmark.x() - 1, top, landmark.z(), Material.SHROOMLIGHT);
    }

    private static void writeArch(Landmark landmark, long seed, TerrainSink sink) {
        int span = 8 + landmark.size();
        int height = 22 + landmark.size() * 3;
        int[] along = axis(landmark.rotation());
        int[] across = {-along[1], along[0]};
        Material tower = Material.CRACKED_POLISHED_BLACKSTONE_BRICKS;

        for (int side = -1; side <= 1; side += 2) {
            int towerX = landmark.x() + along[0] * span * side;
            int towerZ = landmark.z() + along[1] * span * side;
            int ground = ArenaShape.surfaceY(towerX, towerZ, seed);
            for (int y = ground; y <= ground + height; y++) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        boolean corner = Math.abs(dx) == 1 && Math.abs(dz) == 1;
                        if (corner && y % 5 != 0) continue;
                        Material block = y % 6 == 0 ? Material.CRYING_OBSIDIAN
                                : y == ground + height ? Material.POLISHED_BLACKSTONE_BRICKS
                                : tower;
                        sink.set(towerX + dx, y, towerZ + dz, block);
                    }
                }
            }
            sink.set(towerX, ground + height + 1, towerZ, Material.SHROOMLIGHT);
        }

        // A broken span: solid for a while, then a gap with chains hanging into it.
        int deck = landmark.y() + height - 4;
        for (int alongStep = -span + 2; alongStep <= span - 2; alongStep++) {
            int deckX = landmark.x() + along[0] * alongStep;
            int deckZ = landmark.z() + along[1] * alongStep;
            boolean broken = Math.abs(alongStep - 2) <= 2;
            for (int wide = -1; wide <= 1; wide++) {
                int x = deckX + across[0] * wide;
                int z = deckZ + across[1] * wide;
                if (broken) {
                    // The gap is the point of the ruin: only the chains still span it.
                    int chain = 3 + Math.floorMod(ArenaNoise.hash(x, z, 991), 5);
                    sink.set(x, deck, z, Material.IRON_CHAIN);
                    sink.set(x, deck - chain, z, Material.IRON_CHAIN);
                } else {
                    sink.set(x, deck, z, Material.CRYING_OBSIDIAN);
                    sink.set(x, deck + 1, z, Material.POLISHED_BLACKSTONE_BRICKS);
                    if (wide == 0) sink.set(x, deck + 2, z, Material.POLISHED_BLACKSTONE_BRICK_SLAB);
                }
            }
        }
    }

    private static void writeRing(Landmark landmark, TerrainSink sink) {
        int radius = 6 + landmark.size();
        int[] along = axis(landmark.rotation());
        int[] across = {-along[1], along[0]};
        for (int dy = -radius - 2; dy <= radius + 2; dy++) {
            for (int wide = -radius - 2; wide <= radius + 2; wide++) {
                double distance = Math.sqrt((double) dy * dy + (double) wide * wide);
                if (distance > radius + 1.5) continue;
                int x = landmark.x() + across[0] * wide;
                int z = landmark.z() + across[1] * wide;
                int y = landmark.y() + radius + dy;
                if (y <= ArenaShape.MIN_BUILD_Y) continue;
                Material block = distance >= radius - 1.5
                        ? Material.POLISHED_BLACKSTONE_BRICKS
                        : Material.CRYING_OBSIDIAN;
                sink.set(x, y, z, block);
            }
        }
        for (int wide = -radius - 2; wide <= radius + 2; wide++) {
            int x = landmark.x() + across[0] * wide;
            int z = landmark.z() + across[1] * wide;
            for (int y = landmark.y() - 2; y <= landmark.y(); y++) {
                sink.set(x, y, z, y == landmark.y() ? Material.CHISELED_POLISHED_BLACKSTONE
                        : Material.POLISHED_BLACKSTONE_BRICKS);
            }
        }
    }

    private static void writeWell(Landmark landmark, long seed, TerrainSink sink) {
        int radius = 4 + landmark.size() / 3;
        int depth = 6 + landmark.size();
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                double distance = Math.sqrt((double) dx * dx + (double) dz * dz);
                int x = landmark.x() + dx;
                int z = landmark.z() + dz;
                int ground = ArenaShape.surfaceY(x, z, seed);
                if (distance > radius + 0.5) continue;
                if (distance >= radius - 0.5) {
                    for (int y = ground; y <= ground + 2; y++) {
                        sink.set(x, y, z, y == ground + 2 ? Material.CHISELED_POLISHED_BLACKSTONE
                                : Material.POLISHED_BLACKSTONE_BRICKS);
                    }
                    if (ArenaNoise.hash(x, z, 733) % 5 == 0) {
                        sink.set(x, ground + 3, z, Material.IRON_CHAIN);
                    }
                } else {
                    int bottom = ground - depth;
                    sink.column(x, z, bottom, ground - 1, Material.AIR);
                    sink.set(x, bottom - 1, z, Material.SOUL_SOIL);
                    sink.set(x, bottom - 2, z, Material.SOUL_SOIL);
                    if (distance <= radius - 2.5 && ArenaNoise.hash(x, z, 811) % 3 == 0) {
                        sink.set(x, bottom, z, Material.SOUL_FIRE);
                    }
                }
            }
        }
    }

    private static void writeSpires(Landmark landmark, long seed, TerrainSink sink) {
        int count = 5 + landmark.size();
        for (int index = 0; index < count; index++) {
            int hash = ArenaNoise.hash(landmark.x() + index, landmark.z() - index, 617);
            double angle = Math.toRadians(Math.floorMod(hash, 360));
            double radius = 2.0 + Math.floorMod(hash >>> 9, 9);
            int x = landmark.x() + (int) Math.round(Math.cos(angle) * radius);
            int z = landmark.z() + (int) Math.round(Math.sin(angle) * radius);
            int ground = ArenaShape.surfaceY(x, z, seed);
            int height = 10 + Math.floorMod(hash >>> 3, 17) + landmark.size();
            int width = height > 20 ? 1 : 0;
            for (int y = ground; y <= ground + height; y++) {
                for (int dx = -width; dx <= width; dx++) {
                    for (int dz = -width; dz <= width; dz++) {
                        Material block = y == ground + height ? Material.MAGMA_BLOCK
                                : y % 9 == 0 ? Material.CRYING_OBSIDIAN
                                : Material.BASALT;
                        sink.set(x + dx, y, z + dz, block);
                    }
                }
            }
        }
    }

    private static void writeShard(Landmark landmark, TerrainSink sink) {
        int radius = 4 + landmark.size();
        int halfHeight = 3;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double flat = (double) dx * dx + (double) dz * dz;
                if (flat > (double) radius * radius) continue;
                double shape = 1.0 - flat / ((double) radius * radius);
                int top = (int) Math.round(halfHeight * Math.sqrt(shape));
                int x = landmark.x() + dx;
                int z = landmark.z() + dz;
                for (int dy = -top; dy <= top; dy++) {
                    int y = landmark.y() + dy;
                    boolean shell = dy == top || dy == -top || Math.abs(dx) == radius - 1;
                    Material block = shell ? Material.OBSIDIAN : Material.DEEPSLATE;
                    if (dy == -top && ArenaNoise.hash(x, z, 421) % 6 == 0) block = Material.SHROOMLIGHT;
                    if (dy == top && ArenaNoise.hash(x, z, 429) % 11 == 0) block = Material.CRYING_OBSIDIAN;
                    sink.set(x, y, z, block);
                }
                if (halfHeight > 0 && Math.abs(dx) + Math.abs(dz) <= 2) {
                    int hang = 2 + Math.floorMod(ArenaNoise.hash(x, z, 431), 4);
                    sink.set(x, landmark.y() - halfHeight - hang, z, Material.IRON_CHAIN);
                    sink.set(x, landmark.y() - halfHeight - 1, z, Material.BASALT);
                }
            }
        }
    }

    /** Quarter-turn direction along the landmark's axis. */
    private static int[] axis(int rotation) {
        return switch (Math.floorMod(rotation, ROTATIONS)) {
            case 0 -> new int[]{1, 0};
            case 1 -> new int[]{0, 1};
            case 2 -> new int[]{-1, 0};
            default -> new int[]{0, -1};
        };
    }
}
