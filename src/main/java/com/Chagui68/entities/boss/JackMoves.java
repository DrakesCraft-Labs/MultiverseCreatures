package com.Chagui68.entities.boss;

import com.Chagui68.entities.boss.fx.Ease;
import com.Chagui68.utils.MscLimb;
import org.joml.Quaternionf;

/**
 * JackStar's three signature moves as pure pose maths, the same way {@link NixMoves} does it for
 * NIX: for a move and a tick, how each limb swings and how each elbow and knee folds. The world side
 * lives in {@link JackStarBoss} and keys off the same phase constants.
 *
 * <p>A positive X rotation swings an arm forward ({@code PI} holds it straight up); a positive Y
 * rotation sweeps the right arm across the body, as the three-slash does.
 */
public final class JackMoves {

    public enum Move {
        NONE(0),
        /** fork(): a glitching cube thrown at the target that splits in two every time it lands. */
        FORK_BOMB(58),
        /** Binary Rain: Jack types into the air and columns of falling bits pound the arena. */
        BINARY_RAIN(78),
        /** Stack Overflow: four dashing cuts through the target, then the whole stack unwinds at once. */
        STACK_OVERFLOW(66);

        public final int ticks;

        Move(int ticks) {
            this.ticks = ticks;
        }
    }

    // fork(): wind-up, throw (the cube leaves the hand FORK_RELEASE ticks into the swing), then the forks.
    public static final int FORK_WIND = 14;
    public static final int FORK_RELEASE = 4;
    public static final int FORK_FLIGHT = 14;
    public static final int FORK_HOP = 9;

    // Binary Rain.
    public static final int RAIN_RAISE = 16;
    public static final int RAIN_END = 60;

    // Stack Overflow: four dashes of DASH ticks each with a short pause, then the unwinding.
    public static final int STACK_READY = 12;
    public static final int STACK_DASHES = 4;
    public static final int STACK_DASH = 6;
    public static final int STACK_GAP = 2;
    public static final int STACK_UNWIND = STACK_READY + STACK_DASHES * (STACK_DASH + STACK_GAP) + 4;

    private JackMoves() {
    }

    public static Quaternionf limb(Move move, JackStarBoss.LimbGroup group, int t) {
        Quaternionf q = new Quaternionf();
        switch (move) {
            case FORK_BOMB -> fork(group, t, q);
            case BINARY_RAIN -> rain(group, t, q);
            case STACK_OVERFLOW -> stack(group, t, q);
            default -> {
            }
        }
        return q;
    }

    public static Quaternionf lower(Move move, JackStarBoss.JackPart part, int t) {
        if (!JackModel.hangsFromSecondJoint(part)) return new Quaternionf();
        boolean arm = part.group == JackStarBoss.LimbGroup.ARM_RIGHT || part.group == JackStarBoss.LimbGroup.ARM_LEFT;
        boolean right = part.group == JackStarBoss.LimbGroup.ARM_RIGHT;
        float bend = switch (move) {
            case FORK_BOMB -> {
                if (!arm) yield -0.35f * phase(t, 0, FORK_WIND) * (1 - phase(t, FORK_WIND + 6, FORK_WIND + 20));
                // The throwing elbow cocks back, then whips straight.
                if (right) yield t < FORK_WIND ? 1.1f * phase(t, 0, FORK_WIND) : 1.1f * (1 - phase(t, FORK_WIND, FORK_WIND + 6));
                yield 0.2f;
            }
            case BINARY_RAIN -> arm ? 0.7f * phase(t, 0, RAIN_RAISE) * (1 - phase(t, RAIN_END, Move.BINARY_RAIN.ticks)) : 0f;
            case STACK_OVERFLOW -> {
                if (t >= STACK_UNWIND) yield arm ? 0f : -0.3f * (1 - phase(t, STACK_UNWIND, Move.STACK_OVERFLOW.ticks));
                yield arm ? 0.4f : -0.7f;
            }
            default -> 0f;
        };
        return MscLimb.bendAngle(bend);
    }

