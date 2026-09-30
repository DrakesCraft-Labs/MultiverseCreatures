package com.Chagui68.entities.boss.attack.ground;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.BossPuppet;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.entities.boss.seal.SealPlane;
import com.Chagui68.entities.boss.attack.BossAttackBase;
import com.Chagui68.utils.MscEntityUtils;
import com.Chagui68.utils.MscLog;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * Rune Ward — the boss plants a floating rune that keeps pulsing after he steps away.
 *
 * <p>ANIMATION (what tells it apart on screen)
 * <ul>
 *   <li>0-18: he kneels with both hands raised inside a large pentagram drawn on the floor — the only
 *       attack with a kneeling cast.</li>
 *   <li>19: an amethyst rune is <b>planted</b> in front of him and stays there. Every 25 ticks it
 *       throbs and pushes a ring of runes outward, weakening whoever it touches.</li>
 *   <li>151: the rune shatters. Nothing else in the arsenal leaves something behind after the
 *       animation ends, which makes this one unmistakable.</li>
 * </ul>
 */
public class RuneWardAttack extends BossAttackBase {

    private static final int CAST_TICKS = 19;
    private static final int WARD_TICKS = 150;
    private static final int PULSE_EVERY = 25;
    private static final double PULSE_RADIUS = 5.0;
    private static final String WARD_TAG = "MSC_RuneWard";

    private final double damage;

    public RuneWardAttack(BossHost boss) {
        super(boss);
        this.damage = plugin.getConfig().getDouble("entities.armor-stand-boss.rune-ward-damage", 4.0);
    }

