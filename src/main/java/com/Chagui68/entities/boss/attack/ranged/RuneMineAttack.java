package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Rune Mines — the boss scatters armed runes across the floor around his target.
 *
 * <p>ANIMATION (what tells it apart on screen)
 * <ul>
 *   <li>0-17: he crouches and sweeps one arm low across the ground while a runic triangle is drawn
 *       under him — the casting stance is a low sweep, not a raised-hand charge.</li>
 *   <li>18: six runes are flung out and land scattered around the target. They spend a second arming
 *       (grey, flat) before they turn violet and start pulsing: the only attack that leaves a
 *       <b>field of triggers</b> the players have to walk around.</li>
 *   <li>Each rune bursts upward when stepped on, and fades out on its own if nobody takes the bait.</li>
 * </ul>
 */
public class RuneMineAttack extends BossAttackBase {

    private static final int WIND_UP_TICKS = 18;
    private static final int FLIGHT_TICKS = 9;
    private static final int ARM_TICKS = 25;
    private static final int MINE_LIFE = 140;
    private static final int MINES = 6;
    private static final double SCATTER = 4.0;
    private static final double TRIGGER_RADIUS = 1.5;
    private static final double BLAST_RADIUS = 3.2;

    private final double damage;

    public RuneMineAttack(BossHost boss) {
        super(boss);
        this.damage = plugin.getConfig().getDouble("entities.armor-stand-boss.rune-mine-damage", 8.0);
    }

    /** One flung rune: it flies, lands, arms and then either bursts or fades. */
    private static final class Mine {
        final Location from;
        final Location to;
        Location current;
        int age;
        boolean spent;

        Mine(Location from, Location to) {
            this.from = from.clone();
            this.to = to.clone();
            this.current = from.clone();
        }
    }

