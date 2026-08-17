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

public class ArmorSpikesAttack extends BossAttackBase {
    public ArmorSpikesAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
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
                if (t < 25) {
                    double phase = (double) t / 25;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(80 * phase), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(-80 * phase), 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(5 * phase), 0, 0));
                    double r = 2.0 + phase * 4.0;
                    for (int a = 0; a < 16; a++) {
                        double angle = (2 * Math.PI * a / 16) + t * 0.05;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        Location pl = new Location(world, x, center.getY() + phase * 4, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x666666), 1.5f * (float) phase));
                        world.spawnParticle(Particle.CRIT, pl, 1, 0.1, 0.1, 0.1, 0.02);
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.7f);
                } else if (t < 45) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(80), Math.toRadians(90)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-80), Math.toRadians(-90)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(15), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-5), 0, 0));
                    for (int a = 0; a < 24; a++) {
                        double angle = (2 * Math.PI * a / 24) + t * 0.1;
                        double r = 3.0 + Math.sin(t * 0.2 + a) * 2.0;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        Location pl = new Location(world, x, center.getY() + 0.5, z);
                        world.spawnParticle(Particle.SWEEP_ATTACK, pl, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.CRIT, pl, 1, 0.2, 0.2, 0.2, 0.03);
                    }
                    if (t % 3 == 0) {
                        world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f, 0.9f);
                        double dmg = sealDamage * 0.25;
                        for (Player p : boss.getValidPlayers(world)) {
                            if (p.getLocation().distanceSquared(center) < 49) {
                                MscEntityUtils.damageBy(stand.entidad(), p, dmg);
                                Vector away = p.getLocation().toVector().subtract(center.toVector());
                                if (away.lengthSquared() > 0) p.setVelocity(away.normalize().multiply(0.8).setY(0.3));
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
        return "armorspikes";
    }
}
