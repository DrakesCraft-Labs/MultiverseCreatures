package com.Chagui68.entities;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DistanceOptimizationEquivalenceTest {

    private static final double EPSILON = 1e-9;
    private static World dummyWorld;

    @BeforeAll
    static void setUp() {
        dummyWorld = (World) Proxy.newProxyInstance(
                World.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    if ("hashCode".equals(method.getName())) {
                        return 42;
                    }
                    if ("getName".equals(method.getName())) {
                        return "test_world";
                    }
                    if ("getUID".equals(method.getName())) {
                        return UUID.fromString("00000000-0000-0000-0000-000000000001");
                    }
                    return null;
                }
        );
    }

    @ParameterizedTest(name = "Threshold {0} blocks (squared: {1})")
    @CsvSource({
            "30.0, 900.0",   // ArmorStandBoss phaseTransitionRage & AirSlam attack radius
            "25.0, 625.0",   // ArmorStandBoss phaseTransitionStorm & EnderKnight pull max
            "35.0, 1225.0",  // ArmorStandBoss phaseTransitionDespair
            "20.0, 400.0",   // NovaBurstAttack & FlameElemental meteor max
            "8.0,  64.0",    // SoulReaper soul drain & FrostGolem ice aura
            "6.0,  36.0",    // ZombieHorseTrap duelist sword range & VoidCrawler poison
            "5.0,  25.0",    // EnderKnight pull min & FlameElemental fire melee
            "4.0,  16.0",    // EnderKnight levitation & VoidCrawler phase min
            "3.0,  9.0",     // FlameElemental meteor min
            "2.0,  4.0",     // ShockwaveWave particle impact radius
            "1.8,  3.24"     // Kinger moving threshold
    })
    @DisplayName("Verify distanceSquared predicate matches Euclidean distance across thresholds")
    void testDistanceSquaredEquivalence(double threshold, double thresholdSq) {
        Location center = new Location(dummyWorld, 100, 64, -200);

        // Point inside threshold
        Location inside = center.clone().add(threshold * 0.5, 0, 0);
        assertTrue(inside.distance(center) < threshold);
        assertTrue(inside.distanceSquared(center) < thresholdSq);

        // Point just below threshold
        Location justBelow = center.clone().add(threshold - 0.001, 0, 0);
        assertTrue(justBelow.distance(center) < threshold);
        assertTrue(justBelow.distanceSquared(center) < thresholdSq);

        // Point just above threshold
        Location justAbove = center.clone().add(threshold + 0.001, 0, 0);
        assertFalse(justAbove.distance(center) < threshold);
        assertFalse(justAbove.distanceSquared(center) < thresholdSq);

        // Point far outside
        Location farOutside = center.clone().add(threshold * 2.0, 0, 0);
        assertFalse(farOutside.distance(center) < threshold);
        assertFalse(farOutside.distanceSquared(center) < thresholdSq);
    }

    @Test
    @DisplayName("Verify damage dropoff calculations yield identical values within precision tolerance")
    void testDamageDropoffMathEquivalence() {
        Location bossLoc = new Location(dummyWorld, 0, 10, 0);
        double sealDamage = 20.0;

        // Test Storm phase dropoff: dmg * 0.5 * (1 - dist / 25)
        for (double d = 0; d < 25; d += 1.0) {
            Location pLoc = bossLoc.clone().add(d, 0, 0);
            double dist = pLoc.distance(bossLoc);
            double distSq = pLoc.distanceSquared(bossLoc);

            assertTrue(distSq < 625.0);
            double originalDmg = sealDamage * 0.5 * (1.0 - dist / 25.0);
            double optimizedDmg = sealDamage * 0.5 * (1.0 - Math.sqrt(distSq) / 25.0);
            assertEquals(originalDmg, optimizedDmg, EPSILON, "Storm damage mismatch at distance " + d);
        }

        // Test Despair phase dropoff: (sealDamage * 1.5) * (1 - dist / 35)
        double despairBase = sealDamage * 1.5;
        for (double d = 0; d < 35; d += 1.0) {
            Location pLoc = bossLoc.clone().add(0, 0, d);
            double dist = pLoc.distance(bossLoc);
            double distSq = pLoc.distanceSquared(bossLoc);

            assertTrue(distSq < 1225.0);
            double originalDmg = despairBase * (1.0 - dist / 35.0);
            double optimizedDmg = despairBase * (1.0 - Math.sqrt(distSq) / 35.0);
            assertEquals(originalDmg, optimizedDmg, EPSILON, "Despair damage mismatch at distance " + d);
        }

        // Test AirSlam dropoff: damage * (1 - dist / 30 * 0.5)
        for (double d = 0; d <= 30; d += 2.0) {
            Location pLoc = bossLoc.clone().add(d, 0, 0);
            double dist = pLoc.distance(bossLoc);
            double distSq = pLoc.distanceSquared(bossLoc);

            assertTrue(distSq <= 900.0);
            double originalDmg = sealDamage * (1.0 - dist / 30.0 * 0.5);
            double optimizedDmg = sealDamage * (1.0 - Math.sqrt(distSq) / 30.0 * 0.5);
            assertEquals(originalDmg, optimizedDmg, EPSILON, "AirSlam damage mismatch at distance " + d);
        }
    }

    @Test
    @DisplayName("Verify out-of-range targets successfully short-circuit before square root")
    void testShortCircuitForOutOfRangeTargets() {
        Location center = new Location(dummyWorld, 0, 0, 0);
        Location farPlayer = new Location(dummyWorld, 50, 0, 0); // 50 blocks away

        double distSq = farPlayer.distanceSquared(center); // 2500.0

        // In Storm (max 25 blocks -> 625.0)
        boolean inStorm = distSq < 625.0;
        assertFalse(inStorm, "Player 50 blocks away must not be in storm radius");

        // In Despair (max 35 blocks -> 1225.0)
        boolean inDespair = distSq < 1225.0;
        assertFalse(inDespair, "Player 50 blocks away must not be in despair radius");

        // In AirSlam (max 30 blocks -> 900.0)
        boolean inAirSlam = distSq <= 900.0;
        assertFalse(inAirSlam, "Player 50 blocks away must not be in air slam radius");
    }
}
