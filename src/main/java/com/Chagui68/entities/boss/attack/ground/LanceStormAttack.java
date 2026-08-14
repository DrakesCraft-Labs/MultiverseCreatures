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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class LanceStormAttack extends BossAttackBase {
    public LanceStormAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnRunicTriangleSeal(stand, 60);
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
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-160 * phase), Math.toRadians(10), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-160 * phase), Math.toRadians(-10), 0));
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.6f);
                } else if (t < 45) {
                    double angle = (t - 15) * 0.3;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-160 + Math.sin(angle) * 40), Math.toRadians(10 + Math.cos(angle) * 20), Math.toRadians(Math.sin(angle) * 15)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-160 + Math.sin(angle + Math.PI) * 40), Math.toRadians(-10 + Math.cos(angle + Math.PI) * 20), Math.toRadians(-Math.sin(angle + Math.PI) * 15)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(Math.sin(angle * 0.5) * 10), 0, 0));
                    for (int a = 0; a < 12; a++) {
                        double a2 = (2 * Math.PI * a / 12) + angle;
                        double r = 3.0 + Math.sin(angle + a) * 1.5;
                        double x = center.getX() + Math.cos(a2) * r;
                        double z = center.getZ() + Math.sin(a2) * r;
                        Location pl = new Location(world, x, center.getY() + 1 + Math.sin(angle + a * 0.3) * 0.5, z);
                        world.spawnParticle(Particle.CRIT, pl, 2, 0.2, 0.2, 0.2, 0.03);
                        world.spawnParticle(Particle.SWEEP_ATTACK, pl, 1, 0, 0, 0, 0);
                    }
                    world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 0.8f + (float) Math.sin(angle) * 0.2f);
                    double dmg = sealDamage * 0.3;
                    for (Player p : boss.getValidPlayers(world)) {
                        if (p.getLocation().distanceSquared(center) < 36) {
                            p.damage(dmg);
                            Vector away = p.getLocation().toVector().subtract(center.toVector());
                            if (away.lengthSquared() > 0) p.setVelocity(away.normalize().multiply(0.5).setY(0.2));
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
        return "lancestorm";
    }
}
