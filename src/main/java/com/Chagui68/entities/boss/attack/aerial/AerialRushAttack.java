package com.Chagui68.entities.boss.attack.aerial;

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

public class AerialRushAttack extends BossAttackBase {
    public AerialRushAttack(BossHost boss) {
        super(boss);
    }

    private static final double HOVER_HEIGHT = 18.0;
    private static final double RUSH_SPEED = 2.2;
    private static final double DASH_SPEED = 3.0;
    private static final double IMPACT_RADIUS = 8.0;
    private static final double IMPACT_RADIUS_SQ = IMPACT_RADIUS * IMPACT_RADIUS;
    private static final double SHOCKWAVE_RADIUS = 14.0;

    @Override
    public void execute(BossInstance instance) {
        if (!instance.isFlying) return;
        ArmorStand stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();

        new BukkitRunnable() {
            int t = 0;
            int phase = 0;
            int phaseTick = 0;
            Player target = null;
            Location dashStart = null;
            Location dashEnd = null;
            Location impactLoc = null;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    boss.resetBossPose(instance);
                    cancel();
                    return;
                }

                if (phase == 0) {
                    doHoverPhase(stand, world, center);
                } else if (phase == 1) {
                    doSelectPhase(stand, world, center);
                } else if (phase == 2) {
                    doRushPhase(stand, world);
                } else if (phase == 3) {
                    doImpactPhase(stand, world);
                } else if (phase == 4) {
                    doRisePhase(stand, world);
                } else if (phase == 5) {
                    doSlamPhase(stand, world);
                } else {
                    boss.resetBossPose(instance);
                    cancel();
                }
            }

            private void nextPhase(int newPhase) {
                phase = newPhase;
                phaseTick = 0;
            }

