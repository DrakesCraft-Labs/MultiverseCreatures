package com.Chagui68.listener.weapons.magic;

import com.Chagui68.items.weapons.magic.ChaosForge;
import com.Chagui68.items.components.ChaosOrb;
import com.Chagui68.items.components.CondensedChaosOrb;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChaosForgeHandler implements Listener {

    private static final NamespacedKey REFORGED_KEY = new NamespacedKey("multiversecreatures", "msc_chaos_reforged");

    private final Random random = new Random();

    private boolean isForge(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(ChaosForge.FORGE_KEY, PersistentDataType.INTEGER);
    }

    private boolean isOrb(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(ChaosOrb.KEY, PersistentDataType.INTEGER);
    }

    private boolean isCondensedOrb(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(CondensedChaosOrb.CONDENSED_CHAOS_ORB_KEY, PersistentDataType.INTEGER);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (event.getHand() == null) return;

        Player p = event.getPlayer();

        ItemStack mainItem = p.getInventory().getItemInMainHand();
        ItemStack offItem = p.getInventory().getItemInOffHand();

        boolean forgeInMain = isForge(mainItem);
        boolean forgeInOff = isForge(offItem);
        if (!forgeInMain && !forgeInOff) return;

        event.setCancelled(true);

        ItemStack targetItem = forgeInMain ? offItem : mainItem;

        if (targetItem == null || targetItem.getType() == Material.AIR) {
            p.sendMessage(ChatColor.RED + "Hold the item to reforge in your other hand.");
            return;
        }
        if (!targetItem.hasItemMeta()) {
            p.sendMessage(ChatColor.RED + "This item cannot be reforged.");
            return;
        }
        ItemMeta meta = targetItem.getItemMeta();
        if (meta.getEnchants().isEmpty()) {
            p.sendMessage(ChatColor.RED + "This item has no enchantments to reforge.");
            return;
        }
        boolean alreadyReforged = meta.getPersistentDataContainer().has(REFORGED_KEY, PersistentDataType.INTEGER);

        List<Enchantment> eligible = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            if (entry.getValue() < ChaosForge.MAX_ENCHANT_LEVEL) {
                eligible.add(entry.getKey());
            }
        }

        if (eligible.isEmpty()) {
            p.sendMessage(ChatColor.YELLOW + "All enchantments are already at maximum level (" + ChaosForge.MAX_ENCHANT_LEVEL + ").");
            return;
        }

        Enchantment selected = eligible.get(random.nextInt(eligible.size()));
        meta.addEnchant(selected, meta.getEnchantLevel(selected) + 1, true);

        if (hasCondensedOrb(p)) {
            if (!alreadyReforged) {
                p.sendMessage(ChatColor.RED + "This item must first be reforged with a Chaos Orb before using a Condensed Chaos Orb.");
                return;
            }
            consumeCondensedOrb(p);
        } else {
            if (alreadyReforged) {
                p.sendMessage(ChatColor.RED + "This item has already been reforged by the Chaos Forge. Use a Condensed Chaos Orb to reforge it again.");
                return;
            }
            if (!consumeOrb(p)) {
                p.sendMessage(ChatColor.RED + "You need a Chaos Orb (or a Condensed Chaos Orb) in your inventory.");
                return;
            }
            meta.getPersistentDataContainer().set(REFORGED_KEY, PersistentDataType.INTEGER, 1);
        }

        targetItem.setItemMeta(meta);

        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.2f, 0.8f);
        p.getWorld().spawnParticle(Particle.ENCHANT, p.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
        p.sendMessage(ChatColor.LIGHT_PURPLE + "The Chaos Forge twists the enchantments...");
    }

    private boolean hasCondensedOrb(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && isCondensedOrb(item)) {
                return true;
            }
        }
        return false;
    }

    private void consumeCondensedOrb(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && isCondensedOrb(item)) {
                item.setAmount(item.getAmount() - 1);
                return;
            }
        }
    }

    private boolean consumeOrb(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && isOrb(item)) {
                item.setAmount(item.getAmount() - 1);
                return true;
            }
        }
        return false;
    }
}