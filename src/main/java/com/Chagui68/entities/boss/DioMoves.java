package com.Chagui68.entities.boss;

import com.Chagui68.entities.boss.fx.Pose;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * The pure side of DIO and The World: their poses, where The World floats, where the knives of a
 * time stop hang, and the shapes the attacks hit. {@link DioBoss} plays them on the server.
 *
 * <p>Poses use the armor stand's angles in degrees: an arm at {@code x = -90} points forward and at
 * {@code -180} straight up; a positive {@code z} lifts the right arm out to the side, a negative one
 * the left.
 */
public final class DioMoves {

    /** The attacks DIO picks from, each with its length in ticks (time stop: without the stop itself). */
    public enum Attack {
        /** "ZA WARUDO!": time stops for everyone around him, and knives are waiting when it moves again. */
        TIME_STOP(70),
        /** "MUDA MUDA MUDA!": The World rushes in and buries the target under a barrage of fists. */
        MUDA_BARRAGE(64),
        /** "ROAD ROLLER DA!": DIO leaps into the sky and drops a road roller on the target. */
        ROAD_ROLLER(96),
        /** A fan of throwing knives. */
        KNIFE_FAN(40),
        /** Space Ripper Stingy Eyes: two jets of pressurised fluid fired from his eyes. */
        EYE_BEAMS(56),
        /** The World's single heavy punch: his basic attack at close range. */
        PUNCH(16);

        public final int ticks;

        Attack(int ticks) {
            this.ticks = ticks;
        }
    }

    // ------------------------------------------------------------------ DIO

    /** His arrogant stance: chin up, one hand on the hip. */
    public static final Pose DIO_IDLE = Pose.REST.withHead(-8, 0, 0)
            .withRightArm(-15, 0, 28).withLeftArm(-25, 0, -10)
            .withRightLeg(-4, 0, 3).withLeftLeg(4, 0, -3);
    /** "ZA WARUDO!": the right hand thrown up at the sky. */
    public static final Pose DIO_SKY = Pose.REST.withHead(-25, 0, 0)
            .withRightArm(-160, 0, 25).withLeftArm(-40, 0, -35).withBody(-6, 0, 0);
    /** Knives fanned between the fingers, drawn back behind the head. */
    public static final Pose DIO_KNIVES_DRAWN = Pose.REST.withHead(-5, 0, 0)
            .withRightArm(-150, -35, 35).withLeftArm(-60, 25, -10).withBody(-4, 0, 0);
    /** The throw follows through across the body. */
    public static final Pose DIO_KNIVES_THROWN = Pose.REST
            .withRightArm(-75, 40, -10).withLeftArm(-20, 0, -25).withBody(8, 0, 0);
    /** Leaning in, head down a touch, arms back: the eyes are about to fire. */
    public static final Pose DIO_EYES = Pose.REST.withHead(5, 0, 0).withBody(12, 0, 0)
            .withRightArm(25, 0, 25).withLeftArm(25, 0, -25);
    /** In the air: arms flung up and out, knees drawn up. */
    public static final Pose DIO_LEAP = Pose.REST.withHead(-20, 0, 0)
            .withRightArm(-170, 0, 30).withLeftArm(-170, 0, -30)
            .withRightLeg(-45, 0, 0).withLeftLeg(-20, 0, 0);
    /** Crouched on the road roller, one fist raised. */
    public static final Pose DIO_ON_ROLLER = Pose.REST.withHead(15, 0, 0).withBody(15, 0, 0)
            .withRightArm(-150, 0, 20).withLeftArm(-60, 0, -30)
            .withRightLeg(-55, 0, 0).withLeftLeg(-20, 0, 0);
    /** Arms folded while The World does the work. */
    public static final Pose DIO_ARMS_FOLDED = Pose.REST.withHead(-10, 0, 0)
            .withRightArm(-70, 50, 0).withLeftArm(-70, -50, 0);

    // ------------------------------------------------------------------ The World

    /** Arms folded, floating behind his master. */
    public static final Pose WORLD_IDLE = Pose.REST.withHead(-5, 0, 0)
            .withRightArm(-80, 48, 0).withLeftArm(-80, -48, 0)
            .withRightLeg(-12, 0, 0).withLeftLeg(8, 0, 0);
    /** Arms spread open for the time stop. */
    public static final Pose WORLD_SPREAD = Pose.REST.withHead(-20, 0, 0).withBody(-8, 0, 0)
            .withRightArm(-150, 0, 55).withLeftArm(-150, 0, -55);
    /** Fist drawn back for a heavy punch. */
    public static final Pose WORLD_WIND_UP = Pose.REST.withBody(-6, 12, 0)
            .withRightArm(-30, -20, 30).withLeftArm(-70, 0, -20);
    /** The heavy punch, right fist straight out. */
    public static final Pose WORLD_PUNCH = Pose.REST.withBody(8, -10, 0)
            .withRightArm(-95, 0, 0).withLeftArm(-40, 0, -20);
    /** Both fists driven straight out: the barrage's last "MUDA!". */
    public static final Pose WORLD_FINISHER = Pose.REST.withBody(12, 0, 0)
            .withRightArm(-95, -8, 0).withLeftArm(-95, 8, 0);

    private DioMoves() {
    }

    /**
     * One frame of the barrage: the fists alternate every tick, each landing at a slightly
     * different height and angle so the arms blur the way they do in the anime.
     *
     * @param jitter two numbers in [-1, 1] that vary the frame
     */
    public static Pose barrage(int tick, double jitterA, double jitterB) {
        double up = -95 + 14 * jitterA;
        double side = 18 * jitterB;
        Pose base = Pose.REST.withBody(10, 6 * jitterB, 0);
        return tick % 2 == 0
                ? base.withRightArm(up, side, 0).withLeftArm(-45, -10, -15)
                : base.withLeftArm(up, -side, 0).withRightArm(-45, 10, 15);
    }

