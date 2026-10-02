package com.Chagui68.entities.boss.seal;

import java.util.ArrayList;
import java.util.List;

/**
 * The wings a boss spreads, as a pure function of pose, frame and profile.
 *
 * <p>Each wing is built like a bird's or an angel's: a bone that leaves the back between the
 * shoulder blades, rises through an elbow and a wrist and ends in a hooked tip; long flight feathers
 * that hang from that bone, fanning out towards the tip; and a shorter row of coverts over their
 * roots. The old wings were a fan of straight lines all leaving one shoulder point, which read as a
 * bundle of sticks rather than a wing.
 *
 * <p>A flap turns the whole wing rigidly about its root, around the body's backward axis, so the
 * tips sweep up and down. Colours are the caller's business: the same strokes are painted gold for
 * the golden seal and red for the burning one.
 *
 * <p>The layout is built around the body's side vector ({@code +x} at yaw 0) and its backward
 * vector, so a stand that turns carries its wings with it.
 */
public final class WingGeometry {

    /** The pale wings of the golden seal. */
    public static final Profile GOLDEN_WINGS = new Profile(9, 9.6, 1.1, 0.9, 9.0, 4.5, 0.45, 0.09, 8);

    /** The longer, fuller wings of the Sentinel: more feathers and a slower, deeper beat. */
    public static final Profile BURNING_WINGS = new Profile(12, 9.8, 1.2, 1.0, 11.0, 5.5, 0.55, 0.07, 9);

    /** The shape of the bone, as fractions of the wing's length: root, elbow, wrist, tip (out, up, back). */
    private static final double[][] BONE = {
            {0.00, 0.00, 0.00},
            {0.36, 0.34, 0.12},
            {0.70, 0.46, 0.22},
            {1.00, 0.28, 0.42}};

    private WingGeometry() {
    }

    /**
     * One wing style.
     *
     * @param feathers       flight feathers per wing
     * @param shoulderHeight how high the wing roots sit above the stand's feet
     * @param shoulderWidth  half the distance between the two roots
     * @param rootBack       how far behind the body's centre the roots are
     * @param length         how far out the wing reaches, root to tip
     * @param featherLength  the length of the longest flight feather
     * @param flapAmplitude  how far the wing swings, in radians
     * @param flapSpeed      flap speed per tick
     * @param samples        subdivisions along a stroke (points drawn = samples + 1)
     */
    public record Profile(int feathers, double shoulderHeight, double shoulderWidth, double rootBack,
                          double length, double featherLength, double flapAmplitude, double flapSpeed,
                          int samples) {
    }

    /** What a stroke is, so it can be painted accordingly. */
    public enum Part {BONE, FEATHER, COVERT}

    /** One line of the wing, from its root end to its free end. */
    public record Stroke(Part part, List<WingPoint> points) {
    }

    /** The flap of a frame: a slow sine around the spread pose. */
    public static double flap(int ticks, Profile profile) {
        return Math.sin(ticks * profile.flapSpeed()) * profile.flapAmplitude();
    }

    /** Where a wing roots, side {@code -1} or {@code +1}. */
    public static WingPoint root(double cx, double cy, double cz, double yawDegrees, int side, Profile profile) {
        double yaw = Math.toRadians(yawDegrees);
        double sideX = Math.cos(yaw), sideZ = Math.sin(yaw);
        double backX = Math.sin(yaw), backZ = -Math.cos(yaw);
        return new WingPoint(
                cx + side * profile.shoulderWidth() * sideX + profile.rootBack() * backX,
                cy + profile.shoulderHeight(),
                cz + side * profile.shoulderWidth() * sideZ + profile.rootBack() * backZ);
    }

