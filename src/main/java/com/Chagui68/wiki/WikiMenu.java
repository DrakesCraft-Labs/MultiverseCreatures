package com.Chagui68.wiki;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * One screen of the wiki: the home page with its shelves, the list of pages on one shelf, or a
 * single page with its recipe, where it drops and what it is used in.
 *
 * <p>Every clickable slot carries its own action, so the listener only has to forward the click.
 * A screen also knows how to redraw itself in the other language and which screen came before it,
 * so the language flag and the back arrow work from anywhere.</p>
 */
public final class WikiMenu implements InventoryHolder {

    private static final int[] SHELVES = {20, 21, 22, 23, 24, 29, 30, 31, 32};
    private static final int[] LIST = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private static final int[] GRID = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int STATION = 23;
    private static final int RESULT = 25;
    private static final int HOW_TO = 16;
    private static final int[] USED_IN = {37, 38, 39, 40, 41, 42, 43};
    private static final int BACK = 45;
    private static final int CLOSE = 49;
    private static final int HOME = 53;
    private static final int LANGUAGE = 8;

    private final Inventory inventory;
    private final Map<Integer, Consumer<Player>> actions = new HashMap<>();
    private final WikiLang lang;

    private WikiMenu(WikiLang lang, Component title) {
        this.lang = lang;
        this.inventory = Bukkit.createInventory(this, 54, title);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    /** Runs what the slot does, if anything. */
    void click(Player player, int slot) {
        Consumer<Player> action = actions.get(slot);
        if (action != null) {
            action.accept(player);
        }
    }

    private static void open(Player player, WikiMenu menu) {
        player.openInventory(menu.getInventory());
    }

    // -------------------------------------------------------------------- home

    /** The home page: one shelf per category. */
    public static WikiMenu home(WikiLang lang) {
        WikiMenu menu = new WikiMenu(lang, WikiText.title(DARK_PURPLE, "✦ Multiverse Wiki ✦"));
        menu.frame();
        List<Component> intro = new ArrayList<>(WikiText.paragraph(GRAY, "", lang.pick(
                "Every item of MultiverseCreatures: how to craft it, who drops it and what it is used for.",
                "Todos los objetos de MultiverseCreatures: cómo fabricarlos, quién los suelta y para qué sirven.")));
        intro.add(Component.empty());
        intro.add(WikiText.line(YELLOW, lang.pick("Pick a shelf below.", "Elige una sección abajo.")));
        intro.add(WikiText.line(DARK_GRAY, lang.pick("Pages: ", "Páginas: ") + WikiCatalogue.entries().size()));
        menu.inventory.setItem(4, button(Material.KNOWLEDGE_BOOK, WikiText.title(GOLD, "MultiverseCreatures Wiki"), intro));
        WikiCategory[] categories = WikiCategory.values();
        for (int i = 0; i < categories.length && i < SHELVES.length; i++) {
            WikiCategory category = categories[i];
            List<Component> lore = new ArrayList<>(WikiText.paragraph(GRAY, "", category.blurb(lang)));
            lore.add(Component.empty());
            lore.add(WikiText.line(DARK_GRAY, WikiCatalogue.in(category).size() + " " + lang.pick("pages", "páginas")));
            lore.add(WikiText.line(YELLOW, lang.pick("▶ Click to open", "▶ Clic para abrir")));
            menu.inventory.setItem(SHELVES[i], button(category.icon(), WikiText.title(category.color(), category.name(lang)), lore));
            menu.actions.put(SHELVES[i], p -> open(p, category(lang, category, 0)));
        }
        menu.language(l -> home(l));
        menu.inventory.setItem(CLOSE, button(Material.BARRIER, WikiText.title(RED, lang.pick("Close", "Cerrar")), List.of()));
        menu.actions.put(CLOSE, Player::closeInventory);
        return menu;
    }

    // ---------------------------------------------------------------- category

    /** The pages of one shelf, 28 to a screen. */
    public static WikiMenu category(WikiLang lang, WikiCategory category, int page) {
        List<WikiEntry> entries = WikiCatalogue.in(category);
        int pages = Math.max(1, (entries.size() + LIST.length - 1) / LIST.length);
        int shown = Math.max(0, Math.min(pages - 1, page));
        Component title = WikiText.title(category.color(), category.name(lang))
                .append(WikiText.line(DARK_GRAY, pages > 1 ? "  " + (shown + 1) + "/" + pages : ""));
        WikiMenu menu = new WikiMenu(lang, title);
        menu.frame();
        menu.inventory.setItem(4, button(category.icon(), WikiText.title(category.color(), category.name(lang)),
                WikiText.paragraph(GRAY, "", category.blurb(lang))));
        FileConfiguration config = config();
        for (int i = 0; i < LIST.length; i++) {
            int index = shown * LIST.length + i;
            if (index >= entries.size()) {
                break;
            }
            WikiEntry entry = entries.get(index);
            ItemStack icon = entry.icon(lang);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                lore.add(Component.empty());
                lore.addAll(summary(entry, lang, config));
                lore.add(WikiText.line(YELLOW, lang.pick("▶ Click for the full page", "▶ Clic para ver la página")));
                meta.lore(lore);
                icon.setItemMeta(meta);
            }
            menu.inventory.setItem(LIST[i], icon);
            menu.actions.put(LIST[i], p -> open(p, page(lang, entry, 0, l -> category(l, category, shown))));
        }
        if (shown > 0) {
            menu.inventory.setItem(48, button(Material.ARROW, WikiText.title(YELLOW, lang.pick("« Previous page", "« Página anterior")), List.of()));
            menu.actions.put(48, p -> open(p, category(lang, category, shown - 1)));
        }
        if (shown < pages - 1) {
            menu.inventory.setItem(50, button(Material.ARROW, WikiText.title(YELLOW, lang.pick("Next page »", "Página siguiente »")), List.of()));
            menu.actions.put(50, p -> open(p, category(lang, category, shown + 1)));
        }
        menu.back(l -> home(l));
        menu.language(l -> category(l, category, shown));
        menu.closeAndHome(false);
        return menu;
    }

