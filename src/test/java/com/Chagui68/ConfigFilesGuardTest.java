package com.Chagui68;

import com.Chagui68.entities.Kinger;
import com.Chagui68.entities.boss.JackStarBoss;
import com.Chagui68.entities.boss.NixBoss;
import com.Chagui68.utils.MscEntityUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Keeps {@code config.yml}, {@code plugin.yml} and the code in agreement.
 *
 * {@code config.yml} is the only file server owners edit, and its own header promises that "all
 * paths match the code". Nothing enforced that promise: {@code NullshearEdgeHandler} read five
 * {@code items.nullshear-edge.*} keys that did not exist in the file, and two Excalibur keys were
 * documented but hardcoded. Both kinds of drift are silent — the code just keeps using its default
 * — so this guard parses the resources and checks the contract instead of trusting it.
 *
 * <p>It also proves the file is valid YAML at all: a broken indent in config.yml normally only
 * shows up when the server starts.
 */
class ConfigFilesGuardTest {

    private static final Path RESOURCES = Path.of("src", "main", "resources");
    private static final Path CONFIG = RESOURCES.resolve("config.yml");
    private static final Path PLUGIN = RESOURCES.resolve("plugin.yml");
    private static final Path SOURCES = Path.of("src", "main", "java");

    /** A quoted literal that looks like a config path: lowercase, at least one dot. */
    private static final Pattern CONFIG_LITERAL = Pattern.compile("\"([a-z][a-z0-9-]*(?:\\.[a-z0-9-]+)+)\"");

    /** Sub-command names that plugin.yml's usage line must mention. */
    private static final List<String> SUB_COMMANDS = List.of(
            "spawn", "give", "attack", "music", "seal", "dummy", "dimtp", "cleanstands", "kill", "debug",
            "reload");

    // ------------------------------------------------------------------ resources

    @Test
    @DisplayName("config.yml parses and keeps the sections its header documents")
    void configParses() {
        Map<String, Object> config = loadConfig();
        Set<String> paths = flatten(config);
        for (String section : List.of("general", "recipes", "boss-arena", "entities", "items", "commands")) {
            assertTrue(paths.contains(section), "config.yml lost its " + section + " section");
        }
    }

    @Test
    @DisplayName("plugin.yml declares the command, its node and the bypass permission")
    void pluginYmlIsComplete() {
        Map<String, Object> plugin = load(PLUGIN);

        assertEquals("com.Chagui68.MultiverseCreatures", plugin.get("main"));
        assertEquals("${project.version}", plugin.get("version"),
                "the version must stay a Maven placeholder, not a hardcoded number");
        assertEquals("1.21", plugin.get("api-version"));

        Map<String, Object> command = child(child(plugin, "commands"), "msc");

        Object permissionNode = command.get("permission");
        assertEquals("msc.admin", permissionNode, "plugin.yml must declare the node the code checks");
        assertEquals(permissionNode, value(loadConfig(), "commands.permission"),
                "commands.permission in config.yml and plugin.yml must be the same node");

        String usage = String.valueOf(command.get("usage"));
        for (String subCommand : SUB_COMMANDS) {
            assertTrue(usage.contains(subCommand), "usage line does not mention " + subCommand + ": " + usage);
        }

        Map<String, Object> permissions = child(plugin, "permissions");
        assertTrue(permissions.containsKey("msc.admin"));
        assertTrue(permissions.containsKey("msc.admin.bypass"),
                "msc.admin.bypass is checked by the boss dimension handlers and must be documented here");

        // The per-subcommand gate reads commands.subcommand-permissions.<name>; the documented
        // default is an empty map, which leaves every subcommand open to whoever passed the main
        // gate. A missing key would make the escape hatch invisible in the shipped config.
        Object subCommandPermissions = child(loadConfig(), "commands").get("subcommand-permissions");
        assertInstanceOf(Map.class, subCommandPermissions,
                "commands.subcommand-permissions must exist as a map in config.yml");
        assertTrue(((Map<?, ?>) subCommandPermissions).isEmpty(),
                "the shipped default must not restrict any subcommand");
    }

