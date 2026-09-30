package com.Chagui68.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Adventure Components for item names and lore.
 *
 * WHY IT EXISTS
 *
 * Item text used to be built by concatenating {@code ChatColor} constants, which relies on the
 * legacy {@code setDisplayName(String)} / {@code setLore(List<String>)} API that Paper deprecates.
 * These helpers produce real {@link Component}s instead, so item text no longer goes through a
 * String round-trip.
 *
 * THE TEXT IS UNCHANGED
 *
 * Every helper composes exactly the same characters with exactly the same colours as the legacy
 * string it replaces, including the mid-sentence colour switches in lines like
 * {@code rich(GRAY, "Blocking absorbs ", RED, "50% ", GRAY, "of incoming")} and the shared
 * {@code ✦ Multiverse ✦} footer. Nothing here reformats, rewraps or "improves" the wording.
 *
 * Only the colour and the decoration of a segment come from the caller: a segment never inherits
 * a colour from its neighbour, which is what keeps an appended line identical to the concatenated
 * one it came from.
 */
public final class MscText {

    private MscText() {
    }

    /** A line with no formatting at all (plain white). */
    public static Component plain(String text) {
        return Component.text(text);
    }

    /**
     * The plain text of a component, with no colour codes or decorations.
     *
     * For code that <em>compares</em> a name instead of showing it: reading a name back through the
     * legacy {@code getCustomName()} string is the deprecated path, and flattening the component
     * gives the same answer without depending on how the server serialises colours.
     *
     * A nameless entity yields an empty string, so callers never have to null-check. Note this is
     * the counterpart of {@link #plain(String)}: one builds a component, this one reads it back.
     */
    public static String plainText(Component component) {
        return component == null ? "" : PlainTextComponentSerializer.plainText().serialize(component);
    }

    /** A single-colour line. */
    public static Component line(TextColor color, String text) {
        return Component.text(text, color);
    }

    /** A bold name line, as used for every MSC item name. */
    public static Component title(TextColor color, String text) {
        return Component.text(text, color).decorate(TextDecoration.BOLD);
    }

    /** An italic line, used for the flavour quotes in lore. */
    public static Component quote(TextColor color, String text) {
        return Component.text(text, color).decorate(TextDecoration.ITALIC);
    }

    /** An empty lore line, used as a spacer between the sections of an item's lore. */
    public static Component blank() {
        return Component.empty();
    }

    /**
     * A line that switches colour mid-sentence.
     *
     * Arguments alternate colour and text: {@code rich(YELLOW, "  ▸ ", GRAY, "Grants ", GOLD, "Strength III")}.
     * Each segment keeps its own colour, so the result renders exactly like the concatenated
     * legacy string.
     *
     * @throws IllegalArgumentException when the arguments are not colour/text pairs
     */
    public static Component rich(Object... colorAndText) {
        if (colorAndText == null || colorAndText.length == 0) {
            return Component.empty();
        }
        if (colorAndText.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "rich() expects (color, text) pairs, got " + colorAndText.length + " arguments");
        }
        Component line = Component.empty();
        for (int i = 0; i < colorAndText.length; i += 2) {
            if (!(colorAndText[i] instanceof TextColor color)) {
                throw new IllegalArgumentException(
                        "rich() argument " + i + " must be a TextColor, got "
                                + (colorAndText[i] == null ? "null" : colorAndText[i].getClass().getName()));
            }
            line = line.append(Component.text(String.valueOf(colorAndText[i + 1]), color));
        }
        return line;
    }

    /** The {@code ✦ Multiverse ✦} footer that closes every MSC item lore. */
    public static Component footer(String label) {
        return footer(NamedTextColor.GRAY, label);
    }

    /**
     * The {@code ✦ label ✦} footer, for the items that label it in their own colour
     * (for example {@code ✦ JackStar Systems ✦}, which is gold).
     */
    public static Component footer(TextColor labelColor, String label) {
        return rich(NamedTextColor.DARK_GRAY, "✦ ",
                labelColor, label,
                NamedTextColor.DARK_GRAY, " ✦");
    }
}
