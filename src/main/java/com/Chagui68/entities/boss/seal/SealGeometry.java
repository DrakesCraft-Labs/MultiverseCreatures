package com.Chagui68.entities.boss.seal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The numbers and the points every magic seal is built from, with no server in sight.
 *
 * <p>The seals are drawn one particle at a time by {@code MagicSealListener}, which is why they
 * used to be impossible to test: a shape and a particle call were the same line. Everything that
 * decides <em>where</em> the particles go lives here as a pure function of radius, samples and
 * frame, and the listener only walks the result. That split is what the seal tests hold down, and
 * it is also what lets one circle or one pentagram be reused by six different seals.
 */
public final class SealGeometry {

    /** The inner radius of the star ring, as a fraction of its outer one. */
    public static final double INNER_STAR = 0.55;

    /** How much taller a seal drawn on a vertical plane is, so it reads at the same size. */
    public static final double VERTICAL_SCALE = 1.3;

    private SealGeometry() {
    }

    // ------------------------------------------------------- shapes in the plane

    /** A full circle: {@code samples} points, the first one at {@code rotation}. */
    public static List<SealPoint> circle(double radius, int samples, double rotation) {
        List<SealPoint> points = new ArrayList<>(samples);
        double step = 2 * Math.PI / samples;
        for (int i = 0; i < samples; i++) {
            double angle = i * step + rotation;
            points.add(new SealPoint(radius * Math.cos(angle), radius * Math.sin(angle)));
        }
        return points;
    }

