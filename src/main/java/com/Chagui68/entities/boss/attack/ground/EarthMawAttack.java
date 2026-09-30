package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Location;
import org.bukkit.Material;
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

/**
 * Earth Maw — the ground opens as a pair of jaws in a frontal cone and snaps shut.
 *
 * <p>ANIMATION (what tells it apart on screen)
 * <ul>
 *   <li>0-15: the boss crouches with both arms drawn inward while two rows of jagged teeth appear in
 *       the cone ahead, opening wider every tick. A Quake Seal cracks the ground under them.</li>
 *   <li>16: the rows <b>snap shut</b> — the only attack in the arsenal with a closing bite — and pull
 *       everyone caught inside towards the centre before chewing for another second.</li>
 * </ul>
 */
public class EarthMawAttack extends BossAttackBase {

    private static final int WIND_UP_TICKS = 16;
    private static final double CONE_LENGTH = 5.2;
    private static final double CONE_ANGLE_DEG = 34.0;
    private static final double HALF_ANGLE_COS = Math.cos(Math.toRadians(CONE_ANGLE_DEG));

    private final double damage;

    public EarthMawAttack(BossHost boss) {
        super(boss);
        this.damage = plugin.getConfig().getDouble("entities.armor-stand-boss.earth-maw-damage", 9.0);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location origin = stand.getLocation().clone();
        LivingEntity attacker = stand.entidad();

        Vector facing = origin.getDirection().setY(0);
        if (facing.lengthSquared() < 0.01) facing = new Vector(0, 0, 1);
        final Vector direction = facing.normalize();
        Vector side = direction.clone().crossProduct(new Vector(0, 1, 0));
        final Vector right = side.lengthSquared() < 0.01 ? new Vector(1, 0, 0) : side.normalize();

        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnQuakeSeal(
                    origin.clone().add(direction.clone().multiply(2.6)).add(0, 0.3, 0), 60);
        }

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 70) {
                    cancel();
                    return;
                }

                Location center = stand.getLocation();

                if (t < WIND_UP_TICKS) {
                    double p = (double) t / WIND_UP_TICKS;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-55 * p), Math.toRadians(55 * p), Math.toRadians(45 * p)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-55 * p), Math.toRadians(-55 * p), Math.toRadians(-45 * p)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(26 * p), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(24 * p), 0, 0));
                    stand.setRightLegPose(new EulerAngle(Math.toRadians(-14 * p), 0, 0));
                    stand.setLeftLegPose(new EulerAngle(Math.toRadians(14 * p), 0, 0));

                    drawJaws(world, center, direction, right, 0.15 + p * 0.85, p);
                    if (t == 0) world.playSound(center, Sound.BLOCK_BASALT_BREAK, 1.3f, 0.6f);
                    if (t % 6 == 0) world.playSound(center, Sound.ENTITY_RAVAGER_ROAR, 0.7f, 0.7f + (float) p * 0.3f);
                } else if (t == WIND_UP_TICKS) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-20), Math.toRadians(20), Math.toRadians(80)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-20), Math.toRadians(-20), Math.toRadians(-80)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(32), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(18), 0, 0));

                    world.playSound(center, Sound.ENTITY_RAVAGER_ATTACK, 2.0f, 0.6f);
                    world.playSound(center, Sound.ENTITY_PHANTOM_BITE, 1.6f, 0.5f);
                    world.playSound(center, Sound.BLOCK_ANVIL_LAND, 1.2f, 0.7f);
                    drawSnap(world, center, direction, right);
                    bite(center, direction, attacker);
                } else if (t < WIND_UP_TICKS + 26) {
                    if (t % 8 == 0) {
                        world.playSound(center, Sound.ENTITY_RAVAGER_ATTACK, 0.8f, 0.4f);
                    }
                    world.spawnParticle(Particle.BLOCK, center.clone().add(direction.clone().multiply(3.0)).add(0, 0.2, 0),
                            6, 1.4, 0.2, 1.4, 0.02, Material.COARSE_DIRT.createBlockData());
                } else {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Two rows of teeth along the cone, opening as {@code open} grows from 0 to 1. */
    private void drawJaws(World world, Location center, Vector direction, Vector right, double open, double charge) {
        double spread = CONE_ANGLE_DEG * (0.35 + 0.65 * open);
        double gap = 1.15 * open;

        for (double dist = 1.4; dist <= CONE_LENGTH; dist += 0.55) {
            for (double angle = -spread; angle <= spread; angle += spread / 3.0) {
                double rad = Math.toRadians(angle);
                Vector offset = direction.clone().multiply(Math.cos(rad) * dist)
                        .add(right.clone().multiply(Math.sin(rad) * dist));
                double taper = 1.0 - (dist / CONE_LENGTH) * 0.45;

                Location upper = center.clone().add(offset).add(0, 0.95 + gap, 0);
                Location lower = center.clone().add(offset).add(0, 0.1 - gap * 0.4, 0);
                world.spawnParticle(Particle.BLOCK, upper, 1, 0.05, 0.05, 0.05, 0,
                        Material.DEEPSLATE.createBlockData());
                world.spawnParticle(Particle.BLOCK, lower, 1, 0.05, 0.05, 0.05, 0,
                        Material.DEEPSLATE.createBlockData());
                if (charge > 0.7) {
                    world.spawnParticle(Particle.FALLING_DUST, upper, 1, 0.05, 0.05, 0.05, 0,
                            Material.GRAVEL.createBlockData());
                }
                if (taper < 0.6) {
                    world.spawnParticle(Particle.DUST, upper, 1, 0.1, 0.05, 0.1, 0,
                            new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(0x6B4A2B), 0.8f));
                }
            }
        }
    }

    /** The rows collapse inwards and a dust wave closes the cone. */
    private void drawSnap(World world, Location center, Vector direction, Vector right) {
        for (double dist = 1.4; dist <= CONE_LENGTH; dist += 0.5) {
            for (double angle = -CONE_ANGLE_DEG; angle <= CONE_ANGLE_DEG; angle += 10.0) {
                double rad = Math.toRadians(angle);
                Vector offset = direction.clone().multiply(Math.cos(rad) * dist)
                        .add(right.clone().multiply(Math.sin(rad) * dist));
                world.spawnParticle(Particle.BLOCK, center.clone().add(offset).add(0, 0.5, 0), 4, 0.2, 0.35, 0.2, 0.05,
                        Material.DEEPSLATE.createBlockData());
            }
            world.spawnParticle(Particle.CLOUD, center.clone().add(direction.clone().multiply(dist)).add(0, 0.3, 0),
                    6, 0.5, 0.15, 0.5, 0.04);
        }
        world.spawnParticle(Particle.EXPLOSION, center.clone().add(direction.clone().multiply(2.5)).add(0, 0.4, 0),
                8, 1.4, 0.3, 1.4, 0);
    }

    /** Everyone inside the cone is bitten, dragged towards the centre and slowed. */
    private void bite(Location center, Vector direction, LivingEntity attacker) {
        World world = center.getWorld();
        for (Player p : boss.getValidPlayers(world)) {
            Location pl = p.getLocation();
            Vector toPlayer = pl.toVector().subtract(center.toVector());
            double dy = toPlayer.getY();
            Vector flat = toPlayer.clone().setY(0);
            double distance = flat.length();
            if (distance > CONE_LENGTH || dy < -1.5 || dy > 2.8) continue;

            Vector flatDir = distance < 0.01 ? direction.clone() : flat.clone().normalize();
            if (flatDir.dot(direction) < HALF_ANGLE_COS) continue;

            MscEntityUtils.damageBy(attacker, p, damage);
            // Dragged inwards, into the jaws: the cone points away from the boss, so the pull is reversed.
            Vector pull = direction.clone().multiply(-0.34).setY(0.22);
            p.setVelocity(p.getVelocity().add(pull));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 0));
            world.spawnParticle(Particle.DAMAGE_INDICATOR, pl.clone().add(0, 1, 0), 8, 0.35, 0.4, 0.35, 0.08);
        }
    }

    @Override
    public String getName() {
        return "earthmaw";
    }
}
