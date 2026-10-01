package com.Chagui68.entities;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** When and where Kinger's melee swing lands. */
class KingerMeleeTest {

    @Test
    @DisplayName("The hit lands at the peak of the swing, not on the tick it starts")
    void hitLandsAtThePeak() {
        int total = 12;
        assertFalse(Kinger.isMeleeImpactTick(12, total), "the arms have not moved yet");
        assertFalse(Kinger.isMeleeImpactTick(7, total));
        assertTrue(Kinger.isMeleeImpactTick(6, total), "sin(progress * PI) peaks halfway");
        assertTrue(Kinger.isMeleeImpactTick(1, 1), "a one-tick swing hits at once");
    }

    @Test
    @DisplayName("The swing reaches what is in front of him, not behind")
    void onlyTheFrontIsHit() {
        Vector facing = new Vector(0, 0, 1);
        assertTrue(Kinger.inMeleeArc(facing, new Vector(0, 0, 2), 3.5));
        assertTrue(Kinger.inMeleeArc(facing, new Vector(2, 0, 0.1), 3.5), "the flank is still in the arc");
        assertFalse(Kinger.inMeleeArc(facing, new Vector(0, 0, -2), 3.5), "nothing behind him is hit");
    }

    @Test
    @DisplayName("The reach is a sphere: the corners of the old cube are out of range")
    void reachIsASphere() {
        Vector facing = new Vector(0, 0, 1);
        assertFalse(Kinger.inMeleeArc(facing, new Vector(3.0, 0, 3.0), 3.5));
        assertTrue(Kinger.inMeleeArc(facing, new Vector(0, 0.2, 0.1), 3.5), "standing on top of him");
    }
}
