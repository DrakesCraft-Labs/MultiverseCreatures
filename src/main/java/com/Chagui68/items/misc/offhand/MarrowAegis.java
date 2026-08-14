package com.Chagui68.items.misc.offhand;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction;
import io.papermc.paper.datacomponent.item.blocksattacks.ItemDamageFunction;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class MarrowAegis {

    public static final NamespacedKey MARROW_KEY = new NamespacedKey("multiversecreatures", "msc_marrow_aegis");
    public static final ItemStack MARROW_AEGIS = new ItemStack(Material.SHIELD);

    public static final double REFLECT_FRACTION = 0.5;
    /**
     * Fraction of the incoming hit that the aegis blocks. Kept below 1.0 on
     * purpose: a fully-blocked hit skips the vanilla damage tick on modern
     * Paper (no EntityDamageEvent fires), so the reflect would never trigger.
     */
    public static final float BLOCK_FRACTION = 0.5f;
    public static final long RECHARGE_COOLDOWN_MS = 15000L;
    public static final int EFFECT_DURATION_TICKS = 140;

    public static final String RECHARGE_KEY = "msc_marrow_aegis_until";

    static {
        ItemMeta meta = MARROW_AEGIS.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "" + ChatColor.BOLD + "Marrow Aegis");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A shield carved from reinforced bone,");
            lore.add(ChatColor.GRAY + "imbued with the marrowguard's resolve.");
            lore.add("");
            lore.add(ChatColor.WHITE + "Passive Effects:");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Blocking absorbs " + ChatColor.RED + "50% " + ChatColor.GRAY + "of incoming");
            lore.add(ChatColor.GRAY + "    damage and reflects the " + ChatColor.DARK_RED + "full hit back");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "On a successful block, grants " + ChatColor.GOLD + "Resistance II");
            lore.add(ChatColor.GRAY + "    and " + ChatColor.GOLD + "Strength I " + ChatColor.GRAY + "for " + ChatColor.GOLD + "7 seconds");
            lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Effect cooldown: " + ChatColor.GOLD + "15 seconds");
            lore.add("");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Death's architecture,");
            lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "preserved in marrow.\"");
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "Multiverse" + ChatColor.DARK_GRAY + " ✦");

            meta.setLore(lore);
            meta.getPersistentDataContainer().set(MARROW_KEY, PersistentDataType.INTEGER, 1);
            meta.setUnbreakable(true);
            MARROW_AEGIS.setItemMeta(meta);

            BlocksAttacks vanilla = Material.SHIELD.getDefaultData(DataComponentTypes.BLOCKS_ATTACKS);
            if (vanilla != null) {
                BlocksAttacks custom = BlocksAttacks.blocksAttacks()
                        .blockDelaySeconds(vanilla.blockDelaySeconds())
                        .disableCooldownScale(vanilla.disableCooldownScale())
                        .damageReductions(List.of(DamageReduction.damageReduction()
                                .horizontalBlockingAngle(180f)
                                .base(0f)
                                .factor(BLOCK_FRACTION)
                                .build()))
                        .itemDamage(ItemDamageFunction.itemDamageFunction()
                                .threshold(0f)
                                .base(0f)
                                .factor(0f)
                                .build())
                        .bypassedBy(vanilla.bypassedBy())
                        .blockSound(vanilla.blockSound())
                        .disableSound(vanilla.disableSound())
                        .build();
                MARROW_AEGIS.setData(DataComponentTypes.BLOCKS_ATTACKS, custom);
            }
        }
    }
}
