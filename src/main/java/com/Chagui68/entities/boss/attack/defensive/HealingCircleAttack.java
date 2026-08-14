package com.Chagui68.entities.boss.attack.defensive;

import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.EulerAngle;

import java.util.Random;

public class HealingCircleAttack extends BossAttackBase {

    public HealingCircleAttack(BossHost boss) {
        super(boss);
    }

    @Override
    public void execute(BossInstance instance) {
        startHealingCircle(instance, true);
    }

    public void startHealingCircle(BossInstance instance, boolean telegraph) {
        if (instance.healingCircleActive) return;
        if (instance.isFlying) return;
        BossPuppet stand = instance.stand;
        if (stand.isDead() || !stand.isValid()) return;
        World world = stand.getWorld();

        if (telegraph) {
            world.playSound(stand.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 1.2f);
        }

        double maxHealth = stand.getAttribute(Attribute.MAX_HEALTH) != null
                ? stand.getAttribute(Attribute.MAX_HEALTH).getValue() : 500.0;
        double maxHeal = maxHealth * 0.03;

        instance.healingCircleActive = true;
        instance.healingCircleTimer = 0;
        instance.healingCircleHealed = 0;

        instance.healingCircleTask = new org.bukkit.scheduler.BukkitRunnable() {
            int t = 0;
            boolean casting = telegraph;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    removeHealingCircle(instance);
                    cancel();
                    return;
                }

                Location center = stand.getLocation();
                double radius = 4.0;

                if (casting) {
                    t++;
                    double phase = Math.min(1.0, (double) t / 30);

                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-140 * phase), 0, 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-140 * phase), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-15 * phase), 0, 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-5 * phase), 0, 0));

                    int samples = (int) (10 + phase * 25);
                    for (int i = 0; i < samples; i++) {
                        double angle = (2 * Math.PI * i / samples) + t * 0.03;
                        double x = center.getX() + Math.cos(angle) * radius * phase;
                        double z = center.getZ() + Math.sin(angle) * radius * phase;
                        double y = center.getY() + 0.1 + Math.sin(t * 0.15 + i * 0.5) * 0.2;
                        Location pl = new Location(world, x, y, z);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new org.bukkit.Particle.DustOptions(Color.fromRGB(0x44FF44), 1.2f * (float) phase));
                    }

                    for (int i = 0; i < (int) (2 + phase * 5); i++) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        double r = random.nextDouble() * radius * phase;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        Location pl = new Location(world, x, center.getY() + 0.3 + random.nextDouble() * 2 * phase, z);
                        world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.HEART, pl, 1, 0, 0, 0, 0);
                    }

                    if (t >= 35) {
                        casting = false;
                        t = 0;
                        boss.resetBossPose(instance);
                        world.playSound(center, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.6f);
                        world.spawnParticle(Particle.EXPLOSION, center.clone().add(0, 0.5, 0), 8, 2.0, 0.5, 2.0, 0);
                    }
                    return;
                }

                t++;
                int samples = 30;
                for (int i = 0; i < samples; i++) {
                    double angle = (2 * Math.PI * i / samples) + (t * 0.02);
                    double x = center.getX() + Math.cos(angle) * radius;
                    double z = center.getZ() + Math.sin(angle) * radius;
                    double y = center.getY() + 0.1 + Math.sin(t * 0.1 + i) * 0.1;
                    Location pl = new Location(world, x, y, z);
                    world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                            new org.bukkit.Particle.DustOptions(Color.fromRGB(0x44FF44), 1.5f));
                }

                for (int i = 0; i < 5; i++) {
                    double angle = random.nextDouble() * Math.PI * 2;
                    double r = random.nextDouble() * radius;
                    double x = center.getX() + Math.cos(angle) * r;
                    double z = center.getZ() + Math.sin(angle) * r;
                    Location pl = new Location(world, x, center.getY() + 0.5 + random.nextDouble() * 2, z);
                    world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.HEART, pl, 1, 0, 0, 0, 0);
                }

                double maxHealth = stand.getAttribute(Attribute.MAX_HEALTH) != null
                        ? stand.getAttribute(Attribute.MAX_HEALTH).getValue() : 500.0;
                double maxHeal = maxHealth * 0.03;
                if (instance.healingCircleHealed < maxHeal && stand.getHealth() < maxHealth) {
                    double healAmount = maxHealth * 0.0015;
                    double remaining = maxHeal - instance.healingCircleHealed;
                    double toHeal = Math.min(healAmount, remaining);
                    toHeal = Math.min(toHeal, maxHealth - stand.getHealth());
                    if (toHeal > 0) {
                        stand.setHealth(stand.getHealth() + toHeal);
                        instance.healingCircleHealed += toHeal;
                        if (instance.bossBar != null) {
                            instance.bossBar.setProgress(stand.getHealth() / maxHealth);
                        }
                    }
                }

                if (t >= 200) {
                    removeHealingCircle(instance);
                    cancel();
                }
            }
        };
        instance.healingCircleTask.runTaskTimer(plugin, 0L, 1L);
    }

    private void removeHealingCircle(BossInstance instance) {
        instance.healingCircleActive = false;
        instance.healingCircleTimer = 0;
        instance.healingCircleHealed = 0;
        if (instance.healingCircleTask != null) {
            instance.healingCircleTask.cancel();
            instance.healingCircleTask = null;
        }
    }

    @Override
    public String getName() {
        return "healingcircle";
    }
}