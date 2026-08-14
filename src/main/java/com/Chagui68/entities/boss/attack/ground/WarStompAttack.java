package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import org.bukkit.Location;
import org.bukkit.Material;
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

public class WarStompAttack extends BossAttackBase {
    public WarStompAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnQuakeSeal(center.clone().add(0, 0.3, 0), 50);
        }

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                if (t < 15) {
                    double phase = (double) t / 15;
                    stand.setRightLegPose(new EulerAngle(Math.toRadians(-30 * phase), 0, 0));
                    stand.setLeftLegPose(new EulerAngle(Math.toRadians(30 * phase), 0, 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-10 * phase), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-10 * phase), 0, 0));
                    world.spawnParticle(Particle.CRIT, center.clone().add(0, -0.5, 0), 3, 0.5, 0.1, 0.5, 0.02);
                    if (t == 1) world.playSound(center, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.6f);
                } else if (t < 40) {
                    stand.setRightLegPose(new EulerAngle(Math.toRadians(-30), 0, 0));
                    stand.setLeftLegPose(new EulerAngle(Math.toRadians(30), 0, 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-10), 0, 0));
                    if (t == 15) {
                        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.5f, 0.4f);
                        world.spawnParticle(Particle.EXPLOSION, center, 15, 3, 0.5, 3, 0);
                        world.spawnParticle(Particle.CLOUD, center.clone().add(0, 0.2, 0), 40, 4, 0.5, 4, 0.15);
                        for (int ring = 0; ring < 5; ring++) {
                            final int r = ring;
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    double radius = 2.5 + r * 2.5;
                                    int pts = (int) (radius * 4);
                                    for (int a = 0; a < pts; a++) {
                                        double angle = (2 * Math.PI * a / pts) + r * 0.5;
                                        double x = center.getX() + Math.cos(angle) * radius;
                                        double z = center.getZ() + Math.sin(angle) * radius;
                                        Location pl = new Location(world, x, center.getY(), z);
                                        world.spawnParticle(Particle.BLOCK, pl, 2, 0.3, 0.3, 0.3, 0.05, Material.DIRT.createBlockData());
                                        world.spawnParticle(Particle.CRIT, pl.clone().add(0, 0.5, 0), 1, 0.2, 0.2, 0.2, 0.02);
                                    }
                                    for (Player p : boss.getValidPlayers(world)) {
                                        if (p.getLocation().distance(center) < radius + 1) {
                                            p.damage(8.0 * (1 - r * 0.1));
                                            boss.launchPlayer(p, 0.5);
                                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1));
                                        }
                                    }
                                }
                            }.runTaskLater(plugin, r * 3L);
                        }
                    }
                    world.spawnParticle(Particle.SMOKE, center, 3, 2, 0.1, 2, 0.02);
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
        return "warstomp";
    }
}
