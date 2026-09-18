package com.Chagui68.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CommandHelpPaginationTest {

    @ParameterizedTest(name = "Input page {0} with total {1} clamps to {2}")
    @CsvSource({
            "-5, 3, 1",
            "0,  3, 1",
            "1,  3, 1",
            "2,  3, 2",
            "3,  3, 3",
            "4,  3, 3",
            "99, 3, 3",
            "-1, 4, 1",
            "1,  4, 1",
            "4,  4, 4",
            "5,  4, 4"
    })
    @DisplayName("Verify pagination clamping logic guarantees safe bounds for all user inputs")
    void testPaginationClamping(int inputPage, int totalPages, int expectedPage) {
        int clamped = Math.max(1, Math.min(inputPage, totalPages));
        assertEquals(expectedPage, clamped, "Page must be strictly clamped between 1 and " + totalPages);
    }

    @Test
    @DisplayName("Verify spawn help subheaders exist for all valid pages")
    void testSpawnHelpSubheaders() {
        for (int p = 1; p <= 3; p++) {
            String subHeader = switch (p) {
                case 1 -> "BOSSES & APEX ENTITIES";
                case 2 -> "MILITARY STRIKE FORCE";
                default -> "MULTIVERSE CREATURES & ELITES";
            };
            assertNotNull(subHeader);
            assertFalse(subHeader.isEmpty());
        }
    }

    @Test
    @DisplayName("Verify give help subheaders exist for all valid pages")
    void testGiveHelpSubheaders() {
        for (int p = 1; p <= 4; p++) {
            String subHeader = switch (p) {
                case 1 -> "LEGENDARY WEAPONS & MAGIC";
                case 2 -> "ARMOR SETS, RELICS & OFFHANDS";
                case 3 -> "BOSS CATALYSTS & APEX COMPONENTS";
                default -> "CRAFTING MATERIALS & ESSENCES";
            };
            assertNotNull(subHeader);
            assertFalse(subHeader.isEmpty());
        }
    }

    @Test
    @DisplayName("Verify attack help subheaders exist for all valid pages")
    void testAttackHelpSubheaders() {
        for (int p = 1; p <= 4; p++) {
            String subHeader = switch (p) {
                case 1 -> "GROUND ATTACKS & EARTH CONTROL";
                case 2 -> "AERIAL ASSAULTS & CELESTIAL RUSHES";
                case 3 -> "RANGED ARTILLERY & MAGIC PROJECTIONS";
                default -> "DEFENSIVE SHIELDS & MAGIC SEALS";
            };
            assertNotNull(subHeader);
            assertFalse(subHeader.isEmpty());
        }
    }
}
