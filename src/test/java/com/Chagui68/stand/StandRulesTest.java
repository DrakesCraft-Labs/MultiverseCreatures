package com.Chagui68.stand;

import org.bukkit.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The rules of the Stand Arrow arc that decide life and death: what the Arrow does to whoever it
 * pierces, which Stand awakens, how Sheer Heart Attack finds its way and when the sun burns.
 */
class StandRulesTest {

    @Test
    @DisplayName("A bearer is never killed and always awakens; anybody else dies 70% of the time")
    void arrowOutcomes() {
        for (double roll : new double[]{0.0, 0.5, 0.99}) {
            assertEquals(StandArrowRoll.Outcome.STAND, StandArrowRoll.decide(roll, 0.7, true, false, false),
                    "DIO's blood makes the Arrow always awaken a Stand");
            assertEquals(StandArrowRoll.Outcome.STAND, StandArrowRoll.decide(roll, 0.7, true, true, false),
                    "a bearer is never treated as unworthy");
            assertEquals(StandArrowRoll.Outcome.REJECTED, StandArrowRoll.decide(roll, 0.7, false, true, false),
                    "an unworthy player gets nothing until they drink DIO's blood");
            assertEquals(StandArrowRoll.Outcome.RESONATE, StandArrowRoll.decide(roll, 0.7, true, false, true),
                    "a Stand user is never chosen (nor killed) again");
        }
        assertEquals(StandArrowRoll.Outcome.DEATH, StandArrowRoll.decide(0.0, 0.7, false, false, false));
        assertEquals(StandArrowRoll.Outcome.DEATH, StandArrowRoll.decide(0.69, 0.7, false, false, false));
        assertEquals(StandArrowRoll.Outcome.STAND, StandArrowRoll.decide(0.7, 0.7, false, false, false),
                "a non bearer who survives awakens a Stand");

        int deaths = 0;
        int runs = 100_000;
        for (int i = 0; i < runs; i++) {
            if (StandArrowRoll.decide((i + 0.5) / runs, 0.7, false, false, false) == StandArrowRoll.Outcome.DEATH) {
                deaths++;
            }
        }
        assertEquals(0.7, deaths / (double) runs, 1e-3);
    }

    @Test
    @DisplayName("Stands are rolled by weight and a zero weight is never rolled")
    void standWeights() {
        Map<StandType, Integer> weights = new EnumMap<>(StandType.class);
        for (StandType type : StandType.values()) {
            weights.put(type, type.defaultWeight());
        }
        int total = weights.values().stream().mapToInt(Integer::intValue).sum();
        Map<StandType, Integer> counts = new EnumMap<>(StandType.class);
        int runs = 100_000;
        for (int i = 0; i < runs; i++) {
            counts.merge(StandType.roll((i + 0.5) / runs, weights), 1, Integer::sum);
        }
        for (StandType type : StandType.values()) {
            assertEquals(type.defaultWeight() / (double) total, counts.getOrDefault(type, 0) / (double) runs, 2e-3,
                    type.name());
        }
        weights.put(StandType.THE_WORLD, 0);
        Set<StandType> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            seen.add(StandType.roll(i / 1000.0, weights));
        }
        assertFalse(seen.contains(StandType.THE_WORLD));
        Map<StandType, Integer> none = new EnumMap<>(StandType.class);
        assertNull(StandType.roll(0.5, none), "no weights, no Stand");
    }

    @Test
    @DisplayName("Stand names are understood however they are typed")
    void standKeys() {
        assertEquals(StandType.KILLER_QUEEN, StandType.byKey("killer-queen"));
        assertEquals(StandType.KILLER_QUEEN, StandType.byKey("Killer Queen"));
        assertEquals(StandType.MAGICIANS_RED, StandType.byKey("magician's red"));
        assertEquals(StandType.THE_WORLD, StandType.byKey("THE_WORLD"));
        assertNull(StandType.byKey("gold experience"));
        assertNull(StandType.byKey(null));
    }

    /** A flat floor at y = 64 (blocks at 63 and below are solid). */
    private static boolean floor(int x, int y, int z) {
        return y <= 63;
    }

    @Test
    @DisplayName("Sheer Heart Attack rolls along the ground and climbs single steps")
    void rollsOnTheGround() {
        SheerHeartAttackPath.Step step = SheerHeartAttackPath.step(0.5, 64, 0.5, 10.5, 64, 0.5, 0.2,
                StandRulesTest::floor);
        assertEquals(0.7, step.x(), 1e-9);
        assertEquals(64, step.y(), 1e-9);
        assertFalse(step.phasing());

        // A single block step up ahead: it climbs instead of phasing.
        SheerHeartAttackPath.Blocks stair = (x, y, z) -> y <= 63 || (x >= 1 && y == 64);
        SheerHeartAttackPath.Step up = SheerHeartAttackPath.step(0.9, 64, 0.5, 10.5, 65, 0.5, 0.2, stair);
        assertEquals(65, up.y(), 1e-9);
        assertFalse(up.phasing());
    }

    @Test
    @DisplayName("A wall it cannot climb does not stop it: it goes through")
    void phasesThroughWalls() {
        SheerHeartAttackPath.Blocks wall = (x, y, z) -> y <= 63 || (x >= 1 && x <= 3 && y <= 70);
        SheerHeartAttackPath.Step step = SheerHeartAttackPath.step(0.9, 64, 0.5, 10.5, 64, 0.5, 0.2, wall);
        assertTrue(step.phasing(), "a five block wall is phased through");
        assertTrue(step.x() > 0.9, "it keeps moving towards the target");

        double x = 0.5;
        double y = 64;
        for (int i = 0; i < 100 && x < 10; i++) {
            SheerHeartAttackPath.Step next = SheerHeartAttackPath.step(x, y, 0.5, 10.5, 64, 0.5, 0.2, wall);
            x = next.x();
            y = next.y();
        }
        assertTrue(x >= 10, "it came out on the other side");
        assertEquals(64, y, 1e-9, "and is rolling on the ground again");
    }

    @Test
    @DisplayName("It digs straight down or up to a target right above or below it")
    void reachesTargetsAboveAndBelow() {
        SheerHeartAttackPath.Step down = SheerHeartAttackPath.step(0.5, 64, 0.5, 0.5, 40, 0.5, 0.2,
                StandRulesTest::floor);
        assertTrue(down.phasing());
        assertEquals(63.8, down.y(), 1e-9);
        assertTrue(SheerHeartAttackPath.arrived(0.5, 64, 0.5, 0.9, 64, 0.5, 1.2));
        assertFalse(SheerHeartAttackPath.arrived(0.5, 64, 0.5, 5.5, 64, 0.5, 1.2));
    }

    @Test
    @DisplayName("Only open sky in an overworld day burns a vampire; rain protects when set to")
    void sunlight() {
        assertTrue(VampireManager.sunlit(World.Environment.NORMAL, true, true, 15, true));
        assertFalse(VampireManager.sunlit(World.Environment.NORMAL, false, true, 15, true), "night");
        assertFalse(VampireManager.sunlit(World.Environment.NORMAL, true, true, 14, true), "under a roof");
        assertFalse(VampireManager.sunlit(World.Environment.NETHER, true, true, 15, true), "no sun in the Nether");
        assertFalse(VampireManager.sunlit(World.Environment.NORMAL, true, false, 15, true), "rain clouds");
        assertTrue(VampireManager.sunlit(World.Environment.NORMAL, true, false, 15, false), "rain set not to protect");
    }
}
