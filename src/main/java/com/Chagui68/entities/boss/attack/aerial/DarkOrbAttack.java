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

import java.util.ArrayList;
import java.util.List;

public class DarkOrbAttack extends BossAttackBase {
    public DarkOrbAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();

        new BukkitRunnable() {
            int t = 0;
            List<Player> orbTargets = new ArrayList<>();
            int orbIndex = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                if (t < 20) {
                    double phase = (double) t / 20;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase + 90), Math.toRadians(30 * phase), Math.toRadians(10 * phase)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase + 90), Math.toRadians(-30 * phase), Math.toRadians(-10 * phase)));
                    stand.setBodyPose(new EulerAngle(0, Math.toRadians(180 * phase), 0));
                    double r = 1.0 + phase * 2.0;
                    for (int a = 0; a < 10; a++) {
                        double angle = (2 * Math.PI * a / 10) + t * 0.05;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        double y = center.getY() + Math.sin(angle * 2) * 1.0;
                        Location pl = new Location(world, x, y, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x8800AA), 2.0f * (float) phase));
                        world.spawnParticle(Particle.WITCH, pl, 1, 0, 0, 0, 0);
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.5f);
                } else if (t == 20) {
                    orbTargets.addAll(boss.getValidPlayers(world));
                    if (orbTargets.isEmpty()) {
                        boss.resetBossPose(instance);
                        cancel();
                        return;
                    }
                } else if (t < 80 && orbIndex < orbTargets.size()) {
                    Player target = orbTargets.get(orbIndex % orbTargets.size());
                    if (t % 10 == 0) {
                        Location start = center.clone().add(0, 2, 0);
                        world.spawnParticle(Particle.EXPLOSION, start, 3, 0.5, 0.5, 0.5, 0);
                        world.playSound(start, Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.8f);
                        new BukkitRunnable() {
                            int ft = 0;

                            @Override
                            public void run() {
                                if (ft > 30 || stand.isDead() || !target.isOnline() || target.isDead()) {
                                    cancel();
                                    return;
                                }
                                double progress = (double) ft / 30;
                                Location orbLoc = start.clone().add(target.getLocation().toVector().subtract(start.toVector()).multiply(progress));
                                orbLoc.setY(orbLoc.getY() + 2);
                                world.spawnParticle(Particle.DUST, orbLoc, 3, 0, 0, 0, 0,
                                        new Particle.DustOptions(Color.fromRGB(0x8800AA), 2.5f));
                                world.spawnParticle(Particle.WITCH, orbLoc, 2, 0.2, 0.2, 0.2, 0);
                                world.spawnParticle(Particle.END_ROD, orbLoc, 1, 0, 0, 0, 0);
                                if (progress > 0.8) {
                                    world.spawnParticle(Particle.EXPLOSION, target.getLocation().add(0, 1, 0), 10, 1, 0.5, 1, 0);
                                    world.playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.7f);
                                    double dmg = sealDamage * 0.8;
                                    MscEntityUtils.damageBy(stand, target, dmg);
                                    target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1));
                                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                                    cancel();
                                }
                                ft++;
                            }
                        }.runTaskTimer(plugin, 0L, 1L);
                        orbIndex++;
                        if (orbIndex >= orbTargets.size() * 2) orbIndex = orbTargets.size();
                    }
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(30), Math.toRadians(10)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-30), Math.toRadians(-10)));
                    stand.setBodyPose(new EulerAngle(0, Math.toRadians((t - 20) * 6), 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-15), 0, 0));
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
        return "darkorb";
    }
}