    /** The 5 chords of a pentagram, each sampled as its own run of points. */
    public static List<List<SealPoint>> pentagram(double radius, int samplesPerEdge, double startAngle) {
        SealPoint[] vertices = new SealPoint[5];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = point(radius, startAngle + i * (2 * Math.PI / 5));
        }
        int[] order = {0, 2, 4, 1, 3, 0};
        List<List<SealPoint>> chords = new ArrayList<>(5);
        for (int i = 0; i < order.length - 1; i++) {
            chords.add(chord(vertices[order[i]], vertices[order[i + 1]], samplesPerEdge));
        }
        return chords;
    }

    /** The 3 edges of an equilateral triangle, dividing a {@code samples} budget over them. */
    public static List<List<SealPoint>> triangle(double radius, int samples, double rotation) {
        int perEdge = Math.max(1, samples / 3);
        List<List<SealPoint>> edges = new ArrayList<>(3);
        SealPoint[] vertices = new SealPoint[3];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = point(radius, rotation + i * (2 * Math.PI / 3));
        }
        for (int i = 0; i < vertices.length; i++) {
            edges.add(chord(vertices[i], vertices[(i + 1) % vertices.length], perEdge));
        }
        return edges;
    }

    /**
     * A ring of 12 segments zig-zagging between {@code radius} and {@link #INNER_STAR} of it: out,
     * in, out again, all the way around, closing on the point it started from.
     */
    public static List<List<SealPoint>> starRing(double radius, int samples, double rotation) {
        int perSegment = Math.max(1, samples / 12);
        List<List<SealPoint>> segments = new ArrayList<>(12);
        for (int i = 0; i < 6; i++) {
            double first = rotation + i * Math.PI / 3;
            double second = first + Math.PI / 3;
            SealPoint outer = point(radius, first);
            SealPoint inner = point(radius * INNER_STAR, second);
            SealPoint next = point(radius, second);
            segments.add(chord(outer, inner, perSegment));
            segments.add(chord(inner, next, perSegment));
        }
        return segments;
    }

    /** A ring of {@code count} spokes from {@code innerRadius} out to {@code outerRadius}. */
    public static List<List<SealPoint>> spokes(double innerRadius, double outerRadius, int count,
                                               double rotation, int samplesPerSpoke) {
        List<SealPoint> inner = circle(innerRadius, count, rotation);
        List<SealPoint> outer = circle(outerRadius, count, rotation);
        List<List<SealPoint>> spokes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            spokes.add(chord(inner.get(i), outer.get(i), Math.max(1, samplesPerSpoke)));
        }
        return spokes;
    }

    /** A chord of the plane, sampled from {@code from} to {@code to}, both ends included. */
    public static List<SealPoint> chord(SealPoint from, SealPoint to, int samples) {
        List<SealPoint> points = new ArrayList<>(samples + 1);
        for (int i = 0; i <= samples; i++) {
            double f = (double) i / samples;
            points.add(new SealPoint(
                    from.u() + (to.u() - from.u()) * f,
                    from.v() + (to.v() - from.v()) * f));
        }
        return points;
    }

    /** Points whose radius wanders inside {@code [minRadius, maxRadius]} — a rune band. */
    public static List<SealPoint> runes(double minRadius, double maxRadius, int samples, Random random) {
        List<SealPoint> points = new ArrayList<>(samples);
        double step = 2 * Math.PI / samples;
        for (int i = 0; i < samples; i++) {
            double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);
            points.add(point(radius, i * step));
        }
        return points;
    }

    /** A random point of the ring between {@code minRadius} and {@code maxRadius}. */
    public static SealPoint randomRadial(double minRadius, double maxRadius, Random random) {
        double angle = random.nextDouble() * 2 * Math.PI;
        double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);
        return point(radius, angle);
    }

    /** The two diagonals of an executioner's cross, turned by {@code rotation} radians. */
    public static List<List<SealPoint>> cross(double size, double rotation, int samplesPerDiagonal) {
        List<List<SealPoint>> diagonals = new ArrayList<>(2);
        for (int diagonal = 0; diagonal < 2; diagonal++) {
            List<SealPoint> points = new ArrayList<>(samplesPerDiagonal + 1);
            for (int i = 0; i <= samplesPerDiagonal; i++) {
                double f = (double) i / samplesPerDiagonal;
                double x = -size + 2 * size * f;
                double y = (diagonal == 0 ? 1 : -1) * (size - 2 * size * f);
                points.add(rotate(x, y, rotation));
            }
            diagonals.add(points);
        }
        return diagonals;
    }

    /** A point turned around the origin, counter-clockwise. */
    public static SealPoint rotate(double u, double v, double rotation) {
        double cos = Math.cos(rotation);
        double sin = Math.sin(rotation);
        return new SealPoint(u * cos - v * sin, u * sin + v * cos);
    }

    /** A world-space chord, sampled from {@code from} to {@code to}, both ends included. */
    public static List<double[]> line(double[] from, double[] to, int samples) {
        List<double[]> points = new ArrayList<>(samples + 1);
        for (int i = 0; i <= samples; i++) {
            double f = (double) i / samples;
            points.add(new double[]{
                    from[0] + (to[0] - from[0]) * f,
                    from[1] + (to[1] - from[1]) * f,
                    from[2] + (to[2] - from[2]) * f});
        }
        return points;
    }

    /**
     * The horizontal offset of a point {@code u} along the seal's own +u axis, turned by the yaw
     * the seal faces — the way {@code spawnFlamingPentagram} rotates its star into the stand.
     */
    public static double[] yawOffset(double u, double yawDegrees) {
        double yaw = Math.toRadians(-yawDegrees);
        return new double[]{u * Math.cos(yaw), u * Math.sin(yaw)};
    }

    // ------------------------------------------------------- density and scale

    /** Sample budget of a big pentagram: never fewer than 60, more as the seal grows. */
    public static int pentagramSamples(double radius) {
        return Math.max(60, (int) (60 * radius / 6.0));
    }

    /** Sample budget of the ring that encloses a big pentagram: never fewer than 220. */
    public static int ringSamples(double radius) {
        return Math.max(220, (int) (220 * radius / 6.0));
    }

    /** How many flames a big pentagram's aura gets: it grows with the seal. */
    public static int auraCount(double radius) {
        return (int) (28 * radius / 6.0);
    }

    /** How far out the pentagram's aura burns, as a fraction of the seal. */
    public static double auraRadius(double radius) {
        return radius * 0.42;
    }

    /** The ring that encloses a pentagram, just outside its points. */
    public static double enclosingRingRadius(double radius) {
        return radius * 1.24;
    }

    /** The four radii of a celestial seal, scaled up when the seal stands instead of lying flat. */
    public static CelestialRadii celestialRadii(SealPlane plane) {
        double scale = plane.vertical() ? VERTICAL_SCALE : 1.0;
        return new CelestialRadii(3.0 * scale, 2.0 * scale, 2.5 * scale, 1.4 * scale);
    }

    /** Outer ring, middle ring, star ring and triangle of a celestial seal. */
    public record CelestialRadii(double outer, double middle, double star, double triangle) {
    }

    // ------------------------------------------------------- wandering effects

    /** The vortex orbit stays inside 38–62 % of the seal, breathing with the phase. */
    public static double vortexRadius(double radius, double phase) {
        return radius * (0.3 + (Math.sin(phase) * 0.3 + 0.5) * 0.4);
    }

    /** The quake ring sits at 80 % of the seal, wandering half a block either way. */
    public static double quakeRadius(double radius, double phase) {
        return radius * 0.8 + Math.sin(phase) * 0.5;
    }

    /** The divine ring sits at 75 % of the seal, wandering 0.3 blocks either way. */
    public static double divineRadius(double radius, double phase) {
        return radius * 0.75 + Math.sin(phase) * 0.3;
    }

    /** The cross pulses 8 % around its size while the execution runs. */
    public static double crossScale(double size, int ticks) {
        return size * (1.0 + 0.08 * Math.sin(ticks * 0.12));
    }

    // ------------------------------------------------------- timing rules

    /** The shield's column: at least half a block, exactly the drop when the shield hangs higher. */
    public static double cylinderHeight(double topY, double groundY) {
        return Math.max(0.5, topY - groundY);
    }

    /** Where sample {@code index} of an ascending spiral sits, always inside the column. */
    public static double spiralHeight(int index, double stepPerSample, double phase, double height) {
        double raw = index * stepPerSample + phase;
        double wrapped = raw % height;
        return wrapped < 0 ? wrapped + height : wrapped;
    }

    /** Whether this frame is a multiple of {@code period} — a repaint every so often, not every frame. */
    public static boolean every(int ticks, int period) {
        return ticks % period == 0;
    }

    /** Whether the effect has entered its last {@code window} ticks. */
    public static boolean inFinalWindow(int ticks, int durationTicks, int window) {
        return ticks >= durationTicks - window;
    }

    /** How far a sequence has run, as a fraction in {@code [0, 1]}. */
    public static double progress(int ticks, int durationTicks) {
        return (double) ticks / durationTicks;
    }

    /** Whether the sequence is past its {@code threshold} — the executioner's cross turns bright. */
    public static boolean latePhase(double progress, double threshold) {
        return progress > threshold;
    }

    private static SealPoint point(double radius, double angle) {
        return new SealPoint(radius * Math.cos(angle), radius * Math.sin(angle));
    }
}
