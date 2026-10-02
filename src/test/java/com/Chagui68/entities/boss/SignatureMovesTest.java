package com.Chagui68.entities.boss;

import com.Chagui68.utils.MscLimb;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The pose tables behind NIX's and JackStar's signature moves: every frame is a real rotation, no
 * joint folds past what the model allows, consecutive frames never jump (a jump reads as the model
 * teleporting a limb), and each move hands the body back close to its rest pose.
 */
class SignatureMovesTest {

    /** Largest rotation a limb may make between two ticks, in radians; the fastest chop is ~0.9. */
    private static final double MAX_STEP = 1.2;
    /** How far from rest a limb may be on the last tick of a move. */
    private static final double END_SLACK = 0.35;

    @ParameterizedTest
    @EnumSource(value = NixMoves.Move.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
    void nixMovesStayInsideTheJoints(NixMoves.Move move) {
        for (NixBoss.LimbGroup group : NixBoss.LimbGroup.values()) {
            Quaternionf previous = NixMoves.limb(move, group, 0);
            for (int t = 0; t < move.ticks; t++) {
                Quaternionf q = NixMoves.limb(move, group, t);
                assertReal(q, move + " " + group + " @" + t);
                assertTrue(angle(previous, q) <= MAX_STEP, move + " " + group + " jumps at tick " + t);
                previous = q;
            }
            assertTrue(angle(previous, new Quaternionf()) <= END_SLACK, move + " leaves " + group + " far from rest");
        }
        for (NixBoss.NixPart part : NixBoss.NixPart.values()) {
            for (int t = 0; t < move.ticks; t++) {
                Quaternionf q = NixMoves.lower(move, part, t);
                assertReal(q, move + " " + part + " @" + t);
                assertTrue(angle(q, new Quaternionf()) <= MscLimb.MAX_BEND + 1e-4, move + " folds " + part + " too far");
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = JackMoves.Move.class, names = "NONE", mode = EnumSource.Mode.EXCLUDE)
    void jackMovesStayInsideTheJoints(JackMoves.Move move) {
        for (JackStarBoss.LimbGroup group : JackStarBoss.LimbGroup.values()) {
            Quaternionf previous = JackMoves.limb(move, group, 0);
            for (int t = 0; t < move.ticks; t++) {
                Quaternionf q = JackMoves.limb(move, group, t);
                assertReal(q, move + " " + group + " @" + t);
                assertTrue(angle(previous, q) <= MAX_STEP, move + " " + group + " jumps at tick " + t);
                previous = q;
            }
            assertTrue(angle(previous, new Quaternionf()) <= END_SLACK, move + " leaves " + group + " far from rest");
        }
        for (JackStarBoss.JackPart part : JackStarBoss.JackPart.values()) {
            for (int t = 0; t < move.ticks; t++) {
                Quaternionf q = JackMoves.lower(move, part, t);
                assertReal(q, move + " " + part + " @" + t);
                assertTrue(angle(q, new Quaternionf()) <= MscLimb.MAX_BEND + 1e-4, move + " folds " + part + " too far");
            }
        }
    }

    @Test
    void theBlowsLandWithTheBody() {
        // Gallows: arms overhead in the air, forward-down a few ticks after landing.
        float air = angleX(NixMoves.limb(NixMoves.Move.GALLOWS, NixBoss.LimbGroup.ARM_RIGHT, NixMoves.GALLOWS_LAND - 1));
        float slam = angleX(NixMoves.limb(NixMoves.Move.GALLOWS, NixBoss.LimbGroup.ARM_RIGHT, NixMoves.GALLOWS_LAND + 4));
        assertTrue(air > 2.5 && slam < 1.2, "gallows arms: air " + air + ", slam " + slam);
        // Condemnation: the arm is up while the sentence holds and down once the blades fall.
        float held = angleX(NixMoves.limb(NixMoves.Move.CONDEMN, NixBoss.LimbGroup.ARM_RIGHT, NixMoves.CONDEMN_CHOP - 4));
        float chopped = angleX(NixMoves.limb(NixMoves.Move.CONDEMN, NixBoss.LimbGroup.ARM_RIGHT,
                NixMoves.CONDEMN_CHOP + NixMoves.CONDEMN_FALL));
        assertTrue(held > 2.7 && chopped < 1.2, "condemn arm: held " + held + ", chopped " + chopped);
        // fork(): the throwing arm is cocked back before the release and forward after it.
        float cocked = angleX(JackMoves.limb(JackMoves.Move.FORK_BOMB, JackStarBoss.LimbGroup.ARM_RIGHT, JackMoves.FORK_WIND - 1));
        float thrown = angleX(JackMoves.limb(JackMoves.Move.FORK_BOMB, JackStarBoss.LimbGroup.ARM_RIGHT, JackMoves.FORK_WIND + 8));
        assertTrue(cocked < -1.0 && thrown > 1.0, "fork arm: cocked " + cocked + ", thrown " + thrown);
    }

    private static void assertReal(Quaternionf q, String what) {
        assertTrue(Float.isFinite(q.x) && Float.isFinite(q.y) && Float.isFinite(q.z) && Float.isFinite(q.w), what);
        assertTrue(Math.abs(q.lengthSquared() - 1) < 1e-3, what + " is not a rotation");
    }

    /** The angle between two rotations. */
    private static double angle(Quaternionf a, Quaternionf b) {
        double dot = Math.abs(a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w);
        return 2 * Math.acos(Math.min(1, dot));
    }

    /** The X (forward swing) component of a rotation that is mostly about X. */
    private static float angleX(Quaternionf q) {
        return 2f * (float) Math.atan2(q.x, q.w);
    }
}
