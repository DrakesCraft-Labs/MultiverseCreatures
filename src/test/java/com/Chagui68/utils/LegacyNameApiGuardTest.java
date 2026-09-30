package com.Chagui68.utils;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keeps the ChatColor migration finished.
 *
 * Item names, item lore and mob names are now Adventure Components. The deprecated String setters
 * still exist in the Paper API and still work, so a new item or mob written the old way would
 * compile, pass every other test and only show up as a subtly wrong tooltip in game. This guard
 * reads the sources instead of waiting for someone to notice.
 *
 * Only the six item/entity name APIs are checked. Text a player reads as chat, a title, an action
 * bar or a boss bar is a different concern and is deliberately not flagged.
 *
 * Matches inside comments are ignored, so this file's own explanation of the API and the notes in
 * {@link MscText} do not count as usage.
 */
class LegacyNameApiGuardTest {

    /** The deprecated String-based name and lore APIs, with the parenthesis so prose does not match. */
    private static final List<String> LEGACY_APIS = List.of(
            "setDisplayName(", "setLore(", "setItemName(",
            "setCustomName(", "getDisplayName(", "getCustomName(");

    @Test
    @DisplayName("Verify no item or entity name is built through the deprecated String API")
    void testNoSourceUsesTheDeprecatedNameApi() {
        Path sources = Path.of("src", "main", "java");
        Assumptions.assumeTrue(Files.isDirectory(sources),
                "Skipped: src/main/java is not reachable from " + Path.of("").toAbsolutePath());

        List<Path> javaFiles;
        try (Stream<Path> files = Files.walk(sources)) {
            javaFiles = files.filter(path -> path.toString().endsWith(".java")).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the sources under " + sources, e);
        }
        // Without this the guard could pass vacuously if the walk ever stopped finding the sources.
        assertTrue(javaFiles.size() > 100,
                "Expected to scan the whole main source set, found only " + javaFiles.size() + " files");

        List<String> offenders = javaFiles.stream()
                .flatMap(LegacyNameApiGuardTest::findLegacyUsage)
                .sorted()
                .toList();

        assertTrue(offenders.isEmpty(),
                "Item names, item lore and mob names must be built as Adventure Components. "
                        + "Use MscText (or displayName/lore/itemName/customName) instead of:\n  "
                        + String.join("\n  ", offenders));
    }

    @Test
    @DisplayName("Verify the guard actually detects the deprecated calls it looks for")
    void testGuardDetectsLegacyUsage() throws IOException {
        Path sample = Files.createTempFile("legacy-name-api", ".java");
        try {
            Files.writeString(sample, String.join("\n",
                    "class Sample {",
                    "    void names(ItemMeta meta, Entity entity) {",
                    "        meta.setDisplayName(\"x\");",
                    "        meta.setLore(java.util.List.of(\"y\"));",
                    "        meta.setItemName(\"z\");",
                    "        entity.setCustomName(\"n\");",
                    "        String a = meta.getDisplayName();",
                    "        String b = entity.getCustomName();",
                    "    }",
                    "}"));
            assertEquals(6, findLegacyUsage(sample).count(),
                    "Each of the six deprecated APIs must be reported");

            Files.writeString(sample, String.join("\n",
                    "class Clean {",
                    "    // meta.setLore(List.of(\"ignored inside a comment\"))",
                    "    void ok(ItemMeta meta) {",
                    "        meta.displayName(MscText.plain(\"x\"));",
                    "    }",
                    "}"));
            assertEquals(0, findLegacyUsage(sample).count(),
                    "Component-based calls and comments must not be reported");
        } finally {
            Files.deleteIfExists(sample);
        }
    }

    /** Every line of a file that calls a deprecated name API outside a comment. */
    private static Stream<String> findLegacyUsage(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }

        List<String> found = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("//") || line.startsWith("*") || line.startsWith("/*")) continue;
            for (String api : LEGACY_APIS) {
                if (line.contains(api)) {
                    found.add(file + ":" + (i + 1) + " -> " + line);
                }
            }
        }
        return found.stream();
    }
}
