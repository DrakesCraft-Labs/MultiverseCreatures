package com.Chagui68.entities.boss;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.boss.DioMoves.Attack;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.LiveStage;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Pose;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.entities.boss.fx.Timeline;
import com.Chagui68.utils.DisplaySuit;
import com.Chagui68.utils.MscBossBar;
import com.Chagui68.utils.MscEntityUtils;
import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * DIO — from JoJo's Bizarre Adventure — with his Stand, The World, floating at his shoulder.
 *
 * <p>DIO is a visible armor stand dressed in his yellow jacket, with his own face; The World is a
 * second, larger stand in gold armour. Both are posed every tick from {@link DioMoves}, and every
 * attack is a {@link Timeline} played one at a time: ZA WARUDO (time stops, knives wait in the air
 * and fly when it moves again), the MUDA MUDA barrage, ROAD ROLLER DA, a fan of knives, the Space
 * Ripper Stingy Eyes and The World's heavy punch. Health is virtual, as on the other dressed bosses.
 */
public class DioBoss implements Listener {

    public static final String TAG = "MSC_DioBoss";
    /** The World, DIO's Stand. */
    public static final String STAND_TAG = "MSC_DioStand";
    static final String OWNER_PREFIX = "MSC_DioOwner_";
    /** Knives, the road roller and the menacing letters: swept on restart like every attack prop. */
    private static final String PROP_TAG = LiveStage.PROP_TAG;

    private static final double DIO_SCALE = 1.15;
    private static final double WORLD_SCALE = 1.35;
    /** Height of an armour stand at scale 1: The World's head model is sized to the stand it rides. */
    private static final double ARMOR_STAND_HEIGHT = 1.975;
    /** Tag of the heads The World is drawn with; "Boss" keeps them out of a player's time stop. */
    private static final String WORLD_PART_TAG = "MSC_DioBoss_TheWorldPart";
    private static final String DIO_PART_TAG = "MSC_DioBoss_DioPart";
    /**
     * Share of the way each body moves towards its pose every tick. The World is quicker, so the
     * fists of its barrage, which alternate every tick, still land.
     */
    private static final float DIO_EASE = 0.4f;
    private static final float WORLD_EASE = 0.65f;
    private static final Color GOLD = Color.fromRGB(0xFFD23F);
    private static final Color GREEN = Color.fromRGB(0x3CCB5A);
    private static final Color FROZEN = Color.fromRGB(0x6E6A86);
    private static final Color INVERTED = Color.fromRGB(0x2B1E5C);
    private static final Color STINGY = Color.fromRGB(0xD8F4FF);

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, DioInstance> active = new HashMap<>();
    private BukkitTask ticker;
    private int sweepClock;

    private double health;
    private double aggroRange;
    private double moveSpeed;
    private double maxDamagePerHit;
    private double punchDamage;
    private double barrageDamage;
    private double barrageFinisherDamage;
    private double knifeDamage;
    private double roadRollerDamage;
    private double eyeBeamDamage;
    private int timeStopTicks;
    private double timeStopRadius;
    private int timeStopCooldownTicks;
    private int attackCooldownTicks;

