package com.Chagui68.testsupport;

import com.Chagui68.ritual.terrain.TerrainSink;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link TerrainSink} that keeps what was written to it, so the terrain generator can be driven
 * from a test without a server.
 *
 * <p>It mirrors the two things the real chunk sink does — it only accepts blocks inside the window
 * it was opened for, and only inside the world's height range — and it counts what it dropped.
 * That counter is what lets a test tell "the generator only writes where it should" apart from
 * "the generator writes the world and the sink throws most of it away": a landmark reaching into a
 * neighbouring chunk is expected, generating an entire canyon for every block is not.
 */
public final class RecordingTerrain implements TerrainSink {

    /** Height range of a real world: anything outside it could never be written to a chunk. */
    private static final int MIN_Y = -64;
    private static final int MAX_Y = 320;

    private final int minX;
    private final int minZ;
    private final int maxX;
    private final int maxZ;
    private final Map<Long, Material> blocks = new HashMap<>();
    private final Map<Long, Integer> topByColumn = new HashMap<>();
    private long dropped;

    private RecordingTerrain(int minX, int minZ, int size) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = minX + size - 1;
        this.maxZ = minZ + size - 1;
    }

    /** A sink accepting a square window; anything outside it is counted and discarded. */
    public static RecordingTerrain window(int minX, int minZ, int size) {
        return new RecordingTerrain(minX, minZ, size);
    }

    @Override
    public void set(int x, int y, int z, Material material) {
        if (x < minX || x > maxX || z < minZ || z > maxZ || y < MIN_Y || y > MAX_Y) {
            dropped++;
            return;
        }
        long key = key(x, y, z);
        blocks.put(key, material);
        long column = key(x, 0, z);
        topByColumn.merge(column, y, Math::max);
    }

    @Override
    public void column(int x, int z, int fromY, int toY, Material material) {
        for (int y = fromY; y <= toY; y++) {
            set(x, y, z, material);
        }
    }

    /** The material recorded at a position, or air when nothing was written. */
    public Material at(int x, int y, int z) {
        return blocks.getOrDefault(key(x, y, z), Material.AIR);
    }

    /** Whether anything was recorded at a position. */
    public boolean has(int x, int y, int z) {
        return blocks.containsKey(key(x, y, z));
    }

    /** The highest written y of a column, or {@link Integer#MIN_VALUE} when it stayed empty. */
    public int topY(int x, int z) {
        return topByColumn.getOrDefault(key(x, 0, z), Integer.MIN_VALUE);
    }

    /** How many writes landed outside the window, or outside the world's height range. */
    public long dropped() {
        return dropped;
    }

    /** How many blocks are recorded. */
    public int size() {
        return blocks.size();
    }

    /** A copy of the recorded blocks, keyed the same way {@link #at} looks them up. */
    public Map<Long, Material> snapshot() {
        return new HashMap<>(blocks);
    }

    /**
     * A position key: 21 bits of x, 21 bits of z and 12 bits of y packed into a long.
     *
     * <p>The fields have to be disjoint and nothing may be truncated: an earlier version offset the
     * coordinates into the high bits and lost the overflow, so two columns of a chunk collapsed
     * onto one key and a test could read a block that belonged to a neighbour. {@link #keyX},
     * {@link #keyZ} and {@link #keyY} undo the packing; coordinates are limited to ±1,048,576
     * blocks, which is far beyond any world.
     */
    public static long key(int x, int y, int z) {
        long column = ((long) (x & X_MASK) << 21) | (z & Z_MASK);
        return (column << 12) | ((y + 1024) & Y_MASK);
    }

    /** The x a {@link #key} was built from. */
    public static int keyX(long key) {
        return sign21((int) ((key >>> 33) & X_MASK));
    }

    /** The z a {@link #key} was built from. */
    public static int keyZ(long key) {
        return sign21((int) ((key >>> 12) & Z_MASK));
    }

    /** The y a {@link #key} was built from. */
    public static int keyY(long key) {
        return (int) (key & 0xFFF) - 1024;
    }

    private static int sign21(int value) {
        return value >= (1 << 20) ? value - (1 << 21) : value;
    }

    // 21 bits per horizontal coordinate: the number of bits is the whole contract here, so the
    // masks are named. Getting one of these wrong packed 25 bits where 21 belonged and let a
    // column's blocks land on a neighbour's key, which showed up as a flat arena floor reading
    // twenty blocks tall.
    private static final long X_MASK = 0x1F_FFFFL;
    private static final long Z_MASK = 0x1F_FFFFL;
    private static final long Y_MASK = 0xFFFL;
}
