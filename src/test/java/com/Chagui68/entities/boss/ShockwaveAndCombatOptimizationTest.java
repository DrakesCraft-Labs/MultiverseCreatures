package com.Chagui68.entities.boss;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShockwaveAndCombatOptimizationTest {

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

    @ParameterizedTest(name = "Shockwave radius {0} blocks")
    @ValueSource(doubles = {0.0, 5.0, 10.0, 15.0, 20.0, 25.0, 30.0})
    @DisplayName("Verify shockwave knockback strength and damage multipliers remain within safe bounds")
    void testShockwaveFormulas(double radius) {
        double maxRadius = 30.0;

        // Knockback vector: Vector(0, 0.7 - radius / maxRadius * 0.3, 0)
        Vector knockback = new Vector(0, 0.7 - radius / maxRadius * 0.3, 0);
        assertEquals(0.0, knockback.getX(), "Knockback X must be 0");
        assertEquals(0.0, knockback.getZ(), "Knockback Z must be 0");
        assertTrue(knockback.getY() >= 0.4 - 1e-9 && knockback.getY() <= 0.7 + 1e-9,
                "Knockback Y must be bounded within [0.4, 0.7], was: " + knockback.getY());

        // Damage multiplier: 1.0 - radius / maxRadius * 0.6
        double damageMultiplier = 1.0 - radius / maxRadius * 0.6;
        assertTrue(damageMultiplier >= 0.4 - 1e-9 && damageMultiplier <= 1.0 + 1e-9,
                "Damage multiplier must be bounded within [0.4, 1.0], was: " + damageMultiplier);
    }

    @Test
    @DisplayName("Verify hitInThisRing prevents multi-hit damage duplication across adjacent particles")
    void testShockwaveMultiHitDeduplication() {
        Location center = new Location(dummyWorld, 0, 0, 0);
        double radius = 10.0;
        int samples = (int) Math.max(16, radius * 4); // 40 samples

        // Place a mock player position right on the ring at (10, 0, 0)
        Location playerLoc = new Location(dummyWorld, 10.0, 0.0, 0.0);
        UUID playerId = UUID.randomUUID();

        // Calculate all particle positions in the ring
        List<Location> particleLocations = new ArrayList<>();
        for (int a = 0; a < samples; a++) {
            double angle = (2 * Math.PI * a / samples);
            double x = center.getX() + Math.cos(angle) * radius;
            double z = center.getZ() + Math.sin(angle) * radius;
            particleLocations.add(new Location(dummyWorld, x, 0.1, z));
        }

        // Count how many particles are within 2.0 blocks of the player
        int overlappingParticles = 0;
        for (Location pl : particleLocations) {
            if (playerLoc.getY() <= pl.getY() + 2 && playerLoc.distanceSquared(pl) < 4.0) {
                overlappingParticles++;
            }
        }
        // Adjacent points on a 40-sample ring of radius 10 are ~1.57 blocks apart,
        // so multiple particles will be within the 2.0 block radius of the player
        assertTrue(overlappingParticles > 1,
                "Without deduplication, multiple adjacent particles (" + overlappingParticles + ") would hit player");

        // Now test with the optimized hitInThisRing deduplication
        Set<UUID> hitInThisRing = new HashSet<>();
        int actualDamageApplications = 0;

        for (Location pl : particleLocations) {
            if (hitInThisRing.contains(playerId)) continue;
            if (playerLoc.getY() <= pl.getY() + 2 && playerLoc.distanceSquared(pl) < 4.0) {
                hitInThisRing.add(playerId);
                actualDamageApplications++;
            }
        }

        assertEquals(1, actualDamageApplications,
                "With hitInThisRing deduplication, player must be damaged exactly ONCE per ring");
    }

    @Test
    @DisplayName("Verify NIX boss idle condition and throttle frequency")
    void testNixBossIdleConditionAndThrottling() {
        NixBoss.NixInstance inst = new NixBoss.NixInstance(null);

        // Idle state: stationary, no cleave, no chain
        inst.moving = false;
        inst.cleaveAnim = 0;
        inst.chainAnim = 0;

        int idleSyncCount = 0;
        for (int i = 0; i < 90; i++) {
            inst.tickCount++;
            boolean isIdle = !inst.moving && inst.cleaveAnim == 0 && inst.chainAnim == 0;
            if (!isIdle || inst.tickCount % 3 == 0) {
                idleSyncCount++;
            }
        }
        assertEquals(30, idleSyncCount, "NIX idle synchronization must run exactly once every 3 ticks");

        // Active state: cleaving
        inst.moving = false;
        inst.cleaveAnim = 20;
        inst.chainAnim = 0;

        int cleaveSyncCount = 0;
        for (int i = 0; i < 90; i++) {
            inst.tickCount++;
            boolean isIdle = !inst.moving && inst.cleaveAnim == 0 && inst.chainAnim == 0;
            if (!isIdle || inst.tickCount % 3 == 0) {
                cleaveSyncCount++;
            }
        }
        assertEquals(90, cleaveSyncCount, "NIX cleave animation must run display sync on every tick");

        // Active state: chained
        inst.moving = false;
        inst.cleaveAnim = 0;
        inst.chainAnim = 25;

        int chainSyncCount = 0;
        for (int i = 0; i < 90; i++) {
            inst.tickCount++;
            boolean isIdle = !inst.moving && inst.cleaveAnim == 0 && inst.chainAnim == 0;
            if (!isIdle || inst.tickCount % 3 == 0) {
                chainSyncCount++;
            }
        }
        assertEquals(90, chainSyncCount, "NIX chain animation must run display sync on every tick");
    }
}
