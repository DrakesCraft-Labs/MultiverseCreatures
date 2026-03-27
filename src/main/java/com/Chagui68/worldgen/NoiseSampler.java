package com.Chagui68.worldgen;

import java.util.Random;

/**
 * A standalone noise sampler that doesn't depend on external libraries.
 * Uses a Simplex Noise implementation with Fractal (fBm) support.
 */
public class NoiseSampler {
    private final SimplexNoise engine;
    private final SimplexNoise caveEngine;
    private final float frequency;
    private final int octaves = 3;
    private final double lacunarity = 2.0;
    private final double gain = 0.5;

    public NoiseSampler(long seed, float frequency) {
        this.engine = new SimplexNoise(seed);
        this.caveEngine = new SimplexNoise(seed + 12345); // Different seed for caves
        this.frequency = frequency;
    }

    public double sample(double x, double y, double z) {
        return sampleInternal(engine, x, y, z, frequency);
    }

    public double sampleCave(double x, double y, double z) {
        // Higher frequency for caves
        return sampleInternal(caveEngine, x, y, z, frequency * 4.0f);
    }

    private double sampleInternal(SimplexNoise e, double x, double y, double z, float freq) {
        double sum = 0;
        double amp = 1.0;
        double f = freq;
        
        for (int i = 0; i < octaves; i++) {
            sum += e.noise(x * f, y * f, z * f) * amp;
            amp *= gain;
            f *= lacunarity;
        }
        return sum;
    }

    /**
     * Internal Simplex Noise class based on Stefan Gustavson's implementation.
     */
    private static class SimplexNoise {
        private static final int[][] grad3 = {
            {1,1,0},{-1,1,0},{1,-1,0},{-1,-1,0},
            {1,0,1},{-1,0,1},{1,0,-1},{-1,0,-1},
            {0,1,1},{0,-1,1},{0,1,-1},{0,-1,-1}
        };

        private final short[] p = new short[256];
        private final short[] perm = new short[512];
        private final short[] permMod12 = new short[512];

        public SimplexNoise(long seed) {
            Random rand = new Random(seed);
            for(int i=0; i<256; i++) p[i] = (short)rand.nextInt(256);
            for(int i=0; i<512; i++) {
                perm[i] = p[i & 255];
                permMod12[i] = (short)(perm[i] % 12);
            }
        }

        private static double dot(int g[], double x, double y, double z) {
            return g[0]*x + g[1]*y + g[2]*z;
        }

        public double noise(double xin, double yin, double zin) {
            double n0, n1, n2, n3;
            final double F3 = 1.0/3.0;
            double s = (xin+yin+zin)*F3;
            int i = floor(xin+s);
            int j = floor(yin+s);
            int k = floor(zin+s);
            final double G3 = 1.0/6.0;
            double t = (i+j+k)*G3;
            double X0 = i-t;
            double Y0 = j-t;
            double Z0 = k-t;
            double x0 = xin-X0;
            double y0 = yin-Y0;
            double z0 = zin-Z0;
            int i1, j1, k1;
            int i2, j2, k2;
            if(x0>=y0) {
                if(y0>=z0) { i1=1; j1=0; k1=0; i2=1; j2=1; k2=0; }
                else if(x0>=z0) { i1=1; j1=0; k1=0; i2=1; j2=0; k2=1; }
                else { i1=0; j1=0; k1=1; i2=1; j2=0; k2=1; }
            } else {
                if(y0<z0) { i1=0; j1=0; k1=1; i2=0; j2=1; k2=1; }
                else if(x0<z0) { i1=0; j1=1; k1=0; i2=0; j2=1; k2=1; }
                else { i1=0; j1=1; k1=0; i2=1; j2=1; k2=0; }
            }
            double x1 = x0 - i1 + G3;
            double y1 = y0 - j1 + G3;
            double z1 = z0 - k1 + G3;
            double x2 = x0 - i2 + 2.0*G3;
            double y2 = y0 - j2 + 2.0*G3;
            double z2 = z0 - k2 + 2.0*G3;
            double x3 = x0 - 1.0 + 3.0*G3;
            double y3 = y0 - 1.0 + 3.0*G3;
            double z3 = z0 - 1.0 + 3.0*G3;
            int ii = i & 255;
            int jj = j & 255;
            int kk = k & 255;
            double t0 = 0.6 - x0*x0 - y0*y0 - z0*z0;
            if(t0<0) n0 = 0.0;
            else {
                t0 *= t0;
                n0 = t0 * t0 * dot(grad3[permMod12[ii+perm[jj+perm[kk]]]], x0, y0, z0);
            }
            double t1 = 0.6 - x1*x1 - y1*y1 - z1*z1;
            if(t1<0) n1 = 0.0;
            else {
                t1 *= t1;
                n1 = t1 * t1 * dot(grad3[permMod12[ii+i1+perm[jj+j1+perm[kk+k1]]]], x1, y1, z1);
            }
            double t2 = 0.6 - x2*x2 - y2*y2 - z2*z2;
            if(t2<0) n2 = 0.0;
            else {
                t2 *= t2;
                n2 = t2 * t2 * dot(grad3[permMod12[ii+i2+perm[jj+j2+perm[kk+k2]]]], x2, y2, z2);
            }
            double t3 = 0.6 - x3*x3 - y3*y3 - z3*z3;
            if(t3<0) n3 = 0.0;
            else {
                t3 *= t3;
                n3 = t3 * t3 * dot(grad3[permMod12[ii+1+perm[jj+1+perm[kk+1]]]], x3, y3, z3);
            }
            return 32.0*(n0 + n1 + n2 + n3);
        }

        private static int floor(double x) {
            return x>0 ? (int)x : (int)x-1;
        }
    }
}
