package com.Chagui68.utils;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.World;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Central world allowlist for every MultiverseCreatures spawn path. */
public final class MscWorldPolicy {

    private static final List<String> DEFAULT_ALLOWED_WORLDS = List.of(
            "world",
            "world_nether",
            "world_the_end",
            "boss_dimension",
            "drakes_bosses"
    );

    private MscWorldPolicy() {
    }

    /**
     * Returns whether MSC creatures may exist in the supplied world.
     * Missing configuration falls back to Survival and dedicated boss worlds.
     */
    public static boolean isAllowed(MultiverseCreatures plugin, World world) {
        if (plugin == null || world == null) return false;
        List<String> configured = plugin.getConfig().getStringList("general.allowed-creature-worlds");
        List<String> source = configured.isEmpty() ? DEFAULT_ALLOWED_WORLDS : configured;
        String worldName = world.getName().toLowerCase(Locale.ROOT);
        for (String allowed : source) {
            if (allowed != null && worldName.equals(allowed.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /** Normalized configured names, used only for concise startup diagnostics. */
    public static Set<String> allowedWorldNames(MultiverseCreatures plugin) {
        List<String> configured = plugin.getConfig().getStringList("general.allowed-creature-worlds");
        List<String> source = configured.isEmpty() ? DEFAULT_ALLOWED_WORLDS : configured;
        Set<String> names = new HashSet<>();
        for (String value : source) {
            if (value != null && !value.isBlank()) names.add(value.trim().toLowerCase(Locale.ROOT));
        }
        return Set.copyOf(names);
    }
}
