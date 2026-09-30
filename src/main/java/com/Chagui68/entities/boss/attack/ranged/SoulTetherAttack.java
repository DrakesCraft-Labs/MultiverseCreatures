package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.boss.MagicSealListener;
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
import java.util.List;

/**
 * Soul Tethers — the boss hooks several players at once and reels them in.
 *
 * <p>ANIMATION (what tells it apart on screen)
 * <ul>
 *   <li>0-13: both arms rise overhead while soul embers drain out of every nearby player and fly into
 *       his chest. This is the only channel that visibly <b>takes</b> something from the players.</li>
 *   <li>14-88: a glowing tether is drawn from his chest to every hooked player. The line is visible
 *       the whole time — nothing else in the arsenal stays connected to several targets — and it
 *       yanks them a step closer on every pulse.</li>
 *   <li>89: the tethers snap taut and reel everyone in hard, then recoil and let go.</li>
 * </ul>
 */
public class SoulTetherAttack extends BossAttackBase {

    private static final int CHARGE_TICKS = 14;
    private static final int TETHER_TICKS = 75;
    private static final int MAX_TARGETS = 4;
    private static final double RANGE = 26.0;
    private static final int PULSE_EVERY = 10;

    private final double pulseDamage;
    private final double snapDamage;

    public SoulTetherAttack(BossHost boss) {
        super(boss);
        this.pulseDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.soul-tether-damage", 3.0);
        this.snapDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.soul-tether-snap-damage", 13.0);
    }

    @Override
    public void execute(BossInstance instance) {
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        LivingEntity attacker = stand.entidad();
        List<Player> hooked = nearestTargets(stand.getLocation());

        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnPentagramSeal(stand.getLocation().clone().add(0, 0.2, 0), 100,
                    MagicSealListener.Plane.XZ);
        }

        new BukkitRunnable() {
            int t = 0;
            boolean snapped = false;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > CHARGE_TICKS + TETHER_TICKS + 25) {
                    cancel();
                    return;
                }

                Location center = stand.getLocation();
                hooked.removeIf(p -> !p.isOnline() || p.isDead()
                        || p.getLocation().distanceSquared(center) > RANGE * RANGE);

