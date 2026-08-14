package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
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

import java.util.List;

public class SpiritBeamAttack extends BossAttackBase {
    public SpiritBeamAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnDivineSeal(center.clone().add(0, 0.5, 0), 60);
        }
        List<Player> targets = boss.getValidPlayersNear(center, 10000);

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 95) {
                    cancel();
                    return;
                }
                if (t < 35) {
                    double phase = (double) t / 35;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(45), Math.toRadians(40 * phase)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180 * phase), Math.toRadians(-45), Math.toRadians(-40 * phase)));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-30 * phase), 0, 0));
                    for (int a = 0; a < 16; a++) {
                        double angle = (2 * Math.PI * a / 16) + t * 0.05;
                        double r = 3.0 + phase * 4;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        Location pl = new Location(world, x, center.getY() + 1, z);
                        world.spawnParticle(Particle.SOUL, pl, 1, 0.1, 0.3, 0.1, 0.02);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x44FFCC), 1.5f * (float) phase));
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.5f);
                    if (t % 6 == 0) world.playSound(center, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 0.8f, 0.4f);
                } else if (t < 90) {
                    if (t == 35) {
                        world.spawnParticle(Particle.FLASH, center.clone().add(0, 1, 0), 1,
                                Color.WHITE);
                        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.5f);
                    }
                    for (Player p : targets) {
                        if (!p.isOnline() || p.isDead()) continue;
                        Vector toP = p.getEyeLocation().toVector().subtract(center.toVector());
                        double dist = toP.length();
                        if (dist > 0.1) toP.normalize();
                        for (double d = 0; d < Math.min(dist, 25); d += 0.6) {
                            Location pl = center.clone().add(0, 1.5, 0).add(toP.clone().multiply(d));
                            world.spawnParticle(Particle.SOUL, pl, 2, 0.2, 0.2, 0.2, 0.05);
                            world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(0x44FFCC), 2.0f));
                            world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                        }
                        if (t % 10 == 0) {
                            p.damage(sealDamage * 0.4);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 50, 1));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 50, 1));
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
        return "spiritbeam";
    }
}
