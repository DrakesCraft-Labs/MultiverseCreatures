package com.Chagui68.entities.boss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The {@link PenetratingHit} snapshot is what {@code /msc debug} prints, so it has to reproduce the
 * exact pipeline the damage handler runs: credit the armour/Protection/Resistance modifiers back,
 * cap, then pierce Resistance. If this drifts from {@code onBossAttacksPlayer} the command would
 * confidently report numbers that never reached the player.
 */
class PenetratingHitTest {

    private static final double EPS = 1e-9;

    @Test
    @DisplayName("A full-netherite cleave is restored, capped and pierced exactly like the handler")
    void reproducesTheLivePipeline() {
        // Event as the engine reports it for a fully armoured player with Resistance I.
        PenetratingHit hit = PenetratingHit.of(3.52, -17.6, 0.0, -0.88, 0, 0.2, 15.0, 1_000L);

        assertEquals(3.52, hit.eventDamage(), EPS);
        assertEquals(-17.6, hit.armorCreditedBack(), EPS);
        assertEquals(0.0, hit.protectionCreditedBack(), EPS);
        assertEquals(-0.88, hit.resistanceCreditedBack(), EPS);
        assertEquals(22.0, hit.throughArmor(), EPS, "armour must be credited back before the cap");
        assertEquals(15.0, hit.cap(), EPS);
        assertEquals(15.0, hit.raw(), EPS, "the cap applies before Resistance");
        assertEquals(12.6, hit.dealt(), EPS, "Resistance I at 20% pierce: 15 * 0.84");
    }

    @Test
    @DisplayName("Without Resistance the hit is the capped damage, whatever the pierce")
    void noResistanceMeansNoPierceEffect() {
        for (double pierce : new double[]{0.0, 0.2, 1.0}) {
            PenetratingHit hit = PenetratingHit.of(20.0, 0.0, 0.0, 0.0, -1, pierce, 15.0, 0L);
            assertEquals(15.0, hit.dealt(), EPS, "pierce " + pierce + " changed an unmitigated hit");
            assertEquals(0, hit.resistanceLevel(), "no effect means level 0");
        }
    }

    @Test
    @DisplayName("A hit fully absorbed by armour leaves a zero raw value so the handler skips it")
    void absorbedHitHasNoRawDamage() {
        PenetratingHit hit = PenetratingHit.of(0.0, 0.0, 0.0, 0.0, 0, 0.2, 15.0, 0L);
        assertEquals(0.0, hit.throughArmor(), EPS);
        assertEquals(0.0, hit.raw(), EPS);
        assertEquals(0.0, hit.dealt(), EPS, "the report must never print NaN or a negative hit");
    }

    @Test
    @DisplayName("Resistance is reported as a player-facing level")
    void resistanceLevelIsOneBased() {
        assertEquals(0, PenetratingHit.of(10, 0, 0, 0, -1, 0.2, 15, 0L).resistanceLevel());
        assertEquals(1, PenetratingHit.of(10, 0, 0, 0, 0, 0.2, 15, 0L).resistanceLevel());
        assertEquals(3, PenetratingHit.of(10, 0, 0, 0, 2, 0.2, 15, 0L).resistanceLevel());
    }

    @Test
    @DisplayName("Age is elapsed time and never goes negative when the clock moves back")
    void ageIsClamped() {
        PenetratingHit hit = PenetratingHit.of(10, 0, 0, 0, 0, 0.2, 15, 5_000L);
        assertEquals(1_500L, hit.ageMillis(6_500L));
        assertEquals(0L, hit.ageMillis(5_000L));
        assertEquals(0L, hit.ageMillis(4_000L), "a backwards clock must not produce a negative age");
    }
}