    @Test
    @DisplayName("No source reads a config key that config.yml does not define")
    void everyLiteralKeyExists() {
        Assumptions.assumeTrue(Files.isDirectory(SOURCES),
                "Skipped: src/main/java is not reachable from " + Path.of("").toAbsolutePath());

        Set<String> defined = flatten(loadConfig());
        Set<String> roots = new LinkedHashSet<>();
        for (String path : defined) {
            roots.add(path.split("\\.", 2)[0]);
        }

        List<Path> javaFiles;
        try (Stream<Path> files = Files.walk(SOURCES)) {
            javaFiles = files.filter(path -> path.toString().endsWith(".java")).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the sources under " + SOURCES, e);
        }
        assertTrue(javaFiles.size() > 100,
                "Expected to scan the whole main source set, found only " + javaFiles.size() + " files");

        List<String> literals = javaFiles.stream()
                .flatMap(ConfigFilesGuardTest::configLiterals)
                .filter(literal -> roots.contains(literal.split("\\.", 2)[0]))
                .toList();
        // Without this the guard could pass vacuously if the scan ever stopped matching literals.
        assertTrue(literals.size() > 150,
                "Expected to find the keys the code reads, found only " + literals.size());

        List<String> missing = literals.stream()
                // "items.grimoire." + spell is assembled at runtime: the literal is only a prefix.
                .map(literal -> literal.endsWith(".") ? literal.substring(0, literal.length() - 1) : literal)
                .filter(literal -> !defined.contains(literal))
                .distinct()
                .sorted()
                .toList();

        assertTrue(missing.isEmpty(),
                "These config paths are read by the code but missing from config.yml, so they silently "
                        + "fall back to code defaults:\n  " + String.join("\n  ", missing));
    }

    @Test
    @DisplayName("The guard detects the literals it is looking for")
    void guardDetectsLiterals() throws IOException {
        Path sample = Files.createTempFile("config-literal", ".java");
        try {
            Files.writeString(sample, String.join("\n",
                    "class Sample {",
                    "    // getConfig().getInt(\"items.ghost.interval-ticks\", 1)",
                    "    int a = config.getInt(\"entities.armor-stand-boss.health\", 3200);",
                    "    long b = config.getLong(\"items.excalibur.solar-flare.cooldown-ms\", 15000);",
                    "    String c = \"not.a.config.path\";",
                    "    String d = \"msc.admin\";",
                    "}"));
            List<String> found = configLiterals(sample).toList();
            assertEquals(List.of("entities.armor-stand-boss.health", "items.excalibur.solar-flare.cooldown-ms",
                    "not.a.config.path", "msc.admin"), found,
                    "every dotted literal outside comments must be reported, in file order");
            assertFalse(found.contains("items.ghost.interval-ticks"), "comment literals must be ignored");
        } finally {
            Files.deleteIfExists(sample);
        }
    }

    // ------------------------------------------------------------------ sentinel knobs

    @Nested
    @DisplayName("Player-facing knobs")
    class KnobDefaults {

        @Test
        @DisplayName("Sentinel phases, defences and the despawn timer are configurable")
        void sentinelKnobs() {
            Map<String, Object> config = loadConfig();
            String base = "entities.armor-stand-boss.";

            Object thresholds = value(config, base + "phase-thresholds");
            assertInstanceOf(List.class, thresholds, "phase-thresholds must be a list");
            double previous = 1.0;
            for (Object entry : (List<?>) thresholds) {
                double threshold = ((Number) entry).doubleValue();
                assertTrue(threshold > 0 && threshold <= 1, "threshold out of range: " + threshold);
                assertTrue(threshold < previous, "thresholds must descend: " + thresholds);
                previous = threshold;
            }

            for (String key : List.of("defense-duration-stone-skin-ticks",
                    "defense-duration-reflect-barrier-ticks", "defense-duration-absorb-shield-ticks")) {
                assertTrue(((Number) value(config, base + key)).intValue() >= 1, key + " must be at least 1 tick");
            }
            assertTrue(((Number) value(config, base + "no-player-despawn-ticks")).intValue() >= 0,
                    "0 means 'despawn as soon as nobody is in range' and must stay allowed");
            for (String key : List.of("ground-recovery-grace-ticks", "ground-recovery-search-radius",
                    "ground-recovery-cooldown-ticks")) {
                assertTrue(((Number) value(config, base + key)).doubleValue() > 0, key + " must be positive");
            }
        }

        @Test
        @DisplayName("Every toggleable mob and item keeps its enabled flag")
        void enabledFlags() {
            Map<String, Object> config = loadConfig();
            assertInstanceOf(Boolean.class, value(config, "general.debug"));
            assertInstanceOf(Number.class, value(config, "general.spawn-rate-multiplier"));
            assertInstanceOf(Boolean.class, value(config, "recipes.enabled"));

            for (String mob : List.of("armor-stand-boss", "mahoraga", "nix-executioner", "jackstar-architect",
                    "head-slime", "creeper-jr", "zombie-horse-trap", "warlord", "kinger")) {
                assertInstanceOf(Boolean.class, value(config, "entities." + mob + ".enabled"),
                        mob + " must expose an enabled flag");
            }
            assertInstanceOf(Boolean.class, value(config, "items.excalibur.enabled"));
            assertInstanceOf(Boolean.class, value(config, "items.grimoire.enabled"));
        }

