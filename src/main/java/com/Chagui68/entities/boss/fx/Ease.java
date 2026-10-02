package com.Chagui68.entities.boss.fx;

/**
 * Easing curves for animation: each maps a progress in {@code [0, 1]} to a shaped progress.
 *
 * <p>A pose that moves linearly looks mechanical. A wind-up should accelerate into the strike
 * ({@link #IN}), a strike should land hard and settle ({@link #OUT_BACK}), and a recovery should
 * glide back ({@link #IN_OUT}).
 */
@FunctionalInterface
public interface Ease {

    double apply(double t);

    Ease LINEAR = t -> t;
    /** Slow start, fast finish: the wind-up into a blow. */
    Ease IN = t -> t * t * t;
    /** Fast start, slow finish: a blow that lands and stops. */
    Ease OUT = t -> 1 - Math.pow(1 - t, 3);
    /** Smooth both ways: recoveries, floating, breathing. */
    Ease IN_OUT = t -> t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    /** Overshoots then settles: impacts that should feel heavy. */
    Ease OUT_BACK = t -> {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    };
    /** Pulls back a little before going: a telegraphed swing. */
    Ease IN_BACK = t -> {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return c3 * t * t * t - c1 * t * t;
    };

    /** Clamps the progress into {@code [0, 1]} before shaping it. */
    static double at(Ease ease, double t) {
        return ease.apply(Math.max(0.0, Math.min(1.0, t)));
    }
}
