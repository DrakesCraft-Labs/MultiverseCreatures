package com.Chagui68.wiki;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * {@code /wiki}: opens the in-game wiki.
 *
 * <pre>
 * /wiki              the home page
 * /wiki &lt;page&gt;       a page straight away, such as /wiki venomfang
 * /wiki hand         the page of the item in your hand
 * /wiki en|es        switch the language and open the home page
 * </pre>
 */
public final class WikiCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("The wiki can only be opened by a player.", NamedTextColor.RED));
            return true;
        }
        WikiLang lang = WikiLang.of(player);
        if (args.length == 0) {
            player.openInventory(WikiMenu.home(lang).getInventory());
            return true;
        }
        String wanted = args[0].toLowerCase(Locale.ROOT);
        if (wanted.equals("en") || wanted.equals("es")) {
            WikiLang chosen = wanted.equals("es") ? WikiLang.ES : WikiLang.EN;
            WikiLang.set(player, chosen);
            player.openInventory(WikiMenu.home(chosen).getInventory());
            return true;
        }
        WikiEntry entry = wanted.equals("hand")
                ? WikiRecipes.pageOf(player.getInventory().getItemInMainHand())
                : WikiCatalogue.byId(wanted.replace("_", "").replace("-", ""));
        if (entry == null) {
            entry = WikiCatalogue.byId(wanted);
        }
        if (entry == null) {
            player.sendMessage(Component.text(lang.pick("No wiki page called ", "No hay ninguna página llamada ")
                    + args[0] + ".", NamedTextColor.RED));
            return true;
        }
        WikiEntry found = entry;
        player.openInventory(WikiMenu.page(lang, found, 0, l -> WikiMenu.category(l, found.category(), 0))
                .getInventory());
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
                                      @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length != 1) {
            return out;
        }
        out.add("hand");
        out.add("en");
        out.add("es");
        for (WikiEntry entry : WikiCatalogue.entries()) {
            out.add(entry.id());
        }
        String typed = args[0].toLowerCase(Locale.ROOT);
        out.removeIf(option -> !option.startsWith(typed));
        return out;
    }
}
