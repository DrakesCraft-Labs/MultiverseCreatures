package com.Chagui68.ritual.terrain;

/**
 * Deterministic value noise for the ritual dimension.
 *
 * <p>Everything here is a pure function of the coordinates and a salt derived from the world seed,
 * which is what lets two chunks agree on the column they share without ever looking at each other.
 * All fields return values in {@code [0, 1)}.
 */
public final class TerrainNoise {

    private TerrainNoise() {
    }

    /** Mixes two coordinates and a salt into a well distributed hash. */
    public static int hash(int x, int z, int salt) {
        long h = (long) x * 0x9E3779B97F4A7C15L
                + (long) z * 0xC2B2AE3D27D4EB4FL
                + (long) salt * 0x165667B19E3779F9L;
        h ^= h >>> 29;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 32;
        h *= 0x94D049BB133111EBL;
        h ^= h >>> 29;
        return (int) (h ^ (h >>> 32));
    }

    /** A uniform value in {@code [0, 1)} for one lattice point. */
    public static double unit(int x, int z, int salt) {
        return (hash(x, z, salt) >>> 8) * 0x1.0p-24;
    }

    /** Value noise in {@code [0, 1)}, sampled in lattice units. */
    public static double value(double x, double z, int salt) {
        int xi = (int) Math.floor(x);
        int zi = (int) Math.floor(z);
        double tx = smooth(x - xi);
        double tz = smooth(z - zi);
        double south = lerp(unit(xi, zi, salt), unit(xi + 1, zi, salt), tx);
        double north = lerp(unit(xi, zi + 1, salt), unit(xi + 1, zi + 1, salt), tx);
        return lerp(south, north, tz);
    }

    /**
     * Fractal value noise in {@code [0, 1)}.
     *
     * @param scale lattice cells per block: smaller means wider features
     */
    public static double fbm(double x, double z, int salt, double scale, int octaves) {
        double sum = 0.0;
        double amplitude = 1.0;
        double normalise = 0.0;
        double frequency = scale;
        for (int octave = 0; octave < octaves; octave++) {
            sum += amplitude * value(x * frequency, z * frequency, salt + octave * 101);
            normalise += amplitude;
            amplitude *= 0.5;
            frequency *= 2.0;
        }
        return sum / normalise;
    }

    /** Ridged noise in {@code [0, 1)}: thin crests reach 1, everything else falls towards 0. */
    public static double ridge(double x, double z, int salt, double scale, int octaves) {
        return 1.0 - Math.abs(2.0 * fbm(x, z, salt, scale, octaves) - 1.0);
    }

    public static double smooth(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    public static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    /** Smoothstep from {@code edge0} to {@code edge1}; 0 below, 1 above (or the reverse when descending). */
    public static double smoothstep(double edge0, double edge1, double value) {
        if (edge0 == edge1) return value < edge0 ? 0.0 : 1.0;
        double t = (value - edge0) / (edge1 - edge0);
        return smooth(Math.max(0.0, Math.min(1.0, t)));
    }

    /** A stable salt from a world seed, so two worlds grow different wastelands. */
    public static int saltOf(long seed) {
        return (int) (seed ^ (seed >>> 32));
    }
}
