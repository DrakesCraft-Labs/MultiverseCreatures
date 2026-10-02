package com.Chagui68.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Keeps an existing {@code config.yml} in step with the one the jar ships.
 *
 * <p>{@code saveDefaultConfig()} only writes a config when there is none, so every server that
 * updates the plugin keeps its old file. New knobs are then missing, and a missing numeric key is
 * invisible: the code silently falls back to a default that nobody can see or change. That is how
 * the ten keys of the boss dimension's geometry and the new {@code boss-dimension.*} switches would
 * have arrived — documented, shipped, and absent from every running server.
 *
 * <p>It runs on start ({@code onEnable}) and on {@code /msc reload}, so a running server picks up a
 * release's keys without a restart.
 *
 * <p>The migration only ever <strong>adds</strong>: it reads the jar's defaults, writes the keys
 * the file does not have, stamps {@link #VERSION_KEY} and saves. A value an operator edited is
 * never touched, and a key they deleted on purpose comes back — which is the lesser evil in a file
 * whose header promises that all paths match the code.
 */
public final class MscConfigMigration {

    /**
     * Version of the shipped {@code config.yml}. Bump it when a release adds, moves or renames keys
     * so the log stops claiming the file is current once it is not.
     */
    public static final int CONFIG_VERSION = 3;

    /** Key holding that version; written into the file so an operator can see what they have. */
    public static final String VERSION_KEY = "config-version";

    /** How many added keys the log line lists before it summarises the rest. */
    private static final int LOGGED_KEYS = 8;

    private MscConfigMigration() {
    }

    /** Every dotted path inside a section, lists and scalars included. */
    public static Set<String> paths(ConfigurationSection section) {
        Set<String> paths = new LinkedHashSet<>();
        collect(section, "", paths);
        return paths;
    }

    /** The paths present in {@code defaults} but not in {@code present}, sorted. */
    public static Set<String> missingKeys(Set<String> defaults, Set<String> present) {
        Set<String> missing = new TreeSet<>(defaults);
        missing.removeAll(present);
        return missing;
    }

    /** The version the file declares, or 0 when it has none. */
    public static int version(Plugin plugin) {
        return plugin.getConfig().getInt(VERSION_KEY, 0);
    }

    /**
     * Adds the shipped defaults the file is missing and saves it when anything changed.
     *
     * @return the keys that were added, sorted, or an empty set when the file was already current
     */
    public static Set<String> mergeNewDefaults(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection defaults = config.getDefaults();
        Set<String> missing = defaults == null
                ? new TreeSet<>()
                : missingKeys(paths(defaults), paths(config));
        boolean outdated = version(plugin) != CONFIG_VERSION;
        if (missing.isEmpty() && !outdated) {
            return missing;
        }

        for (String key : missing) {
            config.set(key, defaults.get(key));
        }
        config.set(VERSION_KEY, CONFIG_VERSION);
        plugin.saveConfig();
        return missing;
    }

    /**
     * Runs the migration and reports what it did; a current file logs nothing.
     *
     * @return the keys that were added, sorted, or an empty set when the file was already current
     */
    public static Set<String> run(Plugin plugin) {
        int previous = version(plugin);
        Set<String> added = mergeNewDefaults(plugin);
        if (previous == CONFIG_VERSION && added.isEmpty()) {
            return added;
        }
        if (previous != CONFIG_VERSION) {
            plugin.getLogger().info("config.yml version " + previous + " -> " + CONFIG_VERSION);
        }
        if (!added.isEmpty()) {
            plugin.getLogger().info("config.yml: " + added.size()
                    + " new key(s) added from the shipped defaults (your values were kept)");
            String listed = String.join(", ", added.stream().limit(LOGGED_KEYS).toList());
            plugin.getLogger().info("  " + listed
                    + (added.size() > LOGGED_KEYS ? " … and " + (added.size() - LOGGED_KEYS) + " more" : ""));
        }
        return added;
    }

    private static void collect(ConfigurationSection section, String prefix, Set<String> paths) {
        for (String key : section.getKeys(false)) {
            String path = prefix.isEmpty() ? key : prefix + "." + key;
            paths.add(path);
            ConfigurationSection child = section.getConfigurationSection(key);
            if (child != null) {
                collect(child, path, paths);
            }
        }
    }
}