        @Test
        @DisplayName("Each dressed boss ships the hitbox scale its geometry tests prove is right")
        void hitboxScales() {
            Map<String, Object> config = loadConfig();

            // The scale is the size of the invisible stand the suit is hit through, so the shipped
            // value is the one the model tests prove covers the model: change the knob and the fight
            // changes, so the two must agree out of the box.
            assertEquals(Kinger.MODEL_HITBOX_SCALE, number(config, "entities.kinger.hitbox-scale"), 1.0e-9,
                    "Kinger ships a hitbox scale the model test does not cover");
            assertEquals(NixBoss.MODEL_HITBOX_SCALE, number(config, "entities.nix-executioner.hitbox-scale"), 1.0e-9,
                    "NIX ships a hitbox scale the model test does not cover");
            assertEquals(JackStarBoss.MODEL_HITBOX_SCALE, number(config, "entities.jackstar-architect.hitbox-scale"), 1.0e-9,
                    "Jack Star ships a hitbox scale the model test does not cover");

            for (String boss : List.of("entities.kinger.", "entities.nix-executioner.", "entities.jackstar-architect.")) {
                double scale = number(config, boss + "hitbox-scale");
                assertTrue(scale >= MscEntityUtils.MIN_HITBOX_SCALE && scale <= MscEntityUtils.MAX_HITBOX_SCALE,
                        boss + "hitbox-scale is outside the range the code clamps to: " + scale);
            }
        }

        @Test
        @DisplayName("The Nullshear Edge keys the handler reads exist (regression)")
        void nullshearEdgeKeys() {
            Map<String, Object> config = loadConfig();
            String base = "items.nullshear-edge.";
            assertInRange(((Number) value(config, base + "darkness-chance")).doubleValue(), 0, 1);
            assertTrue(((Number) value(config, base + "darkness-duration-ticks")).intValue() > 0);
            assertInRange(((Number) value(config, base + "void-fraction")).doubleValue(), 0, 1);
            assertTrue(((Number) value(config, base + "void-blink-cooldown-ms")).longValue() > 0);
            assertTrue(((Number) value(config, base + "void-blink-range")).doubleValue() > 0);
        }

        private static void assertInRange(double value, double min, double max) {
            assertTrue(value >= min && value <= max, value + " is outside [" + min + ", " + max + "]");
        }
    }

    // ------------------------------------------------------------------ helpers

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadConfig() {
        return load(CONFIG);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> load(Path path) {
        Assumptions.assumeTrue(Files.isRegularFile(path), "Skipped: " + path + " is not reachable");
        try (InputStream in = Files.newInputStream(path)) {
            Object root = new Yaml().load(in);
            if (!(root instanceof Map)) throw new IllegalStateException(path + " is not a YAML mapping");
            return (Map<String, Object>) root;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + path, e);
        }
    }

    private static Set<String> flatten(Map<String, Object> config) {
        Set<String> paths = new LinkedHashSet<>();
        flattenInto(config, "", paths);
        return paths;
    }

    private static void flattenInto(Object node, String prefix, Set<String> paths) {
        if (!(node instanceof Map<?, ?> map)) return;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            paths.add(path);
            flattenInto(entry.getValue(), path, paths);
        }
    }

    private static double number(Map<String, Object> config, String path) {
        Object value = value(config, path);
        assertInstanceOf(Number.class, value, path + " must be a number");
        return ((Number) value).doubleValue();
    }

    /** Follows a dotted path and fails with a readable message when a segment is missing. */
    private static Object value(Map<String, Object> config, String path) {
        Object current = config;
        for (String segment : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map) || !map.containsKey(segment)) {
                throw new AssertionError("config.yml has no " + path);
            }
            current = map.get(segment);
        }
        return current;
    }

    private static Optional<Map<String, Object>> section(Map<String, Object> root, String key) {
        Object raw = root.get(key);
        if (!(raw instanceof Map<?, ?> map)) return Optional.empty();
        Map<String, Object> copy = new java.util.LinkedHashMap<>();
        map.forEach((k, v) -> copy.put(String.valueOf(k), v));
        return Optional.of(copy);
    }

    /** A required nested section; fails with the section name when it is missing or not a mapping. */
    private static Map<String, Object> child(Map<String, Object> root, String key) {
        return section(root, key).orElseThrow(() -> new AssertionError("missing section " + key));
    }

    /** Every config-looking string literal in a file, ignoring comments. */
    private static Stream<String> configLiterals(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }

        List<String> found = new ArrayList<>();
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.startsWith("//") || line.startsWith("*") || line.startsWith("/*")) continue;
            Matcher matcher = CONFIG_LITERAL.matcher(line);
            while (matcher.find()) {
                found.add(matcher.group(1));
            }
        }
        return found.stream();
    }
}
