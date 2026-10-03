package com.Chagui68.wiki;

import com.Chagui68.items.components.RefinedNetherite;
import com.Chagui68.items.components.WheelCore;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The recipes of the plugin as the server has them, indexed by the item they make and by the
 * items they use.
 *
 * <p>Reading them back from the server instead of writing them out a second time is what keeps
 * the wiki honest: a recipe changed in {@code RecipeManager} changes here too. The index is built
 * the first time someone opens a page, which is always after the recipes were registered, and
 * dropped by {@link #reset()} on {@code /msc reload}.</p>
 */
public final class WikiRecipes {

    private static final String NAMESPACE = "multiversecreatures";

    private static Map<String, List<WikiRecipe>> byResult;
    private static Map<String, Set<String>> usedIn;

    private WikiRecipes() {
    }

    /**
     * What makes an item of the plugin itself: the sorted ids of its persistent data in the plugin's
     * namespace, or null for a vanilla item.
     */
    public static String identity(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        Set<String> keys = new TreeSet<>();
        for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
            if (key.getNamespace().equals(NAMESPACE)) {
                keys.add(key.getKey());
            }
        }
        return keys.isEmpty() ? null : String.join("+", keys);
    }

    public static synchronized void reset() {
        byResult = null;
        usedIn = null;
    }

    /** Every recipe that makes this item, registered and hand written alike. */
    public static List<WikiRecipe> making(WikiEntry entry) {
        List<WikiRecipe> recipes = new ArrayList<>();
        if (entry.isItem()) {
            recipes.addAll(index().getOrDefault(identity(entry.item()), List.of()));
        }
        recipes.addAll(entry.extraRecipes());
        return recipes;
    }

    /** The pages of the items that use this one as an ingredient. */
    public static List<WikiEntry> usedIn(WikiEntry entry) {
        List<WikiEntry> pages = new ArrayList<>();
        if (!entry.isItem()) {
            return pages;
        }
        index();
        Set<String> results = new LinkedHashSet<>(usedIn.getOrDefault(identity(entry.item()), Set.of()));
        // The hand written recipes (brews and trades) count too.
        for (WikiEntry other : WikiCatalogue.entries()) {
            for (WikiRecipe recipe : other.extraRecipes()) {
                for (ItemStack ingredient : recipe.grid()) {
                    String id = identity(ingredient);
                    if (id != null && id.equals(identity(entry.item()))) {
                        results.add(identity(recipe.result()));
                    }
                }
            }
        }
        for (WikiEntry other : WikiCatalogue.entries()) {
            if (other != entry && other.isItem() && results.contains(identity(other.item()))) {
                pages.add(other);
            }
        }
        return pages;
    }

    /** The page of a plugin item, or null for a vanilla item or one with no page. */
    public static WikiEntry pageOf(ItemStack item) {
        String id = identity(item);
        if (id == null) {
            return null;
        }
        for (WikiEntry entry : WikiCatalogue.entries()) {
            if (entry.isItem() && id.equals(identity(entry.item()))) {
                return entry;
            }
        }
        return null;
    }

    private static synchronized Map<String, List<WikiRecipe>> index() {
        if (byResult != null) {
            return byResult;
        }
        Map<String, List<WikiRecipe>> made = new LinkedHashMap<>();
        Map<String, Set<String>> uses = new LinkedHashMap<>();
        Iterator<Recipe> recipes = Bukkit.recipeIterator();
        while (recipes.hasNext()) {
            Recipe recipe = recipes.next();
            if (!(recipe instanceof Keyed keyed) || !keyed.getKey().getNamespace().equals(NAMESPACE)) {
                continue;
            }
            WikiRecipe drawn = draw(keyed.getKey(), recipe);
            if (drawn == null) {
                continue;
            }
            String result = identity(drawn.result());
            if (result == null) {
                continue;
            }
            made.computeIfAbsent(result, id -> new ArrayList<>()).add(drawn);
            for (ItemStack ingredient : drawn.grid()) {
                String id = identity(ingredient);
                if (id != null) {
                    uses.computeIfAbsent(id, key -> new LinkedHashSet<>()).add(result);
                }
            }
        }
        byResult = made;
        usedIn = uses;
        return made;
    }

    private static WikiRecipe draw(NamespacedKey key, Recipe recipe) {
        if (recipe instanceof ShapedRecipe shaped) {
            Map<Character, RecipeChoice> choices = shaped.getChoiceMap();
            List<ItemStack> grid = WikiRecipe.layout(shaped.getShape(), c -> first(choices.get(c)));
            return new WikiRecipe(WikiRecipe.Station.CRAFTING, grid, shaped.getResult());
        }
        if (recipe instanceof ShapelessRecipe shapeless) {
            List<ItemStack> grid = new ArrayList<>();
            for (RecipeChoice choice : shapeless.getChoiceList()) {
                grid.add(first(choice));
            }
            return new WikiRecipe(WikiRecipe.Station.CRAFTING, grid, shapeless.getResult());
        }
        if (recipe instanceof CookingRecipe<?> cooking) {
            WikiRecipe.Station station = recipe instanceof BlastingRecipe
                    ? WikiRecipe.Station.BLAST_FURNACE : WikiRecipe.Station.FURNACE;
            return WikiRecipe.cooking(station, smelted(key, cooking.getInputChoice()), cooking.getResult());
        }
        return null;
    }

    /**
     * What goes into a furnace recipe. Two of them take any item of the material the custom item is
     * made of (the recipe guard lets only the right one in), so the page shows the custom item.
     */
    private static ItemStack smelted(NamespacedKey key, RecipeChoice input) {
        String name = key.getKey();
        if (name.startsWith("molten_wheel_core")) {
            return WheelCore.WHEEL_CORE.clone();
        }
        if (name.startsWith("molten_netherite")) {
            return RefinedNetherite.REFINED_NETHERITE.clone();
        }
        return first(input);
    }

    /** The item a recipe slot shows: the exact item, or the first material it accepts. */
    private static ItemStack first(RecipeChoice choice) {
        if (choice instanceof RecipeChoice.ExactChoice exact && !exact.getChoices().isEmpty()) {
            return exact.getChoices().get(0).clone();
        }
        if (choice instanceof RecipeChoice.MaterialChoice material && !material.getChoices().isEmpty()) {
            return new ItemStack(material.getChoices().get(0));
        }
        return null;
    }
}
