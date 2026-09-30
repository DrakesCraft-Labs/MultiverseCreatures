package com.Chagui68.ritual.terrain;

import org.bukkit.Material;

/**
 * Where the terrain generator writes blocks.
 *
 * <p>The generator never touches {@code ChunkData} directly: it writes world coordinates into a
 * sink, and the sink decides what lands. That keeps the whole shape — the arena, the terraces,
 * the canyons, the landmarks — a pure function that a unit test can run without a server, while
 * the real sink is the only place that knows the current chunk and drops writes outside it (a
 * landmark straddling a chunk boundary is drawn by whichever chunk owns each column).
 */
public interface TerrainSink {

    /** Writes one block, in world coordinates. */
    void set(int x, int y, int z, Material material);

    /** Fills a vertical run of the same material, both ends inclusive. */
    void column(int x, int z, int fromY, int toY, Material material);

    /** Filler for tests and for landmark details: a run of one block. */
    default void set(int x, int y, int z, Material material, int count) {
        for (int offset = 0; offset < count; offset++) {
            set(x, y + offset, z, material);
        }
    }
}
