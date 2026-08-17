package com.Chagui68.entities.boss.attack.aerial;

import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class CrossSlashAttack extends BossAttackBase {
    public CrossSlashAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        double baseY = boss.getGroundY(center, 40) + 0.5;

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
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(30 * phase), Math.toRadians(-20 * phase)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(-30 * phase), Math.toRadians(20 * phase)));
                    stand.setBodyPose(new EulerAngle(0, 0, Math.toRadians(10 * phase)));
                    double r = 1.5 + phase * 3.0;
                    for (int a = 0; a < 12; a++) {
                        double angle = (2 * Math.PI * a / 12) + t * 0.04;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        Location pl = new Location(world, x, center.getY(), z);
                        world.spawnParticle(Particle.END_ROD, pl, 2, 0.2, 0.2, 0.2, 0.01);
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.0f);
                } else if (t < 50) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-160 + Math.sin((t - 30) * 0.3) * 30), Math.toRadians(30), Math.toRadians(-20)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-160 + Math.sin((t - 30) * 0.3 + Math.PI) * 30), Math.toRadians(-30), Math.toRadians(20)));
                    stand.setBodyPose(new EulerAngle(0, 0, Math.toRadians(10)));
                    double sz = 3.0 + (t - 30) * 0.5;
                    for (int d = 0; d < (int) sz; d++) {
                        double h = 0.5 + d * 0.3;
                        Location p1 = new Location(world, center.getX() + d * 0.5, baseY + h, center.getZ());
                        Location p2 = new Location(world, center.getX() - d * 0.5, baseY + h, center.getZ());
                        Location p3 = new Location(world, center.getX(), baseY + h, center.getZ() + d * 0.5);
                        Location p4 = new Location(world, center.getX(), baseY + h, center.getZ() - d * 0.5);
                        for (Location pl : new Location[]{p1, p2, p3, p4}) {
                            world.spawnParticle(Particle.CRIT, pl, 2, 0.1, 0.1, 0.1, 0.03);
                            world.spawnParticle(Particle.SWEEP_ATTACK, pl, 1, 0, 0, 0, 0);
                        }
                    }
                    world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 0.6f);
                    double dmg = sealDamage * 0.6;
                    for (Player p : boss.getValidPlayers(world)) {
                        Location pLoc = p.getLocation();
                        Vector diff = pLoc.toVector().subtract(center.toVector());
                        diff.setY(0);
                        if (diff.length() < sz && Math.abs(pLoc.getY() - baseY) < 3.5) {
                            MscEntityUtils.damageBy(stand, p, dmg);
                            boss.launchPlayer(p, 0.8);
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
        return "crossslash";
    }
}
