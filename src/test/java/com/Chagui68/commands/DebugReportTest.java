package com.Chagui68.commands;

import com.Chagui68.entities.boss.BossDamageSample;
import com.Chagui68.entities.boss.BossId;
import com.Chagui68.entities.boss.PenetratingHit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The rendered lines of {@code /msc debug}. Both helpers are pure, so the exact figures an admin
 * reads can be pinned down without a sender or a running server: a wrong number here would be a
 * confidently wrong diagnosis of a boss's damage.
 */
class DebugReportTest {

    @Test
    @DisplayName("A penetrating hit lists the event, the credited-back defences and the final value")
    void penetratingBlockListsTheWholePipeline() {
        PenetratingHit hit = PenetratingHit.of(3.52, -17.6, 0.0, -0.88, 0, 0.2, 15.0, 1_000L);

        assertEquals(List.of(
                "      &7Event &8: &f3.52 &8· &7armour &8: &f-17.60 &8· &7protection &8: &f0.00"
                        + " &8· &7resistance &8: &f-0.88",
                "      &7Through armour &8: &f22.00 &8· &7cap &8: &f15.00 &8· &7pierce &8: &f20%"
                        + " &8· &7Resistance &8: &flevel 1",
                "      &c&lFinal damage dealt &8: &c&l12.60"),
                CommandMenu.penetratingLines(hit));
    }

    @Test
    @DisplayName("A dealt sample shows the attack, what it asked for and what the player took")
    void dealtSampleShowsIntendedVersusTaken() {
        BossDamageSample sample = BossDamageSample.dealt(BossId.NIX, "Guillotine Cleave", 22.0, 9.9,
                1_000L);

        assertEquals(List.of(
                "   &e▸ &cDEALT to player &8· &7Guillotine Cleave &8· &72.5s ago",
                "      &7Intended &8: &f22.00 &8· &7Applied &8: &f9.90"),
                CommandMenu.sampleLines(sample, 3_500L));
    }

    @Test
    @DisplayName("A taken sample spells out the cap or split between the hit and what it cost")
    void takenSampleShowsTheMechanic() {
        BossDamageSample sample = BossDamageSample.taken(BossId.JACK_STAR, "Incoming hit", 134.0, 87.1,
                "load balancer: 46.9 shared", 0L);

        assertEquals(List.of(
                "   &e▸ &aTAKEN from player &8· &7Incoming hit &8· &71.5s ago",
                "      &7Hit &8: &f134.00 &8· &7load balancer: 46.9 shared &8· &7Applied &8: &f87.10"),
                CommandMenu.sampleLines(sample, 1_500L));
    }

    @Test
    @DisplayName("A taken sample with no mechanic note skips the separator instead of printing a blank")
    void takenSampleOmitsAnEmptyNote() {
        BossDamageSample sample = BossDamageSample.taken(BossId.SENTINEL, "Incoming hit", 12.0, 12.0, "",
                0L);

        List<String> lines = CommandMenu.sampleLines(sample, 0L);
        assertEquals("      &7Hit &8: &f12.00 &8· &7Applied &8: &f12.00", lines.get(1));
        assertFalse(lines.get(1).contains("·  &8·"), "an empty note must not leave a dangling separator");
    }
}
