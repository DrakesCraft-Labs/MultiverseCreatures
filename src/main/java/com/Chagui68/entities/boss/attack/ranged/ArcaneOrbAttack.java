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

public class ArcaneOrbAttack extends BossAttackBase {
    public ArcaneOrbAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        Player target = boss.detectTarget(stand);
        if (target == null) return;

        new BukkitRunnable() {
            int t = 0;
            Location pos = center.clone().add(0, 1.5, 0);
            Vector vel = target.getLocation().toVector().subtract(pos.toVector()).normalize().multiply(0.8);

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 120) {
                    cancel();
                    return;
                }
                if (t < 25) {
                    double phase = (double) t / 25;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(45), Math.toRadians(45 * phase)));
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.4f);
                    if (t % 3 == 0)
                        world.spawnParticle(Particle.END_ROD, center.clone().add(0, 1.5, 0), 3, 0.3, 0.3, 0.3, 0.01);
                } else {
                    if (target.isOnline()) {
                        Vector toTarget = target.getLocation().toVector().subtract(pos.toVector());
                        double dist = toTarget.length();
                        if (dist > 0.5) {
                            vel.add(toTarget.normalize().multiply(0.06));
                        }
                        if (vel.lengthSquared() > 1.2) vel.normalize().multiply(1.1);
                    }
                    pos.add(vel);
                    world.spawnParticle(Particle.PORTAL, pos, 5, 0.3, 0.3, 0.3, 0.05);
                    world.spawnParticle(Particle.DUST, pos, 2, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0xBB44FF), 2.0f));
                    world.spawnParticle(Particle.END_ROD, pos, 1, 0.1, 0.1, 0.1, 0);
                    for (Player p : boss.getValidPlayers(world)) {
                        if (p.getLocation().distanceSquared(pos) < 6) {
                            world.spawnParticle(Particle.EXPLOSION, pos, 12, 1, 0.5, 1, 0);
                            world.playSound(pos, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.5f);
                            for (Player near : boss.getValidPlayers(world)) {
                                if (near.getLocation().distanceSquared(pos) < 25) {
                                    near.damage(sealDamage * 0.6);
                                    Vector away = near.getLocation().toVector().subtract(pos.toVector());
                                    if (away.lengthSquared() > 0)
                                        near.setVelocity(away.normalize().multiply(0.8).setY(0.4));
                                }
                            }
                            boss.resetBossPose(instance);
                            cancel();
                            return;
                        }
                    }
                    if (pos.distanceSquared(center) > 2500) {
                        boss.resetBossPose(instance);
                        cancel();
                        return;
                    }
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "arcaneorb";
    }
}
