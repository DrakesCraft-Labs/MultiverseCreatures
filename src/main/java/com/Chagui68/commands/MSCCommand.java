package com.Chagui68.commands;

import com.Chagui68.items.armor.EightHandledWheel;
import com.Chagui68.items.armor.ObsidianBastion;
import com.Chagui68.items.components.BoneMarrow;
import com.Chagui68.items.components.ChaosCore;
import com.Chagui68.items.components.ChaosFragment;
import com.Chagui68.items.components.ChaosOrb;
import com.Chagui68.items.components.ChaosPowder;
import com.Chagui68.items.components.CompressedGoldBlock;
import com.Chagui68.items.components.CondensedChaosOrb;
import com.Chagui68.items.components.EnderFragment;
import com.Chagui68.items.components.FrostHeart;
import com.Chagui68.items.components.HeadSlimeHeart;
import com.Chagui68.items.components.MagmaCore;
import com.Chagui68.items.components.MoltenMarrow;
import com.Chagui68.items.components.MoltenNetherite;
import com.Chagui68.items.components.MoltenWheelCore;
import com.Chagui68.items.components.MultiversalCore;
import com.Chagui68.items.components.MilitaryComponent;
import com.Chagui68.items.components.ObsidianShard;
import com.Chagui68.items.components.OssifiedPlate;
import com.Chagui68.items.components.ReaperCore;
import com.Chagui68.items.components.ReaperEssence;
import com.Chagui68.items.components.RefinedNetherite;
import com.Chagui68.items.components.RefinedWheelCore;
import com.Chagui68.items.components.ReinforcedBone;
import com.Chagui68.items.components.SentinelCore;
import com.Chagui68.items.components.ReinforcedBoneBlock;
import com.Chagui68.items.components.EnderCore;
import com.Chagui68.items.components.ShadowCloak;
import com.Chagui68.items.components.StarCore;
import com.Chagui68.items.components.StormCrystal;
import com.Chagui68.items.components.SwordMold;
import com.Chagui68.items.components.VenomGland;
import com.Chagui68.items.components.VoidEssence;
import com.Chagui68.items.components.WheelCore;
import com.Chagui68.items.components.WheelEssence;
import com.Chagui68.items.dio.DioStandHead;
import com.Chagui68.items.food.HeadSlimeGelatin;
import com.Chagui68.items.food.ScoobyCookie;
import com.Chagui68.items.misc.IceCrown;
import com.Chagui68.items.misc.MantisClaws;
import com.Chagui68.items.misc.MilitaryMine;
import com.Chagui68.items.misc.WirtsLantern;
import com.Chagui68.items.misc.offhand.FrostHeartOffhand;
import com.Chagui68.items.misc.offhand.MarrowAegis;
import com.Chagui68.items.misc.offhand.VeilwalkerMantle;
import com.Chagui68.items.weapons.magic.ChaosForge;
import com.Chagui68.items.weapons.magic.SentinelGrimoire;
import com.Chagui68.items.weapons.magic.SkyfireTalisman;
import com.Chagui68.items.weapons.melee.CinderGreatsword;
import com.Chagui68.items.weapons.melee.Excalibur;
import com.Chagui68.items.weapons.melee.NullshearEdge;
import com.Chagui68.items.weapons.melee.SoulreapScythe;
import com.Chagui68.items.weapons.melee.Venomfang;
import com.Chagui68.items.weapons.ranged.AetherPullshot;
import com.Chagui68.music.MusicDisc;
import com.Chagui68.entities.miniboss.Mahoraga;
import com.Chagui68.entities.boss.MagicSealListener;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.Chagui68.entities.handler.MobHandler;
import com.Chagui68.MultiverseCreatures;

import static org.bukkit.ChatColor.*;

public class MSCCommand implements CommandExecutor, TabCompleter {

    private final MultiverseCreatures plugin;
    private final MobHandler mobHandler;
    private final Map<UUID, ArmorStand> playerDummies = new HashMap<>();
    private final Map<UUID, BukkitRunnable> dummyWingTasks = new HashMap<>();

    private static final List<String> SPAWNABLE_ENTITIES = Arrays.asList(
            "armorstand", "merchant", "dio", "creeperjr", "headslime", "zombietrap", "tank",
            "duelist", "lancer", "camel", "sniper", "mahoraga", "shadowrogue", "flameelemental",
            "frostgolem", "voidcrawler", "stormcaller", "boneshield", "venomwitch",
            "obsidianguard", "soulreaper", "chaosmage", "enderknight", "kinger", "disctrader"
    );

    public MSCCommand(MultiverseCreatures plugin, MobHandler mobHandler) {
        this.plugin = plugin;
        this.mobHandler = mobHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "spawn":
                handleSpawn(sender, args);
                break;
            case "seal":
                handleSeal(sender, args);
                break;
            case "give":
                handleGive(sender, args);
                break;
            case "dummy":
                handleDummy(sender, args);
                break;
            case "dimtp":
                handleDimtp(sender, args);
                break;
            case "attack":
                handleAttack(sender, args);
                break;
            case "music":
                handleMusic(sender, args);
                break;
            case "cleanstands":
                handleCleanStands(sender);
                break;
            default:
                sender.sendMessage(RED + "Unknown command. Use /msc for help.");
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(RED + "Only players can spawn entities.");
            return;
        }

        if (args.length < 2) {
            sendSpawnHelp(sender, 1);
            return;
        }

        Player p = (Player) sender;
        String type = args[1].toLowerCase();

        if (type.equals("help")) {
            sendSpawnHelp(sender, parseHelpPage(args, 2, sender));
            return;
        }
        if (type.matches("\\d+")) {
            sendSpawnHelp(sender, Integer.parseInt(type));
            return;
        }

