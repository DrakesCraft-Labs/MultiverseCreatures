package com.Chagui68.entities.boss;

import com.Chagui68.entities.boss.fx.Pose;
import org.bukkit.util.Vector;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** DIO's and The World's poses and the geometry their attacks use. */
class DioMovesTest {

    private static final Vector NORTH = new Vector(0, 0, -1);

    @Test
    @DisplayName("The World floats behind DIO's right shoulder, whichever way he faces")
    void theWorldFloatsBehindHim() {
        for (Vector forward : List.of(new Vector(0, 0, 1), NORTH, new Vector(1, 0, 0), new Vector(1, 0, 1))) {
            Vector feet = new Vector(10, 64, -5);
            Vector at = DioMoves.worldIdle(feet, forward, 1.15, 0);
            Vector offset = at.clone().subtract(feet);
            Vector f = DioMoves.flat(forward);
            assertTrue(offset.dot(f) < 0, "The World is behind him, not in front");
            assertTrue(offset.dot(DioMoves.right(forward)) > 0, "The World is on his right");
            assertTrue(offset.getY() > 0, "The World floats above the ground");
        }
    }

    @Test
    @DisplayName("Right is right: facing +z (yaw 0) it points to -x")
    void rightFollowsMinecraftYaw() {
        Vector right = DioMoves.right(new Vector(0, 0, 1));
        assertEquals(-1, right.getX(), 1e-9);
        assertEquals(0, right.getZ(), 1e-9);
        assertEquals(0, DioMoves.yawOf(new Vector(0, 0, 1)), 1e-6);
        assertEquals(90, DioMoves.yawOf(new Vector(-1, 0, 0)), 1e-6);
    }

    @Test
    @DisplayName("The knives of a time stop ring the frozen player, every one within reach of the chest")
    void knivesRingThePlayer() {
        Vector chest = new Vector(3, 70, 3);
        List<Vector> ring = DioMoves.knifeRing(chest, 8, 2.4, 0.7);
        assertEquals(8, ring.size());
        for (Vector knife : ring) {
            double flat = Math.hypot(knife.getX() - chest.getX(), knife.getZ() - chest.getZ());
            assertTrue(flat >= 2.4 * 0.7 - 1e-9 && flat <= 2.4 + 1e-9, "a knife hangs too close or too far: " + flat);
            assertTrue(Math.abs(knife.getY() - chest.getY()) <= 0.5);
        }
    }

    @Test
    @DisplayName("The barrage cone only reaches what is in front, and only so far")
    void coneReachesTheFront() {
        Vector apex = new Vector(0, 0, 0);
        assertTrue(DioMoves.inCone(apex, NORTH, 50, 4, new Vector(0.5, 0, -3)));
        assertFalse(DioMoves.inCone(apex, NORTH, 50, 4, new Vector(0, 0, 3)), "behind");
        assertFalse(DioMoves.inCone(apex, NORTH, 50, 4, new Vector(0, 0, -6)), "out of reach");
        assertFalse(DioMoves.inCone(apex, NORTH, 50, 4, new Vector(3, 0, -0.5)), "off to the side");
    }

    @Test
    @DisplayName("The barrage alternates fists every tick")
    void barrageAlternates() {
        Pose even = DioMoves.barrage(0, 0, 0);
        Pose odd = DioMoves.barrage(1, 0, 0);
        assertTrue(even.rightArm()[0] < -80 && even.leftArm()[0] > -60, "right fist out on even ticks");
        assertTrue(odd.leftArm()[0] < -80 && odd.rightArm()[0] > -60, "left fist out on odd ticks");
    }

    @Test
    @DisplayName("A knife's blade points where it flies")
    void bladePointsAlongFlight() {
        Vector dir = new Vector(0.3, -0.2, 1).normalize();
        float k = (float) (1 / Math.sqrt(2));
        Vector3f tip = DioMoves.blade(dir).transform(new Vector3f(k, k, 0));
        assertEquals(dir.getX(), tip.x, 1e-5);
        assertEquals(dir.getY(), tip.y, 1e-5);
        assertEquals(dir.getZ(), tip.z, 1e-5);
    }

    @Test
    @DisplayName("The road roller is a solid, real block model sitting on its base")
    void roadRollerIsBuilt() {
        for (DioMoves.RollerPart part : DioMoves.ROAD_ROLLER) {
            assertTrue(org.bukkit.Material.matchMaterial(part.block()) != null, part.block() + " is not a block");
            for (float size : part.size()) assertTrue(size > 0);
            assertTrue(part.corner()[1] >= 0, "nothing hangs below the ground");
            assertTrue(part.corner()[1] + part.size()[1] <= DioMoves.ROLLER_ROOF + 1e-6, "the roof is the top");
        }
    }
}
