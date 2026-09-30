package com.Chagui68.entities.boss.seal;

/**
 * The plane a seal is drawn on: which world axes the shape's own {@code (u, v)} map to.
 *
 * <p>This is the whole difference between a seal lying on the floor and one standing in the air,
 * and it is one small function instead of a switch repeated in every drawing helper. A flat seal
 * keeps one height (plus a small normal lift so the runes do not z-fight the floor); a vertical one
 * keeps one axis — X for YZ, Z for XY — and the shape lives on the other two.
 */
public enum SealPlane {

    XZ,
    XY,
    YZ;

    /** Whether the seal stands up instead of lying flat on the floor. */
    public boolean vertical() {
        return this != XZ;
    }

    /** The world offset of a point of the plane, at the seal's own height. */
    public double[] toWorld(double cx, double cy, double cz, double u, double v) {
        return toWorld(cx, cy, cz, u, v, 0);
    }

    /** The world offset of a point of the plane, lifted {@code normal} along the plane's normal. */
    public double[] toWorld(double cx, double cy, double cz, double u, double v, double normal) {
        return switch (this) {
            case XZ -> new double[]{cx + u, cy + normal, cz + v};
            case XY -> new double[]{cx + u, cy + v, cz + normal};
            case YZ -> new double[]{cx + normal, cy + u, cz + v};
        };
    }
}
