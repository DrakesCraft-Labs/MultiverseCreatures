package com.Chagui68.entities.boss.attack.defensive;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.MagicSealListener;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

import java.util.Random;

public class AbsorbShieldAttack extends BossAttackBase {

    public AbsorbShieldAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.activeDefense != instance.activeDefense.NONE || !boss.isOnGround(instance.stand)) return;
        instance.activeDefense = instance.activeDefense.ABSORB_SHIELD;
        instance.defenseTimer = 0;
        instance.absorbShieldHealth = 100.0;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        world.playSound(loc, Sound.ITEM_SHIELD_BLOCK, 2.0f, 1.5f);
        world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 0.5f);
        world.spawnParticle(Particle.FLASH, loc.clone().add(0, 5, 0), 1,
                Color.WHITE);
        world.spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 5, 0), 30, 3, 5, 3, 0);

        stand.setBodyPose(new EulerAngle(Math.toRadians(5), 0, 0));
        stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(30), Math.toRadians(0)));
        stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-30), Math.toRadians(0)));
        stand.setHeadPose(new EulerAngle(Math.toRadians(5), 0, 0));

        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnCelestialSeal(stand, 300);
        }

        instance.defenseTask = new org.bukkit.scheduler.BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || instance.activeDefense != instance.activeDefense.ABSORB_SHIELD) {
                    cancel();
                    return;
                }
                if (t % 5 == 0) {
                    double healthPct = instance.absorbShieldHealth / 100.0;
                    Color shieldColor = healthPct > 0.5 ? Color.fromRGB(0x88CCFF) : Color.fromRGB(0xFF6666);
                    world.spawnParticle(Particle.DUST, stand.getLocation().add(0, 5, 0), 8, 2, 4, 2, 0,
                            new org.bukkit.Particle.DustOptions(shieldColor, 2.0f));
                    world.spawnParticle(Particle.END_ROD, stand.getLocation().add(0, 5, 0), 5, 1.5, 3, 1.5, 0.02);
                }
                t++;
            }
        };
        instance.defenseTask.runTaskTimer(plugin, 0L, 3L);
    }

    @Override
    public String getName() {
        return "absorbshield";
    }
}