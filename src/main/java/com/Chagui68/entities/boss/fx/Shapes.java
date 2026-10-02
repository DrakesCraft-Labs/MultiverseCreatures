package com.Chagui68.entities.boss.fx;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * The geometry effects are drawn along: pure point lists, so a shape can be tested and previewed
 * without a server. Planar shapes take two unit axes {@code u} and {@code v} spanning their plane;
 * {@link #FLAT_U}/{@link #FLAT_V} give a horizontal one.
 */
public final class Shapes {

    public static final Vector FLAT_U = new Vector(1, 0, 0);
    public static final Vector FLAT_V = new Vector(0, 0, 1);
    public static final Vector UP = new Vector(0, 1, 0);

    private Shapes() {
    }

    /** Points every {@code step} blocks from {@code a} to {@code b}, both ends included. */
    public static List<Vector> line(Vector a, Vector b, double step) {
        List<Vector> points = new ArrayList<>();
        Vector delta = b.clone().subtract(a);
        double length = delta.length();
        int n = Math.max(1, (int) Math.ceil(length / Math.max(0.05, step)));
        for (int i = 0; i <= n; i++) {
            points.add(a.clone().add(delta.clone().multiply((double) i / n)));
        }
        return points;
    }

    /** A circle (or an arc of one) in the plane of {@code u}, {@code v}. Angles in radians. */
    public static List<Vector> arc(Vector center, double radius, double from, double to, int points,
                                   Vector u, Vector v) {
        List<Vector> out = new ArrayList<>(points);
        for (int i = 0; i < points; i++) {
            double t = points == 1 ? 0 : (double) i / (points - 1);
            double angle = from + (to - from) * t;
            out.add(onCircle(center, radius, angle, u, v));
        }
        return out;
    }

    /** A closed circle of {@code points} evenly spaced points. */
    public static List<Vector> circle(Vector center, double radius, int points, Vector u, Vector v, double phase) {
        List<Vector> out = new ArrayList<>(points);
        for (int i = 0; i < points; i++) {
            out.add(onCircle(center, radius, phase + 2 * Math.PI * i / points, u, v));
        }
        return out;
    }

    /** A horizontal circle with roughly one point every {@code spacing} blocks. */
    public static List<Vector> ring(Vector center, double radius, double spacing, double phase) {
        int points = Math.max(8, (int) Math.ceil(2 * Math.PI * radius / Math.max(0.1, spacing)));
        return circle(center, radius, points, FLAT_U, FLAT_V, phase);
    }

    public static Vector onCircle(Vector center, double radius, double angle, Vector u, Vector v) {
        return center.clone()
                .add(u.clone().multiply(Math.cos(angle) * radius))
                .add(v.clone().multiply(Math.sin(angle) * radius));
    }

    /** A rising helix around a vertical axis. */
    public static List<Vector> helix(Vector base, double radius, double height, double turns, int points,
                                     double phase) {
        List<Vector> out = new ArrayList<>(points);
        for (int i = 0; i < points; i++) {
            double t = (double) i / Math.max(1, points - 1);
            double angle = phase + t * turns * 2 * Math.PI;
            out.add(base.clone().add(new Vector(Math.cos(angle) * radius, t * height, Math.sin(angle) * radius)));
        }
        return out;
    }

    /** Evenly spread points on a sphere (Fibonacci lattice). */
    public static List<Vector> sphere(Vector center, double radius, int points) {
        List<Vector> out = new ArrayList<>(points);
        double golden = Math.PI * (3 - Math.sqrt(5));
        for (int i = 0; i < points; i++) {
            double y = 1 - 2.0 * (i + 0.5) / points;
            double r = Math.sqrt(1 - y * y);
            double angle = golden * i;
            out.add(center.clone().add(new Vector(Math.cos(angle) * r, y, Math.sin(angle) * r).multiply(radius)));
        }
        return out;
    }

    /**
     * A star polygon: {@code vertices} points on a circle joined every {@code skip} vertices (5 and 2
     * draw a pentagram), sampled every {@code step} blocks along the edges.
     */
    public static List<Vector> star(Vector center, double radius, int vertices, int skip, double phase,
                                    double step, Vector u, Vector v) {
        List<Vector> corners = circle(center, radius, vertices, u, v, phase);
        List<Vector> out = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            out.addAll(line(corners.get(i), corners.get((i + skip) % vertices), step));
        }
        return out;
    }

    /** Two unit axes perpendicular to {@code normal}, for drawing a shape facing that way. */
    public static Vector[] planeAxes(Vector normal) {
        Vector n = normal.clone().normalize();
        Vector helper = Math.abs(n.getY()) < 0.9 ? new Vector(0, 1, 0) : new Vector(1, 0, 0);
        Vector u = n.clone().crossProduct(helper).normalize();
        Vector v = n.clone().crossProduct(u).normalize();
        return new Vector[]{u, v};
    }

    /** The horizontal unit vector at {@code angle} radians from +x towards +z. */
    public static Vector heading(double angle) {
        return new Vector(Math.cos(angle), 0, Math.sin(angle));
    }

    /** The horizontal component of {@code v}, normalised (or +z when it has none). */
    public static Vector flat(Vector v) {
        Vector f = new Vector(v.getX(), 0, v.getZ());
        return f.lengthSquared() < 1e-9 ? new Vector(0, 0, 1) : f.normalize();
    }
}
