package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class MeteorStormAttack extends BossAttackBase {
    public MeteorStormAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();

        new BukkitRunnable() {
            int t = 0;
            int meteorsFired = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                if (t < 25) {
                    double phase = (double) t / 25;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-160 * phase), Math.toRadians(40), Math.toRadians(20 * phase)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-160 * phase), Math.toRadians(-40), Math.toRadians(-20 * phase)));
                    if (t == 1) world.playSound(center, Sound.ENTITY_BLAZE_SHOOT, 1.2f, 0.5f);
                } else if (meteorsFired < 5) {
                    if (t % 6 == 0) {
                        meteorsFired++;
                        Player target = boss.detectTarget(stand);
                        if (target != null) {
                            final Location dest = target.getLocation();
                            final Location start = dest.clone().add(0, 18, 0);
                            new BukkitRunnable() {
                                int ft = 0;

                                @Override
                                public void run() {
                                    if (ft > 30) {
                                        cancel();
                                        return;
                                    }
                                    Location fall = start.clone().subtract(0, ft * 0.6, 0);
                                    world.spawnParticle(Particle.FLAME, fall, 4, 0.4, 0.2, 0.4, 0.02);
                                    world.spawnParticle(Particle.LAVA, fall, 2, 0.3, 0.1, 0.3, 0);
                                    if (fall.getY() <= dest.getY() + 0.5) {
                                        world.spawnParticle(Particle.EXPLOSION, fall, 10, 1.5, 0.5, 1.5, 0);
                                        world.playSound(fall, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);
                                        double dmg = sealDamage * 0.5;
                                        for (Player p : boss.getValidPlayers(world)) {
                                            if (p.getLocation().distanceSquared(fall) < 16) {
                                                p.damage(dmg);
                                                p.setFireTicks(60);
                                                boss.launchPlayer(p, 0.5);
                                            }
                                        }
                                        cancel();
                                    }
                                    ft++;
                                }
                            }.runTaskTimer(plugin, 0L, 1L);
                        }
                    }
                } else if (t > 100) {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "meteorstorm";
    }
}