        switch (type) {
            case "merchant" -> {
                mobHandler.spawnShaggy(p.getLocation());
                sender.sendMessage(GREEN + "Spawned Multiverse Merchant!");
            }
            case "dio" -> {
                boolean success = plugin.getDioBoss().trySpawnDio(p.getLocation());
                if (success) {
                    sender.sendMessage(GREEN + "Spawned Dio Brando!");
                } else {
                    sender.sendMessage(RED + "Failed to spawn Dio Brando.");
                }
            }
            case "creeperjr" -> {
                boolean success = plugin.getCreeperJr().trySpawn(p.getLocation());
                if (success) {
                    sender.sendMessage(GREEN + "Spawned Creeper Jr.!");
                } else {
                    sender.sendMessage(RED + "Failed to spawn Creeper Jr.");
                }
            }
            case "headslime" -> {
                boolean success = plugin.getHeadSlime().trySpawn(p.getLocation());
                if (success) {
                    sender.sendMessage(GREEN + "Spawned Head Slime!");
                } else {
                    sender.sendMessage(RED + "Failed to spawn Head Slime.");
                }
            }
            case "zombietrap", "army" -> {
                boolean success = plugin.getZombieHorseTrap().trySpawn(p.getLocation());
                if (success) {
                    sender.sendMessage(GREEN + "Spawned Military Zombie Horse trap!");
                } else {
                    sender.sendMessage(RED + "Failed to spawn trap.");
                }
            }
            case "tank" -> {
                boolean success = plugin.getZombieHorseTrap().trySpawnTank(p.getLocation());
                if (success) {
                    sender.sendMessage(GREEN + "Spawned Zombie Tank!");
                } else {
                    sender.sendMessage(RED + "Failed to spawn Zombie Tank.");
                }
            }
            case "duelist" -> {
                boolean success = plugin.getZombieHorseTrap().trySpawnDuelist(p.getLocation());
                if (success) {
                    sender.sendMessage(GREEN + "Spawned Military Skeleton Duelist!");
                } else {
                    sender.sendMessage(RED + "Failed to spawn Duelist.");
                }
            }
            case "lancer" -> {
                boolean success = plugin.getZombieHorseTrap().trySpawnLancer(p.getLocation());
                if (success) {
                    sender.sendMessage(GREEN + "Spawned Zombie Lancer on horse!");
                } else {
                    sender.sendMessage(RED + "Failed to spawn Lancer.");
                }
            }
            case "camel" -> {
                boolean success = plugin.getZombieHorseTrap().trySpawnCamel(p.getLocation());
                if (success) {
                    sender.sendMessage(GREEN + "Spawned Camel with riders!");
                } else {
                    sender.sendMessage(RED + "Failed to spawn Camel.");
                }
            }
            case "sniper" -> {
                boolean success = plugin.getZombieHorseTrap().trySpawnSniper(p.getLocation());
                if (success) {
                    sender.sendMessage(GREEN + "Spawned Sniper Skeleton!");
                } else {
                    sender.sendMessage(RED + "Failed to spawn Sniper.");
                }
            }
            case "mahoraga" -> {
                boolean success = plugin.getMahoraga().trySpawn(p.getLocation());
                if (success) {
                    sender.sendMessage(GREEN + "Spawned Mahoraga!");
                } else {
                    sender.sendMessage(RED + "Failed to spawn Mahoraga.");
                }
            }
            case "armorstand", "armorstandboss" -> {
                boolean success = plugin.getArmorStandBoss().trySpawn(p.getLocation());
                if (success) {
                    sender.sendMessage(GREEN + "Spawned ArmorStand Boss!");
                } else {
                    sender.sendMessage(RED + "Failed to spawn ArmorStand Boss.");
                }
            }
            case "shadowrogue", "rogue" -> {
                boolean success = plugin.getShadowRogue().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Shadow Rogue!");
                else sender.sendMessage(RED + "Failed to spawn Shadow Rogue.");
            }
            case "flameelemental", "flame" -> {
                boolean success = plugin.getFlameElemental().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Flame Elemental!");
                else sender.sendMessage(RED + "Failed to spawn Flame Elemental.");
            }
            case "frostgolem", "frost" -> {
                boolean success = plugin.getFrostGolem().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Frost Golem!");
                else sender.sendMessage(RED + "Failed to spawn Frost Golem.");
            }
            case "voidcrawler", "void" -> {
                boolean success = plugin.getVoidCrawler().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Void Crawler!");
                else sender.sendMessage(RED + "Failed to spawn Void Crawler.");
            }
            case "stormcaller", "storm" -> {
                boolean success = plugin.getStormCaller().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Storm Caller!");
                else sender.sendMessage(RED + "Failed to spawn Storm Caller.");
            }
            case "boneshield", "bone" -> {
                boolean success = plugin.getBoneShield().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Bone Shield!");
                else sender.sendMessage(RED + "Failed to spawn Bone Shield.");
            }
            case "venomwitch", "venom" -> {
                boolean success = plugin.getVenomWitch().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Venom Witch!");
                else sender.sendMessage(RED + "Failed to spawn Venom Witch.");
            }
            case "obsidianguard", "obsidian" -> {
                boolean success = plugin.getObsidianGuard().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Obsidian Guard!");
                else sender.sendMessage(RED + "Failed to spawn Obsidian Guard.");
            }
            case "soulreaper", "reaper" -> {
                boolean success = plugin.getSoulReaper().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Soul Reaper!");
                else sender.sendMessage(RED + "Failed to spawn Soul Reaper.");
            }
            case "chaosmage", "chaos" -> {
                boolean success = plugin.getChaosMage().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Chaos Mage!");
                else sender.sendMessage(RED + "Failed to spawn Chaos Mage.");
            }
            case "enderknight", "ender" -> {
                boolean success = plugin.getEnderKnight().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Ender Knight!");
                else sender.sendMessage(RED + "Failed to spawn Ender Knight.");
            }
            case "kinger" -> {
                boolean success = plugin.getKinger().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Kinger!");
                else sender.sendMessage(RED + "Failed to spawn Kinger.");
            }
            case "disctrader" -> {
                boolean success = plugin.getDiscTrader().trySpawn(p.getLocation());
                if (success) sender.sendMessage(GREEN + "Spawned Disc Trader!");
                else sender.sendMessage(RED + "Failed to spawn Disc Trader.");
            }
            default -> sendSpawnHelp(sender, 1);
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(RED + "Only players can receive items.");
            return;
        }

