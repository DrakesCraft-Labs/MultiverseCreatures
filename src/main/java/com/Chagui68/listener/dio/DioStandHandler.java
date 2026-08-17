package com.Chagui68.listener.dio;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.ability.FreezeAbility;
import com.Chagui68.items.dio.DioStandHead;
import org.bukkit.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.*;

public class DioStandHandler implements Listener {

    private final MultiverseCreatures plugin;
    private final Map<UUID, DioPlayerStand> playerStands = new HashMap<>();
    private final Map<UUID, Long> freezeCooldowns = new HashMap<>();
    private final Map<UUID, Long> punchCooldowns = new HashMap<>();

    public DioStandHandler(MultiverseCreatures plugin) {
        this.plugin = plugin;
        if (!plugin.isEnabled("items.dio-stand")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startStandVisualTask();
    }

    private void startStandVisualTask() {
        new BukkitRunnable() {
            int scanTick = 0;

            @Override
            public void run() {
                for (var entry : new HashMap<>(playerStands).entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    DioPlayerStand dps = entry.getValue();

                    if (player == null || !player.isOnline() || !hasStandInInventory(player)) {
                        if (dps.stand != null && dps.stand.isValid()) dps.stand.remove();
                        playerStands.remove(entry.getKey());
                        continue;
                    }

                    if (dps.stand == null || !dps.stand.isValid()) {
                        dps.stand = spawnStandFor(player);
                        if (dps.stand == null) {
                            playerStands.remove(entry.getKey());
                            continue;
                        }
                    }

                    Location behind = player.getLocation()
                            .add(player.getLocation().getDirection().multiply(-1).setY(0).normalize().multiply(1))
                            .add(0, 2, 0);
                    dps.stand.teleport(behind);
                    dps.stand.setRotation(player.getLocation().getYaw(), 0);
                }

                if (++scanTick % 20 == 0) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (playerStands.containsKey(player.getUniqueId())) continue;
                        if (!hasStandInInventory(player)) continue;

                        ArmorStand stand = spawnStandFor(player);
                        if (stand != null) {
                            playerStands.put(player.getUniqueId(), new DioPlayerStand(stand));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private ArmorStand spawnStandFor(Player player) {
        ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
        stand.setVisible(true);
        stand.setMarker(false);
        stand.setSmall(false);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setCollidable(false);
        stand.setCanPickupItems(false);
        stand.setArms(true);
        stand.setBasePlate(false);
        stand.setSilent(true);

        ItemStack head = DioStandHead.getHead();
        if (stand.getEquipment() != null) {
            stand.getEquipment().setHelmet(head);
            stand.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
            stand.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
            stand.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
        }

        stand.addScoreboardTag("MSC_PlayerDioStand");
        return stand;
    }

    private boolean hasStandInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (DioStandHead.isDioStandHead(item)) return true;
        }
        return false;
    }

    private boolean isHoldingStand(Player player) {
        return DioStandHead.isDioStandHead(player.getInventory().getItemInMainHand()) ||
                DioStandHead.isDioStandHead(player.getInventory().getItemInOffHand());
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (!isHoldingStand(player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!DioStandHead.isDioStandHead(item)) {
            item = player.getInventory().getItemInOffHand();
            if (!DioStandHead.isDioStandHead(item)) return;
        }

        UUID pid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long cooldownMs = plugin.getConfig().getLong("items.dio-stand.cooldown-ms", 120000);
        Long lastUse = freezeCooldowns.get(pid);

        if (lastUse != null && now - lastUse < cooldownMs) {
            long remaining = (cooldownMs - (now - lastUse)) / 1000;
            player.sendMessage(ChatColor.RED + "THE WORLD: FREEZING on cooldown (" + remaining + "s)");
            return;
        }

        freezeCooldowns.put(pid, now);

        double freezeRadius = plugin.getConfig().getDouble("items.dio-stand.freeze-radius", 50.0);
        int freezeDurationTicks = plugin.getConfig().getInt("items.dio-stand.freeze-duration-ticks", 100);
        FreezeAbility freeze = plugin.getFreezeAbility();

        for (Player target : player.getWorld().getPlayers()) {
            if (target.equals(player)) continue;
            if (target.getLocation().distanceSquared(player.getLocation()) > freezeRadius * freezeRadius) continue;

            if (hasStandInInventory(target)) continue;

            freeze.freezePlayer(target, freezeDurationTicks, "Dio Stand - THE WORLD: FREEZING");

            target.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "THE WORLD: FREEZING",
                    ChatColor.GRAY + "Frozen by " + player.getName() + "!", 5, 40, 10);
            target.playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.5f);
        }

        player.sendMessage(ChatColor.GREEN + "THE WORLD: FREEZING activated!");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.5f);

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!isHoldingStand(player)) return;

        // The real damage is re-dealt below through a THORNS source with the
        // player as direct entity, so CraftBukkit fires this same event type
        // again. Without this guard the re-dealt hit would re-enter here,
        // recursing until a StackOverflowError, and it would also zero the
        // event before systems like the Eight-Handled Wheel could adapt.
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) return;

        if (!(event.getEntity() instanceof LivingEntity target)) return;

        double rawDamage = event.getDamage();
        event.setDamage(0);
        // Deal the true damage through the real damage pipeline (THORNS bypasses
        // armor) so protective systems like the Eight-Handled Wheel can adapt to it.
        if (!event.isCancelled()) {
            DamageSource source = DamageSource.builder(DamageType.THORNS)
                    .withCausingEntity(player)
                    .withDirectEntity(player)
                    .build();
            target.damage(rawDamage, source);
        }

        long now = System.currentTimeMillis();
        Long lastPunch = punchCooldowns.get(player.getUniqueId());
        if (lastPunch != null && now - lastPunch < 15000) return;
        punchCooldowns.put(player.getUniqueId(), now);

