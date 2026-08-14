package com.Chagui68.listener.misc;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.music.MusicDisc;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Makes custom MSC music discs work inside a jukebox exactly like vanilla
 * discs: insert with right-click, eject by right-clicking again with an
 * empty hand, and the NBS song plays (or stops) accordingly.
 *
 * The vanilla "13" sound is suppressed because MSC discs are created
 * without the "jukebox_playable" data component (see MusicDisc.create),
 * so the jukebox never starts vanilla playback and only the NBS song
 * is audible.
 */
public class DiscJukeboxHandler implements Listener {

    private final MultiverseCreatures plugin;
    private final Map<String, BukkitTask> playing = new HashMap<>();

    public DiscJukeboxHandler(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onJukeboxClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.JUKEBOX) return;
        if (!(block.getState() instanceof Jukebox jukebox)) return;

        ItemStack held = event.getItem();
        String songKey = MusicDisc.getSongKey(held);

        if (songKey != null) {
            if (event.getPlayer().isSneaking()) return;
            event.setCancelled(true);
            stop(block);
            if (!jukebox.hasRecord()) {
                jukebox.setRecord(MusicDisc.create(songKey, plugin.getMusicManager()));
                jukebox.update(true, false);
                Location center = block.getLocation().add(0.5, 0.5, 0.5);
                BukkitTask task = plugin.getMusicManager().playAt(songKey, center, 32, true);
                if (task != null) {
                    playing.put(blockKey(block), task);
                } else {
                    plugin.getLogger().warning("DiscJukeboxHandler: song not found for key '" + songKey + "'");
                }
            }
        } else if (held == null || held.getType() == Material.AIR) {
            ItemStack record = jukebox.getRecord();
            if (jukebox.hasRecord() && MusicDisc.getSongKey(record) != null) {
                event.setCancelled(true);
                stop(block);
                jukebox.setRecord(null);
                jukebox.update(true, false);
                block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), record);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onJukeboxBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.JUKEBOX) {
            stop(event.getBlock());
        }
    }

    public void stopAll() {
        for (BukkitTask task : playing.values()) {
            task.cancel();
        }
        playing.clear();
    }

    private void stop(Block block) {
        BukkitTask task = playing.remove(blockKey(block));
        if (task != null) task.cancel();
    }

    private String blockKey(Block block) {
        return block.getWorld().getName() + "|" + block.getX() + "," + block.getY() + "," + block.getZ();
    }
}