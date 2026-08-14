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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShadowVolleyAttack extends BossAttackBase {
    public ShadowVolleyAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        Player target = boss.detectTarget(stand);
        if (target == null) return;

        Vector baseDir = target.getLocation().toVector().subtract(center.toVector()).normalize();
        Vector right = baseDir.clone().crossProduct(new Vector(0, 1, 0)).normalize();
        final double spreadAngle = 0.45;

        new BukkitRunnable() {
            int t = 0;
            int volley = 0;
            final List<Location> projectiles = new ArrayList<>();
            final List<Vector> directions = new ArrayList<>();

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 80) {
                    cancel();
                    return;
                }
                if (t < 20) {
                    double phase = (double) t / 20;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-120 * phase), Math.toRadians(30), Math.toRadians(40 * phase)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-120 * phase), Math.toRadians(-30), Math.toRadians(-40 * phase)));
                    if (t == 1) world.playSound(center, Sound.ENTITY_WITHER_SHOOT, 0.8f, 0.4f);
                } else if (volley < 5) {
                    if (t % 3 == 0) {
                        double offset = (volley - 2) * spreadAngle;
                        Vector dir = baseDir.clone();
                        Vector finalDir = dir.clone().multiply(Math.cos(offset))
                                .add(right.clone().multiply(Math.sin(offset))).normalize();
                        projectiles.add(center.clone().add(0, 1.5, 0));
                        directions.add(finalDir);
                        volley++;
                        world.playSound(center, Sound.ENTITY_ARROW_SHOOT, 0.7f, 0.6f);
                    }
                }
                Iterator<Location> it = projectiles.iterator();
                Iterator<Vector> itd = directions.iterator();
                while (it.hasNext()) {
                    Location p = it.next();
                    Vector d = itd.next();
                    p.add(d.clone().multiply(1.3));
                    world.spawnParticle(Particle.PORTAL, p, 3, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.DUST, p, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0x330044), 1.5f));
                    for (Player pl : boss.getValidPlayers(world)) {
                        if (pl.getLocation().distanceSquared(p) < 4) {
                            pl.damage(sealDamage * 0.5);
                            pl.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                            pl.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                            world.spawnParticle(Particle.EXPLOSION, p, 3, 0.3, 0.3, 0.3, 0);
                            it.remove();
                            itd.remove();
                            break;
                        }
                    }
                    if (p.distanceSquared(center) > 1600) {
                        it.remove();
                        itd.remove();
                    }
                }
                if (volley >= 5 && projectiles.isEmpty()) {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "shadowvolley";
    }
}