                if (t < CHARGE_TICKS) {
                    double p = (double) t / CHARGE_TICKS;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-30 - 150 * p), Math.toRadians(10), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-30 - 150 * p), Math.toRadians(-10), 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-14 * p), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-22 * p), 0, 0));

                    for (Player p2 : hooked) {
                        Vector toBoss = center.clone().add(0, 1.4, 0).toVector().subtract(p2.getEyeLocation().toVector());
                        double distance = toBoss.length();
                        if (distance > 0.2) toBoss.normalize();
                        double travelled = Math.min(distance, 6.0 * p);
                        Location ember = p2.getEyeLocation().clone().add(toBoss.multiply(travelled));
                        world.spawnParticle(Particle.SOUL, ember, 2, 0.05, 0.05, 0.05, 0.01);
                        world.spawnParticle(Particle.SCULK_SOUL, ember, 1, 0.05, 0.05, 0.05, 0);
                    }
                    if (t == 0) {
                        world.playSound(center, Sound.BLOCK_SOUL_SAND_BREAK, 1.4f, 0.6f);
                        world.playSound(center, Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.6f);
                    }
                } else if (!snapped) {
                    if (t == CHARGE_TICKS) {
                        world.playSound(center, Sound.ENTITY_WITHER_SHOOT, 1.6f, 0.6f);
                        world.playSound(center, Sound.ITEM_TOTEM_USE, 1.0f, 1.4f);
                    }
                    boolean release = t >= CHARGE_TICKS + TETHER_TICKS;
                    if (release) {
                        snapped = true;
                        snap(world, center, hooked, attacker);
                    } else {
                        int since = t - CHARGE_TICKS;
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(-15), Math.toRadians(70), Math.toRadians(20)));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-15), Math.toRadians(-70), Math.toRadians(-20)));
                        stand.setBodyPose(new EulerAngle(Math.toRadians(10), 0, 0));
                        stand.setHeadPose(new EulerAngle(Math.toRadians(12), 0, 0));
                        drawTethers(world, center, hooked);
                        if (since % PULSE_EVERY == 0 && since > 0) {
                            for (Player p2 : hooked) {
                                MscEntityUtils.damageBy(attacker, p2, pulseDamage);
                                pull(p2, center, 0.1, 0.05);
                                p2.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 25, 0));
                                world.spawnParticle(Particle.SOUL_FIRE_FLAME, p2.getLocation().add(0, 1, 0),
                                        6, 0.3, 0.4, 0.3, 0.01);
                            }
                            world.playSound(center, Sound.BLOCK_SOUL_SAND_BREAK, 0.9f, 0.7f);
                        }
                    }
                } else if (t > CHARGE_TICKS + TETHER_TICKS + 18) {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** The visible line: a rope of soul embers sampled along every tether. */
    private void drawTethers(World world, Location center, List<Player> hooked) {
        Location chest = center.clone().add(0, 1.4, 0);
        for (Player p : hooked) {
            Location eye = p.getEyeLocation();
            Vector step = eye.toVector().subtract(chest.toVector());
            double distance = step.length();
            if (distance < 0.2) continue;
            Vector unit = step.normalize();
            for (double d = 0.0; d < distance; d += 0.9) {
                Location point = chest.clone().add(unit.clone().multiply(d));
                world.spawnParticle(Particle.SOUL, point, 1, 0.05, 0.05, 0.05, 0.005);
                world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.fromRGB(0x66DDFF), 1.0f));
            }
            world.spawnParticle(Particle.END_ROD, eye, 2, 0.15, 0.15, 0.15, 0);
        }
    }

    /** Every tether goes taut at once and drags its victim to the boss. */
    private void snap(World world, Location center, List<Player> hooked, LivingEntity attacker) {
        world.playSound(center, Sound.ENTITY_WITHER_HURT, 1.6f, 0.5f);
        world.playSound(center, Sound.BLOCK_CHAIN_BREAK, 1.2f, 1.1f);
        world.spawnParticle(Particle.REVERSE_PORTAL, center.clone().add(0, 1.4, 0), 40, 0.6, 0.8, 0.6, 0.1);

        for (Player p : hooked) {
            MscEntityUtils.damageBy(attacker, p, snapDamage);
            pull(p, center, 0.95, 0.3);
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
            world.spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 10, 0.4, 0.4, 0.4, 0.08);
            world.spawnParticle(Particle.SOUL, p.getLocation().add(0, 1, 0), 20, 0.4, 0.5, 0.4, 0.05);
        }
    }

    private void pull(Player p, Location center, double strength, double lift) {
        Vector toBoss = center.toVector().subtract(p.getLocation().toVector()).setY(0);
        if (toBoss.lengthSquared() < 0.01) return;
        Vector add = toBoss.normalize().multiply(strength).setY(lift);
        p.setVelocity(p.getVelocity().add(add));
    }

    private List<Player> nearestTargets(Location center) {
        List<Player> candidates = new ArrayList<>(boss.getValidPlayers(center.getWorld()));
        candidates.removeIf(p -> p.getLocation().distanceSquared(center) > RANGE * RANGE);
        List<Player> sorted = new ArrayList<>();
        while (!candidates.isEmpty() && sorted.size() < MAX_TARGETS) {
            Player best = null;
            double bestDistance = Double.MAX_VALUE;
            for (Player p : candidates) {
                double d = p.getLocation().distanceSquared(center);
                if (d < bestDistance) {
                    bestDistance = d;
                    best = p;
                }
            }
            candidates.remove(best);
            sorted.add(best);
        }
        return sorted;
    }

    @Override
    public String getName() {
        return "soultethers";
    }
}