        if (args.length < 2) {
            sendGiveHelp(sender, 1);
            return;
        }

        Player target = (Player) sender;
        String itemName = args[1].toLowerCase();
        int amount = 1;

        if (itemName.equals("help")) {
            sendGiveHelp(sender, parseHelpPage(args, 2, sender));
            return;
        }
        if (itemName.matches("\\d+")) {
            sendGiveHelp(sender, Integer.parseInt(itemName));
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

        ItemStack item = switch (itemName) {
            case "scoobycookie", "cookie" -> ScoobyCookie.SCOOBY_COOKIE.clone();
            case "excalibur", "sword" -> Excalibur.EXCALIBUR_SWORD.clone();
            case "icecrown", "crown" -> IceCrown.ICE_CROWN.clone();
            case "wirtslantern", "lantern" -> WirtsLantern.WIRTS_LANTERN.clone();
            case "starcore", "star" -> StarCore.STAR_CORE.clone();
            case "swordmold", "mold" -> SwordMold.SWORD_MOLD.clone();
            case "diostand", "dio" -> DioStandHead.getHead();
            case "mantisclaws", "claws" -> MantisClaws.MANTIS_CLAWS_ITEM.clone();
            case "militarycomponent", "component" -> MilitaryComponent.MILITARY_COMPONENT.clone();
            case "militarymine", "mine" -> MilitaryMine.MILITARY_MINE.clone();
            case "headslimeheart", "heart" -> HeadSlimeHeart.HEAD_SLIME_HEART.clone();
            case "headslimegelatin", "gelatin" -> HeadSlimeGelatin.HEAD_SLIME_GELATIN.clone();
            // Weapons
            case "aetherpullshot", "pullshot" -> AetherPullshot.AETHER_PULLSHOT.clone();
            case "chaosforge" -> ChaosForge.CHAOS_FORGE.clone();
            case "cindergreatsword", "greatsword" -> CinderGreatsword.CINDER_GREATSWORD.clone();
            case "nullshearedge", "nullshear" -> NullshearEdge.NULLSHEAR_EDGE.clone();
            case "soulreapscythe", "scythe" -> SoulreapScythe.SOULREAP_SCYTHE.clone();
            case "venomfang", "dagger" -> Venomfang.VENOMFANG.clone();
            case "skyfiretalisman", "talisman" -> SkyfireTalisman.SKYFIRE_TALISMAN.clone();
            case "sentinelgrimoire", "grimoire" -> SentinelGrimoire.GRIMOIRE.clone();
            // Armor
            case "eighthandledwheel", "wheel" -> EightHandledWheel.EIGHT_HANDLED_WHEEL.clone();
            case "obsidianbastionhelmet", "bastionhelmet" -> ObsidianBastion.HELMET.clone();
            case "obsidianbastionchestplate", "bastionchestplate" -> ObsidianBastion.CHESTPLATE.clone();
            case "obsidianbastionleggings", "bastionleggings" -> ObsidianBastion.LEGGINGS.clone();
            case "obsidianbastionboots", "bastionboots" -> ObsidianBastion.BOOTS.clone();
            // Off-hand / misc equipables
            case "frostheartoffhand", "frostoffhand" -> FrostHeartOffhand.FROST_HEART_OFFHAND.clone();
            case "marrowaegis", "aegis" -> MarrowAegis.MARROW_AEGIS.clone();
            case "veilwalkermantle", "mantle" -> VeilwalkerMantle.VEILWALKER_MANTLE.clone();
            // Components / ingredients
            case "chaosorb" -> ChaosOrb.CHAOS_ORB.clone();
            case "chaospowder" -> ChaosPowder.CHAOS_POWDER.clone();
            case "chaosfragment" -> ChaosFragment.CHAOS_FRAGMENT.clone();
            case "chaoscore" -> ChaosCore.CHAOS_CORE.clone();
            case "condensedchaosorb", "condensed" -> CondensedChaosOrb.CONDENSED_CHAOS_ORB.clone();
            case "enderfragment", "ender" -> EnderFragment.ENDER_FRAGMENT.clone();
            case "frostheart", "frost" -> FrostHeart.FROST_HEART.clone();
            case "magmacore", "magma" -> MagmaCore.MAGMA_CORE.clone();
            case "obsidianshard", "shard" -> ObsidianShard.OBSIDIAN_SHARD.clone();
            case "reaperessence", "reaper" -> ReaperEssence.REAPER_ESSENCE.clone();
            case "reinforcedbone", "bone" -> ReinforcedBone.REINFORCED_BONE.clone();
            case "reinforcedboneblock" -> ReinforcedBoneBlock.REINFORCED_BONE_BLOCK.clone();
            case "bonemarrow", "marrow" -> BoneMarrow.BONE_MARROW.clone();
            case "ossifiedplate", "plate" -> OssifiedPlate.OSSIFIED_PLATE.clone();
            case "moltenmarrow" -> MoltenMarrow.MOLTEN_MARROW.clone();
            case "endercore" -> EnderCore.ENDER_CORE.clone();
            case "shadowcloak", "cloak" -> ShadowCloak.SHADOW_CLOAK.clone();
            case "stormcrystal", "storm" -> StormCrystal.STORM_CRYSTAL.clone();
            case "venomgland", "venom" -> VenomGland.VENOM_GLAND.clone();
            case "voidessence", "void" -> VoidEssence.VOID_ESSENCE.clone();
            case "wheelessence", "whelessence" -> WheelEssence.WHEEL_ESSENCE.clone();
            // Intermediate components
            case "wheelcore" -> WheelCore.WHEEL_CORE.clone();
            case "reapercore" -> ReaperCore.REAPER_CORE.clone();
            case "refinednetherite" -> RefinedNetherite.REFINED_NETHERITE.clone();
            case "sentinelcore", "sentinel" -> SentinelCore.SENTINEL_CORE.clone();
            case "multiversalcore", "multiverse" -> MultiversalCore.MULTIVERSAL_CORE.clone();
            case "compressedgoldblock", "goldblock" -> CompressedGoldBlock.COMPRESSED_GOLD_BLOCK.clone();
            case "moltenwheelcore", "moltenwheel" -> MoltenWheelCore.MOLTEN_WHEEL_CORE.clone();
            case "moltennetherite", "molten" -> MoltenNetherite.MOLTEN_NETHERITE.clone();
            case "refinedwheelcore", "refinedwheel" -> RefinedWheelCore.REFINED_WHEEL_CORE.clone();
            default -> null;
        };

        if (item == null) {
            sendGiveHelp(sender, 1);
            return;
        }

        item.setAmount(amount);
        target.getInventory().addItem(item);
        sender.sendMessage(GREEN + "Gave " + amount + "x " + item.getItemMeta().getDisplayName() + GREEN + "!");
    }

