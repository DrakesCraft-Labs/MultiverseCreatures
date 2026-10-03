package com.Chagui68.listener.bossdimension;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.LiveStage;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.integration.DrakesBossesIntegration;
import com.Chagui68.ritual.BossDimensionManager;
import com.Chagui68.ritual.PantheonAltarStructure;
import com.Chagui68.ritual.PantheonAltarStructure.Pantheon;
import com.Chagui68.ritual.PantheonGod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * Summons DrakesBosses' gods at a Pantheon Altar, only inside the Boss Dimension: light the altar's
 * four candles and its pantheon wakes; drop one of its gods' offerings on the core and that god
 * descends, spawned by DrakesBosses itself (stats, skills, loot and rewards stay theirs).
 *
 * <p>DrakesBosses is a soft dependency: without it the candles light and nothing answers. A god
 * counts as a boss fight for {@link BossFightGuard} while it lives, and one left alone with no
 * player near it for {@code drakes-bosses.no-player-despawn-seconds} is sent away so it cannot lock
 * the dimension for good.
 */
public class PantheonInvocationManager implements Listener {

    private static final String ENABLED_KEY = "drakes-bosses.enabled";
    private static final String ARRIVAL_DELAY_KEY = "drakes-bosses.arrival-delay-ticks";
    private static final String DESPAWN_KEY = "drakes-bosses.no-player-despawn-seconds";
    /** How near a player has to be for a god to count as being fought. */
    private static final double PRESENCE_RANGE = 64.0;
    /** Ticks between two checks of the summoned gods. */
    private static final long WATCH_PERIOD = 100L;

    private final MultiverseCreatures plugin;
    /** One open altar per world, like every other ritual of the dimension. */
    private final Map<UUID, InvocationData> activeInvocations = new HashMap<>();
    /** Worlds where an offering was accepted and the god is on its way. */
    private final Set<UUID> arriving = new HashSet<>();
    /** Summoned gods by entity id, with the last time a player was near them. */
    private final Map<UUID, Long> summonedGods = new HashMap<>();
    private BukkitTask watchdog;

    public PantheonInvocationManager(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    private boolean inBossWorld(World world) {
        BossDimensionManager dim = plugin.getBossDimensionManager();
        return dim != null && dim.getBossWorld() != null && world.equals(dim.getBossWorld());
    }

    private boolean enabled() {
        return plugin.getConfig().getBoolean(ENABLED_KEY, true);
    }

    /** Whether a summoned god is alive in {@code world}, or one is about to arrive there. */
    public boolean isBossActiveIn(World world) {
        if (arriving.contains(world.getUID())) return true;
        for (UUID id : summonedGods.keySet()) {
            Entity god = Bukkit.getEntity(id);
            if (god != null && god.isValid() && god.getWorld().equals(world)) return true;
        }
        return false;
    }

    @EventHandler
    public void onCandleLight(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        Pantheon pantheon = Pantheon.byCandle(block.getType());
        if (pantheon == null) return;
        World world = block.getWorld();
        if (!inBossWorld(world) || !enabled()) return;
        Player player = event.getPlayer();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Location candle = block.getLocation();
            for (int ox = -4; ox <= 0; ox++) {
                for (int oz = -4; oz <= 0; oz++) {
                    Location origin = candle.clone().add(ox, 0, oz);
                    if (!PantheonAltarStructure.isStructureComplete(origin, pantheon)) continue;
                    if (!PantheonAltarStructure.containsCandle(origin, candle)) continue;
                    if (activeInvocations.containsKey(world.getUID())) continue;
                    if (!PantheonAltarStructure.areAllCandlesLit(origin, pantheon)) continue;
                    if (!DrakesBossesIntegration.isAvailable()) {
                        player.sendMessage(Component.text("The gods of " + pantheon.displayName()
                                + " do not answer: DrakesBosses is not installed.", NamedTextColor.RED));
                        return;
                    }
                    if (plugin.isBossFightIn(world)) {
                        player.sendMessage(Component.text("The altar does not wake while another boss fights "
                                + "in the dimension.", NamedTextColor.RED));
                        return;
                    }
                    startInvocation(origin, pantheon);
                    return;
                }
            }
        }, 5L);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        World world = event.getPlayer().getWorld();
        if (!inBossWorld(world)) return;
        InvocationData data = activeInvocations.get(world.getUID());
        if (data == null) return;

