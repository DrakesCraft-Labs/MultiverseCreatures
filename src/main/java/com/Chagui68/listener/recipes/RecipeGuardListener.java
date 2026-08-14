package com.Chagui68.listener.recipes;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Set;

public class RecipeGuardListener implements Listener {

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

    public static boolean isCustomItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        Set<NamespacedKey> keys = item.getItemMeta().getPersistentDataContainer().getKeys();
        for (NamespacedKey key : keys) {
            if (key.getNamespace().equals("multiversecreatures")) return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result != null && result.getType() != Material.AIR && isCustomItem(result)) return;

        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (isCustomItem(ingredient)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    /**
     * Only these two custom components may enter a furnace; everything else (custom OR
     * vanilla items of the same material) must never even start the smelting animation.
     */
    private boolean canSmeltInput(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return true;
        String key = getComponentKey(item);
        if (key != null) {
            return "msc_wheel_core".equals(key) || "msc_refined_netherite".equals(key);
        }
        // Vanilla items that would otherwise match the material-based custom recipes
        return item.getType() != Material.MUSIC_DISC_OTHERSIDE && item.getType() != Material.NETHERITE_INGOT;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        // The furnace only lights when a recipe matches the input, so refusing the
        // fuel burn for invalid inputs prevents the smelting animation from ever starting.
        if (event.getBlock().getState() instanceof Furnace furnace
                && !canSmeltInput(furnace.getInventory().getSmelting())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFurnaceClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof FurnaceInventory)) return;
        ItemStack attempted;
        if (event.getRawSlot() == 0) {
            // Direct click/cursor placed on the cooking slot
            attempted = event.getCursor();
        } else if (event.isShiftClick() && event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            // Shift-click from the player's own inventory, which would land in the
            // cooking slot even while the furnace is already lit (no burn event).
            attempted = event.getCurrentItem();
        } else {
            return;
        }
        if (!canSmeltInput(attempted)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHopperMove(InventoryMoveItemEvent event) {
        if (event.getDestination() instanceof FurnaceInventory && !canSmeltInput(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        ItemStack source = event.getSource();
        ItemStack result = event.getResult();
        
        // If result is custom, validate source has the CORRECT component key for that result
        if (isCustomItem(result)) {
            String resultKey = getComponentKey(result);
            String sourceKey = getComponentKey(source);
            
            // Define valid source→result pairs for material-based furnace recipes
            boolean validPair = switch (resultKey) {
                case "msc_molten_wheel_core" -> "msc_wheel_core".equals(sourceKey);
                case "msc_molten_netherite" -> "msc_refined_netherite".equals(sourceKey);
                default -> false;
            };
            
            if (!validPair) {
                event.setCancelled(true);
                return;
            }
        }
        
        // If source is custom AND result is NOT custom, cancel (protect vanilla recipes)
        if (isCustomItem(source) && !isCustomItem(result)) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Gets the mod's component key from an ItemStack, or null if none.
     */
    private String getComponentKey(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.getKeys().stream()
                .filter(key -> key.getNamespace().equals("multiversecreatures")
                        && COMPONENT_KEYS.contains(key.getKey()))
                .map(NamespacedKey::getKey)
                .findFirst()
                .orElse(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStonecutterClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.STONECUTTER) return;
        if (isCustomItem(event.getInventory().getItem(0))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        SmithingInventory inv = event.getInventory();
        if (isCustomItem(inv.getInputEquipment()) || isCustomItem(inv.getInputMineral())) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (isCustomItem(event.getInventory().getFirstItem()) ||
                isCustomItem(event.getInventory().getSecondItem())) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBrew(BrewEvent event) {
        if (isCustomItem(event.getContents().getIngredient())) {
            event.setCancelled(true);
        }
    }
}
