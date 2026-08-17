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

public class AirSlamAttack extends BossAttackBase {
    public AirSlamAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        execute(instance, true);
    }

    public void execute(BossInstance instance, boolean telegraph) {
        if (!instance.isFlying) return;
        BossPuppet stand = instance.stand;
        if (stand.isDead() || !stand.isValid()) return;
        World world = stand.getWorld();

        instance.aerialAttacksDone.clear();
        double targetY = boss.getGroundY(stand.getLocation(), 80);

        if (instance.flyTask != null) {
            instance.flyTask.cancel();
            instance.flyTask = null;
        }

        if (telegraph) {
            world.playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.3f);
        }

        instance.flyTask = new BukkitRunnable() {
            int tick = 0;
            boolean windup = telegraph;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    instance.flyTask = null;
                    cancel();
                    return;
                }

                if (windup) {
                    tick++;
                    Location loc = stand.getLocation();

                    if (tick <= 20) {
                        double phase = (double) tick / 20;

                        stand.setRightArmPose(new EulerAngle(
                                Math.toRadians(160 * phase + 180),
                                Math.toRadians(45 * phase),
                                Math.toRadians(20 * phase)
                        ));
                        stand.setLeftArmPose(new EulerAngle(
                                Math.toRadians(160 * phase + 180),
                                Math.toRadians(-45 * phase),
                                Math.toRadians(-20 * phase)
                        ));
                        stand.setBodyPose(new EulerAngle(Math.toRadians(45 * phase), 0, 0));
                        stand.setHeadPose(new EulerAngle(Math.toRadians(45 * phase), 0, 0));
                        stand.setRightLegPose(new EulerAngle(Math.toRadians(20 * phase), 0, 0));
                        stand.setLeftLegPose(new EulerAngle(Math.toRadians(-20 * phase), 0, 0));

                        world.spawnParticle(Particle.FLAME, loc, 4, 1.0, 0.5, 1.0, 0.02);
                        world.spawnParticle(Particle.SMOKE, loc, 3, 0.8, 0.3, 0.8, 0.04);
                        world.spawnParticle(Particle.CRIT, loc.clone().add(0, -1, 0), 3, 0.5, 0.1, 0.5, 0.03);

                        double ringR = 1.5 + phase * 4;
                        for (int a = 0; a < 16; a++) {
                            double angle = (2 * Math.PI * a / 16);
                            double x = loc.getX() + Math.cos(angle) * ringR;
                            double z = loc.getZ() + Math.sin(angle) * ringR;
                            Location pl = new Location(world, x, targetY + 0.5, z);
                            world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(0xFF4400), 1.8f * (float) phase));
                        }
                    }

                    if (tick == 10) {
                        world.playSound(loc, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.8f);
                    }
                    if (tick % 4 == 0 && tick > 0) {
                        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f * (float) (tick / 20.0), 0.3f);
                    }

                    if (tick >= 25) {
                        windup = false;
                        tick = 0;
                        instance.isFlying = false;
                        instance.flyingTimer = 0;
                        stand.setRightArmPose(new EulerAngle(Math.toRadians(180 + 160), Math.toRadians(45), Math.toRadians(20)));
                        stand.setLeftArmPose(new EulerAngle(Math.toRadians(180 + 160), Math.toRadians(-45), Math.toRadians(-20)));
                        stand.setBodyPose(new EulerAngle(Math.toRadians(45), 0, 0));
                        stand.setHeadPose(new EulerAngle(Math.toRadians(45), 0, 0));
                        stand.setRightLegPose(new EulerAngle(Math.toRadians(20), 0, 0));
                        stand.setLeftLegPose(new EulerAngle(Math.toRadians(-20), 0, 0));
                        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.3f);
                    }
                    return;
                }

                Location loc = stand.getLocation();
                stand.setRightArmPose(new EulerAngle(Math.toRadians(180 + 160), Math.toRadians(45), Math.toRadians(20)));
                stand.setLeftArmPose(new EulerAngle(Math.toRadians(180 + 160), Math.toRadians(-45), Math.toRadians(-20)));
                stand.setBodyPose(new EulerAngle(Math.toRadians(60), 0, 0));
                stand.setHeadPose(new EulerAngle(Math.toRadians(70), 0, 0));

                double newY = loc.getY() - 1.6;
                if (newY <= targetY || tick >= 30) {
                    loc.setY(Math.max(targetY, newY));
                    stand.teleport(loc);

                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(70), Math.toRadians(0)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-70), Math.toRadians(0)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(100), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(80), 0, 0));
                    stand.setRightLegPose(new EulerAngle(Math.toRadians(-15), 0, 0));
                    stand.setLeftLegPose(new EulerAngle(Math.toRadians(15), 0, 0));

                    world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.5f, 0.3f);
                    world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.3f);
                    world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.5f);

                    world.spawnParticle(Particle.EXPLOSION, loc, 20, 3.0, 1.0, 3.0, 0);
                    world.spawnParticle(Particle.CLOUD, loc, 100, 6.0, 1.5, 6.0, 0.3);
                    world.spawnParticle(Particle.FLAME, loc, 60, 3.0, 0.8, 3.0, 0.08);

                    final double ATTACK_RADIUS = 30.0;
                    final double impactX = loc.getX();
                    final double impactY = loc.getY();
                    final double impactZ = loc.getZ();

                    double damage = sealDamage;
                    for (Player p : boss.getValidPlayers(world)) {
                        double dist = p.getLocation().distance(loc);
                        if (dist <= ATTACK_RADIUS) {
                            MscEntityUtils.damageBy(stand, p, damage * (1 - dist / ATTACK_RADIUS * 0.5));
                            boss.launchPlayer(p, 0.8 + (1 - dist / ATTACK_RADIUS) * 0.5);
                        }
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            boss.spawnShockwaveWave(world, new Location(world, impactX, impactY, impactZ), ATTACK_RADIUS);
                        }
                    }.runTask(plugin);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            boss.resetBossPose(instance);
                        }
                    }.runTaskLater(plugin, 60L);

                    instance.flyTask = null;
                    cancel();
                    return;
                }

                tick++;
                loc.setY(newY);
                stand.teleport(loc);
                world.spawnParticle(Particle.CRIT, loc, 5, 0.5, 0.3, 0.5, 0.03);
                world.spawnParticle(Particle.FLAME, loc, 3, 0.3, 0.1, 0.3, 0.01);
            }
        };
        instance.flyTask.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "airslam";
    }
}
