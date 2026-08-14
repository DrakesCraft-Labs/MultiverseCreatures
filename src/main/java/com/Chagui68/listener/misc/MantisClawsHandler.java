package com.Chagui68.listener.misc;

import com.Chagui68.items.misc.MantisClaws;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MantisClawsHandler implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Boolean> lastJumpInput = new HashMap<>();
    private final Set<UUID> clingingPlayers = new HashSet<>();
    private final Set<UUID> mantisPlayers = new HashSet<>();
    private int rescanTick = 0;
    private static final double WALL_JUMP_VERTICAL = 0.55;

    public MantisClawsHandler(Plugin plugin) {
        this.plugin = plugin;
        startClingTicker();
    }

    private void startClingTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (++rescanTick % 20 == 0) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (holdingMantisClaws(p)) {
                            mantisPlayers.add(p.getUniqueId());
                        }
                    }
                }

                for (UUID pid : new HashSet<>(mantisPlayers)) {
                    Player p = Bukkit.getPlayer(pid);
                    if (p == null || !p.isOnline()) {
                        mantisPlayers.remove(pid);
                        continue;
                    }
                    try {
                        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
                        if (p.isGliding()) continue;
                        if (p.isInWater()) continue;
                        if (p.getLocation().getBlock().isLiquid()) continue;
                        if (!holdingMantisClaws(p)) {
                            mantisPlayers.remove(pid);
                            continue;
                        }
                        if (p.isOnGround()) continue;

                        BlockFace wallDir = getWallDirection(p);

                        if (wallDir != null && p.isSneaking()) {
                            clingingPlayers.add(pid);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 2, false, false));
                            Vector vel = p.getVelocity();
                            if (vel.getY() < 0) {
                                vel.setY(Math.max(vel.getY(), -0.1));
                                p.setVelocity(vel);
                            }
                            if (p.getTicksLived() % 2 == 0) {
                                p.getWorld().spawnParticle(Particle.CRIT, p.getLocation(), 1, 0.3, 0.5, 0.3, 0);
                            }
                        } else {
                            if (clingingPlayers.remove(pid)) {
                                p.removePotionEffect(PotionEffectType.JUMP_BOOST);
                            }
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("[MantisClaws] Error in tick for " + p.getName() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (holdingMantisClaws(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        addPacketHandler(player);
        if (holdingMantisClaws(player)) {
            mantisPlayers.add(player.getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        removePacketHandler(player);
        mantisPlayers.remove(player.getUniqueId());
    }

    private void addPacketHandler(Player player) {
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object serverPlayer = getHandle.invoke(player);

            Field connField = serverPlayer.getClass().getField("connection");
            Object serverGamePacketListener = connField.get(serverPlayer);

            Field netField = serverGamePacketListener.getClass().getField("connection");
            Object networkManager = netField.get(serverGamePacketListener);

            Field chField = networkManager.getClass().getField("channel");
            Channel channel = (Channel) chField.get(networkManager);

            if (channel.pipeline().get("msc_jump_detect") == null) {
                channel.pipeline().addBefore("packet_handler", "msc_jump_detect", new ChannelDuplexHandler() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ServerboundPlayerInputPacket");
                        if (packetClass.isInstance(msg)) {
                            Method inputMethod = packetClass.getMethod("input");
                            Object input = inputMethod.invoke(msg);
                            Method jumpMethod = findBooleanMethod(input.getClass(), "jump", "isJumping", "getJumping", "jumping");
                            if (jumpMethod != null) {
                                boolean jumping = (boolean) jumpMethod.invoke(input);
                                handleJumpInput(player, jumping);
                            } else {
                                plugin.getLogger().warning("[MantisClaws] Can't find jump method in " + input.getClass().getName());
                                for (Method m : input.getClass().getMethods()) {
                                    if (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class) {
                                        plugin.getLogger().warning("  -> " + m.getName());
                                    }
                                }
                            }
                        }
                        super.channelRead(ctx, msg);
                    }
                });
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[MantisClaws] Failed to attach packet handler for " + player.getName() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private void removePacketHandler(Player player) {
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object serverPlayer = getHandle.invoke(player);

            Field connField = serverPlayer.getClass().getField("connection");
            Object serverGamePacketListener = connField.get(serverPlayer);

            Field netField = serverGamePacketListener.getClass().getField("connection");
            Object networkManager = netField.get(serverGamePacketListener);

            Field chField = networkManager.getClass().getField("channel");
            Channel channel = (Channel) chField.get(networkManager);

            if (channel.pipeline().get("msc_jump_detect") != null) {
                channel.pipeline().remove("msc_jump_detect");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[MantisClaws] Failed to remove packet handler for " + player.getName() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private void handleJumpInput(Player player, boolean jumping) {
        UUID pid = player.getUniqueId();
        boolean wasJumping = lastJumpInput.getOrDefault(pid, false);
        lastJumpInput.put(pid, jumping);

        if (!jumping || wasJumping) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            if (player.isOnGround()) return;
            if (!holdingMantisClaws(player)) return;
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
            if (player.isInWater() || player.getLocation().getBlock().isLiquid()) return;

            BlockFace wallDir = getWallDirection(player);
            if (wallDir == null) return;

            applyVerticalJump(player);
        });
    }

    private boolean holdingMantisClaws(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(MantisClaws.MANTIS_CLAWS_KEY, PersistentDataType.INTEGER);
    }

    private BlockFace getWallDirection(Player p) {
        Location loc = p.getLocation();
        int x = loc.getBlockX();
        int yBase = loc.getBlockY();
        int z = loc.getBlockZ();

        for (int dy = 0; dy <= 1; dy++) {
            Block center = loc.getWorld().getBlockAt(x, yBase + dy, z);

            Block north = center.getRelative(BlockFace.NORTH);
            if (north.getType().isSolid()) return BlockFace.NORTH;

            Block south = center.getRelative(BlockFace.SOUTH);
            if (south.getType().isSolid()) return BlockFace.SOUTH;

            Block east = center.getRelative(BlockFace.EAST);
            if (east.getType().isSolid()) return BlockFace.EAST;

            Block west = center.getRelative(BlockFace.WEST);
            if (west.getType().isSolid()) return BlockFace.WEST;
        }

        return null;
    }

    private Method findBooleanMethod(Class<?> clazz, String... names) {
        for (String name : names) {
            try {
                Method m = clazz.getMethod(name);
                if (m.getReturnType() == boolean.class) return m;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private void applyVerticalJump(Player p) {
        Vector vel = p.getVelocity();
        vel.setY(WALL_JUMP_VERTICAL);
        p.setVelocity(vel);
        p.setFallDistance(0);

        Location loc = p.getLocation();
        p.getWorld().spawnParticle(Particle.CRIT, loc, 8, 0.4, 0.5, 0.4, 0.1);
        p.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.3f);
    }
}
