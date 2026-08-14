package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class LanceSnipeAttack extends BossAttackBase {
    public LanceSnipeAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        Player target = boss.detectTarget(stand);
        if (target == null) return;

        Vector dir = target.getEyeLocation().toVector().subtract(center.toVector()).normalize();
        new BukkitRunnable() {
            int t = 0;
            double traveled = 0;
            final double speed = 2.0;
            final double maxRange = 30.0;
            Location pos = center.clone().add(0, 1.5, 0);

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 80) {
                    cancel();
                    return;
                }
                if (t < 20) {
                    double phase = (double) t / 20;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(30 * phase), 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(15 * phase), 0, 0));
                    world.spawnParticle(Particle.END_ROD, center.clone().add(0, 1.5, 0), 2, 0.3, 0.3, 0.3, 0);
                    if (t == 1) world.playSound(center, Sound.ENTITY_ARROW_SHOOT, 0.6f, 0.5f);
                } else if (traveled < maxRange) {
                    pos.add(dir.clone().multiply(speed));
                    traveled += speed;
                    world.spawnParticle(Particle.CRIT, pos, 4, 0.1, 0.1, 0.1, 0.02);
                    world.spawnParticle(Particle.DUST, pos, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0xFFAA00), 1.8f));
                    for (Player p : boss.getValidPlayers(world)) {
                        if (p.getLocation().distanceSquared(pos) < 4) {
                            p.damage(sealDamage);
                            p.setVelocity(dir.clone().setY(0.4).multiply(0.5));
                            world.spawnParticle(Particle.EXPLOSION, pos, 5, 0.3, 0.3, 0.3, 0);
                            world.playSound(pos, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);
                            cancel();
                            return;
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
        return "lancesnipe";
    }
}