    /** One or two lines saying how an entry is obtained, for the lists. */
    private static List<Component> summary(WikiEntry entry, WikiLang lang, FileConfiguration config) {
        List<Component> lines = new ArrayList<>();
        List<String> stations = new ArrayList<>();
        for (WikiRecipe recipe : WikiRecipes.making(entry)) {
            String name = recipe.station().name(lang);
            if (!stations.contains(name)) {
                stations.add(name);
            }
        }
        if (!stations.isEmpty()) {
            lines.add(WikiText.line(AQUA, "⚒ " + String.join(", ", stations)));
        }
        if (entry.isItem()) {
            for (WikiSource source : entry.sources()) {
                String chance = source.chance(config);
                lines.add(WikiText.line(GREEN, "✦ " + source.title(lang) + (chance == null ? "" : " (" + chance + ")")));
            }
        }
        return lines;
    }

    // -------------------------------------------------------------------- page

    /**
     * One page: the item, a recipe (cycling through them when there are several), where it comes
     * from and what it is used in.
     *
     * @param back the screen the back arrow returns to, drawn in whatever language is current then
     */
    public static WikiMenu page(WikiLang lang, WikiEntry entry, int recipeIndex, Function<WikiLang, WikiMenu> back) {
        WikiMenu menu = new WikiMenu(lang, WikiText.title(entry.category().color(), "Wiki » ").append(entry.name(lang)));
        menu.frame();
        FileConfiguration config = config();

        ItemStack icon = entry.icon(lang);
        String about = entry.about(lang);
        if (about != null && entry.isItem()) {
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
                lore.add(Component.empty());
                lore.addAll(WikiText.paragraph(AQUA, "", about));
                meta.lore(lore);
                icon.setItemMeta(meta);
            }
        }
        menu.inventory.setItem(4, icon);

