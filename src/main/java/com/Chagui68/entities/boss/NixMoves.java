package com.Chagui68.entities.boss;

import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.utils.MscLimb;
import org.joml.Quaternionf;

/**
 * NIX's three signature moves as pure pose maths: for a move and a tick, how far each limb swings
 * and how far each elbow and knee folds. {@link NixBoss} drives the world side (movement, particles,
 * damage) from the same phase boundaries, so the body and the blow always line up.
 *
 * <p>Sign conventions follow the model: a positive X rotation swings an arm forward (a full
 * {@code PI} holds it straight up), a positive Z rotation lifts the right arm out to the side and a
 * negative one the left. A positive elbow bend folds the forearm forward; a negative knee bend folds
 * the shin back.
 */
public final class NixMoves {

    /** The moves, each with its length in ticks. */
    public enum Move {
        NONE(0),
        /** Blood Harvest: arms flung wide, three whirling revolutions that cut everything close. */
        HARVEST(64),
        /** Gallows Leap: a deep crouch, a leap onto the target and a two-handed slam on landing. */
        GALLOWS(52),
        /** Condemnation: a raised arm brands every player with a guillotine that falls on its chop. */
        CONDEMN(70);

        public final int ticks;

        Move(int ticks) {
            this.ticks = ticks;
        }
    }

    // Harvest phases.
    public static final int HARVEST_WIND = 16;
    public static final int HARVEST_SPIN_END = 52;
    public static final int HARVEST_REVOLUTION = 12;

    // Gallows phases.
    public static final int GALLOWS_CROUCH = 14;
    public static final int GALLOWS_LAND = 32;

    // Condemnation phases.
    public static final int CONDEMN_RAISE = 14;
    public static final int CONDEMN_CHOP = 44;
    public static final int CONDEMN_FALL = 3;

    private NixMoves() {
    }

    /** The limb rotation of {@code group} on tick {@code t} of {@code move}. */
    public static Quaternionf limb(Move move, NixBoss.LimbGroup group, int t) {
        Quaternionf q = new Quaternionf();
        switch (move) {
            case HARVEST -> harvest(group, t, q);
            case GALLOWS -> gallows(group, t, q);
            case CONDEMN -> condemn(group, t, q);
            default -> {
            }
        }
        return q;
    }

    /** The fold of an elbow or knee piece on tick {@code t} of {@code move}; identity above the joint. */
    public static Quaternionf lower(Move move, NixBoss.NixPart part, int t) {
        if (!NixModel.hangsFromSecondJoint(part)) return new Quaternionf();
        boolean arm = part.group == NixBoss.LimbGroup.ARM_RIGHT || part.group == NixBoss.LimbGroup.ARM_LEFT;
        float bend = switch (move) {
            case HARVEST -> arm ? 0.15f : -0.45f * (float) Math.sin(Math.PI * Math.min(1, t / (double) HARVEST_WIND));
            case GALLOWS -> {
                if (t < GALLOWS_CROUCH) yield arm ? 0.5f * phase(t, 0, GALLOWS_CROUCH) : -1.1f * phase(t, 0, GALLOWS_CROUCH);
                if (t < GALLOWS_LAND) yield arm ? 0.3f : -0.9f;
                // Landing: knees take the blow, then straighten.
                yield arm ? 0f : -1.0f * (1 - phase(t, GALLOWS_LAND, GALLOWS_LAND + 14));
            }
            case CONDEMN -> arm ? 0.05f : (t >= CONDEMN_CHOP && t < CONDEMN_CHOP + 10 ? -0.4f : 0f);
            default -> 0f;
        };
        return MscLimb.bendAngle(bend);
    }

    /** Arms rise out to the sides, hold through the spin and come back down. */
    private static void harvest(NixBoss.LimbGroup group, int t, Quaternionf q) {
        float out;
        if (t < HARVEST_WIND) out = (float) Ease.at(Ease.OUT_BACK, phase(t, 0, HARVEST_WIND));
        else if (t < HARVEST_SPIN_END) out = 1;
        else out = 1 - (float) Ease.at(Ease.IN_OUT, phase(t, HARVEST_SPIN_END, Move.HARVEST.ticks));
        switch (group) {
            case ARM_RIGHT -> q.rotateZ(1.45f * out).rotateX(0.25f * out);
            case ARM_LEFT -> q.rotateZ(-1.45f * out).rotateX(0.25f * out);
            // Leaning into the spin.
            case TORSO_UPPER -> q.rotateX(0.12f * out);
            case HEAD -> q.rotateX(-0.15f * out);
            case LEG_RIGHT -> q.rotateZ(-0.18f * out);
            case LEG_LEFT -> q.rotateZ(0.18f * out);
            default -> {
            }
        }
    }