    @Override
    public void execute(BossInstance instance) {
        if (instance.isFlying) return;
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        Location origin = stand.getLocation().clone();
        LivingEntity attacker = stand.entidad();

        Vector facing = origin.getDirection().setY(0);
        if (facing.lengthSquared() < 0.01) facing = new Vector(0, 0, 1);
        final Vector direction = facing.normalize();

        if (plugin.getMagicSealListener() != null) {
            plugin.getMagicSealListener().spawnLargePentagramSeal(origin.clone().add(0, 0.25, 0), 160, 5.0,
                    SealPlane.XZ);
        }

        new BukkitRunnable() {
            int t = 0;
            ItemDisplay ward = null;
            Location wardBase = null;
            int pulses = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > WARD_TICKS + 20) {
                    removeWard(world);
                    cancel();
                    return;
                }

                Location center = stand.getLocation();

                if (t < CAST_TICKS) {
                    double p = (double) t / CAST_TICKS;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-150 * p), 0, Math.toRadians(-16 * p)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-150 * p), 0, Math.toRadians(16 * p)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(12 * p), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(18 * p), 0, 0));
                    stand.setRightLegPose(new EulerAngle(Math.toRadians(-22 * p), 0, 0));
                    stand.setLeftLegPose(new EulerAngle(Math.toRadians(-22 * p), 0, 0));

                    double radius = 1.0 + 4.5 * p;
                    for (int i = 0; i < 24; i++) {
                        double angle = Math.PI * 2 * i / 24 + t * 0.08;
                        Location pl = center.clone().add(Math.cos(angle) * radius, 0.3, Math.sin(angle) * radius);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x9B6BFF), 1.1f));
                    }
                    world.spawnParticle(Particle.ENCHANT, center.clone().add(0, 1.4, 0), 4, 0.6, 0.5, 0.6, 0.05);
                    if (t == 0) world.playSound(center, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.4f, 0.6f);
                } else if (t == CAST_TICKS) {
                    wardBase = stand.getLocation().clone().add(direction.clone().multiply(1.3)).add(0, 1.35, 0);
                    ward = plantWard(world, wardBase);
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-52), Math.toRadians(18), Math.toRadians(24)));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-52), Math.toRadians(-18), Math.toRadians(-24)));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(14), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(10), 0, 0));
                    stand.setRightLegPose(new EulerAngle(Math.toRadians(0), 0, 0));
                    stand.setLeftLegPose(new EulerAngle(Math.toRadians(0), 0, 0));

                    world.playSound(wardBase, Sound.BLOCK_AMETHYST_BLOCK_PLACE, 1.6f, 0.8f);
                    world.playSound(wardBase, Sound.BLOCK_BEACON_ACTIVATE, 1.4f, 1.4f);
                    world.spawnParticle(Particle.EXPLOSION, wardBase, 2, 0.2, 0.2, 0.2, 0);
                } else if (t <= WARD_TICKS) {
                    int since = t - CAST_TICKS;
                    if (ward != null && ward.isValid() && wardBase != null) {
                        double bob = Math.sin(since * 0.15) * 0.16;
                        Location loc = wardBase.clone().add(0, bob, 0);
                        loc.setYaw(since * 4.0f);
                        ward.teleport(loc);
                    }
                    if (since % PULSE_EVERY == 0 && wardBase != null) {
                        pulse(world, wardBase, attacker);
                        pulses++;
                    }
                    if (since % 5 == 0 && wardBase != null) {
                        world.spawnParticle(Particle.ENCHANT, wardBase.clone().add(0, 0.2, 0), 3, 0.3, 0.3, 0.3, 0.02);
                    }
                } else {
                    if (wardBase != null) {
                        world.spawnParticle(Particle.BLOCK, wardBase, 20, 0.3, 0.3, 0.3, 0.05,
                                Material.AMETHYST_BLOCK.createBlockData());
                        world.playSound(wardBase, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.4f, 0.9f);
                    }
                    removeWard(world);
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }

            private void removeWard(World world) {
                if (ward != null && ward.isValid()) {
                    world.spawnParticle(Particle.REVERSE_PORTAL, ward.getLocation(), 20, 0.3, 0.3, 0.3, 0.05);
                    ward.remove();
                }
                ward = null;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Plants the rune display; returns null when the server refuses the entity (it never throws). */
    private ItemDisplay plantWard(World world, Location loc) {
        ItemDisplay ward;
        try {
            ward = (ItemDisplay) world.spawnEntity(loc, EntityType.ITEM_DISPLAY);
        } catch (Throwable refused) {
            return null;
        }
        if (ward == null) return null;

        ward.setItemStack(new ItemStack(Material.AMETHYST_SHARD));
        ward.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
        ward.setBillboard(Display.Billboard.FIXED);
        try {
            org.bukkit.util.Transformation transformation = ward.getTransformation();
            transformation.getScale().set(2.4f);
            ward.setTransformation(transformation);
        } catch (Throwable e) {
            MscLog.debug("could not scale the rune ward", e);
        }
        ward.setViewRange(64.0f);
        ward.setGravity(false);
        ward.setInvulnerable(true);
        ward.setPersistent(false);
        ward.addScoreboardTag(WARD_TAG);
        return ward;
    }

    /** One ward throb: a ring of runes expands and weakens everyone it reaches. */
    private void pulse(World world, Location wardBase, LivingEntity attacker) {
        world.playSound(wardBase, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.4f, 1.2f);
        world.playSound(wardBase, Sound.BLOCK_BEACON_AMBIENT, 1.2f, 1.6f);

        for (int ring = 0; ring < 3; ring++) {
            final int r = ring;
            new BukkitRunnable() {
                @Override
                public void run() {
                    double radius = 1.2 + r * 1.9;
                    int points = (int) Math.max(12, radius * 8);
                    for (int i = 0; i < points; i++) {
                        double angle = Math.PI * 2 * i / points;
                        Location pl = wardBase.clone().add(Math.cos(angle) * radius, -0.9, Math.sin(angle) * radius);
                        world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(Color.fromRGB(0x9B6BFF), 1.3f));
                        world.spawnParticle(Particle.END_ROD, pl, 1, 0, 0, 0, 0);
                    }
                }
            }.runTaskLater(plugin, r * 3L);
        }

        for (Player p : boss.getValidPlayers(world)) {
            if (p.getLocation().distanceSquared(wardBase) > PULSE_RADIUS * PULSE_RADIUS) continue;
            MscEntityUtils.damageBy(attacker, p, damage);
            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 25, 0));
            world.spawnParticle(Particle.DAMAGE_INDICATOR, p.getLocation().add(0, 1, 0), 6, 0.3, 0.3, 0.3, 0.05);
        }
    }

    @Override
    public String getName() {
        return "runeward";
    }
}
