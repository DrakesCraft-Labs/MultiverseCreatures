package com.Chagui68.entities.boss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The Sentinel's penetrating hits bypass armour outright, but Resistance is only partially pierced:
 * {@code penetrating-resistance-pierce} (default 0.2) is the share of the potion's mitigation the
 * boss ignores, so the potion keeps protecting with the rest.
 *
 * <p>The maths lives in static helpers because the damage path itself needs a live server; the
 * plugin cancels the original event and re-applies {@code OUT_OF_WORLD} damage, which the engine
 * does <em>not</em> reduce by Resistance, so these helpers are the only place the potion is
 * accounted for — hence the exact expectations below.
 */
class PenetratingDamageTest {

    private static final double EPS = 1e-9;

    @Test
    @DisplayName("Default penetration is 20% of the Resistance reduction")
    void defaultPierceIsTwentyPercent() {
        assertEquals(0.2, ArmorStandBoss.RESISTANCE_PIERCE, EPS);
    }

    @Test
    @DisplayName("Armour, Protection and Resistance reductions are credited back")
    void mitigationsAreCreditedBack() {
        // A 22-damage cleave after full netherite + Resistance I: armour -17.6, resistance -0.88.
        assertEquals(22.0, ArmorStandBoss.unmitigated(3.52, -17.6, 0.0, -0.88), 1e-9);
        // No mitigation at all leaves the event damage untouched.
        assertEquals(15.0, ArmorStandBoss.unmitigated(15.0, 0.0, 0.0, 0.0), EPS);
        // Shield blocking is NOT credited back: it is part of the damage the player kept.
        assertEquals(2.2, ArmorStandBoss.unmitigated(2.2), EPS);
    }

    @Test
    @DisplayName("A fully absorbed hit never turns into negative damage")
    void creditedBackDamageNeverGoesNegative() {
        assertEquals(0.0, ArmorStandBoss.unmitigated(2.0, 5.0), EPS, "clamped at zero");
        assertEquals(0.0, ArmorStandBoss.unmitigated(0.0), EPS);
    }

    @Test
    @DisplayName("Resistance mitigates 20% per level and caps at 100%")
    void mitigationFollowsVanilla() {
        assertEquals(0.0, ArmorStandBoss.resistanceMitigation(-1), EPS, "no effect, no mitigation");
        assertEquals(0.2, ArmorStandBoss.resistanceMitigation(0), EPS, "Resistance I");
        assertEquals(0.4, ArmorStandBoss.resistanceMitigation(1), EPS, "Resistance II");
        assertEquals(0.6, ArmorStandBoss.resistanceMitigation(2), EPS, "Resistance III");
        assertEquals(0.8, ArmorStandBoss.resistanceMitigation(3), EPS, "Resistance IV");
        assertEquals(1.0, ArmorStandBoss.resistanceMitigation(4), EPS, "Resistance V is immunity");
        assertEquals(1.0, ArmorStandBoss.resistanceMitigation(12), EPS, "still capped");
    }

    @Test
    @DisplayName("Without Resistance the hit lands at full strength whatever the pierce is")
    void noResistanceMeansNoChange() {
        for (double pierce : new double[]{0.0, 0.2, 0.5, 1.0}) {
            assertEquals(10.0, ArmorStandBoss.penetratingDamage(10.0, -1, pierce), EPS,
                    "pierce " + pierce + " must not change an unmitigated hit");
        }
    }

    @Test
    @DisplayName("With the default pierce the potion still blocks 80% of its reduction")
    void defaultPierceKeepsMostOfTheProtection() {
        // Resistance I: 20% reduction, 20% of it ignored -> 16% blocked, 8.4 damage from a 10 hit.
        assertEquals(8.4, ArmorStandBoss.penetratingDamage(10.0, 0, 0.2), EPS);
        // Resistance II: 40% reduction -> 32% blocked, 6.8 damage.
        assertEquals(6.8, ArmorStandBoss.penetratingDamage(10.0, 1, 0.2), EPS);
        // Resistance V: 100% reduction -> 80% blocked, 2.0 damage.
        assertEquals(2.0, ArmorStandBoss.penetratingDamage(10.0, 4, 0.2), EPS);
    }

    @Test
    @DisplayName("Pierce 0 respects Resistance fully, pierce 1 ignores it entirely")
    void pierceExtremesBracketThePotion() {
        assertEquals(8.0, ArmorStandBoss.penetratingDamage(10.0, 0, 0.0), EPS);
        assertEquals(6.0, ArmorStandBoss.penetratingDamage(10.0, 1, 0.0), EPS);
        assertEquals(10.0, ArmorStandBoss.penetratingDamage(10.0, 0, 1.0), EPS);
        assertEquals(10.0, ArmorStandBoss.penetratingDamage(10.0, 4, 1.0), EPS);
    }

    @Test
    @DisplayName("Out-of-range pierce values are clamped instead of scaling damage past its bounds")
    void pierceIsClamped() {
        assertEquals(ArmorStandBoss.penetratingDamage(10.0, 1, 0.0),
                ArmorStandBoss.penetratingDamage(10.0, 1, -5.0), EPS);
        assertEquals(ArmorStandBoss.penetratingDamage(10.0, 1, 1.0),
                ArmorStandBoss.penetratingDamage(10.0, 1, 3.0), EPS);
    }

    @Test
    @DisplayName("A penetrating hit never heals damage back: it stays inside (mitigated, raw]")
    void damageStaysInsideItsBounds() {
        for (int amplifier = -1; amplifier <= 6; amplifier++) {
            for (double pierce = -1.0; pierce <= 2.0; pierce += 0.05) {
                double dealt = ArmorStandBoss.penetratingDamage(12.5, amplifier, pierce);
                assertTrue(dealt <= 12.5 + EPS, "hit grew to " + dealt + " (amp " + amplifier + ")");
                assertTrue(dealt >= 12.5 * (1.0 - ArmorStandBoss.resistanceMitigation(amplifier)) - EPS,
                        "hit fell below the fully mitigatable floor: " + dealt);
            }
        }
    }

}
