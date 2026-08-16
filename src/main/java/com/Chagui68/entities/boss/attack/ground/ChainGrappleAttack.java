package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class ChainGrappleAttack extends BossAttackBase {
    public ChainGrappleAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnPentagramSeal(center, 60, MagicSealListener.Plane.XZ);
        }

        new BukkitRunnable() {
            int t = 0;
            boolean pulled = false;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                if (!pulled) {
                    if (t < 20) {
                        double phase = (double) t / 20;
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase + 90), Math.toRadians(20 * phase), 0));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase + 90), Math.toRadians(-20 * phase), 0));
                        world.spawnParticle(Particle.END_ROD, center.clone().add(0, 6, 0), 2, 0.5, 0.5, 0.5, 0.01);
                        if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.9f);
                    } else if (t < 40) {
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(20), 0));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-20), 0));
                        Player target = boss.detectTarget(stand);
                        if (target != null) {
                            Location tLoc = target.getLocation();
                            Vector toBoss = center.toVector().subtract(tLoc.toVector());
                            double dist = toBoss.length();
                            if (dist > 3) {
                                target.setVelocity(toBoss.normalize().multiply(Math.min(1.5, dist * 0.05)));
                                MscEntityUtils.damageBy(stand, target, 2.0);
                                for (double y = 0; y < dist; y += 1) {
                                    Location chain = tLoc.clone().add(toBoss.normalize().multiply(y));
                                    chain.setY(chain.getY() + 1);
                                    world.spawnParticle(Particle.END_ROD, chain, 1, 0, 0, 0, 0);
                                    world.spawnParticle(Particle.FLAME, chain, 1, 0.1, 0.1, 0.1, 0.01);
                                }
                            } else {
                                pulled = true;
                                t = 0;
                            }
                        }
                        world.playSound(center, Sound.ENTITY_ARROW_SHOOT, 0.8f, 0.5f);
                    } else {
                        pulled = true;
                        t = 0;
                    }
                } else {
                    if (t < 15) {
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(60), Math.toRadians(30)));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-60), Math.toRadians(-30)));
                        stand.setBodyPose(new EulerAngle(Math.toRadians(20), 0, 0));
                        world.spawnParticle(Particle.EXPLOSION, center, 5, 1, 0.5, 1, 0);
                        Player target = boss.detectTarget(stand);
                        if (target != null && target.getLocation().distanceSquared(center) < 16) {
                            MscEntityUtils.damageBy(stand, target, sealDamage);
                            boss.launchPlayer(target, 1.5);
                        }
                    } else {
                        boss.resetBossPose(instance);
                        cancel();
                    }
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "chaingrapple";
    }
}
