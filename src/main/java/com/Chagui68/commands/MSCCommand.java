package com.Chagui68.commands;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.Kinger;
import com.Chagui68.entities.KingerModel;
import com.Chagui68.entities.boss.ArmorStandBoss;
import com.Chagui68.entities.boss.BossDamageSample;
import com.Chagui68.entities.boss.JackModel;
import com.Chagui68.entities.boss.JackStarBoss;
import com.Chagui68.entities.boss.NixBoss;
import com.Chagui68.entities.boss.NixModel;
import com.Chagui68.entities.boss.PenetratingHit;
import com.Chagui68.entities.handler.MobHandler;
import com.Chagui68.music.MusicDisc;
import com.Chagui68.utils.MscGeometryOverlay;
import com.Chagui68.utils.MscLog;
import com.Chagui68.utils.MscText;
import com.Chagui68.utils.MscWorldPolicy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

/**
 * {@code /msc} — the admin command of MultiverseCreatures.
 *
 * <p>This class only does three things: gate the permission, route a sub-command to its handler and
 * drive tab completion. Everything else lives next to it:
 * <ul>
 *   <li>{@link SpawnCatalogue} / {@link GiveCatalogue} / {@link AttackCatalogue} — the data tables
 *       (aliases, items, help text), so the lists are declared once instead of per branch;</li>
 *   <li>{@link CommandMenu} — every rendered menu and the pagination maths;</li>
 *   <li>{@link DummyStudio} / {@link SealStudio} — the two biggest subsystems;</li>
 *   <li>{@link MscKillFilter} — the pure "is this one of ours?" predicates.</li>
 * </ul>
 */
public class MSCCommand implements CommandExecutor, TabCompleter {

    /** Permission node that gates every /msc subcommand when commands.permission is unset. */
    private static final String DEFAULT_COMMAND_PERMISSION = "msc.admin";

    /** Sub-commands shown by {@code /msc} and offered by tab completion. */
    private static final List<String> SUB_COMMANDS = List.of(
            "spawn", "give", "attack", "music", "cleanstands", "kill", "debug", "reload", "seal", "dummy",
            "dimtp");

    /** How far {@code /msc debug} looks for the player the executor is aiming at. */
    private static final int DEBUG_TARGET_RANGE = 30;

    private static final List<String> MUSIC_ACTIONS = List.of("play", "stop", "list", "disc");

    /** Suggested kill radii, cheapest to most destructive. */
    private static final List<String> KILL_RADII = List.of("10", "25", "50", "100", "200");

    private static final List<String> GIVE_TARGETS = List.of("@a", "@p", "@r", "@s");

    /** What {@code /msc debug} can draw instead of reporting damage. */
    private static final List<String> DEBUG_ACTIONS = List.of("geometry");

    private static final List<String> GEOMETRY_TARGETS = List.of("kinger", "nix", "jack", "sentinel");

    private final MultiverseCreatures plugin;
    private final MobHandler mobHandler;
    private final DummyStudio dummyStudio;
    private final SealStudio sealStudio;

    public MSCCommand(MultiverseCreatures plugin, MobHandler mobHandler) {
        this.plugin = plugin;
        this.mobHandler = mobHandler;
        this.dummyStudio = new DummyStudio(plugin);
        this.sealStudio = new SealStudio(plugin);
    }

    /**
     * Permission gate for /msc, driven by the config instead of a bare isOp() check.
     *
     * {@code commands.permission} is the node required to run the command, and
     * {@code commands.op-only} keeps server operators working when they lack that node. Both keys
     * used to be documented in config.yml and read by nobody, while plugin.yml declared a node
     * that the executor never consulted.
     */
    private boolean canUseCommands(CommandSender sender) {
        return canUseCommands(
                plugin.getConfig().getBoolean("commands.op-only", true),
                sender.isOp(),
                sender.hasPermission(resolveCommandPermission()));
    }