    @Override
    public void execute(BossInstance instance) {
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        LivingEntity attacker = stand.entidad();
        Player target = boss.detectTarget(stand);
        if (target == null) return;

        if (plugin.getMagicSealListener() != null && stand.armorStand() != null) {
            plugin.getMagicSealListener().spawnRunicTriangleSeal(stand.armorStand(), 80);
        }

        new BukkitRunnable() {
            int t = 0;
            final List<Mine> mines = new ArrayList<>();

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 230) {
                    cancel();
                    return;
                }

                Location center = stand.getLocation();

                if (t < WIND_UP_TICKS) {
                    double p = (double) t / WIND_UP_TICKS;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-25), Math.toRadians(120 * p), Math.toRadians(-55 * p)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-40), Math.toRadians(-35), Math.toRadians(20)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(22 * p), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(18 * p), 0, 0));

                    if (t == 0) world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.4f, 0.7f);
                    double sweep = Math.toRadians(70 + 200 * p);
                    Location hand = center.clone().add(Math.cos(sweep) * 1.6, 0.6, Math.sin(sweep) * 1.6);
                    world.spawnParticle(Particle.DUST, hand, 3, 0.1, 0.1, 0.1, 0,
                            new Particle.DustOptions(Color.fromRGB(0x9B6BFF), 1.3f));
                    if (t == WIND_UP_TICKS - 1) {
                        world.playSound(center, Sound.ENTITY_WITCH_THROW, 1.2f, 1.2f);
                        launch(world, center, target, mines);
                    }
                } else {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-20), Math.toRadians(45), Math.toRadians(-20)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-40), Math.toRadians(-35), Math.toRadians(20)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(16), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(12), 0, 0));
                }

                tickMines(world, mines, attacker);

                if (t > WIND_UP_TICKS + FLIGHT_TICKS && mines.isEmpty()) {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Flings the runes: each one lands somewhere in the ring around the target. */
    private void launch(World world, Location center, Player target, List<Mine> mines) {
        Location from = center.clone().add(0, 1.5, 0);
        List<Location> landings = new ArrayList<>();
        for (int i = 0; i < MINES; i++) {
            Location landing = null;
            for (int attempt = 0; attempt < 8 && landing == null; attempt++) {
                double angle = random.nextDouble() * Math.PI * 2;
                double radius = 1.4 + random.nextDouble() * (SCATTER - 1.4);
                Location candidate = target.getLocation().clone()
                        .add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                candidate.setY(boss.getGroundY(candidate, 5) + 0.05);
                boolean tooClose = false;
                for (Location taken : landings) {
                    if (taken.distanceSquared(candidate) < 1.6) tooClose = true;
                }
                if (!tooClose) landing = candidate;
            }
            if (landing == null) landing = target.getLocation().clone();
            landings.add(landing);
            mines.add(new Mine(from, landing));
        }
        world.playSound(from, Sound.BLOCK_AMETHYST_BLOCK_PLACE, 1.4f, 1.1f);
    }

    /** Advances flight, arming and triggering of every rune. */
    private void tickMines(World world, List<Mine> mines, LivingEntity attacker) {
        Iterator<Mine> it = mines.iterator();
        while (it.hasNext()) {
            Mine mine = it.next();
            if (mine.spent) {
                it.remove();
                continue;
            }

            if (mine.age < FLIGHT_TICKS) {
                double p = (double) mine.age / FLIGHT_TICKS;
                Location current = mine.from.clone().add(mine.to.toVector().subtract(mine.from.toVector()).multiply(p));
                current.add(0, Math.sin(p * Math.PI) * 2.2, 0);
                mine.current = current;
                world.spawnParticle(Particle.DUST, current, 2, 0.05, 0.05, 0.05, 0,
                        new Particle.DustOptions(Color.fromRGB(0x9B6BFF), 1.2f));
                world.spawnParticle(Particle.END_ROD, current, 1, 0.03, 0.03, 0.03, 0);
            } else {
                int landed = mine.age - FLIGHT_TICKS;
                mine.current = mine.to;
                if (landed == 0) {
                    world.playSound(mine.to, Sound.BLOCK_AMETHYST_BLOCK_PLACE, 0.9f, 1.4f);
                    world.spawnParticle(Particle.CLOUD, mine.to, 6, 0.2, 0.1, 0.2, 0.02);
                }

                boolean armed = landed >= ARM_TICKS;
                drawRune(world, mine.to, landed, armed);

                if (armed) {
                    for (Player p : boss.getValidPlayers(world)) {
                        if (p.getLocation().distanceSquared(mine.to) < TRIGGER_RADIUS * TRIGGER_RADIUS) {
                            detonate(world, mine.to, attacker);
                            mine.spent = true;
                            break;
                        }
                    }
                }
                if (!mine.spent && landed >= MINE_LIFE) {
                    world.spawnParticle(Particle.DUST, mine.to.clone().add(0, 0.2, 0), 12, 0.25, 0.1, 0.25, 0,
                            new Particle.DustOptions(Color.fromRGB(0x443355), 1.0f));
                    world.playSound(mine.to, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 0.5f);
                    mine.spent = true;
                }
            }
            mine.age++;
        }
    }

    /** A small rune circle: dim while arming, violet and breathing once it is live. */
    private void drawRune(World world, Location base, int landed, boolean armed) {
        if (landed % (armed ? 3 : 6) != 0) return;
        Color color = armed ? Color.fromRGB(0x9B6BFF) : Color.fromRGB(0x6B6B6B);
        double radius = armed ? 0.9 + 0.08 * Math.sin(landed * 0.2) : 0.7;
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI * 2 * i / 12 + (armed ? landed * 0.05 : 0);
            Location pl = base.clone().add(Math.cos(angle) * radius, 0.08, Math.sin(angle) * radius);
            world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1.1f));
        }
        if (armed) {
            world.spawnParticle(Particle.ENCHANT, base.clone().add(0, 0.3, 0), 2, 0.2, 0.1, 0.2, 0.02);
        }
    }

    /** The rune bursts: expanding violet ring, upward knock and a slow hex. */
    private void detonate(World world, Location base, LivingEntity attacker) {
        world.playSound(base, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.5f, 1.0f);
        world.playSound(base, Sound.ENTITY_GENERIC_EXPLODE, 0.9f, 1.3f);
        world.spawnParticle(Particle.EXPLOSION, base.clone().add(0, 0.4, 0), 3, 0.3, 0.2, 0.3, 0);

        for (int ring = 0; ring < 4; ring++) {
            double radius = 0.6 + ring * 0.85;
            int points = (int) Math.max(10, radius * 12);
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2 * i / points;
                Location pl = base.clone().add(Math.cos(angle) * radius, 0.12, Math.sin(angle) * radius);
                world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(0xB98BFF), 1.6f));
                if (ring == 3 && i % 3 == 0) world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0.2, 0, 0.02);
            }
        }

        for (Player p : boss.getValidPlayers(world)) {
            if (p.getLocation().distanceSquared(base) > BLAST_RADIUS * BLAST_RADIUS) continue;
            MscEntityUtils.damageBy(attacker, p, damage);
            Vector away = p.getLocation().toVector().subtract(base.toVector()).setY(0);
            if (away.lengthSquared() < 0.01) away = new Vector(0, 0, 1);
            p.setVelocity(away.normalize().multiply(0.35).setY(0.62));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
            world.spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 6, 0.3, 0.3, 0.3, 0.05);
        }
    }

    @Override
    public String getName() {
        return "runemines";
    }
}
