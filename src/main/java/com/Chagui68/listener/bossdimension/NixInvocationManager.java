package com.Chagui68.listener.bossdimension;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.ExecutionerWarrant;
import com.Chagui68.ritual.BossDimensionManager;
import com.Chagui68.ritual.NixInvocationStructure;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NixInvocationManager implements Listener {

    private final MultiverseCreatures plugin;
    private final Map<UUID, InvocationData> activeInvocations = new HashMap<>();

    public NixInvocationManager(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCandleLight(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.RED_CANDLE) return;

        World world = block.getWorld();
        BossDimensionManager dim = plugin.getBossDimensionManager();
        if (dim == null || dim.getBossWorld() == null || !world.equals(dim.getBossWorld())) return;

        // If Nix is already alive in this world, prevent restarting invocation
        if (plugin.getNixBoss() != null && plugin.getNixBoss().isBossActiveIn(world)) {
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Location candleLoc = block.getLocation();
            for (int ox = -4; ox <= 0; ox++) {
                for (int oz = -4; oz <= 0; oz++) {
                    Location origin = candleLoc.clone().add(ox, 0, oz);
                    if (!NixInvocationStructure.isStructureComplete(origin)) continue;
                    if (!NixInvocationStructure.containsCandle(origin, candleLoc)) continue;
                    if (activeInvocations.containsKey(origin.getWorld().getUID())) continue;
                    if (!NixInvocationStructure.areAllCandlesLit(origin)) continue;

                    startInvocation(origin);
                    return;
                }
            }
        }, 5L);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack dropped = event.getItemDrop().getItemStack();
        if (!isValidOffering(dropped)) return;

        World world = event.getPlayer().getWorld();
        BossDimensionManager dim = plugin.getBossDimensionManager();
        if (dim == null || dim.getBossWorld() == null || !world.equals(dim.getBossWorld())) return;

        InvocationData data = activeInvocations.get(world.getUID());
        if (data == null) return;

        Location dropLoc = event.getItemDrop().getLocation();
        Location anvilLoc = NixInvocationStructure.getAnvilLocation(data.origin);
        double radius = NixInvocationStructure.getRadius();

        if (dropLoc.distance(anvilLoc) > radius) return;
        if (Math.abs(dropLoc.getY() - data.origin.getY()) > 3) return;

        // Consume the offering
        event.getItemDrop().remove();

        Player player = event.getPlayer();
        player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "⛓ The death sentence is sealed in blood... NIX has arrived!");

        // Broadcast title to players in the boss dimension
        for (Player p : world.getPlayers()) {
            p.sendTitle(
                    ChatColor.DARK_RED + "" + ChatColor.BOLD + "NIX - The Executioner",
                    ChatColor.RED + "The sentence has been passed...",
                    10, 50, 20
            );
        }

        stopInvocation(world);
        NixInvocationStructure.extinguishAllCandles(data.origin);

        // Climax execution sounds and effects
        world.strikeLightningEffect(anvilLoc);
        world.playSound(anvilLoc, Sound.BLOCK_ANVIL_LAND, 2.0f, 0.4f);
        world.playSound(anvilLoc, Sound.ITEM_TRIDENT_THUNDER, 1.8f, 0.7f);
        world.playSound(anvilLoc, Sound.ENTITY_WITHER_SPAWN, 1.6f, 0.5f);
        world.playSound(anvilLoc, Sound.BLOCK_CHAIN_BREAK, 2.0f, 0.6f);

        world.spawnParticle(Particle.DUST, anvilLoc.clone().add(0, 1.0, 0), 120, 2.0, 2.5, 2.0, 0,
                new Particle.DustOptions(Color.fromRGB(0x8B0000), 2.5f));
        world.spawnParticle(Particle.SWEEP_ATTACK, anvilLoc.clone().add(0, 1.0, 0), 15, 1.5, 1.0, 1.5, 0);

        if (plugin.getNixBoss() != null) {
            plugin.getNixBoss().trySpawn(anvilLoc.clone().add(0, 0.5, 0));
        }
    }

    private boolean isValidOffering(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(ExecutionerWarrant.KEY, PersistentDataType.INTEGER)) {
            return true;
        }
        return item.getType() == Material.NETHERITE_AXE
                || item.getType() == Material.WITHER_SKELETON_SKULL;
    }

    private void startInvocation(Location origin) {
        World world = origin.getWorld();
        if (world == null) return;

        world.playSound(NixInvocationStructure.getAnvilLocation(origin), Sound.BLOCK_CHAIN_PLACE, 1.5f, 0.5f);

        BukkitRunnable task = new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (!NixInvocationStructure.areAllCandlesLit(origin)
                        || !NixInvocationStructure.isStructureComplete(origin)) {
                    stopInvocation(world);
                    return;
                }

                Location anvil = NixInvocationStructure.getAnvilLocation(origin);
                List<Location> gallows = NixInvocationStructure.getCornerGallowsLocations(origin);

                // Draw blood/chain particle lines connecting gallows to anvil
                Particle.DustOptions crimsonDust = new Particle.DustOptions(Color.fromRGB(0x8B0000), 1.2f);
                for (Location corner : gallows) {
                    drawParticleLine(world, corner, anvil, crimsonDust, 12);
                }

                // Dark smoke rising from anvil
                world.spawnParticle(Particle.SMOKE, anvil.clone().add(0, 0.5, 0), 4, 0.2, 0.3, 0.2, 0.02);
                world.spawnParticle(Particle.CRIMSON_SPORE, anvil.clone().add(0, 0.3, 0), 3, 0.3, 0.2, 0.3, 0.01);

                // Periodic ominous chain sounds
                tick++;
                if (tick % 25 == 0) {
                    world.playSound(anvil, Sound.BLOCK_CHAIN_PLACE, 0.9f, 0.6f);
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 2L);
        activeInvocations.put(world.getUID(), new InvocationData(origin, task));
        plugin.getLogger().info("Nix executioner invocation active at " + origin);
    }

    private void stopInvocation(World world) {
        InvocationData data = activeInvocations.remove(world.getUID());
        if (data != null && data.task != null) {
            data.task.cancel();
        }
    }

    private void drawParticleLine(World world, Location from, Location to, Particle.DustOptions dust, int samples) {
        Vector diff = to.toVector().subtract(from.toVector());
        for (int i = 0; i <= samples; i++) {
            double t = (double) i / samples;
            Location pt = from.clone().add(diff.clone().multiply(t));
            world.spawnParticle(Particle.DUST, pt, 1, 0, 0, 0, 0, dust);
        }
    }

    private static class InvocationData {
        final Location origin;
        final BukkitRunnable task;

        InvocationData(Location origin, BukkitRunnable task) {
            this.origin = origin;
            this.task = task;
        }
    }
}
