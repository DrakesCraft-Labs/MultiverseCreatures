package com.Chagui68.entities.boss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Nix must never lose more than {@code entities.nix-executioner.max-damage-per-hit} per hit, so a
 * single burst bug (or a stacked damage source) cannot delete the boss. The cap lives in
 * {@link NixBoss#capIncomingDamage(double, double)} because the real damage path needs a live
 * server to run.
 */
class NixDamageCapTest {

    @Test
    @DisplayName("Default cap is 100 damage per hit")
    void defaultCapIsOneHundred() {
        assertEquals(100.0, NixBoss.DEFAULT_MAX_DAMAGE_PER_HIT);
    }

    @Test
    @DisplayName("Hits above the cap are clamped, hits below it pass through untouched")
    void capClampsOnlyAbove() {
        assertEquals(100.0, NixBoss.capIncomingDamage(250.0, 100.0));
        assertEquals(100.0, NixBoss.capIncomingDamage(101.0, 100.0));
        assertEquals(100.0, NixBoss.capIncomingDamage(100.0, 100.0));
        assertEquals(64.5, NixBoss.capIncomingDamage(64.5, 100.0));
        assertEquals(1.0, NixBoss.capIncomingDamage(1.0, 100.0));
    }

    @Test
    @DisplayName("A hit is never inflated by the cap")
    void capNeverIncreasesDamage() {
        for (double hit : new double[]{0.0, 0.5, 7.0, 99.999}) {
            assertEquals(hit, NixBoss.capIncomingDamage(hit, 100.0));
        }
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, -100.0})
    @DisplayName("A non-positive cap disables the limit (legacy behaviour)")
    void nonPositiveCapDisablesTheLimit(double disabled) {
        assertEquals(500.0, NixBoss.capIncomingDamage(500.0, disabled));
    }

    @Test
    @DisplayName("The clamped hit is what actually leaves the health pool")
    void clampedHitDrainsThePools() {
        double health = 450.0;
        double afterOneShot = Math.max(0.0, health - NixBoss.capIncomingDamage(10_000.0, 100.0));
        assertEquals(350.0, afterOneShot);
        // Four capped hits still leave the boss alive; the fifth finishes it.
        assertEquals(50.0, Math.max(0.0, afterOneShot - 3 * NixBoss.capIncomingDamage(1_000.0, 100.0)));
        assertEquals(0.0, Math.max(0.0, 50.0 - NixBoss.capIncomingDamage(1_000.0, 100.0)));
    }
}
