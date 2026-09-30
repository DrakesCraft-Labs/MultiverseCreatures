package com.Chagui68.entities.boss.attack.ranged;

import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.BossHost;
import com.Chagui68.entities.boss.BossPuppet;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Plague Brand — the boss brands a player and the plague jumps between bodies.
 *
 * <p>ANIMATION (what tells it apart on screen)
 * <ul>
 *   <li>0-19: he spreads both arms sideways with the palms open and a sickly green circle closes on
 *       the chosen victim, so they know they have been picked.</li>
 *   <li>20: the mark lands — green spores burst over the victim, who keeps a visible halo of spores
 *       and is drained every second.</li>
 *   <li>While marked, anyone standing next to the victim catches the brand too. The boss also keeps
 *       his <b>head tracking the marked player</b> after the hit, which no other attack does.</li>
 * </ul>
 */
public class PlagueBrandAttack extends BossAttackBase {

    private static final int CHARGE_TICKS = 20;
    private static final int BRAND_TICKS = 120;
    private static final int SPREAD_TICKS = 60;
    private static final int PULSE_EVERY = 20;
    private static final double SPREAD_RADIUS = 4.0;

    private final double hitDamage;
    private final double pulseDamage;

    public PlagueBrandAttack(BossHost boss) {
        super(boss);
        this.hitDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.plague-brand-hit-damage", 9.0);
        this.pulseDamage = plugin.getConfig().getDouble("entities.armor-stand-boss.plague-brand-damage", 4.0);
    }