        List<WikiRecipe> recipes = WikiRecipes.making(entry);
        Function<WikiLang, WikiMenu> self = l -> page(l, entry, recipeIndex, back);
        if (!recipes.isEmpty()) {
            int shown = Math.floorMod(recipeIndex, recipes.size());
            WikiRecipe recipe = recipes.get(shown);
            for (int i = 0; i < GRID.length; i++) {
                ItemStack ingredient = recipe.grid().get(i);
                if (ingredient == null) {
                    menu.inventory.setItem(GRID[i], null);
                    continue;
                }
                WikiEntry target = WikiRecipes.pageOf(ingredient);
                ItemStack shownIngredient = ingredient.clone();
                if (target != null && target != entry) {
                    appendLore(shownIngredient, WikiText.line(YELLOW, lang.pick("▶ Click to open its page", "▶ Clic para abrir su página")));
                    menu.actions.put(GRID[i], p -> open(p, page(lang, target, 0, self)));
                }
                menu.inventory.setItem(GRID[i], shownIngredient);
            }
            List<Component> stationLore = new ArrayList<>();
            if (recipes.size() > 1) {
                stationLore.add(WikiText.line(GRAY, lang.pick("Recipe ", "Receta ") + (shown + 1) + "/" + recipes.size()));
                stationLore.add(WikiText.line(YELLOW, lang.pick("▶ Click for the next one", "▶ Clic para ver la siguiente")));
                menu.actions.put(STATION, p -> open(p, page(lang, entry, shown + 1, back)));
            }
            menu.inventory.setItem(STATION, button(recipe.station().icon(),
                    WikiText.title(AQUA, "→ " + recipe.station().name(lang)), stationLore));
            menu.inventory.setItem(RESULT, recipe.result().clone());
        } else {
            List<WikiSource> sources = entry.sources();
            int[] slots = spread(sources.size());
            for (int i = 0; i < slots.length; i++) {
                menu.inventory.setItem(slots[i], sourceIcon(sources.get(i), lang, config));
            }
            if (sources.isEmpty()) {
                menu.inventory.setItem(22, button(Material.PAPER, WikiText.title(GRAY,
                        lang.pick("Nothing to craft", "No se fabrica")), List.of()));
            }
        }
        if (!recipes.isEmpty() || entry.isItem()) {
            menu.inventory.setItem(HOW_TO, howTo(entry, recipes, lang, config));
        }

        List<WikiEntry> uses = WikiRecipes.usedIn(entry);
        if (!uses.isEmpty()) {
            menu.inventory.setItem(36, button(Material.CRAFTING_TABLE, WikiText.title(GOLD, lang.pick("Used in", "Se usa en")),
                    List.of(WikiText.line(GRAY, lang.pick("Click one to open its page.", "Haz clic en uno para abrir su página.")))));
            for (int i = 0; i < Math.min(USED_IN.length, uses.size()); i++) {
                WikiEntry use = uses.get(i);
                ItemStack shownUse = use.icon(lang);
                appendLore(shownUse, WikiText.line(YELLOW, lang.pick("▶ Click to open its page", "▶ Clic para abrir su página")));
                menu.inventory.setItem(USED_IN[i], shownUse);
                menu.actions.put(USED_IN[i], p -> open(p, page(lang, use, 0, self)));
            }
        }

