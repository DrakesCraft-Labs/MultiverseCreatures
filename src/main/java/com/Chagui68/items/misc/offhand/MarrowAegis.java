package com.Chagui68.items.misc.offhand;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction;
import io.papermc.paper.datacomponent.item.blocksattacks.ItemDamageFunction;
import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import static net.kyori.adventure.text.format.NamedTextColor.*;
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
            meta.displayName(MscText.title(WHITE, "Marrow Aegis"));

            List<Component> lore = new ArrayList<>();
            lore.add(MscText.line(GRAY, "A shield carved from reinforced bone,"));
            lore.add(MscText.line(GRAY, "imbued with the marrowguard's resolve."));
            lore.add(MscText.blank());
            lore.add(MscText.line(WHITE, "Passive Effects:"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Blocking absorbs ", RED, "50% ", GRAY, "of incoming"));
            lore.add(MscText.rich(GRAY, "    damage and reflects the ", DARK_RED, "full hit back"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "On a successful block, grants ", GOLD, "Resistance II"));
            lore.add(MscText.rich(GRAY, "    and ", GOLD, "Strength I ", GRAY, "for ", GOLD, "7 seconds"));
            lore.add(MscText.rich(YELLOW, "  ▸ ", GRAY, "Effect cooldown: ", GOLD, "15 seconds"));
            lore.add(MscText.blank());
            lore.add(MscText.quote(DARK_PURPLE, "\"Death's architecture,"));
            lore.add(MscText.quote(DARK_PURPLE, "preserved in marrow.\""));
            lore.add(MscText.blank());
            lore.add(MscText.footer("Multiverse"));

            meta.lore(lore);
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
