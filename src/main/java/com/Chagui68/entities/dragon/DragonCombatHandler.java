package com.Chagui68.entities.dragon;

import com.Chagui68.utils.VersionSafe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Bat;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Chest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class DragonCombatHandler implements Listener {

    private final Plugin plugin;
    private static final double DRAGON_HEALTH = 2000.0;
    private static final double DRAGON_DAMAGE = 15.0;
    private static final double DAMAGE_CAP = 50.0;
    private static final double REGEN_MULTIPLIER = 0.2; // Scaled mathematically from 0.2 to match 2000 HP pool
    private static final double EXECUTION_RADIUS = 300.0; // Configurable radius from center
    private static final double EXECUTION_RADIUS_SQ = EXECUTION_RADIUS * EXECUTION_RADIUS;
    private static final long GLOBAL_COOLDOWN_MS = 60000; // 1 minute
    private static final double SLAM_DAMAGE = 20.0; // Damage on impact
    private static final double STORM_BOLT_DAMAGE = 10.0; // Damage per lightning bolt
    private static final double VOID_BREATH_DAMAGE = 4.0; // Damage per tick (0.5s)
    private static final double ABYSSAL_SCREAM_RADIUS_STEP = 50.0; // Radius growth per pulse
    private static final double SHADOW_BAT_HEALTH = 20.0; // Health for shadow enemies

    // Phase Passive Constants
    private static final double STATIC_DISCHARGE_DAMAGE = 5.0;
    private static final double STATIC_DISCHARGE_KNOCKBACK = 1.2;
    private static final double SIPHON_LIFE_PERCENT = 0.005; // 0.5% max health per hit
    private static final double SOUL_LINK_RADIUS = 12.0;
    private static final double GRAVITY_WELL_FORCE = 0.15;
    private static final double SHADOW_ARMOR_REDUCTION = 0.30; // 30% reduction
    private static final String PHASE_MESSAGE_PREFIX = ChatColor.GOLD + "[MSC] ";

    private final Random random = new Random();

    // Tracking for custom attacks
    private final Set<UUID> slammingDragons = new HashSet<>();
    private final Set<UUID> enragedDragons = new HashSet<>();
    private final Map<UUID, BarColor> currentPhaseColors = new HashMap<>(); // Dragon UUID -> Current color
    private final Map<UUID, Integer> executionTimers = new HashMap<>(); // Player UUID -> Seconds left
    private final Map<UUID, Long> lastGlobalAbilityTimes = new HashMap<>(); // Dragon UUID -> Timestamp
    private boolean stormActive = false;
    private boolean breathFloorActive = false;
    private boolean rouletteActive = false;

    public DragonCombatHandler(Plugin plugin) {
        this.plugin = plugin;
        startExecutionScanner();
    }

    public boolean isSlamming(EnderDragon dragon) {
        return slammingDragons.contains(dragon.getUniqueId());
    }

    /**
     * Constant scanner that checks for players with low health near the dragon.
     */
    private void startExecutionScanner() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (EnderDragon dragon : world.getEntitiesByClass(EnderDragon.class)) {
                        if (dragon.getScoreboardTags().contains("MSC_CustomDragon") && dragon.isValid()) {
                            checkPlayersUnderExecution(dragon);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Execute every second (20 ticks)
    }

    private void checkPlayersUnderExecution(EnderDragon dragon) {
        if (dragon.getWorld() == null)
            return;
        Location center = new Location(dragon.getWorld(), 0, 65, 0); // Center of the End

        for (Player p : dragon.getWorld().getPlayers()) {
            // Check distance from center instead of dragon
            if (p.getLocation().distanceSquared(center) > EXECUTION_RADIUS_SQ) {
                executionTimers.remove(p.getUniqueId());
                continue;
            }

            // Oppressive Presence: Hunger aura near dragon
            PotionEffectType hunger = VersionSafe.getPotionEffectSafe("HUNGER");
            if (hunger != null)
                VersionSafe.applyPotionEffectSafe(p, hunger, 60, 1);

            BarColor currentPhase = currentPhaseColors.getOrDefault(dragon.getUniqueId(), BarColor.WHITE);

            // Deep Chill: Slowness and Freezing in BLUE phase
            if (currentPhase == BarColor.BLUE) {
                PotionEffectType slow = VersionSafe.getPotionEffectSafe("SLOWNESS", "SLOW");
                if (slow != null)
                    VersionSafe.applyPotionEffectSafe(p, slow, 60, 0);
                p.setFreezeTicks(Math.min(p.getFreezeTicks() + 40, 140)); // Screen frost effect
                VersionSafe.sendActionBarSafe(p, ChatColor.AQUA + "DEEP CHILL: You are freezing!");
            }

            // Soul Link: Weakness if apart in PINK/PURPLE phases
            if (currentPhase == BarColor.PINK || currentPhase == BarColor.PURPLE) {
                if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                    boolean nearAlly = false;
                    int aliveAlliesInSurvival = 0;

                    for (Player ally : dragon.getWorld().getPlayers()) {
                        if (ally.equals(p))
                            continue;
                        if (ally.isDead() || (ally.getGameMode() != GameMode.SURVIVAL
                                && ally.getGameMode() != GameMode.ADVENTURE))
                            continue;

                        aliveAlliesInSurvival++;
                        if (ally.getLocation().distanceSquared(p.getLocation()) <= SOUL_LINK_RADIUS
                                * SOUL_LINK_RADIUS) {
                            nearAlly = true;
                        }
                    }

                    if (aliveAlliesInSurvival > 0 && !nearAlly) {
                        PotionEffectType weakness = VersionSafe.getPotionEffectSafe("WEAKNESS");
                        if (weakness != null)
                            VersionSafe.applyPotionEffectSafe(p, weakness, 60, 0);
                        VersionSafe.sendActionBarSafe(p,
                                ChatColor.RED + "SOUL LINK BROKEN: Stay near your teammates!");
                    }
                }
            }

            // Gravity Well: Pull down in PURPLE phase
            if (currentPhase == BarColor.PURPLE && !p.isOnGround()) {
                p.setVelocity(p.getVelocity().add(new org.bukkit.util.Vector(0, -GRAVITY_WELL_FORCE, 0)));
            }

            Attribute maxHealthAttr = VersionSafe.getAttributeSafe("MAX_HEALTH", "GENERIC_MAX_HEALTH");
            if (maxHealthAttr == null || p.getAttribute(maxHealthAttr) == null)
                continue;
            double maxHealth = p.getAttribute(maxHealthAttr).getValue();
            double healthPercent = p.getHealth() / maxHealth;

            // Trigger if health is <= 20% and in Survival or Adventure mode
            if (healthPercent <= 0.20
                    && (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE)) {
                int timeLeft = executionTimers.getOrDefault(p.getUniqueId(), 10);

                if (timeLeft > 0) {
                    // Send centered title countdown
                    String title = ChatColor.DARK_RED + "" + ChatColor.BOLD + "ABYSSAL EXECUTION: " + timeLeft + "s";
                    String subtitle = ChatColor.RED + "HEAL ABOVE 20% NOW!";

                    // short stay (25 ticks = 1.25s) to refresh every second
                    p.sendTitle(title, subtitle, 0, 25, 5);

                    if (timeLeft == 10) {
                        VersionSafe.playSoundSafe(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f);
                    } else {
                        VersionSafe.playSoundSafe(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f,
                                0.5f + (10 - timeLeft) * 0.1f);
                    }

                    executionTimers.put(p.getUniqueId(), timeLeft - 1);
                } else {
                    // Time's up! Fatal blow.
                    performAbyssalExecution(dragon, p);
                    executionTimers.remove(p.getUniqueId());
                }
            } else {
                // Safety achieved
                if (executionTimers.containsKey(p.getUniqueId())) {
                    p.sendTitle(ChatColor.GREEN + "EXECUTION AVERTED", ChatColor.DARK_GREEN + "You are safe for now", 5,
                            40, 5);
                    executionTimers.remove(p.getUniqueId());
                }
            }
        }
    }

    public void performAbyssalExecution(EnderDragon dragon, Player victim) {
        // Visual effects
        victim.sendTitle(ChatColor.DARK_RED + "EXECUTED", ChatColor.RED + "The void consumes you", 5, 20, 5);
        VersionSafe.spawnExplosionParticle(victim.getLocation(), 1);
        VersionSafe.spawnBreathParticle(victim.getLocation(), 100, 1, 1, 1, 0.5);
        VersionSafe.playSoundSafe(victim.getLocation(), 2f, 0.2f, "ENTITY_GENERIC_EXPLODE",
                "EXPLODE");

        // Lethal action
        victim.setHealth(0);
    }

    /**
     * Configures a dragon instance with custom attributes.
     */
    public void configureDragon(EnderDragon dragon) {
        double maxHealth = 5000.0; // Assume setup health
        String initialName = getDynamicName(1.0);
        dragon.setCustomName(initialName);
        dragon.setCustomNameVisible(true);

        // Initialize attributes safely for different versions
        VersionSafe.setAttributeBaseSafe(dragon, DRAGON_HEALTH, "MAX_HEALTH", "GENERIC_MAX_HEALTH");
        dragon.setHealth(DRAGON_HEALTH);

        VersionSafe.setAttributeBaseSafe(dragon, DRAGON_DAMAGE, "ATTACK_DAMAGE", "GENERIC_ATTACK_DAMAGE");

        updateDragonBossBar(dragon);
        if (!dragon.getScoreboardTags().contains("MSC_CustomDragon")) {
            dragon.addScoreboardTag("MSC_CustomDragon");
        }
    }

    public void forcePhaseColor(EnderDragon dragon, BarColor color) {
        currentPhaseColors.put(dragon.getUniqueId(), color);
        if (dragon.getBossBar() != null) {
            dragon.getBossBar().setColor(color);
        }
    }

    private void updateDragonBossBar(EnderDragon dragon) {
        if (dragon.getBossBar() == null)
            return;

        Attribute maxAttr = VersionSafe.getAttributeSafe("MAX_HEALTH", "GENERIC_MAX_HEALTH");
        if (maxAttr == null || dragon.getAttribute(maxAttr) == null)
            return;
        double healthPercent = (dragon.getHealth() / dragon.getAttribute(maxAttr).getValue())
                * 100;

        BarColor color;
        if (healthPercent > 90)
            color = BarColor.WHITE;
        else if (healthPercent > 80)
            color = BarColor.GREEN;
        else if (healthPercent > 70)
            color = BarColor.BLUE;
        else if (healthPercent > 60)
            color = BarColor.YELLOW;
        else if (healthPercent > 50)
            color = BarColor.PINK;
        else if (healthPercent > 40)
            color = BarColor.PURPLE;
        else if (healthPercent > 30)
            color = BarColor.WHITE;
        else if (healthPercent > 20)
            color = BarColor.GREEN;
        else if (healthPercent > 10)
            color = BarColor.YELLOW;
        else
            color = BarColor.RED;

        BarColor oldColor = currentPhaseColors.get(dragon.getUniqueId());
        if (oldColor != color) {
            currentPhaseColors.put(dragon.getUniqueId(), color);
            String colorName = color.name().charAt(0) + color.name().substring(1).toLowerCase();
            String message = PHASE_MESSAGE_PREFIX + ChatColor.GRAY + "The Dragon's scales turn " +
                    ChatColor.valueOf(color.name()) + colorName + ChatColor.GRAY + "...";
            for (Player p : dragon.getWorld().getPlayers()) {
                p.sendMessage(message);
            }
        }

        dragon.getBossBar().setColor(color);
        dragon.getBossBar().setStyle(BarStyle.SEGMENTED_20);

        String dynamicName = getDynamicName(healthPercent / 100.0);
        dragon.setCustomName(dynamicName); // Update physical name too
        dragon.getBossBar().setTitle(dynamicName + ChatColor.GRAY + " [" + (int) healthPercent + "%]");
    }

    private String getDynamicName(double healthPercent) {
        if (healthPercent > 0.70) {
            return ChatColor.AQUA + "[ Sovereign of the End ]";
        } else if (healthPercent > 0.30) {
            return ChatColor.GOLD + "[ The Enraged Monarch ]";
        } else if (healthPercent > 0.10) {
            return ChatColor.RED + "[ World Destroyer ]";
        } else {
            return ChatColor.DARK_RED + "" + ChatColor.BOLD + "[ GOD OF THE VOID ]";
        }
    }

    public void performGroundSlam(EnderDragon dragon) {
        if (slammingDragons.contains(dragon.getUniqueId()))
            return;

        slammingDragons.add(dragon.getUniqueId());
        dragon.setPhase(EnderDragon.Phase.FLY_TO_PORTAL); // Force the dragon to head towards the center/lowering
        VersionSafe.playSoundSafe(dragon.getLocation(), 5f, 0.5f, "ENTITY_ENDER_DRAGON_GROWL",
                "ENTITY_ENDERDRAGON_GROWL", "ENTITY_ENDER_DRAGON_AMBIENT");

        // Notify players
        String title = ChatColor.DARK_RED + "" + ChatColor.BOLD + "ABYSSAL DASH";
        String subtitle = ChatColor.RED + "THE VOID IS CHARGING!";
        for (Player p : dragon.getWorld().getPlayers()) {
            p.sendTitle(title, subtitle, 10, 40, 10);
        }

        Bukkit.getScheduler().runTaskTimer(plugin, new java.util.function.Consumer<org.bukkit.scheduler.BukkitTask>() {
            int ticks = 0;

            @Override
            public void accept(org.bukkit.scheduler.BukkitTask task) {
                ticks++;
                if (!dragon.isValid() || dragon.isDead() || ticks > 100) { // 5 second maximum timeout
                    slammingDragons.remove(dragon.getUniqueId());
                    task.cancel();
                    return;
                }

                VersionSafe.spawnBreathParticle(dragon.getLocation(), 10, 2, 1, 2, 0.1);

                // Create a non-destructive explosion every few ticks to damage nearby players
                if (ticks % 4 == 0) {
                    dragon.getWorld().createExplosion(dragon.getLocation(), 3f, false, false);
                    VersionSafe.playSoundSafe(dragon.getLocation(), 1f, 0.5f,
                            "ENTITY_GENERIC_EXPLODE", "EXPLODE");

                    // Restore TNT-like explosion particles safely
                    VersionSafe.spawnExplosionParticle(dragon.getLocation(), 3);

                    // Add a small circular distribution of particles for impact vibe
                    for (int i = 0; i < 8; i++) {
                        double angle = i * Math.PI / 4;
                        double x = Math.cos(angle) * 2;
                        double z = Math.sin(angle) * 2;
                        VersionSafe.spawnBreathParticle(dragon.getLocation().clone().add(x, 0, z), 5,
                                0.1, 0.1, 0.1, 0.05);
                    }
                }

                // Dynamic targeting: find nearest Survival/Adventure player
                Player currentTarget = null;
                double minDistanceSq = Double.MAX_VALUE;
                for (Player p : dragon.getWorld().getPlayers()) {
                    if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                        double distSq = p.getLocation().distanceSquared(dragon.getLocation());
                        if (distSq < minDistanceSq) {
                            minDistanceSq = distSq;
                            currentTarget = p;
                        }
                    }
                }

                if (currentTarget != null) {
                    double distSq = currentTarget.getLocation().distanceSquared(dragon.getLocation());

                    // Stop if we hit the player (within 5 blocks radius)
                    if (distSq < 25) { // 5 blocks squared
                        slammingDragons.remove(dragon.getUniqueId());
                        triggerImpactEffect(dragon);
                        task.cancel();
                        return;
                    }

                    // Update movement direction to follow the nearest victim
                    Vector currentDir = currentTarget.getLocation().toVector().subtract(dragon.getLocation().toVector())
                            .normalize();
                    dragon.setVelocity(currentDir.multiply(3.0)); // Slightly lower speed for better control
                } else {
                    // Fallback to plunging straight down to end the attack if no target
                    dragon.setVelocity(new Vector(0, -2.5, 0));

                    if (dragon.getLocation().getBlock().getType().isSolid() || dragon.getLocation().getY() < 20) {
                        slammingDragons.remove(dragon.getUniqueId());
                        triggerImpactEffect(dragon);
                        task.cancel();
                    }
                }
            }
        }, 0L, 1L);
    }

    public void performVoidBreath(EnderDragon dragon) {
        if (breathFloorActive)
            return;
        breathFloorActive = true;

        VersionSafe.playSoundSafe(dragon.getLocation(), 5f, 2.0f, "ENTITY_ENDER_DRAGON_GROWL",
                "ENTITY_ENDERDRAGON_GROWL");

        String title = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "VOID CORRUPTION";
        String subtitle = ChatColor.RED + "THE FLOOR IS PERISHING! BUILD UP!";
        for (Player p : dragon.getWorld().getPlayers()) {
            p.sendTitle(title, subtitle, 10, 60, 10);
            VersionSafe.playSoundSafe(p.getLocation(), 1f, 0.5f, "ENTITY_WITHER_SPAWN",
                    "WITHER_SPAWN");
        }

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks += 10;
                if (!dragon.isValid() || dragon.isDead() || ticks > 300) { // 15 seconds
                    breathFloorActive = false;
                    this.cancel();
                    return;
                }

                for (Player p : dragon.getWorld().getPlayers()) {
                    if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE)
                        continue;

                    // Check if player is standing on End Stone
                    Block b = p.getLocation().clone().subtract(0, 0.1, 0).getBlock();
                    if (b.getType() == Material.END_STONE) {
                        p.damage(VOID_BREATH_DAMAGE, dragon);
                        VersionSafe.spawnBreathParticle(p.getLocation(), 40, 1, 0, 1, 0.05);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public void startAbilityRoulette(EnderDragon dragon) {
        if (rouletteActive)
            return;
        rouletteActive = true;

        new BukkitRunnable() {
            int ticks = 0;
            String[] abilitiesList = { "💥 ABYSSAL DASH 💥", "⚡ LIGHTNING STORM ⚡", "🟣 VOID CORRUPTION 🟣",
                    "🦇 SHADOW ENEMIES 🦇", "🔊 ABYSSAL SCREAM 🔊", "☄️ METEOR SHOWER ☄️", "🌫️ ABYSSAL MIST 🌫️",
                    "⚛️ VOID BEAM ⚛️" };
            ChatColor[] colors = { ChatColor.RED, ChatColor.GOLD, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE,
                    ChatColor.AQUA, ChatColor.RED, ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE };

            @Override
            public void run() {
                ticks++;

                // Visual "rolling" effect
                int index = ticks % abilitiesList.length;
                String titleText = colors[index] + "" + ChatColor.BOLD + abilitiesList[index];
                String subtitleText = ChatColor.GRAY + "--- ROLLING ---";

                for (Player p : dragon.getWorld().getPlayers()) {
                    p.sendTitle(titleText, subtitleText, 0, 5, 0);
                    VersionSafe.playSoundSafe(p.getLocation(), 0.5f, 1.5f, "BLOCK_NOTE_BLOCK_HAT",
                            "BLOCK_NOTE_HAT");
                }

                if (ticks > 60) { // Roll for 3.0 seconds
                    this.cancel();
                    rouletteActive = false;

                    int choice = random.nextInt(8);
                    switch (choice) {
                        case 0:
                            performGroundSlam(dragon);
                            break;
                        case 1:
                            performLightningStorm(dragon);
                            break;
                        case 2:
                            performVoidBreath(dragon);
                            break;
                        case 3:
                            performShadowEnemies(dragon);
                            break;
                        case 4:
                            performAbyssalScream(dragon);
                            break;
                        case 5:
                            performMeteorShower(dragon);
                            break;
                        case 6:
                            performAbyssalMist(dragon);
                            break;
                        case 7:
                            performVoidBeam(dragon);
                            break;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void performShadowEnemies(EnderDragon dragon) {
        VersionSafe.playSoundSafe(dragon.getLocation(), 5f, 0.5f, "ENTITY_ENDER_DRAGON_GROWL",
                "ENTITY_ENDERDRAGON_GROWL");

        String title = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "SHADOW ENEMIES";
        String subtitle = ChatColor.LIGHT_PURPLE + "THE VOID CHASES YOU!";
        for (Player p : dragon.getWorld().getPlayers()) {
            p.sendTitle(title, subtitle, 10, 40, 10);
            VersionSafe.playSoundSafe(p.getLocation(), 1f, 1f, "ENTITY_BAT_AMBIENT",
                    "ENTITY_BAT_AMBIENT");
        }

        for (int i = 0; i < 2; i++) {
            Location spawnLoc = dragon.getLocation().clone().add(random.nextInt(11) - 5, 2, random.nextInt(11) - 5);
            Bat bat = dragon.getWorld().spawn(spawnLoc, Bat.class);
            bat.addScoreboardTag("MSC_ShadowEnemy");
            bat.setCustomName(ChatColor.DARK_PURPLE + "Void Shadow");
            bat.setCustomNameVisible(false);

            VersionSafe.setAttributeBaseSafe(bat, SHADOW_BAT_HEALTH, "MAX_HEALTH", "GENERIC_MAX_HEALTH");
            bat.setHealth(SHADOW_BAT_HEALTH);

            new BukkitRunnable() {
                int lifetime = 0;

                @Override
                public void run() {
                    lifetime += 5;
                    if (lifetime > 200 || !bat.isValid() || bat.isDead() || !dragon.isValid()) { // 10 second timeout
                        if (bat.isValid()) {
                            VersionSafe.spawnBreathParticle(bat.getLocation(), 20, 0.5, 0.5, 0.5,
                                    0.05);
                            bat.remove();
                        }
                        this.cancel();
                        return;
                    }

                    // Visual aura
                    VersionSafe.spawnSmokeParticle(bat.getLocation(), 5, 0.2, 0.2, 0.2, 0.02);

                    // Pursuit logic: Find nearest player
                    Player target = null;
                    double minDistSq = 2500; // 50 blocks
                    for (Player p : bat.getWorld().getPlayers()) {
                        if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                            double d2 = p.getLocation().distanceSquared(bat.getLocation());
                            if (d2 < minDistSq) {
                                minDistSq = d2;
                                target = p;
                            }
                        }
                    }

                    if (target != null) {
                        Vector dir = target.getEyeLocation().toVector().subtract(bat.getLocation().toVector())
                                .normalize();
                        bat.setVelocity(dir.multiply(0.4));

                        // Apply effects if close
                        if (bat.getLocation().distanceSquared(target.getLocation()) < 16) {
                            PotionEffectType darkness = VersionSafe.getPotionEffectSafe("DARKNESS");
                            if (darkness != null)
                                VersionSafe.applyPotionEffectSafe(target, darkness, 60, 0);

                            PotionEffectType blindness = VersionSafe.getPotionEffectSafe("BLINDNESS");
                            if (blindness != null)
                                VersionSafe.applyPotionEffectSafe(target, blindness, 60, 0);
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 5L);
        }
    }

    public void performAbyssalScream(EnderDragon dragon) {
        VersionSafe.playSoundSafe(dragon.getLocation(), 10f, 0.1f, "ENTITY_ENDER_DRAGON_GROWL",
                "ENTITY_ENDERDRAGON_GROWL");
        VersionSafe.playSoundSafe(dragon.getLocation(), 5f, 0.5f, "ENTITY_DRAGON_FIREBALL_EXPLODE",
                "ENTITY_ENDERDRAGON_FIREBALL_EXPLODE");

        String title = ChatColor.AQUA + "" + ChatColor.BOLD + "ABYSSAL SCREAM";
        String subtitle = ChatColor.GRAY + "NO ESCAPE FROM THE VOID!";
        for (Player p : dragon.getWorld().getPlayers()) {
            p.sendTitle(title, subtitle, 5, 20, 5);
        }

        new BukkitRunnable() {
            int ticks = 0;
            private final Set<UUID> hitPlayers = new HashSet<>();

            @Override
            public void run() {
                ticks++;
                if (ticks > 5) {
                    this.cancel();
                    return;
                }

                // Expanding wave effect
                double radius = ticks * ABYSSAL_SCREAM_RADIUS_STEP;
                for (double angle = 0; angle < 360; angle += 15) {
                    double rad = Math.toRadians(angle);
                    double x = Math.cos(rad) * radius;
                    double z = Math.sin(rad) * radius;
                    Location loc = dragon.getLocation().clone().add(x, 2, z);
                    VersionSafe.spawnBreathParticle(loc, 5, 0.5, 0.5, 0.5, 0.05);
                }

                // Apply effect to players in range
                for (Player p : dragon.getWorld().getPlayers()) {
                    if (hitPlayers.contains(p.getUniqueId()))
                        continue;

                    if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                        if (p.getLocation().distanceSquared(dragon.getLocation()) < (radius * radius)) {
                            hitPlayers.add(p.getUniqueId());
                            PotionEffectType levitation = VersionSafe.getPotionEffectSafe("LEVITATION");
                            if (levitation != null)
                                VersionSafe.applyPotionEffectSafe(p, levitation, 200, 1);
                            VersionSafe.playSoundSafe(p.getLocation(), 0.5f, 0.5f,
                                    "ENTITY_PHANTOM_SWOOP", "ENTITY_PHANTOM_SWOOP");
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public void performMeteorShower(EnderDragon dragon) {
        VersionSafe.playSoundSafe(dragon.getLocation(), 2f, 0.5f, "ENTITY_WITHER_SHOOT",
                "WITHER_SHOOT");

        String title = ChatColor.RED + "" + ChatColor.BOLD + "METEOR SHOWER";
        String subtitle = ChatColor.GRAY + "DESTRUCTION FROM THE HEAVENS!";
        for (Player p : dragon.getWorld().getPlayers()) {
            p.sendTitle(title, subtitle, 10, 40, 10);
        }

        new org.bukkit.scheduler.BukkitRunnable() {
            int waves = 0;

            @Override
            public void run() {
                waves++;
                if (waves > 15) { // 15 waves of meteors
                    this.cancel();
                    return;
                }

                // Spawn 3-5 meteors per wave around players
                for (Player p : dragon.getWorld().getPlayers()) {
                    if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                        for (int i = 0; i < 2; i++) {
                            double offsetX = (random.nextDouble() - 0.5) * 40;
                            double offsetZ = (random.nextDouble() - 0.5) * 40;
                            Location spawnLoc = p.getLocation().clone().add(offsetX, 50, offsetZ);
                            Location targetLoc = p.getLocation().clone().add(offsetX * 0.5, 0, offsetZ * 0.5);

                            spawnMeteor(spawnLoc, targetLoc);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void spawnMeteor(Location start, Location target) {
        new BukkitRunnable() {
            Location current = start.clone();
            Vector dir = target.toVector().subtract(start.toVector()).normalize().multiply(1.5);
            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                if (ticks > 60 || current.getY() < 0 || current.getBlock().getType().isSolid()) {
                    // Impact
                    VersionSafe.spawnLargeExplosion(current, 1);
                    VersionSafe.playSoundSafe(current, 0.8f, 0.8f, "ENTITY_GENERIC_EXPLODE",
                            "EXPLODE");

                    // Small damage radius, low environmental impact
                    for (Entity e : current.getWorld().getNearbyEntities(current, 4, 4, 4)) {
                        if (e instanceof Player) {
                            ((Player) e).damage(6.0); // Balanced damage
                        }
                    }
                    this.cancel();
                    return;
                }

                current.add(dir);
                VersionSafe.spawnFireParticle(current, 10, 0.2, 0.2, 0.2, 0.05);
                VersionSafe.spawnSmokeParticle(current, 5, 0.1, 0.1, 0.1, 0.02);
                if (ticks % 2 == 0) {
                    VersionSafe.spawnLavaParticle(current, 3, 0.1, 0.1, 0.1, 0.1);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void performAbyssalMist(EnderDragon dragon) {
        VersionSafe.playSoundSafe(dragon.getLocation(), 2f, 0.5f, "ENTITY_SQUID_SQUISH",
                "ENTITY_SQUID_SQUIRT", "ENTITY_PLAYER_BREATH");

        String title = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "ABYSSAL MIST";
        String subtitle = ChatColor.GRAY + "THE VOID CONSUMES YOUR SIGHT!";
        for (Player p : dragon.getWorld().getPlayers()) {
            p.sendTitle(title, subtitle, 10, 40, 10);

            PotionEffectType blindness = VersionSafe.getPotionEffectSafe("BLINDNESS");
            if (blindness != null)
                VersionSafe.applyPotionEffectSafe(p, blindness, 300, 0);

            PotionEffectType darkness = VersionSafe.getPotionEffectSafe("DARKNESS");
            if (darkness != null)
                VersionSafe.applyPotionEffectSafe(p, darkness, 300, 0);
        }

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                if (ticks > 300) { // 15 seconds
                    this.cancel();
                    return;
                }

                // Visual mist around players
                for (Player p : dragon.getWorld().getPlayers()) {
                    if (random.nextInt(3) == 0) {
                        Location loc = p.getLocation().add(
                                (random.nextDouble() - 0.5) * 10,
                                random.nextDouble() * 3,
                                (random.nextDouble() - 0.5) * 10);
                        VersionSafe.spawnMistParticle(loc, 5, 1, 1, 1, 0.01);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void triggerImpactEffect(EnderDragon dragon) {
        VersionSafe.playSoundSafe(dragon.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 5f, 0.8f);
        VersionSafe.spawnLargeExplosion(dragon.getLocation(), 5);
        VersionSafe.spawnBreathParticle(dragon.getLocation(), 100, 5, 2, 5, 0.2);

        for (Entity entity : dragon.getNearbyEntities(20, 5, 20)) {
            if (entity instanceof Player) {
                Player p = (Player) entity;
                p.damage(SLAM_DAMAGE, dragon);
                p.setVelocity(p.getLocation().toVector().subtract(dragon.getLocation().toVector()).normalize()
                        .multiply(2).setY(1.5));
            }
        }
    }

    public void performLightningStorm(EnderDragon dragon) {
        if (stormActive)
            return;
        stormActive = true;

        VersionSafe.playSoundSafe(dragon.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 5f, 1.5f);

        String title = ChatColor.GOLD + "LIGHTNING STORM!";
        String subtitle = ChatColor.YELLOW + "Take cover under solid blocks!";
        for (Player p : dragon.getWorld().getPlayers()) {
            p.sendTitle(title, subtitle, 10, 60, 10);
        }

        Map<Integer, Integer> yFrequencies = new HashMap<>();
        for (Player p : dragon.getWorld().getPlayers()) {
            int y = p.getLocation().getBlockY();
            yFrequencies.put(y, yFrequencies.getOrDefault(y, 0) + 1);
        }

        int targetY = 65;
        int maxFocus = 0;
        for (Map.Entry<Integer, Integer> entry : yFrequencies.entrySet()) {
            if (entry.getValue() > maxFocus) {
                maxFocus = entry.getValue();
                targetY = entry.getKey();
            }
        }

        final int finalTargetY = targetY;

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 50 || !dragon.isValid()) {
                    stormActive = false;
                    this.cancel();
                    return;
                }
                for (int i = 0; i < 5; i++) {
                    spawnRandomLightning(dragon.getLocation());
                    count++;
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void spawnRandomLightning(Location center) {
        World world = center.getWorld();
        if (world == null)
            return;

        int x = center.getBlockX();
        int z = center.getBlockZ();

        // 10% chance to target near a random survival/adventure player, 90% chance
        // completely random
        boolean targeted = false;
        if (random.nextDouble() < 0.10) {
            java.util.List<Player> validTargets = new java.util.ArrayList<>();
            for (Player p : world.getPlayers()) {
                if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                    if (p.getLocation().distanceSquared(center) < 10000) { // Within 100 blocks
                        validTargets.add(p);
                    }
                }
            }
            if (!validTargets.isEmpty()) {
                Player target = validTargets.get(random.nextInt(validTargets.size()));
                x = target.getLocation().getBlockX() + (random.nextInt(15) - 7); // Cluster near player
                z = target.getLocation().getBlockZ() + (random.nextInt(15) - 7);
                targeted = true;
            }
        }

        if (!targeted) {
            // Purely random strikes within an 80-block area around the dragon
            x = center.getBlockX() + (random.nextInt(81) - 40);
            z = center.getBlockZ() + (random.nextInt(81) - 40);
        }

        int highestY = world.getHighestBlockYAt(x, z);
        Location strikeLoc = new Location(world, x, highestY, z);

        // Visual lightning (no damage to terrain)
        world.strikeLightningEffect(strikeLoc);
        VersionSafe.playSoundSafe(strikeLoc, 5f, 1f, "ENTITY_LIGHTNING_BOLT_THUNDER", "ENTITY_LIGHTNING_THUNDER");

        // Apply custom damage for the lightning bolt to nearby players at or above the
        // strike point
        // This ensures cover (blocks above) protects the player.
        for (Player p : world.getPlayers()) {
            if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE)
                continue;

            Location pLoc = p.getLocation();
            // Check if player is near the XZ of the strike
            double dx = pLoc.getX() - (x + 0.5);
            double dz = pLoc.getZ() - (z + 0.5);
            double distXZSq = dx * dx + dz * dz;

            if (distXZSq < 16) { // 4 block horizontal radius for customized strikes
                // Player is safe if they are below the highest solid block (under cover)
                if (pLoc.getY() >= highestY - 1.1) {
                    p.damage(STORM_BOLT_DAMAGE);
                }
            }
        }
    }

    @EventHandler
    public void onDragonDamage(EntityDamageEvent event) {
        EnderDragon dragon = null;
        if (event.getEntity() instanceof EnderDragon) {
            dragon = (EnderDragon) event.getEntity();
        } else if (event.getEntity() instanceof ComplexEntityPart) {
            ComplexEntityPart part = (ComplexEntityPart) event.getEntity();
            if (part.getParent() instanceof EnderDragon) {
                dragon = (EnderDragon) part.getParent();
            }
        }

        if (dragon == null)
            return;

        if (dragon.getScoreboardTags().contains("MSC_ShadowClone")) {
            event.setCancelled(true);
            VersionSafe.spawnLargeExplosion(dragon.getLocation(), 1);
            VersionSafe.playSoundSafe(dragon.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 0.5f);
            dragon.remove();
            return;
        }

        if (dragon.getScoreboardTags().contains("MSC_CustomDragon")) {
            long now = System.currentTimeMillis();

            // Leaving health effectively as 2000 HP (no 5k scaling)
            double damageScale = 1.0;
            double scaledDamage = event.getDamage() * damageScale;
            double scaledCap = DAMAGE_CAP * damageScale;

            Attribute maxHealthAttr = VersionSafe.getAttributeSafe("MAX_HEALTH", "GENERIC_MAX_HEALTH");
            if (maxHealthAttr == null || dragon.getAttribute(maxHealthAttr) == null)
                return;
            double maxHealth = dragon.getAttribute(maxHealthAttr).getValue();
            double healthPercent = dragon.getHealth() / maxHealth;

            // Resistance scales from 0% at full health up to 50% max at 0 health
            double dynamicResistanceMultiplier = (1.0 - healthPercent) * 0.50;
            scaledDamage = scaledDamage * (1.0 - dynamicResistanceMultiplier);

            // Re-apply damage cap check in case scaled damage somehow bypasses logic before
            // this
            if (scaledDamage > scaledCap) {
                scaledDamage = scaledCap;
            }
            event.setDamage(scaledDamage);

            double phaseShiftThreshold = 50.0 * damageScale;

            int phaseIndex = (int) (healthPercent * 10);
            if (phaseIndex > 9)
                phaseIndex = 9;

            long lastGlobal = lastGlobalAbilityTimes.getOrDefault(dragon.getUniqueId(), 0L);
            boolean globalReady = (now - lastGlobal >= GLOBAL_COOLDOWN_MS);

            // The Roulette is now the ONLY trigger for special abilities
            if (globalReady && !rouletteActive && !stormActive && !breathFloorActive && !isSlamming(dragon)) {
                lastGlobalAbilityTimes.put(dragon.getUniqueId(), now);
                startAbilityRoulette(dragon);
            }

            if (phaseIndex == 0 && !enragedDragons.contains(dragon.getUniqueId())) {
                enragedDragons.add(dragon.getUniqueId());
                String enrageTitle = ChatColor.DARK_RED + "" + ChatColor.BOLD + "QUEEN ENRAGED";
                String enrageSubtitle = ChatColor.RED + "Final Stand - No Escape!";
                for (Player p : dragon.getWorld().getPlayers()) {
                    p.sendTitle(enrageTitle, enrageSubtitle, 10, 40, 10);
                    VersionSafe.playSoundSafe(p.getLocation(), 1f, 0.5f, "ENTITY_ENDER_DRAGON_GROWL",
                            "ENTITY_ENDERDRAGON_GROWL");
                }
            }

            // Sync update phase colors before applying passives so we don't read stale data
            updateDragonBossBar(dragon);
            BarColor phaseColor = currentPhaseColors.getOrDefault(dragon.getUniqueId(), BarColor.WHITE);

            // Shadow Armor: RED phase reduction
            if (phaseColor == BarColor.RED) {
                event.setDamage(event.getDamage() * (1.0 - SHADOW_ARMOR_REDUCTION));
            }

            // Phase Shift: WHITE phase teleport on heavy damage
            if (phaseColor == BarColor.WHITE && event.getDamage() > phaseShiftThreshold && random.nextDouble() < 0.20) {
                Location loc = dragon.getLocation().add(random.nextInt(31) - 15, random.nextInt(11) - 5,
                        random.nextInt(31) - 15);
                dragon.teleport(loc);
                VersionSafe.spawnBreathParticle(dragon.getLocation(), 40, 1.5, 1.5, 1.5, 0.1);
                VersionSafe.playSoundSafe(dragon.getLocation(), 1f, 1f, "ENTITY_ENDERMAN_TELEPORT",
                        "ENTITY_ENDERMAN_TELEPORT");
            }

            // Abyssal Thorns and other combat triggers
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent ee = (EntityDamageByEntityEvent) event;
                if (ee.getDamager() instanceof Player) {
                    Player attacker = (Player) ee.getDamager();

                    // Static Discharge: BLUE phase lightning counter
                    if (phaseColor == BarColor.BLUE) {
                        attacker.getWorld().strikeLightningEffect(attacker.getLocation()); // VIsual only
                        attacker.damage(STATIC_DISCHARGE_DAMAGE, dragon); // Directly attribute damage
                        org.bukkit.util.Vector kb = attacker.getLocation().toVector()
                                .subtract(dragon.getLocation().toVector()).normalize()
                                .multiply(STATIC_DISCHARGE_KNOCKBACK);
                        attacker.setVelocity(kb);
                        attacker.sendMessage(ChatColor.AQUA + "STATIC DISCHARGE: You were struck by lightning!");
                    }

                    // Solar Flare: YELLOW phase blindness on hit
                    if (phaseColor == BarColor.YELLOW && random.nextDouble() < 0.20) {
                        PotionEffectType blind = VersionSafe.getPotionEffectSafe("BLINDNESS");
                        if (blind != null)
                            VersionSafe.applyPotionEffectSafe(attacker, blind, 40, 0);
                        VersionSafe.spawnExplosionParticle(attacker.getLocation(), 1);
                        VersionSafe.playSoundSafe(attacker.getLocation(), 1f, 2f, "BLOCK_BEACON_ACTIVATE",
                                "BLOCK_BEACON_ACTIVATE");
                        attacker.sendMessage(ChatColor.YELLOW + "SOLAR FLARE: You are blinded by the flash!");
                    }

                    // Abyssal Thorns: PINK or PURPLE phase 15% chance to apply Wither II
                    if ((phaseColor == BarColor.PINK || phaseColor == BarColor.PURPLE) && random.nextDouble() < 0.15) {
                        PotionEffectType wither = VersionSafe.getPotionEffectSafe("WITHER");
                        if (wither != null)
                            VersionSafe.applyPotionEffectSafe(attacker, wither, 60, 1);
                        VersionSafe.spawnBreathParticle(attacker.getLocation(), 10, 0.3, 0.3, 0.3, 0.05);
                        VersionSafe.playSoundSafe(attacker.getLocation(), 0.5f, 0.5f, "ENTITY_ELDER_GUARDIAN_CURSE",
                                "ENTITY_ELDER_GUARDIAN_CURSE");
                    }
                }
            }

            final EnderDragon finalDragon = dragon;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (finalDragon.isValid()) {
                    updateDragonBossBar(finalDragon);
                }
            });
        }
    }

    @EventHandler
    public void onDragonAttack(EntityDamageByEntityEvent event) {
        EnderDragon dragon = null;
        if (event.getDamager() instanceof EnderDragon) {
            dragon = (EnderDragon) event.getDamager();
        } else if (event.getDamager() instanceof ComplexEntityPart) {
            ComplexEntityPart part = (ComplexEntityPart) event.getDamager();
            if (part.getParent() instanceof EnderDragon) {
                dragon = (EnderDragon) part.getParent();
            }
        }

        if (dragon == null || !dragon.getScoreboardTags().contains("MSC_CustomDragon"))
            return;

        BarColor phaseColor = currentPhaseColors.getOrDefault(dragon.getUniqueId(), BarColor.WHITE);

        // Siphon Life: GREEN phase healing on hit
        if (phaseColor == BarColor.GREEN) {
            Attribute maxHealthAttr = VersionSafe.getAttributeSafe("MAX_HEALTH", "GENERIC_MAX_HEALTH");
            if (maxHealthAttr != null && dragon.getAttribute(maxHealthAttr) != null) {
                double maxVal = dragon.getAttribute(maxHealthAttr).getValue();
                double healAmount = maxVal * SIPHON_LIFE_PERCENT;
                dragon.setHealth(Math.min(dragon.getHealth() + healAmount, maxVal));
                VersionSafe.spawnBreathParticle(dragon.getLocation(), 20, 1.0, 1.0, 1.0, 0.05);
                VersionSafe.playSoundSafe(dragon.getLocation(), 0.8f, 1.2f, "ENTITY_PLAYER_LEVELUP",
                        "ENTITY_EXPERIENCE_ORB_PICKUP");
            }
        }
    }

    @EventHandler
    public void onDragonRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof EnderDragon))
            return;

        EnderDragon dragon = (EnderDragon) event.getEntity();
        if (dragon.getScoreboardTags().contains("MSC_CustomDragon")) {
            if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.ENDER_CRYSTAL) {
                event.setAmount(event.getAmount() * REGEN_MULTIPLIER);
            }
        }
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon))
            return;

        EnderDragon dragon = (EnderDragon) event.getEntity();
        if (dragon.getScoreboardTags().contains("MSC_CustomDragon")) {
            event.getDrops().add(new ItemStack(Material.DRAGON_BREATH, 5));
            event.getDrops().add(new ItemStack(Material.ELYTRA, 1));

            slammingDragons.remove(dragon.getUniqueId());
            enragedDragons.remove(dragon.getUniqueId());
            currentPhaseColors.remove(dragon.getUniqueId());
            stormActive = false;

            String vicTitle = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "QUEEN DEFEATED";
            String vicSubtitle = ChatColor.WHITE + "The End is safe... for now.";
            for (Player p : event.getEntity().getWorld().getPlayers()) {
                p.sendTitle(vicTitle, vicSubtitle, 20, 100, 20);
                VersionSafe.playSoundSafe(p.getLocation(), 1f, 1f, "UI_TOAST_CHALLENGE_COMPLETE",
                        "UI_BUTTON_CLICK");
            }
            spawnRewardChests(dragon.getWorld());
        }
    }


    private void fillChestRandomly(Chest chest, String theme) {
        chest.getInventory().clear();
        int slots = chest.getInventory().getSize();
        
        switch (theme) {
            case "COMBAT":
                addRandomItems(chest, getCombatPool(), 3, 6);
                break;
            case "UTILITY":
                addRandomItems(chest, getUtilityPool(), 3, 6);
                break;
            case "MATERIAL":
                addRandomItems(chest, getMaterialPool(), 2, 5);
                break;
        }
    }

    private void addRandomItems(Chest chest, List<ItemStack> pool, int min, int max) {
        int count = min + random.nextInt(max - min + 1);
        for (int i = 0; i < count; i++) {
            ItemStack item = pool.get(random.nextInt(pool.size())).clone();
            // Randomize stack size for certain items
            if (item.getMaxStackSize() > 1 && item.getAmount() == 1) {
                item.setAmount(1 + random.nextInt(Math.min(item.getMaxStackSize(), 8)));
            }
            
            int slot = random.nextInt(chest.getInventory().getSize());
            // Try to find an empty slot
            for (int attempt = 0; attempt < 10 && chest.getInventory().getItem(slot) != null; attempt++) {
                slot = random.nextInt(chest.getInventory().getSize());
            }
            chest.getInventory().setItem(slot, item);
        }
    }

    private List<ItemStack> getCombatPool() {
        return java.util.Arrays.asList(
            new ItemStack(Material.NETHERITE_SWORD),
            new ItemStack(Material.NETHERITE_AXE),
            new ItemStack(Material.ENCHANTED_GOLDEN_APPLE),
            new ItemStack(Material.GOLDEN_APPLE, 4),
            new ItemStack(Material.TOTEM_OF_UNDYING),
            new ItemStack(Material.DIAMOND_CHESTPLATE),
            new ItemStack(Material.DIAMOND_LEGGINGS),
            new ItemStack(Material.END_CRYSTAL, 2),
            new ItemStack(Material.OBSIDIAN, 16)
        );
    }

    private List<ItemStack> getUtilityPool() {
        return java.util.Arrays.asList(
            new ItemStack(Material.ELYTRA),
            new ItemStack(Material.FIREWORK_ROCKET, 32),
            new ItemStack(Material.ENDER_CHEST, 4),
            new ItemStack(Material.SHULKER_BOX),
            new ItemStack(Material.EXPERIENCE_BOTTLE, 16),
            new ItemStack(Material.POTION), // Will be water bottle, but could be improved
            new ItemStack(Material.GOLDEN_CARROT, 32),
            new ItemStack(Material.CHORUS_FRUIT, 16)
        );
    }

    private List<ItemStack> getMaterialPool() {
        return java.util.Arrays.asList(
            new ItemStack(Material.NETHERITE_INGOT),
            new ItemStack(Material.NETHERITE_SCRAP, 2),
            new ItemStack(Material.DIAMOND, 8),
            new ItemStack(Material.EMERALD_BLOCK, 2),
            new ItemStack(Material.DRAGON_BREATH, 16),
            new ItemStack(Material.ANCIENT_DEBRIS, 2),
            new ItemStack(Material.GOLD_BLOCK, 4),
            new ItemStack(Material.DRAGON_EGG)
        );
    }

    public void spawnRewardChests(World world) {
        Location base = new Location(world, 0, 62, 7); // Behind the exit portal
        Location[] locs = {
                base.clone().add(2, 0, 0),
                base.clone().add(0, 0, 0),
                base.clone().add(-2, 0, 0)
        };

        String[] themes = { "§6§lCOMBAT REWARD", "§b§lUTILITY REWARD", "§d§lMATERIAL REWARD" };
        String[] internalThemes = { "COMBAT", "UTILITY", "MATERIAL" };

        for (int i = 0; i < 3; i++) {
            Location l = locs[i];
            l.getBlock().setType(Material.CHEST);
            if (l.getBlock().getState() instanceof Chest) {
                Chest chest = (Chest) l.getBlock().getState();
                chest.getPersistentDataContainer().set(new NamespacedKey(plugin, "MSC_RewardChest"),
                        PersistentDataType.BYTE, (byte) 1);
                chest.setCustomName(themes[i]);
                
                // Fill with randomized loot
                fillChestRandomly(chest, internalThemes[i]);
                
                chest.update();
            }
            VersionSafe.spawnPortalParticle(l.clone().add(0.5, 0.5, 0.5), 50, 0.5, 0.5, 0.5, 0.1);
        }
    }

    @EventHandler
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.CHEST)
            return;

        Block block = event.getClickedBlock();
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            NamespacedKey key = new NamespacedKey(plugin, "MSC_RewardChest");
            if (chest.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
                // Remove the other chests
                World world = block.getWorld();
                Location base = new Location(world, 0, 62, 7);
                Location[] locs = {
                        base.clone().add(2, 0, 0),
                        base.clone().add(0, 0, 0),
                        base.clone().add(-2, 0, 0)
                };

                for (Location l : locs) {
                    if (l.getBlockX() == block.getX() && l.getBlockZ() == block.getZ())
                        continue;

                    if (l.getBlock().getType() == Material.CHEST) {
                        l.getBlock().setType(Material.AIR);
                        VersionSafe.spawnLargeSmokeParticle(l.clone().add(0.5, 0.5, 0.5), 30, 0.3,
                                0.3, 0.3, 0.05);
                        VersionSafe.playSoundSafe(l, 1f, 0.5f, "ENTITY_ENDERMAN_TELEPORT",
                                "ENTITY_ENDERMEN_TELEPORT");
                    }
                }

                // Remove the tag so this doesn't trigger again for the same chest
                chest.getPersistentDataContainer().remove(key);
                chest.update();

                event.getPlayer()
                        .sendMessage("§d§l[MSC] §fYou have chosen your reward! The other options have vanished.");
            }
        }
    }

    public void performVoidBeam(EnderDragon dragon) {
        dragon.setPhase(EnderDragon.Phase.HOVER);
        VersionSafe.playSoundSafe(dragon.getLocation(), 5f, 0.5f, "ENTITY_ENDER_DRAGON_GROWL",
                "ENTITY_ENDERDRAGON_GROWL");

        String title = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "VOID BEAM";
        String subtitle = ChatColor.LIGHT_PURPLE + "THE VOID IS FOCUSING...";
        for (Player p : dragon.getWorld().getPlayers()) {
            p.sendTitle(title, subtitle, 10, 40, 10);
        }

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks += 2;
                if (ticks > 100 || !dragon.isValid() || dragon.isDead()) {
                    dragon.setPhase(EnderDragon.Phase.FLY_TO_PORTAL);
                    this.cancel();
                    return;
                }

                dragon.setVelocity(new Vector(0, 0.05, 0));

                Player target = null;
                double minDistSq = 6400; // 80 blocks
                for (Player p : dragon.getWorld().getPlayers()) {
                    if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                        double d2 = p.getLocation().distanceSquared(dragon.getLocation());
                        if (d2 < minDistSq) {
                            minDistSq = d2;
                            target = p;
                        }
                    }
                }

                if (target != null) {
                    Location start = dragon.getEyeLocation();
                    Location end = target.getEyeLocation();
                    Vector direction = end.toVector().subtract(start.toVector());
                    double distance = direction.length();
                    direction.normalize();

                    for (double d = 0; d < Math.min(distance, 60); d += 0.5) {
                        Location particleLoc = start.clone().add(direction.clone().multiply(d));
                        VersionSafe.spawnBreathParticle(particleLoc, 1, 0.1, 0.1, 0.1, 0.01);
                        if (ticks % 10 == 0) {
                            VersionSafe.spawnSmokeParticle(particleLoc, 1, 0.05, 0.05, 0.05, 0.01);
                        }
                    }

                    if (ticks % 10 == 0) {
                        for (Player p : dragon.getWorld().getPlayers()) {
                            if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
                                double distToBeam = getDistanceToLine(p.getEyeLocation(), start, end);
                                if (distToBeam < 2.5) {
                                    p.damage(4.0, dragon);
                                    p.setFireTicks(20);
                                }
                            }
                        }
                        VersionSafe.playSoundSafe(end, 1f, 1.5f, "ENTITY_GLOW_SQUID_SQUISH",
                                "ENTITY_ENDER_DRAGON_FLAP");
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private double getDistanceToLine(Location p, Location start, Location end) {
        Vector v = end.toVector().subtract(start.toVector());
        Vector w = p.toVector().subtract(start.toVector());
        double c1 = w.dot(v);
        if (c1 <= 0)
            return p.distance(start);
        double c2 = v.dot(v);
        if (c2 <= c1)
            return p.distance(end);
        double b = c1 / c2;
        Vector pb = start.toVector().add(v.multiply(b));
        return p.toVector().distance(pb);
    }
}
