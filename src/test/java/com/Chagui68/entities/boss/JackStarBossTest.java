package com.Chagui68.entities.boss;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JackStarBossTest {

    @ParameterizedTest(name = "{0} health is phase {1}")
    @CsvSource({
            "1.0,  1", "0.81, 1",
            "0.8,  2", "0.61, 2",
            "0.6,  3", "0.41, 3",
            "0.4,  4", "0.16, 4",
            "0.15, 5", "0.0,  5"})
    @DisplayName("Each health band maps to its phase, boundaries included")
    void phaseLadder(double ratio, int phase) {
        assertEquals(phase, JackStarBoss.phaseFor(ratio, false));
    }

    @Test
    @DisplayName("Kernel panic is the last phase whatever the health")
    void kernelPanicIsTheLastPhase() {
        assertEquals(5, JackStarBoss.phaseFor(1.0, true));
    }

    @Test
    @DisplayName("Every limb is the number of pieces its joints expect")
    void limbGroupsHaveTheirPieces() {
        Map<JackStarBoss.LimbGroup, Integer> counts = new EnumMap<>(JackStarBoss.LimbGroup.class);
        for (JackStarBoss.JackPart part : JackStarBoss.JackPart.values()) {
            counts.merge(part.group, 1, Integer::sum);
        }
        assertEquals(Map.of(
                JackStarBoss.LimbGroup.HEAD, 1, JackStarBoss.LimbGroup.TORSO_UPPER, 1,
                JackStarBoss.LimbGroup.TORSO_LOWER, 1, JackStarBoss.LimbGroup.ARM_RIGHT, 2,
                JackStarBoss.LimbGroup.ARM_LEFT, 2, JackStarBoss.LimbGroup.LEG_RIGHT, 2,
                JackStarBoss.LimbGroup.LEG_LEFT, 2), counts);
    }
}