    /** Crouch with the arms thrown back, tuck in the air with them overhead, slam down on landing. */
    private static void gallows(NixBoss.LimbGroup group, int t, Quaternionf q) {
        boolean arm = group == NixBoss.LimbGroup.ARM_RIGHT || group == NixBoss.LimbGroup.ARM_LEFT;
        boolean leg = group == NixBoss.LimbGroup.LEG_RIGHT || group == NixBoss.LimbGroup.LEG_LEFT;
        float a;
        if (t < GALLOWS_CROUCH) {
            float p = (float) Ease.at(Ease.IN_OUT, phase(t, 0, GALLOWS_CROUCH));
            a = arm ? -0.9f * p : leg ? 0.7f * p : group == NixBoss.LimbGroup.TORSO_UPPER || group == NixBoss.LimbGroup.TORSO_LOWER ? 0.35f * p : 0;
        } else if (t < GALLOWS_LAND) {
            float p = (float) Ease.at(Ease.IN_OUT, phase(t, GALLOWS_CROUCH, GALLOWS_CROUCH + 10));
            a = arm ? -0.9f + 3.75f * p : leg ? 0.7f - 0.2f * p : group == NixBoss.LimbGroup.TORSO_UPPER ? 0.35f - 0.55f * p : 0;
            // The arms start coming down just before touchdown.
            if (arm && t >= GALLOWS_LAND - 3) a = 2.85f + (0.9f - 2.85f) * (float) Ease.at(Ease.IN, phase(t, GALLOWS_LAND - 3, GALLOWS_LAND + 3));
        } else {
            // The chop: overhead to forward-down across touchdown, then a slow recovery.
            float hit = (float) Ease.at(Ease.IN, phase(t, GALLOWS_LAND - 3, GALLOWS_LAND + 3));
            float rest = (float) Ease.at(Ease.IN_OUT, phase(t, GALLOWS_LAND + 12, Move.GALLOWS.ticks));
            if (arm) a = (2.85f + (0.9f - 2.85f) * hit) * (1 - rest);
            else if (leg) a = 0.6f * (1 - rest);
            else if (group == NixBoss.LimbGroup.TORSO_UPPER) a = (-0.2f + 0.6f * hit) * (1 - rest);
            else a = 0;
        }
        q.rotateX(a);
        if (group == NixBoss.LimbGroup.HEAD) q.rotateX(t < GALLOWS_LAND ? -0.3f : 0);
    }

    /** The right arm rises straight up and holds; on the chop it comes down like a blade. */
    private static void condemn(NixBoss.LimbGroup group, int t, Quaternionf q) {
        float raise = (float) Ease.at(Ease.OUT_BACK, phase(t, 0, CONDEMN_RAISE));
        float chop = (float) Ease.at(Ease.IN, phase(t, CONDEMN_CHOP - 2, CONDEMN_CHOP + CONDEMN_FALL));
        float rest = (float) Ease.at(Ease.IN_OUT, phase(t, CONDEMN_CHOP + 10, Move.CONDEMN.ticks));
        switch (group) {
            case ARM_RIGHT -> {
                float up = 3.0f * raise;
                // A small tremor while it holds the sentence.
                float shake = t > CONDEMN_RAISE && t < CONDEMN_CHOP ? 0.04f * (float) Math.sin(t * 1.7) : 0;
                q.rotateX((up + (0.9f - up) * chop + shake) * (1 - rest));
            }
            // The left hand points at the condemned until the blades fall.
            case ARM_LEFT -> q.rotateX(1.5f * raise * (1 - chop) * (1 - rest)).rotateZ(-0.2f * raise * (1 - rest));
            case TORSO_UPPER -> q.rotateX((-0.12f * raise + 0.45f * chop) * (1 - rest));
            // Looks up at the raised hand, then down at the condemned as the blades fall.
            case HEAD -> q.rotateX((0.35f * raise - 0.6f * chop) * (1 - rest));
            default -> {
            }
        }
    }

    /** 0 → 1 across {@code [from, to)}, clamped. */
    static float phase(int t, int from, int to) {
        if (to <= from) return t >= to ? 1 : 0;
        return Math.max(0f, Math.min(1f, (t - from) / (float) (to - from)));
    }
}