    /** Pounding straight down onto the road roller, fists alternating. */
    public static Pose pound(int tick) {
        Pose base = Pose.REST.withBody(25, 0, 0).withHead(20, 0, 0);
        return tick % 2 == 0
                ? base.withRightArm(-20, 0, 0).withLeftArm(-110, 0, -20)
                : base.withLeftArm(-20, 0, 0).withRightArm(-110, 0, 20);
    }

    /** DIO's walk: an unhurried stride, arms barely moving. */
    public static Pose walk(double phase) {
        double swing = Math.sin(phase) * 28;
        return DIO_IDLE.withRightLeg(swing, 0, 3).withLeftLeg(-swing, 0, -3)
                .withLeftArm(-25 - swing * 0.4, 0, -10);
    }

    // ------------------------------------------------------------------ geometry

    /**
     * Where The World floats when it is not fighting: behind DIO's right shoulder, a little above
     * him, bobbing gently.
     */
    public static Vector worldIdle(Vector feet, Vector forward, double scale, int tick) {
        Vector right = right(forward);
        double bob = Math.sin(tick * 0.08) * 0.15;
        return feet.clone()
                .subtract(forward.clone().multiply(1.1 * scale))
                .add(right.multiply(0.9 * scale))
                .add(new Vector(0, 0.45 * scale + bob, 0));
    }

    /** The side vector to the right of a horizontal facing (Minecraft: yaw 0 faces +z, right is -x). */
    public static Vector right(Vector forward) {
        Vector f = flat(forward);
        return new Vector(-f.getZ(), 0, f.getX());
    }

    /** A vector with its height dropped and its length set to 1 (+z when it had no horizontal part). */
    public static Vector flat(Vector v) {
        Vector f = new Vector(v.getX(), 0, v.getZ());
        return f.lengthSquared() < 1e-9 ? new Vector(0, 0, 1) : f.normalize();
    }

    /**
     * Where the knives of a time stop hang around a frozen player: an uneven ring at chest height,
     * so they read as thrown, not placed.
     */
    public static List<Vector> knifeRing(Vector chest, int count, double radius, double phase) {
        List<Vector> out = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle = phase + i * 2 * Math.PI / count;
            double height = 0.45 * Math.sin(i * 2.3 + phase);
            double r = radius * (0.85 + 0.15 * Math.cos(i * 1.7));
            out.add(chest.clone().add(new Vector(Math.cos(angle) * r, height, Math.sin(angle) * r)));
        }
        return out;
    }

    /** Whether {@code point} is inside a cone from {@code apex} along {@code direction}. */
    public static boolean inCone(Vector apex, Vector direction, double halfAngleDegrees, double length, Vector point) {
        Vector to = point.clone().subtract(apex);
        double distance = to.length();
        if (distance > length) return false;
        if (distance < 1e-6) return true;
        double cos = to.dot(direction.clone().normalize()) / distance;
        return cos >= Math.cos(Math.toRadians(halfAngleDegrees));
    }

    /** Distance from {@code point} to the segment {@code a}-{@code b}. */
    public static double distanceToSegment(Vector point, Vector a, Vector b) {
        Vector ab = b.clone().subtract(a);
        double len = ab.lengthSquared();
        double t = len < 1e-9 ? 0 : Math.max(0, Math.min(1, point.clone().subtract(a).dot(ab) / len));
        return point.distance(a.clone().add(ab.multiply(t)));
    }

    /** The rotation that lays an item sprite's blade (handle bottom-left, point top-right) along {@code direction}. */
    public static Quaternionf blade(Vector direction) {
        Vector d = direction.clone().normalize();
        float k = (float) (1 / Math.sqrt(2));
        return new Quaternionf().rotationTo(k, k, 0, (float) d.getX(), (float) d.getY(), (float) d.getZ());
    }

    /** The yaw (degrees, Minecraft convention) that faces along {@code direction}. */
    public static float yawOf(Vector direction) {
        return (float) Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
    }

    /** The parts of the road roller: block, size (x across, y up, z along), and the corner offset from its centre. */
    public record RollerPart(String block, float[] size, float[] corner) {
    }

    /** The road roller, about 3.4 blocks wide and 5 long, built around its centre at ground level. */
    public static final List<RollerPart> ROAD_ROLLER = List.of(
            new RollerPart("YELLOW_CONCRETE", new float[]{3.0f, 1.7f, 3.2f}, new float[]{-1.5f, 0.9f, -1.6f}),
            new RollerPart("YELLOW_CONCRETE_POWDER", new float[]{2.4f, 1.4f, 1.6f}, new float[]{-1.2f, 2.6f, -1.4f}),
            new RollerPart("BLACK_CONCRETE", new float[]{2.6f, 0.15f, 1.8f}, new float[]{-1.3f, 4.0f, -1.5f}),
            new RollerPart("GRAY_CONCRETE", new float[]{3.4f, 1.8f, 1.8f}, new float[]{-1.7f, 0.0f, 1.6f}),
            new RollerPart("BLACK_CONCRETE", new float[]{0.7f, 1.6f, 1.6f}, new float[]{-2.0f, 0.0f, -1.9f}),
            new RollerPart("BLACK_CONCRETE", new float[]{0.7f, 1.6f, 1.6f}, new float[]{1.3f, 0.0f, -1.9f}));

    /** Height of the roller's roof above its base, where DIO crouches. */
    public static final double ROLLER_ROOF = 4.15;
}
