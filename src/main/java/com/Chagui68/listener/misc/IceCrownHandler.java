package com.Chagui68.listener.misc;

import com.Chagui68.items.misc.IceCrown;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IceCrownHandler implements Listener {

    private final Plugin plugin;
    private final Random random = new Random();

    private final Map<UUID, Long> snowBlockCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> blizzardCooldowns = new ConcurrentHashMap<>();
    private final Set<UUID> icePathActive = ConcurrentHashMap.newKeySet();
    private final Set<UUID> activeBlizzards = ConcurrentHashMap.newKeySet();
    private final Map<UUID, SelectedBlock> selectedBlocks = new ConcurrentHashMap<>();

    private long snowBlockCooldownMs;
    private static final long SELECT_TIMEOUT_MS = 10000;
    private long blizzardCooldownMs;
    private double projectileSpeed;
    private int projectileMaxTicks;
    private double blizzardDamagePerTick;
    private double adjacentFreezeChance;
    private int blizzardDurationTicks;
    private double blizzardStartingRadius;
    private double blizzardMaxRadius;
    private double blizzardRadiusGrowth;
    private int blizzardSlownessDuration;
    private int blizzardSlownessAmplifier;
    private int blizzardDarknessDuration;
    private int blizzardDarknessAmplifier;
    private double blizzardPushStrength;
    private static final double GRAVITY = 0.08;

    private static class SelectedBlock {
        final Location location;
        final BlockData blockData;
        final Material material;
        final long timestamp;

        SelectedBlock(Location location, BlockData blockData, Material material) {
            this.location = location;
            this.blockData = blockData;
            this.material = material;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > SELECT_TIMEOUT_MS;
        }
    }

    public IceCrownHandler(Plugin plugin) {
        this.plugin = plugin;
        var config = plugin.getConfig();
        snowBlockCooldownMs = config.getLong("items.ice-crown.snow-block-launch.cooldown-ms", 10000);
        blizzardCooldownMs = config.getLong("items.ice-crown.blizzard.cooldown-ms", 60000);
        projectileSpeed = config.getDouble("items.ice-crown.snow-block-launch.projectile-speed", 2.0);
        projectileMaxTicks = config.getInt("items.ice-crown.snow-block-launch.max-ticks", 100);
        blizzardDamagePerTick = config.getDouble("items.ice-crown.blizzard.damage-per-tick", 3.5);
        adjacentFreezeChance = config.getDouble("items.ice-crown.ice-path.adjacent-freeze-chance", 0.3);
        blizzardDurationTicks = config.getInt("items.ice-crown.blizzard.duration-ticks", 100);
        blizzardStartingRadius = config.getDouble("items.ice-crown.blizzard.starting-radius", 1.0);
        blizzardMaxRadius = config.getDouble("items.ice-crown.blizzard.max-radius", 8.0);
        blizzardRadiusGrowth = config.getDouble("items.ice-crown.blizzard.radius-growth-per-tick", 0.07);
        blizzardSlownessDuration = config.getInt("items.ice-crown.blizzard.slowness.duration-ticks", 40);
        blizzardSlownessAmplifier = config.getInt("items.ice-crown.blizzard.slowness.amplifier", 2);
        blizzardDarknessDuration = config.getInt("items.ice-crown.blizzard.darkness.duration-ticks", 40);
        blizzardDarknessAmplifier = config.getInt("items.ice-crown.blizzard.darkness.amplifier", 2);
        blizzardPushStrength = config.getDouble("items.ice-crown.blizzard.push-strength", 0.5);
    }

    private boolean hasCrown(Player p) {
        ItemStack main = p.getInventory().getItemInMainHand();
        ItemStack off = p.getInventory().getItemInOffHand();
        return isCrown(main) || isCrown(off);
    }

    private boolean isCrown(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(IceCrown.ICE_CROWN_KEY, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (!isCrown(item)) return;

        Player p = e.getPlayer();

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getHand() != EquipmentSlot.HAND) {
                e.setCancelled(true);
                return;
            }
            if (p.isSneaking()) {
                performBlizzard(p);
            } else {
                performSnowBlockLaunch(p);
            }
            e.setCancelled(true);
        }

        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (e.getHand() != EquipmentSlot.HAND) return;
            toggleIcePath(p);
            e.setCancelled(true);
        }
    }

    private void performSnowBlockLaunch(Player p) {
        UUID id = p.getUniqueId();
        Long last = snowBlockCooldowns.get(id);
        if (last != null && System.currentTimeMillis() - last < snowBlockCooldownMs) {
            long remaining = (snowBlockCooldownMs - (System.currentTimeMillis() - last)) / 1000;
            p.sendMessage(ChatColor.RED + "Snow Block Launch on cooldown (" + remaining + "s)");
            return;
        }

        SelectedBlock selected = selectedBlocks.get(id);

        if (selected == null || selected.isExpired()) {
            Block targetBlock = p.getTargetBlockExact(5);
            if (targetBlock == null || !isSnowOrIce(targetBlock.getType())) {
                p.sendMessage(ChatColor.RED + "You must be looking at a snow or ice block!");
                return;
            }

            Location blockLoc = targetBlock.getLocation();
            selectedBlocks.put(id, new SelectedBlock(blockLoc, targetBlock.getBlockData(), targetBlock.getType()));

            raiseBlockVisual(p, blockLoc, targetBlock.getBlockData());

            p.sendMessage(ChatColor.AQUA + "Block selected! Right-click an entity to launch it.");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
            return;
        }

        LivingEntity target = getTargetInSight(p, 30);
        if (target == null) {
            p.sendMessage(ChatColor.RED + "No target in sight! Look at an entity to attack.");
            return;
        }

        selectedBlocks.remove(id);

        Block originalBlock = selected.location.getBlock();
        originalBlock.setType(Material.AIR);

        Location start = selected.location.clone().add(0.5, 3.5, 0.5);
        final boolean isSnow = isSnow(selected.material);

        FallingBlock fb = p.getWorld().spawnFallingBlock(start, selected.blockData);
        fb.setDropItem(false);
        fb.setHurtEntities(false);
        fb.setInvulnerable(true);
        fb.addScoreboardTag("ICE_CROWN_PROJECTILE");
        fb.setGravity(true);

        Vector toTarget = target.getEyeLocation().toVector().subtract(start.toVector());
        double distance = toTarget.length();
        if (distance > 0) {
            Vector direction = toTarget.normalize();
            double horizontalDistance = Math.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ());
            double timeToTarget = horizontalDistance / projectileSpeed;
            double requiredYVelocity = (toTarget.getY() + 0.5 * GRAVITY * timeToTarget * timeToTarget) / timeToTarget;

            Vector velocity = direction.multiply(projectileSpeed).setY(requiredYVelocity);
            fb.setVelocity(velocity);
        }

        p.getWorld().playSound(start, Sound.ENTITY_SNOW_GOLEM_SHOOT, 1.2f, 0.7f);
        p.getWorld().spawnParticle(Particle.SNOWFLAKE, start, 20, 0.3, 0.3, 0.3, 0.1);
        if (selected.blockData != null) {
            p.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, start, 15, 0.3, 0.3, 0.3, 0, selected.blockData);
        }

        snowBlockCooldowns.put(id, System.currentTimeMillis());

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                if (ticks > projectileMaxTicks || fb.isDead() || !fb.isValid()) {
                    if (fb.isValid()) fb.remove();
                    cancel();
                    return;
                }

                Location loc = fb.getLocation();
                p.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, loc, 2, 0.1, 0.1, 0.1, 0);
                p.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 3, 0.15, 0.15, 0.15, 0.02);

                if (fb.isOnGround()) {
                    impactSnowBlock(fb, p, target, selected.blockData, isSnow);
                    cancel();
                    return;
                }

                if (fb.getLocation().distanceSquared(target.getLocation()) < 2.25) {
                    impactSnowBlock(fb, p, target, selected.blockData, isSnow);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void raiseBlockVisual(Player p, Location blockLoc, BlockData blockData) {
        Location start = blockLoc.clone().add(0.5, 0.5, 0.5);
        Location end = blockLoc.clone().add(0.5, 3.5, 0.5);

        FallingBlock risingBlock = p.getWorld().spawnFallingBlock(start, blockData);
        risingBlock.setDropItem(false);
        risingBlock.setHurtEntities(false);
        risingBlock.setInvulnerable(true);
        risingBlock.setGravity(false);
        risingBlock.setVelocity(new Vector(0, 0.3, 0));

        new BukkitRunnable() {
            double progress = 0;

            @Override
            public void run() {
                progress += 0.1;
                if (progress >= 1.0) {
                    if (risingBlock.isValid()) risingBlock.remove();
                    cancel();
                    return;
                }

                Location current = start.clone().add(0, progress * 3.0, 0);
                risingBlock.teleport(current);
                p.getWorld().spawnParticle(Particle.SNOWFLAKE, current, 5, 0.2, 0.2, 0.2, 0.05);
                if (blockData != null) {
                    p.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, current, 3, 0.2, 0.2, 0.2, 0, blockData);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        p.getWorld().playSound(start, Sound.BLOCK_SNOW_STEP, 0.8f, 1.2f);
        p.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, start, 30, 0.5, 0.5, 0.5, 0.1, blockData);
    }

    private void impactSnowBlock(FallingBlock fb, Player shooter, LivingEntity target, BlockData blockData, boolean isSnow) {
        Location loc = fb.getLocation();
        World w = loc.getWorld();

        if (blockData != null) {
            w.spawnParticle(Particle.BLOCK_CRUMBLE, loc, 40, 0.6, 0.6, 0.6, 0.1, blockData);
        }
        w.spawnParticle(Particle.SNOWFLAKE, loc, 25, 0.5, 0.5, 0.5, 0.15);
        w.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.2f, 0.5f);
        w.playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.8f, 1f);

        double damage = 12.0;
        target.damage(damage, shooter);

        if (isSnow) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 0));
        } else {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 100, 0));
        }

        Vector kbDir = target.getLocation().toVector().subtract(loc.toVector());
        if (kbDir.lengthSquared() > 0) {
            Vector kb = kbDir.normalize().multiply(0.8).setY(0.3);
            target.setVelocity(target.getVelocity().add(kb));
        }

        for (Block b : getNearbyBlocks(loc, 2)) {
            if (b.getType() == Material.WATER) b.setType(Material.ICE);
        }

        if (fb.isValid()) fb.remove();
    }

    private void performBlizzard(Player p) {
        UUID id = p.getUniqueId();
        Long last = blizzardCooldowns.get(id);
        if (last != null && System.currentTimeMillis() - last < blizzardCooldownMs) {
            long remaining = (blizzardCooldownMs - (System.currentTimeMillis() - last)) / 1000;
            p.sendMessage(ChatColor.RED + "Blizzard on cooldown (" + remaining + "s)");
            return;
        }

        if (activeBlizzards.contains(id)) {
            p.sendMessage(ChatColor.RED + "Blizzard already active!");
            return;
        }

        activeBlizzards.add(id);
        blizzardCooldowns.put(id, System.currentTimeMillis());

        final UUID blizzardId = id;
        p.sendTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "BLIZZARD!", ChatColor.WHITE + "Winter is unleashed!", 5, 30, 10);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.5f, 0.3f);
        p.getWorld().playSound(p.getLocation(), Sound.WEATHER_RAIN_ABOVE, 1f, 1f);

        new BukkitRunnable() {
            int ticks = 0;
            double expandingRadius = blizzardStartingRadius;

            @Override
            public void run() {
                ticks += 2;
                if (ticks > blizzardDurationTicks || !p.isOnline()) {
                    activeBlizzards.remove(blizzardId);
                    cancel();
                    return;
                }

                expandingRadius = Math.min(blizzardMaxRadius, blizzardStartingRadius + ticks * blizzardRadiusGrowth);
                Location center = p.getLocation();

                for (double angle = 0; angle < 360; angle += 10) {
                    double rad = Math.toRadians(angle);
                    double x = Math.cos(rad) * expandingRadius;
                    double z = Math.sin(rad) * expandingRadius;
                    Location loc = center.clone().add(x, random.nextDouble() * 3, z);
                    p.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 2, 0.3, 0.3, 0.3, 0.01);
                    if (random.nextDouble() < 0.3) {
                        p.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0.2, 0.1, 0.2, 0.01);
                    }
                }

                for (int i = 0; i < 8; i++) {
                    double px = center.getX() + (random.nextDouble() - 0.5) * expandingRadius * 2;
                    double py = center.getY() + random.nextDouble() * 4;
                    double pz = center.getZ() + (random.nextDouble() - 0.5) * expandingRadius * 2;
                    p.getWorld().spawnParticle(Particle.SNOWFLAKE, new Location(center.getWorld(), px, py, pz), 3, 0.4, 0.4, 0.4, 0.03);
                }

                for (Entity entity : p.getWorld().getNearbyEntities(center, expandingRadius, 4, expandingRadius)) {
                    if (entity instanceof LivingEntity && entity != p) {
                        LivingEntity le = (LivingEntity) entity;
                        le.damage(blizzardDamagePerTick, p);
                        le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, blizzardSlownessDuration, blizzardSlownessAmplifier));
                        le.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, blizzardDarknessDuration, blizzardDarknessAmplifier));
                        Vector pushDir = le.getLocation().toVector().subtract(center.toVector());
                        if (pushDir.lengthSquared() > 0) {
                            le.setVelocity(le.getVelocity().add(pushDir.normalize().multiply(blizzardPushStrength)));
                        }
                    }
                }

                int radiusInt = (int) Math.ceil(expandingRadius);
                for (int x = -radiusInt; x <= radiusInt; x++) {
                    for (int z = -radiusInt; z <= radiusInt; z++) {
                        if (x * x + z * z > expandingRadius * expandingRadius) continue;
                        Block b = center.clone().add(x, -1, z).getBlock();
                        if (b.getType() == Material.WATER) b.setType(Material.ICE);
                        Block ground = center.clone().add(x, 0, z).getBlock();
                        if (ground.getType() == Material.WATER) ground.setType(Material.ICE);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!icePathActive.contains(p.getUniqueId())) return;
        if (!hasCrown(p)) {
            icePathActive.remove(p.getUniqueId());
            return;
        }

        Location to = e.getTo();
        if (to == null) return;
        Block below = to.clone().subtract(0, 1, 0).getBlock();

        if (below.getType() == Material.WATER) {
            below.setType(Material.FROSTED_ICE);
            p.getWorld().spawnParticle(Particle.SNOWFLAKE, below.getLocation().add(0.5, 1, 0.5), 3, 0.2, 0.1, 0.2, 0.01);
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Block adj = to.clone().add(dx, -1, dz).getBlock();
                if (adj.getType() == Material.WATER && random.nextDouble() < adjacentFreezeChance) {
                    adj.setType(Material.FROSTED_ICE);
                }
            }
        }
    }

    private void toggleIcePath(Player p) {
        UUID id = p.getUniqueId();
        if (icePathActive.contains(id)) {
            icePathActive.remove(id);
            p.sendMessage(ChatColor.RED + "Ice Path: " + ChatColor.GRAY + "DISABLED");
            p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1.5f);
        } else {
            icePathActive.add(id);
            p.sendMessage(ChatColor.AQUA + "Ice Path: " + ChatColor.GREEN + "ENABLED");
            p.playSound(p.getLocation(), Sound.BLOCK_SNOW_STEP, 0.8f, 0.5f);
        }
    }

    private boolean isSnowOrIce(Material m) {
        return m == Material.SNOW_BLOCK || m == Material.SNOW
                || m == Material.ICE || m == Material.PACKED_ICE
                || m == Material.BLUE_ICE || m == Material.FROSTED_ICE;
    }

    private boolean isSnow(Material m) {
        return m == Material.SNOW_BLOCK || m == Material.SNOW;
    }

    private LivingEntity getTargetInSight(Player p, double range) {
        RayTraceResult ray = p.getWorld().rayTrace(p.getEyeLocation(),
                p.getEyeLocation().getDirection(), range, FluidCollisionMode.NEVER, true, 0.5,
                entity -> entity instanceof LivingEntity && entity != p);
        return ray != null ? (LivingEntity) ray.getHitEntity() : null;
    }

    private List<Block> getNearbyBlocks(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    blocks.add(center.clone().add(x, y, z).getBlock());
                }
            }
        }
        return blocks;
    }

    @EventHandler
    public void onCrownDamageModifier(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p) {
            if (hasCrown(p)) {
                e.setDamage(e.getDamage() * 0.8);
            }
        }
        if (e.getEntity() instanceof Player p && hasCrown(p)) {
            if (e.getCause() == EntityDamageEvent.DamageCause.FREEZE) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        snowBlockCooldowns.remove(id);
        blizzardCooldowns.remove(id);
        icePathActive.remove(id);
        activeBlizzards.remove(id);
        selectedBlocks.remove(id);
    }

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        ItemStack item = e.getPlayerItem();
        if (item == null || !isCrown(item)) return;
        e.setCancelled(true);
    }
}