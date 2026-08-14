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

public class VoidBeamAttack extends BossAttackBase {
    public VoidBeamAttack(BossHost boss) {
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
            Location targetLastLoc = target.getLocation();

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || !target.isOnline()) {
                    cancel();
                    return;
                }
                if (t < 25) {
                    double phase = (double) t / 25;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(60 * phase), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(-60 * phase), 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-30 * phase), 0, 0));
                    if (t == 1) world.playSound(center, Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.4f);
                } else if (t < 105) {
                    targetLastLoc = target.getLocation();
                    Vector toTarget = targetLastLoc.toVector().subtract(center.toVector());
                    double dist = toTarget.length();
                    if (dist > 0.1) toTarget.normalize();
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-30), 0, 0));
                    Location beamPos = center.clone().add(0, 1.5, 0);
                    for (double d = 0; d < Math.min(dist, 30); d += 0.8) {
                        Location pl = beamPos.clone().add(toTarget.clone().multiply(d));
                        world.spawnParticle(Particle.PORTAL, pl, 3, 0.2, 0.2, 0.2, 0.05);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x660066), 2.0f));
                    }
                    if (t % 5 == 0) world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.6f, 0.3f);
                    for (Player p : boss.getValidPlayers(world)) {
                        Vector toP = p.getEyeLocation().toVector().subtract(center.toVector());
                        if (toP.lengthSquared() < 900) {
                            Vector norm = toP.clone().normalize();
                            double proj = norm.dot(toTarget);
                            if (proj > 0 && proj > 0.95) {
                                p.damage(sealDamage * 0.25);
                                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 0));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0));
                            }
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
        return "voidbeam";
    }
}