    private void handleMusic(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "Only players can use this command.");
            return;
        }
        if (args.length < 2) {
            sendMusicHelp(player);
            return;
        }

        var music = plugin.getMusicManager();

        switch (args[1].toLowerCase()) {
            case "help" -> sendMusicHelp(player);
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
                    for (String s : songs) {
                        player.sendMessage(YELLOW + " - " + s);
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
            default -> sendMusicHelp(player);
        }
    }

    private void handleAttack(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "Only players can use this command.");
            return;
        }
        if (args.length < 2) {
            sendAttackHelp(player, 1);
            return;
        }

        String attackName = args[1].toLowerCase();
        if (attackName.equals("help")) {
            sendAttackHelp(player, parseHelpPage(args, 2, sender));
            return;
        }
        if (attackName.matches("\\d+")) {
            sendAttackHelp(player, Integer.parseInt(attackName));
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

    private void handleCleanStands(CommandSender sender) {
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof ArmorStand stand)) continue;
                for (String tag : stand.getScoreboardTags()) {
                    if (tag.startsWith("MSC_")) {
                        stand.remove();
                        count++;
                        break;
                    }
                }
            }
        }
        sender.sendMessage(GREEN + "Removed " + count + " custom armor stands.");
    }

    private void handleSeal(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "Only players can spawn seals.");
            return;
        }

        if (args.length < 2) {
            sendSealHelp(player, 1);
            return;
        }

        String type = args[1].toLowerCase();
        if (type.equals("help")) {
            sendSealHelp(player, parseHelpPage(args, 2, sender));
            return;
        }
        if (type.matches("\\d+")) {
            sendSealHelp(player, Integer.parseInt(type));
            return;
        }
        final MagicSealListener.Plane plane;
        if (args.length >= 3) {
            switch (args[2].toLowerCase()) {
                case "vertical-north", "vertical", "v", "xy" -> plane = MagicSealListener.Plane.XY;
                case "vertical-east", "ez", "yz" -> plane = MagicSealListener.Plane.YZ;
                case "horizontal", "h", "xz" -> plane = MagicSealListener.Plane.XZ;
                default -> {
                    sender.sendMessage(RED + "Unknown plane. Use: horizontal, vertical-north, vertical-east");
                    return;
                }
            }
        } else {
            plane = MagicSealListener.Plane.XZ;
        }

        Location center = player.getLocation();
        MagicSealListener listener = plugin.getMagicSealListener();

        switch (type) {
            case "pentagram" -> {
                spawnTemporaryStandAndFire(player, center, (stand, ticks) -> listener.spawnPentagramSeal(stand, ticks, plane));
                sender.sendMessage(GOLD + "Spawned Pentagram Seal for 6 seconds in plane " + plane + ".");
            }
            case "triangle", "runic" -> {
                spawnTemporaryStandAndFire(player, center, (stand, ticks) -> listener.spawnRunicTriangleSeal(stand, ticks, plane));
                sender.sendMessage(GOLD + "Spawned Runic Triangle Seal for 6 seconds in plane " + plane + ".");
            }
            case "celestial" -> {
                spawnTemporaryStandAndFire(player, center, (stand, ticks) -> listener.spawnCelestialSeal(stand, ticks, plane));
                sender.sendMessage(GOLD + "Spawned Celestial Seal for 6 seconds in plane " + plane + ".");
            }
            case "circle" -> {
                drawSingleCircle(center, 5.0, Color.fromRGB(0xFFFF55), 200, 130, plane);
                sender.sendMessage(GOLD + "Drew single yellow circle (radius 5) in plane " + plane + ".");
            }
            case "ring" -> {
                drawSingleCircle(center, 8.0, Color.fromRGB(0x00FFFF), 280, 130, plane);
                sender.sendMessage(GOLD + "Drew single aqua ring (radius 8) in plane " + plane + ".");
            }
            case "star" -> {
                drawSixPointStar(center, 6.0, Color.WHITE, 100, 130, plane);
                sender.sendMessage(GOLD + "Drew six-point star (radius 6) in plane " + plane + ".");
            }
            case "floating", "shield" -> {
                listener.spawnFloatingShieldSeal(center, 120);
                sender.sendMessage(GOLD + "Spawned Floating Shield Seal for 6 seconds.");
            }
            case "wings" -> {
                listener.spawnWingSeal(center, player.getLocation().getYaw(), 120);
                sender.sendMessage(GOLD + "Spawned Wing Seal for 6 seconds.");
            }
            case "wings2" -> {
                listener.spawnWingSeal2(center, player.getLocation().getYaw(), 120);
                sender.sendMessage(GOLD + "Spawned Wing Seal 2 for 6 seconds.");
            }
            case "vortex" -> {
                listener.spawnVortexSeal(center, 120);
                sender.sendMessage(GOLD + "Spawned Vortex Seal for 6 seconds.");
            }
            case "quake" -> {
                listener.spawnQuakeSeal(center, 120);
                sender.sendMessage(GOLD + "Spawned Quake Seal for 6 seconds.");
            }
            case "divine" -> {
                listener.spawnDivineSeal(center, 120);
                sender.sendMessage(GOLD + "Spawned Divine Seal for 6 seconds.");
            }
            case "storm" -> {
                listener.spawnStormSeal(center, 120);
                sender.sendMessage(GOLD + "Spawned Storm Seal for 6 seconds.");
            }
            default -> sendSealHelp(player, 1);
        }
    }

    private interface SealTask {
        void run(ArmorStand stand, int durationTicks);
    }

    private void spawnTemporaryStandAndFire(Player player, Location center, SealTask task) {
        ArmorStand marker = player.getWorld().spawn(center, ArmorStand.class, entity -> {
            entity.setVisible(false);
            entity.setGravity(false);
            entity.setMarker(true);
            entity.setCustomNameVisible(false);
        });
        marker.addScoreboardTag("MSC_SealMarker");
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> task.run(marker, 120), 1L);
        plugin.getServer().getScheduler().runTaskLater(plugin, marker::remove, 130L);
    }

    private void drawSingleCircle(Location center, double radius, org.bukkit.Color color, int samples, int ticks, MagicSealListener.Plane plane) {
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= ticks) {
                    cancel();
                    return;
                }
                double step = (2 * Math.PI) / samples;
                World world = center.getWorld();
                for (int i = 0; i < samples; i++) {
                    double a = i * step + (t * 0.05);
                    double c = radius * Math.cos(a);
                    double s = radius * Math.sin(a);
                    double x, y, z;
                    switch (plane) {
                        case XZ -> {
                            x = center.getX() + c;
                            y = center.getY() + 0.05;
                            z = center.getZ() + s;
                        }
                        case XY -> {
                            x = center.getX() + c;
                            y = center.getY() + s;
                            z = center.getZ();
                        }
                        case YZ -> {
                            x = center.getX();
                            y = center.getY() + c;
                            z = center.getZ() + s;
                        }
                        default -> {
                            x = center.getX() + c;
                            y = center.getY() + 0.05;
                            z = center.getZ() + s;
                        }
                    }
                    Location loc = new Location(world, x, y, z);
                    world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(color, 1.8f));
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void drawSixPointStar(Location center, double radius, org.bukkit.Color color, int samples, int ticks, MagicSealListener.Plane plane) {
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= ticks) {
                    cancel();
                    return;
                }
                double step = (2 * Math.PI) / samples;
                World world = center.getWorld();
                for (int i = 0; i < samples; i++) {
                    double angle = i * step + (t * 0.03);
                    double r = radius * (i % (samples / 6) == 0 ? 1.0 : 0.55);
                    double x, y, z;
                    switch (plane) {
                        case XZ -> {
                            x = center.getX() + r * Math.cos(angle);
                            y = center.getY() + 0.1;
                            z = center.getZ() + r * Math.sin(angle);
                        }
                        case XY -> {
                            x = center.getX() + r * Math.cos(angle);
                            y = center.getY() + r * Math.sin(angle);
                            z = center.getZ();
                        }
                        case YZ -> {
                            x = center.getX();
                            y = center.getY() + r * Math.cos(angle);
                            z = center.getZ() + r * Math.sin(angle);
                        }
                        default -> {
                            x = center.getX() + r * Math.cos(angle);
                            y = center.getY() + 0.1;
                            z = center.getZ() + r * Math.sin(angle);
                        }
                    }
                    Location loc = new Location(world, x, y, z);
                    world.spawnParticle(Particle.DUST, loc, 1, 0, 0, 0, 0,
                            new Particle.DustOptions(color, 1.8f));
                }
                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void handleDummy(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "Only players can use this command.");
            return;
        }

        if (args.length < 2) {
            sendDummyHelp(player, 1);
            return;
        }

        if (args[1].equalsIgnoreCase("help")) {
            sendDummyHelp(player, parseHelpPage(args, 2, sender));
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "spawn" -> spawnDummy(player);
            case "remove" -> removeDummy(player);
            case "set" -> dummySetPose(player, args);
            case "wings" -> dummyWings(player);
            case "wings2" -> dummyWings2(player);
            case "nowings" -> dummyNoWings(player);
            case "animate" -> dummyAnimate(player, args);
            default -> dummyAdjustPose(player, args);
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
            s.setCustomName(LIGHT_PURPLE + "Pose Dummy");
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

        org.bukkit.attribute.AttributeInstance scaleAttr = stand.getAttribute(Attribute.SCALE);
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

        stand.addScoreboardTag("MSC_Dummy");

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

    private void dummyWings(Player player) {
        ArmorStand stand = playerDummies.get(player.getUniqueId());
        if (stand == null || !stand.isValid()) {
            player.sendMessage(RED + "You don't have a dummy. Use /msc dummy spawn first.");
            return;
        }
        cancelDummyWings(player);
        if (plugin.getMagicSealListener() != null) {
            BukkitRunnable task = plugin.getMagicSealListener().spawnWingSeal(stand);
            if (task != null) dummyWingTasks.put(player.getUniqueId(), task);
            player.sendMessage(GREEN + "Wing seal added to your dummy.");
        } else {
            player.sendMessage(RED + "MagicSealListener not available.");
        }
    }

    private void dummyWings2(Player player) {
        ArmorStand stand = playerDummies.get(player.getUniqueId());
        if (stand == null || !stand.isValid()) {
            player.sendMessage(RED + "You don't have a dummy. Use /msc dummy spawn first.");
            return;
        }
        cancelDummyWings(player);
        if (plugin.getMagicSealListener() != null) {
            BukkitRunnable task = plugin.getMagicSealListener().spawnWingSeal2(stand);
            if (task != null) dummyWingTasks.put(player.getUniqueId(), task);
            player.sendMessage(GREEN + "Wing seal 2 added to your dummy.");
        } else {
            player.sendMessage(RED + "MagicSealListener not available.");
        }
    }

    private void dummyNoWings(Player player) {
        if (!playerDummies.containsKey(player.getUniqueId())) {
            player.sendMessage(RED + "You don't have a dummy. Use /msc dummy spawn first.");
            return;
        }
        cancelDummyWings(player);
        player.sendMessage(GREEN + "Wing effects removed from your dummy.");
    }

    private void cancelDummyWings(Player player) {
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

    private void dummyAnimate(Player player, String[] args) {
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
                    ArmorStand marker = (ArmorStand) world.spawnEntity(base.clone().add(5, 0, 0), EntityType.ARMOR_STAND);
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
                        ArmorStand marker2 = (ArmorStand) world.spawnEntity(base.clone().add(-5, 0, 0), EntityType.ARMOR_STAND);
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

    private void dummySetPose(Player player, String[] args) {
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

    private void dummyAdjustPose(Player player, String[] args) {
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

    private void handleDimtp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(RED + "Only players can use this command.");
            return;
        }
        if (args.length < 2 || args[1].equalsIgnoreCase("help")) {
            sendDimtpHelp(player);
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

    private void sendLine(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    private void sendMenuHeader(CommandSender sender, String title) {
        sendLine(sender, "");
        sendLine(sender, "&8&m" + "-".repeat(40));
        sendLine(sender, " &6&l★ &e&l" + title + " &6&l★");
        sendLine(sender, "&8&m" + "-".repeat(40));
    }

    private void sendMenuFooter(CommandSender sender) {
        sendLine(sender, "&8&m" + "-".repeat(40));
    }

    private void sendCommandEntry(CommandSender sender, String command, String description) {
        sendLine(sender, " &e&l" + command);
        sendLine(sender, "    &7" + description);
    }

    private static final int HELP_LINES_PER_PAGE = 12;

    private void sendPaginatedMenu(CommandSender sender, String title, String usage, List<String> lines, int page, String navCommand) {
        int totalPages = Math.max(1, (int) Math.ceil(lines.size() / (double) HELP_LINES_PER_PAGE));
        page = Math.max(1, Math.min(page, totalPages));
        sendMenuHeader(sender, title);
        if (usage != null) {
            sendLine(sender, " &7Usage: &e" + usage);
            sendLine(sender, "");
        }
        int start = (page - 1) * HELP_LINES_PER_PAGE;
        for (int i = start; i < Math.min(lines.size(), start + HELP_LINES_PER_PAGE); i++) {
            sendLine(sender, lines.get(i));
        }
        sendMenuFooter(sender);
        if (totalPages > 1) {
            String next = page < totalPages ? " &8· &7Use &e/msc " + navCommand + " help " + (page + 1) : "";
            sendLine(sender, " &7Page &e" + page + "&7/&e" + totalPages + next);
        }
    }

    private int parseHelpPage(String[] args, int index, CommandSender sender) {
        if (args.length > index) {
            try {
                int page = Integer.parseInt(args[index]);
                if (page >= 1) {
                    return page;
                }
            } catch (NumberFormatException ignored) {
            }
            sender.sendMessage(RED + "Invalid page. Use a number >= 1.");
        }
        return 1;
    }

    private List<String> categoryLines(String label, List<String> items) {
        List<String> lines = new ArrayList<>();
        lines.add(" &6&l" + label + "&8:");
        for (String item : items) {
            lines.add("   &e• &f" + item);
        }
        return lines;
    }

    private void sendSpawnHelp(CommandSender sender, int page) {
        List<String> lines = new ArrayList<>();
        lines.addAll(categoryLines("Entities", SPAWNABLE_ENTITIES));
        sendPaginatedMenu(sender, "MSC SPAWN", "/msc spawn <type>", lines, page, "spawn");
    }

    private void sendGiveHelp(CommandSender sender, int page) {
        List<String> lines = new ArrayList<>();
        lines.addAll(categoryLines("Food", Arrays.asList("scoobycookie")));
        lines.addAll(categoryLines("Weapons", Arrays.asList("excalibur", "aetherpullshot", "chaosforge",
                "cindergreatsword", "nullshearedge", "soulreapscythe", "venomfang", "skyfiretalisman",
                "sentinelgrimoire")));
        lines.addAll(categoryLines("Armor", Arrays.asList("eighthandledwheel", "obsidianbastionhelmet",
                "obsidianbastionchestplate", "obsidianbastionleggings", "obsidianbastionboots")));
        lines.addAll(categoryLines("Equipables", Arrays.asList("diostand", "icecrown", "wirtslantern", "mantisclaws",
                "militarymine", "frostheartoffhand", "marrowaegis", "veilwalkermantle")));
        lines.addAll(categoryLines("Components", Arrays.asList("starcore", "militarycomponent", "headslimeheart",
                "headslimegelatin", "chaosorb", "chaospowder", "chaosfragment", "chaoscore", "condensedchaosorb",
                "enderfragment", "frostheart", "magmacore", "obsidianshard",
                "reaperessence", "reinforcedbone", "shadowcloak", "stormcrystal", "venomgland", "voidessence",
                "wheelessence", "wheelcore", "reapercore", "refinednetherite", "swordmold",
                "reinforcedboneblock", "endercore", "sentinelcore", "multiversalcore", "compressedgoldblock",
                "moltenwheelcore", "moltennetherite", "refinedwheelcore")));
        sendPaginatedMenu(sender, "MSC GIVE", "/msc give <item> [amount]", lines, page, "give");
    }

    private void sendSealHelp(CommandSender sender, int page) {
        List<String> lines = new ArrayList<>();
        lines.addAll(categoryLines("Patterns", Arrays.asList("pentagram", "triangle", "celestial", "circle",
                "ring", "star", "floating", "wings", "wings2", "vortex", "quake", "divine", "storm")));
        lines.addAll(categoryLines("Planes", Arrays.asList("horizontal (default)", "vertical-north", "vertical-east")));
        sendPaginatedMenu(sender, "MSC SEAL", "/msc seal <pattern> [plane]", lines, page, "seal");
    }

    private void sendAttackHelp(CommandSender sender, int page) {
        List<String> lines = new ArrayList<>();
        lines.addAll(categoryLines("Ground", Arrays.asList("groundslam", "groundshatter", "shieldbash", "lancestorm",
                "earthpillar", "chaingrapple", "warstomp", "armorspikes", "vortexpull", "mirrorimage", "doombeam")));
        lines.addAll(categoryLines("Aerial", Arrays.asList("starfall", "aerialrush", "sonicboom", "lightningstorm",
                "gravitywell", "crossslash", "novaburst", "darkorb", "windcutter", "heavenlyjudgment",
                "rainoflances", "airslam", "hoverbarrage")));
        lines.addAll(categoryLines("Ranged", Arrays.asList("lancesnipe", "meteorstorm", "voidbeam", "frostlance",
                "lightningspear", "shadowvolley", "chainlightning", "crystalbarrage", "arcaneorb", "voidrift",
                "arcanemissiles", "spiritbeam")));
        lines.addAll(categoryLines("Defensive", Arrays.asList("stoneskin", "reflectbarrier", "absorbshield",
                "shieldseal", "healingcircle", "trianglecall")));
        sendPaginatedMenu(sender, "MSC ATTACK", "/msc attack <attack> [range]", lines, page, "attack");
    }

    private void sendDummyHelp(Player player, int page) {
        List<String> lines = new ArrayList<>();
        lines.add(" &e&l/msc dummy spawn");
        lines.add("    &7Spawn a dummy armor stand (same scale/gear as boss)");
        lines.add(" &e&l/msc dummy remove");
        lines.add("    &7Remove your dummy");
        lines.add(" &e&l/msc dummy <part> <axis> <degrees>");
        lines.add("    &7Adjust pose incrementally");
        lines.add("    &7Parts: rightarm, leftarm, body, head, rightleg, leftleg");
        lines.add("    &7Axes: x (pitch), y (yaw), z (roll)  ·  Ex: /msc dummy rightarm x 10");
        lines.add(" &e&l/msc dummy set <part> <x> <y> <z>");
        lines.add("    &7Set exact pose in degrees  ·  Ex: /msc dummy set rightarm -75 0 -15");
        lines.add(" &e&l/msc dummy wings|wings2|nowings");
        lines.add("    &7Add gold/red wing seal or remove wing effects");
        lines.add(" &e&l/msc dummy animate <anim>");
        lines.add("    &7Animations: flyup, land, airslam, shieldseal, healingcircle, rain, pentagram, triangle");
        sendPaginatedMenu(player, "MSC DUMMY", "/msc dummy <action> [args]", lines, page, "dummy");
    }

    private void sendMusicHelp(CommandSender sender) {
        sendMenuHeader(sender, "MSC MUSIC");
        sendLine(sender, " &7Usage: &e/msc music <play|stop|list|disc> [name] [loop]");
        sendLine(sender, "");
        sendLine(sender, " &6&lActions&8:");
        sendLine(sender, "   &e• &fplay <name> [loop]");
        sendLine(sender, "      &7Play a song from plugins/MultiverseCreatures/music/ (.nbs)");
        sendLine(sender, "   &e• &fstop");
        sendLine(sender, "      &7Stop the song currently playing");
        sendLine(sender, "   &e• &flist");
        sendLine(sender, "      &7List all available songs");
        sendLine(sender, "   &e• &fdisc <name>");
        sendLine(sender, "      &7Get the jukebox music disc of a song");
        sendMenuFooter(sender);
    }

    private void sendDimtpHelp(Player player) {
        sendMenuHeader(player, "MSC DIMTP");
        sendLine(player, " &7Usage: &e/msc dimtp <world>");
        sendLine(player, "");
        sendLine(player, " &6&lInfo&8:");
        sendLine(player, "   &e• &fworld");
        sendLine(player, "      &7Teleport to a loaded dimension, keeping coordinates");
        StringBuilder worlds = new StringBuilder();
        for (World w : Bukkit.getWorlds()) {
            if (worlds.length() > 0) worlds.append("&8, &f");
            worlds.append(w.getName());
        }
        sendLine(player, "      &7Worlds: &f" + worlds);
        sendMenuFooter(player);
    }

    private void sendHelp(CommandSender sender) {
        sendMenuHeader(sender, "MULTIVERSE CREATURES");
        sendCommandEntry(sender, "/msc spawn <type>", "Spawn custom mobs. Use /msc spawn alone to list them.");
        sendCommandEntry(sender, "/msc give <item> [amount]", "Give custom items. Use /msc give alone to list them.");
        sendCommandEntry(sender, "/msc seal <pattern> [plane]", "Spawn particle seals & battle effects.");
        sendCommandEntry(sender, "/msc attack <attack> [range]", "Trigger a registered boss attack on the nearest boss.");
        sendCommandEntry(sender, "/msc dummy", "Spawn and pose a test armor stand.");
        sendCommandEntry(sender, "/msc music <play|stop|list|disc> [name] [loop]", "Play .nbs music files or get a jukebox disc.");
        sendCommandEntry(sender, "/msc dimtp <world>", "Teleport between dimensions.");
        sendCommandEntry(sender, "/msc cleanstands", "Remove all custom plugin armor stands.");
        sendLine(sender, "");
        sendLine(sender, " &7&oTip: &e/msc <spawn|give|seal|attack|dummy> help [page]");
        sendMenuFooter(sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.isOp()) {
            return completions;
        }

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("spawn", "give", "attack", "music", "cleanstands", "seal", "dummy", "dimtp");
            completions.addAll(subCommands.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList()));
        } else if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            if (args[1].toLowerCase().startsWith("help")) {
                completions.add("help");
            }
            if (subCmd.equals("spawn")) {
                List<String> entities = SPAWNABLE_ENTITIES;
                completions.addAll(entities.stream()
                        .filter(e -> e.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList()));
            } else if (subCmd.equals("give")) {
                List<String> items = Arrays.asList(
                        "scoobycookie", "excalibur", "icecrown", "wirtslantern", "starcore", "diostand", "mantisclaws", "militarycomponent", "militarymine", "headslimeheart", "headslimegelatin",
                        "aetherpullshot", "chaosforge", "cindergreatsword", "nullshearedge", "soulreapscythe", "skyfiretalisman", "sentinelgrimoire", "venomfang",
                        "eighthandledwheel", "obsidianbastionhelmet", "obsidianbastionchestplate", "obsidianbastionleggings", "obsidianbastionboots",
                        "frostheartoffhand", "marrowaegis", "veilwalkermantle",
                        "chaosorb", "chaospowder", "chaosfragment", "chaoscore", "condensedchaosorb", "enderfragment", "frostheart", "magmacore", "obsidianshard", "reaperessence", "reinforcedbone", "reinforcedboneblock", "endercore", "swordmold", "shadowcloak", "stormcrystal", "venomgland", "voidessence", "wheelessence",
                        "wheelcore", "reapercore", "refinednetherite", "sentinelcore", "sentinel", "multiversalcore", "multiverse", "compressedgoldblock", "goldblock", "moltenwheelcore", "moltenwheel", "moltennetherite", "molten", "refinedwheelcore", "refinedwheel"
                );
                completions.addAll(items.stream()
                        .filter(i -> i.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList()));
            } else if (subCmd.equals("seal")) {
                List<String> seals = Arrays.asList("pentagram", "triangle", "celestial", "circle", "ring", "star", "floating", "wings", "wings2", "vortex", "quake", "divine", "storm");
                completions.addAll(seals.stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList()));
            } else if (subCmd.equals("attack")) {
                List<String> attacks = Arrays.asList(
                        "groundslam", "groundshatter", "shieldbash", "lancestorm", "earthpillar", "chaingrapple", "warstomp", "armorspikes", "vortexpull", "mirrorimage", "doombeam",
                        "starfall", "aerialrush", "sonicboom", "lightningstorm", "gravitywell", "crossslash", "novaburst", "darkorb", "windcutter", "heavenlyjudgment", "rainoflances", "airslam", "hoverbarrage",
                        "lancesnipe", "meteorstorm", "voidbeam", "frostlance", "lightningspear", "shadowvolley", "chainlightning", "crystalbarrage", "arcaneorb", "voidrift", "arcanemissiles", "spiritbeam",
                        "stoneskin", "reflectbarrier", "absorbshield", "shieldseal", "healingcircle", "trianglecall"
                );
                completions.addAll(attacks.stream()
                        .filter(a -> a.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList()));
            } else if (subCmd.equals("music")) {
                List<String> actions = Arrays.asList("play", "stop", "list", "disc");
                completions.addAll(actions.stream()
                        .filter(a -> a.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList()));
            } else if (subCmd.equals("dummy")) {
                List<String> actions = Arrays.asList("spawn", "remove", "set", "wings", "wings2", "nowings", "animate", "rightarm", "leftarm", "body", "head", "rightleg", "leftleg");
                completions.addAll(actions.stream()
                        .filter(a -> a.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList()));
            } else if (subCmd.equals("dimtp")) {
                List<String> worlds = new ArrayList<>();
                for (World w : Bukkit.getWorlds()) {
                    worlds.add(w.getName());
                }
                completions.addAll(worlds.stream()
                        .filter(w -> w.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList()));
            }
        } else if (args.length == 3) {
            String subCmd = args[0].toLowerCase();
            if (subCmd.equals("music") && (args[1].equalsIgnoreCase("play") || args[1].equalsIgnoreCase("disc"))) {
                var songs = plugin.getMusicManager().getSongNames();
                completions.addAll(songs.stream()
                        .filter(s -> s.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList()));
            } else if (subCmd.equals("dummy")) {
                String action = args[1].toLowerCase();
                if (action.equals("set")) {
                    List<String> parts = Arrays.asList("rightarm", "leftarm", "body", "head", "rightleg", "leftleg");
                    completions.addAll(parts.stream()
                            .filter(p -> p.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList()));
                } else if (action.equals("animate")) {
                    List<String> anims = Arrays.asList("flyup", "land", "airslam", "shieldseal", "healingcircle", "rain", "pentagram", "triangle");
                    completions.addAll(anims.stream()
                            .filter(a -> a.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList()));
                } else if (Arrays.asList("rightarm", "leftarm", "body", "head", "rightleg", "leftleg").contains(action)) {
                    List<String> axes = Arrays.asList("x", "y", "z");
                    completions.addAll(axes.stream()
                            .filter(a -> a.startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList()));
                }
            }
        }

        return completions;
    }
}