    /** Pure decision behind {@link #canUseCommands}, kept separate so it can be unit tested. */
    static boolean canUseCommands(boolean opOnly, boolean senderIsOp, boolean senderHasNode) {
        if (senderHasNode) return true;
        return opOnly && senderIsOp;
    }

    /**
     * Optional per-subcommand gate, configured under {@code commands.subcommand-permissions}. A
     * server owner can pin a sensitive subcommand to its own node (e.g. {@code debug: msc.debug})
     * without a code change; an absent or blank entry leaves the subcommand open to anyone who
     * passed the main gate.
     */
    private boolean canUseSubCommand(CommandSender sender, String subCommand) {
        String node = plugin.getConfig()
                .getString("commands.subcommand-permissions." + subCommand.toLowerCase());
        if (node == null || node.isBlank()) return canUseSubCommand(false, false);
        return canUseSubCommand(true, sender.hasPermission(node.trim()));
    }

    /** Pure rule behind {@link #canUseSubCommand}, kept separate so it can be unit tested. */
    static boolean canUseSubCommand(boolean restricted, boolean senderHasNode) {
        return !restricted || senderHasNode;
    }

    /** The configured permission node, falling back to the one declared in plugin.yml. */
    private String resolveCommandPermission() {
        String node = plugin.getConfig().getString("commands.permission", DEFAULT_COMMAND_PERMISSION);
        if (node == null || node.isBlank()) return DEFAULT_COMMAND_PERMISSION;
        return node.trim();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!canUseCommands(sender)) {
            sender.sendMessage(RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length > 0 && !canUseSubCommand(sender, args[0])) {
            sender.sendMessage(RED + "You do not have permission to use /msc " + args[0].toLowerCase() + ".");
            return true;
        }

        if (args.length == 0) {
            new CommandMenu(sender).help();
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawn" -> handleSpawn(sender, args);
            case "seal" -> sealStudio.handle(sender, args);
            case "give" -> handleGive(sender, args);
            case "dummy" -> dummyStudio.handle(sender, args);
            case "dimtp" -> handleDimtp(sender, args);
            case "attack" -> handleAttack(sender, args);
            case "music" -> handleMusic(sender, args);
            case "cleanstands" -> handleCleanStands(sender, args);
            case "kill" -> handleKill(sender, args);
            case "debug" -> handleDebug(sender, args);
            case "reload" -> handleReload(sender);
            default -> {
                sender.sendMessage(RED + "Unknown command. Use /msc for help.");
                new CommandMenu(sender).help();
            }
        }

        return true;
    }

    // ------------------------------------------------------------------ spawn

