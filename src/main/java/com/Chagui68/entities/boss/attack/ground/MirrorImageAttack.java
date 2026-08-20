package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.utils.MscEntityUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.List;

public class MirrorImageAttack extends BossAttackBase {
    private final double mirrorDamage;

    public MirrorImageAttack(BossHost boss) {
        super(boss);
        mirrorDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.mirror-image-damage", 6.0);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation();
        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnCelestialSeal(center.clone().add(0, 0.5, 0), 80);
        }

        new BukkitRunnable() {
            int t = 0;
            List<ArmorStand> mirrors = new ArrayList<>();

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    for (ArmorStand m : mirrors) if (m.isValid()) m.remove();
                    cancel();
                    return;
                }
                if (t < 30) {
                    double phase = (double) t / 30;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(45 * phase), Math.toRadians(20 * phase)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90 * phase), Math.toRadians(-45 * phase), Math.toRadians(-20 * phase)));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(-15 * phase), 0, 0));
                    for (int a = 0; a < 6; a++) {
                        double angle = (2 * Math.PI * a / 6) + t * 0.05;
                        double r = 4.0 * phase;
                        double x = center.getX() + Math.cos(angle) * r;
                        double z = center.getZ() + Math.sin(angle) * r;
                        world.spawnParticle(Particle.END_ROD, new Location(world, x, center.getY() + 1, z), 2, 0.2, 0.5, 0.2, 0.01);
                        world.spawnParticle(Particle.DUST, new Location(world, x, center.getY() + 1, z), 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.WHITE, 2.0f * (float) phase));
                    }
                    if (t == 1) world.playSound(center, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.2f);
                } else if (t == 30) {
                    for (int a = 0; a < 4; a++) {
                        double angle = (2 * Math.PI * a / 4);
                        double r = 5.0;
                        Location mLoc = center.clone().add(Math.cos(angle) * r, 0, Math.sin(angle) * r);
                        mLoc.setDirection(center.toVector().subtract(mLoc.toVector()));
                        ArmorStand mirror = (ArmorStand) world.spawnEntity(mLoc, EntityType.ARMOR_STAND);
                        if (mirror != null) {
                            mirror.setVisible(true);
                            mirror.setSmall(false);
                            mirror.setArms(true);
                            mirror.setBasePlate(false);
                            mirror.setGravity(false);
                            mirror.setInvulnerable(true);
                            mirror.setAI(false);
                            mirror.setCollidable(false);
                            mirror.setMarker(false);
                            mirror.setCustomNameVisible(false);
                            mirror.addScoreboardTag("MSC_BossMirror");
                            ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
                            ItemMeta sm = sword.getItemMeta();
                            if (sm != null) {
                                sm.setUnbreakable(true);
                                sm.setItemName("Mirror Blade");
                                sword.setItemMeta(sm);
                            }
                            EntityEquipment eq = mirror.getEquipment();
                            if (eq != null) eq.setItemInMainHand(sword);
                            mirrors.add(mirror);
                            world.playSound(mLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.3f);
                            world.spawnParticle(Particle.EXPLOSION, mLoc, 5, 0.5, 1, 0.5, 0);
                        }
                    }
                } else if (t < 80) {
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(45), Math.toRadians(20)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90), Math.toRadians(-45), Math.toRadians(-20)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(-5), 0, 0));
                    for (ArmorStand m : mirrors) {
                        if (!m.isValid()) continue;
                        m.setRightArmPose(stand.getRightArmPose());
                        m.setLeftArmPose(stand.getLeftArmPose());
                        m.setBodyPose(stand.getBodyPose());
                        m.setHeadPose(stand.getHeadPose());
                        Location mTarget = m.getLocation().add(m.getLocation().getDirection().multiply(0.5));
                        m.teleport(mTarget);
                        world.spawnParticle(Particle.END_ROD, m.getLocation(), 1, 0.3, 0.5, 0.3, 0);
                        Player near = boss.findNearestPlayer(m.getLocation(), 5);
                        if (near != null) {
                            MscEntityUtils.damageBy(stand.entidad(), near, mirrorDamage);
                            near.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                            world.playSound(m.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.8f, 1.2f);
                            world.spawnParticle(Particle.CRIT, near.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.05);
                        }
                    }
                } else {
                    for (ArmorStand m : mirrors) if (m.isValid()) m.remove();
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public String getName() {
        return "mirrorimage";
    }
}
