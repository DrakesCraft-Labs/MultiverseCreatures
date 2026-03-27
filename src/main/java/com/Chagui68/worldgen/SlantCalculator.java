package com.Chagui68.worldgen;

/**
 * Calculates the slope (slant) of the terrain at a given coordinate.
 * Implementation based on Terra's Derivative calculation method.
 */
public class SlantCalculator {
    private static final double DERIVATIVE_DIST = 0.55;
    private final NoiseSampler sampler;

    public SlantCalculator(NoiseSampler sampler) {
        this.sampler = sampler;
    }

    /**
     * Calculates slant as the magnitude of the approximation of the gradient.
     * Higher values indicate steeper terrain.
     */
    public double getSlant(double x, double y, double z) {
        double baseSample = sampler.sample(x, y, z);

        double xVal1 = (sampler.sample(x + DERIVATIVE_DIST, y, z) - baseSample) / DERIVATIVE_DIST;
        double xVal2 = (sampler.sample(x - DERIVATIVE_DIST, y, z) - baseSample) / DERIVATIVE_DIST;
        double zVal1 = (sampler.sample(x, y, z + DERIVATIVE_DIST) - baseSample) / DERIVATIVE_DIST;
        double zVal2 = (sampler.sample(x, y, z - DERIVATIVE_DIST) - baseSample) / DERIVATIVE_DIST;
        double yVal1 = (sampler.sample(x, y + DERIVATIVE_DIST, z) - baseSample) / DERIVATIVE_DIST;
        double yVal2 = (sampler.sample(x, y - DERIVATIVE_DIST, z) - baseSample) / DERIVATIVE_DIST;

        return Math.sqrt(
            ((xVal2 - xVal1) * (xVal2 - xVal1)) + 
            ((zVal2 - zVal1) * (zVal2 - zVal1)) + 
            ((yVal2 - yVal1) * (yVal2 - yVal1))
        );
    }
}