        Item drop = event.getItemDrop();
        Location altar = PantheonAltarStructure.getAltarLocation(data.origin);
        if (drop.getLocation().distance(altar) > PantheonAltarStructure.getRadius()) return;
        PantheonGod god = PantheonGod.byOffering(drop.getItemStack().getType(), this::configuredOffering);
        if (god == null) return;

        Player player = event.getPlayer();
        if (god.pantheon() != data.pantheon) {
            player.sendMessage(Component.text(god.displayName() + " belongs to " + god.pantheon().displayName()
                    + ": this altar only listens to " + data.pantheon.displayName() + ".", NamedTextColor.RED));
            return;
        }

        consumeOne(drop);
        stopInvocation(world);
        PantheonAltarStructure.extinguishAllCandles(data.origin, data.pantheon);
        arrive(world, altar, god, player);
    }

    private String configuredOffering(String godId) {
        return plugin.getConfig().getString(PantheonGod.OFFERINGS_KEY + "." + godId);
    }

    /** Takes one item of the offering; the rest of a stack stays on the ground to be picked up. */
    private static void consumeOne(Item drop) {
        ItemStack stack = drop.getItemStack();
        if (stack.getAmount() <= 1) {
            drop.remove();
            return;
        }
        stack.setAmount(stack.getAmount() - 1);
        drop.setItemStack(stack);
    }

    /** The offering burns, the sky answers, and after a beat DrakesBosses spawns the god. */
    private void arrive(World world, Location altar, PantheonGod god, Player summoner) {
        Pantheon pantheon = god.pantheon();
        Fx fx = LiveStage.fxIn(world);
        Vector center = altar.toVector().add(new Vector(0, 1.2, 0));
        arriving.add(world.getUID());

        fx.sound(center, Sfx.BEACON_DEACTIVATE, 3f, 0.6f);
        fx.sound(center, Sfx.TRIAL_SPAWNER_OMINOUS, 3f, 0.7f);
        fx.gather(center, 6, 24, pantheon.primary(), 30);
        fx.draw(Shapes.sphere(center, 3, 120), fx.dust(pantheon.accent(), 1.8f));
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(altar) > 60 * 60) continue;
            p.showTitle(Title.title(
                    Component.text("✦ " + god.displayName() + " ✦", TextColor.color(pantheon.primary().asRGB()),
                            TextDecoration.BOLD),
                    Component.text(summoner.getName() + " has awakened a god of " + pantheon.displayName(),
                            NamedTextColor.GRAY),
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(2500), Duration.ofMillis(600))));
        }

        int delay = Math.max(1, Math.min(200, plugin.getConfig().getInt(ARRIVAL_DELAY_KEY, 40)));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            arriving.remove(world.getUID());
            LivingEntity boss = DrakesBossesIntegration.spawnBoss(god.id(), altar.clone().add(0, 0.2, 0));
            if (boss == null) {
                // The offering was not spent: give it back on the altar.
                world.dropItemNaturally(altar, new ItemStack(god.offering(this::configuredOffering)));
                summoner.sendMessage(Component.text("The offering to " + god.displayName()
                        + " faded without an answer (check the console).", NamedTextColor.RED));
                return;
            }
            world.strikeLightningEffect(altar);
            fx.sound(center, Sfx.BEACON_ACTIVATE, 3f, 0.6f);
            fx.flash(center, pantheon.primary());
            fx.burst(center, Particle.END_ROD, 80, 0.4);
            watch(boss);
            plugin.getLogger().info("[Pantheon] " + summoner.getName() + " summoned " + god.id()
                    + " in " + world.getName() + " at " + altar.toVector());
        }, delay);
    }

    private void startInvocation(Location origin, Pantheon pantheon) {
        World world = origin.getWorld();
        if (world == null) return;
        Fx fx = LiveStage.fxIn(world);
        Location altar = PantheonAltarStructure.getAltarLocation(origin);
        fx.sound(altar.toVector(), Sfx.BELL_RESONATE, 2f, 0.7f);
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(altar) > 40 * 40) continue;
            p.sendMessage(Component.text("✦ The altar of " + pantheon.displayName() + " awakens. Offer:",
                    TextColor.color(pantheon.primary().asRGB())));
            p.sendMessage(Component.text(offeringsOf(pantheon), NamedTextColor.GRAY));
        }

        BukkitRunnable task = new BukkitRunnable() {
            private int tick;

            @Override
            public void run() {
                if (!PantheonAltarStructure.areAllCandlesLit(origin, pantheon)
                        || !PantheonAltarStructure.isStructureComplete(origin, pantheon)) {
                    stopInvocation(world);
                    return;
                }
                tick += 2;
                Vector top = altar.toVector();
                Vector crown = top.clone().add(new Vector(0, 2.5, 0));
                for (Location pillar : PantheonAltarStructure.getPillarTops(origin)) {
                    fx.line(pillar.toVector(), crown, 0.4, fx.dust(pantheon.primary(), 0.9f).sometimes(0.5));
                }
                // A slowly turning star on the ground and a halo over the altar.
                Vector ground = top.clone().add(new Vector(0, -0.85, 0));
                double phase = tick / 40.0;
                fx.draw(Shapes.star(ground, 2.2, 5, 2, phase, 0.35, Shapes.FLAT_U, Shapes.FLAT_V),
                        fx.dust(pantheon.accent(), 0.8f).sometimes(0.6));
                fx.ring(crown, 0.9, 0.3, -phase, fx.dust(pantheon.primary(), 1.1f));
                if (tick % 6 == 0) fx.cloud(Particle.END_ROD, top.clone().add(new Vector(0, 0.3, 0)), 1, 0.3, 0.01);
                if (tick % 40 == 0) fx.sound(top, Sfx.WARDEN_HEARTBEAT, 1.2f, 0.8f);
            }
        };
        task.runTaskTimer(plugin, 0L, 2L);
        activeInvocations.put(world.getUID(), new InvocationData(origin, pantheon, task));
    }

    /** "Zeus: Lightning Rod · Poseidón: Heart Of The Sea · …", as the server has them configured. */
    private String offeringsOf(Pantheon pantheon) {
        StringJoiner list = new StringJoiner(" · ");
        for (PantheonGod god : PantheonGod.values()) {
            if (god.pantheon() != pantheon) continue;
            list.add(god.displayName() + ": " + itemName(god.offering(this::configuredOffering)));
        }
        return list.toString();
    }

    private static String itemName(Material material) {
        String[] words = material.name().toLowerCase(java.util.Locale.ROOT).split("_");
        StringJoiner name = new StringJoiner(" ");
        for (String word : words) name.add(Character.toUpperCase(word.charAt(0)) + word.substring(1));
        return name.toString();
    }

    private void stopInvocation(World world) {
        InvocationData data = activeInvocations.remove(world.getUID());
        if (data != null && data.task != null) data.task.cancel();
    }

    /** Starts tracking a summoned god, and the watchdog that sends forgotten ones away. */
    private void watch(LivingEntity god) {
        summonedGods.put(god.getUniqueId(), System.currentTimeMillis());
        if (watchdog == null) {
            watchdog = plugin.getServer().getScheduler().runTaskTimer(plugin, this::checkGods, WATCH_PERIOD, WATCH_PERIOD);
        }
    }

    private void checkGods() {
        long now = System.currentTimeMillis();
        long limit = Math.max(0, plugin.getConfig().getLong(DESPAWN_KEY, 300)) * 1000L;
        Iterator<Map.Entry<UUID, Long>> it = summonedGods.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            Entity god = Bukkit.getEntity(entry.getKey());
            if (god == null || !god.isValid()) {
                it.remove();
                continue;
            }
            if (hasPlayerNear(god)) {
                entry.setValue(now);
            } else if (limit > 0 && now - entry.getValue() >= limit) {
                // DrakesBosses notices the invalid entity on its next tick and clears its boss bar.
                god.remove();
                it.remove();
                plugin.getLogger().info("[Pantheon] A god with nobody left to fight was sent away from "
                        + god.getWorld().getName() + ".");
            }
        }
        if (summonedGods.isEmpty()) stopWatchdog();
    }

    private void stopWatchdog() {
        if (watchdog != null) {
            watchdog.cancel();
            watchdog = null;
        }
    }

    private static boolean hasPlayerNear(Entity god) {
        for (Player p : god.getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR || p.isDead()) continue;
            if (p.getLocation().distanceSquared(god.getLocation()) <= PRESENCE_RANGE * PRESENCE_RANGE) return true;
        }
        return false;
    }

    /** Stops the watchdog and every open altar; called on disable. */
    public void stopTasks() {
        stopWatchdog();
        for (InvocationData data : activeInvocations.values()) {
            if (data.task != null) data.task.cancel();
        }
        activeInvocations.clear();
    }

    private record InvocationData(Location origin, Pantheon pantheon, BukkitRunnable task) {
    }
}
