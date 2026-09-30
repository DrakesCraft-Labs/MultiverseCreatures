package com.Chagui68.entities.boss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The maths behind "can this boss be damaged": every hit resolves through
 * {@link JackResilience#resolve}, so the only way JackStar takes nothing is an explicit dodge.
 */
class JackResilienceTest {

    private static final double EPSILON = 1.0e-9;

    @Test
    @DisplayName("A solo fight is not split at all")
    void soloFightKeepsTheWholeHit() {
        JackResilience.Split split = JackResilience.splitIncoming(20.0, 1);
        assertEquals(20.0, split.toBoss(), EPSILON);
        assertEquals(0.0, split.sharedTotal(), EPSILON);
        assertEquals(0.0, split.perPartyMember(), EPSILON);
    }

    @Test
    @DisplayName("The load balancer keeps 65% on Jack and shares 35% with the party")
    void partySharesThirtyFivePercent() {
        JackResilience.Split split = JackResilience.splitIncoming(100.0, 4);
        assertEquals(65.0, split.toBoss(), EPSILON);
        assertEquals(35.0, split.sharedTotal(), EPSILON);
        assertEquals(8.75, split.perPartyMember(), EPSILON);
    }

    @Test
    @DisplayName("A split never deletes or duplicates damage")
    void splitConservesTheHit() {
        for (double incoming : new double[]{1.0, 7.5, 23.25, 100.0, 500.0}) {
            for (int partySize : new int[]{1, 2, 3, 8, 40}) {
                JackResilience.Split split = JackResilience.splitIncoming(incoming, partySize);
                assertEquals(incoming, split.toBoss() + split.sharedTotal(), EPSILON,
                        "hit of " + incoming + " with a party of " + partySize);
                assertEquals(split.sharedTotal(), split.perPartyMember() * partySize, 1.0e-6,
                        "the party share must be split among its members");
            }
        }
    }

    @Test
    @DisplayName("A landed hit always reaches JackStar, whatever the party size")
    void landedHitsAlwaysReachTheBoss() {
        for (double incoming : new double[]{1.0, 4.0, 19.9, 120.0}) {
            for (int partySize : new int[]{1, 2, 5, 30}) {
                JackResilience.Resolution hit = JackResilience.resolve(incoming, 0.99, 0.22, partySize);
                assertFalse(hit.dodged(), "a roll above the dodge chance cannot be a dodge");
                assertTrue(hit.toBoss() > 0.0,
                        "JackStar must lose health on a landed hit (incoming " + incoming + ", party " + partySize + ")");
            }
        }
    }

    @Test
    @DisplayName("A dodge, and only a dodge, leaves the boss untouched")
    void dodgeTakesNothing() {
        JackResilience.Resolution hit = JackResilience.resolve(40.0, 0.05, 0.22, 3);
        assertTrue(hit.dodged());
        assertEquals(0.0, hit.toBoss(), EPSILON);
        assertEquals(0.0, hit.split().sharedTotal(), EPSILON);
        assertEquals(0.0, hit.split().perPartyMember(), EPSILON);
    }

    @Test
    @DisplayName("A roll exactly on the dodge chance still lands")
    void dodgeIsStrictlyLessThan() {
        JackResilience.Resolution hit = JackResilience.resolve(10.0, 0.22, 0.22, 1);
        assertFalse(hit.dodged());
        assertEquals(10.0, hit.toBoss(), EPSILON);
    }

    @Test
    @DisplayName("The demoted (micro) form dodges and drops packets far more often")
    void demotedFormIsSlippery() {
        assertEquals(0.45, JackResilience.effectiveChance(0.22, 0.6f), EPSILON);
        assertEquals(0.45, JackResilience.effectiveChance(0.22, JackResilience.DEMOTED_SCALE - 0.01f), EPSILON);
        assertEquals(0.22, JackResilience.effectiveChance(0.22, JackResilience.DEMOTED_SCALE), EPSILON);
        assertEquals(0.22, JackResilience.effectiveChance(0.22, 1.0f), EPSILON);
    }
}
