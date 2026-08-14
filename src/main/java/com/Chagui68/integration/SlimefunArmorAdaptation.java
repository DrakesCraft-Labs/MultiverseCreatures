package com.Chagui68.integration;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.Set;

/**
 * Soft dependency bridge to the Slimefun ecosystem (Slimefun, InfinityExpansion,
 * SlimeTinker). No compile-time dependency: item identification is done through
 * the persistent data keys those plugins write on their items, so this class
 * degrades gracefully (returns 0) when the plugins are not installed.
 *
 * - Slimefun items: PDC key "slimefun:slimefun_item" holds the item id.
 * - Tinker armour: PDC key "slimetinker:ST_Armour" = "Y" marks tinkered armour;
 *   "slimetinker:ST_Material_Plate" holds the plate material id.
 */
public final class SlimefunArmorAdaptation {

    private static final boolean TINKER_ENABLED = Bukkit.getPluginManager().isPluginEnabled("SlimeTinker");
    private static final boolean INFINITY_ENABLED = Bukkit.getPluginManager().isPluginEnabled("InfinityExpansion");

    private static final NamespacedKey SF_ITEM_ID = new NamespacedKey("slimefun", "slimefun_item");
    private static final NamespacedKey ST_IS_ARMOUR = new NamespacedKey("slimetinker", "ST_Armour");
    private static final NamespacedKey ST_PLATE = new NamespacedKey("slimetinker", "ST_Material_Plate");
    private static final NamespacedKey ST_LINKS = new NamespacedKey("slimetinker", "ST_Material_Links");
    private static final NamespacedKey ST_MODS = new NamespacedKey("slimetinker", "ST_Modifier_Map");

    private static final double TINKER_TIER_1 = 0.3;
    private static final double TINKER_TIER_2 = 0.6;
    private static final double TINKER_TIER_3 = 1.4;
    private static final double TINKER_TIER_4 = 2.2;
    private static final double TINKER_TIER_5 = 3.0;
    private static final double TINKER_UNKNOWN = 0.6;
    private static final double GENERIC_SF_ARMOR = 0.5;
    private static final double INFINITY_ARMOR = 3.0;

    private static final Set<String> INFINITY_ARMOR_IDS = Set.of(
            "INFINITY_CROWN", "INFINITY_CHESTPLATE", "INFINITY_LEGGINGS", "INFINITY_BOOTS"
    );

