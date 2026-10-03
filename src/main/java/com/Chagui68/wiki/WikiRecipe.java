package com.Chagui68.wiki;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A recipe as the wiki draws it: a 3x3 grid of ingredients (null for an empty slot), the station
 * that makes it and what comes out.
 *
 * <p>Crafting recipes fill the grid as the crafting table does. A furnace puts its input in the
 * middle, a brewing stand puts the ingredient on top and the potion at the bottom, and a trade lays
 * its price across the middle row.</p>
 */
public record WikiRecipe(Station station, List<ItemStack> grid, ItemStack result) {

    /** Where a recipe is made. */
    public enum Station {
        CRAFTING(Material.CRAFTING_TABLE, "Crafting Table", "Mesa de crafteo"),
        FURNACE(Material.FURNACE, "Furnace", "Horno"),
        BLAST_FURNACE(Material.BLAST_FURNACE, "Blast Furnace", "Alto horno"),
        BREWING(Material.BREWING_STAND, "Brewing Stand", "Soporte para pociones"),
        TRADE(Material.EMERALD, "Trade with a merchant", "Intercambio con un mercader");

        private final Material icon;
        private final String nameEn;
        private final String nameEs;

        Station(Material icon, String nameEn, String nameEs) {
            this.icon = icon;
            this.nameEn = nameEn;
            this.nameEs = nameEs;
        }

        public Material icon() {
            return icon;
        }

        public String name(WikiLang lang) {
            return lang.pick(nameEn, nameEs);
        }
    }

    public WikiRecipe {
        List<ItemStack> nine = new ArrayList<>(Collections.nCopies(9, (ItemStack) null));
        for (int i = 0; i < Math.min(9, grid.size()); i++) {
            nine.set(i, grid.get(i));
        }
        grid = Collections.unmodifiableList(nine);
    }

    /** A furnace or blast furnace recipe: the input sits in the middle of the grid. */
    public static WikiRecipe cooking(Station station, ItemStack input, ItemStack result) {
        List<ItemStack> grid = new ArrayList<>(Collections.nCopies(9, (ItemStack) null));
        grid.set(4, input);
        return new WikiRecipe(station, grid, result);
    }

    /** A brewing stand recipe: the ingredient on top, the potion below. */
    public static WikiRecipe brewing(ItemStack ingredient, ItemStack potion, ItemStack result) {
        List<ItemStack> grid = new ArrayList<>(Collections.nCopies(9, (ItemStack) null));
        grid.set(1, ingredient);
        grid.set(7, potion);
        return new WikiRecipe(Station.BREWING, grid, result);
    }

    /** A trade: up to two prices on the middle row. */
    public static WikiRecipe trade(ItemStack first, ItemStack second, ItemStack result) {
        List<ItemStack> grid = new ArrayList<>(Collections.nCopies(9, (ItemStack) null));
        grid.set(3, first);
        grid.set(5, second);
        return new WikiRecipe(Station.TRADE, grid, result);
    }

    /**
     * Places a shaped recipe in the 3x3 grid, centred when it is smaller, the way a player would
     * usually lay it out.
     *
     * @param rows    the recipe shape, one string per row
     * @param resolve the ingredient of each key character, or null for none
     */
    public static List<ItemStack> layout(String[] rows, java.util.function.Function<Character, ItemStack> resolve) {
        List<ItemStack> grid = new ArrayList<>(Collections.nCopies(9, (ItemStack) null));
        int height = Math.min(3, rows.length);
        int width = 0;
        for (String row : rows) {
            width = Math.max(width, row.length());
        }
        width = Math.min(3, width);
        int top = (3 - height) / 2;
        int left = (3 - width) / 2;
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < Math.min(3, rows[r].length()); c++) {
                char key = rows[r].charAt(c);
                if (key == ' ') {
                    continue;
                }
                grid.set((top + r) * 3 + left + c, resolve.apply(key));
            }
        }
        return grid;
    }
}
