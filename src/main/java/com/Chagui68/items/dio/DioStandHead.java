package com.Chagui68.items.dio;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class DioStandHead {

    public static final NamespacedKey DIO_STAND_KEY = new NamespacedKey("multiversecreatures", "msc_dio_stand");

    private static final String STAND_TEXTURE = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDAxMTdkNDJhODdiMGNlZmE0NzhlMzAxODg4ZTcxNTQ4YTk5ODdmOTExMzQwYjVjMjAwMDIyOWU3MzM0Nzg3OSJ9fX0=";

    private static ItemStack cachedItem;

    public static ItemStack createItem() {
        if (cachedItem != null) return cachedItem.clone();

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "Dio_Stand");
            String json = new String(Base64.getDecoder().decode(STAND_TEXTURE));
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String url = obj.getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .get("url").getAsString();
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(url));
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[MultiverseCreatures] Failed to set Stand head texture: " + e.getMessage());
        }

        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Dio's Stand Head");
        meta.setAttributeModifiers(null);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "The manifestation of The World's power.");
        lore.add("");
        lore.add(ChatColor.WHITE + "Active Ability:");
        lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Right-Click: " + ChatColor.YELLOW + "THE WORLD: FREEZING");
        lore.add(ChatColor.GRAY + "  Freezes all nearby players.");
        lore.add("");
        lore.add(ChatColor.WHITE + "Passive Ability:");
        lore.add(ChatColor.YELLOW + "  ▸ " + ChatColor.GRAY + "Attack: " + ChatColor.YELLOW + "Stand Rush");
        lore.add(ChatColor.GRAY + "  Your Stand punches the target.");
        lore.add("");
        lore.add(ChatColor.GRAY + "Cooldown: " + ChatColor.RED + "2 minutes");
        lore.add("");
        lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "\"Za Warudo! Toki wo tomare!\"");
        lore.add(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "*Cannot be placed or worn as a helmet*");
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "✦ " + ChatColor.GRAY + "JoJo" + ChatColor.DARK_GRAY + " ✦");

        meta.setLore(lore);
        meta.getPersistentDataContainer().set(DIO_STAND_KEY, PersistentDataType.INTEGER, 1);
        meta.setCustomModelData(1001);
        meta.setUnbreakable(true);

        head.setItemMeta(meta);
        cachedItem = head.clone();
        return head;
    }

    public static ItemStack getHead() {
        return createItem();
    }

    public static boolean isDioStandHead(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(DIO_STAND_KEY, PersistentDataType.INTEGER);
    }
}
