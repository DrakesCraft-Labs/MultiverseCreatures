package com.Chagui68.entities;

import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BossInstance {
    public enum ShieldState {NORMAL, PLANTED, SLAM_DONE}

    public enum DefenseState {NONE, STONE_SKIN, REFLECT_BARRIER, ABSORB_SHIELD}

    public final ArmorStand stand;
    public BossBar bossBar;
    public int currentPhase = 0;
    public int noPlayerTicks = 0;
    public ShieldState shieldState = ShieldState.NORMAL;
    public Entity shieldHolder;
    public int shieldTimer = 0;
    public int shieldCooldown = 0;
    public int hoverBarrageCooldown = 0;
    public int groundAttackCooldown = 0;
    public boolean hoverBarrageActive = false;
    public boolean triangleCallActive = false;
    public boolean isFlying = false;
    public int flyingTimer = 0;
    public double groundY = 0;
    public final Set<String> aerialAttacksDone = new HashSet<>();
    public boolean shieldSealActive = false;
    public int shieldSealTimer = 0;
    public BukkitRunnable shieldSealTask;
    public ItemStack shieldSealSavedShield;
    public final List<ItemDisplay> shieldSealDisplays = new ArrayList<>();
    public boolean healingCircleActive = false;
    public int healingCircleTimer = 0;
    public double healingCircleHealed = 0;
    public BukkitRunnable healingCircleTask;
    public BukkitRunnable groundSlamTask;
    public BukkitRunnable floatingShieldTask;
    public BukkitRunnable wingTask;
    public BukkitRunnable hoverBarrageTask;
    public BukkitRunnable triangleCallTask;
    public BukkitRunnable flyTask;
    public int hoverBarrageTicks = 0;
    public int airStuckTicks = 0;
    public double lastAirY = Double.MAX_VALUE;
    public final Map<UUID, Location> pentagramCenters = new HashMap<>();
    public final Set<UUID> bossMusicListeners = new HashSet<>();
    public int bossMusicTick = 0;
    public boolean invulnerable = false;
    public int invulnerableTimer = 0;
    public DefenseState activeDefense = DefenseState.NONE;
    public int defenseTimer = 0;
    public int defenseCooldown = 0;
    public double absorbShieldHealth = 0;
    public BukkitRunnable defenseTask;

    public BossInstance(ArmorStand stand) {
        this.stand = stand;
    }
}
