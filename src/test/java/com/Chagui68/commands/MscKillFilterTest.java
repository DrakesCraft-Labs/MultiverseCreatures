package com.Chagui68.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins what {@code /msc kill} considers "one of ours".
 *
 * The decision used to be an inline chain of {@code contains()} checks over scoreboard tags and the
 * entity's custom name, which meant a typo could silently stop purging a boss. It is now a pure
 * function, so the rules — including the legacy name fallbacks — are written down and tested.
 */
class MscKillFilterTest {

    @Nested
    @DisplayName("Plugin tag")
    class PluginTag {

        @Test
        @DisplayName("Any tag starting with MSC_ marks the entity")
        void detectsPrefix() {
            assertTrue(MscKillFilter.hasPluginTag(List.of("MSC_Dummy")));
            assertTrue(MscKillFilter.hasPluginTag(List.of("other", "MSC_SealMarker")));
            assertTrue(MscKillFilter.hasPluginTag(Set.of("MSC_")));
        }

        @Test
        @DisplayName("Unrelated tags and empty input are not ours")
        void rejectsEverythingElse() {
            assertFalse(MscKillFilter.hasPluginTag(List.of()));
            assertFalse(MscKillFilter.hasPluginTag(null));
            assertFalse(MscKillFilter.hasPluginTag(List.of("msc_lowercase", "MSC", "tamed")));
            assertFalse(MscKillFilter.hasPluginTag(java.util.Arrays.asList((String) null, "tamed")));
            assertTrue(MscKillFilter.hasPluginTag(java.util.Arrays.asList((String) null, "MSC_Real")));
        }
    }

    @Nested
    @DisplayName("Creature identification")
    class Identification {

        @Test
        @DisplayName("Tagged entities are ours whatever they are called")
        void taggedWins() {
            assertTrue(MscKillFilter.isMscCreature(List.of("MSC_Mahoraga"), "Mahoraga"));
            assertTrue(MscKillFilter.isMscCreature(List.of("MSC_Warlord"), "Bob the Zombie"));
            assertTrue(MscKillFilter.isMscCreature(List.of("MSC_Warlord"), null));
        }

        @Test
        @DisplayName("Legacy names still count when a creature spawned untagged")
        void legacyNamesFallback() {
            assertTrue(MscKillFilter.isMscCreature(List.of(), "Mahoraga"));
            assertTrue(MscKillFilter.isMscCreature(List.of(), "Garou [Hero Hunter]"));
            assertTrue(MscKillFilter.isMscCreature(List.of(), "Bone Shield Skeleton"));
            assertTrue(MscKillFilter.isMscCreature(List.of(), "Void Crawler"));
            assertTrue(MscKillFilter.isMscCreature(List.of(), "Shadow Rogue"));
            assertTrue(MscKillFilter.isMscCreature(List.of(), "Flame Elemental"));
            assertTrue(MscKillFilter.isMscCreature(List.of(), "Chaos Mage"));
            assertTrue(MscKillFilter.isMscCreature(List.of(), "Orcish Warlord"));
        }

        @Test
        @DisplayName("Plain vanilla mobs are left alone")
        void vanillaStays() {
            assertFalse(MscKillFilter.isMscCreature(List.of(), "Zombie"));
            assertFalse(MscKillFilter.isMscCreature(List.of(), "Cow"));
            assertFalse(MscKillFilter.isMscCreature(List.of(), ""));
            assertFalse(MscKillFilter.isMscCreature(List.of(), null));
            assertFalse(MscKillFilter.isMscCreature(null, null));
        }
    }

    @Nested
    @DisplayName("Type filter")
    class TypeFilter {

        @Test
        @DisplayName("The type matches tags with - and _ stripped")
        void matchesCleanedTags() {
            assertTrue(MscKillFilter.matchesType("mahoraga", List.of("MSC_Mahoraga"), null));
            assertTrue(MscKillFilter.matchesType("soul-reaper", List.of("MSC_SoulReaper"), null));
            assertTrue(MscKillFilter.matchesType("zombie_horse", List.of("MSC_ZombieHorseTrap"), null));
            assertTrue(MscKillFilter.matchesType("ArmorStand", List.of("MSC_ArmorStand"), null));
        }

        @Test
        @DisplayName("The type also matches the plain name, as a substring")
        void matchesName() {
            assertTrue(MscKillFilter.matchesType("garou", List.of(), "Garou [Hero Hunter]"));
            assertTrue(MscKillFilter.matchesType("kinger", List.of(), "Kinger"));
            assertFalse(MscKillFilter.matchesType("kinger", List.of(), "Garou [Hero Hunter]"));
        }

        @Test
        @DisplayName("Missing or blank type never matches")
        void blankNeverMatches() {
            assertFalse(MscKillFilter.matchesType(null, List.of("MSC_Whatever"), "Mahoraga"));
            assertFalse(MscKillFilter.matchesType("", List.of("MSC_Whatever"), "Mahoraga"));
            assertFalse(MscKillFilter.matchesType("  ", List.of("MSC_Whatever"), "Mahoraga"));
        }
    }
}
