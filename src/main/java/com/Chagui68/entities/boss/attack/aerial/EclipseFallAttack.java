package com.Chagui68.entities.boss.attack.aerial;

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

/**
 * Eclipse Fall — the boss conjures an eclipse over his head and drops it on the ground.
 *
 * <p>ANIMATION (what tells it apart on screen)
 * <ul>
 *   <li>0-24: both arms lift straight up while a <b>dark disc</b> forms three blocks above him,
 *       ringed by a pentagram; the boss himself never touches the ground.</li>
 *   <li>25-34: the disc <b>falls</b> — a ball of shadow with a comet trail, telegraphed by the ever
 *       darker sky under it.</li>
 *   <li>35: it lands as a black shockwave that blinds everyone nearby. The boss keeps hovering, so
 *       unlike the air slam this one never drags him down.</li>
 * </ul>
 */
public class EclipseFallAttack extends BossAttackBase {

    private static final int CHARGE_TICKS = 25;
    private static final double DISC_HEIGHT = 3.4;
    private static final double BLAST_RADIUS = 5.0;

    private final double damage;

    public EclipseFallAttack(BossHost boss) {
        super(boss);
        this.damage = plugin.getConfig().getDouble("entities.armor-stand-boss.eclipse-fall-damage", 16.0);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Player target = boss.detectTarget(stand);
        if (target == null) return;
        LivingEntity attacker = stand.entidad();

        Location targetGround = target.getLocation().clone();
        targetGround.setY(boss.getGroundY(targetGround, 6));

        new BukkitRunnable() {
            int t = 0;
            Location disc = null;
            boolean dropped = false;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 90) {
                    cancel();
                    return;
                }

                Location center = stand.getLocation();

                if (t < CHARGE_TICKS) {
                    double p = (double) t / CHARGE_TICKS;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-30 - 130 * p), Math.toRadians(8), Math.toRadians(-10 * p)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-30 - 130 * p), Math.toRadians(-8), Math.toRadians(10 * p)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-14 * p), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-16 * p), 0, 0));

                    Location discCenter = center.clone().add(0, DISC_HEIGHT, 0);
                    double radius = 0.6 + 1.7 * p;
                    for (int i = 0; i < 28; i++) {
                        double angle = Math.PI * 2 * i / 28 + t * 0.09;
                        double r = radius * (0.75 + 0.25 * Math.sin(i));
                        Location pl = discCenter.clone().add(Math.cos(angle) * r, 0, Math.sin(angle) * r);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x1B0B2A), 1.8f));
                        if (i % 4 == 0) {
                            world.spawnParticle(Particle.SOUL_FIRE_FLAME, pl, 1, 0, 0, 0, 0.01);
                        }
                    }
                    world.spawnParticle(Particle.SCULK_SOUL, discCenter, 2, radius * 0.7, 0.2, radius * 0.7, 0.01);
                    if (t == 0) {
                        world.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.6f, 0.6f);
                        world.playSound(center, Sound.ENTITY_PHANTOM_SWOOP, 1.2f, 0.6f);
                        if (plugin.getMagicSealListener() != null) {
                            plugin.getMagicSealListener().spawnLargePentagramSeal(
                                    center.clone().add(0, DISC_HEIGHT + 1.2, 0), 120, 4.0, MagicSealListener.Plane.XZ);
                        }
                    }
                } else if (!dropped) {
                    dropped = true;
                    disc = center.clone().add(0, DISC_HEIGHT, 0);
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-20), Math.toRadians(20), Math.toRadians(20)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-20), Math.toRadians(-20), Math.toRadians(-20)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(20), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(24), 0, 0));
                    world.playSound(center, Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.6f, 0.7f);
                } else if (disc != null) {
                    Vector step = targetGround.clone().add(0, 0.4, 0).toVector().subtract(disc.toVector());
                    double distance = step.length();
                    if (distance < 0.7) {
                        detonate(world, targetGround, attacker);
                        disc = null;
                        world.playSound(targetGround, Sound.ENTITY_GENERIC_EXPLODE, 2.2f, 0.5f);
                        world.playSound(targetGround, Sound.BLOCK_ANVIL_DESTROY, 1.4f, 0.6f);
                    } else {
                        Vector motion = step.normalize().multiply(Math.min(1.1, 0.35 + distance * 0.1));
                        disc.add(motion);
                        world.spawnParticle(Particle.DUST, disc, 6, 0.45, 0.45, 0.45, 0,
                                new Particle.DustOptions(Color.fromRGB(0x120618), 2.0f));
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, disc, 3, 0.3, 0.3, 0.3, 0.02);
                        world.spawnParticle(Particle.SCULK_SOUL, disc, 2, 0.35, 0.35, 0.35, 0.01);
                        world.spawnParticle(Particle.SMOKE, disc, 4, 0.3, 0.3, 0.3, 0.01);
                    }
                } else if (t > CHARGE_TICKS + 40) {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** The impact: black shockwave, blindness and damage inside the blast radius. */
    private void detonate(World world, Location center, LivingEntity attacker) {
        boss.spawnShockwaveWave(attacker, world, center, BLAST_RADIUS * 0.8);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, center.clone().add(0, 0.4, 0), 2);
        world.spawnParticle(Particle.SCULK_SOUL, center.clone().add(0, 0.5, 0), 60, 2.0, 0.4, 2.0, 0.05);
        world.spawnParticle(Particle.DUST, center.clone().add(0, 0.3, 0), 80, 2.2, 0.2, 2.2, 0,
                new Particle.DustOptions(Color.fromRGB(0x1B0B2A), 2.2f));
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnLargePentagramSeal(center.clone().add(0, 0.3, 0), 100, 5.0,
                    MagicSealListener.Plane.XZ);
        }

        for (Player p : boss.getValidPlayers(world)) {
            if (p.getLocation().distanceSquared(center) > BLAST_RADIUS * BLAST_RADIUS) continue;
            MscEntityUtils.damageBy(attacker, p, damage);
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0));
            world.spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 10, 0.4, 0.5, 0.4, 0.08);
        }
    }

    @Override
    public String getName() {
        return "eclipsefall";
    }
}
