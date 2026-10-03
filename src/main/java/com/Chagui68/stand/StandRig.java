package com.Chagui68.stand;

import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Map;

/**
 * The skeleton every Stand body hangs on, and the poses it takes.
 *
 * <p>A Stand is a figure of block displays that all stand at the same spot, each one placed by its
 * own transformation. The figure is split into {@link Part}s: a part turns about its own joint (the
 * neck, a shoulder, an elbow, a hip, a knee) and carries its children with it, so a bent elbow
 * stays attached to the shoulder however the arm swings. Model space has its feet at the origin,
 * {@code +y} up, {@code +z} forward and the Stand's right hand on {@code -x}, which is how a
 * display turned to its user's yaw draws it.</p>
 *
 * <p>Everything here is plain maths, free of the game API, so the tests can check the poses.</p>
 */
public final class StandRig {

    /** One rigid part of the body, the joint it turns about and the part it hangs from. */
    public enum Part {
        BODY(null, 0, 0.83f, 0),
        HEAD(BODY, 0, 1.48f, 0),
        ARM_R(BODY, -0.42f, 1.32f, 0),
        FOREARM_R(ARM_R, -0.42f, 0.98f, 0),
        ARM_L(BODY, 0.42f, 1.32f, 0),
        FOREARM_L(ARM_L, 0.42f, 0.98f, 0),
        THIGH_R(BODY, -0.14f, 0.78f, 0),
        SHIN_R(THIGH_R, -0.14f, 0.40f, 0),
        THIGH_L(BODY, 0.14f, 0.78f, 0),
        SHIN_L(THIGH_L, 0.14f, 0.40f, 0);

        private final Part parent;
        private final Vector3f pivot;

        Part(Part parent, float x, float y, float z) {
            this.parent = parent;
            this.pivot = new Vector3f(x, y, z);
        }

        public Part parent() {
            return parent;
        }

        public Vector3f pivot() {
            return new Vector3f(pivot);
        }

        /** The same part on the other side of the body; the head and the trunk are their own mirror. */
        public Part mirror() {
            return switch (this) {
                case ARM_R -> ARM_L;
                case ARM_L -> ARM_R;
                case FOREARM_R -> FOREARM_L;
                case FOREARM_L -> FOREARM_R;
                case THIGH_R -> THIGH_L;
                case THIGH_L -> THIGH_R;
                case SHIN_R -> SHIN_L;
                case SHIN_L -> SHIN_R;
                default -> this;
            };
        }
    }

    /** Where a part ends up: {@code point -> rotation * point + translation}. */
    public record Frame(Quaternionf rotation, Vector3f translation) {

        static final Frame IDENTITY = new Frame(new Quaternionf(), new Vector3f());

        public Vector3f apply(Vector3f point) {
            return new Vector3f(point).rotate(rotation).add(translation);
        }
    }

    private StandRig() {
    }

    /** A rotation from degrees about x, then y, then z. */
    public static Quaternionf degrees(float x, float y, float z) {
        return new Quaternionf().rotationXYZ((float) Math.toRadians(x), (float) Math.toRadians(y),
                (float) Math.toRadians(z));
    }

    /**
     * The frame of every part, from the rotation each one takes about its own joint.
     *
     * @param local   rotation of each part about its pivot; a missing part keeps its rest pose
     * @param lift    extra translation of the whole body, for the breathing bob
     */
    public static Map<Part, Frame> solve(Map<Part, Quaternionf> local, Vector3f lift) {
        Map<Part, Frame> frames = new EnumMap<>(Part.class);
        for (Part part : Part.values()) {
            Quaternionf turn = local.getOrDefault(part, new Quaternionf());
            Vector3f pivot = part.pivot();
            // About its own joint: x -> turn * (x - pivot) + pivot.
            Vector3f offset = new Vector3f(pivot).sub(new Vector3f(pivot).rotate(turn));
            Frame parent = part.parent() == null ? new Frame(new Quaternionf(), new Vector3f(lift))
                    : frames.get(part.parent());
            Quaternionf rotation = new Quaternionf(parent.rotation()).mul(turn);
            Vector3f translation = new Vector3f(offset).rotate(parent.rotation()).add(parent.translation());
            frames.put(part, new Frame(rotation, translation));
        }
        return frames;
    }

    /**
     * The display transformation of one box: centred on {@code center}, {@code size} big, turned by
     * {@code spin} about its own centre, then carried by its part's frame and scaled about the feet.
     */
    public static Transformation place(Vector3f center, Vector3f size, Quaternionf spin, Frame frame, float scale) {
        Vector3f corner = new Vector3f(size).mul(-0.5f).rotate(spin).add(center);
        Vector3f translation = frame.apply(corner).mul(scale);
        Quaternionf rotation = new Quaternionf(frame.rotation()).mul(spin);
        return new Transformation(translation, rotation, new Vector3f(size).mul(scale), new Quaternionf());
    }

    // ------------------------------------------------------------------ poses

