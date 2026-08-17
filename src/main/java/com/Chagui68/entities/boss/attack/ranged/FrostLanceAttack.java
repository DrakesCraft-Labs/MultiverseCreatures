package com.Chagui68.entities.boss.attack.ranged;

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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class FrostLanceAttack extends BossAttackBase {
    public FrostLanceAttack(BossHost boss) {
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
            final double speed = 1.6;
            final double maxRange = 35.0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 100) {
                    cancel();
                    return;
                }
                if (t < 22) {
                    double phase = (double) t / 22;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-120 * phase), Math.toRadians(20), 0));
                    if (t == 1) world.playSound(center, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
                } else if (traveled < maxRange) {
                    pos.add(dir.clone().multiply(speed));
                    traveled += speed;
                    world.spawnParticle(Particle.SNOWFLAKE, pos, 3, 0.15, 0.15, 0.15, 0);
                    world.spawnParticle(Particle.DUST, pos, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0x88DDFF), 1.5f));
                    for (Player p : boss.getValidPlayers(world)) {
                        if (p.getLocation().distanceSquared(pos) < 6) {
                            MscEntityUtils.damageBy(stand.entidad(), p, sealDamage * 0.8);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 3));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, -4));
                            world.spawnParticle(Particle.EXPLOSION, pos, 5, 0.5, 0.5, 0.5, 0);
                            world.playSound(pos, Sound.ENTITY_PLAYER_HURT_FREEZE, 1.2f, 0.6f);
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
        return "frostlance";
    }
}
