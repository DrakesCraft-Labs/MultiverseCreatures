package com.Chagui68.wiki;

import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The pure parts of the wiki: wrapping, chances, language and recipe layout. */
class WikiTextTest {

    @Test
    @DisplayName("Lore paragraphs wrap on spaces and never exceed the width")
    void wrapsOnSpaces() {
        List<String> lines = WikiText.wrap("Takes the place of some Witches, raids included, and drops a crystal.", 20);
        assertTrue(lines.size() > 1);
        for (String line : lines) {
            assertTrue(line.length() <= 20, line);
        }
        assertEquals("Takes the place of some Witches, raids included, and drops a crystal.", String.join(" ", lines));
    }

    @Test
    @DisplayName("Chances read as percentages")
    void formatsChances() {
        assertEquals("60%", WikiSource.formatChance(0.6));
        assertEquals("0.2%", WikiSource.formatChance(0.002));
        assertEquals("100%", WikiSource.formatChance(1.5));
        assertEquals("75%", new WikiSource(null, "", "", "", "", "x", 75.0, true).chance(null));
    }

    @Test
    @DisplayName("Stand rarity is its weight over every Stand's weight")
    void standRarityIsAShare() {
        assertEquals("30%", new WikiSource(null, "", "", "", "", "stands.hermit-purple.weight", 30, false).chance(null));
        assertEquals("3%", new WikiSource(null, "", "", "", "", "stands.the-world.weight", 3, false).chance(null));
    }

    @Test
    @DisplayName("Spanish clients read Spanish, everyone else English")
    void languageFromLocale() {
        assertEquals(WikiLang.ES, WikiLang.fromLocale("es"));
        assertEquals(WikiLang.ES, WikiLang.fromLocale("ES"));
        assertEquals(WikiLang.EN, WikiLang.fromLocale("en"));
        assertEquals(WikiLang.EN, WikiLang.fromLocale(null));
        assertEquals("Hola", WikiLang.ES.pick("Hello", "Hola"));
    }

    @Test
    @DisplayName("Small recipes are centred in the grid like on a crafting table")
    void layoutCentresSmallRecipes() {
        ItemStack marker = null;
        List<ItemStack> grid = WikiRecipe.layout(new String[]{"AB"}, c -> marker);
        assertEquals(9, grid.size());
        List<Character> seen = new java.util.ArrayList<>();
        WikiRecipe.layout(new String[]{"AB"}, c -> {
            seen.add(c);
            return null;
        });
        assertEquals(List.of('A', 'B'), seen);
        int[] slots = WikiMenu.spread(3);
        assertArrayEquals(new int[]{20, 22, 24}, slots);
    }
}
