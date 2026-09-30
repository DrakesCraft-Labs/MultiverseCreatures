package com.Chagui68.entities.boss.seal;

import com.Chagui68.testsupport.ProjectPaths;
import com.Chagui68.testsupport.SourceText;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The split is the point: the listener paints, the seal package computes, and a seal can only be
 * tested while that stays true. These are the properties that keep the refactor from being undone
 * one shortcut at a time — a {@code Math.cos} reads harmless in a drawing method and is exactly how
 * the file grew to a thousand untestable lines.
 */
class SealOrchestrationGuardTest {

    private static final Path LISTENER =
            ProjectPaths.source("com", "Chagui68", "entities", "boss", "MagicSealListener.java");
    private static final Path PACKAGE =
            ProjectPaths.source("com", "Chagui68", "entities", "boss", "seal");

    /** Every server type the pure package must not know about. */
    private static final List<String> SERVER_TOKENS = List.of(
            "org.bukkit", "Bukkit", "Particle", "Location", "spawnParticle", "JavaPlugin", "MultiverseCreatures");

    /** Shape math that belongs to the geometry, not to the brush. */
    private static final List<String> TRIGONOMETRY = List.of(
            "Math.cos(", "Math.sin(", "Math.tan(", "Math.toRadians(", "Math.sqrt(", "Math.pow(");

    @Test
    @DisplayName("The listener has one scheduling site, so every seal shares the same loop")
    void theListenerSchedulesOnce() {
        String code = SourceText.codeOnly(ProjectPaths.read(LISTENER));
        assertEquals(1, count(code, ".runTaskTimer("),
                "the seals have to go through the shared repeat() helper; a second scheduling site means a "
                        + "seal grew its own loop again");
    }

    @Test
    @DisplayName("The listener draws; it does not do shape math")
    void theListenerDoesNotComputeShapes() {
        String code = SourceText.codeOnly(ProjectPaths.read(LISTENER));
        for (String call : TRIGONOMETRY) {
            assertFalse(code.contains(call),
                    "MagicSealListener calls " + call + ": trigonometry belongs in the seal package, where it "
                            + "can be tested without a server");
        }
    }

    @Test
    @DisplayName("The seal package never touches the server")
    void theSealPackageIsPure() {
        List<Path> files = ProjectPaths.javaFiles(PACKAGE);
        assertTrue(files.size() >= 5, "the pure seal package should hold the plane, the points and the shapes");
        for (Path file : files) {
            String code = SourceText.codeOnly(ProjectPaths.read(file));
            for (String token : SERVER_TOKENS) {
                assertFalse(code.contains(token),
                        ProjectPaths.relative(file) + " mentions " + token
                                + ": the geometry has to stay runnable from a plain unit test");
            }
        }
    }

    @Test
    @DisplayName("The listener delegates its shapes to the seal package")
    void theListenerDelegates() {
        String code = SourceText.codeOnly(ProjectPaths.read(LISTENER));
        assertTrue(code.contains("SealGeometry."), "the listener stopped using the extracted seal geometry");
        assertTrue(code.contains("WingGeometry."), "the listener stopped using the extracted wing geometry");
        assertTrue(count(code, "SealGeometry.") >= 20,
                "the listener should be a walk over the geometry, not a handful of leftover shapes");
    }

    @Test
    @DisplayName("The orchestrator stays thin")
    void theListenerStaysThin() {
        long lines = ProjectPaths.read(LISTENER).lines().count();
        assertTrue(lines < 700, "MagicSealListener is " + lines + " lines again: a seal has to be a short "
                + "frame body over the geometry, not its own drawing code");
    }

    private static int count(String text, String token) {
        int seen = 0;
        for (int index = text.indexOf(token); index >= 0; index = text.indexOf(token, index + 1)) {
            seen++;
        }
        return seen;
    }
}
