package com.Chagui68.entities.boss.fx;

import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.Random;

/** Lightning drawn as lightning: a jagged path with a bright core, a blue glow and sparks. */
public final class Bolts {

    private Bolts() {
    }

    /** A bolt from {@code a} to {@code b}, kinked at random every couple of blocks. */
    public static void bolt(Fx fx, Vector a, Vector b, Random random) {
        Vector delta = b.clone().subtract(a);
        double length = delta.length();
        int kinks = Math.max(2, (int) (length / 2.5));
        Vector[] axes = Shapes.planeAxes(delta);
        Vector previous = a.clone();
        for (int i = 1; i <= kinks; i++) {
            double t = (double) i / kinks;
            Vector next = a.clone().add(delta.clone().multiply(t));
            if (i < kinks) {
                double jitter = Math.min(1.6, length * 0.08);
                next.add(axes[0].clone().multiply((random.nextDouble() - 0.5) * 2 * jitter))
                        .add(axes[1].clone().multiply((random.nextDouble() - 0.5) * 2 * jitter));
            }
            fx.line(previous, next, 0.25, fx.dust(Palette.ICE, 1.1f));
            fx.line(previous, next, 0.7, fx.dust(Palette.STORM, 1.9f).and(fx.particle(Particle.ELECTRIC_SPARK).sometimes(0.4)));
            previous = next;
        }
    }

    /** A bolt falling from the sky to {@code to}. */
    public static void strike(Fx fx, Vector to, Random random) {
        bolt(fx, to.clone().add(new Vector(random.nextGaussian() * 2, 30, random.nextGaussian() * 2)), to, random);
    }

    /** A flicker of lightning inside a storm cloud. */
    public static void inCloud(Fx fx, Vector cloud, double radius, Random random) {
        Vector a = Shapes.onCircle(cloud, radius * random.nextDouble(), random.nextDouble() * 6.28, Shapes.FLAT_U, Shapes.FLAT_V);
        Vector b = Shapes.onCircle(cloud, radius * random.nextDouble(), random.nextDouble() * 6.28, Shapes.FLAT_U, Shapes.FLAT_V);
        bolt(fx, a, b, random);
        fx.flash(a.midpoint(b), Palette.STORM);
    }
}
