package com.Chagui68.entities.boss;

import com.Chagui68.entities.BossInstance.DefenseState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The Obsidian Sentinel's incoming-damage maths, extracted from its event handler.
 *
 * <p>This is the boss's balance: a hit can be halved by a shield seal, shaved by the healing circle,
 * reduced again by the active defence, soaked by the absorb shield and only then clamped to
 * {@code max-damage-per-hit}. None of it had a test while it lived inline in a 2 300-line class, so
 * the order and the edge cases are pinned here.
 */
class SentinelDefenseTest {

    private static final double EPS = 1e-9;
    /** High enough that only the defences under test move the number. */
    private static final double NO_CAP = 1000.0;

    /** No seal, no circle, no active defence. */
    private static SentinelDefense plain(double cap) {
        return new SentinelDefense(false, false, false, DefenseState.NONE, 0.0, cap);
    }

    @Test
    @DisplayName("With nothing up the hit lands whole, the trace stays empty and nothing is reflected")
    void noDefencesLeavesTheHitAlone() {
        SentinelDefense.Result hit = plain(NO_CAP).resolve(42.0);
        assertEquals(42.0, hit.applied(), EPS);
        assertEquals(0.0, hit.absorbed(), EPS);
        assertEquals(0.0, hit.reflected(), EPS);
        assertFalse(hit.shieldBroken());
        assertTrue(hit.steps().isEmpty());
    }

    @Test
    @DisplayName("The cap is applied last and only when the hit actually exceeds it")
    void capAppliesLast() {
        SentinelDefense.Result over = plain(50.0).resolve(120.0);
        assertEquals(50.0, over.applied(), EPS);
        assertEquals(List.of("cap 50.0"), over.steps());

        SentinelDefense.Result exactly = plain(50.0).resolve(50.0);
        assertEquals(50.0, exactly.applied(), EPS);
        assertTrue(exactly.steps().isEmpty(), "a hit equal to the cap must not be marked as capped");

        SentinelDefense.Result under = plain(50.0).resolve(30.0);
        assertEquals(30.0, under.applied(), EPS);
    }

    @Test
    @DisplayName("An invulnerable boss ignores the hit entirely, without even a cap step")
    void invulnerableZeroesTheHit() {
        SentinelDefense everythingOn =
                new SentinelDefense(true, true, true, DefenseState.STONE_SKIN, 100.0, 10.0);
        SentinelDefense.Result hit = everythingOn.resolve(500.0);
        assertEquals(0.0, hit.applied(), EPS);
        assertEquals(0.0, hit.reflected(), EPS);
        assertEquals(List.of("invulnerable"), hit.steps());
    }

    @Test
    @DisplayName("Shield seal halves, the healing circle shaves a fifth, and both stack")
    void sealAndCircleStack() {
        assertEquals(50.0, new SentinelDefense(false, true, false, DefenseState.NONE, 0.0, NO_CAP)
                .resolve(100.0).applied(), EPS);
        assertEquals(80.0, new SentinelDefense(false, false, true, DefenseState.NONE, 0.0, NO_CAP)
                .resolve(100.0).applied(), EPS);
        assertEquals(40.0, new SentinelDefense(false, true, true, DefenseState.NONE, 0.0, NO_CAP)
                .resolve(100.0).applied(), EPS, "100 * 0.5 * 0.8");
    }

    @Test
    @DisplayName("Stone skin halves the hit and the absorb shield spends its health")
    void activeDefencesApply() {
        assertEquals(50.0, new SentinelDefense(false, false, false, DefenseState.STONE_SKIN, 0.0, NO_CAP)
                .resolve(100.0).applied(), EPS);

        SentinelDefense.Result partly =
                new SentinelDefense(false, false, false, DefenseState.ABSORB_SHIELD, 20.0, NO_CAP)
                        .resolve(60.0);
        assertEquals(20.0, partly.absorbed(), EPS);
        assertEquals(40.0, partly.applied(), EPS, "20 soaked, 40 through");
        assertTrue(partly.shieldBroken(), "20 shield against 60 damage empties it");

        SentinelDefense.Result held =
                new SentinelDefense(false, false, false, DefenseState.ABSORB_SHIELD, 100.0, NO_CAP)
                        .resolve(60.0);
        assertEquals(60.0, held.absorbed(), EPS);
        assertEquals(0.0, held.applied(), EPS);
        assertFalse(held.shieldBroken(), "60 damage must not empty a 100-point shield");
    }

