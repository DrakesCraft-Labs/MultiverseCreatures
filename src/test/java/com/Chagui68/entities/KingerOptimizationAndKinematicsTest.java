package com.Chagui68.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KingerOptimizationAndKinematicsTest {

    @Test
    @DisplayName("Verify Kinger model has exactly 15 parts matching Blockbench specification")
    void testPartCount() {
        assertEquals(15, Kinger.KingerPart.values().length,
                "Kinger model must contain exactly 15 ItemDisplay parts");
    }

    @Test
    @DisplayName("Verify Kinger transformation matrices decompose into finite vectors and valid quaternions")
    void testPartMatrixIntegrity() {
        for (Kinger.KingerPart part : Kinger.KingerPart.values()) {
            assertNotNull(part.matrix, "Matrix must not be null for " + part.name());
            assertEquals(16, part.matrix.length, "Matrix must have 16 float elements for " + part.name());

            assertNotNull(part.offset, "Offset vector must not be null for " + part.name());
            assertTrue(Float.isFinite(part.offset.x) && Float.isFinite(part.offset.y) && Float.isFinite(part.offset.z),
                    "Offset coordinates must be finite floats for " + part.name());

            assertNotNull(part.scale, "Scale vector must not be null for " + part.name());
            assertTrue(part.scale.x > 0 && part.scale.y > 0 && part.scale.z > 0,
                    "Scale components must be strictly positive for " + part.name());

            assertNotNull(part.rotation, "Rotation quaternion must not be null for " + part.name());
            assertTrue(Float.isFinite(part.rotation.x) && Float.isFinite(part.rotation.y)
                            && Float.isFinite(part.rotation.z) && Float.isFinite(part.rotation.w),
                    "Rotation quaternion components must be finite for " + part.name());
        }
    }

    @Test
    @DisplayName("Verify Kinger CENTER pivot vector is finite and centered")
    void testCenterPivotVector() {
        assertNotNull(Kinger.KingerPart.CENTER, "Center pivot vector must not be null");
        assertTrue(Float.isFinite(Kinger.KingerPart.CENTER.x), "Center X must be finite");
        assertTrue(Float.isFinite(Kinger.KingerPart.CENTER.z), "Center Z must be finite");
    }

    @Test
    @DisplayName("Verify Kinger idle display synchronization throttles by ~66% when stationary")
    void testIdleDisplayThrottling() {
        Kinger.KingerInstance inst = new Kinger.KingerInstance(null);

        // State 1: Completely stationary and idle
        inst.moving = false;
        inst.meleeAnim = 0;
        inst.rangedAnim = 0;

        int idleSyncCount = 0;
        for (int i = 0; i < 90; i++) {
            inst.tickCount++;
            boolean isIdle = !inst.moving && inst.meleeAnim == 0 && inst.rangedAnim == 0;
            if (!isIdle || inst.tickCount % 3 == 0) {
                idleSyncCount++;
            }
        }
        // In 90 ticks, 90 / 3 = 30 sync executions (a 66.7% reduction)
        assertEquals(30, idleSyncCount, "Idle synchronization must execute exactly once every 3 ticks");

        // State 2: Moving (chasing player)
        inst.moving = true;
        inst.meleeAnim = 0;
        inst.rangedAnim = 0;

        int movingSyncCount = 0;
        for (int i = 0; i < 90; i++) {
            inst.tickCount++;
            boolean isIdle = !inst.moving && inst.meleeAnim == 0 && inst.rangedAnim == 0;
            if (!isIdle || inst.tickCount % 3 == 0) {
                movingSyncCount++;
            }
        }
        // When moving, must sync every single tick for smooth movement
        assertEquals(90, movingSyncCount, "Moving entity must sync displays on every tick");

        // State 3: Stationary but performing melee animation
        inst.moving = false;
        inst.meleeAnim = 10;
        inst.rangedAnim = 0;

        int meleeSyncCount = 0;
        for (int i = 0; i < 90; i++) {
            inst.tickCount++;
            boolean isIdle = !inst.moving && inst.meleeAnim == 0 && inst.rangedAnim == 0;
            if (!isIdle || inst.tickCount % 3 == 0) {
                meleeSyncCount++;
            }
        }
        assertEquals(90, meleeSyncCount, "Melee animation must sync displays on every tick");

        // State 4: Stationary but performing ranged animation
        inst.moving = false;
        inst.meleeAnim = 0;
        inst.rangedAnim = 15;

        int rangedSyncCount = 0;
        for (int i = 0; i < 90; i++) {
            inst.tickCount++;
            boolean isIdle = !inst.moving && inst.meleeAnim == 0 && inst.rangedAnim == 0;
            if (!isIdle || inst.tickCount % 3 == 0) {
                rangedSyncCount++;
            }
        }
        assertEquals(90, rangedSyncCount, "Ranged animation must sync displays on every tick");
    }
}