    private static final Map<String, Double> TINKER_TIERS = Map.ofEntries(
            // Soft / organic materials
            Map.entry("LEATHER", TINKER_TIER_1), Map.entry("STRING", TINKER_TIER_1),
            Map.entry("VINE", TINKER_TIER_1), Map.entry("CRIMSON_ROOTS", TINKER_TIER_1),
            Map.entry("WARPED_ROOTS", TINKER_TIER_1), Map.entry("WEEPING_VINES", TINKER_TIER_1),
            Map.entry("TWISTING_VINES", TINKER_TIER_1), Map.entry("SLIME", TINKER_TIER_1),
            Map.entry("REDSTONE", TINKER_TIER_1), Map.entry("SILICON", TINKER_TIER_1),
            Map.entry("RUBBER", TINKER_TIER_1), Map.entry("COAL", TINKER_TIER_1),
            Map.entry("SCRAP", TINKER_TIER_1), Map.entry("CARBON_MESH", TINKER_TIER_1),
            // Base metals
            Map.entry("IRON", TINKER_TIER_2), Map.entry("GOLD", TINKER_TIER_2),
            Map.entry("COPPER", TINKER_TIER_2), Map.entry("LEAD", TINKER_TIER_2),
            Map.entry("SILVER", TINKER_TIER_2), Map.entry("ALUMINUM", TINKER_TIER_2),
            Map.entry("TIN", TINKER_TIER_2), Map.entry("ZINC", TINKER_TIER_2),
            Map.entry("MAGNESIUM", TINKER_TIER_2), Map.entry("STEEL", TINKER_TIER_2),
            Map.entry("REFINED_IRON", TINKER_TIER_2), Map.entry("MIXED_METAL", TINKER_TIER_2),
            Map.entry("STAINLESS_STEEL", TINKER_TIER_2), Map.entry("FERROSILICON", TINKER_TIER_2),
            Map.entry("BRONZE", TINKER_TIER_2), Map.entry("BILLON", TINKER_TIER_2),
            Map.entry("BRASS", TINKER_TIER_2), Map.entry("ALUMINUM_BRASS", TINKER_TIER_2),
            Map.entry("NICKEL", TINKER_TIER_2), Map.entry("SOLDER", TINKER_TIER_2),
            // High-grade alloys & gems
            Map.entry("DIAMOND", TINKER_TIER_3), Map.entry("COBALT", TINKER_TIER_3),
            Map.entry("DURALUMIN", TINKER_TIER_3), Map.entry("ALUMINUM_BRONZE", TINKER_TIER_3),
            Map.entry("CORINTHIAN_BRONZE", TINKER_TIER_3), Map.entry("HARDENED_METAL", TINKER_TIER_3),
            Map.entry("REDSTONE_ALLOY", TINKER_TIER_3), Map.entry("MAGSTEEL", TINKER_TIER_3),
            Map.entry("TITANIUM", TINKER_TIER_3), Map.entry("MYTHRIL", TINKER_TIER_3),
            Map.entry("ADAMANTITE", TINKER_TIER_3), Map.entry("THORIUM", TINKER_TIER_3),
            Map.entry("MAG_THOR", TINKER_TIER_3), Map.entry("IRIDIUM", TINKER_TIER_3),
            Map.entry("VEX_GEM", TINKER_TIER_3), Map.entry("STARDUST", TINKER_TIER_3),
            Map.entry("GHOSTLY_ESSENCE", TINKER_TIER_3), Map.entry("TESSERACT", TINKER_TIER_3),
            Map.entry("OSMIUM", TINKER_TIER_3), Map.entry("SLIMESTEEL", TINKER_TIER_3),
            Map.entry("SEGGANESSON", TINKER_TIER_3), Map.entry("DAXI_STRENGTH", TINKER_TIER_3),
            Map.entry("DAXI_ABSORPTION", TINKER_TIER_3), Map.entry("DAXI_FORTITUDE", TINKER_TIER_3),
            Map.entry("DAXI_SATURATION", TINKER_TIER_3), Map.entry("DAXI_REGENERATION", TINKER_TIER_3),
            // Endgame alloys & rare materials
            Map.entry("DAMASCUS_STEEL", TINKER_TIER_4), Map.entry("REINFORCED_ALLOY", TINKER_TIER_4),
            Map.entry("ADVANCED_ALLOY", TINKER_TIER_4), Map.entry("REINFORCED_SLIMESTEEL", TINKER_TIER_4),
            Map.entry("OSMIUM_SUPERALLOY", TINKER_TIER_4), Map.entry("UNPATENTABLIUM", TINKER_TIER_4),
            Map.entry("VOID", TINKER_TIER_4), Map.entry("MAGNONIUM", TINKER_TIER_4),
            Map.entry("DRACONIC", TINKER_TIER_4), Map.entry("REINFORCED_DRACONIUM", TINKER_TIER_4),
            Map.entry("BOOMERITE", TINKER_TIER_4), Map.entry("SEFIRITE", TINKER_TIER_4),
            Map.entry("CRINGLEIUM", TINKER_TIER_4), Map.entry("NICEINIUM", TINKER_TIER_4),
            Map.entry("SMITHIUM", TINKER_TIER_4), Map.entry("ANNIVERSARIUM", TINKER_TIER_4),
            Map.entry("MOLTEN_PRESENCE", TINKER_TIER_4), Map.entry("REMOTININIUM", TINKER_TIER_4),
            Map.entry("ULTIMANINIUM", TINKER_TIER_4), Map.entry("LIQUID_CHRISTMAS", TINKER_TIER_4),
            Map.entry("FLOWING_FONDNESS", TINKER_TIER_4), Map.entry("DETAILED_DEVOTION", TINKER_TIER_4),
            Map.entry("PURIFIED_PASSION", TINKER_TIER_4), Map.entry("LIQUID_LOVE", TINKER_TIER_4),
            // Singularities & Infinity materials
            Map.entry("IRON_SINGULARITY", TINKER_TIER_5), Map.entry("GOLD_SINGULARITY", TINKER_TIER_5),
            Map.entry("COPPER_SINGULARITY", TINKER_TIER_5), Map.entry("LEAD_SINGULARITY", TINKER_TIER_5),
            Map.entry("SILVER_SINGULARITY", TINKER_TIER_5), Map.entry("ALUMINUM_SINGULARITY", TINKER_TIER_5),
            Map.entry("TIN_SINGULARITY", TINKER_TIER_5), Map.entry("ZINC_SINGULARITY", TINKER_TIER_5),
            Map.entry("MAGNESIUM_SINGULARITY", TINKER_TIER_5), Map.entry("DIAMOND_SINGULARITY", TINKER_TIER_5),
            Map.entry("FORTUNE_SINGULARITY", TINKER_TIER_5), Map.entry("MAGIC_SINGULARITY", TINKER_TIER_5),
            Map.entry("EARTH_SINGULARITY", TINKER_TIER_5), Map.entry("METAL_SINGULARITY", TINKER_TIER_5),
            Map.entry("INFINITY", TINKER_TIER_5), Map.entry("INFINITY_SINGULARITY", TINKER_TIER_5)
    );