    @Override
    public void execute(BossInstance instance) {
        BossPuppet stand = instance.stand;
        World world = stand.getWorld();
        LivingEntity attacker = stand.entidad();
        Player first = boss.detectTarget(stand);
        if (first == null) return;
        final UUID initial = first.getUniqueId();

        new BukkitRunnable() {
            int t = 0;
            boolean landed = false;
            final Map<UUID, Integer> branded = new HashMap<>();

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid() || t > CHARGE_TICKS + BRAND_TICKS + 20) {
                    cancel();
                    return;
                }

                Location center = stand.getLocation();
                dropGone(branded);

                if (t < CHARGE_TICKS) {
                    double p = (double) t / CHARGE_TICKS;
                    stand.setRightArmPose(new EulerAngle(Math.toRadians(-20), Math.toRadians(88 * p), 0));
                    stand.setLeftArmPose(new EulerAngle(Math.toRadians(-20), Math.toRadians(-88 * p), 0));
                    stand.setBodyPose(new EulerAngle(Math.toRadians(10 * p), 0, 0));
                    stand.setHeadPose(new EulerAngle(Math.toRadians(14 * p), 0, 0));

                    Player victim = plugin.getServer().getPlayer(initial);
                    if (victim != null) {
                        Location ground = victim.getLocation();
                        double radius = 4.2 - 3.0 * p;
                        for (int i = 0; i < 26; i++) {
                            double angle = Math.PI * 2 * i / 26 + t * 0.06;
                            Location pl = ground.clone().add(Math.cos(angle) * radius, 0.2, Math.sin(angle) * radius);
                            world.spawnParticle(Particle.DUST, pl, 1, 0, 0, 0, 0,
                                    new Particle.DustOptions(Color.fromRGB(0x5FBF3F), 1.3f));
                            if (i % 3 == 0) world.spawnParticle(Particle.WITCH, pl, 1, 0.05, 0.1, 0.05, 0.01);
                        }
                    }
                    if (t == 0) world.playSound(center, Sound.ENTITY_WITCH_THROW, 1.2f, 0.7f);
                    if (t == CHARGE_TICKS - 1) world.playSound(center, Sound.BLOCK_SCULK_SPREAD, 1.2f, 0.8f);
                } else if (!landed) {
                    landed = true;
                    Player victim = plugin.getServer().getPlayer(initial);
                    if (victim != null) {
                        world.playSound(victim.getLocation(), Sound.ENTITY_WITCH_THROW, 1.6f, 0.6f);
                        world.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.2f, 0.6f);
                        world.spawnParticle(Particle.WITCH, victim.getLocation().add(0, 1, 0), 40, 0.5, 0.7, 0.5, 0.03);
                        world.spawnParticle(Particle.SCULK_CHARGE_POP, victim.getLocation().add(0, 1, 0),
                                20, 0.4, 0.5, 0.4, 0.02);
                        MscEntityUtils.damageBy(attacker, victim, hitDamage);
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 0));
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 120, 1));
                        branded.put(victim.getUniqueId(), BRAND_TICKS);
                    }
                } else if (!branded.isEmpty()) {
                    tickBrands(stand, world, center, branded, attacker);
                } else if (t > CHARGE_TICKS + 30) {
                    boss.resetBossPose(instance);
                    cancel();
                }
                t++;
            }

            /** Drains, trails and spreads the mark. */
            private void tickBrands(BossPuppet stand, World world, Location center, Map<UUID, Integer> branded, LivingEntity attacker) {
                Iterator<Map.Entry<UUID, Integer>> it = branded.entrySet().iterator();
                boolean spreadNow = (t - CHARGE_TICKS) % PULSE_EVERY == 0;
                List<Player> infected = new ArrayList<>();

                while (it.hasNext()) {
                    Map.Entry<UUID, Integer> entry = it.next();
                    Player victim = plugin.getServer().getPlayer(entry.getKey());
                    if (victim == null || !victim.isOnline() || victim.isDead()) {
                        it.remove();
                        continue;
                    }

                    Location loc = victim.getLocation();
                    for (int i = 0; i < 5; i++) {
                        double angle = Math.PI * 2 * i / 5 + t * 0.12;
                        Location pl = loc.clone().add(Math.cos(angle) * 0.7, 0.15 + (t % 20) * 0.06, Math.sin(angle) * 0.7);
                        world.spawnParticle(Particle.SPORE_BLOSSOM_AIR, pl, 1, 0.05, 0.05, 0.05, 0);
                    }
                    world.spawnParticle(Particle.WITCH, loc.clone().add(0, 1.2, 0), 2, 0.25, 0.3, 0.25, 0.01);
                    if (t % 10 == 0) world.spawnParticle(Particle.SCULK_CHARGE_POP, loc.clone().add(0, 1.6, 0), 4, 0.2, 0.15, 0.2, 0);

                    int remaining = entry.getValue() - 1;
                    if (remaining <= 0) {
                        it.remove();
                        world.spawnParticle(Particle.FLASH, loc.clone().add(0, 1, 0), 1, Color.fromRGB(0x66CC33));
                        continue;
                    }
                    entry.setValue(remaining);

                    if (remaining % PULSE_EVERY == 0) {
                        MscEntityUtils.damageBy(attacker, victim, pulseDamage);
                        world.spawnParticle(Particle.DAMAGE_INDICATOR, loc.clone().add(0, 1, 0), 6, 0.3, 0.3, 0.3, 0.05);
                        world.playSound(loc, Sound.ENTITY_WITCH_HURT, 0.7f, 0.6f);
                    }

                    if (spreadNow && remaining > SPREAD_TICKS / 2) {
                        Player neighbour = nearestUnbranded(branded, loc);
                        if (neighbour != null) infected.add(neighbour);
                    }
                }

                // Applied after the loop: the map must not change while it is being iterated.
                for (Player neighbour : infected) {
                    branded.put(neighbour.getUniqueId(), SPREAD_TICKS);
                    world.spawnParticle(Particle.WITCH, neighbour.getLocation().add(0, 1, 0), 16, 0.3, 0.4, 0.3, 0.02);
                    world.playSound(neighbour.getLocation(), Sound.ENTITY_WITCH_THROW, 1.0f, 1.3f);
                }

                // His head follows the nearest marked player for the whole plague.
                aimHeadAt(stand, center, nearestMarked(branded, center));
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void dropGone(Map<UUID, Integer> branded) {
        branded.entrySet().removeIf(e -> {
            Player p = plugin.getServer().getPlayer(e.getKey());
            return p == null || !p.isOnline();
        });
    }

    private Player nearestUnbranded(Map<UUID, Integer> branded, Location from) {
        Player best = null;
        double bestDistance = SPREAD_RADIUS * SPREAD_RADIUS;
        for (Player p : boss.getValidPlayers(from.getWorld())) {
            if (branded.containsKey(p.getUniqueId())) continue;
            double d = p.getLocation().distanceSquared(from);
            if (d < bestDistance) {
                bestDistance = d;
                best = p;
            }
        }
        return best;
    }

    private Player nearestMarked(Map<UUID, Integer> branded, Location center) {
        Player best = null;
        double bestDistance = Double.MAX_VALUE;
        for (UUID id : branded.keySet()) {
            Player p = plugin.getServer().getPlayer(id);
            if (p == null || !p.isOnline()) continue;
            double d = p.getLocation().distanceSquared(center);
            if (d < bestDistance) {
                bestDistance = d;
                best = p;
            }
        }
        return best;
    }

    /** Points the boss's head at a player; the only attack that keeps tracking after the impact. */
    private void aimHeadAt(BossPuppet stand, Location center, Player target) {
        if (target == null) return;
        Vector to = target.getEyeLocation().toVector().subtract(center.clone().add(0, 1.5, 0).toVector());
        if (to.lengthSquared() < 0.01) return;
        double yaw = Math.toDegrees(Math.atan2(-to.getX(), to.getZ()));
        double pitch = Math.toDegrees(-Math.asin(to.clone().normalize().getY()));
        double relative = ((yaw - center.getYaw()) % 360.0 + 540.0) % 360.0 - 180.0;
        stand.setHeadPose(new EulerAngle(Math.toRadians(pitch), Math.toRadians(relative), 0));
    }

    @Override
    public String getName() {
        return "plaguebrand";
    }
}