    @Test
    @DisplayName("The reflect barrier throws back 30% of the reduced hit, taken before the cap")
    void reflectBarrierReflectsFromTheReducedHit() {
        SentinelDefense.Result open = new SentinelDefense(false, false, false, DefenseState.REFLECT_BARRIER, 0.0, NO_CAP)
                .resolve(100.0);
        assertEquals(70.0, open.applied(), EPS);
        assertEquals(21.0, open.reflected(), EPS, "30% of the 70 that got through");

        // The cap must not shrink what the barrier returns: the reflection is read from the reduced
        // hit, which the old inline code also computed before clamping.
        SentinelDefense.Result capped = new SentinelDefense(false, false, false, DefenseState.REFLECT_BARRIER, 0.0, 40.0)
                .resolve(100.0);
        assertEquals(40.0, capped.applied(), EPS);
        assertEquals(21.0, capped.reflected(), EPS, "the cap applies to the boss, not to the reflection");
    }

    @Test
    @DisplayName("The absorb shield is spent before the cap, so the cap sees what got through")
    void absorbShieldIsSpentBeforeTheCap() {
        SentinelDefense.Result hit =
                new SentinelDefense(false, false, false, DefenseState.ABSORB_SHIELD, 20.0, 30.0).resolve(60.0);
        assertEquals(30.0, hit.applied(), EPS, "60 - 20 soaked = 40, then capped to 30");
        assertEquals(List.of("absorb shield -20.0", "cap 30.0"), hit.steps());
    }

    @Test
    @DisplayName("Every defence runs before the cap, so a 200 hit lands at the cap and not below it")
    void defencesAreAppliedBeforeTheCap() {
        SentinelDefense all = new SentinelDefense(false, true, true, DefenseState.STONE_SKIN, 0.0, 30.0);
        assertEquals(30.0, all.resolve(200.0).applied(), EPS, "200 * 0.5 * 0.8 * 0.5 = 40, capped to 30");
    }

    @Test
    @DisplayName("The trace reads exactly as the /msc debug note, in order")
    void traceWordingIsPinned() {
        SentinelDefense all = new SentinelDefense(false, true, true, DefenseState.STONE_SKIN, 0.0, 30.0);
        assertEquals(List.of("shield seal ×0.5", "healing circle ×0.8", "stone skin ×0.5", "cap 30.0"),
                all.resolve(200.0).steps());
    }

    @Test
    @DisplayName("A stand that is not a tracked boss keeps only the cap")
    void missingInstanceLeavesOnlyTheCap() {
        SentinelDefense none = SentinelDefense.from(null, 15.0);
        assertEquals(15.0, none.resolve(80.0).applied(), EPS);
        assertEquals(15.0, none.resolve(15.0).applied(), EPS);
        assertFalse(none.invulnerable());
        assertEquals(DefenseState.NONE, none.activeDefense());
    }

    @Test
    @DisplayName("No combination of defences ever enlarges or negates a hit into an impossible value")
    void damageNeverGrows() {
        DefenseState[] states = {DefenseState.NONE, DefenseState.STONE_SKIN, DefenseState.REFLECT_BARRIER,
                DefenseState.ABSORB_SHIELD};
        for (DefenseState state : states) {
            for (double incoming : new double[]{0.0, 1.0, 50.0, 500.0}) {
                for (boolean seal : new boolean[]{false, true}) {
                    SentinelDefense defense = new SentinelDefense(false, seal, true, state, 25.0, 40.0);
                    SentinelDefense.Result hit = defense.resolve(incoming);
                    assertTrue(hit.applied() <= incoming + EPS,
                            state + " grew " + incoming + " into " + hit.applied());
                    assertTrue(hit.applied() >= 0, "applied damage went negative: " + hit.applied());
                    assertTrue(hit.absorbed() >= 0, "absorbed damage went negative");
                    assertTrue(hit.reflected() >= 0, "reflected damage went negative");
                }
            }
        }
    }
}
