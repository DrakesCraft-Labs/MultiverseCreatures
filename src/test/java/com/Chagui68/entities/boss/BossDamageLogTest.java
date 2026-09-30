package com.Chagui68.entities.boss;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The registry behind {@code /msc debug}. It keeps one sample per player, boss and direction, and
 * has to hand them back in a stable order so the report does not reshuffle between runs.
 */
class BossDamageLogTest {

    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

    @Test
    @DisplayName("An unknown player has no samples")
    void unknownPlayerIsEmpty() {
        BossDamageLog log = new BossDamageLog();
        assertTrue(log.samplesFor(PLAYER).isEmpty());
    }

    @Test
    @DisplayName("Samples come back ordered by boss and then by direction, regardless of insert order")
    void samplesAreOrdered() {
        BossDamageLog log = new BossDamageLog();
        BossDamageSample jackDealt =
                BossDamageSample.dealt(BossId.JACK_STAR, "Sigkill -9", 35.0, 20.0, 0L);
        BossDamageSample nixTaken =
                BossDamageSample.taken(BossId.NIX, "Incoming hit", 300.0, 100.0, "cap 100.0", 0L);
        BossDamageSample sentinelTaken =
                BossDamageSample.taken(BossId.SENTINEL, "Incoming hit", 44.0, 22.0, "stone skin ×0.5", 0L);
        BossDamageSample nixDealt =
                BossDamageSample.dealt(BossId.NIX, "Guillotine Cleave", 22.0, 9.9, 0L);

        // Inserted deliberately out of order.
        log.record(PLAYER, jackDealt);
        log.record(PLAYER, nixTaken);
        log.record(PLAYER, sentinelTaken);
        log.record(PLAYER, nixDealt);

        assertEquals(List.of(sentinelTaken, nixDealt, nixTaken, jackDealt), log.samplesFor(PLAYER));
    }

    @Test
    @DisplayName("Recording a new sample for the same slot replaces the previous one")
    void latestSampleWins() {
        BossDamageLog log = new BossDamageLog();
        log.record(PLAYER, BossDamageSample.dealt(BossId.NIX, "Guillotine Cleave", 22.0, 9.9, 1L));
        BossDamageSample newest =
                BossDamageSample.dealt(BossId.NIX, "Guillotine Cleave", 22.0, 3.3, 2L);
        log.record(PLAYER, newest);

        assertEquals(List.of(newest), log.samplesFor(PLAYER));
    }

    @Test
    @DisplayName("Players are tracked independently and forget() clears one of them")
    void playersAreIndependent() {
        BossDamageLog log = new BossDamageLog();
        UUID other = UUID.fromString("00000000-0000-0000-0000-0000000000bb");
        log.record(PLAYER, BossDamageSample.dealt(BossId.NIX, "Guillotine Cleave", 22.0, 9.9, 0L));
        log.record(other, BossDamageSample.dealt(BossId.NIX, "Guillotine Cleave", 22.0, 9.9, 0L));

        log.forget(PLAYER);
        assertTrue(log.samplesFor(PLAYER).isEmpty());
        assertEquals(1, log.samplesFor(other).size(), "forgetting one player must not touch another");
    }

    @Test
    @DisplayName("Null records and negative ages are tolerated instead of throwing")
    void nullsAreIgnored() {
        BossDamageLog log = new BossDamageLog();
        log.record(null, BossDamageSample.dealt(BossId.NIX, "Guillotine Cleave", 1.0, 1.0, 0L));
        log.record(PLAYER, null);
        assertTrue(log.samplesFor(PLAYER).isEmpty());

        BossDamageSample sample = BossDamageSample.dealt(BossId.NIX, "Guillotine Cleave", 1.0, 1.0, 5_000L);
        assertEquals(0L, sample.ageMillis(4_000L), "a backwards clock must not produce a negative age");
    }

    @Test
    @DisplayName("forgetBoss drops that boss for every player and leaves the other bosses alone")
    void forgetBossIsScopedToTheBoss() {
        BossDamageLog log = new BossDamageLog();
        UUID other = UUID.fromString("00000000-0000-0000-0000-0000000000bb");
        BossDamageSample sentinel =
                BossDamageSample.taken(BossId.SENTINEL, "Incoming hit", 40.0, 20.0, "stone skin ×0.5", 0L);
        BossDamageSample nix =
                BossDamageSample.dealt(BossId.NIX, "Guillotine Cleave", 22.0, 9.9, 0L);
        log.record(PLAYER, sentinel);
        log.record(PLAYER, nix);
        log.record(other, sentinel);

        log.forgetBoss(BossId.SENTINEL);

        assertEquals(List.of(nix), log.samplesFor(PLAYER), "only the Sentinel sample must go");
        assertTrue(log.samplesFor(other).isEmpty(), "the other player's Sentinel sample must go too");
    }

    @Test
    @DisplayName("forgetBoss(null) is a no-op instead of throwing")
    void forgetBossToleratesNull() {
        BossDamageLog log = new BossDamageLog();
        log.record(PLAYER, BossDamageSample.dealt(BossId.NIX, "Guillotine Cleave", 22.0, 9.9, 0L));
        log.forgetBoss(null);
        assertEquals(1, log.samplesFor(PLAYER).size());
    }
}
