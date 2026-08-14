package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * Barrido del verdugo: golpe devastador de arco amplio que barre todo lo que
 * este delante del jefe. Telegrafiado, mucho daño, empuje fuerte y debilidad.
 */
public class ExecutionerSweepAttack extends BossAttackBase {
    public ExecutionerSweepAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();

        new BukkitRunnable() {
            int t = 0;
            boolean charging = true;
            boolean done = false;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                Location loc = stand.getLocation();
                Vector dir = loc.getDirection();
                if (dir.lengthSquared() < 0.01) dir = new Vector(0, 0, 1);
                dir.setY(0).normalize();
                final Vector fDir = dir;

                if (charging) {
                    double phase = Math.min(1.0, (double) t / 25);
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-160 * phase), Math.toRadians(60 * phase), Math.toRadians(20 * phase)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-160 * phase), Math.toRadians(-60 * phase), Math.toRadians(-20 * phase)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-25 * phase), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-20 * phase), 0, 0));
                    world.spawnParticle(Particle.END_ROD, loc.clone().add(0, 7, 0), 3, 2.0, 0.5, 2.0, 0.01);
                    if (t == 1) world.playSound(loc, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 0.6f);
                    if (t % 5 == 0 && t > 0) world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 0.5f);
                    if (t >= 25) {
                        charging = false;
                        t = 0;
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(-170), Math.toRadians(80), Math.toRadians(30)));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-170), Math.toRadians(-80), Math.toRadians(-30)));
                        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 2.0f, 0.9f);
                        world.spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(fDir.clone().multiply(5)), 30, 4.0, 1.5, 2.0, 0);
                        world.spawnParticle(Particle.CRIT, loc.clone().add(fDir.clone().multiply(6)), 40, 4.0, 2.0, 3.0, 0.1);
                        world.spawnParticle(Particle.EXPLOSION, loc.clone().add(fDir.clone().multiply(5)), 8, 2.0, 1.0, 2.0, 0);

                        for (Player p : boss.getValidPlayers(world)) {
                            Vector toP = p.getLocation().toVector().subtract(loc.toVector());
                            toP.setY(0);
                            if (toP.lengthSquared() > 81) continue;
                            if (toP.lengthSquared() < 0.01) continue;
                            if (toP.normalize().dot(fDir) < 0.0) continue;

                            p.damage(16.0);
                            p.setVelocity(fDir.clone().multiply(2.0).setY(0.9));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
                        }
                    }
                } else if (t >= 30) {
                    if (!done) {
                        done = true;
                        boss.resetBossPose(instance);
                    }
                    cancel();
                    return;
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "executionsweep";
    }
}