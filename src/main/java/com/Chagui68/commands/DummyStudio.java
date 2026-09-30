package com.Chagui68.commands;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.BossInstance;
import com.Chagui68.entities.boss.ArmorStandBoss;
import com.Chagui68.entities.boss.AttackPreview;
import com.Chagui68.entities.boss.MagicSealListener;
import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

/**
 * The {@code /msc dummy} subsystem: one enlarged armour stand per player, its pose commands, the
 * wing seals and the scripted animations (fly up, slam, rain, seals...).
 *
 * <p>This was the biggest single block of {@code MSCCommand} (spawning, pose maths, particle
 * timelines and its own help page). It owns the per-player dummy registry, so the command no longer
 * has to.
 */
final class DummyStudio {

    /** Actions accepted right after {@code /msc dummy}. */
    static final List<String> ACTIONS = List.of(
            "spawn", "remove", "set", "wings", "wings2", "nowings", "animate", "attack");

    /** Poses that can be set or nudged, in help and completion order. */
    static final List<String> PARTS = List.of(
            "rightarm", "leftarm", "body", "head", "rightleg", "leftleg");

    /** Axes of a pose nudge ({@code /msc dummy <part> <axis> <degrees>}). */
    static final List<String> AXES = List.of("x", "y", "z");

    /** Scripted animations of {@code /msc dummy animate <anim>}. */
    static final List<String> ANIMATIONS = List.of(
            "flyup", "land", "airslam", "shieldseal", "healingcircle", "rain", "pentagram", "triangle");

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, ArmorStand> playerDummies = new HashMap<>();
    private final Map<UUID, BukkitRunnable> dummyWingTasks = new HashMap<>();

    DummyStudio(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    /** Everything the {@code /msc dummy} hint offers after the sub-command. */
    static List<String> actionCompletions() {
        List<String> all = new ArrayList<>(ACTIONS);
        all.addAll(PARTS);
        return all;
    }

    static List<String> helpLines() {
        return List.of(
                " &e&l/msc dummy spawn",
                "    &7Spawn a dummy armor stand (same scale/gear as boss)",
                " &e&l/msc dummy remove",
                "    &7Remove your dummy",
                " &e&l/msc dummy <part> <axis> <degrees>",
                "    &7Adjust pose incrementally",
                "    &7Parts: rightarm, leftarm, body, head, rightleg, leftleg",
                "    &7Axes: x (pitch), y (yaw), z (roll)  ·  Ex: /msc dummy rightarm x 10",
                " &e&l/msc dummy set <part> <x> <y> <z>",
                "    &7Set exact pose in degrees  ·  Ex: /msc dummy set rightarm -75 0 -15",
                " &e&l/msc dummy wings|wings2|nowings",
                "    &7Add gold/red wing seal or remove wing effects",
                " &e&l/msc dummy animate <anim>",
                "    &7Animations: flyup, land, airslam, shieldseal, healingcircle, rain, pentagram, triangle",
                " &e&l/msc dummy attack <attack|random>",
                "    &7Make the dummy perform a real boss attack: full animation,",
                "    &7particles and seals, but no damage to anyone",
                " &e&l/msc dummy attack list [page]",
                "    &7Browse every attack name &8· &7Ex: /msc dummy attack obsidianwings");
    }

    void handle(CommandSender sender, String[] args) {
        CommandMenu menu = new CommandMenu(sender);
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "Only players can use this command.");
            return;
        }

        if (args.length < 2) {
            menu.dummyHelp(1);
            return;
        }