        menu.back(back);
        menu.language(self);
        menu.closeAndHome(true);
        return menu;
    }

    /** The book that says, in words, every way to get the entry. */
    private static ItemStack howTo(WikiEntry entry, List<WikiRecipe> recipes, WikiLang lang, FileConfiguration config) {
        List<Component> lore = new ArrayList<>();
        if (!recipes.isEmpty()) {
            List<String> stations = new ArrayList<>();
            for (WikiRecipe recipe : recipes) {
                if (!stations.contains(recipe.station().name(lang))) {
                    stations.add(recipe.station().name(lang));
                }
            }
            lore.add(WikiText.line(AQUA, "⚒ " + String.join(", ", stations)));
            lore.addAll(WikiText.paragraph(GRAY, "  ", lang.pick(
                    "The recipe is on the left. Click an ingredient to open its own page.",
                    "La receta está a la izquierda. Haz clic en un ingrediente para abrir su página.")));
        }
        for (WikiSource source : entry.sources()) {
            if (!lore.isEmpty()) {
                lore.add(Component.empty());
            }
            String chance = source.chance(config);
            lore.add(WikiText.line(GREEN, "✦ " + source.title(lang) + (chance == null ? "" : " (" + chance + ")")));
            lore.addAll(WikiText.paragraph(GRAY, "  ", source.text(lang)));
        }
        if (lore.isEmpty()) {
            lore.addAll(WikiText.paragraph(GRAY, "", lang.pick(
                    "An admin can give it with /msc give.", "Un administrador puede darlo con /msc give.")));
        }
        return button(Material.WRITABLE_BOOK, WikiText.title(GOLD, lang.pick("How to get it", "Cómo conseguirlo")), lore);
    }

    private static ItemStack sourceIcon(WikiSource source, WikiLang lang, FileConfiguration config) {
        List<Component> lore = new ArrayList<>(WikiText.paragraph(GRAY, "", source.text(lang)));
        String chance = source.chance(config);
        if (chance != null) {
            lore.add(Component.empty());
            lore.add(WikiText.line(YELLOW, lang.pick("Chance: ", "Probabilidad: ") + chance));
        }
        return button(source.icon(), WikiText.title(GREEN, source.title(lang)), lore);
    }

    /** Where {@code count} source icons sit: centred on the middle rows of the screen. */
    static int[] spread(int count) {
        return switch (count) {
            case 0 -> new int[0];
            case 1 -> new int[]{22};
            case 2 -> new int[]{21, 23};
            case 3 -> new int[]{20, 22, 24};
            case 4 -> new int[]{19, 21, 23, 25};
            case 5 -> new int[]{20, 21, 22, 23, 24};
            default -> {
                int[] all = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
                int[] slots = new int[Math.min(count, all.length)];
                System.arraycopy(all, 0, slots, 0, slots.length);
                yield slots;
            }
        };
    }

    // ---------------------------------------------------------------- widgets

    private void frame() {
        ItemStack pane = filler();
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, pane);
            inventory.setItem(45 + i, pane);
        }
    }

    private void language(Function<WikiLang, WikiMenu> redraw) {
        WikiLang next = lang.other();
        inventory.setItem(LANGUAGE, button(Material.GLOBE_BANNER_PATTERN,
                WikiText.title(AQUA, lang.pick("Language: ", "Idioma: ") + lang.label()),
                List.of(WikiText.line(YELLOW, lang.pick("▶ Click: ", "▶ Clic: ") + next.label()))));
        actions.put(LANGUAGE, p -> {
            WikiLang.set(p, next);
            open(p, redraw.apply(next));
        });
    }

    private void back(Function<WikiLang, WikiMenu> to) {
        ItemStack arrow = button(Material.ARROW, WikiText.title(YELLOW, lang.pick("« Back", "« Volver")), List.of());
        inventory.setItem(0, arrow);
        inventory.setItem(BACK, arrow);
        Consumer<Player> go = p -> open(p, to.apply(WikiLang.of(p)));
        actions.put(0, go);
        actions.put(BACK, go);
    }

    private void closeAndHome(boolean withHome) {
        inventory.setItem(CLOSE, button(Material.BARRIER, WikiText.title(RED, lang.pick("Close", "Cerrar")), List.of()));
        actions.put(CLOSE, Player::closeInventory);
        if (withHome) {
            inventory.setItem(HOME, button(Material.KNOWLEDGE_BOOK, WikiText.title(GOLD, lang.pick("Wiki home", "Inicio de la wiki")), List.of()));
            actions.put(HOME, p -> open(p, home(WikiLang.of(p))));
        }
    }

    private static ItemStack filler() {
        return button(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), List.of());
    }

    static ItemStack button(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void appendLore(ItemStack item, Component line) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        List<Component> lore = meta.hasLore() && meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
        lore.add(Component.empty());
        lore.add(line);
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    private static FileConfiguration config() {
        org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("MultiverseCreatures");
        return plugin == null ? null : plugin.getConfig();
    }

    /** Used by tests and the listener to tell a wiki screen from any other inventory. */
    static boolean isWiki(InventoryHolder holder) {
        return holder instanceof WikiMenu;
    }
}
