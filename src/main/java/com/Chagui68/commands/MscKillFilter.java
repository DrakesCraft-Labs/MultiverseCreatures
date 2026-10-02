package com.Chagui68.commands;

import java.util.Collection;
import java.util.List;

/**
 * Pure predicate layer behind {@code /msc kill}.
 *
 * <p>Deciding whether an entity belongs to MultiverseCreatures used to be a chain of string checks
 * buried inside the command's world scan. It is now a plain function of a scoreboard-tag list and a
 * plain-text name, which unit tests can exercise without a server.
 */
final class MscKillFilter {

    private static final String TAG_PREFIX = "MSC_";
    /** JackStar predates the prefix convention: his stand, body, minions and props are tagged in lowercase. */
    private static final String JACK_TAG_PREFIX = "msc_jackstar_";

    /**
     * Fallback names for creatures that historically spawned without the {@code MSC_} tag. Kept
     * verbatim so {@code /msc kill} keeps purging the same entities.
     */
    private static final List<String> KNOWN_NAMES = List.of(
            "Mahoraga", "Garou", "Bone Shield", "Void Crawler",
            "Shadow Rogue", "Flame Elemental", "Chaos Mage", "Warlord");

    private MscKillFilter() {}

    /** True when the entity carries a plugin tag. */
    static boolean hasPluginTag(Collection<String> scoreboardTags) {
        if (scoreboardTags == null) return false;
        for (String tag : scoreboardTags) {
            if (tag != null && (tag.startsWith(TAG_PREFIX) || tag.startsWith(JACK_TAG_PREFIX))) return true;
        }
        return false;
    }

    /** True when the entity is one of ours, by tag or by its (plain) custom name. */
    static boolean isMscCreature(Collection<String> scoreboardTags, String plainName) {
        if (hasPluginTag(scoreboardTags)) return true;
        if (plainName == null || plainName.isEmpty()) return false;
        for (String known : KNOWN_NAMES) {
            if (plainName.contains(known)) return true;
        }
        return false;
    }

    /**
     * True when an already-identified creature matches {@code targetType}: either one of its tags
     * contains the type (with {@code -}/{@code _} stripped) or its name contains the raw type.
     */
    static boolean matchesType(String targetType, Collection<String> scoreboardTags, String plainName) {
        if (targetType == null || targetType.isBlank()) return false;
        String wanted = targetType.toLowerCase();
        String cleaned = wanted.replace("-", "").replace("_", "");
        if (scoreboardTags != null) {
            for (String tag : scoreboardTags) {
                if (tag != null && tag.toLowerCase().contains(cleaned)) return true;
            }
        }
        return plainName != null && plainName.toLowerCase().contains(wanted);
    }
}