    public DioBoss(MultiverseCreatures plugin) {
        this.plugin = plugin;
        reloadConfig();
        if (!plugin.isEnabled("entities.dio-brando")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (stand.getScoreboardTags().contains(TAG)) adopt(stand);
            }
        }
        startTicker();
    }

    public void reloadConfig() {
        var config = plugin.getConfig();
        health = config.getDouble("entities.dio-brando.health", 900.0);
        aggroRange = config.getDouble("entities.dio-brando.aggro-range", 32.0);
        moveSpeed = config.getDouble("entities.dio-brando.move-speed", 0.26);
        maxDamagePerHit = config.getDouble("entities.dio-brando.max-damage-per-hit", 100.0);
        punchDamage = config.getDouble("entities.dio-brando.punch-damage", 12.0);
        barrageDamage = config.getDouble("entities.dio-brando.barrage-damage", 2.5);
        barrageFinisherDamage = config.getDouble("entities.dio-brando.barrage-finisher-damage", 14.0);
        knifeDamage = config.getDouble("entities.dio-brando.knife-damage", 5.0);
        roadRollerDamage = config.getDouble("entities.dio-brando.road-roller-damage", 26.0);
        eyeBeamDamage = config.getDouble("entities.dio-brando.eye-beam-damage", 5.0);
        timeStopTicks = Math.max(20, config.getInt("entities.dio-brando.time-stop-ticks", 100));
        timeStopRadius = config.getDouble("entities.dio-brando.time-stop-radius", 40.0);
        timeStopCooldownTicks = Math.max(100, config.getInt("entities.dio-brando.time-stop-cooldown-ticks", 700));
        attackCooldownTicks = Math.max(10, config.getInt("entities.dio-brando.attack-cooldown-ticks", 45));
    }

    private void startTicker() {
        if (ticker != null) ticker.cancel();
        ticker = new BukkitRunnable() {
            @Override
            public void run() {
                for (DioInstance inst : new ArrayList<>(active.values())) tick(inst);
                if (++sweepClock % 40 == 0) sweepStands();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Stops the tick loop and lets every frozen player, mob and projectile go. */
    public void stopTasks() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        for (DioInstance inst : active.values()) {
            resumeTime(inst);
            removeProps(inst);
        }
    }

    public boolean isBossActiveIn(World world) {
        for (DioInstance inst : active.values()) {
            if (inst.stand.isValid() && inst.stand.getWorld().equals(world)) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ spawning

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.dio-brando")) return false;
        World world = location.getWorld();
        if (world == null) return false;
        Location at = location.clone();
        BossArena.settle(at);
        ArmorStand stand = world.spawn(at, ArmorStand.class, s -> {
            s.setVisible(false);
            s.setArms(true);
            s.setBasePlate(false);
            s.setGravity(false);
            s.setInvulnerable(false);
            s.setCanPickupItems(false);
            s.setPersistent(true);
            s.customName(MscText.title(NamedTextColor.GOLD, "DIO"));
            s.setCustomNameVisible(true);
            s.addScoreboardTag(TAG);
            AttributeInstance scale = s.getAttribute(Attribute.SCALE);
            if (scale != null) scale.setBaseValue(DIO_SCALE);
            dressDio(s);
        });
        MscEntityUtils.initVirtualHealth(stand, health);
        DioInstance inst = adopt(stand);
        Fx fx = LiveStage.fxIn(world);
        Vector feet = at.toVector();
        fx.sound(feet, Sfx.WITHER_SPAWN, 2f, 0.8f);
        fx.sound(feet, Sfx.BELL_RESONATE, 2f, 0.5f);
        fx.burst(feet.clone().add(new Vector(0, 1.2, 0)), Particle.END_ROD, 60, 0.3);
        fx.ring(feet.clone().add(new Vector(0, 0.1, 0)), 3, 0.4, 0, fx.dust(GOLD, 1.6f));
        say(inst, "Kono DIO da!", NamedTextColor.GOLD);
        return true;
    }

    private DioInstance adopt(ArmorStand stand) {
        DioInstance existing = active.get(stand.getUniqueId());
        if (existing != null) return existing;
        if (!stand.getPersistentDataContainer().has(MscEntityUtils.KEY_VIRTUAL_MAX_HEALTH,
                org.bukkit.persistence.PersistentDataType.DOUBLE)) {
            MscEntityUtils.initVirtualHealth(stand, health);
        }
        // A DIO adopted after a restart takes the body this release draws him with.
        dressDio(stand);
        DioInstance inst = new DioInstance(stand);
        for (ArmorStand candidate : stand.getWorld().getEntitiesByClass(ArmorStand.class)) {
            if (candidate.getScoreboardTags().contains(STAND_TAG)
                    && stand.getUniqueId().equals(DisplaySuit.ownerOf(candidate, OWNER_PREFIX))) {
                inst.world = candidate;
                break;
            }
        }
        if (inst.world == null) inst.world = spawnTheWorld(stand);
        else dressTheWorld(inst.world);
        inst.bossBar = MscBossBar.create("§6§lDIO §7— §eThe World", BarColor.YELLOW, BarStyle.SEGMENTED_10,
                BarFlag.DARKEN_SKY);
        MscBossBar.showInWorld(inst.bossBar, stand.getWorld());
        active.put(stand.getUniqueId(), inst);
        return inst;
    }

    private ArmorStand spawnTheWorld(ArmorStand dio) {
        Location at = dio.getLocation().clone().add(0, 0.6, 0);
        return dio.getWorld().spawn(at, ArmorStand.class, s -> {
            s.setVisible(false);
            s.setArms(true);
            s.setBasePlate(false);
            s.setGravity(false);
            s.setInvulnerable(true);
            s.setCanPickupItems(false);
            s.setPersistent(true);
            s.setCollidable(false);
            s.customName(MscText.title(NamedTextColor.YELLOW, "The World"));
            s.setCustomNameVisible(false);
            s.addScoreboardTag(STAND_TAG);
            s.addScoreboardTag(OWNER_PREFIX + dio.getUniqueId().toString().replace("-", ""));
            AttributeInstance scale = s.getAttribute(Attribute.SCALE);
            if (scale != null) scale.setBaseValue(WORLD_SCALE);
            dressTheWorld(s);
        });
    }

    /** DIO is drawn with his own heads: his armour stand is only the hitbox they ride. */
    private void dressDio(ArmorStand stand) {
        hide(stand);
    }

    /** The World is drawn with its own heads: its armour stand is only the anchor they ride. */
    private void dressTheWorld(ArmorStand stand) {
        hide(stand);
    }

    private static void hide(ArmorStand stand) {
        stand.setVisible(false);
        var eq = stand.getEquipment();
        if (eq != null) eq.clear();
        lock(stand);
    }

    private static void lock(ArmorStand stand) {
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)) {
            stand.addEquipmentLock(slot, ArmorStand.LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(slot, ArmorStand.LockType.ADDING_OR_CHANGING);
        }
    }

    // ------------------------------------------------------------------ the tick

    private void tick(DioInstance inst) {
        ArmorStand stand = inst.stand;
        if (stand.isDead() || !stand.isValid()) {
            cleanup(inst);
            return;
        }
        if (!stand.getWorld().isChunkLoaded(stand.getLocation().getBlockX() >> 4, stand.getLocation().getBlockZ() >> 4)) return;
        inst.tick++;
        if (inst.world == null || !inst.world.isValid()) inst.world = spawnTheWorld(stand);

        Player target = findTarget(stand);
        Location loc = stand.getLocation();
        double dist = target == null ? Double.MAX_VALUE
                : Math.hypot(target.getLocation().getX() - loc.getX(), target.getLocation().getZ() - loc.getZ());
        updatePhase(inst);

        if (inst.attack != null) {
            boolean running = inst.attack.tick();
            // An attack turns DIO to face his target (and moves him); keep that, instead of putting
            // back where he stood before the attack ran, which turned him round and back every tick.
            loc = stand.getLocation();
            if (!running) {
                inst.attack = null;
                inst.attackName = null;
                inst.attackCooldown = attackCooldownTicks / inst.phase + random.nextInt(20);
            }
        } else {
            if (inst.attackCooldown > 0) inst.attackCooldown--;
            if (inst.timeStopCooldown > 0) inst.timeStopCooldown--;
            if (target != null && dist <= aggroRange) {
                Vector toTarget = target.getLocation().toVector().subtract(loc.toVector()).setY(0);
                if (toTarget.lengthSquared() > 0.01) loc.setDirection(toTarget);
                if (inst.attackCooldown <= 0) {
                    Attack pick = pickAttack(inst, dist);
                    if (pick != null) start(inst, pick, target);
                }
                if (inst.attack == null && dist > 3.2) {
                    inst.walkPhase += 0.28;
                    BossArena.walk(loc, DioMoves.flat(toTarget).multiply(Math.min(moveSpeed * (inst.phase == 2 ? 1.25 : 1), dist)), true);
                    inst.dioPose = DioMoves.walk(inst.walkPhase);
                } else if (inst.attack == null) {
                    inst.dioPose = DioMoves.DIO_IDLE;
                }
                greet(inst, stand.getWorld());
            } else {
                inst.dioPose = DioMoves.DIO_IDLE;
            }
            if (inst.attack == null) menacing(inst, stand.getWorld());
        }

        if (inst.timeStopped) holdTime(inst);
        tickProps(inst);

        if (!inst.airborne) BossArena.settle(loc);
        if (inst.airborne && inst.flightTo != null) {
            loc.setX(inst.flightTo.getX());
            loc.setY(inst.flightTo.getY());
            loc.setZ(inst.flightTo.getZ());
        }
        stand.teleport(loc);
        pose(stand, inst.dioPose);
        bodyOfDio(inst, loc);

        if (!inst.worldControlled) {
            Vector forward = DioMoves.flat(loc.getDirection());
            inst.worldAt = DioMoves.worldIdle(loc.toVector(), forward, DIO_SCALE, inst.tick);
            inst.worldYaw = loc.getYaw();
            inst.worldPose = DioMoves.WORLD_IDLE;
        }
        Location worldLoc = inst.worldAt.toLocation(stand.getWorld(), inst.worldYaw, 0);
        inst.world.teleport(worldLoc);
        pose(inst.world, inst.worldPose);
        bodyOfTheWorld(inst, worldLoc);
        aura(inst);

        double current = MscEntityUtils.getVirtualHealth(stand);
        double max = MscEntityUtils.getVirtualMaxHealth(stand);
        inst.bossBar.setProgress(MscEntityUtils.calculateVirtualProgress(current, max));
        if (inst.tick % 20 == 0) MscBossBar.showInWorld(inst.bossBar, stand.getWorld());
    }

    private Player findTarget(ArmorStand stand) {
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        for (Player p : stand.getWorld().getPlayers()) {
            if (!fighting(p)) continue;
            double d = p.getLocation().distanceSquared(stand.getLocation());
            if (d <= aggroRange * aggroRange && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    private static boolean fighting(Player p) {
        return p.isValid() && !p.isDead() && p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR;
    }

    private List<Player> playersNear(Location center, double radius) {
        List<Player> out = new ArrayList<>();
        for (Player p : center.getWorld().getPlayers()) {
            if (fighting(p) && p.getLocation().distanceSquared(center) <= radius * radius) out.add(p);
        }
        return out;
    }

    private void updatePhase(DioInstance inst) {
        if (inst.phase == 2) return;
        double ratio = MscEntityUtils.getVirtualHealth(inst.stand) / Math.max(1, MscEntityUtils.getVirtualMaxHealth(inst.stand));
        if (ratio > 0.5) return;
        inst.phase = 2;
        inst.bossBar.setColor(BarColor.RED);
        Fx fx = LiveStage.fxIn(inst.stand.getWorld());
        Vector feet = inst.stand.getLocation().toVector();
        fx.sound(feet, Sfx.WITHER_AMBIENT, 3f, 0.6f);
        fx.sound(feet, Sfx.DRAGON_GROWL, 2f, 1.4f);
        fx.burst(feet.clone().add(new Vector(0, 1.5, 0)), Particle.DAMAGE_INDICATOR, 40, 0.4);
        for (Player p : playersNear(inst.stand.getLocation(), 60)) {
            p.showTitle(Title.title(Component.text("WRYYYYYYYY!", NamedTextColor.GOLD, TextDecoration.BOLD),
                    Component.text("DIO is enraged", NamedTextColor.RED), times(5, 40, 15)));
        }
    }

    /** Which attack fits the distance, never the same one twice in a row. */
    private Attack pickAttack(DioInstance inst, double dist) {
        if (inst.timeStopCooldown <= 0 && dist <= timeStopRadius * 0.6) return Attack.TIME_STOP;
        List<Attack> options = new ArrayList<>();
        if (dist <= 3.8) {
            options.addAll(List.of(Attack.PUNCH, Attack.PUNCH, Attack.MUDA_BARRAGE, Attack.MUDA_BARRAGE, Attack.EYE_BEAMS));
        } else if (dist <= 12) {
            options.addAll(List.of(Attack.MUDA_BARRAGE, Attack.KNIFE_FAN, Attack.KNIFE_FAN, Attack.EYE_BEAMS, Attack.ROAD_ROLLER));
        } else {
            options.addAll(List.of(Attack.ROAD_ROLLER, Attack.ROAD_ROLLER, Attack.KNIFE_FAN, Attack.EYE_BEAMS));
        }
        options.removeIf(a -> a.name().equals(inst.lastAttack));
        return options.isEmpty() ? null : options.get(random.nextInt(options.size()));
    }

    private void start(DioInstance inst, Attack attack, Player target) {
        Timeline t = switch (attack) {
            case TIME_STOP -> timeStop(inst);
            case MUDA_BARRAGE -> mudaBarrage(inst, target);
            case ROAD_ROLLER -> roadRoller(inst, target);
            case KNIFE_FAN -> knifeFan(inst, target);
            case EYE_BEAMS -> eyeBeams(inst, target);
            case PUNCH -> punch(inst, target);
        };
        t.onFinish(() -> {
            inst.worldControlled = false;
            inst.airborne = false;
            inst.flightTo = null;
        });
        inst.attack = t;
        inst.attackName = attack.name();
        inst.lastAttack = attack.name();
    }

    // ------------------------------------------------------------------ ZA WARUDO

    private Timeline timeStop(DioInstance inst) {
        World world = inst.stand.getWorld();
        Fx fx = LiveStage.fxIn(world);
        int cast = 20;
        int stop = timeStopTicks + (inst.phase == 2 ? 40 : 0);
        int resume = cast + stop;
        inst.timeStopCooldown = timeStopCooldownTicks;
        Timeline t = new Timeline();
        t.at(0, () -> {
            inst.dioPose = DioMoves.DIO_SKY;
            inst.worldControlled = true;
            for (Player p : playersNear(inst.stand.getLocation(), timeStopRadius + 10)) {
                p.showTitle(Title.title(Component.text("ZA WARUDO!", NamedTextColor.GOLD, TextDecoration.BOLD),
                        Component.text("「ザ・ワールド」", NamedTextColor.YELLOW), times(3, 30, 8)));
            }
            say(inst, "ZA WARUDO!", NamedTextColor.GOLD);
            fx.sound(inst.stand.getLocation().toVector(), Sfx.BEACON_DEACTIVATE, 3f, 0.5f);
        });
        t.span(0, cast, (tick, p) -> {
            Vector feet = inst.stand.getLocation().toVector();
            Vector forward = DioMoves.flat(inst.stand.getLocation().getDirection());
            // The World rises above its master with its arms spread.
            inst.worldAt = feet.clone().subtract(forward.clone().multiply(0.6)).add(new Vector(0, 1.2 + 0.8 * p, 0));
            inst.worldYaw = inst.stand.getLocation().getYaw();
            inst.worldPose = DioMoves.WORLD_IDLE.lerp(DioMoves.WORLD_SPREAD, Math.min(1, p * 1.4));
            Vector chest = inst.worldAt.clone().add(new Vector(0, 1.4, 0));
            fx.gather(chest, 4 - 2 * p, 4, GOLD, 8);
            if (tick % 4 == 0) fx.sound(chest, Sfx.NOTE_HAT, 1.5f, 0.5f + (float) p);
        });
        // The shell of stopped time sweeps out across the arena.
        t.span(10, cast + 4, (tick, p) -> {
            Vector center = inst.worldAt.clone().add(new Vector(0, 1.4, 0));
            double r = timeStopRadius * p;
            fx.draw(Shapes.sphere(center, r, (int) Math.min(220, 30 + r * 6)), fx.dust(p < 0.5 ? INVERTED : FROZEN, 2.2f));
            if (tick == 0) {
                fx.sound(center, Sfx.WARDEN_SONIC_BOOM, 3f, 0.5f);
                fx.sound(center, Sfx.BELL_RESONATE, 3f, 0.5f);
                fx.flash(center, Color.WHITE);
            }
        });
        t.at(cast, () -> {
            freezeTime(inst);
            inst.dioPose = DioMoves.DIO_KNIVES_DRAWN;
            for (Player p : inst.frozen.keySet().stream().map(Bukkit::getPlayer).filter(java.util.Objects::nonNull).toList()) {
                p.showTitle(Title.title(Component.empty(),
                        Component.text("⏸ Time has stopped", NamedTextColor.GRAY), times(0, 40, 10)));
            }
        });
        // One second at a time, as he counts it.
        for (int s = 1; s * 20 < stop; s++) {
            int second = s;
            t.at(cast + s * 20, () -> {
                for (UUID id : inst.frozen.keySet()) {
                    Player p = Bukkit.getPlayer(id);
                    if (p != null) p.sendActionBar(Component.text("⏱ " + second + "-byō keika...", NamedTextColor.YELLOW));
                }
                fx.sound(inst.stand.getLocation().toVector(), Sfx.NOTE_HAT, 2f, 0.6f);
            });
        }
        // Knives thrown into stopped time: they leave his hand and stop a step later.
        int perPlayer = inst.phase == 2 ? 8 : 6;
        t.at(cast + 4, () -> {
            int i = 0;
            for (UUID id : inst.frozen.keySet()) {
                Player p = Bukkit.getPlayer(id);
                if (p == null) continue;
                Vector chest = p.getLocation().toVector().add(new Vector(0, 1.1, 0));
                List<Vector> ring = DioMoves.knifeRing(chest, perPlayer, 2.4, random.nextDouble() * Math.PI);
                for (Vector spot : ring) {
                    int delay = 2 + i * 3;
                    inst.pendingKnives.add(new PendingKnife(cast + 4 + delay, spot, chest.clone().subtract(spot).normalize()));
                    i++;
                }
            }
        });
        t.span(cast + 4, resume, (tick, p) -> {
            int now = cast + 4 + tick;
            boolean drawn = (tick / 6) % 2 == 0;
            inst.dioPose = drawn ? DioMoves.DIO_KNIVES_DRAWN : DioMoves.DIO_KNIVES_THROWN;
            for (PendingKnife knife : new ArrayList<>(inst.pendingKnives)) {
                if (knife.at() != now) continue;
                inst.pendingKnives.remove(knife);
                Vector hand = hand(inst.stand);
                Knife k = spawnKnife(world, hand, knife.spot().clone().subtract(hand));
                k.stopAt = knife.spot();
                k.aim = knife.aim();
                k.speed = 1.6;
                inst.knives.add(k);
                fx.sound(hand, Sfx.TRIDENT_THROW, 1.2f, 1.6f);
            }
        });
        t.at(resume - 20, () -> say(inst, "Soshite... toki wa ugokidasu.", NamedTextColor.YELLOW));
        t.at(resume, () -> {
            resumeTime(inst);
            fx.sound(inst.stand.getLocation().toVector(), Sfx.BEACON_ACTIVATE, 3f, 0.6f);
            for (Knife k : inst.knives) {
                if (k.aim == null) continue;
                k.stopAt = null;
                k.dir = k.aim.clone();
                k.speed = 1.1;
                k.flying = 14;
                k.armed = true;
            }
        });
        t.span(resume, resume + 18, (tick, p) -> {
            inst.dioPose = DioMoves.DIO_IDLE;
            inst.worldPose = DioMoves.WORLD_IDLE;
        });
        t.onFinish(() -> {
            resumeTime(inst);
            inst.pendingKnives.clear();
        });
        return t;
    }

    /** Stops time for every player, mob and projectile within the radius. */
    private void freezeTime(DioInstance inst) {
        Location center = inst.stand.getLocation();
        inst.timeStopped = true;
        for (Player p : playersNear(center, timeStopRadius)) {
            inst.frozen.put(p.getUniqueId(), p.getLocation().clone());
            p.setVelocity(new Vector());
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, timeStopTicks + 60, 6, false, false, false));
        }
        for (Entity e : center.getWorld().getNearbyEntities(center, timeStopRadius, timeStopRadius, timeStopRadius)) {
            if (e instanceof Player || e == inst.stand || e == inst.world) continue;
            if (e instanceof LivingEntity living && !(e instanceof ArmorStand) && living.hasAI()) {
                living.setAI(false);
                inst.frozenMobs.add(living.getUniqueId());
            } else if (e instanceof Projectile projectile && projectile.hasGravity()) {
                inst.frozenProjectiles.put(projectile.getUniqueId(), projectile.getVelocity());
                projectile.setGravity(false);
                projectile.setVelocity(new Vector());
            }
        }
    }

    /** Holds every frozen player where time caught them; they can still look around. */
    private void holdTime(DioInstance inst) {
        Fx fx = LiveStage.fxIn(inst.stand.getWorld());
        for (Map.Entry<UUID, Location> entry : inst.frozen.entrySet()) {
            Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null || !p.isValid()) continue;
            Location hold = entry.getValue().clone();
            hold.setYaw(p.getLocation().getYaw());
            hold.setPitch(p.getLocation().getPitch());
            if (p.getLocation().distanceSquared(hold) > 0.0025) p.teleport(hold);
            p.setVelocity(new Vector());
            p.setFallDistance(0);
            if (inst.tick % 4 == 0) fx.cloud(Particle.WHITE_ASH, hold.toVector().add(new Vector(0, 1, 0)), 3, 0.6, 0);
        }
        for (UUID id : inst.frozenProjectiles.keySet()) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) e.setVelocity(new Vector());
        }
    }

    /** Lets time move again: players, mobs and projectiles exactly as they were. */
    private void resumeTime(DioInstance inst) {
        if (!inst.timeStopped) return;
        inst.timeStopped = false;
        for (UUID id : inst.frozen.keySet()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) p.removePotionEffect(PotionEffectType.SLOWNESS);
        }
        inst.frozen.clear();
        for (UUID id : inst.frozenMobs) {
            if (Bukkit.getEntity(id) instanceof LivingEntity living && living.isValid()) living.setAI(true);
        }
        inst.frozenMobs.clear();
        for (Map.Entry<UUID, Vector> entry : inst.frozenProjectiles.entrySet()) {
            Entity e = Bukkit.getEntity(entry.getKey());
            if (e != null) {
                e.setGravity(true);
                e.setVelocity(entry.getValue());
            }
        }
        inst.frozenProjectiles.clear();
    }

    private boolean isFrozen(Player player) {
        for (DioInstance inst : active.values()) {
            if (inst.frozen.containsKey(player.getUniqueId())) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ MUDA MUDA

    private Timeline mudaBarrage(DioInstance inst, Player target) {
        World world = inst.stand.getWorld();
        Fx fx = LiveStage.fxIn(world);
        int rush = 8;
        int barrage = 44;
        Vector[] spot = new Vector[1];
        Vector[] facing = new Vector[1];
        Timeline t = new Timeline();
        t.at(0, () -> {
            inst.worldControlled = true;
            inst.dioPose = DioMoves.DIO_ARMS_FOLDED;
            Vector feet = inst.stand.getLocation().toVector();
            Vector to = target.getLocation().toVector().subtract(feet).setY(0);
            facing[0] = DioMoves.flat(to);
            double reach = Math.max(1.2, Math.min(10, to.length() - 1.8));
            spot[0] = feet.clone().add(facing[0].clone().multiply(reach)).add(new Vector(0, 0.25, 0));
            say(inst, "The World!", NamedTextColor.YELLOW);
            fx.sound(feet, Sfx.PLAYER_ATTACK_SWEEP, 2f, 0.6f);
        });
        t.span(0, rush, (tick, p) -> {
            Vector from = inst.worldAt.clone();
            inst.worldAt = from.add(spot[0].clone().subtract(from).multiply(0.45));
            inst.worldYaw = DioMoves.yawOf(facing[0]);
            inst.worldPose = DioMoves.WORLD_WIND_UP;
            fx.dust(GOLD, 1.6f, 0.4, 4).at(inst.worldAt.clone().add(new Vector(0, 1.3, 0)));
        });
        t.span(rush, rush + barrage, (tick, p) -> {
            inst.worldAt = spot[0].clone();
            inst.worldPose = DioMoves.barrage(tick, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1);
            Vector chest = spot[0].clone().add(new Vector(0, 1.3, 0));
            Vector right = DioMoves.right(facing[0]);
            for (int i = 0; i < 4; i++) {
                Vector fist = chest.clone()
                        .add(facing[0].clone().multiply(1.2 + random.nextDouble() * 2.2))
                        .add(right.clone().multiply(random.nextGaussian() * 0.6))
                        .add(new Vector(0, random.nextGaussian() * 0.45, 0));
                fx.dust(GOLD, 1.3f, 0.12, 3).at(fist);
                if (i == 0) fx.cloud(Particle.CRIT, fist, 3, 0.2, 0.2);
            }
            if (tick % 3 == 0) fx.cloud(Particle.SWEEP_ATTACK, chest.clone().add(facing[0].clone().multiply(2)), 1, 0.6, 0);
            fx.sound(chest, tick % 2 == 0 ? Sfx.PLAYER_ATTACK_STRONG : Sfx.PLAYER_ATTACK_KNOCKBACK,
                    0.8f, 0.8f + random.nextFloat() * 0.6f);
            if (tick % 6 == 0) {
                for (Player p2 : playersNear(chest.toLocation(world), 12)) {
                    p2.sendActionBar(Component.text(muda(tick / 6 + 1), NamedTextColor.GOLD, TextDecoration.BOLD));
                }
            }
            if (tick % 3 == 0) {
                for (Player victim : playersInCone(world, spot[0], facing[0], 50, 4.2)) {
                    MscEntityUtils.damageBy(inst.stand, victim, barrageDamage);
                    victim.setVelocity(facing[0].clone().multiply(0.12).setY(0.05));
                }
            }
        });
        t.at(rush + barrage, () -> {
            inst.worldPose = DioMoves.WORLD_FINISHER;
            Vector chest = spot[0].clone().add(new Vector(0, 1.3, 0));
            fx.impact(chest.clone().add(facing[0].clone().multiply(2)), GOLD, 2.5);
            fx.sound(chest, Sfx.EXPLODE, 2f, 1.2f);
            fx.sound(chest, Sfx.IRON_GOLEM_ATTACK, 2f, 0.6f);
            say(inst, "MUDAAAA!", NamedTextColor.GOLD);
            for (Player victim : playersInCone(world, spot[0], facing[0], 55, 4.5)) {
                MscEntityUtils.damageBy(inst.stand, victim, barrageFinisherDamage);
                victim.setVelocity(facing[0].clone().multiply(2.4).setY(0.7));
            }
        });
        t.span(rush + barrage + 4, Attack.MUDA_BARRAGE.ticks, (tick, p) -> {
            Vector home = DioMoves.worldIdle(inst.stand.getLocation().toVector(), DioMoves.flat(inst.stand.getLocation().getDirection()), DIO_SCALE, inst.tick);
            inst.worldAt = inst.worldAt.clone().add(home.subtract(inst.worldAt).multiply(0.3));
            inst.worldPose = DioMoves.WORLD_FINISHER.lerp(DioMoves.WORLD_IDLE, p);
            inst.dioPose = DioMoves.DIO_IDLE;
        });
        return t;
    }

    private static String muda(int count) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < Math.min(8, count); i++) out.append("MUDA ");
        return out.toString().trim() + "!";
    }

    private List<Player> playersInCone(World world, Vector apex, Vector direction, double halfAngle, double length) {
        List<Player> out = new ArrayList<>();
        for (Player p : playersNear(apex.toLocation(world), length + 2)) {
            Vector chest = p.getLocation().toVector().add(new Vector(0, 1, 0));
            Vector base = apex.clone().add(new Vector(0, 1, 0));
            if (DioMoves.inCone(base, direction, halfAngle, length, chest)) out.add(p);
        }
        return out;
    }

    // ------------------------------------------------------------------ ROAD ROLLER DA

    private Timeline roadRoller(DioInstance inst, Player target) {
        World world = inst.stand.getWorld();
        Fx fx = LiveStage.fxIn(world);
        int leap = 20;
        int fall = 22;
        int impact = leap + fall;
        int pound = 30;
        double height = 24;
        Vector[] landing = new Vector[1];
        Vector[] start = new Vector[1];
        float[] yaw = new float[1];
        List<BlockDisplay> roller = new ArrayList<>();
        Set<UUID> struck = new HashSet<>();
        Timeline t = new Timeline();
        t.at(0, () -> {
            start[0] = inst.stand.getLocation().toVector();
            Location aim = target.getLocation();
            double floor = BossArena.findFloorY(aim.clone().add(0, 3, 0), 16);
            landing[0] = aim.toVector().setY(Double.isNaN(floor) ? aim.getY() : floor);
            yaw[0] = DioMoves.yawOf(landing[0].clone().subtract(start[0]));
            inst.airborne = true;
            inst.worldControlled = true;
            inst.dioPose = DioMoves.DIO_LEAP;
            fx.sound(start[0], Sfx.WIND_CHARGE_BURST, 2f, 0.7f);
            fx.flatBurst(start[0], Particle.CLOUD, 30, 0.4);
        });
        // The warning: where the roller will land, growing hotter.
        t.span(0, impact, (tick, p) -> {
            if (landing[0] == null || tick % 2 != 0) return;
            Vector floor = landing[0].clone().add(new Vector(0, 0.15, 0));
            fx.ring(floor, 5.5, 0.5, tick * 0.1, fx.dust(Palette.mix(Palette.WARNING, Palette.WARNING_HOT, p), 1.6f));
            fx.ring(floor, 3.2, 0.5, -tick * 0.1, fx.dust(Palette.WARNING, 1.2f));
        });
        // Up: an arc high over the target.
        t.span(1, leap, (tick, p) -> {
            Vector across = landing[0].clone().subtract(start[0]).multiply(p);
            inst.flightTo = start[0].clone().add(across).add(new Vector(0, height * Math.sin(p * Math.PI / 2), 0));
            inst.worldAt = inst.flightTo.clone().add(new Vector(0, 1.8, 0));
            inst.worldPose = DioMoves.WORLD_SPREAD;
            fx.cloud(Particle.CLOUD, inst.flightTo, 2, 0.3, 0.02);
        });
        t.at(leap, () -> {
            Vector top = landing[0].clone().add(new Vector(0, height, 0));
            for (DioMoves.RollerPart part : DioMoves.ROAD_ROLLER) {
                roller.add(spawnRollerPart(world, top, yaw[0], part, inst));
            }
            inst.dioPose = DioMoves.DIO_ON_ROLLER;
            for (Player p : playersNear(landing[0].toLocation(world), 50)) {
                p.showTitle(Title.title(Component.text("ROAD ROLLER DA!", NamedTextColor.GOLD, TextDecoration.BOLD),
                        Component.text("「ロードローラーだ!」", NamedTextColor.YELLOW), times(2, 30, 8)));
            }
            say(inst, "ROAD ROLLER DA!", NamedTextColor.GOLD);
            fx.sound(top, Sfx.ANVIL_PLACE, 3f, 0.5f);
        });
        // Down: the roller falls with DIO on its roof, faster and faster.
        t.span(leap, impact, (tick, p) -> {
            double y = height * (1 - p * p);
            Vector base = landing[0].clone().add(new Vector(0, y, 0));
            moveRoller(roller, base, yaw[0]);
            inst.flightTo = base.clone().add(new Vector(0, DioMoves.ROLLER_ROOF, 0));
            inst.worldAt = inst.flightTo.clone().add(new Vector(0, 1.2, 0));
            inst.worldPose = DioMoves.pound(tick);
            fx.cloud(Particle.LARGE_SMOKE, base.clone().add(new Vector(0, 2, 0)), 3, 1.5, 0.02);
        });
        t.at(impact, () -> {
            Vector center = landing[0].clone();
            fx.impact(center.clone().add(new Vector(0, 1, 0)), GOLD, 4);
            fx.burst(center.clone().add(new Vector(0, 1, 0)), Particle.EXPLOSION_EMITTER, 1, 0);
            fx.flatBurst(center, Particle.CLOUD, 60, 0.6);
            fx.crumble(groundAt(world, center), 50, 3).at(center.clone().add(new Vector(0, 0.5, 0)));
            fx.sound(center, Sfx.EXPLODE, 3f, 0.6f);
            fx.sound(center, Sfx.ANVIL_LAND, 3f, 0.5f);
            fx.sound(center, Sfx.MACE_SMASH_GROUND, 3f, 0.6f);
            for (Player p : playersNear(center.toLocation(world), 5.5)) {
                struck.add(p.getUniqueId());
                MscEntityUtils.damageBy(inst.stand, p, roadRollerDamage);
                Vector away = DioMoves.flat(p.getLocation().toVector().subtract(center));
                p.setVelocity(away.multiply(1.1).setY(0.8));
            }
        });
        // MUDA on the roof: The World pounds the roller into whoever is under it.
        t.span(impact, impact + pound, (tick, p) -> {
            Vector base = landing[0].clone();
            Vector shake = new Vector(random.nextGaussian() * 0.05, 0, random.nextGaussian() * 0.05);
            moveRoller(roller, base.clone().add(shake), yaw[0]);
            inst.flightTo = base.clone().add(new Vector(0, DioMoves.ROLLER_ROOF, 0));
            inst.worldAt = inst.flightTo.clone().add(new Vector(0, 1.2, 0));
            inst.worldPose = DioMoves.pound(tick);
            Vector roof = inst.flightTo.clone().add(new Vector(0, 0.3, 0));
            fx.dust(GOLD, 1.4f, 0.8, 3).at(roof);
            fx.cloud(Particle.CRIT, roof, 4, 0.8, 0.2);
            fx.sound(roof, Sfx.ANVIL_LAND, 0.7f, 1.4f + random.nextFloat() * 0.4f);
            if (tick % 6 == 0) {
                for (Player p2 : playersNear(base.toLocation(world), 4.5)) {
                    MscEntityUtils.damageBy(inst.stand, p2, barrageDamage);
                }
                for (Player p2 : playersNear(base.toLocation(world), 30)) {
                    p2.sendActionBar(Component.text(muda(tick / 6 + 1), NamedTextColor.GOLD, TextDecoration.BOLD));
                }
            }
        });
        t.at(impact + pound, () -> {
            Vector center = landing[0].clone();
            say(inst, "WRYYYYYYY!", NamedTextColor.GOLD);
            fx.impact(center.clone().add(new Vector(0, 2, 0)), Palette.EMBER, 4);
            fx.burst(center.clone().add(new Vector(0, 2, 0)), Particle.EXPLOSION_EMITTER, 2, 0);
            fx.sound(center, Sfx.EXPLODE, 3f, 0.8f);
            for (BlockDisplay part : roller) {
                fx.crumble(part.getBlock().getMaterial(), 20, 1).at(part.getLocation().toVector().add(new Vector(0, 1, 0)));
                part.remove();
            }
            roller.clear();
            for (Player p : playersNear(center.toLocation(world), 7)) {
                MscEntityUtils.damageBy(inst.stand, p, roadRollerDamage * 0.5);
                p.setVelocity(DioMoves.flat(p.getLocation().toVector().subtract(center)).multiply(1.3).setY(0.9));
            }
            // He steps down beside the wreck.
            Vector beside = center.clone().add(DioMoves.right(new Vector(-Math.sin(Math.toRadians(yaw[0])), 0, Math.cos(Math.toRadians(yaw[0])))).multiply(3.5));
            double floor = BossArena.findFloorY(beside.toLocation(world).add(0, 4, 0), 12);
            inst.flightTo = beside.setY(Double.isNaN(floor) ? center.getY() : floor);
            inst.dioPose = DioMoves.DIO_IDLE;
        });
        t.at(impact + pound + 2, () -> {
            inst.airborne = false;
            inst.flightTo = null;
        });
        t.span(impact + pound + 2, Attack.ROAD_ROLLER.ticks, (tick, p) -> inst.worldControlled = false);
        t.onFinish(() -> {
            roller.forEach(Entity::remove);
            roller.clear();
        });
        return t;
    }

    private BlockDisplay spawnRollerPart(World world, Vector base, float yaw, DioMoves.RollerPart part, DioInstance inst) {
        Location at = base.toLocation(world, yaw, 0);
        Material block = Material.valueOf(part.block());
        BlockDisplay display = world.spawn(at, BlockDisplay.class, d -> {
            d.setBlock(block.createBlockData());
            d.setTransformation(new Transformation(
                    new Vector3f(part.corner()[0], part.corner()[1], part.corner()[2]),
                    new Quaternionf(),
                    new Vector3f(part.size()[0], part.size()[1], part.size()[2]),
                    new Quaternionf()));
            d.setTeleportDuration(1);
            d.setPersistent(false);
            d.addScoreboardTag(PROP_TAG);
        });
        inst.props.add(display.getUniqueId());
        return display;
    }

    private static void moveRoller(List<BlockDisplay> roller, Vector base, float yaw) {
        for (BlockDisplay part : roller) {
            part.teleport(base.toLocation(part.getWorld(), yaw, 0));
        }
    }

    private static Material groundAt(World world, Vector at) {
        Material type = world.getBlockAt(at.getBlockX(), at.getBlockY() - 1, at.getBlockZ()).getType();
        return type.isSolid() ? type : Material.STONE;
    }

    // ------------------------------------------------------------------ knives

    private Timeline knifeFan(DioInstance inst, Player target) {
        World world = inst.stand.getWorld();
        Fx fx = LiveStage.fxIn(world);
        int draw = 14;
        int count = inst.phase == 2 ? 9 : 7;
        Timeline t = new Timeline();
        t.span(0, draw, (tick, p) -> {
            inst.dioPose = Pose.REST.lerp(DioMoves.DIO_KNIVES_DRAWN, Math.min(1, p * 1.3));
            Vector to = target.getLocation().toVector().subtract(inst.stand.getLocation().toVector()).setY(0);
            if (to.lengthSquared() > 0.01) {
                Location loc = inst.stand.getLocation();
                loc.setDirection(to);
                inst.stand.teleport(loc);
            }
            Vector hand = hand(inst.stand);
            fx.dust(Palette.ICE, 1.0f, 0.15, 2).at(hand);
            if (tick % 3 == 0) fx.sound(hand, Sfx.CHAIN_BREAK, 0.8f, 1.8f);
        });
        t.at(draw, () -> {
            inst.dioPose = DioMoves.DIO_KNIVES_THROWN;
            Vector hand = hand(inst.stand);
            Vector aim = target.getLocation().toVector().add(new Vector(0, 1.1, 0)).subtract(hand).normalize();
            double spread = Math.toRadians(inst.phase == 2 ? 70 : 55);
            for (int i = 0; i < count; i++) {
                double a = -spread / 2 + spread * i / (count - 1);
                Vector dir = rotateY(aim, a);
                Knife k = spawnKnife(world, hand, dir);
                k.dir = dir;
                k.speed = 1.25;
                k.flying = 24;
                k.armed = true;
                inst.knives.add(k);
            }
            say(inst, "Mudada!", NamedTextColor.YELLOW);
            fx.sound(hand, Sfx.TRIDENT_THROW, 2f, 1.3f);
            fx.sound(hand, Sfx.PLAYER_ATTACK_SWEEP, 1.5f, 1.6f);
        });
        t.span(draw + 6, Attack.KNIFE_FAN.ticks, (tick, p) -> inst.dioPose = DioMoves.DIO_KNIVES_THROWN.lerp(DioMoves.DIO_IDLE, p));
        return t;
    }

    private Knife spawnKnife(World world, Vector at, Vector dir) {
        ItemDisplay display = world.spawn(at.toLocation(world), ItemDisplay.class, d -> {
            d.setItemStack(new ItemStack(Material.IRON_SWORD));
            d.setTransformation(new Transformation(new Vector3f(), DioMoves.blade(dir), new Vector3f(0.7f), new Quaternionf()));
            d.setTeleportDuration(1);
            d.setPersistent(false);
            d.setBrightness(new Display.Brightness(15, 15));
            d.addScoreboardTag(PROP_TAG);
        });
        Knife k = new Knife(display, at.clone());
        k.dir = dir.clone().normalize();
        return k;
    }

    /** Moves every knife, hits whoever an armed one passes through, and clears the spent ones. */
    private void tickProps(DioInstance inst) {
        Fx fx = LiveStage.fxIn(inst.stand.getWorld());
        for (Knife k : new ArrayList<>(inst.knives)) {
            if (!k.display.isValid()) {
                inst.knives.remove(k);
                continue;
            }
            if (k.stopAt != null) {
                // Thrown into stopped time: it travels a step, then hangs.
                Vector to = k.stopAt.clone().subtract(k.pos);
                if (to.length() > 0.05) {
                    k.pos.add(to.multiply(Math.min(1, k.speed / Math.max(0.05, to.length()))));
                    k.display.teleport(k.pos.toLocation(k.display.getWorld()));
                }
                if (k.aim != null) k.display.setTransformation(new Transformation(new Vector3f(), DioMoves.blade(k.aim),
                        new Vector3f(0.7f), new Quaternionf()));
                continue;
            }
            if (k.flying <= 0) {
                k.display.remove();
                inst.knives.remove(k);
                continue;
            }
            Vector before = k.pos.clone();
            k.pos.add(k.dir.clone().multiply(k.speed));
            k.flying--;
            k.display.teleport(k.pos.toLocation(k.display.getWorld()));
            k.display.setTransformation(new Transformation(new Vector3f(), DioMoves.blade(k.dir), new Vector3f(0.7f), new Quaternionf()));
            fx.dust(Palette.ICE, 0.7f).at(before);
            if (!k.armed) continue;
            for (Player p : playersNear(k.pos.toLocation(k.display.getWorld()), 3)) {
                Vector chest = p.getLocation().toVector().add(new Vector(0, 1, 0));
                if (DioMoves.distanceToSegment(chest, before, k.pos) > 0.9) continue;
                MscEntityUtils.damageBy(inst.stand, p, knifeDamage);
                fx.cloud(Particle.CRIT, chest, 6, 0.2, 0.1);
                k.display.remove();
                inst.knives.remove(k);
                break;
            }
            if (k.display.isValid() && k.pos.toLocation(k.display.getWorld()).getBlock().getType().isSolid()) {
                k.display.remove();
                inst.knives.remove(k);
            }
        }
        for (Menacing m : new ArrayList<>(inst.menacing)) {
            m.age++;
            if (!m.display.isValid() || m.age > 34) {
                m.display.remove();
                inst.menacing.remove(m);
                continue;
            }
            m.display.teleport(m.display.getLocation().add(0, 0.035, 0));
        }
    }

    // ------------------------------------------------------------------ Space Ripper Stingy Eyes

    private Timeline eyeBeams(DioInstance inst, Player target) {
        World world = inst.stand.getWorld();
        Fx fx = LiveStage.fxIn(world);
        int charge = 16;
        int fire = 28;
        double length = 22;
        Vector[] aim = new Vector[1];
        Timeline t = new Timeline();
        t.span(0, charge, (tick, p) -> {
            inst.dioPose = Pose.REST.lerp(DioMoves.DIO_EYES, p);
            Vector to = target.getLocation().toVector().add(new Vector(0, 1, 0)).subtract(eyes(inst.stand, 0));
            aim[0] = to.normalize();
            for (int side = -1; side <= 1; side += 2) fx.dust(Palette.BLOOD, 0.8f + (float) p).at(eyes(inst.stand, side));
            if (tick % 4 == 0) fx.sound(eyes(inst.stand, 0), Sfx.BEACON_POWER, 1f, 1.2f + (float) p);
        });
        t.at(charge, () -> {
            say(inst, "Kuukan Ripper Sutingī Aizu!", NamedTextColor.AQUA);
            fx.sound(eyes(inst.stand, 0), Sfx.FIRE_EXTINGUISH, 2f, 1.6f);
        });
        t.span(charge, charge + fire, (tick, p) -> {
            double sweep = Math.toRadians(28) * Math.sin(p * Math.PI * 1.5);
            Vector dir = rotateY(aim[0], sweep);
            for (int side = -1; side <= 1; side += 2) {
                Vector from = eyes(inst.stand, side);
                Vector end = from.clone().add(dir.clone().multiply(length));
                // Stop where the jet meets the ground.
                for (double d = 0; d < length; d += 0.5) {
                    Vector point = from.clone().add(dir.clone().multiply(d));
                    if (point.toLocation(world).getBlock().getType().isSolid()) {
                        end = point;
                        break;
                    }
                }
                fx.line(from, end, 0.25, fx.dust(STINGY, 0.6f).and(fx.dust(Palette.FROST, 0.4f).sometimes(0.3)));
                fx.cloud(Particle.CLOUD, end, 2, 0.15, 0.05);
                fx.cloud(Particle.ELECTRIC_SPARK, end, 2, 0.2, 0.05);
                if (tick % 3 == 0) {
                    for (Player p2 : playersNear(from.toLocation(world), length)) {
                        Vector chest = p2.getLocation().toVector().add(new Vector(0, 1, 0));
                        if (DioMoves.distanceToSegment(chest, from, end) <= 0.9) {
                            MscEntityUtils.damageBy(inst.stand, p2, eyeBeamDamage);
                        }
                    }
                }
            }
            if (tick % 4 == 0) fx.sound(eyes(inst.stand, 0), Sfx.FIRE_EXTINGUISH, 1f, 1.8f);
        });
        t.span(charge + fire, Attack.EYE_BEAMS.ticks, (tick, p) -> inst.dioPose = DioMoves.DIO_EYES.lerp(DioMoves.DIO_IDLE, p));
        return t;
    }

    // ------------------------------------------------------------------ The World's punch

    private Timeline punch(DioInstance inst, Player target) {
        World world = inst.stand.getWorld();
        Fx fx = LiveStage.fxIn(world);
        Vector[] facing = new Vector[1];
        Timeline t = new Timeline();
        t.span(0, 8, (tick, p) -> {
            inst.worldControlled = true;
            Location loc = inst.stand.getLocation();
            facing[0] = DioMoves.flat(target.getLocation().toVector().subtract(loc.toVector()));
            inst.worldAt = loc.toVector().add(facing[0].clone().multiply(0.6)).add(DioMoves.right(facing[0]).multiply(0.7))
                    .add(new Vector(0, 0.3, 0));
            inst.worldYaw = DioMoves.yawOf(facing[0]);
            inst.worldPose = DioMoves.WORLD_IDLE.lerp(DioMoves.WORLD_WIND_UP, p);
            inst.dioPose = DioMoves.DIO_ARMS_FOLDED;
        });
        t.at(8, () -> {
            inst.worldPose = DioMoves.WORLD_PUNCH;
            Vector fist = inst.worldAt.clone().add(new Vector(0, 1.4, 0)).add(facing[0].clone().multiply(1.8));
            fx.impact(fist, GOLD, 1.5);
            fx.sound(fist, Sfx.PLAYER_ATTACK_STRONG, 2f, 0.6f);
            fx.sound(fist, Sfx.IRON_GOLEM_ATTACK, 1.5f, 0.8f);
            say(inst, "Muda!", NamedTextColor.YELLOW);
            for (Player victim : playersInCone(world, inst.worldAt, facing[0], 45, 3.8)) {
                MscEntityUtils.damageBy(inst.stand, victim, punchDamage);
                victim.setVelocity(facing[0].clone().multiply(1.4).setY(0.45));
            }
        });
        t.span(10, Attack.PUNCH.ticks, (tick, p) -> inst.worldPose = DioMoves.WORLD_PUNCH.lerp(DioMoves.WORLD_IDLE, p));
        return t;
    }

    // ------------------------------------------------------------------ flavour

    /** "Oh? You're approaching me?" — once per player per fight. */
    private void greet(DioInstance inst, World world) {
        for (Player p : playersNear(inst.stand.getLocation(), 7)) {
            if (!inst.greeted.add(p.getUniqueId())) continue;
            p.sendMessage(MscText.rich(NamedTextColor.GOLD, "DIO: ", NamedTextColor.YELLOW,
                    "Oh? You're approaching me? Instead of running away, you're coming right to me?"));
            LiveStage.fxIn(world).sound(inst.stand.getLocation().toVector(), Sfx.NOTE_BASEDRUM, 1f, 0.5f);
        }
    }

    /** ゴゴゴ — the menacing letters that drift up around him while he walks. */
    private void menacing(DioInstance inst, World world) {
        if (inst.tick % 16 != 0 || inst.menacing.size() > 6) return;
        Location at = inst.stand.getLocation().clone().add(
                random.nextGaussian() * 1.4, 1.2 + random.nextDouble() * 1.6, random.nextGaussian() * 1.4);
        TextDisplay display = world.spawn(at, TextDisplay.class, d -> {
            d.text(Component.text("ゴ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD));
            d.setBillboard(Display.Billboard.CENTER);
            d.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            d.setShadowed(true);
            d.setTeleportDuration(1);
            d.setPersistent(false);
            float size = 1.2f + random.nextFloat() * 0.8f;
            d.setTransformation(new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(size), new Quaternionf()));
            d.addScoreboardTag(PROP_TAG);
        });
        inst.menacing.add(new Menacing(display));
    }

    /** The golden glow of a Stand around The World. */
    private void aura(DioInstance inst) {
        if (inst.tick % 3 != 0) return;
        Fx fx = LiveStage.fxIn(inst.stand.getWorld());
        Vector body = inst.worldAt.clone().add(new Vector(0, 1.3 * WORLD_SCALE, 0));
        fx.dust(GOLD, 1.1f, 0.6, 2).at(body);
        if (inst.tick % 9 == 0) fx.dust(GREEN, 0.9f, 0.5, 1).at(body);
        if (inst.timeStopped) fx.cloud(Particle.REVERSE_PORTAL, body, 2, 0.6, 0.02);
    }

    private void say(DioInstance inst, String line, NamedTextColor color) {
        for (Player p : playersNear(inst.stand.getLocation(), 50)) {
            p.sendMessage(MscText.rich(NamedTextColor.GOLD, "DIO: ", color, line));
        }
    }

    private static Title.Times times(int in, int stay, int out) {
        return Title.Times.times(Duration.ofMillis(in * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(out * 50L));
    }

    // ------------------------------------------------------------------ body

    /** DIO's own head model (stands/dio-brando.txt). */
    private static com.Chagui68.stand.HeadModel dioModel() {
        return com.Chagui68.stand.HeadModels.of("dio-brando");
    }

    /** DIO's body of textured heads, on his armour stand and in its pose. */
    private void bodyOfDio(DioInstance inst, Location at) {
        com.Chagui68.stand.HeadModel model = dioModel();
        if (model == null) return;
        Pose p = inst.dioPose;
        var pose = com.Chagui68.stand.StandRig.fromArmorStand(p.head(), p.body(), p.leftArm(), p.rightArm(),
                p.leftLeg(), p.rightLeg());
        Location feet = at.clone();
        feet.setPitch(0);
        if (inst.dioBody == null || !inst.dioBody.alive()) {
            if (inst.dioBody != null) inst.dioBody.remove();
            inst.dioBody = new com.Chagui68.stand.HeadPuppet(model,
                    com.Chagui68.stand.HeadPuppet.scaleFor(model, ARMOR_STAND_HEIGHT * DIO_SCALE), DIO_PART_TAG, 0);
            inst.dioBody.spawn(feet, pose, 1f);
            return;
        }
        inst.dioBody.moveTo(feet);
        // Eased on the server so a change of attack never pulls the limbs apart.
        inst.dioBody.ease(pose, new org.joml.Vector3f(), DIO_EASE, 1);
    }

    /** The World's head model (stands/the-world.txt). */
    private static com.Chagui68.stand.HeadModel worldModel() {
        return com.Chagui68.stand.HeadModels.of(com.Chagui68.stand.StandType.THE_WORLD);
    }

    /**
     * The World's body of textured heads, riding its armour stand and taking the stand's pose, so
     * every move written for the stand (the punch, the barrage, the road roller) moves it too.
     */
    private void bodyOfTheWorld(DioInstance inst, Location at) {
        com.Chagui68.stand.HeadModel model = worldModel();
        if (model == null) return;
        Pose p = inst.worldPose;
        var pose = com.Chagui68.stand.StandRig.fromArmorStand(p.head(), p.body(), p.leftArm(), p.rightArm(),
                p.leftLeg(), p.rightLeg());
        if (inst.worldBody == null || !inst.worldBody.alive()) {
            if (inst.worldBody != null) inst.worldBody.remove();
            inst.worldBody = new com.Chagui68.stand.HeadPuppet(model,
                    com.Chagui68.stand.HeadPuppet.scaleFor(model, ARMOR_STAND_HEIGHT * WORLD_SCALE), WORLD_PART_TAG, 0);
            inst.worldBody.spawn(at, pose, 1f);
            return;
        }
        inst.worldBody.moveTo(at);
        inst.worldBody.ease(pose, new org.joml.Vector3f(), WORLD_EASE, 1);
    }

    private static void pose(ArmorStand stand, Pose pose) {
        stand.setHeadPose(Pose.euler(pose.head()));
        stand.setBodyPose(Pose.euler(pose.body()));
        stand.setLeftArmPose(Pose.euler(pose.leftArm()));
        stand.setRightArmPose(Pose.euler(pose.rightArm()));
        stand.setLeftLegPose(Pose.euler(pose.leftLeg()));
        stand.setRightLegPose(Pose.euler(pose.rightLeg()));
    }

    /** About where his right hand is: out at shoulder height, a little forward. */
    private static Vector hand(ArmorStand stand) {
        Location loc = stand.getLocation();
        Vector forward = DioMoves.flat(loc.getDirection());
        return loc.toVector().add(new Vector(0, 1.55 * DIO_SCALE, 0))
                .add(DioMoves.right(forward).multiply(0.45 * DIO_SCALE)).add(forward.multiply(0.3));
    }

    /** His eyes: {@code side} -1 or 1 for one eye, 0 for between them. */
    private static Vector eyes(ArmorStand stand, int side) {
        Location loc = stand.getLocation();
        Vector forward = DioMoves.flat(loc.getDirection());
        return loc.toVector().add(new Vector(0, 1.72 * DIO_SCALE, 0))
                .add(DioMoves.right(forward).multiply(0.12 * side)).add(forward.multiply(0.3));
    }

    private static Vector rotateY(Vector v, double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Vector(v.getX() * c - v.getZ() * s, v.getY(), v.getX() * s + v.getZ() * c);
    }

    // ------------------------------------------------------------------ damage, death, cleanup

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        boolean dio = victim.getScoreboardTags().contains(TAG);
        boolean stand = victim.getScoreboardTags().contains(STAND_TAG);
        if (event instanceof EntityDamageByEntityEvent byEntity) {
            Player attacker = attacker(byEntity.getDamager());
            // Nobody moves in stopped time, and that includes swinging a sword.
            if (attacker != null && isFrozen(attacker)) {
                event.setCancelled(true);
                return;
            }
            if (dio && attacker != null) {
                event.setCancelled(true);
                hurt((ArmorStand) victim, attacker, Math.max(1.0, event.getFinalDamage()));
                return;
            }
        }
        if (dio || stand) event.setCancelled(true);
    }

    private static Player attacker(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player p) return p;
        return null;
    }

    private void hurt(ArmorStand stand, Player attacker, double damage) {
        DioInstance inst = active.get(stand.getUniqueId());
        if (inst == null) return;
        double applied = Math.min(damage, maxDamagePerHit > 0 ? maxDamagePerHit : damage);
        double left = Math.max(0, MscEntityUtils.getVirtualHealth(stand) - applied);
        MscEntityUtils.setVirtualHealth(stand, left);
        Fx fx = LiveStage.fxIn(stand.getWorld());
        Vector chest = stand.getLocation().toVector().add(new Vector(0, 1.3, 0));
        fx.cloud(Particle.DAMAGE_INDICATOR, chest, 4, 0.3, 0.1);
        fx.sound(chest, Sfx.PLAYER_ATTACK_CRIT, 0.8f, 1.1f);
        if (left <= 0) die(inst, attacker);
    }

    private void die(DioInstance inst, Player killer) {
        World world = inst.stand.getWorld();
        Location at = inst.stand.getLocation();
        Fx fx = LiveStage.fxIn(world);
        say(inst, "B-baka na... kono DIO ga... kono DIO ga!", NamedTextColor.RED);
        for (Player p : playersNear(at, 60)) {
            p.showTitle(Title.title(Component.text("DIO HAS FALLEN", NamedTextColor.GOLD, TextDecoration.BOLD),
                    Component.text(killer != null ? "Defeated by " + killer.getName() : "Time flows again",
                            NamedTextColor.YELLOW), times(5, 60, 20)));
        }
        fx.impact(at.toVector().add(new Vector(0, 1.2, 0)), GOLD, 4);
        fx.burst(at.toVector().add(new Vector(0, 1.2, 0)), Particle.EXPLOSION_EMITTER, 1, 0);
        fx.sound(at.toVector(), Sfx.WITHER_DEATH, 2f, 1.2f);
        world.spawn(at, ExperienceOrb.class, orb -> orb.setExperience(1200));
        // His blood: the one ingredient of the Bearer's Elixir.
        int min = Math.max(0, plugin.getConfig().getInt("entities.dio-brando.vampire-blood-min", 1));
        int max = Math.max(min, plugin.getConfig().getInt("entities.dio-brando.vampire-blood-max", 2));
        int blood = min + java.util.concurrent.ThreadLocalRandom.current().nextInt(max - min + 1);
        if (blood > 0) {
            org.bukkit.inventory.ItemStack drop = com.Chagui68.items.components.VampireBlood.VAMPIRE_BLOOD.clone();
            drop.setAmount(blood);
            world.dropItemNaturally(at.clone().add(0, 0.5, 0), drop);
        }
        if (inst.world != null) inst.world.remove();
        if (inst.worldBody != null) inst.worldBody.remove();
        if (inst.dioBody != null) inst.dioBody.remove();
        inst.stand.remove();
        cleanup(inst);
    }

    private void cleanup(DioInstance inst) {
        resumeTime(inst);
        removeProps(inst);
        if (inst.world != null && inst.world.isValid()) inst.world.remove();
        if (inst.worldBody != null) inst.worldBody.remove();
        if (inst.dioBody != null) inst.dioBody.remove();
        if (inst.bossBar != null) inst.bossBar.removeAll();
        active.remove(inst.stand.getUniqueId());
    }

    private void removeProps(DioInstance inst) {
        for (Knife k : inst.knives) k.display.remove();
        inst.knives.clear();
        for (Menacing m : inst.menacing) m.display.remove();
        inst.menacing.clear();
        for (UUID id : inst.props) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) e.remove();
        }
        inst.props.clear();
        inst.attack = null;
    }

    /** Removes any The World whose DIO is gone. */
    private void sweepStands() {
        Map<UUID, java.util.Collection<UUID>> worn = new HashMap<>();
        for (DioInstance inst : active.values()) {
            if (inst.world != null) worn.put(inst.stand.getUniqueId(), List.of(inst.world.getUniqueId()));
        }
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand candidate : world.getEntitiesByClass(ArmorStand.class)) {
                if (!candidate.getScoreboardTags().contains(STAND_TAG)) continue;
                UUID owner = DisplaySuit.ownerOf(candidate, OWNER_PREFIX);
                java.util.Collection<UUID> mine = owner == null ? null : worn.get(owner);
                if (mine != null && mine.contains(candidate.getUniqueId())) continue;
                if (owner != null && mine == null && Bukkit.getEntity(owner) != null) continue;
                candidate.remove();
            }
        }
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof ArmorStand stand && stand.getScoreboardTags().contains(TAG)) adopt(stand);
        }
    }

    @EventHandler
    public void onManipulate(PlayerArmorStandManipulateEvent event) {
        Set<String> tags = event.getRightClicked().getScoreboardTags();
        if (tags.contains(TAG) || tags.contains(STAND_TAG)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        if (isFrozen(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player p && isFrozen(p)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        MscEntityUtils.applyDeathMessage(plugin, event, TAG, "entities.dio-brando.death-messages");
    }

    // ------------------------------------------------------------------ state

    private record PendingKnife(int at, Vector spot, Vector aim) {
    }

    private static final class Knife {
        final ItemDisplay display;
        final Vector pos;
        Vector dir;
        Vector stopAt;
        Vector aim;
        double speed = 1;
        int flying;
        boolean armed;

        Knife(ItemDisplay display, Vector pos) {
            this.display = display;
            this.pos = pos;
        }
    }

    private static final class Menacing {
        final TextDisplay display;
        int age;

        Menacing(TextDisplay display) {
            this.display = display;
        }
    }

    static final class DioInstance {
        final ArmorStand stand;
        ArmorStand world;
        /** The World's textured body, when it has a head model. */
        com.Chagui68.stand.HeadPuppet worldBody;
        /** DIO's own textured body, when he has a head model. */
        com.Chagui68.stand.HeadPuppet dioBody;
        BossBar bossBar;
        Timeline attack;
        String attackName;
        String lastAttack;
        int attackCooldown = 60;
        int timeStopCooldown = 240;
        int tick;
        int phase = 1;
        double walkPhase;
        Pose dioPose = DioMoves.DIO_IDLE;
        Pose worldPose = DioMoves.WORLD_IDLE;
        Vector worldAt = new Vector();
        float worldYaw;
        boolean worldControlled;
        boolean airborne;
        Vector flightTo;
        boolean timeStopped;
        final Map<UUID, Location> frozen = new HashMap<>();
        final List<UUID> frozenMobs = new ArrayList<>();
        final Map<UUID, Vector> frozenProjectiles = new HashMap<>();
        final List<PendingKnife> pendingKnives = new ArrayList<>();
        final List<Knife> knives = new ArrayList<>();
        final List<Menacing> menacing = new ArrayList<>();
        final List<UUID> props = new ArrayList<>();
        final Set<UUID> greeted = new HashSet<>();

        DioInstance(ArmorStand stand) {
            this.stand = stand;
            this.worldAt = stand.getLocation().toVector();
        }
    }
}
