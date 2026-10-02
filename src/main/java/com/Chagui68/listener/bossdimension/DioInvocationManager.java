package com.Chagui68.listener.bossdimension;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.LiveStage;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.ritual.BossDimensionManager;
import com.Chagui68.ritual.DioInvocationStructure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Summons DIO at The World's Throne, only inside the Boss Dimension: light the four yellow
 * candles and a golden clock face appears over the throne, ticking; drop a clock on the throne and
 * time stops for a moment before DIO steps out.
 */
public class DioInvocationManager implements Listener {

    private static final Color GOLD = Color.fromRGB(0xFFD23F);
    private static final Color GREEN = Color.fromRGB(0x3CCB5A);
    private static final Color FROZEN = Color.fromRGB(0x6E6A86);
    /** Ticks of stopped time between the offering and DIO's arrival. */
    private static final int ARRIVAL_DELAY = 50;

    private final MultiverseCreatures plugin;
    private final Map<UUID, InvocationData> activeInvocations = new HashMap<>();

    public DioInvocationManager(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    private boolean inBossWorld(World world) {
        BossDimensionManager dim = plugin.getBossDimensionManager();
        return dim != null && dim.getBossWorld() != null && world.equals(dim.getBossWorld());
    }

    @EventHandler
    public void onCandleLight(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.YELLOW_CANDLE) return;
        World world = block.getWorld();
        if (!inBossWorld(world)) return;
        if (plugin.getDioBoss() != null && plugin.getDioBoss().isBossActiveIn(world)) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Location candle = block.getLocation();
            for (int ox = -4; ox <= 0; ox++) {
                for (int oz = -4; oz <= 0; oz++) {
                    Location origin = candle.clone().add(ox, 0, oz);
                    if (!DioInvocationStructure.isStructureComplete(origin)) continue;
                    if (!DioInvocationStructure.containsCandle(origin, candle)) continue;
                    if (activeInvocations.containsKey(world.getUID())) continue;
                    if (!DioInvocationStructure.areAllCandlesLit(origin)) continue;
                    startInvocation(origin);
                    return;
                }
            }
        }, 5L);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() != Material.CLOCK) return;
        World world = event.getPlayer().getWorld();
        if (!inBossWorld(world)) return;
        InvocationData data = activeInvocations.get(world.getUID());
        if (data == null) return;

        Location throne = DioInvocationStructure.getThroneLocation(data.origin);
        Location dropped = event.getItemDrop().getLocation();
        if (dropped.distance(throne) > DioInvocationStructure.getRadius()) return;

        event.getItemDrop().remove();
        stopInvocation(world);
        DioInvocationStructure.extinguishAllCandles(data.origin);
        arrive(world, throne);
    }

    /** The offering: time stops around the throne, then DIO is simply there. */
    private void arrive(World world, Location throne) {
        Fx fx = LiveStage.fxIn(world);
        Vector center = throne.toVector().add(new Vector(0, 1.5, 0));
        fx.sound(center, Sfx.BEACON_DEACTIVATE, 3f, 0.5f);
        fx.sound(center, Sfx.WARDEN_SONIC_BOOM, 2f, 0.5f);
        fx.flash(center, Color.WHITE);
        fx.draw(Shapes.sphere(center, 6, 160), fx.dust(FROZEN, 2.2f));
        fx.draw(Shapes.sphere(center, 12, 260), fx.dust(FROZEN, 2.2f));
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(throne) > 60 * 60) continue;
            p.showTitle(Title.title(Component.text("ZA WARUDO!", NamedTextColor.GOLD, TextDecoration.BOLD),
                    Component.text("El tiempo se ha detenido...", NamedTextColor.GRAY),
                    Title.Times.times(Duration.ofMillis(100), Duration.ofMillis(2000), Duration.ofMillis(500))));
            if (p.getLocation().distanceSquared(throne) <= 20 * 20) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ARRIVAL_DELAY, 6, false, false, false));
            }
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            fx.sound(center, Sfx.BEACON_ACTIVATE, 3f, 0.6f);
            world.strikeLightningEffect(throne);
            fx.burst(center, Particle.END_ROD, 80, 0.4);
            if (plugin.getDioBoss() != null) plugin.getDioBoss().trySpawn(throne.clone());
        }, ARRIVAL_DELAY);
    }

    private void startInvocation(Location origin) {
        World world = origin.getWorld();
        if (world == null) return;
        Fx fx = LiveStage.fxIn(world);
        Location throne = DioInvocationStructure.getThroneLocation(origin);
        fx.sound(throne.toVector(), Sfx.BELL_RESONATE, 2f, 0.6f);
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(throne) > 40 * 40) continue;
            p.sendMessage(Component.text("⏱ El trono de The World despierta. Ofrece un reloj...", NamedTextColor.GOLD));
        }

        BukkitRunnable task = new BukkitRunnable() {
            private int tick;

            @Override
            public void run() {
                if (!DioInvocationStructure.areAllCandlesLit(origin) || !DioInvocationStructure.isStructureComplete(origin)) {
                    stopInvocation(world);
                    return;
                }
                tick += 2;
                Vector top = throne.toVector();
                for (Location pillar : DioInvocationStructure.getPillarTops(origin)) {
                    fx.line(pillar.toVector(), top.clone().add(new Vector(0, 0.2, 0)), 0.4,
                            fx.dust(tick % 8 < 4 ? GOLD : GREEN, 0.9f).sometimes(0.6));
                }
                // A clock face over the throne: twelve marks and a hand that moves once a second.
                Vector face = top.clone().add(new Vector(0, 2.2, 0));
                Vector u = new Vector(1, 0, 0);
                Vector v = new Vector(0, 1, 0);
                fx.draw(Shapes.circle(face, 1.4, 36, u, v, 0), fx.dust(GOLD, 1.0f));
                fx.draw(Shapes.circle(face, 1.15, 12, u, v, 0), fx.dust(GREEN, 1.2f));
                double hand = Math.PI / 2 - (tick / 20) * Math.PI / 6;
                fx.line(face, Shapes.onCircle(face, 1.0, hand, u, v), 0.12, fx.dust(Color.WHITE, 1.0f));
                fx.line(face, Shapes.onCircle(face, 0.6, Math.PI / 2 - tick / 240.0 * Math.PI / 6, u, v), 0.12,
                        fx.dust(GOLD, 1.2f));
                if (tick % 20 == 0) fx.sound(face, Sfx.NOTE_HAT, 1.2f, tick % 40 == 0 ? 1.2f : 0.9f);
                if (tick % 6 == 0) fx.cloud(Particle.END_ROD, top.clone().add(new Vector(0, 0.3, 0)), 1, 0.3, 0.01);
            }
        };
        task.runTaskTimer(plugin, 0L, 2L);
        activeInvocations.put(world.getUID(), new InvocationData(origin, task));
    }

    private void stopInvocation(World world) {
        InvocationData data = activeInvocations.remove(world.getUID());
        if (data != null && data.task != null) data.task.cancel();
    }

    private record InvocationData(Location origin, BukkitRunnable task) {
    }
}
