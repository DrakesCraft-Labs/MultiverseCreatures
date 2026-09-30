package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * Shadow Step — the boss vanishes through a sigil and reappears behind the target.
 *
 * <p>ANIMATION (what tells it apart on screen)
 * <ul>
 *   <li>0-11: he hunches with his arms crossed while a small pentagram is drawn under his feet and
 *       the body dissolves into dark smoke.</li>
 *   <li>12: he is <b>gone</b> — the only attack that teleports the boss. He lands 1.35 blocks behind
 *       the target through a second sigil.</li>
 *   <li>13-23: a full 360 spin with arms straight out, closing into a backstab burst that throws
 *       survivors away from him.</li>
 * </ul>
 */
public class ShadowStepAttack extends BossAttackBase {

    private static final int FADE_TICKS = 12;
    private static final int SPIN_TICKS = 10;
    private static final double HIT_RADIUS = 3.4;

    private final double damage;

    public ShadowStepAttack(BossHost boss) {
        super(boss);
        this.damage = plugin.getConfig().getDouble("entities.armor-stand-boss.shadow-step-damage", 15.0);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Player target = boss.detectTarget(stand);
        if (target == null) return;
        LivingEntity attacker = stand.entidad();

        new BukkitRunnable() {
            int t = 0;
            boolean moved = false;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > 50) {
                    cancel();
                    return;
                }

                Location center = stand.getLocation();

                if (t < FADE_TICKS) {
                    double p = (double) t / FADE_TICKS;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-20 * p), Math.toRadians(-75 * p), Math.toRadians(-30 * p)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-20 * p), Math.toRadians(75 * p), Math.toRadians(30 * p)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(24 * p), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(20 * p), 0, 0));

                    world.spawnParticle(Particle.SCULK_SOUL, center.clone().add(0, 0.4 + p, 0), 3, 0.35, 0.25, 0.35, 0.01);
                    world.spawnParticle(Particle.SMOKE, center.clone().add(0, 0.2, 0), 4, 0.4, 0.2, 0.4, 0.01);
                    if (t == 0) {
                        world.playSound(center, Sound.ENTITY_ENDERMAN_SCREAM, 1.1f, 0.55f);
                        world.playSound(center, Sound.BLOCK_SCULK_SPREAD, 1.0f, 0.7f);
                        if (plugin.getMagicSealListener() != null) {
                            plugin.getMagicSealListener().spawnPentagramSeal(center.clone().add(0, 0.2, 0), 50,
                                    MagicSealListener.Plane.XZ);
                        }
                    }
                } else if (!moved) {
                    moved = true;
                    Vector behind = target.getLocation().getDirection().setY(0);
                    if (behind.lengthSquared() < 0.01) behind = new Vector(0, 0, 1);
                    Location arrival = target.getLocation().clone().subtract(behind.normalize().multiply(1.35));
                    arrival.setYaw(target.getLocation().getYaw());
                    arrival.setPitch(0);

                    world.spawnParticle(Particle.REVERSE_PORTAL, center.clone().add(0, 1, 0), 40, 0.4, 0.8, 0.4, 0.1);
                    world.spawnParticle(Particle.CLOUD, center.clone().add(0, 1, 0), 14, 0.4, 0.6, 0.4, 0.05);
                    world.playSound(center, Sound.ENTITY_ENDERMAN_TELEPORT, 1.4f, 1.2f);

                    stand.teleport(arrival);

                    world.spawnParticle(Particle.PORTAL, arrival.clone().add(0, 1, 0), 60, 0.4, 0.9, 0.4, 0.12);
                    world.spawnParticle(Particle.SCULK_CHARGE_POP, arrival.clone().add(0, 1, 0), 20, 0.4, 0.6, 0.4, 0);
                    world.playSound(arrival, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.2f, 1.3f);
                    world.playSound(arrival, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.4f);
                    if (plugin.getMagicSealListener() != null) {
                        plugin.getMagicSealListener().spawnPentagramSeal(arrival.clone().add(0, 0.2, 0), 45,
                                MagicSealListener.Plane.XZ);
                    }
                } else if (t < FADE_TICKS + SPIN_TICKS + 1) {
                    int spinTick = t - FADE_TICKS;
                    float spin = (float) (spinTick / (double) SPIN_TICKS) * (float) (Math.PI * 2);
                    stand.setBodyPose(new EulerAngle(0, spin, 0));
                    stand.setRightArmPose(new EulerAngle(0, Math.toRadians(95), 0));
                    stand.setLeftArmPose(new EulerAngle(0, Math.toRadians(-95), 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-6), 0, 0));
                    stand.setRightLegPose(new EulerAngle(Math.toRadians(-8), 0, 0));
                    stand.setLeftLegPose(new EulerAngle(Math.toRadians(8), 0, 0));

                    Location spinCenter = stand.getLocation();
                    for (int i = 0; i < 8; i++) {
                        double angle = spin + (Math.PI * 2 * i / 8);
                        Location pl = spinCenter.clone().add(Math.cos(angle) * 1.7, 1.05, Math.sin(angle) * 1.7);
                        world.spawnParticle(Particle.SWEEP_ATTACK, pl, 1, 0, 0, 0, 0);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x5B2A86), 1.2f));
                    }
                    if (spinTick % 3 == 0) world.playSound(spinCenter, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 1.6f);
                } else if (t == FADE_TICKS + SPIN_TICKS + 1) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-105), Math.toRadians(35), Math.toRadians(55)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-105), Math.toRadians(-35), Math.toRadians(-55)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-14), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-10), 0, 0));

                    Location burst = stand.getLocation();
                    world.playSound(burst, Sound.ENTITY_WITHER_HURT, 1.2f, 0.6f);
                    world.playSound(burst, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.4f, 1.0f);
                    world.spawnParticle(Particle.EXPLOSION, burst.clone().add(0, 1, 0), 4, 0.4, 0.4, 0.4, 0);
                    for (int i = 0; i < 36; i++) {
                        double angle = Math.PI * 2 * i / 36;
                        Location pl = burst.clone().add(Math.cos(angle) * HIT_RADIUS * 0.75, 1.0, Math.sin(angle) * HIT_RADIUS * 0.75);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0xFF2244), 1.6f));
                    }

                    for (Player p : boss.getValidPlayers(world)) {
                        if (p.getLocation().distanceSquared(burst) > HIT_RADIUS * HIT_RADIUS) continue;
                        MscEntityUtils.damageBy(attacker, p, damage);
                        Vector away = p.getLocation().toVector().subtract(burst.toVector()).setY(0);
                        if (away.lengthSquared() < 0.01) away = new Vector(0, 0, 1);
                        p.setVelocity(away.normalize().multiply(0.55).setY(0.28));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1));
                    }
                } else if (t > FADE_TICKS + SPIN_TICKS + 16) {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "shadowstep";
    }
}