    /**
     * Every stroke of both wings, side {@code -1} first: the bone, then each flight feather with
     * its covert. Every stroke starts on the bone and ends at its free end.
     */
    public static List<Stroke> strokes(double cx, double cy, double cz, double yawDegrees, int ticks, Profile profile) {
        double yaw = Math.toRadians(yawDegrees);
        double sideX = Math.cos(yaw), sideZ = Math.sin(yaw);
        double backX = Math.sin(yaw), backZ = -Math.cos(yaw);
        double flap = flap(ticks, profile);
        double cos = Math.cos(flap), sin = Math.sin(flap);
        List<Stroke> out = new ArrayList<>(2 * (1 + 2 * profile.feathers()));
        for (int side = -1; side <= 1; side += 2) {
            WingPoint root = root(cx, cy, cz, yawDegrees, side, profile);
            // Local wing space (out, up, back) -> world, with the flap turning out/up about the root.
            int s = side;
            Local toWorld = (o, u, b) -> {
                double fo = o * cos - u * sin;
                double fu = o * sin + u * cos;
                return new WingPoint(
                        root.x() + s * fo * sideX + b * backX,
                        root.y() + fu,
                        root.z() + s * fo * sideZ + b * backZ);
            };

            List<WingPoint> bone = new ArrayList<>();
            int boneSamples = profile.samples() * 2;
            for (int i = 0; i <= boneSamples; i++) {
                double[] p = boneAt(i / (double) boneSamples);
                bone.add(toWorld.at(p[0] * profile.length(), p[1] * profile.length(), p[2] * profile.length()));
            }
            out.add(new Stroke(Part.BONE, bone));

            for (int f = 0; f < profile.feathers(); f++) {
                double t = profile.feathers() == 1 ? 1 : f / (double) (profile.feathers() - 1);
                // Feathers grow from the elbow region to the tip; the primaries near the wrist are longest.
                double along = 0.18 + 0.82 * t;
                double[] base = boneAt(along);
                double len = profile.featherLength() * (0.55 + 0.45 * Math.sin(Math.PI * Math.min(1, t * 1.15)));
                // They hang down and back, fanning outwards as they near the tip.
                double dirOut = 0.15 + 0.75 * t * t;
                double dirUp = -1.0 + 0.35 * t * t;
                double dirBack = 0.45 - 0.15 * t;
                double norm = Math.sqrt(dirOut * dirOut + dirUp * dirUp + dirBack * dirBack);
                dirOut /= norm;
                dirUp /= norm;
                dirBack /= norm;
                out.add(new Stroke(Part.FEATHER, feather(toWorld, base, profile.length(), dirOut, dirUp, dirBack,
                        len, profile.samples())));
                out.add(new Stroke(Part.COVERT, feather(toWorld, base, profile.length(), dirOut, dirUp, dirBack,
                        len * 0.45, Math.max(1, profile.samples() / 2))));
            }
        }
        return out;
    }

    /** A feather from a point on the bone, with a slight backward curl towards its tip. */
    private static List<WingPoint> feather(Local toWorld, double[] base, double scale, double dirOut, double dirUp,
                                           double dirBack, double length, int samples) {
        List<WingPoint> points = new ArrayList<>(samples + 1);
        for (int i = 0; i <= samples; i++) {
            double f = i / (double) samples;
            double curl = 0.18 * length * f * f;
            points.add(toWorld.at(
                    base[0] * scale + dirOut * length * f,
                    base[1] * scale + dirUp * length * f,
                    base[2] * scale + dirBack * length * f + curl));
        }
        return points;
    }

    /** A point on the bone, {@code t} from 0 (root) to 1 (tip), as fractions of the wing's length. */
    static double[] boneAt(double t) {
        double scaled = Math.max(0, Math.min(1, t)) * (BONE.length - 1);
        int i = Math.min(BONE.length - 2, (int) scaled);
        double f = scaled - i;
        // Smooth the joints a little so the bone reads as one curve.
        double s = f * f * (3 - 2 * f);
        double[] a = BONE[i], b = BONE[i + 1];
        double mix = 0.5 * f + 0.5 * s;
        return new double[]{a[0] + (b[0] - a[0]) * mix, a[1] + (b[1] - a[1]) * mix, a[2] + (b[2] - a[2]) * mix};
    }

    @FunctionalInterface
    private interface Local {
        WingPoint at(double out, double up, double back);
    }
}
