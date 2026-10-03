package com.Chagui68.stand;

import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * The menu Killer Queen's user picks the target of Sheer Heart Attack from: one head per
 * player online (except the user), 45 per page.
 */
public final class SheerHeartAttackMenu implements InventoryHolder {

    private static final int PER_PAGE = 45;
    static final int PREVIOUS = 45;
    static final int CLOSE = 49;
    static final int NEXT = 53;

    private final UUID owner;
    private final int page;
    private final Map<Integer, UUID> targets = new HashMap<>();
    private final Inventory inventory;

    public SheerHeartAttackMenu(Player owner, int page) {
        this.owner = owner.getUniqueId();
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        online.removeIf(player -> player.getUniqueId().equals(this.owner));
        online.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));
        int pages = Math.max(1, (online.size() + PER_PAGE - 1) / PER_PAGE);
        this.page = Math.max(0, Math.min(pages - 1, page));
        this.inventory = Bukkit.createInventory(this, 54,
                MscText.title(LIGHT_PURPLE, "☠ Sheer Heart Attack").append(MscText.line(DARK_GRAY,
                        "  " + (this.page + 1) + "/" + pages)));

        int slot = 0;
        for (int i = this.page * PER_PAGE; i < Math.min(online.size(), (this.page + 1) * PER_PAGE); i++) {
            Player target = online.get(i);
            inventory.setItem(slot, head(owner, target));
            targets.put(slot, target.getUniqueId());
            slot++;
        }
        if (online.isEmpty()) {
            inventory.setItem(22, button(Material.BARRIER, MscText.title(RED, "Nobody else is online"),
                    MscText.line(GRAY, "Sheer Heart Attack needs a target.")));
        }
        ItemStack filler = button(Material.PINK_STAINED_GLASS_PANE, Component.text(" "));
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, filler);
        }
        if (this.page > 0) {
            inventory.setItem(PREVIOUS, button(Material.ARROW, MscText.title(YELLOW, "« Previous page")));
        }
        if (this.page < pages - 1) {
            inventory.setItem(NEXT, button(Material.ARROW, MscText.title(YELLOW, "Next page »")));
        }
        inventory.setItem(CLOSE, button(Material.BARRIER, MscText.title(RED, "Close")));
    }

    private static ItemStack head(Player owner, Player target) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (head.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(target);
            meta.displayName(MscText.title(LIGHT_PURPLE, target.getName()));
            List<Component> lore = new ArrayList<>();
            if (target.getWorld().equals(owner.getWorld())) {
                lore.add(MscText.rich(GRAY, "Distance: ", GOLD,
                        (int) target.getLocation().distance(owner.getLocation()) + " blocks"));
            } else {
                lore.add(MscText.rich(GRAY, "World: ", GOLD, target.getWorld().getName()));
            }
            lore.add(MscText.blank());
            lore.add(MscText.line(GRAY, "Sheer Heart Attack will roll after them,"));
            lore.add(MscText.line(GRAY, "through whatever stands in the way."));
            lore.add(MscText.blank());
            lore.add(MscText.line(YELLOW, "Click to send the bomb"));
            meta.lore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    private static ItemStack button(Material material, Component name, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name);
            meta.lore(List.of(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public UUID owner() {
        return owner;
    }

    public int page() {
        return page;
    }

    /** The player a slot points at, or null. */
    public UUID target(int slot) {
        return targets.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
