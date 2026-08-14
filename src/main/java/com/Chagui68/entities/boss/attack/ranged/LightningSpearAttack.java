package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
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

public class LightningSpearAttack extends BossAttackBase {
    public LightningSpearAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        Player target = boss.detectTarget(stand);
        if (target == null) return;

        Vector dir = target.getLocation().toVector().subtract(center.toVector()).normalize();
        new BukkitRunnable() {
            int t = 0;
            double traveled = 0;
            Location pos = center.clone().add(0, 1.5, 0);
            final double speed = 1.8;
            final double maxRange = 30.0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 80) {
                    cancel();
                    return;
                }
                if (t < 18) {
                    double phase = (double) t / 18;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-150 * phase), Math.toRadians(45), 0));
                    if (t == 1) world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 1.2f);
                } else if (traveled < maxRange) {
                    pos.add(dir.clone().multiply(speed));
                    traveled += speed;
                    world.spawnParticle(Particle.ELECTRIC_SPARK, pos, 3, 0.1, 0.1, 0.1, 0.05);
                    world.spawnParticle(Particle.END_ROD, pos, 1, 0, 0, 0, 0.02);
                    for (Player p : boss.getValidPlayers(world)) {
                        if (p.getLocation().distanceSquared(pos) < 5) {
                            world.strikeLightningEffect(pos);
                            world.playSound(pos, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.8f);
                            p.damage(sealDamage * 0.7);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                            for (Player near : boss.getValidPlayers(world)) {
                                if (near.getLocation().distanceSquared(pos) < 16) {
                                    near.damage(sealDamage * 0.3);
                                    near.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
                                }
                            }
                            cancel();
                            return;
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
        return "lightningspear";
    }
}
