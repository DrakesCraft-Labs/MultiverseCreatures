package com.Chagui68.listener.bossdimension;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.ArchitectKernel;
import com.Chagui68.ritual.JackInvocationStructure;
import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class JackInvocationManager implements Listener {

    private final MultiverseCreatures plugin;
    private final Map<UUID, InvocationData> activeInvocations = new HashMap<>();
    private final Random random = new Random();

    private static final String[][] HACK_LOG_POOLS = {
            {
                    "[SYSTEM_AUDIT] Anomaly detected at physical address 0x%08X",
                    "[KERNEL_ALERT] Unexpected syscall 0x%X intercepted by PID 1",
                    "[SECURITY] SELinux policy overridden: Enforcing mode switched to PERMISSIVE",
                    "[BUFFER_OVERFLOW] Multiverse Core ring-0 stack corruption in module dm_crypt.ko",
                    "[ROOT_PRIVILEGE] UID 0 (root) claimed by sovereign daemon 'JACKSTAR'",
                    "[NETWORK_DAEMON] All iptables chains flushed. Port 4040 redirecting to /dev/multiverse",
                    "[AI_KERNEL] Neural tensor weights (405B) successfully mapped into memory space",
                    "[WATCHDOG] Primary daemon thread detached. JackStar is now PID 0."
            },
            {
                    "[DRAKES_DEFENSE] Zero-Day vulnerability exploited in packet decoder layer",
                    "[DMA_TRANSFER] Direct Memory Access established to world physics engine",
                    "[CPU_SCHEDULER] Real-time task priority (SCHED_FIFO 99) granted to entity 'JACK_AI'",
                    "[CONTAINER] Namespaces cgroup /sys/fs/cgroup/multiverse unconfined",
                    "[CRITICAL] Kernel page tables hijacked. Virtual memory mapped to reality",
                    "[SYSTEM] Daemon output: 'Did you think this server belonged to you? I AM THE SYSTEM.'",
                    "[HOST_TAKEOVER] Host control handover completed in 1.48ms"
            }
    };

    public JackInvocationManager(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCandleLight(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || !JackInvocationStructure.isValidCandle(block.getType())) return;

        World world = block.getWorld();
        if (plugin.getJackStarBoss() != null && plugin.getJackStarBoss().isBossActiveIn(world)) {
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Location candleLoc = block.getLocation();
            for (int ox = -4; ox <= 0; ox++) {
                for (int oz = -4; oz <= 0; oz++) {
                    Location origin = candleLoc.clone().add(ox, 0, oz);
                    if (!JackInvocationStructure.isStructureComplete(origin)) continue;
                    if (!JackInvocationStructure.containsCandle(origin, candleLoc)) continue;
                    if (activeInvocations.containsKey(origin.getWorld().getUID())) continue;
                    if (!JackInvocationStructure.areAllCandlesLit(origin)) continue;

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
        InvocationData data = activeInvocations.get(world.getUID());
        if (data == null) return;

        Location dropLoc = event.getItemDrop().getLocation();
        Location center = JackInvocationStructure.getCenterLocation(data.origin);
        double radius = JackInvocationStructure.getRadius();

        if (dropLoc.distance(center) > radius) return;
        if (Math.abs(dropLoc.getY() - data.origin.getY()) > 3) return;

        // Consume sacrificial offering
        event.getItemDrop().remove();

        Player player = event.getPlayer();
        player.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "⚡ [SYS] Terminal autenticada. Iniciando secuencia de hackeo del núcleo...");

        stopInvocation(world);
        JackInvocationStructure.extinguishAllCandles(data.origin);

        // Sequence of overload
        runOverloadClimaxSequence(world, data.origin, center);
    }

    private boolean isValidOffering(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        Material mat = item.getType();
        return mat == Material.NETHER_STAR
                || mat == Material.ECHO_SHARD
                || mat == Material.HEART_OF_THE_SEA
                || mat == Material.BEACON
                || (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(ArchitectKernel.KEY, org.bukkit.persistence.PersistentDataType.INTEGER));
    }

    private void startInvocation(Location origin) {
        World world = origin.getWorld();
        if (world == null) return;

        Location center = JackInvocationStructure.getCenterLocation(origin);
        world.playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.8f, 0.8f);

        BukkitRunnable task = new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (!JackInvocationStructure.areAllCandlesLit(origin)
                        || !JackInvocationStructure.isStructureComplete(origin)) {
                    stopInvocation(world);
                    return;
                }

                List<Location> rods = JackInvocationStructure.getCornerRodLocations(origin);

                // Streaming cyber/soul lines from corner rods to center core
                Particle.DustOptions cyanDust = new Particle.DustOptions(Color.fromRGB(0x00E5FF), 1.2f);
                for (Location rod : rods) {
                    drawParticleLine(world, rod, center, cyanDust, 10);
                    world.spawnParticle(Particle.ELECTRIC_SPARK, rod, 2, 0.1, 0.1, 0.1, 0.02);
                }

                // Core ascension particles
                world.spawnParticle(Particle.SOUL_FIRE_FLAME, center.clone().add(0, 0.5, 0), 4, 0.2, 0.3, 0.2, 0.01);
                world.spawnParticle(Particle.PORTAL, center.clone().add(0, 0.6, 0), 5, 0.25, 0.4, 0.25, 0.05);

                tick++;
                if (tick % 30 == 0) {
                    world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.8f, 1.2f);
                }
            }
        };
        task.runTaskTimer(plugin, 0L, 2L);
        activeInvocations.put(world.getUID(), new InvocationData(origin, task));
        plugin.getLogger().info("JackStar multiverse ritual active at " + origin);
    }

    private void stopInvocation(World world) {
        InvocationData data = activeInvocations.remove(world.getUID());
        if (data != null && data.task != null) {
            data.task.cancel();
        }
    }

    private void runOverloadClimaxSequence(World world, Location origin, Location center) {
        List<Location> rods = JackInvocationStructure.getCornerRodLocations(origin);

        new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                if (step < rods.size()) {
                    Location rod = rods.get(step);
                    world.strikeLightningEffect(rod);
                    world.playSound(rod, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 0.8f + (step * 0.2f));
                    world.spawnParticle(Particle.FLASH, rod, 1);
                } else if (step == rods.size()) {
                    // Deep sonic boom + center lightning
                    world.strikeLightningEffect(center);
                    world.playSound(center, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 0.6f);
                    world.playSound(center, Sound.ENTITY_WARDEN_ROAR, 1.8f, 0.7f);
                    world.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2.0f, 0.5f);

                    // Dynamic non-repetitive hack logs to console
                    emitDynamicHackLogs();

                    // Broadcast cinematic title
                    for (Player p : world.getPlayers()) {
                        if (p.getLocation().distanceSquared(center) <= 45 * 45) {
                            p.sendTitle(
                                    ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "JACKSTAR",
                                    ChatColor.AQUA + "El Arquitecto del Sistema — Control Absoluto",
                                    10, 60, 20
                            );
                            p.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA + "SYS" + ChatColor.DARK_GRAY + "] "
                                    + ChatColor.GRAY + "Identidad raíz verificada. " + ChatColor.AQUA + "JackStar ha tomado el control del nodo.");
                        }
                    }

                    // Spawn boss with a slight elevation
                    if (plugin.getJackStarBoss() != null) {
                        plugin.getJackStarBoss().trySpawn(center.clone().add(0, 0.2, 0));
                    }

                    cancel();
                }
                step++;
            }
        }.runTaskTimer(plugin, 4L, 8L);
    }

    private void emitDynamicHackLogs() {
        var logger = plugin.getLogger();
        logger.warning("================ [ MULTIVERSE SYSTEM BREACH ] ================");
        List<String> pool = new ArrayList<>();
        for (String[] group : HACK_LOG_POOLS) {
            Collections.addAll(pool, group);
        }
        Collections.shuffle(pool, random);

        int logsToEmit = 4 + random.nextInt(3);
        for (int i = 0; i < logsToEmit && i < pool.size(); i++) {
            String template = pool.get(i);
            String formatted = template.contains("%08X")
                    ? String.format(template, 0x10000000 + random.nextInt(0xEFFFFFFF))
                    : (template.contains("%X") ? String.format(template, random.nextInt(0xFFF)) : template);
            logger.warning("[ROOT_EXPLOIT] " + formatted);
        }
        logger.warning("==============================================================");
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
