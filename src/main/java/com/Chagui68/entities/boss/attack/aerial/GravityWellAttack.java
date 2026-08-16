package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
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

public class GravityWellAttack extends BossAttackBase {
    public GravityWellAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        double groundY = boss.getGroundY(center, 40);
        Location wellLoc = new Location(world, center.getX(), groundY, center.getZ());
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnVortexSeal(wellLoc.clone().add(0, 0.5, 0), 100);
        }

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                if (t < 30) {
                    double phase = (double) t / 30;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(10), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(-10), 0));
                    stand.setBodyPose(new EulerAngle(0, Math.toRadians(360 * phase), 0));
                    double r = phase * 6.0;
                    for (int a = 0; a < 20; a++) {
                        double angle = (2 * Math.PI * a / 20) + phase * Math.PI * 2;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        double y = wellLoc.getY() + Math.abs(Math.sin(angle * 3)) * 3;
                        Location pl = new Location(world, x, y, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x4400AA), 2.0f * (float) phase));
                        world.spawnParticle(Particle.PORTAL, pl, 1, 0, 0, 0, 0);
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.4f);
                } else if (t < 90) {
                    stand.setBodyPose(new EulerAngle(0, Math.toRadians(1080), 0));
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(-20), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(20), 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(20), 0, 0));
                    double r = 6.0 + Math.sin(t * 0.1) * 0.5;
                    for (int a = 0; a < 30; a++) {
                        double angle = (2 * Math.PI * a / 30) + (t - 30) * 0.05;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        double y = wellLoc.getY() + Math.abs(Math.sin(angle * 2 + t * 0.1)) * 4;
                        Location pl = new Location(world, x, y, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x4400AA), 1.8f));
                        world.spawnParticle(Particle.PORTAL, pl, 1, 0, 0, 0, 0);
                    }
                    for (Player p : boss.getValidPlayers(world)) {
                        Vector toCenter = wellLoc.toVector().subtract(p.getLocation().toVector());
                        toCenter.setY(0);
                        double dist = toCenter.length();
                        if (dist < 10 && dist > 2) {
                            p.setVelocity(p.getVelocity().add(toCenter.normalize().multiply(0.2)));
                            MscEntityUtils.damageBy(stand, p, 3.0);
                        } else if (dist <= 2) {
                            MscEntityUtils.damageBy(stand, p, 8.0);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 3));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 2));
                        }
                    }
                    world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 0.3f);
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
        return "gravitywell";
    }
}
