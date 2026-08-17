package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossPuppet;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class WindCutterAttack extends BossAttackBase {
    public WindCutterAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        double baseY = boss.getGroundY(center, 40) + 1.0;

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
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase + 90), Math.toRadians(45 * phase), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase + 90), Math.toRadians(-45 * phase), 0));
                    for (int a = 0; a < 8; a++) {
                        double angle = (2 * Math.PI * a / 8) + t * 0.08;
                        double r = 1.5 + phase * 2.0;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        world.spawnParticle(Particle.END_ROD, new Location(world, x, center.getY(), z), 1, 0, 0, 0, 0);
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.7f);
                } else if (t < 45) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(45 + Math.sin((t - 15) * 0.3) * 15), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-45 + Math.sin((t - 15) * 0.3 + Math.PI) * 15), 0));
                    stand.setBodyPose(new EulerAngle(0, 0, Math.toRadians(5)));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(10), 0, 0));
                    for (int a = 0; a < 6; a++) {
                        double angle = (2 * Math.PI * a / 6);
                        Location start = new Location(world, center.getX() + Math.cos(angle) * 3, baseY, center.getZ() + Math.sin(angle) * 3);
                        world.playSound(start, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.4f, 0.9f);
                        new BukkitRunnable() {
                            int ft = 0;
                            final double fAngle = angle;

                            @Override
                            public void run() {
                                if (ft > 20) {
                                    cancel();
                                    return;
                                }
                                double dist = ft * 1.2;
                                double x = start.getX() + Math.cos(fAngle) * dist;
                                double z = start.getZ() + Math.sin(fAngle) * dist;
                                double y = start.getY() + Math.sin(ft * 0.3) * 0.5;
                                Location wLoc = new Location(world, x, y, z);
                                world.spawnParticle(Particle.DUST, wLoc, 2, 0, 0, 0, 0,
                                        new Particle.DustOptions(Color.fromRGB(0xAAFFEE), 2.0f));
                                world.spawnParticle(Particle.CRIT, wLoc, 2, 0.2, 0.2, 0.2, 0.03);
                                world.spawnParticle(Particle.SWEEP_ATTACK, wLoc, 1, 0, 0, 0, 0);
                                double dmg = sealDamage * 0.25;
                                for (Player p : boss.getValidPlayers(world)) {
                                    if (p.getLocation().distanceSquared(wLoc) < 9) {
                                        MscEntityUtils.damageBy(stand, p, dmg);
                                        p.setVelocity(p.getVelocity().add(new Vector(Math.cos(fAngle) * 0.5, 0.2, Math.sin(fAngle) * 0.5)));
                                    }
                                }
                                ft++;
                            }
                        }.runTaskTimer(plugin, 0L, 1L);
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
        return "windcutter";
    }
}
