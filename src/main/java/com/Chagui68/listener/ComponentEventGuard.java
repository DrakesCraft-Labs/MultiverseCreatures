package com.Chagui68.listener;

import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Set;

/**
 * Keeps crafting components inert: they must never trigger vanilla behaviour
 * (jukebox discs, horse armour, furnace fuel, brewing, dispensers, item frames...).
 */
public class ComponentEventGuard implements Listener {

    private static final Set<String> COMPONENT_KEYS = Set.of(
            "msc_chaos_core", "msc_chaos_fragment", "msc_chaos_orb", "msc_chaos_powder",
            "msc_compressed_gold_block", "msc_condensed_chaos_orb", "msc_ender_core", "msc_ender_fragment",
            "msc_frost_heart", "msc_head_slime_heart", "msc_magma_core", "msc_military_component",
            "msc_molten_netherite", "msc_molten_wheel_core", "msc_multiversal_core", "msc_obsidian_shard",
            "msc_reaper_core", "msc_reaper_essence", "msc_refined_netherite", "msc_refined_wheel_core",
            "msc_reinforced_bone", "msc_reinforced_bone_block", "msc_sentinel_core", "msc_shadow_cloak",
            "msc_star_core", "msc_storm_crystal", "msc_sword_mold", "msc_venom_gland", "msc_void_essence",
            "msc_wheel_core", "msc_wheel_essence"
    );

    private boolean isComponent(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.getKeys().stream()
                .anyMatch(key -> key.getNamespace().equals("multiversecreatures")
                        && COMPONENT_KEYS.contains(key.getKey()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onJukeboxInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        ItemStack held = event.getItem();
        if (!isComponent(held)) return;

        if (event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
            event.setCancelled(true);
            return;
        }

        if (event.getClickedBlock() == null) return;
        Material block = event.getClickedBlock().getType();
        if (block == Material.JUKEBOX || block == Material.BEACON) {
            event.setCancelled(true);
            return;
        }

        Material type = held.getType();
        if (type == Material.ENDER_PEARL || type == Material.ENDER_EYE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBeaconClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.BEACON) return;
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        if (isComponent(cursor) || isComponent(current)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTotemResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (isComponent(player.getInventory().getItemInMainHand())
                || isComponent(player.getInventory().getItemInOffHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof AbstractHorse
                || event.getRightClicked() instanceof ItemFrame
                || event.getRightClicked() instanceof ArmorStand)) return;
        ItemStack held = event.getPlayer().getInventory().getItem(event.getHand());
        if (isComponent(held)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurnaceFuel(FurnaceBurnEvent event) {
        if (isComponent(event.getFuel())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBrew(BrewEvent event) {
        if (isComponent(event.getContents().getIngredient())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        if (isComponent(event.getItem())) {
            event.setCancelled(true);
        }
    }
}