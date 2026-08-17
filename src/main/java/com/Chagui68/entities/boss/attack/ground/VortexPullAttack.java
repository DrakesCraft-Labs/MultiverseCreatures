package com.Chagui68.entities.boss.attack.ground;

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

public class VortexPullAttack extends BossAttackBase {
    public VortexPullAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnVortexSeal(center.clone().add(0, 0.5, 0), 90);
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
                    stand.setBodyPose(new EulerAngle(0, Math.toRadians(360 * phase), 0));
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(90 * phase), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-90 * phase), 0));
                    for (int a = 0; a < 20; a++) {
                        double angle = (2 * Math.PI * a / 20) + phase * Math.PI * 2;
                        double r = 0.5 + phase * 5.0;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        double y = center.getY() + 0.5 + Math.sin(angle * 3 + t * 0.1) * 0.5;
                        Location pl = new Location(world, x, y, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0xAA44FF), 1.8f * (float) phase));
                        world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.6f);
                } else if (t < 80) {
                    stand.setBodyPose(new EulerAngle(0, Math.toRadians(360 * (t - 30) / 50.0 + 360), 0));
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(90), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-90), 0));
                    double r = 5.5 + Math.sin(t * 0.1) * 0.5;
                    for (int a = 0; a < 30; a++) {
                        double angle = (2 * Math.PI * a / 30) + (t - 30) * 0.08;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        double y = center.getY() + 0.5 + Math.sin(angle * 3 + t * 0.1) * 0.3;
                        Location pl = new Location(world, x, y, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0xAA44FF), 1.5f));
                        world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                    }
                    for (int i = 0; i < 3; i++) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double rr = random.nextDouble() * 6;
                        Location pl = new Location(world, center.getX() + Math.cos(angle) * rr, center.getY() + 0.5 + random.nextDouble() * 3, center.getZ() + Math.sin(angle) * rr);
                        world.spawnParticle(Particle.WITCH, pl, 2, 0.2, 0.2, 0.2, 0);
                    }
                    for (Player p : boss.getValidPlayers(world)) {
                        Vector toCenter = center.toVector().subtract(p.getLocation().toVector());
                        double dist = toCenter.length();
                        if (dist < 8 && dist > 1) {
                            p.setVelocity(p.getVelocity().add(toCenter.normalize().multiply(0.15)));
                            MscEntityUtils.damageBy(stand.entidad(), p, 2.0);
                        } else if (dist < 1) {
                            MscEntityUtils.damageBy(stand.entidad(), p, 6.0);
                        }
                    }
                    world.playSound(center, Sound.BLOCK_BEACON_AMBIENT, 0.5f, 0.5f);
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
        return "vortexpull";
    }
}
