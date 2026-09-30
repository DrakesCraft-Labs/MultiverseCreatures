package com.Chagui68.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.kyori.adventure.text.format.NamedTextColor.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves the Adventure migration is text-neutral.
 *
 * Every item name and lore line in the plugin used to be a {@code ChatColor} concatenation. If a
 * migrated line drifts by a single space, a single colour code or a lost bold flag, players see it
 * immediately, so the parity has to be checked mechanically rather than by eye.
 *
 * Each test below serialises the {@link Component} that the migrated code now builds with
 * {@link LegacyComponentSerializer#legacySection()} and compares it to the exact legacy string the
 * old {@code ChatColor} expression produced (the {@code §} codes are written out so the expectation
 * reads as the original concatenation).
 */
class MscTextTest {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private static String legacy(Component component) {
        return LEGACY.serialize(component);
    }

    @Test
    @DisplayName("Verify title() matches a ChatColor colour + BOLD name")
    void testTitleMatchesLegacyBoldName() {
        // "ChatColor.GOLD + \"\" + ChatColor.BOLD + \"Scooby Cookie\""
        assertEquals("\u00a76\u00a7lScooby Cookie", legacy(MscText.title(GOLD, "Scooby Cookie")));
        assertEquals("\u00a7d\u00a7lHead Slime Gelatin",
                legacy(MscText.title(LIGHT_PURPLE, "Head Slime Gelatin")));
        assertEquals("\u00a75\u00a7lGarou Cosmic Core",
                legacy(MscText.title(DARK_PURPLE, "Garou Cosmic Core")));
    }

    @Test
    @DisplayName("Verify line() matches a single-colour ChatColor line")
    void testLineMatchesLegacySingleColourLine() {
        assertEquals("\u00a77A mysterious cookie pulsating",
                legacy(MscText.line(GRAY, "A mysterious cookie pulsating")));
        assertEquals("\u00a7fEffect on Consume:", legacy(MscText.line(WHITE, "Effect on Consume:")));
    }

    @Test
    @DisplayName("Verify quote() matches a ChatColor colour + ITALIC flavour line")
    void testQuoteMatchesLegacyItalicLine() {
        // "ChatColor.DARK_PURPLE + \"\" + ChatColor.ITALIC + \"\\\"Scooby-Dooby-Doo...\\\"\""
        assertEquals("\u00a75\u00a7o\"Scooby-Dooby-Doo...\"",
                legacy(MscText.quote(DARK_PURPLE, "\"Scooby-Dooby-Doo...\"")));
    }

    @Test
    @DisplayName("Verify rich() matches a line that switches colour mid-sentence")
    void testRichMatchesLegacyMidLineColourSwitch() {
        assertEquals("\u00a7e  \u25b8 \u00a77Resistance VI \u00a78(10 seconds)",
                legacy(MscText.rich(YELLOW, "  \u25b8 ", GRAY, "Resistance VI ", DARK_GRAY, "(10 seconds)")));
        assertEquals("\u00a7bFood: \u00a7f2 \u00a7bSaturation: \u00a7f0.4",
                legacy(MscText.rich(AQUA, "Food: ", WHITE, "2 ", AQUA, "Saturation: ", WHITE, "0.4")));
    }

    @Test
    @DisplayName("Verify footer() matches the shared dark-grey + grey item footer")
    void testFooterMatchesLegacyFooter() {
        assertEquals("\u00a78\u2726 \u00a77Mystery Inc.\u00a78 \u2726", legacy(MscText.footer("Mystery Inc.")));
    }

    @Test
    @DisplayName("Verify a coloured footer matches the gold item footer")
    void testColouredFooterMatchesLegacyFooter() {
        // "ChatColor.GOLD + \"\u2726 \" + ChatColor.YELLOW + \"Special\" + ChatColor.GOLD + \" \u2726\""
        assertEquals("\u00a76\u2726 \u00a7eSpecial\u00a76 \u2726",
                legacy(MscText.rich(GOLD, "\u2726 ", YELLOW, "Special", GOLD, " \u2726")));
    }

    @Test
    @DisplayName("Verify blank() serialises to an empty lore line")
    void testBlankIsAnEmptyLine() {
        assertEquals("", legacy(MscText.blank()));
    }

    @Test
    @DisplayName("Verify plain() adds no colour codes, so vanilla item names stay vanilla")
    void testPlainAddsNoColourCodes() {
        // "Bone Wall", "Ender Blade", ... used to be set as plain strings with no colour at all.
        assertEquals("Bone Wall", legacy(MscText.plain("Bone Wall")));
        assertEquals("Soul Reaper's Scythe", legacy(MscText.plain("Soul Reaper's Scythe")));
    }

    @Test
    @DisplayName("Verify a rich() segment never inherits its neighbour's colour")
    void testRichSegmentsDoNotInheritColour() {
        Component line = MscText.rich(GRAY, "Blocking absorbs ", RED, "50% ", GRAY, "of incoming");
        assertEquals("\u00a77Blocking absorbs \u00a7c50% \u00a77of incoming", legacy(line));
    }

    @Test
    @DisplayName("Verify rich() rejects odd argument counts and non-colour arguments")
    void testRichValidatesItsArguments() {
        // An odd argument count cannot be split into (colour, text) pairs.
        assertThrows(IllegalArgumentException.class, () -> MscText.rich(GRAY, "text", RED));
        // An even count whose first argument is not a colour.
        assertThrows(IllegalArgumentException.class, () -> MscText.rich("not a colour", "text"));
    }

    @Test
    @DisplayName("Verify an empty rich() call is an empty line rather than a crash")
    void testRichWithNoArgumentsIsEmpty() {
        assertEquals(Component.empty(), MscText.rich());
        assertEquals("", legacy(MscText.rich()));
    }

    @Test
    @DisplayName("Verify a full migrated lore list serialises line for line")
    void testFullLoreListSerialisesLineForLine() {
        List<Component> lore = List.of(
                MscText.line(GRAY, "Bouncy and wobbly, yet strangely tasty."),
                MscText.blank(),
                MscText.line(WHITE, "Effect on Consume:"),
                MscText.rich(YELLOW, "  \u25b8 ", GRAY, "Head Slime Immunity ", DARK_GRAY, "(10 seconds)"),
                MscText.blank(),
                MscText.rich(AQUA, "Food: ", WHITE, "4 ", AQUA, "Saturation: ", WHITE, "2.4"),
                MscText.blank(),
                MscText.quote(DARK_PURPLE, "\"Slimy yet satisfying!\""),
                MscText.blank(),
                MscText.footer("Slime Kingdom"));

        assertEquals(List.of(
                "\u00a77Bouncy and wobbly, yet strangely tasty.",
                "",
                "\u00a7fEffect on Consume:",
                "\u00a7e  \u25b8 \u00a77Head Slime Immunity \u00a78(10 seconds)",
                "",
                "\u00a7bFood: \u00a7f4 \u00a7bSaturation: \u00a7f2.4",
                "",
                "\u00a75\u00a7o\"Slimy yet satisfying!\"",
                "",
                "\u00a78\u2726 \u00a77Slime Kingdom\u00a78 \u2726"),
                lore.stream().map(MscTextTest::legacy).toList());
    }

    @Test
    @DisplayName("Verify plainText() strips colour and decoration, for code that compares a name")
    void testPlainTextStripsFormatting() {
        assertEquals("Head Slime", MscText.plainText(MscText.title(GREEN, "Head Slime")));
        assertEquals("Obsidian Guard: Face me!", MscText.plainText(
                MscText.rich(DARK_GRAY, "Obsidian Guard: ", GRAY, "Face me!")));
        assertEquals("Garou [Hero Hunter]", MscText.plainText(garouName()));
    }

    @Test
    @DisplayName("Verify plainText() of a nameless entity is empty rather than a crash")
    void testPlainTextOfNamelessEntityIsEmpty() {
        assertEquals("", MscText.plainText(null));
        assertEquals("", MscText.plainText(Component.empty()));
    }

    /**
     * The Garou name tag: a bold dark-purple prefix followed by a light-purple bracket.
     *
     * In legacy formatting a colour code clears bold, so the bracket is NOT bold. Building the two
     * halves as siblings reproduces that; appending the second half to the decorated first one would
     * let the bold leak down into it instead, which is why the code reads the long way round.
     */
    private static Component garouName() {
        return Component.empty()
                .append(MscText.title(DARK_PURPLE, "Garou "))
                .append(MscText.line(LIGHT_PURPLE, "[Hero Hunter]"));
    }

    @Test
    @DisplayName("Verify a bold prefix followed by another colour keeps legacy's bold reset")
    void testBoldPrefixFollowedByColourKeepsLegacyReset() {
        assertEquals("\u00a75\u00a7lGarou \u00a7d[Hero Hunter]", legacy(garouName()));

        // The trap this avoids: as a child of the bold component, the bracket would inherit bold.
        Component leaked = MscText.title(DARK_PURPLE, "Garou ")
                .append(MscText.line(LIGHT_PURPLE, "[Hero Hunter]"));
        assertNotEquals("\u00a75\u00a7lGarou \u00a7d[Hero Hunter]", legacy(leaked));
    }
}
