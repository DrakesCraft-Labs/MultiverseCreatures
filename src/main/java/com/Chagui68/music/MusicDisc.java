package com.Chagui68.music;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import io.papermc.paper.datacomponent.DataComponentTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom music disc. Each disc is bound to one NBS song (stored in the
 * persistent data container) and its only function is to be used inside a
 * jukebox, exactly like a vanilla music disc.
 */
public final class MusicDisc {

    private static final NamespacedKey SONG_KEY = new NamespacedKey("multiversecreatures", "music_disc");

    private MusicDisc() {
    }

    public static ItemStack create(String songKey, MusicManager music) {
        String title = music.getSongTitle(songKey);
        ItemStack disc = new ItemStack(Material.MUSIC_DISC_13);
        ItemMeta meta = disc.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Music Disc" + ChatColor.WHITE + " - " + title);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Song: " + title);
        lore.add(ChatColor.DARK_GRAY + "Jukebox use only.");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(SONG_KEY, PersistentDataType.STRING, songKey.toLowerCase());
        disc.setItemMeta(meta);
        disc.unsetData(DataComponentTypes.JUKEBOX_PLAYABLE);
        return disc;
    }

    public static String getSongKey(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(SONG_KEY, PersistentDataType.STRING);
    }
}