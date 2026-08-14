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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArcaneMissilesAttack extends BossAttackBase {
    public ArcaneMissilesAttack(BossHost boss) {
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
            int missilesFired = 0;
            final List<Location> missiles = new ArrayList<>();
            final List<Vector> missileDirs = new ArrayList<>();

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 110) {
                    cancel();
                    return;
                }
                if (t < 22) {
                    double phase = (double) t / 22;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-120 * phase), Math.toRadians(45), Math.toRadians(30 * phase)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-120 * phase), Math.toRadians(-45), Math.toRadians(-30 * phase)));
                    if (t == 1) world.playSound(center, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.5f);
                } else if (missilesFired < 4) {
                    if (t % 8 == 0) {
                        missilesFired++;
                        Vector dir = target.getLocation().toVector().subtract(center.toVector()).normalize();
                        missiles.add(center.clone().add(0, 1.5, 0));
                        missileDirs.add(dir);
                        world.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f, 0.5f);
                    }
                }
                Iterator<Location> it = missiles.iterator();
                Iterator<Vector> itd = missileDirs.iterator();
                while (it.hasNext()) {
                    Location p = it.next();
                    Vector d = itd.next();
                    if (target.isOnline()) {
                        Vector toT = target.getLocation().toVector().subtract(p.toVector());
                        if (toT.lengthSquared() > 0.1) {
                            d.add(toT.normalize().multiply(0.04));
                            if (d.lengthSquared() > 1.5) d.normalize().multiply(1.2);
                        }
                    }
                    p.add(d);
                    world.spawnParticle(Particle.FLAME, p, 3, 0.05, 0.05, 0.05, 0);
                    world.spawnParticle(Particle.DUST, p, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0xFF6644), 1.5f));
                    for (Player pl : boss.getValidPlayers(world)) {
                        if (pl.getLocation().distanceSquared(p) < 5) {
                            pl.damage(sealDamage * 0.4);
                            pl.setFireTicks(40);
                            world.spawnParticle(Particle.EXPLOSION, p, 4, 0.3, 0.3, 0.3, 0);
                            it.remove();
                            itd.remove();
                            break;
                        }
                    }
                    if (p.distanceSquared(center) > 2500) {
                        it.remove();
                        itd.remove();
                    }
                }
                if (missilesFired >= 4 && missiles.isEmpty()) {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "arcanemissiles";
    }
}
