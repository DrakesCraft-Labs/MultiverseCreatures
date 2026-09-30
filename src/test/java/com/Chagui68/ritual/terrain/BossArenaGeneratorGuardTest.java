package com.Chagui68.ritual.terrain;

import com.Chagui68.testsupport.ProjectPaths;
import com.Chagui68.testsupport.SourceText;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards on the terrain code itself, for the properties a behavioural test cannot see.
 *
 * <p>The generator's correctness rests on three things that are easy to break by accident and
 * invisible when they are: it must not read the {@link java.util.Random} the server hands it (that
 * would make the same chunk generate differently every time), it must declare every generation
 * stage explicitly (a stray {@code true} would sprinkle vanilla decoration over the arena), and the
 * sink must keep dropping writes outside the chunk (without that, a landmark would carve its
 * neighbours' blocks and chunk order would decide the world).
 */
class BossArenaGeneratorGuardTest {

    private static final Path TERRAIN = ProjectPaths.source("com", "Chagui68", "ritual", "terrain");
    private static final Path GENERATOR = TERRAIN.resolve("BossArenaGenerator.java");
    private static final Path MANAGER = ProjectPaths.source("com", "Chagui68", "ritual", "BossDimensionManager.java");

    @Test
    @DisplayName("The generator never reads the Random the server passes in")
    void generationIgnoresTheServerRandom() {
        String body = SourceText.methodBody(ProjectPaths.read(GENERATOR), "public void generateNoise(");
        assertTrue(body.contains("ArenaShape") || body.contains("generateArea"),
                "generateNoise should only forward to the area generator: " + body);
        assertFalse(body.contains("random"),
                "generateNoise uses the chunk Random, so the same chunk would generate differently "
                        + "on every pass and chunk borders would not line up: " + body);
    }

    @Test
    @DisplayName("Every generation stage is declared, and only noise is enabled")
    void stagesAreDeclaredExplicitly() {
        String source = ProjectPaths.read(GENERATOR);
        Map<String, Boolean> expected = Map.of(
                "shouldGenerateNoise", true,
                "shouldGenerateSurface", false,
                "shouldGenerateBedrock", false,
                "shouldGenerateCaves", false,
                "shouldGenerateDecorations", false,
                "shouldGenerateMobs", false,
                "shouldGenerateStructures", false);

        for (Map.Entry<String, Boolean> stage : expected.entrySet()) {
            String body = SourceText.methodBody(source, "public boolean " + stage.getKey() + "(");
            assertTrue(body.contains("return " + stage.getValue() + ";"),
                    stage.getKey() + " must return " + stage.getValue() + ", found: " + body);
        }
        assertTrue(source.contains("isParallelCapable"), "the generator is stateless and must say so");
        assertTrue(SourceText.methodBody(source, "public boolean isParallelCapable(").contains("return true;"));
    }

    @Test
    @DisplayName("The chunk sink still drops everything outside its chunk")
    void theSinkKeepsItsBoundsCheck() {
        String source = ProjectPaths.read(GENERATOR);
        for (String method : List.of("public void set(", "public void column(")) {
            String body = SourceText.methodBody(source, method);
            assertTrue(body.contains("localX < 0") && body.contains("localZ < 0")
                            && body.contains("CHUNK_SIZE"),
                    method + " must reject writes outside the chunk: " + body);
            assertTrue(body.contains("minY") && body.contains("maxY"),
                    method + " must stay inside the world's height range: " + body);
        }
    }

    @Test
    @DisplayName("The shape and the noise stay pure: no Bukkit, so a test can run them anywhere")
    void theShapeStaysPure() {
        for (String file : List.of("ArenaShape.java", "ArenaNoise.java")) {
            assertFalse(SourceText.codeOnly(ProjectPaths.read(TERRAIN.resolve(file))).contains("org.bukkit"),
                    file + " uses Bukkit, so it can no longer be reasoned about (or tested) "
                            + "without a server");
        }
        String sink = SourceText.codeOnly(ProjectPaths.read(TERRAIN.resolve("TerrainSink.java")));
        assertFalse(sink.contains("ChunkData"),
                "the sink interface must stay usable without a chunk, or the terrain tests lose "
                        + "the seam they drive the generator through");
    }

    @Test
    @DisplayName("The dimension is created with the new generator, not the flat world type")
    void theManagerUsesTheNewGenerator() {
        String source = ProjectPaths.read(MANAGER);
        assertTrue(source.contains("new BossArenaGenerator()"),
                "BossDimensionManager no longer installs the arena generator");
        assertFalse(source.contains("WorldType.FLAT"),
                "a flat world type fights the generator: the whole point of the terrain is that it "
                        + "is not a flat plain");
        assertFalse(source.contains("CryingObsidianChunkGenerator"),
                "the old crying-obsidian floor generator should be gone, not kept around");
    }

    @Test
    @DisplayName("No terrain class keeps mutable static state")
    void noMutableStaticState() {
        for (Path file : ProjectPaths.javaFiles(TERRAIN)) {
            List<String> offenders = new ArrayList<>();
            for (String line : SourceText.codeOnly(ProjectPaths.read(file)).lines().toList()) {
                String trimmed = line.trim();
                if (!trimmed.contains("static")) continue;
                if (trimmed.contains("(") || trimmed.contains("final")) continue;
                offenders.add(trimmed);
            }
            assertTrue(offenders.isEmpty(),
                    ProjectPaths.relative(file) + " keeps mutable static state, which two chunks "
                            + "generating in parallel would share: " + offenders);
        }
    }

    @Test
    @DisplayName("Every knob the boss dimension documents is read by the code")
    void documentedKnobsAreUsed() {
        Set<String> documented = new LinkedHashSet<>();
        try (InputStream in = Files.newInputStream(ProjectPaths.resource("config.yml"))) {
            Object root = new Yaml().load(in);
            assertTrue(root instanceof Map, "config.yml is not a mapping");
            Object section = ((Map<?, ?>) root).get("boss-dimension");
            assertTrue(section instanceof Map, "config.yml lost its boss-dimension section");
            for (Object key : ((Map<?, ?>) section).keySet()) {
                documented.add("boss-dimension." + key);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read config.yml", e);
        }
        assertTrue(documented.size() >= 2, "the section should document the dimension's switches");

        StringBuilder sources = new StringBuilder();
        ProjectPaths.javaFiles(ProjectPaths.mainJava()).forEach(file -> sources.append(ProjectPaths.read(file)));
        String all = sources.toString();
        for (String key : documented) {
            assertTrue(all.contains("\"" + key + "\""),
                    key + " is documented in config.yml but no code ever reads it");
        }
    }


    /**
     * The body of the first method whose signature starts with {@code signature}.
     *
     * <p>Braces are counted, which is enough for this source and keeps the guard readable; the
     * assertions all look for text inside the body, so a body that stopped early would fail them
     * rather than pass silently.
     */
}