            private void doHoverPhase(ArmorStand stand, World world, Location center) {
                if (phaseTick == 0) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-160), Math.toRadians(20), Math.toRadians(10)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-160), Math.toRadians(-20), Math.toRadians(-10)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(10), 0, 0));
                    world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.9f);
                }
                double phaseF = (double) phaseTick / 25;
                double targetY = instance.groundY + HOVER_HEIGHT;
                double currentY = stand.getLocation().getY();
                if (currentY < targetY) {
                    double newY = Math.min(targetY, currentY + 0.6 + phaseF * 0.4);
                    Location loc = stand.getLocation();
                    loc.setY(newY);
                    stand.teleport(loc);
                }
                world.spawnParticle(Particle.END_ROD, stand.getLocation(), 3, 0.6, 0.6, 0.6, 0.02);
                world.spawnParticle(Particle.CLOUD, stand.getLocation(), 1, 0.8, 0.3, 0.8, 0.01);
                if (phaseTick % 8 == 0) world.playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.1f);
                phaseTick++;
                if (phaseTick >= 25) nextPhase(1);
            }

            private void doSelectPhase(ArmorStand stand, World world, Location center) {
                List<Player> targets = boss.getValidPlayersNear(center, 10000);
                if (targets.isEmpty()) {
                    boss.resetBossPose(instance);
                    cancel();
                    return;
                }
                target = targets.get(random.nextInt(targets.size()));
                stand.setHeadPose(new EulerAngle(Math.toRadians(25), 0, 0));
                stand.setBodyPose(new EulerAngle(Math.toRadians(20), 0, 0));
                stand.setRightArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(30), Math.toRadians(15)));
                stand.setLeftArmPose(new EulerAngle(Math.toRadians(-180), Math.toRadians(-30), Math.toRadians(-15)));
                world.playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.2f, 0.6f);
                for (int a = 0; a < 3; a++) {
                    double ang = random.nextDouble() * Math.PI * 2;
                    Location pl = stand.getLocation().add(Math.cos(ang) * 2, 0, Math.sin(ang) * 2);
                    world.spawnParticle(Particle.DUST, pl, 4, 0.3, 0.3, 0.3, 0,
                            new Particle.DustOptions(Color.fromRGB(0xFF5555), 2.0f));
                }
                phaseTick++;
                if (phaseTick >= 10) {
                    dashStart = stand.getLocation().clone();
                    nextPhase(2);
                }
            }

            private void doRushPhase(ArmorStand stand, World world) {
                if (target == null || target.isDead() || !target.isOnline()) {
                    nextPhase(5);
                    return;
                }
                Location targetLoc = target.getLocation();
                Vector dir = targetLoc.toVector().subtract(stand.getLocation().toVector());
                double dist = dir.length();
                if (dist > 2.5) {
                    dir.normalize();
                    Location newLoc = stand.getLocation().add(dir.multiply(RUSH_SPEED));
                    stand.teleport(newLoc);
                    world.spawnParticle(Particle.CLOUD, stand.getLocation(), 6, 0.5, 0.3, 0.5, 0.05);
                    world.spawnParticle(Particle.CRIT, stand.getLocation(), 5, 0.5, 0.5, 0.5, 0.05);
                    world.spawnParticle(Particle.SWEEP_ATTACK, stand.getLocation(), 1, 0.2, 0.2, 0.2, 0);
                    if (phaseTick % 3 == 0) world.playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.8f, 1.3f);
                    phaseTick++;
                } else {
                    doHitPlayer(world, target);
                    impactLoc = stand.getLocation().clone();
                    nextPhase(3);
                }
                if (phaseTick > 80) {
                    nextPhase(5);
                }
            }

            private void doHitPlayer(World world, Player p) {
                world.spawnParticle(Particle.EXPLOSION, p.getLocation().add(0, 1, 0), 8, 0.8, 0.8, 0.8, 0);
                world.spawnParticle(Particle.SWEEP_ATTACK, p.getLocation(), 3, 0.4, 0.4, 0.4, 0);
                world.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.5f, 0.6f);
                world.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.2f);
                double dmg = sealDamage * 0.8;
                p.damage(dmg);
                Vector knock = p.getLocation().toVector().subtract(stand.getLocation().toVector());
                if (knock.lengthSquared() > 0) knock.normalize();
                knock.multiply(1.2).setY(0.7);
                p.setVelocity(knock);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2));
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1));
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 0));
            }

            private void doImpactPhase(ArmorStand stand, World world) {
                if (phaseTick == 0) {
                    world.spawnParticle(Particle.EXPLOSION, impactLoc, 12, 2, 1, 2, 0);
                    world.playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
                }
                world.spawnParticle(Particle.CLOUD, impactLoc, 4, 1.5, 0.5, 1.5, 0.05);
                phaseTick++;
                if (phaseTick >= 10) nextPhase(4);
            }

            private void doRisePhase(ArmorStand stand, World world) {
                if (phaseTick == 0) {
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-30), 0, 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-15), 0, 0));
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-200), Math.toRadians(20), Math.toRadians(15)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-200), Math.toRadians(-20), Math.toRadians(-15)));
                    world.playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.7f);
                }
                double newY = stand.getLocation().getY() + 1.2;
                double cap = instance.groundY + 12.0;
                if (newY > cap) newY = cap;
                Location loc = stand.getLocation();
                loc.setY(newY);
                stand.teleport(loc);
                world.spawnParticle(Particle.END_ROD, stand.getLocation(), 4, 0.6, 0.3, 0.6, 0.04);
                world.spawnParticle(Particle.CLOUD, stand.getLocation(), 2, 1.0, 0.2, 1.0, 0.03);
                if (phaseTick % 2 == 0) world.playSound(stand.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.2f, 1.5f);
                if (stand.getLocation().getY() >= cap - 0.1) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-230), Math.toRadians(40), Math.toRadians(30)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-230), Math.toRadians(-40), Math.toRadians(-30)));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-45), 0, 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-25), 0, 0));
                    world.playSound(stand.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.5f);
                    nextPhase(5);
                } else {
                    phaseTick++;
                }
                if (phaseTick > 30) nextPhase(5);
            }

            private void doSlamPhase(ArmorStand stand, World world) {
                if (phaseTick == 0) {
                    world.playSound(stand.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.6f);
                }
                double newY = stand.getLocation().getY() - DASH_SPEED;
                double targetY = boss.getGroundY(stand.getLocation(), 80);
                if (newY <= targetY) {
                    Location loc = stand.getLocation();
                    loc.setY(targetY);
                    stand.teleport(loc);
                    doFinalSlam(world, loc);
                    nextPhase(6);
                } else {
                    Location loc = stand.getLocation();
                    loc.setY(newY);
                    stand.teleport(loc);
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-260), Math.toRadians(50), Math.toRadians(35)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-260), Math.toRadians(-50), Math.toRadians(-35)));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-60), 0, 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-40), 0, 0));
                    world.spawnParticle(Particle.CLOUD, loc, 3, 0.8, 0.4, 0.8, 0.05);
                    world.spawnParticle(Particle.CRIT, loc, 2, 0.5, 0.5, 0.5, 0.06);
                    phaseTick++;
                }
                if (phaseTick > 30) {
                    boss.resetBossPose(instance);
                    cancel();
                }
            }

            private void doFinalSlam(World world, Location loc) {
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
                world.playSound(loc, Sound.ENTITY_WITHER_DEATH, 1.5f, 0.7f);
                world.spawnParticle(Particle.EXPLOSION, loc, 25, 3, 1.5, 3, 0);
                world.spawnParticle(Particle.FLASH, loc, 1,
                        Color.WHITE);

                double dmg = sealDamage * 1.4;
                for (Player p : boss.getValidPlayers(world)) {
                    double dsq = p.getLocation().distanceSquared(loc);
                    if (dsq <= IMPACT_RADIUS_SQ) {
                        double factor = 1.0 - Math.sqrt(dsq) / IMPACT_RADIUS * 0.4;
                        p.damage(dmg * factor);
                        Vector away = p.getLocation().toVector().subtract(loc.toVector());
                        away.setY(0);
                        if (away.lengthSquared() > 0) away.normalize();
                        away.multiply(1.2 * factor).setY(0.8);
                        p.setVelocity(away);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 2));
                    }
                }

                boss.spawnShockwaveWave(world, loc, SHOCKWAVE_RADIUS);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "aerialrush";
    }
}
