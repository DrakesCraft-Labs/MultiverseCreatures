package com.Chagui68.entities.boss;

import com.Chagui68.utils.MscEntityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the numbers the Obsidian Sentinel is built from.
 *
 * <p>The Sentinel is not a suit worn by an invisible stand: the visible, scaled armour stand is the
 * boss, so one number — its scale — is at once the size of the model players see and the box they
 * hit. It used to be a bare {@code 7.5} written inside {@code trySpawn}, the kind of number that is
 * edited once and silently changes the fight for good, and the two pentagram values were literals in
 * the same call. They now have names, the scale sits behind a clamped config knob, and this guard
 * proves the code, the constant and the shipped config still agree.
 *
 * <p>Reading the source is deliberate: there is no server on the test classpath, so "the stand the
 * fight is hit through is set up in exactly one place" can only be checked by reading it.
 */
class SentinelHitboxTest {

    /** The vanilla armour stand's own box, before the scale attribute multiplies it. */
    private static final double STAND_WIDTH = 0.5;
    private static final double STAND_HEIGHT = 1.975;

    private static final Path SOURCE = Path.of("src", "main", "java", "com", "Chagui68",
            "entities", "boss", "ArmorStandBoss.java");

    @Test
    @DisplayName("The Sentinel's scale is one named number the fight was tuned around")
    void theScaleIsOneNamedNumber() throws Exception {
        assertEquals(7.5, (double) constant("MODEL_HITBOX_SCALE"), 1.0e-9,
                "the Sentinel's scale is the fight's baseline; moving it moves the boss's whole reach");

        // The shipped number has to survive the clamp untouched, or an untouched server would play a
        // different fight from the one the constant documents.
        assertEquals(ArmorStandBoss.MODEL_HITBOX_SCALE,
                MscEntityUtils.clampHitboxScale(ArmorStandBoss.MODEL_HITBOX_SCALE), 1.0e-9,
                "the shipped scale falls outside the range the code clamps to");

        double hitbox = ArmorStandBoss.MODEL_HITBOX_SCALE * STAND_HEIGHT;
        assertTrue(hitbox > 10.0,
                "the Sentinel must stay a giant to be the boss it draws, its hitbox is only " + hitbox + " tall");
        assertTrue(hitbox <= MscEntityUtils.MAX_HITBOX_SCALE * STAND_HEIGHT,
                "a config file must not be able to grow the boss past the largest scale the code allows");
        assertTrue(ArmorStandBoss.MODEL_HITBOX_SCALE * STAND_WIDTH > 1.0,
                "the Sentinel must be wider than the axe swing players hit it with");
    }

    @Test
    @DisplayName("The scale comes from the clamped config knob, never from a literal")
    void theScaleComesFromTheClampedKnob() throws IOException {
        String source = source();

        assertTrue(source.contains("\"entities.armor-stand-boss.hitbox-scale\""),
                "the scale must be readable from config.yml like the other bosses' scales");
        assertTrue(source.contains("clampHitboxScale("),
                "a hand-edited config file must be clamped before it reaches the attribute");
        assertTrue(source.contains("setBaseValue(hitboxScale)"),
                "trySpawn must set the attribute from the loaded field, not a number");
        assertFalse(source.contains("setBaseValue(7.5)"),
                "the scale went back to being a literal inside trySpawn");
    }

    @Test
    @DisplayName("The arrival pentagram numbers are named, not buried in the spawn call")
    void theArrivalSealNumbersAreNamed() throws Exception {
        assertEquals(80, (int) constant("SPAWN_SEAL_TICKS"), "the seal's duration is part of the arrival's timing");
        assertEquals(12.0, (double) constant("SPAWN_SEAL_RADIUS"), 1.0e-9, "the seal's radius is part of the arena");

        String source = source();
        assertTrue(source.contains("spawnLargePentagramSeal("),
                "the Sentinel still draws its arrival seal");
        assertTrue(source.contains("SPAWN_SEAL_TICKS,") && source.contains("SPAWN_SEAL_RADIUS,"),
                "the arrival call must pass the named constants instead of repeating the numbers");
    }

    @Test
    @DisplayName("The stand the fight is hit through is configured in exactly one place")
    void theStandIsConfiguredOnce() throws IOException {
        String source = source();

        // Every one of these decides what the boss's body is. A second spawn path that forgot one of
        // them would produce a boss the fight cannot be won against, so there must be only one.
        for (String setup : List.of("setArms(true)", "setBasePlate(false)", "setGravity(false)",
                "setCustomNameVisible(true)", "setInvulnerable(false)", "setPersistent(true)",
                "addScoreboardTag(TAG)")) {
            assertEquals(1, count(source, setup),
                    setup + " must be set exactly once, in the one place that builds the boss");
        }
    }

    // --- helpers -----------------------------------------------------------------------------------

    /** Reads a private constant by name so the test pins the value, not the spelling of a literal. */
    private static Object constant(String name) throws Exception {
        Field field = ArmorStandBoss.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private static String source() {
        try {
            return Files.readString(SOURCE).replace("\r\n", "\n");
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + SOURCE, e);
        }
    }

    private static int count(String source, String token) {
        int found = 0;
        int index = 0;
        while ((index = source.indexOf(token, index)) >= 0) {
            found++;
            index += token.length();
        }
        return found;
    }
}
