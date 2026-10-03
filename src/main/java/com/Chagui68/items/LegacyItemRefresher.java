package com.Chagui68.items;

import com.Chagui68.items.components.ArchitectKernel;
import com.Chagui68.items.components.ExecutionerEdge;
import com.Chagui68.items.components.ExecutionerWarrant;
import com.Chagui68.items.components.VampireBlood;
import com.Chagui68.items.potions.VampirePotions;
import com.Chagui68.items.weapons.melee.ExecutionerGuillotine;
import com.Chagui68.items.weapons.ranged.ArchitectDeployer;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Brings items made by an older release up to date with the current name and lore.
 *
 * <p>The items of the DIO, NIX and Jack Star arc were first written in Spanish. The ones already in
 * players' hands keep the old text, and the recipes compare ingredients exactly, so an old
 * Executioner's Edge would no longer forge the Guillotine. When a player joins or opens a container,
 * every such item gets the current name and lore; its enchantments, durability and the rest of
 * its data are left alone.</p>
 */
public final class LegacyItemRefresher implements Listener {

    private static final Map<NamespacedKey, Supplier<ItemStack>> TEMPLATES = new LinkedHashMap<>();

    static {
        TEMPLATES.put(VampireBlood.KEY, () -> VampireBlood.VAMPIRE_BLOOD);
        TEMPLATES.put(VampirePotions.UNSTABLE_KEY, () -> VampirePotions.UNSTABLE_BLOOD);
        TEMPLATES.put(VampirePotions.ELIXIR_KEY, () -> VampirePotions.BEARER_ELIXIR);
        TEMPLATES.put(ExecutionerEdge.KEY, () -> ExecutionerEdge.EXECUTIONER_EDGE);
        TEMPLATES.put(ExecutionerWarrant.KEY, () -> ExecutionerWarrant.EXECUTIONER_WARRANT);
        TEMPLATES.put(ExecutionerGuillotine.KEY, () -> ExecutionerGuillotine.EXECUTIONER_GUILLOTINE);
        TEMPLATES.put(ArchitectKernel.KEY, () -> ArchitectKernel.ARCHITECT_KERNEL);
        TEMPLATES.put(ArchitectDeployer.KEY, () -> ArchitectDeployer.ARCHITECT_DEPLOYER);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        refresh(player.getInventory());
        refresh(player.getEnderChest());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent event) {
        refresh(event.getInventory());
    }

    /** Updates every outdated item of the inventory in place; returns how many changed. */
    public static int refresh(Inventory inventory) {
        int changed = 0;
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (refresh(item)) {
                inventory.setItem(slot, item);
                changed++;
            }
        }
        return changed;
    }

    /** Gives one item the current name and lore when it is an outdated plugin item. */
    public static boolean refresh(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        for (Map.Entry<NamespacedKey, Supplier<ItemStack>> template : TEMPLATES.entrySet()) {
            if (!meta.getPersistentDataContainer().has(template.getKey())) {
                continue;
            }
            ItemMeta current = template.getValue().get().getItemMeta();
            if (current == null) {
                return false;
            }
            Component name = current.displayName();
            List<Component> lore = current.lore();
            if (Objects.equals(name, meta.displayName()) && Objects.equals(lore, meta.lore())) {
                return false;
            }
            meta.displayName(name);
            meta.lore(lore);
            item.setItemMeta(meta);
            return true;
        }
        return false;
    }
}