        DioPlayerStand dps = playerStands.get(player.getUniqueId());
        if (dps == null || dps.stand == null || !dps.stand.isValid()) return;

        ArmorStand stand = dps.stand;
double standDamage = plugin.getConfig().getDouble("items.dio-stand.stand-damage", 4.0);
        int durationTicks = plugin.getConfig().getInt("items.dio-stand.stand-duration-ticks", 100);
        int intervalTicks = plugin.getConfig().getInt("items.dio-stand.stand-interval-ticks", 3);
        double maxTotalDamage = plugin.getConfig().getDouble("items.dio-stand.stand-total-damage", 20.0);

        new BukkitRunnable() {
            int tick = 0;
            boolean leftArm = true;
            int totalHits = 0;
            double dealt = 0;
            final int maxTicks = durationTicks;

            @Override
            public void run() {
                if (tick >= maxTicks || dealt >= maxTotalDamage || target.isDead() || !player.isOnline()) {
                    if (stand.isValid()) {
                        stand.setLeftArmPose(new EulerAngle(0, 0, 0));
                        stand.setRightArmPose(new EulerAngle(0, 0, 0));
                    }
                    cancel();
                    return;
                }

                Location playerLoc = player.getLocation();
                Location standPos = playerLoc.clone()
                        .add(playerLoc.getDirection().multiply(1.5))
                        .add(0, 1.5, 0);
                if (stand.getWorld().equals(player.getWorld()) && stand.getLocation().distanceSquared(standPos) > 1) {
                    stand.teleport(standPos);
                    stand.setRotation(playerLoc.getYaw(), 0);
                }

                if (tick % intervalTicks == 0 && totalHits < (maxTicks / intervalTicks)) {
                    totalHits++;

                    if (leftArm) {
                        stand.setRightArmPose(new EulerAngle(0, 0, 0));
                        stand.setLeftArmPose(new EulerAngle(-1.57f, 0, 0));
                    } else {
                        stand.setLeftArmPose(new EulerAngle(0, 0, 0));
                        stand.setRightArmPose(new EulerAngle(-1.57f, 0, 0));
                    }
                    leftArm = !leftArm;

                    if (target.isValid()) {
                        DamageSource source = DamageSource.builder(DamageType.THORNS)
                                .withCausingEntity(player)
                                .withDirectEntity(player)
                                .build();
                        // damage() returns void in this API, so a health change
                        // is used to detect blocked hits (e.g. cancelled by the
                        // Eight-Handled Wheel) and skip their impact effects.
                        double hitDamage = Math.min(standDamage, maxTotalDamage - dealt);
                        double before = target.getHealth();
                        target.damage(hitDamage, source);
                        if (target.getHealth() < before) {
                            dealt += hitDamage;
                            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.1);
                            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.8f, 1.2f);
                        }
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack helm = player.getInventory().getHelmet();
        if (DioStandHead.isDioStandHead(helm)) {
            player.getInventory().setHelmet(null);
            player.getInventory().addItem(helm);
            player.sendMessage(ChatColor.RED + "Dio's Stand Head cannot be worn as a helmet!");
        }
        if (hasStandInInventory(player)) {
            ArmorStand stand = spawnStandFor(player);
            if (stand != null) {
                playerStands.put(player.getUniqueId(), new DioPlayerStand(stand));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        DioPlayerStand dps = playerStands.remove(id);
        if (dps != null && dps.stand != null && dps.stand.isValid()) {
            dps.stand.remove();
        }
        freezeCooldowns.remove(id);
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!DioStandHead.isDioStandHead(event.getItem().getItemStack())) return;

        UUID pid = player.getUniqueId();
        if (playerStands.containsKey(pid)) return;

        ArmorStand stand = spawnStandFor(player);
        if (stand != null) {
            playerStands.put(pid, new DioPlayerStand(stand));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (DioStandHead.isDioStandHead(event.getItemInHand())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "This item cannot be placed!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (DioStandHead.isDioStandHead(cursor) || DioStandHead.isDioStandHead(current)) {
            // Shift-clicks can smuggle the head into the helmet slot, bypassing the
            // direct-click checks below.
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "This item cannot be worn as a helmet!");
                return;
            }
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CRAFTING) {
                int slot = event.getSlot();
                if (slot == 5 || slot == 6 || slot == 7 || slot == 8) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "This item cannot be worn as a helmet!");
                }
            }
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
                if (event.getSlot() == 39) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "This item cannot be worn as a helmet!");
                }
            }
        }
    }

    public boolean hasStandAndImmune(Player player) {
        return hasStandInInventory(player);
    }

    @EventHandler
    public void onStandInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) return;
        if (stand.getScoreboardTags().contains("MSC_PlayerDioStand") ||
                stand.getScoreboardTags().contains("MSC_DioStand") ||
                stand.getScoreboardTags().contains("MSC_DioSword")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onStandManipulate(PlayerArmorStandManipulateEvent event) {
        ArmorStand stand = event.getRightClicked();
        if (stand.getScoreboardTags().contains("MSC_PlayerDioStand") ||
                stand.getScoreboardTags().contains("MSC_DioStand") ||
                stand.getScoreboardTags().contains("MSC_DioSword")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onStandDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        if (stand.getScoreboardTags().contains("MSC_PlayerDioStand") ||
                stand.getScoreboardTags().contains("MSC_DioStand") ||
                stand.getScoreboardTags().contains("MSC_DioSword")) {
            event.setCancelled(true);
        }
    }

    private static class DioPlayerStand {
        ArmorStand stand;

        DioPlayerStand(ArmorStand stand) {
            this.stand = stand;
        }
    }
}
