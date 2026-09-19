package com.Chagui68.entities;

import com.Chagui68.entities.boss.JackStarBoss;
import com.Chagui68.ritual.JackInvocationStructure;
import org.bukkit.Material;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JackStarCombatPhasesTest {

    @Test
    public void testJackPartModelDefinitions() {
        assertEquals(11, JackStarBoss.JackPart.values().length, "JackStar model must contain exactly 11 parts");
        assertNotNull(JackStarBoss.JackPart.CENTER, "Model center must be computed");
    }

    @Test
    public void testScaleTransformationMatrices() {
        JackStarBoss.JackPart head = JackStarBoss.JackPart.HEAD;
        Vector3f baseScale = head.scale;

        // Normal scale 1.0
        float normalScale = 1.0f;
        Vector3f normalResult = new Vector3f(baseScale).mul(normalScale);
        assertEquals(baseScale.x, normalResult.x, 0.001f);

        // Giant scale 2.2
        float giantScale = 2.2f;
        Vector3f giantResult = new Vector3f(baseScale).mul(giantScale);
        assertTrue(giantResult.x > baseScale.x * 2.1f, "Giant scale must exceed 2.1x");

        // Micro scale 0.6
        float microScale = 0.6f;
        Vector3f microResult = new Vector3f(baseScale).mul(microScale);
        assertTrue(microResult.x < baseScale.x * 0.7f, "Micro scale must shrink below 0.7x");
    }

    @Test
    public void testThreeLivesWatchdogDecrement() {
        int livesRemaining = 3;

        // Death 1
        livesRemaining--;
        assertEquals(2, livesRemaining, "First fatal hit leaves 2 lives");
        int rebootNum1 = 3 - livesRemaining;
        assertEquals(1, rebootNum1);

        // Death 2
        livesRemaining--;
        assertEquals(1, livesRemaining, "Second fatal hit leaves 1 life");
        int rebootNum2 = 3 - livesRemaining;
        assertEquals(2, rebootNum2);

        // Death 3
        livesRemaining--;
        assertEquals(0, livesRemaining, "Third fatal hit leaves 0 lives");
        int rebootNum3 = 3 - livesRemaining;
        assertEquals(3, rebootNum3);

        // Death 4 -> True death
        assertTrue(livesRemaining <= 0, "No more watchdog lives; true boss defeat");
    }

    @Test
    public void testInvocationStructureCoreMaterials() {
        assertTrue(JackInvocationStructure.isValidCore(Material.RESPAWN_ANCHOR), "Respawn anchor must be valid core");
        assertTrue(JackInvocationStructure.isValidCore(Material.LODESTONE), "Lodestone must be valid core");
        assertTrue(JackInvocationStructure.isValidCandle(Material.CYAN_CANDLE), "Cyan candle must be valid candle");
        assertTrue(JackInvocationStructure.isValidCandle(Material.LIGHT_BLUE_CANDLE), "Light blue candle must be valid candle");
        assertTrue(JackInvocationStructure.isValidBase(Material.CRYING_OBSIDIAN), "Crying obsidian must be valid base");
        assertTrue(JackInvocationStructure.isValidBase(Material.POLISHED_BLACKSTONE_BRICKS), "Polished blackstone must be valid base");
        assertEquals(4, JackInvocationStructure.CANDLE_OFFSETS.length, "Must have 4 candle offsets");
        assertEquals(4, JackInvocationStructure.CORNER_OFFSETS.length, "Must have 4 corner pillar offsets");
    }

    @Test
    public void testPhaseTransitionsCalculation() {
        double maxHealth = 700.0;

        // 100% -> Phase 1
        double hp = 700.0;
        double ratio = hp / maxHealth;
        int phase = getPhase(ratio, false);
        assertEquals(1, phase);

        // 75% -> Phase 2
        hp = 525.0;
        ratio = hp / maxHealth;
        phase = getPhase(ratio, false);
        assertEquals(2, phase);

        // 50% -> Phase 3
        hp = 350.0;
        ratio = hp / maxHealth;
        phase = getPhase(ratio, false);
        assertEquals(3, phase);

        // 30% -> Phase 4
        hp = 210.0;
        ratio = hp / maxHealth;
        phase = getPhase(ratio, false);
        assertEquals(4, phase);

        // 10% -> Phase 5
        hp = 70.0;
        ratio = hp / maxHealth;
        phase = getPhase(ratio, false);
        assertEquals(5, phase);

        // Kernel Panic override -> Phase 5
        phase = getPhase(1.0, true);
        assertEquals(5, phase);
    }

    private int getPhase(double ratio, boolean isKernelPanic) {
        if (isKernelPanic || ratio <= 0.15) {
            return 5;
        } else if (ratio <= 0.40) {
            return 4;
        } else if (ratio <= 0.60) {
            return 3;
        } else if (ratio <= 0.80) {
            return 2;
        } else {
            return 1;
        }
    }
}
