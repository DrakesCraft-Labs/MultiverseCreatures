package com.Chagui68.entities.boss.attack.ranged;

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

import java.util.List;

public class ChainLightningAttack extends BossAttackBase {
    public ChainLightningAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        List<Player> targets = boss.getValidPlayersNear(center, 10000);
        if (targets.isEmpty()) return;

        new BukkitRunnable() {
            int t = 0;
            int strikes = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 100) {
                    cancel();
                    return;
                }
                if (t < 25) {
                    double phase = (double) t / 25;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(60), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(-60), 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-20 * phase), 0, 0));
                    world.spawnParticle(Particle.ELECTRIC_SPARK, center.clone().add(0, 1, 0), 2, 1, 0.5, 1, 0.02);
                    if (t == 1) world.playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
                } else if (strikes < targets.size()) {
                    if (t % 12 == 0) {
                        Player p = targets.get(strikes % targets.size());
                        strikes++;
                        world.strikeLightningEffect(p.getLocation());
                        p.damage(sealDamage * 0.5);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 1));
                        for (Player near : boss.getValidPlayers(world)) {
                            if (near != p && near.getLocation().distanceSquared(p.getLocation()) < 16) {
                                near.damage(sealDamage * 0.3);
                                world.spawnParticle(Particle.ELECTRIC_SPARK, near.getLocation(), 8, 0.3, 0.5, 0.3, 0.1);
                            }
                        }
                        world.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.7f);
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
        return "chainlightning";
    }
}
