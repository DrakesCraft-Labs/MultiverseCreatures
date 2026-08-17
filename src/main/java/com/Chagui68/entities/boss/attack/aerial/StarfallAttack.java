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

public class StarfallAttack extends BossAttackBase {
    public StarfallAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                if (t < 20) {
                    double phase = (double) t / 20;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(30 * phase), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(-30 * phase), 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(30 * phase), 0, 0));
                    for (int a = 0; a < 8; a++) {
                        double angle = (2 * Math.PI * a / 8) + t * 0.04;
                        double r = 2.0 + phase * 4.0;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        Location pl = new Location(world, x, center.getY() + 2, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0xFFFFAA), 2.0f * (float) phase));
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.8f);
                } else if (t < 80) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(30), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(-30), 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(5), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(30), 0, 0));
                    if (t % 5 == 0) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double r = 5 + random.nextDouble() * 15;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        Location start = new Location(world, x, center.getY() + 15 + random.nextDouble() * 10, z);
                        double startGroundY = boss.getGroundY(start, 60);
                        world.spawnParticle(Particle.EXPLOSION, start, 2, 0.5, 0.5, 0.5, 0);
                        world.playSound(start, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.6f, 0.8f);
                        new BukkitRunnable() {
                            int ft = 0;

                            @Override
                            public void run() {
                                if (ft > 45) {
                                    cancel();
                                    return;
                                }
                                Location fall = start.clone().subtract(0, ft * 0.8, 0);
                                world.spawnParticle(Particle.FLAME, fall, 5, 0.5, 0.3, 0.5, 0.02);
                                world.spawnParticle(Particle.DUST, fall, 3, 0, 0, 0, 0,
                                        new Particle.DustOptions(Color.fromRGB(0xFFFFAA), 1.5f));
                                if (fall.getY() <= startGroundY + 0.5) {
                                    world.spawnParticle(Particle.EXPLOSION, fall, 8, 2, 0.5, 2, 0);
                                    world.spawnParticle(Particle.FLAME, fall, 15, 1, 0.3, 1, 0.05);
                                    world.playSound(fall, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.6f);
                                    double dmg = sealDamage * 0.6;
                                    for (Player p : boss.getValidPlayers(world)) {
                                        if (p.getLocation().distanceSquared(fall) < 25) {
                                            MscEntityUtils.damageBy(stand.entidad(), p, dmg);
                                            boss.launchPlayer(p, 0.6);
                                        }
                                    }
                                    cancel();
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
        return "starfall";
    }
}
