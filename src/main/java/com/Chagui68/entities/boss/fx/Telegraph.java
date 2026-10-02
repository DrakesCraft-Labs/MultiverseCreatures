package com.Chagui68.entities.boss.fx;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

/**
 * Warnings drawn on the floor before a blow lands. Each one outlines exactly the {@link Area} the
 * blow will hit, and fills from red to hot yellow as {@code progress} runs from 0 to 1, so a player
 * can read both where and when.
 */
public final class Telegraph {

    private Telegraph() {
    }

    private static Color heat(double progress) {
        return Palette.mix(Palette.WARNING, Palette.WARNING_HOT, progress * progress);
    }

    /** A circle that fills from its centre outwards. */
    public static void circle(Stage stage, Vector center, double radius, double progress) {
        Fx fx = stage.fx();
        Color color = heat(progress);
        Fx.Brush edge = fx.dust(color, 1.4f);
        for (Vector p : Shapes.ring(center, radius, 0.8, progress * 2)) {
            edge.at(stage.onGround(p));
        }
        double filled = radius * progress;
        if (filled > 0.6) {
            Fx.Brush fill = fx.dust(color, 1.0f);
            for (Vector p : Shapes.ring(center, filled, 1.2, 0)) {
                fill.at(stage.onGround(p));
            }
        }
        if (progress > 0.85) {
            fx.cloud(Particle.SMALL_FLAME, stage.onGround(center), 3, radius * 0.4, 0.05, 0.01);
        }
    }

    /** A band between two radii: the path of a shockwave. */
    public static void ring(Stage stage, Vector center, double inner, double outer, double progress) {
        Fx fx = stage.fx();
        Fx.Brush edge = fx.dust(heat(progress), 1.3f);
        for (Vector p : Shapes.ring(center, inner, 1.0, 0)) edge.at(stage.onGround(p));
        for (Vector p : Shapes.ring(center, outer, 1.0, 0)) edge.at(stage.onGround(p));
    }

    /** A rectangle from {@code a} to {@code b}, {@code width} wide, filling along its length. */
    public static void line(Stage stage, Vector a, Vector b, double width, double progress) {
        Fx fx = stage.fx();
        Color color = heat(progress);
        Vector dir = Shapes.flat(b.clone().subtract(a));
        Vector side = new Vector(-dir.getZ(), 0, dir.getX()).multiply(width / 2);
        Fx.Brush edge = fx.dust(color, 1.3f);
        for (Vector p : Shapes.line(a.clone().add(side), b.clone().add(side), 0.8)) edge.at(stage.onGround(p));
        for (Vector p : Shapes.line(a.clone().subtract(side), b.clone().subtract(side), 0.8)) edge.at(stage.onGround(p));
        Vector reach = a.clone().add(b.clone().subtract(a).multiply(progress));
        for (Vector p : Shapes.line(reach.clone().add(side), reach.clone().subtract(side), 0.6)) {
            edge.at(stage.onGround(p));
        }
        Fx.Brush fill = fx.dust(color, 0.9f).sometimes(0.5);
        for (Vector p : Shapes.line(a, reach, 0.9)) fill.at(stage.onGround(p));
    }

    /** A cone from {@code apex}, sweeping its fill from the apex out. */
    public static void cone(Stage stage, Vector apex, Vector direction, double halfAngle, double length,
                            double progress) {
        Fx fx = stage.fx();
        Color color = heat(progress);
        Vector dir = Shapes.flat(direction);
        double base = Math.atan2(dir.getZ(), dir.getX());
        Fx.Brush edge = fx.dust(color, 1.3f);
        for (Vector p : Shapes.line(apex, apex.clone().add(Shapes.heading(base - halfAngle).multiply(length)), 0.8)) {
            edge.at(stage.onGround(p));
        }
        for (Vector p : Shapes.line(apex, apex.clone().add(Shapes.heading(base + halfAngle).multiply(length)), 0.8)) {
            edge.at(stage.onGround(p));
        }
        int points = Math.max(6, (int) (halfAngle * 2 * length / 0.8));
        for (Vector p : Shapes.arc(apex, length, base - halfAngle, base + halfAngle, points, Shapes.FLAT_U, Shapes.FLAT_V)) {
            edge.at(stage.onGround(p));
        }
        double filled = length * progress;
        if (filled > 1) {
            int fillPoints = Math.max(4, (int) (halfAngle * 2 * filled / 1.2));
            Fx.Brush fill = fx.dust(color, 1.0f);
            for (Vector p : Shapes.arc(apex, filled, base - halfAngle, base + halfAngle, fillPoints, Shapes.FLAT_U, Shapes.FLAT_V)) {
                fill.at(stage.onGround(p));
            }
        }
    }
}
