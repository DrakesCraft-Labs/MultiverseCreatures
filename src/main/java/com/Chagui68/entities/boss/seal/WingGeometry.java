package com.Chagui68.entities.boss.seal;

import java.util.ArrayList;
import java.util.List;

/**
 * The wings a boss spreads, as a pure function of pose, frame and profile.
 *
 * <p>The two wing seals differ only in their numbers — feather count, reach, flap speed — so the
 * numbers live in a {@link Profile} and the drawing is one walk over the result. Colors are the
 * caller's business: the same feathers are painted gold for the sentinel and red for the burning
 * seal.
 *
 * <p>The wings are laid out around the body's right vector ({@code +x} at yaw 0, the way the game
 * faces), with the feathers sweeping back along the negative facing vector, so a stand that turns
 * carries its wings with it.
 */
public final class WingGeometry {

    /** The pale wings of the sentinel's wing seal: 8 feathers, ending 9 blocks out. */
    public static final Profile GOLDEN_WINGS =
            new Profile(8, 9.0, 3.75, 9.0, 7.0, -0.6, 1.8, 1.0, 0.5, 0.08, 0.3, 6);

    /** The longer, hotter wings of the second wing seal: 12 feathers, a slower, wider flap. */
    public static final Profile BURNING_WINGS =
            new Profile(12, 9.0, 3.75, 11.0, 8.0, -0.8, 2.0, 1.5, 0.6, 0.06, 0.4, 5);

    private WingGeometry() {
    }

    /**
     * One wing style.
     *
     * @param feathers        feathers per wing
     * @param shoulderHeight  how high the shoulders sit above the stand's feet
     * @param shoulderWidth   half the distance between the shoulders
     * @param length          how far the longest feather reaches
     * @param reach           how far back the last feather trails
     * @param spreadStart     the angle the first feather leaves the shoulder at
     * @param spreadSpan      how much the spread widens by the last feather
     * @param lengthExponent  1 for a sine profile, more for a feather that grows late
     * @param flapAmplitude   how far the wing swings, in radians
     * @param flapSpeed       flap speed per tick
     * @param flapFalloff     how much less the last feather flaps
     * @param samples         subdivisions along a feather (points drawn = samples + 1)
     */
    public record Profile(int feathers, double shoulderHeight, double shoulderWidth, double length,
                          double reach, double spreadStart, double spreadSpan, double lengthExponent,
                          double flapAmplitude, double flapSpeed, double flapFalloff, int samples) {
    }

    /** The flap of a frame: a slow sine around the resting pose. */
    public static double flap(int ticks, Profile profile) {
        return Math.sin(ticks * profile.flapSpeed()) * profile.flapAmplitude();
    }

    /**
     * Every feather of both wings, in draw order: side ({@code -1} then {@code +1}), feather, and
     * the points of the feather from shoulder to tip.
     *
     * <p>Each feather arrives as its own list so the caller can alternate colors along it without
     * counting across feather boundaries — the pattern the seals have always painted.
     */
    public static List<List<WingPoint>> feathers(double cx, double cy, double cz, double yawDegrees,
                                                 int ticks, Profile profile) {
        double yaw = Math.toRadians(yawDegrees);
        double rightX = Math.cos(yaw);
        double rightZ = Math.sin(yaw);
        double backX = Math.sin(yaw);
        double backZ = -Math.cos(yaw);
        double flap = flap(ticks, profile);
        List<List<WingPoint>> wings = new ArrayList<>(2 * profile.feathers());
        for (int side = -1; side <= 1; side += 2) {
            double shoulderX = cx + side * profile.shoulderWidth() * rightX;
            double shoulderZ = cz + side * profile.shoulderWidth() * rightZ;
            double shoulderY = cy + profile.shoulderHeight();
            for (int feather = 0; feather < profile.feathers(); feather++) {
                double p = (double) feather / (profile.feathers() - 1);
                double spread = profile.spreadStart() + p * profile.spreadSpan();
                // The sine runs slightly negative on the last feather (sin(PI + 0.1)); clamping it
                // keeps a fractional exponent from turning that feather into NaN, which is what a
                // bare Math.pow did to the burning wings' outermost feather.
                double shape = Math.max(0, Math.sin(p * Math.PI + 0.1));
                double length = Math.pow(shape, profile.lengthExponent()) * profile.length();
                double out = side * Math.cos(spread) * length;
                double up = Math.sin(spread) * length;
                double backwards = p * profile.reach();
                double featherFlap = flap * (1.0 - p * profile.flapFalloff());
                double cookedUp = up * Math.cos(featherFlap) - backwards * Math.sin(featherFlap);
                double cookedBack = up * Math.sin(featherFlap) + backwards * Math.cos(featherFlap);
                double tipX = shoulderX + out * rightX + cookedBack * backX;
                double tipY = shoulderY + cookedUp;
                double tipZ = shoulderZ + out * rightZ + cookedBack * backZ;
                List<WingPoint> points = new ArrayList<>(profile.samples() + 1);
                for (int sample = 0; sample <= profile.samples(); sample++) {
                    double f = (double) sample / profile.samples();
                    points.add(new WingPoint(
                            shoulderX + (tipX - shoulderX) * f,
                            shoulderY + (tipY - shoulderY) * f,
                            shoulderZ + (tipZ - shoulderZ) * f));
                }
                wings.add(points);
            }
        }
        return wings;
    }
}
