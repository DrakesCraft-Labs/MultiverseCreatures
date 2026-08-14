package com.Chagui68.listener.misc;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.misc.MilitaryMine;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MineHandler implements Listener {

    private static final Set<Material> UNCOPYABLE = Set.of(
            Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK,
            Material.BEDROCK, Material.BARRIER, Material.REINFORCED_DEEPSLATE,
            Material.END_PORTAL, Material.END_PORTAL_FRAME, Material.END_GATEWAY, Material.NETHER_PORTAL,
            Material.STRUCTURE_BLOCK, Material.STRUCTURE_VOID, Material.JIGSAW,
            Material.LIGHT, Material.MOVING_PISTON,
            Material.BUBBLE_COLUMN, Material.WATER, Material.LAVA
    );

    private static final Set<Material> BANNERS = Set.of(
            Material.WHITE_BANNER, Material.ORANGE_BANNER, Material.MAGENTA_BANNER,
            Material.LIGHT_BLUE_BANNER, Material.YELLOW_BANNER, Material.LIME_BANNER,
            Material.PINK_BANNER, Material.GRAY_BANNER, Material.LIGHT_GRAY_BANNER,
            Material.CYAN_BANNER, Material.PURPLE_BANNER, Material.BLUE_BANNER,
            Material.BROWN_BANNER, Material.GREEN_BANNER, Material.RED_BANNER, Material.BLACK_BANNER,
            Material.WHITE_WALL_BANNER, Material.ORANGE_WALL_BANNER, Material.MAGENTA_WALL_BANNER,
            Material.LIGHT_BLUE_WALL_BANNER, Material.YELLOW_WALL_BANNER, Material.LIME_WALL_BANNER,
            Material.PINK_WALL_BANNER, Material.GRAY_WALL_BANNER, Material.LIGHT_GRAY_WALL_BANNER,
            Material.CYAN_WALL_BANNER, Material.PURPLE_WALL_BANNER, Material.BLUE_WALL_BANNER,
            Material.BROWN_WALL_BANNER, Material.GREEN_WALL_BANNER, Material.RED_WALL_BANNER, Material.BLACK_WALL_BANNER
    );

    private static final Set<Material> HANGING_SIGNS = Set.of(
            Material.OAK_HANGING_SIGN, Material.OAK_WALL_HANGING_SIGN,
            Material.SPRUCE_HANGING_SIGN, Material.SPRUCE_WALL_HANGING_SIGN,
            Material.BIRCH_HANGING_SIGN, Material.BIRCH_WALL_HANGING_SIGN,
            Material.JUNGLE_HANGING_SIGN, Material.JUNGLE_WALL_HANGING_SIGN,
            Material.ACACIA_HANGING_SIGN, Material.ACACIA_WALL_HANGING_SIGN,
            Material.DARK_OAK_HANGING_SIGN, Material.DARK_OAK_WALL_HANGING_SIGN,
            Material.MANGROVE_HANGING_SIGN, Material.MANGROVE_WALL_HANGING_SIGN,
            Material.CHERRY_HANGING_SIGN, Material.CHERRY_WALL_HANGING_SIGN,
            Material.BAMBOO_HANGING_SIGN, Material.BAMBOO_WALL_HANGING_SIGN,
            Material.CRIMSON_HANGING_SIGN, Material.CRIMSON_WALL_HANGING_SIGN,
            Material.WARPED_HANGING_SIGN, Material.WARPED_WALL_HANGING_SIGN
    );

    private final MultiverseCreatures plugin;
    private final Map<Location, BlockData> mineLocations = new ConcurrentHashMap<>();

    public MineHandler(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isMilitaryMine(event.getItemInHand())) return;

        Block block = event.getBlock();
        Location loc = block.getLocation();

        Material camouflage = determineCamouflage(block);
        if (camouflage == Material.AIR) {
            camouflage = Material.STONE;
        }
        block.setType(camouflage);
        // Some blocks (e.g. wall signs) break themselves when set without the
        // support they need, leaving an invisible but still-armed mine. Fall back
        // to stone so the mine stays visible.
        if (block.getType() != camouflage) {
            block.setType(Material.STONE);
        }

        mineLocations.put(loc, Bukkit.createBlockData(Material.TNT));
    }

    private boolean isMilitaryMine(ItemStack item) {
        if (item == null || item.getType() != Material.TNT) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(MilitaryMine.MINE_KEY, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying()) return;

        Location to = event.getTo().getBlock().getLocation();
        Block below = to.getBlock().getRelative(BlockFace.DOWN);
        Location belowLoc = below.getLocation();

        if (mineLocations.containsKey(belowLoc)) {
            detonateMine(belowLoc);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        if (mineLocations.containsKey(loc)) {
            detonateMine(loc);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            mineLocations.remove(block.getLocation());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Location loc = event.getClickedBlock().getLocation();
        if (mineLocations.containsKey(loc)) {
            detonateMine(loc);
            event.setCancelled(true);
        }
    }

    private Material determineCamouflage(Block block) {
        Block below = block.getRelative(BlockFace.DOWN);
        Material belowType = below.getType();
        if (isValidCamouflage(belowType)) {
            return belowType;
        }

        Map<Material, Integer> counts = new HashMap<>();
        World world = block.getWorld();
        int bx = block.getX();
        int by = block.getY();
        int bz = block.getZ();

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (dx == 0 && dz == 0) continue;
                Block neighbor = world.getBlockAt(bx + dx, by, bz + dz);
                Material type = neighbor.getType();
                if (isValidCamouflage(type)) {
                    counts.merge(type, 1, Integer::sum);
                }
            }
        }

        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Material.STONE);
    }

    private boolean isValidCamouflage(Material type) {
        if (!type.isBlock() || type == Material.TNT) return false;
        if (type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR) return false;
        if (UNCOPYABLE.contains(type)) return false;

        if (Tag.BUTTONS.isTagged(type)) return false;
        if (Tag.PRESSURE_PLATES.isTagged(type)) return false;
        if (Tag.RAILS.isTagged(type)) return false;
        if (Tag.SIGNS.isTagged(type)) return false;
        if (Tag.WALL_SIGNS.isTagged(type)) return false;
        if (BANNERS.contains(type)) return false;
        if (HANGING_SIGNS.contains(type)) return false;
        if (Tag.BEDS.isTagged(type)) return false;
        if (Tag.SHULKER_BOXES.isTagged(type)) return false;
        if (Tag.DOORS.isTagged(type)) return false;
        if (Tag.TRAPDOORS.isTagged(type)) return false;

        if (Tag.CANDLES.isTagged(type)) return false;
        if (Tag.SMALL_FLOWERS.isTagged(type)) return false;
        if (Tag.CROPS.isTagged(type)) return false;
        if (Tag.SAPLINGS.isTagged(type)) return false;
        if (Tag.FLOWER_POTS.isTagged(type)) return false;
        if (Tag.CORALS.isTagged(type)) return false;
        if (Tag.CORAL_PLANTS.isTagged(type)) return false;
        if (Tag.WALL_CORALS.isTagged(type)) return false;

        return switch (type) {
            // Attached / need-support blocks
            case TORCH, WALL_TORCH, REDSTONE_TORCH, REDSTONE_WALL_TORCH, SOUL_TORCH, SOUL_WALL_TORCH,
                 LANTERN, SOUL_LANTERN,
                 REPEATER, COMPARATOR, REDSTONE_WIRE, LEVER,
                 TRIPWIRE, TRIPWIRE_HOOK, LADDER,
                 POINTED_DRIPSTONE,
                 LIGHTNING_ROD, END_ROD,
                 HOPPER, SCAFFOLDING -> false;

            // Non-full plants / organic
            case BROWN_MUSHROOM, RED_MUSHROOM, CRIMSON_FUNGUS, WARPED_FUNGUS, CRIMSON_ROOTS, WARPED_ROOTS,
                 GLOW_LICHEN, FROGSPAWN,
                 VINE, TWISTING_VINES, WEEPING_VINES, CAVE_VINES,
                 KELP, KELP_PLANT, SEAGRASS, TALL_SEAGRASS,
                 DEAD_BUSH, FERN, TALL_GRASS, LARGE_FERN,
                 SUNFLOWER, LILAC, PEONY, ROSE_BUSH, PITCHER_PLANT,
                 PINK_PETALS,
                 AZALEA, FLOWERING_AZALEA,
                 CHORUS_PLANT, CHORUS_FLOWER,
                 SUGAR_CANE, BAMBOO, COBWEB -> false;

            // Non-full crystals / amethyst
            case SMALL_AMETHYST_BUD, MEDIUM_AMETHYST_BUD, LARGE_AMETHYST_BUD, AMETHYST_CLUSTER -> false;

            // Carpet-like / layers
            case SNOW, MOSS_CARPET, PALE_MOSS_CARPET,
                 SCULK_VEIN, HANGING_ROOTS,
                 BIG_DRIPLEAF, SMALL_DRIPLEAF, SPORE_BLOSSOM -> false;

            // Functional blocks that shouldn't be faked
            case FARMLAND, SEA_PICKLE, TURTLE_EGG, SNIFFER_EGG,
                 DRAGON_EGG, SPAWNER, TRIAL_SPAWNER, VAULT,
                 COPPER_BULB, EXPOSED_COPPER_BULB, WEATHERED_COPPER_BULB, OXIDIZED_COPPER_BULB,
                 WAXED_COPPER_BULB, WAXED_EXPOSED_COPPER_BULB, WAXED_WEATHERED_COPPER_BULB,
                 WAXED_OXIDIZED_COPPER_BULB -> false;

            default -> true;
        };
    }

    private void detonateMine(Location loc) {
        if (!mineLocations.containsKey(loc)) return;
        mineLocations.remove(loc);

        loc.getBlock().setType(Material.AIR);
        loc.getWorld().createExplosion(loc, 4.0f, false, true);
    }
}
