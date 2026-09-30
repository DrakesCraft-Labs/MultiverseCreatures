package com.Chagui68.utils;

import com.Chagui68.testsupport.ProjectPaths;
import com.Chagui68.testsupport.SourceText;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The migration is the difference between a knob being shipped and a knob being usable: an existing
 * server keeps its own {@code config.yml}, so anything the release adds has to be merged into it.
 */
class MscConfigMigrationTest {

    @Test
    @DisplayName("Only the keys the file lacks are reported")
    void missingKeysOnlyReportsAbsentPaths() {
        Set<String> defaults = Set.of("general.debug", "general.spawn-rate-multiplier",
                "boss-dimension.red-sky", "boss-dimension.reset-on-load");
        Set<String> present = Set.of("general.debug", "general.spawn-rate-multiplier",
                "boss-dimension.red-sky");

        assertEquals(Set.of("boss-dimension.reset-on-load"),
                MscConfigMigration.missingKeys(defaults, present),
                "an operator's edited value must never be listed as missing");
        assertTrue(MscConfigMigration.missingKeys(defaults, defaults).isEmpty());
        assertEquals(defaults, MscConfigMigration.missingKeys(defaults, Set.of()));
    }

    @Test
    @DisplayName("Paths are dotted and reach through sections and lists")
    void pathsFlattenSectionsAndLists() {
        MemoryConfiguration config = new MemoryConfiguration();
        config.set("general.debug", false);
        config.set("general.max-alive-per-world", 500);
        config.set("entities.kinger.enabled", true);
        config.set("entities.armor-stand-boss.phase-thresholds", List.of(0.7, 0.4, 0.15));

        Set<String> paths = MscConfigMigration.paths(config);
        assertTrue(paths.contains("general.debug"));
        assertTrue(paths.contains("general.max-alive-per-world"));
        assertTrue(paths.contains("entities.kinger.enabled"));
        assertTrue(paths.contains("entities"), "a section is a path of its own");
        assertTrue(paths.contains("entities.kinger"));
        assertTrue(paths.contains("entities.armor-stand-boss.phase-thresholds"),
                "a list is a leaf: its entries are not config paths");
        assertFalse(paths.contains("entities.armor-stand-boss.phase-thresholds.0"),
                "indexing into a list would invent keys that are not in the file");
    }

    @Test
    @DisplayName("The shipped config declares the version the code expects")
    void shippedConfigIsCurrent() {
        Map<String, Object> config = loadConfig();
        Object declared = config.get(MscConfigMigration.VERSION_KEY);
        assertEquals(MscConfigMigration.CONFIG_VERSION, declared,
                "config.yml and MscConfigMigration.CONFIG_VERSION must agree, or every start logs a "
                        + "migration that does not happen");
    }

    @Test
    @DisplayName("The shipped config is the defaults a migration merges from")
    void shippedConfigShipsEveryDocumentedPath() {
        Set<String> flat = flatten(loadConfig());
        assertTrue(flat.size() > 400, "config.yml should still be the whole contract, found " + flat.size());
        assertTrue(flat.contains(MscConfigMigration.VERSION_KEY));
        assertTrue(flat.contains("boss-dimension.red-sky"), "the dimension section must be shipped");
        assertTrue(flat.contains("boss-dimension.reset-on-load"));
    }

    @Test
    @DisplayName("The plugin runs the migration before anything reads the config")
    void thePluginRunsTheMigration() {
        String source = ProjectPaths.read(ProjectPaths.source("com", "Chagui68", "MultiverseCreatures.java"));
        String onEnable = SourceText.codeOnly(SourceText.methodBody(source, "public void onEnable("));

        int saveDefault = onEnable.indexOf("saveDefaultConfig()");
        int migration = onEnable.indexOf("MscConfigMigration.run(this)");
        int firstRead = onEnable.indexOf("getConfig()");

        assertTrue(saveDefault > 0, "onEnable must still call saveDefaultConfig()");
        assertTrue(migration > saveDefault,
                "the migration has to run after saveDefaultConfig() and inside onEnable");
        assertTrue(firstRead > migration,
                "the migration must run before the first read in onEnable, otherwise a knob added "
                        + "by this release is still missing when the code asks for it");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadConfig() {
        try (InputStream in = Files.newInputStream(ProjectPaths.resource("config.yml"))) {
            Object root = new Yaml().load(in);
            if (!(root instanceof Map)) throw new AssertionError("config.yml is not a mapping");
            return (Map<String, Object>) root;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read config.yml", e);
        }
    }

    private static Set<String> flatten(Map<String, Object> root) {
        Set<String> paths = new java.util.LinkedHashSet<>();
        flattenInto(root, "", paths);
        return paths;
    }

    private static void flattenInto(Object node, String prefix, Set<String> paths) {
        if (!(node instanceof Map<?, ?> map)) return;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String path = prefix.isEmpty() ? String.valueOf(entry.getKey()) : prefix + "." + entry.getKey();
            paths.add(path);
            flattenInto(entry.getValue(), path, paths);
        }
    }
}
