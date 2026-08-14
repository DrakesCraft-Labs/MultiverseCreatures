package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.HeadSlimeHeart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HeadSlime implements Listener {

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, HeadSlimeInstance> activeSlimes = new ConcurrentHashMap<>();

    private double speed;
    private int size;
    private double leapRange;
    private double leapSpeed;
    private int blindIntervalTicks;
    private int damageIntervalTicks;
    private double damagePerInterval;
    private int maxAttachTicks;
    private int buffIntervalTicks;
    private int maxAttachTicksMob;
    private boolean targetEntities;
    private int skeletonBurstCooldown;
    private boolean debug;
    public static final Set<UUID> immunePlayers = ConcurrentHashMap.newKeySet();
    private static final String TAG = "MSC_HeadSlime";
    private static final Set<String> MSC_ENTITY_TAGS = Set.of(
            "MSC_SoulReaper", "MSC_BoneShield", "MSC_ObsidianGuard",
            "MSC_ShadowRogue", "MSC_FlameElemental", "MSC_FrostGolem",
            "MSC_VoidCrawler", "MSC_StormCaller", "MSC_ChaosMage",
            "MSC_EnderKnight", "MSC_VenomWitch"
    );
    private static final Set<String> MSC_BLACKLIST = Set.of(
            TAG, "MSC_DioBoss", "MSC_Mahoraga", "MSC_ArmorBossSummoned"
    );

    private String getMscEntityTag(Set<String> tags) {
        for (String t : tags) {
            if (t.startsWith("MSC_") && !MSC_BLACKLIST.contains(t)) {
                return t;
            }
        }
        return null;
    }

    public HeadSlime(MultiverseCreatures plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        reloadConfig();
        startTicker();
        startParticleTask();
        reloadExisting();
    }

    public void reloadConfig() {
        speed = plugin.getConfig().getDouble("head-slime.speed", 0.5);
        size = plugin.getConfig().getInt("head-slime.size", 1);
        leapRange = plugin.getConfig().getDouble("head-slime.leap-range", 5.0);
        leapSpeed = plugin.getConfig().getDouble("head-slime.leap-speed", 1.2);
        blindIntervalTicks = plugin.getConfig().getInt("head-slime.blind-interval-ticks", 20);
        damageIntervalTicks = plugin.getConfig().getInt("head-slime.damage-interval-ticks", 40);
        damagePerInterval = plugin.getConfig().getDouble("head-slime.damage-per-interval", 3.0);
        maxAttachTicks = plugin.getConfig().getInt("head-slime.max-attach-ticks", 200);
        buffIntervalTicks = plugin.getConfig().getInt("head-slime.buff-interval-ticks", 40);
        maxAttachTicksMob = plugin.getConfig().getInt("head-slime.max-attach-ticks-mob", 600);
        targetEntities = plugin.getConfig().getBoolean("head-slime.target-entities", true);
        skeletonBurstCooldown = plugin.getConfig().getInt("head-slime.skeleton-burst-cooldown", 60);
        debug = plugin.getConfig().getBoolean("head-slime.debug", false);
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (Slime slime : world.getEntitiesByClass(Slime.class)) {
                if (slime.getScoreboardTags().contains(TAG)) {
                    setupSlime(slime);
                }
            }
        }
    }

    private final Set<UUID> removeQueue = new HashSet<>();

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (HeadSlimeInstance inst : activeSlimes.values()) {
                    if (removeQueue.contains(inst.slimeId)) continue;
                    try {
                        tickSlime(inst);
                    } catch (Exception e) {
                        plugin.getLogger().warning("[HeadSlime] Error ticking slime " + inst.slimeId + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    }
                }
                for (UUID id : removeQueue) {
                    activeSlimes.remove(id);
                }
                removeQueue.clear();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void startParticleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID id : immunePlayers) {
                    Player p = Bukkit.getPlayer(id);
                    if (p == null || !p.isOnline()) continue;
                    Location loc = p.getLocation().add(0, 1, 0);
                    for (double angle = 0; angle < 360; angle += 30) {
                        double rad = Math.toRadians(angle);
                        double x = Math.cos(rad) * 1.2;
                        double z = Math.sin(rad) * 1.2;
                        p.getWorld().spawnParticle(Particle.WITCH, loc.clone().add(x, 0, z), 0, 0, 0, 0, 0);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void markForRemoval(HeadSlimeInstance inst) {
        removeQueue.add(inst.slimeId);
        if (inst.slime.isValid()) inst.slime.remove();
    }

    public boolean trySpawn(Location location) {
        Slime slime = (Slime) location.getWorld().spawnEntity(location, EntityType.SLIME);
        if (slime == null) return false;

        slime.addScoreboardTag(TAG);
        setupSlime(slime);
        slime.setCustomName(ChatColor.GREEN + "" + ChatColor.BOLD + "Head Slime");
        slime.setCustomNameVisible(true);
        slime.setRemoveWhenFarAway(false);
        slime.setPersistent(true);
        slime.setCollidable(true);
        slime.setCanPickupItems(false);

        return true;
    }

    private void setupSlime(Slime slime) {
        slime.setSize(size);
        if (slime.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            slime.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
        }
        slime.setAI(true);
        slime.setTarget(null);

        activeSlimes.put(slime.getUniqueId(), new HeadSlimeInstance(slime));
    }

    private void tickSlime(HeadSlimeInstance inst) {
        Slime slime = inst.slime;
        if (slime.isDead() || !slime.isValid()) {
            markForRemoval(inst);
            return;
        }

        if (!slime.getWorld().isChunkLoaded(slime.getLocation().getChunk())) return;

        if (inst.attached) {
            Entity vehicle = slime.getVehicle();
            if (vehicle != null && !vehicle.isDead()) {
                tickAttached(inst);
                return;
            }
            detach(inst, vehicle != null ? vehicle.getLocation() : slime.getLocation());
        }
        tickFree(inst);
    }

    private void tickFree(HeadSlimeInstance inst) {
        Slime slime = inst.slime;
        Entity target = null;

        if (inst.targetId != null) {
            target = Bukkit.getEntity(inst.targetId);
        }

        if (target == null || target.isDead() || !target.isValid()) {
            inst.targetId = findNearestTarget(slime);
            if (inst.targetId == null) return;
            target = Bukkit.getEntity(inst.targetId);
            if (target == null) return;
            if (debug) plugin.getLogger().info("[HeadSlime] Tracking target: " + target.getName());
        }

        // The player consumed gelatin mid-chase: drop the target and pick another.
        if (target instanceof Player p && immunePlayers.contains(p.getUniqueId())) {
            inst.targetId = findNearestTarget(slime);
            return;
        }

        double distSq = slime.getLocation().distanceSquared(target.getLocation());

        if (distSq > leapRange * leapRange * 4) {
            inst.targetId = findNearestTarget(slime);
            return;
        }

        Location targetLoc = target instanceof Player p ? p.getEyeLocation() : target.getLocation().add(0, 1, 0);

        if (distSq > 3.24) {
            Vector dir = targetLoc.toVector().subtract(slime.getLocation().toVector());
            double dist = dir.length();
            dir.normalize();
            slime.setVelocity(dir.multiply(Math.min(leapSpeed, dist * 0.3)));
        } else if (canAttach(slime, target)) {
            target.addPassenger(slime);
            inst.attached = true;
            inst.damageTicks = 0;
            inst.targetId = target.getUniqueId();
            if (debug) plugin.getLogger().info("[HeadSlime] Attached to " + target.getName());
        }
    }

    private boolean canAttach(Slime slime, Entity target) {
        return slime.getVehicle() == null
                && target.getPassengers().isEmpty()
                && !target.isDead()
                && target.isValid()
                && (!(target instanceof Player p) || !immunePlayers.contains(p.getUniqueId()));
    }

    private void tickAttached(HeadSlimeInstance inst) {
        Slime slime = inst.slime;
        Entity vehicle = slime.getVehicle();
        if (vehicle == null || vehicle.isDead()) {
            detach(inst, vehicle != null ? vehicle.getLocation() : slime.getLocation());
            return;
        }

        inst.damageTicks++;

        switch (vehicle) {
            case Player player -> tickAttachedPlayer(inst, player);
            case Mob mob -> tickAttachedMob(inst, mob);
            case null -> {
            }
            default -> detach(inst, vehicle);
        }
    }

    private void tickAttachedPlayer(HeadSlimeInstance inst, Player player) {
        Slime slime = inst.slime;

        if (immunePlayers.contains(player.getUniqueId())) {
            detach(inst, player);
            return;
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindIntervalTicks + 10, 1, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, blindIntervalTicks + 10, 0, false, true));

        if (inst.damageTicks % damageIntervalTicks == 0) {
            player.damage(damagePerInterval, DamageSource.builder(DamageType.MOB_ATTACK).withDirectEntity(slime).withCausingEntity(slime).build());
        }

        if (inst.damageTicks >= maxAttachTicks) {
            detach(inst, player);
        }
    }

    private void tickAttachedMob(HeadSlimeInstance inst, Mob mob) {
        if (mob.getScoreboardTags().contains("MSC_DioBoss")) {
            detach(inst, mob);
            return;
        }

        if (inst.damageTicks % buffIntervalTicks == 0) {
            mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, buffIntervalTicks + 20, 1, false, true));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, buffIntervalTicks + 20, 1, false, true));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, buffIntervalTicks + 20, 0, false, true));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, buffIntervalTicks + 20, 0, false, true));
        }

        if (inst.damageTicks % 20 == 0 && mob instanceof Creature creature) {
            Player nearest = findNearestPlayer(mob.getLocation());
            if (nearest != null) {
                creature.setTarget(nearest);
            }
        }

        if (mob instanceof Creeper creeper) {
            if (inst.originalRadius == -1) {
                inst.originalRadius = creeper.getExplosionRadius();
            }
            creeper.setExplosionRadius(inst.originalRadius * 2);
            creeper.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 1, false, true));
        }

        if (mob instanceof Skeleton skeleton && skeleton.getTarget() instanceof Player target) {
            if (inst.damageTicks - inst.lastSkeletonBurst >= inst.skeletonBurstCooldown) {
                for (int i = 0; i < 3; i++) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Arrow arrow = skeleton.launchProjectile(Arrow.class);
                        if (arrow != null) {
                            arrow.setDamage(4.0);
                            arrow.setCritical(true);
                        }
                    }, i * 4L);
                }
                inst.lastSkeletonBurst = inst.damageTicks;
            }
        }

        applyMscSpecialBuff(inst, mob);

        if (inst.damageTicks >= maxAttachTicksMob) {
            detach(inst, mob);
        }
    }

    private void applyMscSpecialBuff(HeadSlimeInstance inst, Mob mob) {
        String tag = getMscEntityTag(mob.getScoreboardTags());
        if (tag == null) return;

        Location loc = mob.getLocation().add(0, 1.5, 0);
        World world = mob.getWorld();

        switch (tag) {
            case "MSC_SoulReaper" -> {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 2, false, true));
                if (inst.damageTicks % 30 == 0) {
                    world.spawnParticle(Particle.SOUL, loc, 8, 0.5, 0.5, 0.5, 0.02);
                    world.spawnParticle(Particle.DUST, loc, 4, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0xAA00AA), 1.5f));
                }
            }
            case "MSC_BoneShield" -> {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 1, false, true));
                if (inst.damageTicks % 30 == 0) {
                    world.spawnParticle(Particle.END_ROD, loc, 6, 0.5, 0.5, 0.5, 0.02);
                    world.spawnParticle(Particle.DUST, loc, 4, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.WHITE, 1.5f));
                }
            }
            case "MSC_ObsidianGuard" -> {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 60, 1, false, true));
                if (inst.damageTicks % 30 == 0) {
                    world.spawnParticle(Particle.CRIT, loc, 8, 0.5, 0.5, 0.5, 0.05);
                    world.spawnParticle(Particle.DUST, loc, 4, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0x444444), 1.5f));
                }
            }
            case "MSC_ShadowRogue" -> {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 2, false, true));
                mob.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 0, false, true));
                if (inst.damageTicks % 30 == 0) {
                    world.spawnParticle(Particle.WITCH, loc, 4, 0.3, 0.3, 0.3, 0);
                    world.spawnParticle(Particle.DUST, loc, 4, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0x222222), 1.5f));
                }
            }
            case "MSC_FlameElemental" -> {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0, false, true));
                if (inst.damageTicks % 20 == 0) {
                    world.spawnParticle(Particle.FLAME, loc, 10, 0.8, 0.8, 0.8, 0.03);
                    world.spawnParticle(Particle.DUST, loc, 4, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0xFF6600), 1.5f));
                }
            }
            case "MSC_FrostGolem" -> {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 0, false, true));
                if (inst.damageTicks % 30 == 0) {
                    world.spawnParticle(Particle.SNOWFLAKE, loc, 6, 0.5, 0.5, 0.5, 0.01);
                    world.spawnParticle(Particle.DUST, loc, 4, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0x88CCFF), 1.5f));
                }
            }
            case "MSC_VoidCrawler" -> {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, true));
                if (inst.damageTicks % 30 == 0) {
                    world.spawnParticle(Particle.PORTAL, loc, 8, 0.5, 0.5, 0.5, 0.03);
                    world.spawnParticle(Particle.DUST, loc, 4, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0x440066), 1.5f));
                }
            }
            case "MSC_StormCaller" -> {
                if (inst.damageTicks % 30 == 0) {
                    world.spawnParticle(Particle.ELECTRIC_SPARK, loc, 10, 0.8, 0.8, 0.8, 0.05);
                    world.spawnParticle(Particle.DUST, loc, 4, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0xFFFF00), 1.5f));
                }
            }
            case "MSC_ChaosMage" -> {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, true));
                if (inst.damageTicks % 30 == 0) {
                    world.spawnParticle(Particle.WITCH, loc, 6, 0.5, 0.5, 0.5, 0);
                    world.spawnParticle(Particle.DUST, loc, 4, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0x00FF88), 1.5f));
                }
            }
            case "MSC_EnderKnight" -> {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, false, true));
                if (inst.damageTicks % 30 == 0) {
                    world.spawnParticle(Particle.PORTAL, loc, 6, 0.5, 0.5, 0.5, 0.02);
                    world.spawnParticle(Particle.DUST, loc, 4, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0xAA00FF), 1.5f));
                }
            }
            case "MSC_VenomWitch" -> {
                mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 1, false, true));
                if (inst.damageTicks % 30 == 0) {
                    world.spawnParticle(Particle.WITCH, loc, 6, 0.5, 0.5, 0.5, 0);
                    world.spawnParticle(Particle.DUST, loc, 4, 0, 0, 0, 0,
                            new Particle.DustOptions(Color.fromRGB(0x44FF44), 1.5f));
                }
            }
        }
    }

    private void detach(HeadSlimeInstance inst, Entity entity) {
        if (entity instanceof Creeper creeper && inst.originalRadius > 0) {
            creeper.setExplosionRadius(inst.originalRadius);
        }
        detach(inst, entity.getLocation());
        entity.removePassenger(inst.slime);
    }

    private void detach(HeadSlimeInstance inst, Location loc) {
        Slime slime = inst.slime;
        if (slime.getVehicle() != null) {
            slime.getVehicle().removePassenger(slime);
        }
        inst.attached = false;
        inst.targetId = null;

        Location eject = loc.clone().add(
                random.nextDouble() - 0.5, 1.0, random.nextDouble() - 0.5
        );
        slime.teleport(eject);
        slime.setVelocity(new Vector(0, 0.5, 0));
    }

    private UUID findNearestTarget(Slime slime) {
        double rangeSq = leapRange * leapRange;
        Entity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Entity entity : slime.getNearbyEntities(leapRange, leapRange, leapRange)) {
            if (entity.equals(slime)) continue;
            if (entity.isDead() || !entity.isValid()) continue;
            if (hasHeadSlimeAttached(entity)) continue;

            boolean isTarget = entity instanceof Player p
                    && p.getGameMode() != GameMode.CREATIVE
                    && p.getGameMode() != GameMode.SPECTATOR
                    && !p.isDead()
                    && p.isOnline()
                    && !immunePlayers.contains(p.getUniqueId());

            if (!isTarget && targetEntities && entity instanceof Monster) {
                Set<String> tags = entity.getScoreboardTags();
                isTarget = !tags.contains(TAG) && !tags.contains("MSC_DioBoss")
                        && !tags.contains("MSC_Mahoraga") && getMscEntityTag(tags) != null;
            }

            if (!isTarget) continue;

            double dist = slime.getLocation().distanceSquared(entity.getLocation());
            if (dist < nearestDist && dist <= rangeSq) {
                nearestDist = dist;
                nearest = entity;
            }
        }

        return nearest != null ? nearest.getUniqueId() : null;
    }

    private boolean hasHeadSlimeAttached(Entity entity) {
        for (Entity p : entity.getPassengers()) {
            if (p instanceof Slime s && s.getScoreboardTags().contains(TAG)) return true;
        }
        return false;
    }

    private Player findNearestPlayer(Location loc) {
        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.isDead() || !p.isOnline()) continue;
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
            double dist = loc.distanceSquared(p.getLocation());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = p;
            }
        }
        return nearest;
    }


    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.isDead()) return;

        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof Slime slime && slime.getScoreboardTags().contains(TAG)) {
                HeadSlimeInstance inst = activeSlimes.get(slime.getUniqueId());
                if (inst != null && inst.attached) {
                    detach(inst, player.getLocation());
                }
            }
        }
    }

    @EventHandler
    public void onSlimeAttacked(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Slime slime)) return;
        if (!slime.getScoreboardTags().contains(TAG)) return;

        HeadSlimeInstance inst = activeSlimes.get(slime.getUniqueId());
        if (inst != null && inst.attached) {
            Entity vehicle = slime.getVehicle();
            detach(inst, vehicle != null ? vehicle.getLocation() : slime.getLocation());
        }
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper)) return;
        boolean hasSlime = false;
        for (Entity p : creeper.getPassengers()) {
            if (p instanceof Slime s && s.getScoreboardTags().contains(TAG)) {
                hasSlime = true;
                break;
            }
        }
        if (!hasSlime) return;
        for (Player player : event.getLocation().getWorld().getPlayers()) {
            double dist = player.getLocation().distance(event.getLocation());
            if (dist > creeper.getExplosionRadius()) continue;
            double multiplier = 1.0 - (dist / creeper.getExplosionRadius());
            double damage = 24.0 * multiplier;
            if (damage < 1) damage = 1;
            player.damage(damage);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Slime slime)) return;
        if (!slime.getScoreboardTags().contains(TAG)) return;
        List<String> messages = plugin.getConfig().getStringList("head-slime.death-messages");
        if (!messages.isEmpty()) {
            String raw = messages.get(random.nextInt(messages.size()));
            event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', raw.replace("%player%", event.getEntity().getName())));
        }
    }

    @EventHandler
    public void onSlimeDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Slime slime)) return;
        if (!slime.getScoreboardTags().contains(TAG)) return;
        slime.getWorld().dropItemNaturally(slime.getLocation(), HeadSlimeHeart.HEAD_SLIME_HEART.clone());
    }

    private class HeadSlimeInstance {
        final UUID slimeId;
        final Slime slime;
        UUID targetId;
        boolean attached;
        int damageTicks;
        int originalRadius;
        int lastSkeletonBurst;
        int skeletonBurstCooldown;

        HeadSlimeInstance(Slime slime) {
            this.slimeId = slime.getUniqueId();
            this.slime = slime;
            this.targetId = null;
            this.attached = false;
            this.damageTicks = 0;
            this.originalRadius = -1;
            this.lastSkeletonBurst = -100;
            this.skeletonBurstCooldown = HeadSlime.this.skeletonBurstCooldown;
        }
    }
}
