package com.Chagui68.wiki;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

/** Small text helpers for the wiki's item names and lore. */
final class WikiText {

    /** Widest lore line, in characters, before it wraps. */
    static final int WIDTH = 38;

    private WikiText() {
    }

    /** Breaks a paragraph into lines of at most {@code width} characters, on spaces. */
    static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            if (line.length() > 0 && line.length() + 1 + word.length() > width) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) {
                line.append(' ');
            }
            line.append(word);
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    /** A lore line in one colour, never italic. */
    static Component line(TextColor color, String text) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }

    /** A bold, non italic item name. */
    static Component title(TextColor color, String text) {
        return Component.text(text, color).decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false);
    }

    /** A paragraph wrapped into lore lines of one colour, each starting with {@code indent}. */
    static List<Component> paragraph(TextColor color, String indent, String text) {
        List<Component> lines = new ArrayList<>();
        for (String part : wrap(text, WIDTH - indent.length())) {
            lines.add(line(color, indent + part));
        }
        return lines;
    }
}
