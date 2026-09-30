package com.Chagui68.entities.boss.seal;

/**
 * A point of a seal's own plane: {@code u} across it and {@code v} down it.
 *
 * <p>Every seal shape is authored in this flat frame — the {@link SealPlane} is the only thing that
 * knows which world axes it ends up on — so the geometry can be tested with plain numbers and no
 * server.
 */
public record SealPoint(double u, double v) {

    /** The same direction, scaled outwards (or inwards, for a negative factor). */
    public SealPoint scaled(double factor) {
        return new SealPoint(u * factor, v * factor);
    }

    /** The distance from the centre of the plane. */
    public double length() {
        return Math.hypot(u, v);
    }
}
