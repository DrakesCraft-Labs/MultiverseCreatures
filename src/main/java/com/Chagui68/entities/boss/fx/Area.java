package com.Chagui68.entities.boss.fx;

import org.bukkit.util.Vector;

/**
 * A volume an attack hits, tested against a player's feet. Telegraphs draw these same shapes, so
 * what the floor warns about is exactly what gets hit.
 */
@FunctionalInterface
public interface Area {

    boolean contains(Vector point);

    /** A vertical cylinder: within {@code radius} horizontally and {@code below}/{@code above} vertically. */
    static Area cylinder(Vector center, double radius, double below, double above) {
        double r2 = radius * radius;
        return p -> {
            double dx = p.getX() - center.getX();
            double dz = p.getZ() - center.getZ();
            double dy = p.getY() - center.getY();
            return dx * dx + dz * dz <= r2 && dy >= -below && dy <= above;
        };
    }

    /** The band between two radii, for expanding shockwaves a player can jump over. */
    static Area ring(Vector center, double inner, double outer, double height) {
        return p -> {
            double dx = p.getX() - center.getX();
            double dz = p.getZ() - center.getZ();
            double d = Math.sqrt(dx * dx + dz * dz);
            double dy = p.getY() - center.getY();
            return d >= inner && d <= outer && dy >= -1.5 && dy <= height;
        };
    }

    static Area sphere(Vector center, double radius) {
        double r2 = radius * radius;
        return p -> p.distanceSquared(center) <= r2
                || p.clone().add(new Vector(0, 1.6, 0)).distanceSquared(center) <= r2;
    }

    /**
     * A horizontal cone from {@code apex} along {@code direction}, {@code halfAngle} radians each
     * side, {@code length} long and {@code height} tall.
     */
    static Area cone(Vector apex, Vector direction, double halfAngle, double length, double height) {
        Vector dir = Shapes.flat(direction);
        double cos = Math.cos(halfAngle);
        return p -> {
            Vector to = new Vector(p.getX() - apex.getX(), 0, p.getZ() - apex.getZ());
            double d = to.length();
            double dy = p.getY() - apex.getY();
            if (d > length || dy < -2 || dy > height) return false;
            return d < 1.0 || to.multiply(1 / d).dot(dir) >= cos;
        };
    }

    /** A capsule around the segment {@code a}–{@code b}: lines, beams and charges. */
    static Area segment(Vector a, Vector b, double radius) {
        double r2 = radius * radius;
        Vector ab = b.clone().subtract(a);
        double len2 = ab.lengthSquared();
        return p -> {
            for (Vector probe : new Vector[]{p, p.clone().add(new Vector(0, 1.0, 0))}) {
                double t = len2 < 1e-9 ? 0 : Math.max(0, Math.min(1, probe.clone().subtract(a).dot(ab) / len2));
                Vector closest = a.clone().add(ab.clone().multiply(t));
                if (closest.distanceSquared(probe) <= r2) return true;
            }
            return false;
        };
    }

    default Area or(Area other) {
        return p -> contains(p) || other.contains(p);
    }
}
