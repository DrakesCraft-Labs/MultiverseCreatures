package com.Chagui68.ability;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.dio.DioStandHead;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FreezeAbility implements Listener {

    private final MultiverseCreatures plugin;
    private final Map<UUID, FreezeData> frozenPlayers = new HashMap<>();

    public FreezeAbility(MultiverseCreatures plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void freezeInArea(Location center, double radius, int durationTicks, String source) {
        new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (elapsed >= durationTicks) {
                    cancel();
                    return;
                }
                for (Player player : center.getWorld().getPlayers()) {
                    if (player.getLocation().distanceSquared(center) <= radius * radius) {
                        applyFreeze(player, source);
                    } else {
                        removeFreeze(player);
                    }
                }
                elapsed++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void freezePlayer(Player player, int durationTicks, String source) {
        applyFreeze(player, source);
        new BukkitRunnable() {
            @Override
            public void run() {
                removeFreeze(player);
            }
        }.runTaskLater(plugin, durationTicks);
    }

    private void applyFreeze(Player player, String source) {
        UUID id = player.getUniqueId();
        if (frozenPlayers.containsKey(id)) return;

        if (DioStandHead.isDioStandHead(player.getInventory().getItemInMainHand()) ||
                DioStandHead.isDioStandHead(player.getInventory().getItemInOffHand())) {
            return;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (DioStandHead.isDioStandHead(item)) {
                return;
            }
        }

        Location loc = player.getLocation();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 128, false, false));
        player.setAllowFlight(false);
        player.setFlying(false);

        frozenPlayers.put(id, new FreezeData(loc, source));
    }

    private void removeFreeze(Player player) {
        UUID id = player.getUniqueId();
        FreezeData data = frozenPlayers.remove(id);
        if (data == null) return;

        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.setFlying(false);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        FreezeData data = frozenPlayers.get(id);
        if (data == null) return;

        // Allow head rotation, block position change
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {

            event.setTo(data.originalLocation());
            event.getPlayer().setVelocity(new Vector(0, 0, 0));
        }
    }

    @EventHandler
    public void onFlightToggle(PlayerToggleFlightEvent event) {
        if (frozenPlayers.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    public boolean isFrozen(Player player) {
        return frozenPlayers.containsKey(player.getUniqueId());
    }

    public void forceUnfreeze(Player player) {
        removeFreeze(player);
    }

    private record FreezeData(Location originalLocation, String source) {
    }
}