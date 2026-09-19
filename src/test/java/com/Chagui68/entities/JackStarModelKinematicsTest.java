package com.Chagui68.entities;

import com.Chagui68.entities.boss.JackStarBoss;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JackStarModelKinematicsTest {

    @Test
    @DisplayName("Verify JackStar model contains exactly 11 parts matching user specification")
    void testTotalPartCount() {
        assertEquals(11, JackStarBoss.JackPart.values().length,
                "JackStar model must have exactly 11 ItemDisplay parts");
    }

    @Test
    @DisplayName("Verify limb groupings conform to rigid kinematic hierarchy")
    void testLimbGroupCounts() {
        Map<JackStarBoss.LimbGroup, Integer> counts = new EnumMap<>(JackStarBoss.LimbGroup.class);
        for (JackStarBoss.JackPart part : JackStarBoss.JackPart.values()) {
            counts.put(part.group, counts.getOrDefault(part.group, 0) + 1);
        }

        assertEquals(1, counts.get(JackStarBoss.LimbGroup.HEAD), "Head must consist of 1 part");
        assertEquals(1, counts.get(JackStarBoss.LimbGroup.TORSO_UPPER), "Upper torso must consist of 1 part");
        assertEquals(1, counts.get(JackStarBoss.LimbGroup.TORSO_LOWER), "Lower torso/pelvis must consist of 1 part");
        assertEquals(2, counts.get(JackStarBoss.LimbGroup.ARM_RIGHT), "Right arm must consist of 2 segments");
        assertEquals(2, counts.get(JackStarBoss.LimbGroup.ARM_LEFT), "Left arm must consist of 2 segments");
        assertEquals(2, counts.get(JackStarBoss.LimbGroup.LEG_RIGHT), "Right leg must consist of 2 segments");
        assertEquals(2, counts.get(JackStarBoss.LimbGroup.LEG_LEFT), "Left leg must consist of 2 segments");
    }

    @Test
    @DisplayName("Verify transformation matrices decompose into finite, non-null vectors and quaternions")
    void testPartMatrixIntegrity() {
        for (JackStarBoss.JackPart part : JackStarBoss.JackPart.values()) {
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

            assertNotNull(part.profileName, "Skin profile name must not be null for " + part.name());
            assertFalse(part.profileName.isBlank(), "Skin profile name must not be blank for " + part.name());

            assertNotNull(part.texture, "Base64 skin texture must not be null for " + part.name());
            assertFalse(part.texture.isBlank(), "Base64 skin texture must not be blank for " + part.name());
        }
    }

    @Test
    @DisplayName("Verify model horizontal center is finite and non-NaN")
    void testModelCenterCalculation() {
        Vector3f center = JackStarBoss.JackPart.CENTER;
        assertNotNull(center, "Model center must be precalculated");
        assertTrue(Float.isFinite(center.x), "Center X must be a finite float");
        assertTrue(Float.isFinite(center.z), "Center Z must be a finite float");
    }
}