    /**
     * The pose a Stand floats in behind its user: arms ready, legs drifting, the head following
     * where its user looks, all breathing slowly.
     *
     * @param age   ticks since it was summoned
     * @param pitch where its user looks, in degrees (positive is down)
     */
    public static Map<Part, Quaternionf> idle(StandType type, int age, float pitch) {
        float breath = (float) Math.sin(age / 14.0) * 3f;
        Map<Part, Quaternionf> pose = new EnumMap<>(Part.class);
        pose.put(Part.HEAD, degrees(Math.max(-35f, Math.min(35f, pitch * 0.7f)) - breath * 0.3f, 0, 0));
        pose.put(Part.THIGH_R, degrees(-22f + breath, 0, -3f));
        pose.put(Part.SHIN_R, degrees(38f, 0, 0));
        pose.put(Part.THIGH_L, degrees(-8f - breath, 0, 4f));
        pose.put(Part.SHIN_L, degrees(20f, 0, 0));
        switch (type) {
            case STAR_PLATINUM -> {
                // Fists up, ready to throw the first punch.
                pose.put(Part.ARM_R, degrees(-28f + breath, 0, -10f));
                pose.put(Part.FOREARM_R, degrees(-70f, 0, 0));
                pose.put(Part.ARM_L, degrees(-18f - breath, 0, 12f));
                pose.put(Part.FOREARM_L, degrees(-55f, 0, 0));
            }
            case THE_WORLD -> {
                // Arms spread, chest out: the pose of a god looking down.
                pose.put(Part.BODY, degrees(-4f, 0, 0));
                pose.put(Part.HEAD, degrees(Math.max(-35f, Math.min(25f, pitch * 0.7f)) - 8f, 0, 0));
                pose.put(Part.ARM_R, degrees(-6f + breath, 0, -24f));
                pose.put(Part.FOREARM_R, degrees(-22f, 0, 0));
                pose.put(Part.ARM_L, degrees(-6f - breath, 0, 24f));
                pose.put(Part.FOREARM_L, degrees(-22f, 0, 0));
            }
            case MAGICIANS_RED -> {
                // Arms open, palms up, as if holding the flame.
                pose.put(Part.ARM_R, degrees(-20f + breath, 0, -32f));
                pose.put(Part.FOREARM_R, degrees(-62f, 0, 0));
                pose.put(Part.ARM_L, degrees(-20f - breath, 0, 32f));
                pose.put(Part.FOREARM_L, degrees(-62f, 0, 0));
            }
            case KILLER_QUEEN -> {
                // The right hand raised, thumb on the trigger; the left at rest.
                pose.put(Part.ARM_R, degrees(-32f + breath, 0, -6f));
                pose.put(Part.FOREARM_R, degrees(-112f, 0, 0));
                pose.put(Part.ARM_L, degrees(-4f - breath, 0, 8f));
                pose.put(Part.FOREARM_L, degrees(-18f, 0, 0));
            }
            default -> {
                pose.put(Part.ARM_R, degrees(-14f + breath, 0, -12f));
                pose.put(Part.FOREARM_R, degrees(-40f, 0, 0));
                pose.put(Part.ARM_L, degrees(-14f - breath, 0, 12f));
                pose.put(Part.FOREARM_L, degrees(-40f, 0, 0));
            }
        }
        return pose;
    }

    /**
     * One beat of a barrage: one arm thrown all the way forward while the other draws back, the
     * trunk turning behind the punch. Even and odd beats swap the arms.
     */
    public static Map<Part, Quaternionf> barrage(int beat) {
        boolean right = beat % 2 == 0;
        Map<Part, Quaternionf> pose = new EnumMap<>(Part.class);
        pose.put(Part.BODY, degrees(8f, right ? 12f : -12f, 0));
        pose.put(Part.HEAD, degrees(-6f, right ? -10f : 10f, 0));
        Part punching = right ? Part.ARM_R : Part.ARM_L;
        Part drawing = right ? Part.ARM_L : Part.ARM_R;
        float side = right ? -1f : 1f;
        pose.put(punching, degrees(-90f, 0, side * -6f));
        pose.put(right ? Part.FOREARM_R : Part.FOREARM_L, degrees(-2f, 0, 0));
        pose.put(drawing, degrees(-28f, 0, -side * 14f));
        pose.put(right ? Part.FOREARM_L : Part.FOREARM_R, degrees(-110f, 0, 0));
        pose.put(Part.THIGH_R, degrees(-30f, 0, -4f));
        pose.put(Part.SHIN_R, degrees(45f, 0, 0));
        pose.put(Part.THIGH_L, degrees(-12f, 0, 4f));
        pose.put(Part.SHIN_L, degrees(26f, 0, 0));
        return pose;
    }

    /** Where the fist of an arm is in model space, for tests and for the shouts. */
    public static Vector3f fist(Map<Part, Frame> frames, boolean right) {
        Part forearm = right ? Part.FOREARM_R : Part.FOREARM_L;
        return frames.getOrDefault(forearm, Frame.IDENTITY).apply(new Vector3f(right ? -0.42f : 0.42f, 0.66f, 0));
    }
}
