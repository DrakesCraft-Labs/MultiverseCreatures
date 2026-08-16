package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class GroundShatterAttack extends BossAttackBase {
    public GroundShatterAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnQuakeSeal(center.clone().add(0, 0.5, 0), 60);
        }

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                if (t < 20) {
                    double phase = (double) t / 20;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(30 * phase), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(-30 * phase), 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(10 * phase), 0, 0));
                    world.spawnParticle(Particle.CRIT, center.clone().add(0, 1, 0), 5, 1.5, 0.1, 1.5, 0.05);
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.7f);
                } else if (t < 30) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(30), Math.toRadians(90)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-30), Math.toRadians(-90)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(45), 0, 0));
                    if (t == 20) {
                        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.3f);
                        world.spawnParticle(Particle.EXPLOSION, center, 20, 4, 1, 4, 0);
                        double damage = sealDamage;
                        for (int ring = 0; ring < 6; ring++) {
                            final int r = ring;
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    double radius = 2.0 + r * 2.5;
                                    int pts = (int) (radius * 4);
                                    for (int a = 0; a < pts; a++) {
                                        double angle = (2 * Math.PI * a / pts);
                                        double x = center.getX() + Math.cos(angle) * radius;
                                        double z = center.getZ() + Math.sin(angle) * radius;
                                        Location pl = new Location(world, x, center.getY(), z);
                                        world.spawnParticle(Particle.BLOCK, pl, 3, 0.3, 0.6, 0.3, 0.1, Material.DIRT.createBlockData());
                                        world.spawnParticle(Particle.FLAME, pl, 2, 0.2, 0.1, 0.2, 0.02);
                                    }
                                    for (Player p : boss.getValidPlayers(world)) {
                                        if (p.getLocation().distance(center) < radius + 1.5) {
                                            MscEntityUtils.damageBy(stand, p, damage * (1 - r * 0.1));
                                            boss.launchPlayer(p, 0.6);
                                        }
                                    }
                                }
                            }.runTaskLater(plugin, r * 4L);
                        }
                    }
                } else {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "groundshatter";
    }
}
