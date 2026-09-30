package com.Chagui68.utils;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.World;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Central world allowlist for every MultiverseCreatures spawn path.
 *
 * SEMANTICS
 *
 * {@code general.allowed-creature-worlds} is an allowlist. An empty list means "every world",
 * which is what config.yml documents; it used to silently mean "only the worlds hardcoded in
 * this class", so a server with a custom world name got no conversions at all.
 *
 * The plugin's own worlds are always allowed on top of the user list: the boss dimension is
 * created by the plugin and would otherwise be blocked the moment an operator narrows the list.
 */
public final class MscWorldPolicy {

    /**
     * Worlds created and owned by the plugin. Always allowed, whatever the user allowlist says.
     */
    private static final Set<String> INTERNAL_WORLDS = Set.of(
            "boss_dimension",
            "drakes_bosses"
    );

    private MscWorldPolicy() {
    }

    /**
     * Returns whether MSC creatures may exist in the supplied world.
     *
     * Empty configuration means every world, matching the documented behaviour.
     */
    public static boolean isAllowed(MultiverseCreatures plugin, World world) {
        if (plugin == null || world == null) return false;
        List<String> configured = plugin.getConfig().getStringList("general.allowed-creature-worlds");
        return isAllowed(configured, world.getName());
    }

    /**
     * Pure decision used by {@link #isAllowed(MultiverseCreatures, World)}.
     *
     * Kept separate so the policy can be tested without a server: the bug this replaces was a
     * documented-vs-implemented mismatch, and the only way to stop it coming back is a test.
     */
    public static boolean isAllowed(List<String> configuredWorlds, String worldName) {
        if (worldName == null) return false;
        String normalized = normalize(worldName);
        if (INTERNAL_WORLDS.contains(normalized)) return true;
        Set<String> allowed = normalizeAll(configuredWorlds);
        // Documented behaviour: an empty list means "no restriction".
        if (allowed.isEmpty()) return true;
        return allowed.contains(normalized);
    }

    /** Effective allowlist for diagnostics and command output, including the plugin's own worlds. */
    public static Set<String> allowedWorldNames(MultiverseCreatures plugin) {
        List<String> configured = plugin == null
                ? List.of()
                : plugin.getConfig().getStringList("general.allowed-creature-worlds");
        Set<String> names = new HashSet<>(normalizeAll(configured));
        names.addAll(INTERNAL_WORLDS);
        return Set.copyOf(names);
    }

    private static Set<String> normalizeAll(List<String> values) {
        Set<String> names = new HashSet<>();
        if (values == null) return names;
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                names.add(normalize(value));
            }
        }
        return names;
    }

    private static String normalize(String worldName) {
        return worldName.trim().toLowerCase(Locale.ROOT);
    }
}
