package com.Chagui68.wiki;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * One page of the wiki.
 *
 * <p>Most pages are an item of the plugin: the page shows the item itself, with its own lore, and
 * the recipes that make it are found on the server (see {@link WikiRecipes}), so they can never
 * drift from the real ones. What no recipe says — which creature drops it, which merchant sells
 * it — is written here as {@link WikiSource}s. The other pages (the Stands, the bosses) have no
 * item: they are an icon with a description in both languages.</p>
 */
public final class WikiEntry {

    private final String id;
    private final WikiCategory category;
    private final Supplier<ItemStack> item;
    private final Material icon;
    private final NamedTextColor color;
    private final String nameEn;
    private final String nameEs;
    private final String aboutEn;
    private final String aboutEs;
    private final List<WikiSource> sources;
    private final List<Supplier<WikiRecipe>> recipes;

    private WikiEntry(Builder builder) {
        this.id = builder.id;
        this.category = builder.category;
        this.item = builder.item;
        this.icon = builder.icon;
        this.color = builder.color;
        this.nameEn = builder.nameEn;
        this.nameEs = builder.nameEs;
        this.aboutEn = builder.aboutEn;
        this.aboutEs = builder.aboutEs;
        this.sources = List.copyOf(builder.sources);
        this.recipes = List.copyOf(builder.recipes);
    }

    /** A page about an item of the plugin. */
    public static Builder item(String id, WikiCategory category, Supplier<ItemStack> item) {
        Builder builder = new Builder(id, category);
        builder.item = item;
        return builder;
    }

    /** A page about something that is not an item: a Stand, a boss, a ritual. */
    public static Builder info(String id, WikiCategory category, Material icon, NamedTextColor color,
                               String nameEn, String nameEs) {
        Builder builder = new Builder(id, category);
        builder.icon = icon;
        builder.color = color;
        builder.nameEn = nameEn;
        builder.nameEs = nameEs;
        return builder;
    }

    public String id() {
        return id;
    }

    public WikiCategory category() {
        return category;
    }

    /** True for a page about an item, false for an information page. */
    public boolean isItem() {
        return item != null;
    }

    /** A fresh copy of the item, or null for an information page. */
    public ItemStack item() {
        return item == null ? null : item.get().clone();
    }

    /** What the page says beyond the item's own lore, or null. */
    public String about(WikiLang lang) {
        return aboutEn == null ? null : lang.pick(aboutEn, aboutEs);
    }

    public List<WikiSource> sources() {
        return sources;
    }

    /** The recipes written here by hand: brews and trades, which the server keeps elsewhere. */
    public List<WikiRecipe> extraRecipes() {
        List<WikiRecipe> built = new ArrayList<>();
        for (Supplier<WikiRecipe> recipe : recipes) {
            built.add(recipe.get());
        }
        return built;
    }

    /** The icon of the page: the item itself, or the drawn icon of an information page. */
    public ItemStack icon(WikiLang lang) {
        if (item != null) {
            return item();
        }
        ItemStack stack = new ItemStack(icon);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(WikiText.title(color, lang.pick(nameEn, nameEs)));
            List<Component> lore = new ArrayList<>();
            if (aboutEn != null) {
                lore.addAll(WikiText.paragraph(NamedTextColor.GRAY, "", about(lang)));
            }
            meta.lore(lore);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.values());
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /** The page's name in plain text, for the menu title and tab completion. */
    public Component name(WikiLang lang) {
        if (item != null) {
            ItemMeta meta = item.get().getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                return meta.displayName();
            }
            return Component.translatable(item.get().translationKey());
        }
        return Component.text(lang.pick(nameEn, nameEs), color);
    }

    public static final class Builder {

        private final String id;
        private final WikiCategory category;
        private Supplier<ItemStack> item;
        private Material icon;
        private NamedTextColor color;
        private String nameEn;
        private String nameEs;
        private String aboutEn;
        private String aboutEs;
        private final List<WikiSource> sources = new ArrayList<>();
        private final List<Supplier<WikiRecipe>> recipes = new ArrayList<>();

        private Builder(String id, WikiCategory category) {
            this.id = id;
            this.category = category;
        }

        /** A paragraph about the item or the subject of the page, in both languages. */
        public Builder about(String en, String es) {
            this.aboutEn = en;
            this.aboutEs = es;
            return this;
        }

        public Builder source(WikiSource source) {
            sources.add(source);
            return this;
        }

        public Builder recipe(Supplier<WikiRecipe> recipe) {
            recipes.add(recipe);
            return this;
        }

        public WikiEntry build() {
            return new WikiEntry(this);
        }
    }
}