    /** Right arm cocks back with the cube while the left aims, then the throw and a slow recovery. */
    private static void fork(JackStarBoss.LimbGroup group, int t, Quaternionf q) {
        float wind = (float) Ease.at(Ease.IN_OUT, phase(t, 0, FORK_WIND));
        float snap = (float) Ease.at(Ease.IN_OUT, phase(t, FORK_WIND, FORK_WIND + 9));
        float rest = (float) Ease.at(Ease.IN_OUT, phase(t, FORK_WIND + 12, FORK_WIND + 30));
        switch (group) {
            case ARM_RIGHT -> q.rotateX((-1.4f * wind + (1.6f + 1.4f) * snap) * (1 - rest)).rotateZ(0.3f * wind * (1 - snap));
            case ARM_LEFT -> q.rotateX((1.3f * wind - 1.0f * snap) * (1 - rest));
            case TORSO_UPPER -> q.rotateY((-0.35f * wind + 0.6f * snap) * (1 - rest));
            case LEG_RIGHT -> q.rotateX(-0.35f * wind * (1 - rest));
            case LEG_LEFT -> q.rotateX(0.35f * wind * (1 - rest));
            default -> {
            }
        }
    }

    /** Both hands up at the sky, fingers "typing" in small alternating jabs while the rain falls. */
    private static void rain(JackStarBoss.LimbGroup group, int t, Quaternionf q) {
        float up = (float) Ease.at(Ease.OUT_BACK, phase(t, 0, RAIN_RAISE));
        float rest = (float) Ease.at(Ease.IN_OUT, phase(t, RAIN_END, Move.BINARY_RAIN.ticks));
        float typing = t >= RAIN_RAISE && t < RAIN_END ? 0.12f * (float) Math.sin(t * 1.3) : 0;
        switch (group) {
            case ARM_RIGHT -> q.rotateX((2.2f * up + typing) * (1 - rest)).rotateZ(0.25f * up * (1 - rest));
            case ARM_LEFT -> q.rotateX((2.2f * up - typing) * (1 - rest)).rotateZ(-0.25f * up * (1 - rest));
            case HEAD -> q.rotateX(-0.4f * up * (1 - rest));
            case TORSO_UPPER -> q.rotateX(-0.12f * up * (1 - rest));
            default -> {
            }
        }
    }

    /** Low sprint with both blades swept back; each cut lands with a cross slash at the end of its dash. */
    private static void stack(JackStarBoss.LimbGroup group, int t, Quaternionf q) {
        float ready = (float) Ease.at(Ease.IN_OUT, phase(t, 0, STACK_READY));
        float rest = (float) Ease.at(Ease.IN_OUT, phase(t, STACK_UNWIND + 6, Move.STACK_OVERFLOW.ticks));
        // 0 → 1 → 0 across each dash: the cut.
        float cut = 0;
        if (t >= STACK_READY && t < STACK_UNWIND - 4) {
            int local = (t - STACK_READY) % (STACK_DASH + STACK_GAP);
            cut = (float) Math.sin(Math.PI * Math.min(1, local / (double) (STACK_DASH + STACK_GAP - 1)));
        }
        float hold = ready * (1 - rest);
        switch (group) {
            case ARM_RIGHT -> q.rotateX(-0.9f * hold * (1 - cut)).rotateY(-1.5f * cut).rotateZ(0.4f * cut);
            case ARM_LEFT -> q.rotateX(-0.9f * hold * (1 - cut)).rotateY(1.5f * cut).rotateZ(-0.4f * cut);
            case TORSO_UPPER, TORSO_LOWER -> q.rotateX(0.35f * hold).rotateY(0.25f * cut);
            case LEG_RIGHT -> q.rotateX(t >= STACK_READY && t < STACK_UNWIND ? (float) Math.sin(t * 1.4) * 0.6f : 0.3f * hold);
            case LEG_LEFT -> q.rotateX(t >= STACK_READY && t < STACK_UNWIND ? (float) -Math.sin(t * 1.4) * 0.6f : -0.2f * hold);
            case HEAD -> q.rotateX(-0.25f * hold);
            default -> {
            }
        }
    }

    static float phase(int t, int from, int to) {
        if (to <= from) return t >= to ? 1 : 0;
        return Math.max(0f, Math.min(1f, (t - from) / (float) (to - from)));
    }
}
