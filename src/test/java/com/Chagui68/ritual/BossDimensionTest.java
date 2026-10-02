package com.Chagui68.ritual;

import com.Chagui68.testsupport.ProjectPaths;
import com.Chagui68.testsupport.SourceText;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BossDimensionTest {

    @Test
    @DisplayName("Only the plugin's terrain is generated: no vanilla terrain underneath or around it")
    void noVanillaTerrain() {
        String code = SourceText.codeOnly(ProjectPaths.read(
                ProjectPaths.source("com", "Chagui68", "ritual", "terrain", "WastelandGenerator.java")));
        for (String stage : new String[]{"shouldGenerateNoise", "shouldGenerateSurface", "shouldGenerateCaves",
                "shouldGenerateDecorations", "shouldGenerateMobs", "shouldGenerateStructures"}) {
            String body = SourceText.methodBody(code, "public boolean " + stage + "(");
            assertTrue(body.contains("return false;"),
                    stage + " must stay off: vanilla terrain would be generated under and around the battlefield");
        }
    }

    @Test
    @DisplayName("The border is 1500 blocks by default and never leaves 100 - 1500")
    void borderSizeIsClamped() {
        assertEquals(1500, BossDimensionManager.DEFAULT_SIZE);
        assertEquals(700, BossDimensionManager.borderSize(700));
        assertEquals(1500, BossDimensionManager.borderSize(30_000));
        assertEquals(100, BossDimensionManager.borderSize(0));
        assertEquals(100, BossDimensionManager.borderSize(-5));
    }

    @Test
    @DisplayName("A world built by another generator is rebuilt once; the current one is kept")
    void outdatedWorldsAreDetected(@TempDir Path container) throws IOException {
        Path world = container.resolve("boss_dimension");
        assertFalse(BossDimensionManager.builtByAnotherGenerator(world), "no world yet: nothing to rebuild");

        Files.createDirectories(world);
        assertTrue(BossDimensionManager.builtByAnotherGenerator(world), "an unmarked world is an older terrain");

        Files.writeString(world.resolve(BossDimensionManager.GENERATOR_MARKER), "coliseum");
        assertTrue(BossDimensionManager.builtByAnotherGenerator(world));

        Files.writeString(world.resolve(BossDimensionManager.GENERATOR_MARKER), BossDimensionManager.GENERATOR_ID + "\n");
        assertFalse(BossDimensionManager.builtByAnotherGenerator(world), "the current battlefield must not be wiped again");
    }
}