    private void handleSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "Only players can spawn entities.");
            return;
        }

        CommandMenu menu = new CommandMenu(sender);
        if (args.length < 2) {
            menu.spawnHelp(1);
            return;
        }

        if (!MscWorldPolicy.isAllowed(plugin, player.getWorld())) {
            sender.sendMessage(RED + "MultiverseCreatures cannot spawn mobs in this modality.");
            return;
        }
        String type = args[1].toLowerCase();

        if (type.equals("help")) {
            menu.spawnHelp(menu.parsePage(args, 2));
            return;
        }
        if (type.matches("\\d+")) {
            menu.spawnHelp(Integer.parseInt(type));
            return;
        }

        SpawnCatalogue.Type entry = SpawnCatalogue.find(type);
        if (entry == null) {
            menu.spawnHelp(1);
            return;
        }

        boolean success = SpawnCatalogue.spawn(plugin, mobHandler, entry, player.getLocation());
        if (success) {
            sender.sendMessage(GREEN + "Spawned " + entry.spawnedName() + "!");
        } else {
            sender.sendMessage(RED + "Failed to spawn " + entry.failureName() + ".");
        }
    }

    // ------------------------------------------------------------------ give

    private void handleGive(CommandSender sender, String[] args) {
        CommandMenu menu = new CommandMenu(sender);
        if (args.length < 2) {
            menu.giveHelp(1);
            return;
        }

        String itemName = args[1].toLowerCase();
        int amount = 1;

        if (itemName.equals("help")) {
            menu.giveHelp(menu.parsePage(args, 2));
            return;
        }
        if (itemName.matches("\\d+")) {
            menu.giveHelp(Integer.parseInt(itemName));
            return;
        }

        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1 || amount > 64) {
                    sender.sendMessage(RED + "Amount must be between 1 and 64.");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(RED + "Invalid amount.");
                return;
            }
        }

        ItemStack item = GiveCatalogue.find(itemName);
        if (item == null) {
            menu.giveHelp(1);
            return;
        }

        List<Player> targets = resolveGiveTargets(sender, args.length >= 4 ? args[3] : null);
        if (targets.isEmpty()) return;

        item.setAmount(amount);
        Component givenName = item.getItemMeta().displayName();
        if (givenName == null) givenName = Component.text(item.getType().name());
        for (Player target : targets) {
            target.getInventory().addItem(item.clone());
        }
        String recipients = targets.size() == 1
                ? targets.get(0).getName() + "!"
                : targets.size() + " players!";
        // Name the given item with its own component, so the item keeps its colours instead of
        // round-tripping through the deprecated getDisplayName() string.
        sender.sendMessage(Component.text("Gave " + amount + "x ", NamedTextColor.GREEN)
                .append(givenName)
                .append(Component.text(" to " + recipients, NamedTextColor.GREEN)));
    }

    private List<Player> resolveGiveTargets(CommandSender sender, String targetArg) {
        if (targetArg == null) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(RED + "Only players can use this without a target. Specify a player or selector.");
                return List.of();
            }
            return List.of(player);
        }
        if (targetArg.startsWith("@")) {
            if (targetArg.equalsIgnoreCase("@e")) {
                sender.sendMessage(RED + "@e is not supported. Use @a to target players.");
                return List.of();
            }
            List<Entity> entities;
            try {
                entities = Bukkit.selectEntities(sender, targetArg);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(RED + "Cannot use " + targetArg + ": " + e.getMessage());
                return List.of();
            }
            List<Player> players = new ArrayList<>();
            for (Entity entity : entities) {
                if (entity instanceof Player player) players.add(player);
            }
            if (players.isEmpty()) {
                sender.sendMessage(RED + "No players matched " + targetArg + ".");
            }
            return players;
        }
        Player target = Bukkit.getPlayerExact(targetArg);
        if (target == null) {
            sender.sendMessage(RED + "Player " + targetArg + " not found or offline.");
            return List.of();
        }
        return List.of(target);
    }

    // ------------------------------------------------------------------ music

    private void handleMusic(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "Only players can use this command.");
            return;
        }

        CommandMenu menu = new CommandMenu(sender);
        if (args.length < 2) {
            menu.musicHelp();
            return;
        }

        var music = plugin.getMusicManager();

        switch (args[1].toLowerCase()) {
            case "help" -> menu.musicHelp();
            case "play" -> {
                if (args.length < 3) {
                    player.sendMessage(RED + "Usage: /msc music play <name> [loop]");
                    return;
                }
                boolean loop = args.length >= 4 && args[3].equalsIgnoreCase("loop");
                music.play(args[2], player, loop);
            }
            case "stop" -> {
                if (music.isPlaying(player)) {
                    music.stop(player);
                    player.sendMessage(GREEN + "Music stopped.");
                } else {
                    player.sendMessage(YELLOW + "No music is playing.");
                }
            }
            case "list" -> {
                var songs = music.getSongNames();
                if (songs.isEmpty()) {
                    player.sendMessage(YELLOW + "No songs available. Place .nbs files in plugins/MultiverseCreatures/music/");
                } else {
                    player.sendMessage(GOLD + "Available songs:");
                    for (String song : songs) {
                        player.sendMessage(YELLOW + " - " + song);
                    }
                }
            }
            case "disc" -> {
                if (args.length < 3) {
                    player.sendMessage(RED + "Usage: /msc music disc <name>");
                    return;
                }
                ItemStack disc = MusicDisc.create(args[2], music);
                player.getInventory().addItem(disc);
                player.sendMessage(GREEN + "Received music disc: " + GOLD + music.getSongTitle(args[2]));
            }
            default -> menu.musicHelp();
        }
    }

    // ------------------------------------------------------------------ attack

    private void handleAttack(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "Only players can use this command.");
            return;
        }

        CommandMenu menu = new CommandMenu(sender);
        if (args.length < 2) {
            menu.attackHelp(1);
            return;
        }

        String attackName = args[1].toLowerCase();
        if (attackName.equals("help")) {
            menu.attackHelp(menu.parsePage(args, 2));
            return;
        }
        if (attackName.matches("\\d+")) {
            menu.attackHelp(Integer.parseInt(attackName));
            return;
        }
        double range = 100;
        if (args.length >= 3) {
            try {
                range = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(RED + "Invalid range.");
                return;
            }
        }

        var boss = plugin.getArmorStandBoss();
        UUID bossId = boss.findNearestBoss(player.getLocation(), range);
        if (bossId == null) {
            sender.sendMessage(RED + "No boss found within " + (int) range + " blocks.");
            return;
        }
        boolean success = boss.triggerAttack(bossId, attackName);
        if (success) {
            sender.sendMessage(GREEN + "Triggered attack: " + attackName);
        } else {
            sender.sendMessage(RED + "Cannot use " + attackName + " in the boss's current state.");
        }
    }

    // ------------------------------------------------------------------ system

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        mobHandler.reloadConfig();
        if (plugin.getMahoraga() != null) plugin.getMahoraga().reloadConfig();
        if (plugin.getArmorStandBoss() != null) plugin.getArmorStandBoss().reloadConfig();
        if (plugin.getHeadSlime() != null) plugin.getHeadSlime().reloadConfig();
        if (plugin.getWarlord() != null) plugin.getWarlord().reloadConfig();
        if (plugin.getNixBoss() != null) plugin.getNixBoss().reloadConfig();
        if (plugin.getJackStarBoss() != null) plugin.getJackStarBoss().reloadConfig();
        sender.sendMessage(GREEN + "Configuration reloaded. All changes have been applied.");
    }

    private void handleCleanStands(CommandSender sender, String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("help")) {
            new CommandMenu(sender).cleanStandsHelp();
            return;
        }

        World targetWorld = null;
        if (args.length > 1) {
            String worldName = args[1];
            targetWorld = Bukkit.getWorld(worldName);
            if (targetWorld == null) {
                sender.sendMessage(RED + "World '" + worldName + "' not found.");
                return;
            }
        }

        int removed = 0;
        List<World> worldsToScan = targetWorld != null ? List.of(targetWorld) : Bukkit.getWorlds();
        for (World world : worldsToScan) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof ArmorStand stand) {
                    if (stand.getScoreboardTags().stream().anyMatch(tag -> tag.startsWith("MSC_"))) {
                        stand.remove();
                        removed++;
                    }
                }
            }
        }

        if (targetWorld != null) {
            sender.sendMessage(GREEN + "Removed " + YELLOW + removed + GREEN + " MSC armor stands from world " + targetWorld.getName() + ".");
        } else {
            sender.sendMessage(GREEN + "Removed " + YELLOW + removed + GREEN + " MSC armor stands from all loaded dimensions.");
        }
    }

    // ------------------------------------------------------------------ kill

    private void handleKill(CommandSender sender, String[] args) {
        CommandMenu menu = new CommandMenu(sender);
        if (args.length > 1 && args[1].equalsIgnoreCase("help")) {
            menu.killHelp();
            return;
        }

        String targetType = "all";
        Integer radius = null;

        if (args.length == 2) {
            if (args[1].matches("\\d+")) {
                radius = Integer.parseInt(args[1]);
            } else {
                targetType = args[1].toLowerCase();
            }
        } else if (args.length > 2) {
            targetType = args[1].toLowerCase();
            try {
                radius = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                MscLog.debug("the radius argument is not a number", e);
            }
        }

        World world = (sender instanceof Player p) ? p.getWorld() : Bukkit.getWorlds().get(0);
        Location center = (sender instanceof Player p) ? p.getLocation() : null;

        int killed = 0;
        List<Entity> toRemove = new ArrayList<>();

        for (World w : (center != null && radius != null ? List.of(world) : Bukkit.getWorlds())) {
            for (Entity entity : w.getEntities()) {
                if (center != null && radius != null) {
                    if (entity.getWorld() != center.getWorld() || center.distanceSquared(entity.getLocation()) > radius * radius) {
                        continue;
                    }
                }

                var tags = entity.getScoreboardTags();
                String entityName = MscText.plainText(entity.customName());
                if (!MscKillFilter.isMscCreature(tags, entityName)) continue;

                if (targetType.equals("all") || MscKillFilter.matchesType(targetType, tags, entityName)) {
                    toRemove.add(entity);
                }
            }
        }

        for (Entity e : toRemove) {
            e.remove();
            killed++;
        }

        sender.sendMessage(GREEN + "Removed " + YELLOW + killed + GREEN + " MSC creatures (" + targetType + ")"
                + (radius != null ? " within " + radius + " blocks." : " on the server."));
    }

    // ------------------------------------------------------------------ debug

    /**
     * Prints the bosses' last damage samples for a player, so an admin can see why the numbers came
     * out the way they did: what each boss dealt to them and what it took back. The target defaults
     * to the player the executor is looking at; a name can be passed explicitly, which is also the
     * only way to use it from the console.
     */
    /** One boss the overlay knows how to draw: a tag to find it by and the joints it swings around. */
    private record GeometryTarget(String name, String tag, List<Vector3f> joints) {
    }

    private static List<GeometryTarget> geometryTargets() {
        return List.of(
                new GeometryTarget("kinger", Kinger.TAG, List.of(
                        KingerModel.PIVOT_SHOULDER_RIGHT, KingerModel.PIVOT_SHOULDER_LEFT,
                        KingerModel.PIVOT_HIP_RIGHT, KingerModel.PIVOT_HIP_LEFT,
                        KingerModel.PIVOT_NECK, KingerModel.PIVOT_TORSO)),
                new GeometryTarget("nix", NixBoss.TAG, List.of(
                        NixModel.PIVOT_SHOULDER_RIGHT, NixModel.PIVOT_SHOULDER_LEFT,
                        NixModel.PIVOT_HIP_RIGHT, NixModel.PIVOT_HIP_LEFT,
                        NixModel.PIVOT_NECK, NixModel.PIVOT_TORSO)),
                new GeometryTarget("jack", JackStarBoss.TAG, List.of(
                        JackModel.PIVOT_SHOULDER_RIGHT, JackModel.PIVOT_SHOULDER_LEFT,
                        JackModel.PIVOT_HIP_RIGHT, JackModel.PIVOT_HIP_LEFT,
                        JackModel.PIVOT_NECK, JackModel.PIVOT_TORSO)),
                // The Sentinel wears its armour on the stand itself, so it has no joints of its own.
                new GeometryTarget("sentinel", ArmorStandBoss.TAG, List.of()));
    }

    /**
     * Draws a boss's hitbox and limb joints in the world for a few seconds.
     *
     * <p>This is the audit made visible: every visible piece has to sit inside the red box, and every
     * limb has to hang from one of the cyan dots. It exists because the alternative was a throwaway
     * unit test that printed the numbers and had to be deleted afterwards.
     */
    private void handleGeometry(CommandSender sender, String requested) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "Geometry is drawn around a boss, so run it in game.");
            return;
        }
        String kind = (requested == null) ? "" : requested.toLowerCase(Locale.ROOT);

        ArmorStand stand = null;
        GeometryTarget found = null;
        double best = Double.MAX_VALUE;
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 32, 32, 32)) {
            if (!(entity instanceof ArmorStand candidate)) continue;
            for (GeometryTarget target : geometryTargets()) {
                if (!candidate.getScoreboardTags().contains(target.tag())) continue;
                if (!kind.isEmpty() && !target.name().equals(kind)) continue;
                double distance = candidate.getLocation().distanceSquared(player.getLocation());
                if (distance < best) {
                    best = distance;
                    stand = candidate;
                    found = target;
                }
            }
        }
        if (stand == null) {
            sender.sendMessage(YELLOW + "No " + (kind.isEmpty() ? "dressed boss" : kind)
                    + " within 32 blocks of you.");
            return;
        }

        BoundingBox box = stand.getBoundingBox();
        MscGeometryOverlay.show(plugin, stand, found.joints(), MscGeometryOverlay.DEFAULT_TICKS);
        sender.sendMessage(GREEN + "Drawing " + found.name() + " for "
                + (MscGeometryOverlay.DEFAULT_TICKS / 20) + " s: hitbox "
                + String.format("%.2f x %.2f x %.2f", box.getWidthX(), box.getHeight(), box.getWidthZ())
                + " blocks (red), " + found.joints().size() + " joints (cyan).");
    }

    private void handleDebug(CommandSender sender, String[] args) {
        CommandMenu menu = new CommandMenu(sender);
        if (args.length > 1 && args[1].equalsIgnoreCase("help")) {
            menu.debugHelp();
            return;
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("geometry")) {
            handleGeometry(sender, args.length > 2 ? args[2] : null);
            return;
        }

        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(RED + "Player " + args[1] + " not found or offline.");
                return;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(RED + "From the console, name the target: /msc debug <player>.");
                return;
            }
            Entity lookedAt = player.getTargetEntity(DEBUG_TARGET_RANGE, false);
            if (!(lookedAt instanceof Player lookedPlayer)) {
                sender.sendMessage(RED + "You are not looking at a player. Use /msc debug <player>.");
                return;
            }
            target = lookedPlayer;
        }

        ArmorStandBoss boss = plugin.getArmorStandBoss();
        PenetratingHit sentinelDealt = boss == null ? null : boss.lastPenetratingHit(target.getUniqueId());
        List<BossDamageSample> samples = plugin.getBossDamageLog().samplesFor(target.getUniqueId());
        if (sentinelDealt == null && samples.isEmpty()) {
            sender.sendMessage(YELLOW + "No boss damage recorded for " + target.getName() + " yet.");
            sender.sendMessage(GRAY + "Let the Sentinel, Nix or Jack Star hit them (or hit the boss),"
                    + " then run /msc debug again.");
            return;
        }
        menu.debugReport(target.getName(), sentinelDealt, samples,
                boss == null || boss.isPenetratingDamageEnabled());
    }

    // ------------------------------------------------------------------ dimensions

    private void handleDimtp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "Only players can use this command.");
            return;
        }
        CommandMenu menu = new CommandMenu(sender);
        if (args.length < 2 || args[1].equalsIgnoreCase("help")) {
            menu.dimtpHelp(player);
            return;
        }
        String worldName = args[1];
        World targetWorld = Bukkit.getWorld(worldName);
        if (targetWorld == null) {
            sender.sendMessage(RED + "World '" + worldName + "' not found.");
            return;
        }
        if (targetWorld.equals(player.getWorld())) {
            player.sendMessage(YELLOW + "You are already in " + worldName + ".");
            return;
        }
        Location teleportLoc = player.getLocation();
        teleportLoc.setWorld(targetWorld);
        player.teleportAsync(teleportLoc).thenAccept(success -> {
            if (success) {
                player.sendMessage(GREEN + "Teleported to " + worldName + ".");
            } else {
                player.sendMessage(RED + "Failed to teleport to " + worldName + ".");
            }
        });
    }

    // ------------------------------------------------------------------ completion

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!canUseCommands(sender)) {
            return completions;
        }

        if (args.length == 1) {
            addMatching(completions, SUB_COMMANDS, args[0]);
            return completions;
        }

        String subCommand = args[0].toLowerCase();

        if (args.length == 2) {
            if (args[1].toLowerCase().startsWith("help")) {
                completions.add("help");
            }
            switch (subCommand) {
                case "spawn" -> addMatching(completions, SpawnCatalogue.aliases(), args[1]);
                case "kill" -> {
                    List<String> killTargets = new ArrayList<>();
                    killTargets.add("all");
                    killTargets.addAll(SpawnCatalogue.aliases());
                    addMatching(completions, killTargets, args[1]);
                }
                case "give" -> addMatching(completions, GiveCatalogue.aliases(), args[1]);
                case "seal" -> addMatching(completions, SealStudio.PATTERNS, args[1]);
                case "attack" -> addMatching(completions, AttackCatalogue.names(), args[1]);
                case "music" -> addMatching(completions, MUSIC_ACTIONS, args[1]);
                case "dummy" -> addMatching(completions, DummyStudio.actionCompletions(), args[1]);
                case "debug" -> {
                    addMatching(completions, DEBUG_ACTIONS, args[1]);
                    addMatchingPlayers(completions, args[1]);
                }
                case "dimtp", "cleanstands" -> addMatching(completions, worldNames(), args[1]);
                default -> {
                }
            }
            return completions;
        }

        if (args.length == 3) {
            if (subCommand.equals("kill")) {
                addMatching(completions, KILL_RADII, args[2]);
            } else if (subCommand.equals("debug") && args[1].equalsIgnoreCase("geometry")) {
                addMatching(completions, GEOMETRY_TARGETS, args[2]);
            } else if (subCommand.equals("music")
                    && (args[1].equalsIgnoreCase("play") || args[1].equalsIgnoreCase("disc"))) {
                addMatching(completions, plugin.getMusicManager().getSongNames(), args[2]);
            } else if (subCommand.equals("dummy")) {
                String action = args[1].toLowerCase();
                if (action.equals("set")) {
                    addMatching(completions, DummyStudio.PARTS, args[2]);
                } else if (action.equals("animate")) {
                    addMatching(completions, DummyStudio.ANIMATIONS, args[2]);
                } else if (action.equals("attack")) {
                    addMatching(completions, DummyStudio.attackCompletions(), args[2]);
                } else if (DummyStudio.PARTS.contains(action)) {
                    addMatching(completions, DummyStudio.AXES, args[2]);
                }
            }
            return completions;
        }

        if (args.length == 4 && subCommand.equals("give")) {
            addMatching(completions, GIVE_TARGETS, args[3]);
            addMatchingPlayers(completions, args[3]);
        }

        return completions;
    }

    /** Case-insensitive prefix filter that keeps the candidate list's order. */
    private static void addMatching(List<String> completions, List<String> candidates, String prefix) {
        String needle = prefix.toLowerCase();
        for (String candidate : candidates) {
            if (candidate.toLowerCase().startsWith(needle)) {
                completions.add(candidate);
            }
        }
    }

    /** Adds every online player whose name starts with {@code prefix}, keeping the server order. */
    private static void addMatchingPlayers(List<String> completions, String prefix) {
        String needle = prefix.toLowerCase();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getName().toLowerCase().startsWith(needle)) {
                completions.add(online.getName());
            }
        }
    }

    private static List<String> worldNames() {
        List<String> worlds = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            worlds.add(world.getName());
        }
        return worlds;
    }
}
