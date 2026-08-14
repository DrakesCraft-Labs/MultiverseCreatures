package com.Chagui68.listener.ritual;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Candle;
import com.Chagui68.MultiverseCreatures;
import com.Chagui68.ritual.RitualStructure;

public class RitualCandleListener implements Listener {

    private final MultiverseCreatures plugin;

    public RitualCandleListener(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCandleLight(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getType() != Material.CANDLE && block.getType() != Material.CANDLE_CAKE) {
            return;
        }

        Candle candleData = (Candle) block.getBlockData();

        if (candleData.isLit()) {
            return;
        }

        if (event.getItem() == null) {
            return;
        }

        Material itemInHand = event.getItem().getType();
        if (itemInHand != Material.FLINT_AND_STEEL &&
                itemInHand != Material.FIRE_CHARGE &&
                !itemInHand.name().contains("CANDLE")) {
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Block updatedBlock = block.getLocation().getBlock();
            if (updatedBlock.getType() == Material.CANDLE || updatedBlock.getType() == Material.CANDLE_CAKE) {
                Candle updatedCandle = (Candle) updatedBlock.getBlockData();
                if (updatedCandle.isLit()) {
                    checkRitualCompletion(updatedBlock.getLocation());
                }
            }
        }, 5L);
    }

    private void checkRitualCompletion(org.bukkit.Location candleLoc) {
        java.util.Map<org.bukkit.Location, Material> candleLocations =
                RitualStructure.getCandleLocations();

        for (org.bukkit.Location rel : candleLocations.keySet()) {
            int relX = rel.getBlockX();
            int relY = rel.getBlockY();
            int relZ = rel.getBlockZ();

            int startX = candleLoc.getBlockX() - relX;
            int startY = candleLoc.getBlockY() - relY;
            int startZ = candleLoc.getBlockZ() - relZ;

            org.bukkit.Location origin = new org.bukkit.Location(
                    candleLoc.getWorld(),
                    startX,
                    startY,
                    startZ
            );

            if (RitualStructure.isStructureComplete(origin)) {
                com.Chagui68.ritual.RitualManager ritualManager = plugin.getRitualManager();
                if (ritualManager != null) {
                    ritualManager.checkAndStartRitual(origin);
                }
                return;
            }
        }
    }
}