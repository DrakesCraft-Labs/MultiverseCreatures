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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class LightningStormAttack extends BossAttackBase {
    public LightningStormAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        double groundY = boss.getGroundY(center, 40);
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnStormSeal(new Location(world, center.getX(), groundY + 0.5, center.getZ()), 80);
        }

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
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(45 * phase), Math.toRadians(20 * phase)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(-45 * phase), Math.toRadians(-20 * phase)));
                    stand.setBodyPose(new EulerAngle(0, Math.toRadians(180 * phase), 0));
                    for (int a = 0; a < 10; a++) {
                        double angle = (2 * Math.PI * a / 10) + t * 0.06;
                        double r = 1.0 + phase * 3.0;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        world.spawnParticle(Particle.DUST, new Location(world, x, center.getY(), z), 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0xFFFF00), 2.0f * (float) phase));
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.6f);
                } else if (t < 80) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(45), Math.toRadians(20)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(-45), Math.toRadians(-20)));
                    stand.setBodyPose(new EulerAngle(0, Math.toRadians(180 + (t - 20) * 4), 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-20), 0, 0));
                    if (t % 6 == 0) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double r = 3 + random.nextDouble() * 12;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        Location strikeLoc = new Location(world, x, boss.getGroundY(new Location(world, x, center.getY(), z), 40), z);
                        world.strikeLightningEffect(strikeLoc);
                        world.spawnParticle(Particle.FLAME, strikeLoc, 20, 1, 0.5, 1, 0.05);
                        world.playSound(strikeLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.8f);
                        double dmg = sealDamage * 0.6;
                        for (Player p : boss.getValidPlayers(world)) {
                            if (p.getLocation().distanceSquared(strikeLoc) < 16) {
                                MscEntityUtils.damageBy(stand.entidad(), p, dmg);
                                boss.launchPlayer(p, 0.4);
                                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 2));
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
        return "lightningstorm";
    }
}
