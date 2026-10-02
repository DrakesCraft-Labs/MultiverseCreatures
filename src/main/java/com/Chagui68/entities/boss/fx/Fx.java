package com.Chagui68.entities.boss.fx;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Draws effects: {@link Brush brushes} say what one point looks like, the drawing methods say where
 * the points go. A ring of void dust edged with sparks is
 * {@code fx.ring(c, 6, 0.5, 0, fx.dust(VOID, 2).and(fx.particle(Particle.ELECTRIC_SPARK)))}.
 */
public final class Fx {

    /** What one point of a shape looks like. */
    @FunctionalInterface
    public interface Brush {
        void at(Vector point);

        default Brush and(Brush other) {
            return p -> {
                at(p);
                other.at(p);
            };
        }

        /** Paints only a share of the points, for sparse sparkles over a dense shape. */
        default Brush sometimes(double chance) {
            return p -> {
                if (ThreadLocalRandom.current().nextDouble() < chance) at(p);
            };
        }
    }

    private final FxSink sink;

    public Fx(FxSink sink) {
        this.sink = sink;
    }

    public FxSink sink() {
        return sink;
    }

    // ------------------------------------------------------------------ brushes

    public Brush dust(Color color, float size) {
        Particle.DustOptions options = new Particle.DustOptions(color, size);
        return p -> sink.particle(Particle.DUST, p, 1, 0, 0, 0, 0, options);
    }

    /** Dust scattered a little around each point, so a line reads as a glow instead of beads. */
    public Brush dust(Color color, float size, double jitter, int count) {
        Particle.DustOptions options = new Particle.DustOptions(color, size);
        return p -> sink.particle(Particle.DUST, p, count, jitter, jitter, jitter, 0, options);
    }

    /** Dust that shifts colour as it fades: embers cooling, magic dissipating. */
    public Brush fade(Color from, Color to, float size) {
        Particle.DustTransition options = new Particle.DustTransition(from, to, size);
        return p -> sink.particle(Particle.DUST_COLOR_TRANSITION, p, 1, 0, 0, 0, 0, options);
    }

    public Brush particle(Particle type) {
        return p -> sink.particle(type, p, 1, 0, 0, 0, 0, null);
    }

    public Brush particle(Particle type, int count, double spread, double speed) {
        return p -> sink.particle(type, p, count, spread, spread, spread, speed, null);
    }

    /** Fragments of a block bursting from each point. */
    public Brush crumble(Material material, int count, double spread) {
        return p -> sink.particle(Particle.BLOCK, p, count, spread, spread * 0.5, spread, 0.1, material);
    }

    // ------------------------------------------------------------------ shapes

    public void draw(List<Vector> points, Brush brush) {
        for (Vector point : points) brush.at(point);
    }

    public void line(Vector a, Vector b, double step, Brush brush) {
        draw(Shapes.line(a, b, step), brush);
    }

    public void ring(Vector center, double radius, double spacing, double phase, Brush brush) {
        draw(Shapes.ring(center, radius, spacing, phase), brush);
    }

    /** A filled horizontal disc, sampled in rings. */
    public void disc(Vector center, double radius, double spacing, Brush brush) {
        brush.at(center);
        for (double r = spacing; r <= radius; r += spacing) {
            ring(center, r, spacing, r, brush);
        }
    }

    /**
     * A thick crescent: the trail of a blade swung through the plane of {@code u}, {@code v}. The
     * band runs from {@code inner} to {@code outer} radius, thinning towards both tips.
     */
    public void crescent(Vector center, double inner, double outer, double from, double to,
                         Vector u, Vector v, Brush edge, Brush body) {
        int steps = Math.max(8, (int) (Math.abs(to - from) * outer * 2));
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double angle = from + (to - from) * t;
            double taper = Math.sin(Math.PI * t);
            double near = outer - (outer - inner) * taper;
            edge.at(Shapes.onCircle(center, outer, angle, u, v));
            for (double r = near; r < outer - 0.2; r += 0.6) {
                body.at(Shapes.onCircle(center, r, angle, u, v));
            }
        }
    }

    /** A beam with a bright core and a loose glow around it. */
    public void beam(Vector a, Vector b, Color core, Color glow, double width) {
        Brush coreBrush = dust(core, 1.6f);
        Brush glowBrush = dust(glow, 2.2f, width * 0.5, 2);
        line(a, b, 0.35, coreBrush);
        line(a, b, 0.9, glowBrush);
    }

    /** Particles shot outwards in every direction from a point. */
    public void burst(Vector at, Particle type, int count, double speed) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < count; i++) {
            Vector dir = Vector.getRandom().subtract(new Vector(0.5, 0.5, 0.5)).normalize();
            sink.particle(type, at, 0, dir.getX(), dir.getY(), dir.getZ(), speed * (0.6 + random.nextDouble() * 0.8), null);
        }
    }

    /** Particles shot outwards along the ground only, for shockwaves and stomps. */
    public void flatBurst(Vector at, Particle type, int count, double speed) {
        for (int i = 0; i < count; i++) {
            Vector dir = Shapes.heading(2 * Math.PI * i / count);
            sink.particle(type, at, 0, dir.getX(), 0.05, dir.getZ(), speed, null);
        }
    }

    /** One particle moving with {@code velocity}. */
    public void moving(Particle type, Vector at, Vector velocity) {
        double speed = velocity.length();
        if (speed < 1e-6) {
            sink.particle(type, at, 1, 0, 0, 0, 0, null);
            return;
        }
        Vector dir = velocity.clone().multiply(1 / speed);
        sink.particle(type, at, 0, dir.getX(), dir.getY(), dir.getZ(), speed, null);
    }

    public void cloud(Particle type, Vector at, int count, double spread, double speed) {
        sink.particle(type, at, count, spread, spread, spread, speed, null);
    }

    public void cloud(Particle type, Vector at, int count, double spreadXZ, double spreadY, double speed) {
        sink.particle(type, at, count, spreadXZ, spreadY, spreadXZ, speed, null);
    }

    public void flash(Vector at, Color color) {
        sink.particle(Particle.FLASH, at, 1, 0, 0, 0, 0, color);
    }

    public void trail(Vector from, Vector to, Color color, int ticks) {
        sink.trail(from, to, color, ticks);
    }

    /** Several trails converging on a point from around it: energy being gathered. */
    public void gather(Vector at, double radius, int count, Color color, int ticks) {
        for (Vector from : Shapes.sphere(at, radius, count)) {
            sink.trail(from, at, color, ticks);
        }
    }

    public void sound(Vector at, Sfx sound, float volume, float pitch) {
        sink.sound(at, sound, volume, pitch);
    }

    /** The standard impact: a flash, a shell of debris, smoke and a boom. */
    public void impact(Vector at, Color color, double size) {
        flash(at, color);
        cloud(Particle.EXPLOSION, at, (int) Math.max(1, size), size * 0.4, 0);
        burst(at, Particle.LARGE_SMOKE, (int) (size * 6), 0.15 * size);
        draw(Shapes.sphere(at, size * 0.6, (int) (size * 14)), dust(color, 2.4f));
    }
}