        if (args[1].equalsIgnoreCase("help")) {
            menu.dummyHelp(menu.parsePage(args, 2));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "spawn" -> spawnDummy(player);
            case "remove" -> removeDummy(player);
            case "set" -> setPose(player, args);
            case "wings" -> wings(player);
            case "wings2" -> wings2(player);
            case "nowings" -> noWings(player);
            case "animate" -> animate(player, args);
            case "attack" -> attackPreview(player, args);
            default -> adjustPose(player, args);
        }
    }

    private void spawnDummy(Player player) {
        ArmorStand existing = playerDummies.get(player.getUniqueId());
        if (existing != null && existing.isValid()) {
            player.sendMessage(YELLOW + "You already have a dummy. Use /msc dummy remove to remove it first.");
            return;
        }

        ArmorStand stand = player.getWorld().spawn(player.getLocation(), ArmorStand.class, s -> {
            s.setInvulnerable(false);
            s.customName(MscText.line(NamedTextColor.LIGHT_PURPLE, "Pose Dummy"));
            s.setCustomNameVisible(true);
            s.setRemoveWhenFarAway(false);
            s.setPersistent(true);
            s.setAI(true);
            s.setCanPickupItems(false);
            s.setSmall(false);
            s.setArms(true);
            s.setBasePlate(false);
            s.setGravity(false);
        });

        AttributeInstance scaleAttr = stand.getAttribute(Attribute.SCALE);
        if (scaleAttr != null) scaleAttr.setBaseValue(7.5);

        EntityEquipment equip = stand.getEquipment();
        if (equip != null) {
            ItemStack spear = new ItemStack(Material.NETHERITE_SPEAR);
            ItemMeta spearMeta = spear.getItemMeta();
            if (spearMeta != null) {
                spearMeta.setUnbreakable(true);
                spear.setItemMeta(spearMeta);
            }
            equip.setItemInMainHand(spear);

            ItemStack shield = new ItemStack(Material.SHIELD);
            ItemMeta shieldMeta = shield.getItemMeta();
            if (shieldMeta != null) {
                shieldMeta.setUnbreakable(true);
                shield.setItemMeta(shieldMeta);
            }
            equip.setItemInOffHand(shield);
        }

        stand.addScoreboardTag(AttackPreview.TAG);

        playerDummies.put(player.getUniqueId(), stand);
        player.sendMessage(GREEN + "Spawned pose dummy at your location.");
        player.sendMessage(GRAY + "Current pose — RightArm: (0, 0, 0) LeftArm: (0, 0, 0) Body: (0, 0, 0)");
    }

    private void removeDummy(Player player) {
        ArmorStand stand = playerDummies.remove(player.getUniqueId());
        if (stand != null && stand.isValid()) {
            stand.remove();
            player.sendMessage(GREEN + "Dummy removed.");
        } else {
            player.sendMessage(RED + "You don't have a dummy.");
        }
    }

    private void wings(Player player) {
        ArmorStand stand = playerDummies.get(player.getUniqueId());
        if (stand == null || !stand.isValid()) {
            player.sendMessage(RED + "You don't have a dummy. Use /msc dummy spawn first.");
            return;
        }
        cancelWings(player);
        if (plugin.getMagicSealListener() != null) {
            BukkitRunnable task = plugin.getMagicSealListener().spawnWingSeal(stand);
            if (task != null) dummyWingTasks.put(player.getUniqueId(), task);
            player.sendMessage(GREEN + "Wing seal added to your dummy.");
        } else {
            player.sendMessage(RED + "MagicSealListener not available.");
        }
    }

    private void wings2(Player player) {
        ArmorStand stand = playerDummies.get(player.getUniqueId());
        if (stand == null || !stand.isValid()) {
            player.sendMessage(RED + "You don't have a dummy. Use /msc dummy spawn first.");
            return;
        }
        cancelWings(player);
        if (plugin.getMagicSealListener() != null) {
            BukkitRunnable task = plugin.getMagicSealListener().spawnWingSeal2(stand);
            if (task != null) dummyWingTasks.put(player.getUniqueId(), task);
            player.sendMessage(GREEN + "Wing seal 2 added to your dummy.");
        } else {
            player.sendMessage(RED + "MagicSealListener not available.");
        }
    }

    private void noWings(Player player) {
        if (!playerDummies.containsKey(player.getUniqueId())) {
            player.sendMessage(RED + "You don't have a dummy. Use /msc dummy spawn first.");
            return;
        }
        cancelWings(player);
        player.sendMessage(GREEN + "Wing effects removed from your dummy.");
    }

    private void cancelWings(Player player) {
        BukkitRunnable task = dummyWingTasks.remove(player.getUniqueId());
        if (task != null) task.cancel();
    }

    private ArmorStand getOrDummy(Player player) {
        ArmorStand stand = playerDummies.get(player.getUniqueId());
        if (stand == null || !stand.isValid()) {
            player.sendMessage(RED + "You don't have a dummy. Use /msc dummy spawn first.");
            return null;
        }
        return stand;
    }

    /**
     * {@code /msc dummy attack <attack|random|list> [page]}: the dummy acts out a real boss attack.
     *
     * <p>What plays is the attack the Sentinel runs in a fight — the same object, with its
     * choreography, particles and seals — only the actor is the dummy, so nothing it lands can hurt
     * anyone: {@link AttackPreview} refuses every hit an acting dummy deals.
     */
    private void attackPreview(Player player, String[] args) {
        CommandMenu menu = new CommandMenu(player);
        if (args.length < 3) {
            player.sendMessage(RED + "Usage: /msc dummy attack <attack|random|list> [page]");
            player.sendMessage(GRAY + "Names: /msc dummy attack list  ·  or /msc dummy attack random");
            return;
        }

        if (args[2].equalsIgnoreCase("list")) {
            menu.dummyAttackHelp(menu.parsePage(args, 3));
            return;
        }

        ArmorStand stand = getOrDummy(player);
        if (stand == null) return;

        ArmorStandBoss boss = plugin.getArmorStandBoss();
        if (boss == null) {
            player.sendMessage(RED + "The Sentinel's attacks are not loaded, so there is nothing to preview.");
            return;
        }

        String attack = attackFor(args[2], AttackCatalogue.names(), random);
        if (attack == null) {
            player.sendMessage(RED + "Unknown attack: " + args[2] + ". Use /msc dummy attack list.");
            return;
        }

        // A fresh instance per preview keeps one attack's state out of the next one.
        if (!boss.previewAttack(new BossInstance(stand), attack)) {
            player.sendMessage(RED + "The Sentinel cannot run " + attack + ".");
            return;
        }
        player.sendMessage(GREEN + "Previewing " + attack + " on your dummy — animation only, no damage.");
    }

    /**
     * The attack a preview should run: the named one, or a random documented attack.
     *
     * <p>Pure, so every name the dummy accepts can be checked without a server. A {@code null}
     * result means the name is not one the Sentinel knows.
     */
    static String attackFor(String requested, List<String> names, Random random) {
        if (requested == null || names.isEmpty()) return null;
        String key = requested.toLowerCase(Locale.ROOT);
        if (key.equals("random")) return names.get(random.nextInt(names.size()));
        return names.contains(key) ? key : null;
    }

    /** Attack names plus the two non-attack arguments, for tab completion. */
    static List<String> attackCompletions() {
        List<String> all = new ArrayList<>(List.of("random", "list"));
        all.addAll(AttackCatalogue.names());
        return all;
    }

    private void animate(Player player, String[] args) {
        ArmorStand stand = getOrDummy(player);
        if (stand == null) return;
        if (args.length < 3) {
            player.sendMessage(RED + "Usage: /msc dummy animate <anim>");
            player.sendMessage(GRAY + "Animations: flyup, land, airslam, shieldseal, healingcircle, rain, pentagram, triangle");
            return;
        }

        String anim = args[2].toLowerCase();
        World world = stand.getWorld();
        Location base = stand.getLocation();

        switch (anim) {
            case "flyup" -> {
                stand.setRightArmPose(new EulerAngle(Math.toRadians(-45), 0, 0));
                stand.setLeftArmPose(new EulerAngle(Math.toRadians(-45), 0, 0));
                stand.setBodyPose(new EulerAngle(Math.toRadians(-5), 0, 0));
                world.playSound(base, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
                new BukkitRunnable() {
                    int t = 0;

                    @Override
                    public void run() {
                        if (!stand.isValid()) {
                            cancel();
                            return;
                        }
                        if (t < 15) {
                            Location l = stand.getLocation();
                            world.spawnParticle(Particle.CLOUD, l.clone().add(0, -0.5, 0), 5, 1.0, 0.2, 1.0, 0.03);
                            world.spawnParticle(Particle.END_ROD, l, 3, 0.5, 0.1, 0.5, 0.02);
                            t++;
                        } else {
                            stand.setRightArmPose(new EulerAngle(0, 0, 0));
                            stand.setLeftArmPose(new EulerAngle(0, 0, 0));
                            stand.setBodyPose(new EulerAngle(0, 0, 0));
                            world.playSound(base, Sound.ENTITY_ENDER_DRAGON_FLAP, 2.0f, 0.5f);
                            new BukkitRunnable() {
                                int up = 0;

                                @Override
                                public void run() {
                                    if (!stand.isValid()) {
                                        cancel();
                                        return;
                                    }
                                    if (up >= 30) {
                                        world.spawnParticle(Particle.CLOUD, stand.getLocation(), 20, 1.5, 0.3, 1.5, 0.1);
                                        cancel();
                                        return;
                                    }
                                    Location l = stand.getLocation();
                                    l.setY(l.getY() + 0.5);
                                    stand.teleport(l);
                                    world.spawnParticle(Particle.CLOUD, l, 3, 0.3, 0.1, 0.3, 0.02);
                                    up++;
                                }
                            }.runTaskTimer(plugin, 0L, 1L);
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
                player.sendMessage(GREEN + "Playing flyup animation on dummy.");
            }
            case "land" -> {
                stand.setRightArmPose(new EulerAngle(Math.toRadians(10), 0, 0));
                stand.setLeftArmPose(new EulerAngle(Math.toRadians(10), 0, 0));
                stand.setBodyPose(new EulerAngle(Math.toRadians(5), 0, 0));
                world.spawnParticle(Particle.CLOUD, base.clone().add(0, -0.5, 0), 8, 1.0, 0.2, 1.0, 0.05);
                world.playSound(base, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.7f);
                new BukkitRunnable() {
                    int t = 0;

                    @Override
                    public void run() {
                        if (!stand.isValid()) {
                            cancel();
                            return;
                        }
                        Location l = stand.getLocation();
                        double targetY = base.getY();
                        if (t >= 30 || l.getY() - 0.5 <= targetY) {
                            l.setY(targetY);
                            stand.teleport(l);
                            stand.setRightArmPose(new EulerAngle(0, 0, 0));
                            stand.setLeftArmPose(new EulerAngle(0, 0, 0));
                            stand.setBodyPose(new EulerAngle(0, 0, 0));
                            world.spawnParticle(Particle.CLOUD, l, 20, 1.5, 0.5, 1.5, 0.1);
                            world.playSound(l, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 0.7f);
                            cancel();
                            return;
                        }
                        l.setY(Math.max(targetY, l.getY() - 0.5));
                        stand.teleport(l);
                        world.spawnParticle(Particle.CLOUD, l, 3, 0.3, 0.1, 0.3, 0.02);
                        t++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
                player.sendMessage(GREEN + "Playing land animation on dummy.");
            }
            case "airslam" -> {
                world.playSound(base, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.3f);
                new BukkitRunnable() {
                    int t = 0;

                    @Override
                    public void run() {
                        if (!stand.isValid()) {
                            cancel();
                            return;
                        }
                        Location l = stand.getLocation();
                        if (t < 25) {
                            double phase = (double) t / 20;
                            stand.setRightArmPose(new EulerAngle(Math.toRadians(-90 * Math.min(1, phase)), 0, 0));
                            stand.setLeftArmPose(new EulerAngle(Math.toRadians(-90 * Math.min(1, phase)), 0, 0));
                            stand.setBodyPose(new EulerAngle(Math.toRadians(8 * Math.min(1, phase)), 0, 0));
                            world.spawnParticle(Particle.FLAME, l, 4, 1.0, 0.5, 1.0, 0.02);
                            world.spawnParticle(Particle.CRIT, l.clone().add(0, -1, 0), 3, 0.5, 0.5, 0.5, 0.03);
                            t++;
                        } else {
                            stand.setRightArmPose(new EulerAngle(0, 0, 0));
                            stand.setLeftArmPose(new EulerAngle(0, 0, 0));
                            stand.setBodyPose(new EulerAngle(0, 0, 0));
                            world.playSound(l, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.3f);
                            world.spawnParticle(Particle.EXPLOSION, l, 5, 2.0, 0.5, 2.0, 0);
                            world.spawnParticle(Particle.CLOUD, l, 30, 3.0, 1.0, 3.0, 0.1);
                            player.sendMessage(GREEN + "AirSlam wind-up complete! (Impact animation only)");
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
                player.sendMessage(GREEN + "Playing airslam wind-up on dummy.");
            }
            case "shieldseal" -> {
                world.playSound(base, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.8f);
                new BukkitRunnable() {
                    int t = 0;

                    @Override
                    public void run() {
                        if (!stand.isValid()) {
                            cancel();
                            return;
                        }
                        Location l = stand.getLocation();
                        Location front = l.clone().add(l.getDirection().multiply(4));
                        front.setY(front.getY() + 6);

                        if (t < 25) {
                            double phase = Math.min(1.0, (double) t / 20);
                            stand.setRightArmPose(new EulerAngle(Math.toRadians(-60), Math.toRadians(30), 0));
                            stand.setLeftArmPose(new EulerAngle(Math.toRadians(-60), Math.toRadians(-30), 0));

                            int ringPts = (int) (8 + phase * 16);
                            double r = 1.5 + phase * 2.5;
                            for (int a = 0; a < ringPts; a++) {
                                double angle = (2 * Math.PI * a / ringPts);
                                double x = front.getX() + Math.cos(angle) * r;
                                double z = front.getZ() + Math.sin(angle) * r;
                                double y = front.getY() + Math.sin(angle * 2) * 1.0;
                                world.spawnParticle(Particle.DUST, new Location(world, x, y, z), 1, 0, 0, 0, 0,
                                        new Particle.DustOptions(Color.fromRGB(0x88CCFF), 2.0f * (float) phase));
                                world.spawnParticle(Particle.END_ROD, new Location(world, x, y, z), 1, 0, 0, 0, 0);
                            }
                            t++;
                        } else {
                            stand.setRightArmPose(new EulerAngle(0, 0, 0));
                            stand.setLeftArmPose(new EulerAngle(0, 0, 0));
                            world.playSound(front, Sound.ITEM_SHIELD_BLOCK, 1.5f, 1.8f);
                            world.spawnParticle(Particle.EXPLOSION, front, 3, 1.0, 1.0, 1.0, 0);
                            player.sendMessage(GREEN + "ShieldSeal casting complete! (Shield would appear here)");
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
                player.sendMessage(GREEN + "Playing shieldseal casting on dummy.");
            }
            case "healingcircle", "heal" -> {
                world.playSound(base, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1.0f, 1.2f);
                new BukkitRunnable() {
                    int t = 0;

                    @Override
                    public void run() {
                        if (!stand.isValid()) {
                            cancel();
                            return;
                        }
                        Location l = stand.getLocation();

                        if (t < 35) {
                            double phase = Math.min(1.0, (double) t / 30);
                            stand.setRightArmPose(new EulerAngle(Math.toRadians(-140 * phase), 0, 0));
                            stand.setLeftArmPose(new EulerAngle(Math.toRadians(-140 * phase), 0, 0));
                            stand.setHeadPose(new EulerAngle(Math.toRadians(-15 * phase), 0, 0));
                            stand.setBodyPose(new EulerAngle(Math.toRadians(-5 * phase), 0, 0));

                            double radius = 4.0;
                            int samples = (int) (10 + phase * 25);
                            for (int i = 0; i < samples; i++) {
                                double angle = (2 * Math.PI * i / samples) + t * 0.03;
                                double x = l.getX() + Math.cos(angle) * radius * phase;
                                double z = l.getZ() + Math.sin(angle) * radius * phase;
                                double y = l.getY() + 0.1 + Math.sin(t * 0.15 + i * 0.5) * 0.2;
                                world.spawnParticle(Particle.DUST, new Location(world, x, y, z), 1, 0, 0, 0, 0,
                                        new Particle.DustOptions(Color.fromRGB(0x44FF44), 1.2f * (float) phase));
                            }
                            for (int i = 0; i < (int) (2 + phase * 5); i++) {
                                double angle = Math.random() * Math.PI * 2;
                                double r = Math.random() * 4.0 * phase;
                                double x = l.getX() + Math.cos(angle) * r;
                                double z = l.getZ() + Math.sin(angle) * r;
                                world.spawnParticle(Particle.END_ROD, new Location(world, x, l.getY() + 0.3 + Math.random() * 2 * phase, z), 1, 0, 0, 0, 0);
                                world.spawnParticle(Particle.HEART, new Location(world, x, l.getY() + 0.3 + Math.random() * 2 * phase, z), 1, 0, 0, 0, 0);
                            }
                            t++;
                        } else {
                            stand.setRightArmPose(new EulerAngle(0, 0, 0));
                            stand.setLeftArmPose(new EulerAngle(0, 0, 0));
                            stand.setHeadPose(new EulerAngle(0, 0, 0));
                            stand.setBodyPose(new EulerAngle(0, 0, 0));
                            world.playSound(l, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.6f);
                            world.spawnParticle(Particle.EXPLOSION, l.clone().add(0, 0.5, 0), 8, 2.0, 0.5, 2.0, 0);
                            player.sendMessage(GREEN + "HealingCircle casting complete! (Circle would heal here)");
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
                player.sendMessage(GREEN + "Playing healingcircle casting on dummy.");
            }
            case "rain" -> {
                new BukkitRunnable() {
                    int t = 0;

                    @Override
                    public void run() {
                        if (!stand.isValid()) {
                            cancel();
                            return;
                        }
                        if (t < 30) {
                            double phase = Math.min(1.0, (double) t / 25);
                            stand.setRightArmPose(new EulerAngle(
                                    Math.toRadians(-180 + 90 * phase),
                                    Math.toRadians(10 * phase),
                                    Math.toRadians(20 * phase)
                            ));
                            if (t == 0) world.playSound(base, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.5f);

                            for (int pi = 0; pi < 3; pi++) {
                                Location sp = base.clone().add((Math.random() - 0.5) * 6, 20, (Math.random() - 0.5) * 6);
                                world.spawnParticle(Particle.END_ROD, sp, (int) (1 + phase * 2), 0.3, 0.3, 0.3, 0.01);
                                if (t % 5 == 0) {
                                    for (int a = 0; a < (int) (4 * phase); a++) {
                                        double ang = (2 * Math.PI * a / 4);
                                        double r2 = 0.5 + phase * 1.0;
                                        double x2 = sp.getX() + Math.cos(ang) * r2;
                                        double z2 = sp.getZ() + Math.sin(ang) * r2;
                                        world.spawnParticle(Particle.DUST, new Location(world, x2, sp.getY(), z2), 1, 0, 0, 0, 0,
                                                new Particle.DustOptions(Color.fromRGB(0xFFAA00), 1.2f * (float) phase));
                                    }
                                }
                            }
                            t++;
                        } else {
                            stand.setRightArmPose(new EulerAngle(0, 0, 0));
                            world.playSound(base, Sound.ENTITY_ENDER_DRAGON_FLAP, 1.5f, 0.8f);
                            player.sendMessage(GREEN + "Rain of Lances wind-up complete! (Lances would fall here)");
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
                player.sendMessage(GREEN + "Playing rain wind-up on dummy.");
            }
            case "pentagram" -> {
                world.playSound(base, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.6f);
                MagicSealListener seals = plugin.getMagicSealListener();
                if (seals != null) {
                    seals.spawnPentagramSeal(base.clone().add(0, 5, 0), 60, MagicSealListener.Plane.XZ);
                    player.sendMessage(GREEN + "Playing pentagram seal animation above dummy.");
                } else {
                    player.sendMessage(RED + "MagicSealListener not available.");
                }
            }
            case "trianglecall", "triangle" -> {
                world.playSound(base, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 0.9f);
                MagicSealListener seals = plugin.getMagicSealListener();
                if (seals != null) {
                    ArmorStand marker = world.spawn(base.clone().add(5, 0, 0), ArmorStand.class);
                    if (marker != null) {
                        marker.setVisible(false);
                        marker.setGravity(false);
                        marker.setMarker(true);
                        marker.setCustomNameVisible(false);
                        seals.spawnRunicTriangleSeal(marker, 80, MagicSealListener.Plane.YZ);
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (marker.isValid()) marker.remove();
                        }, 85);
                        // Also spawn one on the other side
                        ArmorStand marker2 = world.spawn(base.clone().add(-5, 0, 0), ArmorStand.class);
                        if (marker2 != null) {
                            marker2.setVisible(false);
                            marker2.setGravity(false);
                            marker2.setMarker(true);
                            marker2.setCustomNameVisible(false);
                            seals.spawnRunicTriangleSeal(marker2, 80, MagicSealListener.Plane.YZ);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                if (marker2.isValid()) marker2.remove();
                            }, 85);
                        }
                        player.sendMessage(GREEN + "Playing triangle seal animation on both sides of dummy.");
                    }
                } else {
                    player.sendMessage(RED + "MagicSealListener not available.");
                }
            }
            default ->
                    player.sendMessage(RED + "Unknown animation: " + anim + ". Use: flyup, land, airslam, shieldseal, healingcircle, rain, pentagram, triangle");
        }
    }

    private void setPose(Player player, String[] args) {
        if (args.length < 6) {
            player.sendMessage(RED + "Usage: /msc dummy set <part> <x> <y> <z>");
            player.sendMessage(GRAY + "Example: /msc dummy set rightarm -75 0 -15");
            return;
        }

        ArmorStand stand = getOrDummy(player);
        if (stand == null) return;

        String part = args[2].toLowerCase();
        double x, y, z;
        try {
            x = Math.toRadians(Double.parseDouble(args[3]));
            y = Math.toRadians(Double.parseDouble(args[4]));
            z = Math.toRadians(Double.parseDouble(args[5]));
        } catch (NumberFormatException e) {
            player.sendMessage(RED + "Invalid number. Use degrees (e.g., -75 0 -15).");
            return;
        }

        EulerAngle angle = new EulerAngle(x, y, z);
        switch (part) {
            case "rightarm" -> stand.setRightArmPose(angle);
            case "leftarm" -> stand.setLeftArmPose(angle);
            case "body" -> stand.setBodyPose(angle);
            case "head" -> stand.setHeadPose(angle);
            case "rightleg" -> stand.setRightLegPose(angle);
            case "leftleg" -> stand.setLeftLegPose(angle);
            default -> {
                player.sendMessage(RED + "Unknown part: " + part + ". Use: rightarm, leftarm, body, head, rightleg, leftleg");
                return;
            }
        }

        player.sendMessage(GREEN + "Set " + part + " to (" + args[3] + ", " + args[4] + ", " + args[5] + ") degrees.");
    }

    private void adjustPose(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(RED + "Usage: /msc dummy <part> <axis> <degrees>");
            player.sendMessage(GRAY + "Example: /msc dummy rightarm x 10  (adds 10° pitch to right arm)");
            return;
        }

        ArmorStand stand = getOrDummy(player);
        if (stand == null) return;

        String part = args[1].toLowerCase();
        String axis = args[2].toLowerCase();
        double delta;
        try {
            delta = Math.toRadians(Double.parseDouble(args[3]));
        } catch (NumberFormatException e) {
            player.sendMessage(RED + "Invalid number. Use degrees (e.g., 10 or -5).");
            return;
        }

        EulerAngle current = switch (part) {
            case "rightarm" -> stand.getRightArmPose();
            case "leftarm" -> stand.getLeftArmPose();
            case "body" -> stand.getBodyPose();
            case "head" -> stand.getHeadPose();
            case "rightleg" -> stand.getRightLegPose();
            case "leftleg" -> stand.getLeftLegPose();
            default -> {
                player.sendMessage(RED + "Unknown part: " + part + ". Use: rightarm, leftarm, body, head, rightleg, leftleg");
                yield null;
            }
        };

        if (current == null) return;

        double newX = current.getX();
        double newY = current.getY();
        double newZ = current.getZ();

        switch (axis) {
            case "x", "pitch" -> newX += delta;
            case "y", "yaw" -> newY += delta;
            case "z", "roll" -> newZ += delta;
            default -> {
                player.sendMessage(RED + "Unknown axis: " + axis + ". Use: x (pitch), y (yaw), or z (roll).");
                return;
            }
        }

        EulerAngle newAngle = new EulerAngle(newX, newY, newZ);
        switch (part) {
            case "rightarm" -> stand.setRightArmPose(newAngle);
            case "leftarm" -> stand.setLeftArmPose(newAngle);
            case "body" -> stand.setBodyPose(newAngle);
            case "head" -> stand.setHeadPose(newAngle);
            case "rightleg" -> stand.setRightLegPose(newAngle);
            case "leftleg" -> stand.setLeftLegPose(newAngle);
        }

        player.sendMessage(GREEN + "Adjusted " + part + " " + axis + " by " + args[3] + "°.");
        player.sendMessage(GRAY + "New " + part + " pose: (" +
                String.format("%.1f", Math.toDegrees(newX)) + "°, " +
                String.format("%.1f", Math.toDegrees(newY)) + "°, " +
                String.format("%.1f", Math.toDegrees(newZ)) + "°)");
    }
}
