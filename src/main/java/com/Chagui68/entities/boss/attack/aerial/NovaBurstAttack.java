package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.utils.MscEntityUtils;
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

public class NovaBurstAttack extends BossAttackBase {
    public NovaBurstAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        double groundY = boss.getGroundY(center, 40);
        Location boomLoc = new Location(world, center.getX(), groundY + 1.0, center.getZ());
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnLargePentagramSeal(boomLoc.clone().add(0, 0.5, 0), 50, 8.0, MagicSealListener.Plane.XZ);
        }

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                if (t < 25) {
                    double phase = (double) t / 25;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase + 90), Math.toRadians(80 * phase), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase + 90), Math.toRadians(-80 * phase), 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-30 * phase), 0, 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(15 * phase), 0, 0));
                    double r = 1.0 + phase * 5.0;
                    for (int a = 0; a < 24; a++) {
                        double angle = (2 * Math.PI * a / 24) + t * 0.05;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        double y = center.getY() + Math.sin(angle * 2) * 1.5;
                        Location pl = new Location(world, x, y, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0xFF6600), 2.0f * (float) phase));
                        world.spawnParticle(Particle.FLAME, pl, 1, 0, 0, 0, 0);
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.2f, 0.5f);
                } else if (t == 25) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-45), Math.toRadians(90), Math.toRadians(0)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-45), Math.toRadians(-90), Math.toRadians(0)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-20), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-30), 0, 0));
                    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 0.4f);
                    world.spawnParticle(Particle.EXPLOSION, boomLoc, 30, 6, 3, 6, 0);
                    world.spawnParticle(Particle.FLAME, boomLoc, 80, 4, 2, 4, 0.08);
                    world.spawnParticle(Particle.CLOUD, boomLoc, 60, 5, 2, 5, 0.15);
                    double dmg = sealDamage;
                    for (Player p : boss.getValidPlayers(world)) {
                        double dist = p.getLocation().distance(boomLoc);
                        if (dist < 20) {
                            MscEntityUtils.damageBy(stand, p, dmg * (1 - dist / 20 * 0.6));
                            boss.launchPlayer(p, 1.0 + (1 - dist / 20) * 0.5);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                        }
                    }
                } else if (t < 40) {
                    for (int a = 0; a < 20; a++) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double r = random.nextDouble() * 8;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        world.spawnParticle(Particle.FLAME, new Location(world, x, boomLoc.getY() + random.nextDouble() * 4, z), 2, 0.2, 0.2, 0.2, 0.02);
                        world.spawnParticle(Particle.SMOKE, new Location(world, x, boomLoc.getY() + random.nextDouble() * 3, z), 1, 0.3, 0.3, 0.3, 0.03);
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
        return "novaburst";
    }
}