    private SlimefunArmorAdaptation() {
    }

    /**
     * Bonus for Mahoraga's adaptation, based on the Slimefun/Tinker armour type
     * worn by the player. Returns 0 when the armour (or the plugins) are absent.
     */
    public static double getBonus(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (TINKER_ENABLED && "Y".equals(pdc.get(ST_IS_ARMOUR, PersistentDataType.STRING))) {
            return tinkerTier(pdc.get(ST_PLATE, PersistentDataType.STRING));
        }

        String sfId = pdc.get(SF_ITEM_ID, PersistentDataType.STRING);
        if (sfId != null) {
            if (INFINITY_ENABLED && INFINITY_ARMOR_IDS.contains(sfId)) return INFINITY_ARMOR;
            if (isArmorMaterial(item.getType())) return GENERIC_SF_ARMOR;
        }
        return 0;
    }

    private static double tinkerTier(String plate) {
        if (plate == null) return TINKER_UNKNOWN;
        Double tier = TINKER_TIERS.get(plate);
        return tier != null ? tier : TINKER_UNKNOWN;
    }

    /**
     * True when the item holds the Tinker Diamond modification (reflects and
     * cancels incoming damage). The tool mod map is a single int array under
     * ST_Modifier_Map, indexed by SlimeTinker's tool order: REDSTONE, LAPIS,
     * QUARTZ, DIAMOND, EMERALD, MOD_PLATE.
     */
    public static boolean hasDiamondMod(ItemStack item) {
        if (!TINKER_ENABLED) return false;
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(ST_MODS, PersistentDataType.INTEGER_ARRAY)) return false;
        int[] mods = pdc.get(ST_MODS, PersistentDataType.INTEGER_ARRAY);
        if (mods == null || mods.length <= 3) return false;
        return mods[3] > 0;
    }

    private static boolean isArmorMaterial(org.bukkit.Material material) {
        String name = material.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }

    /**
     * True when the player is wearing the full Infinity Singularity Mail Links
     * set (4 pieces) — the Tinker trait that forces all incoming damage to 1
     * ("Infinite Defence").
     */
    public static boolean isInfinitySingularityLinksSet(Player player) {
        if (!TINKER_ENABLED) return false;
        int pieces = 0;
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null || !armor.hasItemMeta()) continue;
            PersistentDataContainer pdc = armor.getItemMeta().getPersistentDataContainer();
            if (!"Y".equals(pdc.get(ST_IS_ARMOUR, PersistentDataType.STRING))) continue;
            if ("INFINITY_SINGULARITY".equals(pdc.get(ST_LINKS, PersistentDataType.STRING))) {
                pieces++;
            }
        }
        return pieces >= 4;
    }
}
