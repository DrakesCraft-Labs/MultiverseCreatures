package com.Chagui68.entities.boss;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.items.components.ArchitectKernel;
import com.Chagui68.items.food.ScoobyCookie;
import com.Chagui68.utils.MscEntityUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * JACKSTAR — The System Architect
 * Multimodal 5-Phase Boss with 11-part Display model kinematics, Ultra Instinct,
 * Sans vector manipulation, Warden sonic attacks, scale morphing, and root exploit events.
 */
public class JackStarBoss implements Listener {

    public enum LimbGroup {
        HEAD,
        TORSO_UPPER,
        TORSO_LOWER,
        LEG_RIGHT,
        LEG_LEFT,
        ARM_RIGHT,
        ARM_LEFT
    }

    public enum JackPart {
        HEAD("THIRDYBLADE",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI2NDk2MiwKICAicHJvZmlsZUlkIiA6ICJkNWQ5NzVhNWFhMWY0OTFjOWI4MTlhYTkyYzA4OGI0OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUSElSRFlCTEFERSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kZGM0NTc2NWM3MzY1NGYxYWRhNDViNDllMmU1NjdhNjc4YTljYjI0Mjc3YjVhZTVlZTU4NmQxNWZhOTFlMTQ0IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.937f, 0f, 0f, -0.0004296875f, 0f, 0.937f, 0f, 1.873015625f, 0f, 0f, 0.937f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.HEAD),

        TORSO_UPPER("spacecadet18",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI2NzkzNCwKICAicHJvZmlsZUlkIiA6ICI0MDUxNzNiODY0OTM0NTUxOThlOGMzOGJmNmJmNjZiMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcGFjZWNhZGV0MTgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODU3NDA4OGYzMWRlZjFjNmMxNGZlYTIzN2QwM2MxMzM4ZWMyZWE1N2I0NDQ2YjE1Y2E3MTJmY2Y2YWY2MThjOSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.937f, 0f, 0f, -0.0004296875f, 0f, 0.4685f, 0f, 1.404515625f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.TORSO_UPPER),

        TORSO_LOWER("Kaboyio",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI3MDQzMywKICAicHJvZmlsZUlkIiA6ICI5ZjJiY2M1M2U4YzM0OTY4YTc5Yzc0NTExYWQ2NmQyYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJLYWJveWlvIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk4MjJkZGNkNDI4MGJhODIwNjU5MDZlMTJjNGE5N2QxMzhlNWMyNzZmMDQxOTExMTY4NGQ3MTJlYTAyNWI3NjQiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.937f, 0f, 0f, -0.0004296875f, 0f, 0.937f, 0f, 1.170265625f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.TORSO_LOWER),

        LEG_R_UPPER("WaboWebi",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI3MzUxOSwKICAicHJvZmlsZUlkIiA6ICI3Mjc2ZThmYzVkNjE0ODNjYmMwN2IxYjIzNjI3MDA4ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJXYWJvV2ViaSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kZDdmYjJkNDBmMmM3NjE5M2U2NWQ2NGFiOGQzYTMxZjQyY2Y1MzM0Yjc1MDAxNmI5MzI3ZDM3ZmRjMzBkMDhmIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.4685f, 0f, 0f, -0.1175546875f, 0f, 0.4685f, 0f, 0.701765625f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_RIGHT),

        LEG_R_LOWER("ziad87",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI3NzAxNiwKICAicHJvZmlsZUlkIiA6ICJmMTA0NzMxZjljYTU0NmI0OTkzNjM4NTlkZWY5N2NjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJ6aWFkODciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ5ODg2NDYxOTViNmQ5ZDM1ZGNjZWRkYjYzMjQ0NzVmMDQzZjJiY2RjYjlmYzczYTk3ODc0M2ExZTIxOTM3MiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.4685f, 0f, 0f, -0.1175546875f, 0f, 0.937f, 0f, 0.467515625f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_RIGHT),

        LEG_L_UPPER("ElectronicSex",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI3OTY0MCwKICAicHJvZmlsZUlkIiA6ICI1MTAwZGZmZDI0NDI0M2I0OGQxMmVkZTVkMjgxMzk2ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbGVjdHJvbmljU2V4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzllMzYxYzU1YTcwNzAwYzM4NGM2ZDkzYmE2NGZiOWJlMTdkMjE3ZjFmYWNmY2VmZDI0NGU3NDI4YmYzOWFiMDIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.4685f, 0f, 0f, 0.1166953125f, 0f, 0.4685f, 0f, 0.701765625f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_LEFT),

        LEG_L_LOWER("BukkitAPI",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI4MjkyNSwKICAicHJvZmlsZUlkIiA6ICI3YTVkYmRlNDk0NWU0YTE4Yjg2OWY1MGY1NTJjNjlkYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCdWtraXRBUEkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2MwZjI3ODhmZjJkZjYwOTgyMjllNjJhMGMwNTZjNWExNjFkZjNhMTJjOTE4YWY5MGE2MWIxYjJjNWU5MTAxIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.4685f, 0f, 0f, 0.1166953125f, 0f, 0.937f, 0f, 0.467515625f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_LEFT),

        ARM_R_UPPER("elnadXB",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI4NTc1MywKICAicHJvZmlsZUlkIiA6ICI5MTU1ZmYzNTNlMzc0ZmZlYjE0MmE5NmU2MzU2ZjA4NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJlbG5hZFhCIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I3OGU1NzM2ODQzODNjMTkwMGY3YzBmNTViYzdmNGYzYmNiYjAxMDViMjRiZjdmMjFjYTNjYzI2ZWEzM2YwODEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.4685f, 0f, 0f, 0.3474315625f, 0f, 0.4685f, 0f, 1.404515625f, 0f, 0f, 0.4685f, -0.0164649875f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_RIGHT),

        ARM_R_LOWER("vexlehaha",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI4NzYwNSwKICAicHJvZmlsZUlkIiA6ICJkMTNmODljZmRiMmY0OTUxOWE4YjgxMTUzN2FmZWU2ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ2ZXhsZWhhaGEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBlY2I4NjA0MDBmOWUwNTNhNGRmOGQ1N2FmN2YwMjgzY2FlNTVkNmVlNGE0MGE3NDg3MTFiNGE5MTRhOTE4MiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.4685f, 0f, 0f, 0.3474315625f, 0f, 0.937f, 0f, 1.170265625f, 0f, 0f, 0.4685f, -0.0164649875f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_RIGHT),

        ARM_L_UPPER("OverBigboy123",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI4OTU4MSwKICAicHJvZmlsZUlkIiA6ICI5MTEyOTc2ZGJkMTU0MDk4OGM2MjY3OGNkZjU5NTkyMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJPdmVyQmlnYm95MTIzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFhMTFhMzQ0MTEzNTFlNTM0MWM5ODRkMzg2ZTQzMDMyODQ0YjM5MGEwMTdhZTYwZjk0YmNkYWEwNGUzZTQ5M2QiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.4685f, 0f, 0f, -0.3482909375f, 0f, 0.4685f, 0f, 1.404515625f, 0f, 0f, 0.4685f, -0.0164649875f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_LEFT),

        ARM_L_LOWER("colinPAPA",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI5MTk4NCwKICAicHJvZmlsZUlkIiA6ICI4MjA5YjA3MDlmZGM0NjBhYmY2MTljYmNmYTFjNWQ4MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJjb2xpblBBUEEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTdmZjZkNTY2ZWY5MzBhMjg5NmQ3MWIzNDQzMWZkODlkNzAzYTA3MjhhOWIzZTA0NTYyN2M4ODFjNjU2NTRlNiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.4685f, 0f, 0f, -0.3482909375f, 0f, 0.937f, 0f, 1.170265625f, 0f, 0f, 0.4685f, -0.0164649875f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_LEFT);

        public final String profileName;
        public final String texture;
        public final float[] matrix;
        public final Matrix4f rawMatrix;
        public final Vector3f offset;
        public final Quaternionf rotation;
        public final Vector3f scale;
        public final LimbGroup group;

        public static final Vector3f CENTER;

        static {
            float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
            float minZ = Float.POSITIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;

            for (JackPart p : values()) {
                minX = Math.min(minX, p.offset.x);
                maxX = Math.max(maxX, p.offset.x);
                minY = Math.min(minY, p.offset.y);
                maxY = Math.max(maxY, p.offset.y);
                minZ = Math.min(minZ, p.offset.z);
                maxZ = Math.max(maxZ, p.offset.z);
            }
            CENTER = new Vector3f((minX + maxX) * 0.5f, (minY + maxY) * 0.5f, (minZ + maxZ) * 0.5f);
        }

        JackPart(String profileName, String texture, float[] m, LimbGroup group) {
            this.profileName = profileName;
            this.texture = texture;
            this.matrix = m;
            this.group = group;
            this.rawMatrix = new Matrix4f(
                    m[0], m[4], m[8], m[12],
                    m[1], m[5], m[9], m[13],
                    m[2], m[6], m[10], m[14],
                    m[3], m[7], m[11], m[15]
            );
            this.offset = new Vector3f();
            this.rotation = new Quaternionf();
            this.scale = new Vector3f();
            this.rawMatrix.getTranslation(this.offset);
            this.rawMatrix.getUnnormalizedRotation(this.rotation);
            this.rawMatrix.getScale(this.scale);
        }
    }

    public static final String TAG = "msc_jackstar_boss";
    public static final String PART_TAG = "msc_jackstar_part";
    public static final String MINION_TAG = "msc_jackstar_minion";
    /** Tags make display ownership survive a plugin reload without duplicating the model. */
    static final String PART_OWNER_TAG_PREFIX = "msc_jackstar_owner_";
    private static final String CREATIVE_DISPLAY_TAG = "msc_jackstar_creative_display";
    private static final String OBSERVED_BOSS_TAG = "msc_jackstar_observed_boss";

    public static final Vector3f PIVOT_SHOULDER_RIGHT = new Vector3f(0.35f, 1.40f, 0f);
    public static final Vector3f PIVOT_SHOULDER_LEFT  = new Vector3f(-0.35f, 1.40f, 0f);
    public static final Vector3f PIVOT_HIP_RIGHT       = new Vector3f(0.12f, 0.70f, 0f);
    public static final Vector3f PIVOT_HIP_LEFT        = new Vector3f(-0.12f, 0.70f, 0f);
    public static final Vector3f PIVOT_NECK            = new Vector3f(0.0f, 1.87f, 0f);
    public static final Vector3f PIVOT_TORSO           = new Vector3f(0.0f, 1.25f, 0f);

    private final MultiverseCreatures plugin;
    private final Map<UUID, JackInstance> activeInstances = new HashMap<>();
    private final Random random = new Random();

    private double health;
    private double aggroRange;
    private double moveSpeed;
    private double meleeRange;
    private double meleeDamage;
    private double slamDamage;
    private double sigkillDamage;
    private double dodgeChance;
    private double packetLossChance;

    public JackStarBoss(MultiverseCreatures plugin) {
        this.plugin = plugin;
        reloadConfig();
        if (!plugin.isEnabled("entities.jackstar-architect")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        reloadExisting();
        startTicker();
    }

    public void reloadConfig() {
        var config = plugin.getConfig();
        health = config.getDouble("entities.jackstar-architect.health", 700.0);
        aggroRange = config.getDouble("entities.jackstar-architect.aggro-range", 32.0);
        moveSpeed = config.getDouble("entities.jackstar-architect.move-speed", 0.32);
        meleeRange = config.getDouble("entities.jackstar-architect.melee-range", 3.8);
        meleeDamage = config.getDouble("entities.jackstar-architect.melee-damage", 16.0);
        slamDamage = config.getDouble("entities.jackstar-architect.slam-damage", 20.0);
        sigkillDamage = config.getDouble("entities.jackstar-architect.sigkill-damage", 35.0);
        dodgeChance = config.getDouble("entities.jackstar-architect.dodge-chance", 0.22);
        packetLossChance = config.getDouble("entities.jackstar-architect.packet-loss-chance", 0.25);
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (!stand.getScoreboardTags().contains(TAG)) continue;
                if (!stand.getPersistentDataContainer().has(MscEntityUtils.KEY_VIRTUAL_MAX_HEALTH, org.bukkit.persistence.PersistentDataType.DOUBLE)) {
                    MscEntityUtils.initVirtualHealth(stand, health);
                }
                JackInstance inst = new JackInstance(stand);
                restorePartDisplays(inst);
                activeInstances.put(stand.getUniqueId(), inst);
                setupBossBar(inst);
            }
            for (ItemDisplay display : world.getEntitiesByClass(ItemDisplay.class)) {
                if (!display.getScoreboardTags().contains(PART_TAG)) continue;
                boolean hasOwner = display.getScoreboardTags().stream()
                        .anyMatch(tag -> tag.startsWith(PART_OWNER_TAG_PREFIX));
                // Displays made by pre-ownership builds cannot safely be reattached.
                // Removing only those tagged legacy parts avoids a second overlapping body.
                if (!hasOwner) {
                    display.remove();
                    continue;
                }
                boolean nearStand = false;
                for (Entity e : display.getNearbyEntities(4, 4, 4)) {
                    if (e instanceof ArmorStand stand && stand.getScoreboardTags().contains(TAG)) {
                        nearStand = true;
                        break;
                    }
                }
                if (!nearStand) display.remove();
            }
        }
    }

    private void startTicker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (JackInstance inst : new ArrayList<>(activeInstances.values())) {
                    tick(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void tick(JackInstance inst) {
        ArmorStand stand = inst.stand;
        if (stand.isDead() || !stand.isValid()) {
            cleanup(inst);
            activeInstances.remove(stand.getUniqueId());
            return;
        }
        if (!stand.getWorld().isChunkLoaded(stand.getLocation().getChunk())) return;

        // Failover / Watchdog recovery sleep
        if (inst.inFailoverRecovery) {
            inst.failoverTicks--;
            if (inst.failoverTicks <= 0) {
                completeFailoverRecovery(inst);
            }
            return;
        }

        // During a creative-mode invocation JackStar is an observer, never a second
        // attacker. The summoned boss remains a normal, killable plugin boss.
        if (inst.observedBossId != null) {
            tickCreativeObservation(inst);
            return;
        }

        double currentHealth = MscEntityUtils.getVirtualHealth(stand);
        double maxHealth = MscEntityUtils.getVirtualMaxHealth(stand);
        double ratio = currentHealth / Math.max(1.0, maxHealth);

        // Update 5-Phase State Machine
        int prevPhase = inst.currentPhase;
        if (inst.isKernelPanic || ratio <= 0.15) {
            inst.currentPhase = 5;
        } else if (ratio <= 0.40) {
            inst.currentPhase = 4;
        } else if (ratio <= 0.60) {
            inst.currentPhase = 3;
        } else if (ratio <= 0.80) {
            inst.currentPhase = 2;
        } else {
            inst.currentPhase = 1;
        }

        if (inst.currentPhase != prevPhase) {
            handlePhaseTransition(inst, prevPhase, inst.currentPhase);
        }

        Player target = findTarget(stand);
        inst.targetId = (target != null) ? target.getUniqueId() : null;

        Location loc = stand.getLocation();

        // Scale interpolation
        inst.currentScale += (inst.targetScale - inst.currentScale) * 0.08f;

        // Levitating / Flight Mode in Phase 3 & 5
        if (inst.isLevitating) {
            double targetY = 3.8;
            inst.flightYOffset += (targetY - inst.flightYOffset) * 0.05;
            if (inst.tickCount % 2 == 0) {
                Location vortex = loc.clone().add(0, 0.3, 0);
                stand.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, vortex, 3, 0.4, 0.1, 0.4, 0.03);
                stand.getWorld().spawnParticle(Particle.PORTAL, vortex, 3, 0.3, 0.2, 0.3, 0.05);
            }
        } else {
            inst.flightYOffset *= 0.9;
        }

        // Aura particles: Core beacon / plasma effect
        if (inst.tickCount % 5 == 0) {
            Location coreLoc = loc.clone().add(0, 1.2 + inst.flightYOffset, 0);
            stand.getWorld().spawnParticle(inst.isKernelPanic ? Particle.SOUL_FIRE_FLAME : Particle.END_ROD, coreLoc, 2, 0.15, 0.25, 0.15, 0.01);
            if (inst.isKernelPanic) {
                stand.getWorld().spawnParticle(Particle.PORTAL, coreLoc, 5, 0.3, 0.5, 0.3, 0.05);
            }
        }

        // Update Arena Glitch HUD during Phase 5
        if (inst.tickCount % 20 == 0) {
            updateArenaHUD(inst);
        }

        if (target != null) {
            Vector toTarget = target.getLocation().toVector().subtract(loc.toVector());
            toTarget.setY(0);
            double dist = toTarget.length();
            inst.moving = dist > 2.2;

            double currentSpeed = inst.isKernelPanic ? (moveSpeed * 1.45) : (inst.currentScale < 0.8f ? moveSpeed * 1.5 : moveSpeed);

            // Face target smoothly
            if (dist > 0.05) {
                loc.setDirection(toTarget);
            }

            // Movement toward target (with levitation)
            if (inst.moving && dist <= aggroRange && inst.slashAnimTicks <= 3 && inst.slamAnimTicks <= 3) {
                Vector dir = toTarget.clone().normalize();
                double step = Math.min(currentSpeed, dist);
                loc.add(dir.multiply(step));
            }

            // --- Combat AI Routines Across 5 Phases ---

            // 1. Melee Slash Routine (Zoro 3-slash sweep)
            if (dist <= (meleeRange * inst.currentScale) && inst.meleeCooldown <= 0 && inst.slashAnimTicks <= 0 && inst.slamAnimTicks <= 0) {
                inst.slashAnimTicks = 18;
                inst.meleeCooldown = (inst.currentPhase == 5) ? 18 : 28;
                executeThreeSlashImpact(stand, target, inst.currentScale);
            }

            // 2. Sans Vector Slam (Phase 2, 3, 4, 5)
            if (inst.currentPhase >= 2 && dist <= 18.0 && inst.vectorSlamCooldown <= 0 && inst.slamAnimTicks <= 0) {
                inst.slamAnimTicks = 20;
                inst.vectorSlamCooldown = (inst.currentPhase == 5) ? 120 : 180;
                executeVectorSlam(stand, target);
            }

            // 3. Multiverse Minion Summoning (Phase 2 & 5)
            if ((inst.currentPhase == 2 || inst.currentPhase == 5) && inst.minionCooldown <= 0) {
                inst.minionCooldown = (inst.currentPhase == 5) ? 350 : 500; // 25s
                summonMultiverseMinions(inst);
            }

            // 4. Warden Protocol: Darkness pulse + Sonic Boom (Phase 3 & 5)
            if (inst.currentPhase == 3 || inst.currentPhase == 5) {
                if (inst.tickCount % 60 == 0) {
                    pulseWardenDarkness(inst);
                }
                if (inst.sonicBoomCooldown <= 0 && dist <= 22.0) {
                    inst.sonicBoomCooldown = (inst.currentPhase == 5) ? 180 : 260;
                    executeSonicBoom(inst, target);
                }
            }

            // 5. Scale Morphing Routine (Phase 4)
            if (inst.currentPhase == 4) {
                inst.scaleShiftTimer++;
                if (inst.scaleShiftTimer >= 180) { // Every 9 seconds
                    inst.scaleShiftTimer = 0;
                    shiftScalePhase4(inst);
                }
                // Giant stomp
                if (inst.currentScale > 1.8f && dist <= 7.0 && inst.tickCount % 80 == 0) {
                    executeGiantStomp(inst);
                }
            }

            // 6. SIGKILL Ground Rune Attack (Phase 3, 4, 5)
            if (inst.currentPhase >= 3 && inst.sigkillCooldown <= 0 && dist <= 20.0) {
                inst.sigkillCooldown = (inst.currentPhase == 5) ? 140 : 200;
                castSigkillRune(stand, target.getLocation().clone());
            }

            // 7. Defensive Firewall & Elastic Dash
            if (inst.firewallCooldown <= 0 && dist > 8.0 && random.nextDouble() < 0.15) {
                inst.firewallCooldown = 220;
                deployFirewall(inst);
            }
            if (dist > 14.0 && inst.elasticDashCooldown <= 0) {
                inst.elasticDashCooldown = 140;
                executeElasticDash(inst, target);
            }

            // 8. Annoying Builder Defense Routine
            if (inst.buildCooldown <= 0) {
                if (dist <= 4.0 && random.nextDouble() < 0.40) {
                    inst.buildCooldown = (inst.currentPhase == 5) ? 120 : 180;
                    buildFirejailCage(inst, target);
                } else if (dist > 5.5 && random.nextDouble() < 0.35) {
                    inst.buildCooldown = (inst.currentPhase == 5) ? 100 : 160;
                    if (random.nextBoolean()) {
                        buildFirewallBarrier(inst, target);
                    } else {
                        buildStickyCobwebs(inst, target);
                    }
                }
            }
        }

        // Garbage Collector loop (every 5 seconds)
        if (inst.tickCount % 100 == 0) {
            runGarbageCollector(stand);
        }

        // Snapshot checkpointing in Phase 5
        if (inst.currentPhase == 5 && inst.tickCount % 160 == 0) {
            inst.snapshotHp = MscEntityUtils.getVirtualHealth(stand);
            inst.snapshotLoc = loc.clone();
        }

        if (target != null && inst.tickCount % 220 == 0) {
            throwScoobySnack(inst, target);
        }

        // Decrement animation counters & cooldowns
        if (inst.slashAnimTicks > 0) inst.slashAnimTicks--;
        if (inst.slamAnimTicks > 0) inst.slamAnimTicks--;
        if (inst.meleeCooldown > 0) inst.meleeCooldown--;
        if (inst.vectorSlamCooldown > 0) inst.vectorSlamCooldown--;
        if (inst.sigkillCooldown > 0) inst.sigkillCooldown--;
        if (inst.firewallCooldown > 0) inst.firewallCooldown--;
        if (inst.firewallActiveTicks > 0) inst.firewallActiveTicks--;
        if (inst.elasticDashCooldown > 0) inst.elasticDashCooldown--;
        if (inst.sonicBoomCooldown > 0) inst.sonicBoomCooldown--;
        if (inst.minionCooldown > 0) inst.minionCooldown--;
        if (inst.buildCooldown > 0) inst.buildCooldown--;

        inst.animTicks += 0.18f;
        inst.tickCount++;

        // Sync visual model position
        Location finalLoc = loc.clone().add(0, inst.flightYOffset, 0);
        stand.teleport(finalLoc);
        syncDisplays(inst);
    }

    private void handlePhaseTransition(JackInstance inst, int oldP, int newP) {
        ArmorStand stand = inst.stand;
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        world.playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 2.0f, 0.7f);
        world.strikeLightningEffect(loc);

        switch (newP) {
            case 2 -> {
                broadcastToArena(inst, ChatColor.DARK_AQUA + "[SYS] JackStar: " + ChatColor.AQUA
                        + "\"Fase 2: Iniciando subprocesos del Multiverso... ¡Criaturas, asistan al Arquitecto!\"");
                summonMultiverseMinions(inst);
                enterCreativeModeAndSummonBoss(inst);
            }
            case 3 -> {
                broadcastToArena(inst, ChatColor.DARK_AQUA + "[SYS] JackStar: " + ChatColor.DARK_RED
                        + "\"Fase 3: Protocolo Warden activado. Desplegando levitación y oscuridad dimensional.\"");
                inst.isLevitating = true;
                world.playSound(loc, Sound.ENTITY_WARDEN_ROAR, 2.0f, 0.6f);
                enterCreativeModeAndSummonBoss(inst);
            }
            case 4 -> {
                broadcastToArena(inst, ChatColor.DARK_AQUA + "[SYS] JackStar: " + ChatColor.LIGHT_PURPLE
                        + "\"Fase 4: Alquimia de Hitbox. Desbordamiento y compresión de escala en tiempo real.\"");
                inst.isLevitating = false;
                inst.targetScale = 2.2f;
                enterCreativeModeAndSummonBoss(inst);
            }
            case 5 -> {
                broadcastToArena(inst, ChatColor.RED + "" + ChatColor.BOLD
                        + "[KERNEL PANIC] JACKSTAR: MODO RAÍZ DESATADO. EL SERVIDOR ME PERTENECE.");
                inst.isKernelPanic = true;
                inst.targetScale = 1.0f;
                inst.isLevitating = true;
                world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2.0f, 0.5f);
                enterCreativeModeAndSummonBoss(inst);
            }
        }
    }

    private void shiftScalePhase4(JackInstance inst) {
        if (inst.targetScale > 1.2f) {
            // Shift to micro form
            inst.targetScale = 0.6f;
            broadcastToArena(inst, ChatColor.DARK_AQUA + "[SYS] " + ChatColor.AQUA + "Comprimiendo espacio de memoria: Micro-Modo Cuántico (Velocidad +50%)");
            inst.stand.getWorld().playSound(inst.stand.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.8f);
        } else {
            // Shift to giant form
            inst.targetScale = 2.2f;
            broadcastToArena(inst, ChatColor.DARK_AQUA + "[SYS] " + ChatColor.RED + "Desbordamiento de buffer: Asignación Masiva 220% (Modo Titán)");
            inst.stand.getWorld().playSound(inst.stand.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.8f, 0.5f);
        }
    }

    private void summonMultiverseMinions(JackInstance inst) {
        ArmorStand stand = inst.stand;
        Location loc = stand.getLocation();
        World world = stand.getWorld();

        for (int i = 0; i < 2; i++) {
            double angle = Math.toRadians(i * 180 + random.nextInt(60));
            Location spawnLoc = loc.clone().add(Math.cos(angle) * 3.5, 0, Math.sin(angle) * 3.5);
            snapToGround(spawnLoc);

            LivingEntity minion = null;
            if (plugin.getShadowRogue() != null && random.nextBoolean()) {
                plugin.getShadowRogue().trySpawn(spawnLoc);
            } else if (plugin.getFlameElemental() != null) {
                plugin.getFlameElemental().trySpawn(spawnLoc);
            } else if (plugin.getVoidCrawler() != null) {
                plugin.getVoidCrawler().trySpawn(spawnLoc);
            }

            // Find spawned entity nearby to track
            for (Entity e : world.getNearbyEntities(spawnLoc, 2.0, 2.0, 2.0)) {
                if (e instanceof LivingEntity le && !e.equals(stand) && !e.getScoreboardTags().contains(PART_TAG)) {
                    le.addScoreboardTag(MINION_TAG);
                    inst.summonedMinions.add(le.getUniqueId());
                    break;
                }
            }
        }
        world.playSound(loc, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1.5f, 1.0f);
    }

    private void pulseWardenDarkness(JackInstance inst) {
        ArmorStand stand = inst.stand;
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        world.playSound(loc, Sound.ENTITY_WARDEN_HEARTBEAT, 1.6f, 0.9f);
        for (Player p : world.getPlayers()) {
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
            if (p.getLocation().distanceSquared(loc) <= aggroRange * aggroRange) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 70, 0, false, false, true));
            }
        }
    }

    private void executeSonicBoom(JackInstance inst, Player target) {
        ArmorStand stand = inst.stand;
        World world = stand.getWorld();
        Location eye = stand.getLocation().clone().add(0, 1.6 + inst.flightYOffset, 0);
        Location targetLoc = target.getLocation().clone().add(0, 1.0, 0);
        Vector dir = targetLoc.toVector().subtract(eye.toVector()).normalize();

        world.playSound(eye, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.8f, 1.2f);

        new BukkitRunnable() {
            int chargeTicks = 0;

            @Override
            public void run() {
                if (stand.isDead() || !stand.isValid()) {
                    cancel();
                    return;
                }
                if (chargeTicks < 8) {
                    for (double d = 0; d < 18; d += 0.8) {
                        Location pt = eye.clone().add(dir.clone().multiply(d));
                        world.spawnParticle(Particle.ELECTRIC_SPARK, pt, 1, 0, 0, 0, 0);
                    }
                } else {
                    world.playSound(eye, Sound.ENTITY_WARDEN_SONIC_BOOM, 2.0f, 1.0f);
                    for (double d = 0; d < 22; d += 1.5) {
                        Location pt = eye.clone().add(dir.clone().multiply(d));
                        world.spawnParticle(Particle.SONIC_BOOM, pt, 1);
                    }
                    for (Entity e : world.getNearbyEntities(eye, 20, 10, 20)) {
                        if (e instanceof Player p && p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                            Vector toP = p.getLocation().add(0, 1, 0).toVector().subtract(eye.toVector());
                            if (toP.length() <= 20 && Math.abs(toP.normalize().angle(dir)) < 0.40) {
                                p.damage(24.0, stand);
                                p.setVelocity(dir.clone().multiply(1.8).setY(0.45));
                                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0, false, false));
                            }
                        }
                    }
                    cancel();
                }
                chargeTicks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void executeGiantStomp(JackInstance inst) {
        ArmorStand stand = inst.stand;
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        world.playSound(loc, Sound.ENTITY_IRON_GOLEM_ATTACK, 2.0f, 0.5f);
        world.playSound(loc, Sound.ENTITY_WARDEN_ROAR, 1.6f, 0.7f);
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 45, 3.0, 0.2, 3.0, 0.05);
        world.spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 0.5, 0), 10, 2.0, 0.2, 2.0, 0);

        for (Entity e : world.getNearbyEntities(loc, 9.0, 4.0, 9.0)) {
            if (e instanceof Player p && p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                p.damage(18.0, stand);
                p.setVelocity(new Vector(0, 0.85, 0));
                p.sendMessage(ChatColor.RED + "[SÍSMICA] ¡La pisada colosal de JackStar te arrojó por los aires!");
            }
        }
    }

    private void updateArenaHUD(JackInstance inst) {
        ArmorStand stand = inst.stand;
        if (inst.currentPhase < 5 && !inst.isKernelPanic) return;

        for (Player p : stand.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(stand.getLocation()) <= aggroRange * aggroRange) {
                String headerStr = ChatColor.DARK_RED + "" + ChatColor.MAGIC + "ABC" + ChatColor.RED
                        + " [ KERNEL PANIC: ROOT OVERRIDE ] " + ChatColor.DARK_RED + "" + ChatColor.MAGIC + "ABC";
                String footerStr = ChatColor.DARK_GRAY + "HOST COMPROMISED | PID 0x0 | MEM 0x"
                        + Integer.toHexString(random.nextInt(0xFFFFFF)).toUpperCase();
                Component headerComp = LegacyComponentSerializer.legacySection().deserialize(headerStr);
                Component footerComp = LegacyComponentSerializer.legacySection().deserialize(footerStr);
                p.sendPlayerListHeaderAndFooter(headerComp, footerComp);
                p.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(ChatColor.RED + "0x" + Long.toHexString(System.nanoTime()) + " >> CORE DUMP: SYSTEM OVERFLOW"));
            }
        }
    }

    public void broadcastToArena(JackInstance inst, String message) {
        ArmorStand stand = inst.stand;
        for (Player p : stand.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(stand.getLocation()) <= aggroRange * aggroRange) {
                p.sendMessage(message);
            }
        }
    }

    private void executeThreeSlashImpact(ArmorStand stand, Player primaryTarget, float scale) {
        World world = stand.getWorld();
        Location front = stand.getLocation().clone().add(stand.getLocation().getDirection().multiply(1.6 * scale)).add(0, 1.0, 0);

        world.playSound(front, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 0.6f);
        world.playSound(front, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.2f, 0.9f);
        world.spawnParticle(Particle.SWEEP_ATTACK, front, 4, 0.6 * scale, 0.3 * scale, 0.6 * scale, 0);
        world.spawnParticle(Particle.CRIT, front, 30, 0.8 * scale, 0.5 * scale, 0.8 * scale, 0.1);

        for (Entity e : world.getNearbyEntities(front, 3.5 * scale, 2.0 * scale, 3.5 * scale)) {
            if (!(e instanceof Player p)) continue;
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;

            p.damage(meleeDamage * (scale > 1.5f ? 1.4 : 1.0), stand);
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, false, true));
            Vector knock = p.getLocation().toVector().subtract(stand.getLocation().toVector()).normalize().multiply(0.7 * scale).setY(0.25);
            p.setVelocity(knock);
        }
    }

    private void executeVectorSlam(ArmorStand stand, Player target) {
        World world = stand.getWorld();
        Location targetLoc = target.getLocation();

        world.playSound(targetLoc, Sound.ENTITY_WITHER_SHOOT, 1.5f, 0.6f);
        world.playSound(targetLoc, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1.8f, 0.8f);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, targetLoc.clone().add(0, 1.0, 0), 30, 0.5, 1.0, 0.5, 0.05);

        target.setVelocity(new Vector(0, 1.4, 0));
        target.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "⛓ [VECTOR OVERRIDE] "
                + ChatColor.GRAY + "JackStar ha tomado control de tu gravedad.");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isOnline() && target.isValid()) {
                    target.setVelocity(new Vector(0, -1.8, 0));
                    world.playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.4f);
                    world.spawnParticle(Particle.EXPLOSION, target.getLocation(), 1);
                    target.damage(slamDamage, stand);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2, false, true));
                }
            }
        }.runTaskLater(plugin, 18L);
    }

    private void castSigkillRune(ArmorStand stand, Location ground) {
        World world = stand.getWorld();
        snapToGround(ground);

        world.playSound(ground, Sound.BLOCK_BEACON_DEACTIVATE, 1.5f, 0.5f);
        world.playSound(ground, Sound.BLOCK_BELL_RESONATE, 1.8f, 0.6f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks < 50) {
                    double r = 4.0 * ((double) ticks / 50.0);
                    for (int i = 0; i < 20; i++) {
                        double angle = Math.toRadians(i * 18);
                        Location p = ground.clone().add(Math.cos(angle) * r, 0.1, Math.sin(angle) * r);
                        world.spawnParticle(Particle.SOUL_FIRE_FLAME, p, 1, 0, 0, 0, 0);
                    }
                    if (ticks % 10 == 0) {
                        world.playSound(ground, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f, 0.5f + (ticks * 0.02f));
                    }
                } else {
                    world.playSound(ground, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
                    world.playSound(ground, Sound.ITEM_TRIDENT_THUNDER, 1.8f, 0.8f);
                    world.spawnParticle(Particle.EXPLOSION_EMITTER, ground.clone().add(0, 0.5, 0), 1);
                    world.spawnParticle(Particle.PORTAL, ground.clone().add(0, 1.0, 0), 100, 2.0, 1.0, 2.0, 0.1);

                    for (Entity e : world.getNearbyEntities(ground, 4.2, 3.0, 4.2)) {
                        if (e instanceof Player p && p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                            p.damage(sigkillDamage, stand);
                            p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[SIGKILL -9] "
                                    + ChatColor.DARK_RED + "Proceso terminado con señal de aniquilación forzada.");
                        }
                    }
                    cancel();
                }
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void executeElasticDash(JackInstance inst, Player target) {
        ArmorStand stand = inst.stand;
        World world = stand.getWorld();
        Location start = stand.getLocation();
        Location dest = target.getLocation().clone().subtract(target.getLocation().getDirection().multiply(1.5));

        world.playSound(start, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.5f, 1.2f);
        world.spawnParticle(Particle.CLOUD, start.clone().add(0, 1.0, 0), 20, 0.3, 0.5, 0.3, 0.05);

        stand.teleport(dest);
        world.playSound(dest, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.4f, 0.8f);
        world.spawnParticle(Particle.SWEEP_ATTACK, dest.clone().add(0, 1.0, 0), 2);
    }

    private void deployFirewall(JackInstance inst) {
        inst.firewallActiveTicks = 80;
        ArmorStand stand = inst.stand;
        World world = stand.getWorld();
        Location center = stand.getLocation().clone().add(stand.getLocation().getDirection().multiply(1.5)).add(0, 1.0, 0);

        world.playSound(center, Sound.ITEM_SHIELD_BLOCK, 1.6f, 0.8f);
        world.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 1.2f, 1.8f);
        world.spawnParticle(Particle.ELECTRIC_SPARK, center, 25, 0.8, 1.0, 0.8, 0.05);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, center, 15, 0.6, 0.8, 0.6, 0.02);
    }

    private void runGarbageCollector(ArmorStand stand) {
        World world = stand.getWorld();
        Location loc = stand.getLocation();
        int cleaned = 0;

        for (Entity e : world.getNearbyEntities(loc, 14.0, 8.0, 14.0)) {
            if (e instanceof org.bukkit.entity.Arrow || e instanceof org.bukkit.entity.Item) {
                e.remove();
                cleaned++;
            }
        }

        if (cleaned > 0) {
            world.spawnParticle(Particle.WITCH, loc.clone().add(0, 1.5, 0), 15, 0.6, 0.6, 0.6, 0.02);
            world.playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.6f);
        }
    }

    private void triggerMuiDodge(ArmorStand stand, Player attacker) {
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        Location behind = attacker.getLocation().clone().subtract(attacker.getLocation().getDirection().multiply(1.8));
        behind.setDirection(attacker.getLocation().toVector().subtract(behind.toVector()));
        snapToGround(behind);

        world.spawnParticle(Particle.CLOUD, loc.clone().add(0, 1.0, 0), 16, 0.3, 0.5, 0.3, 0.03);
        world.spawnParticle(Particle.FIREWORK, loc.clone().add(0, 1.0, 0), 12, 0.2, 0.4, 0.2, 0.05);
        world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 1.6f);
        world.playSound(behind, Sound.BLOCK_BEACON_POWER_SELECT, 0.9f, 2.0f);

        stand.teleport(behind);
        attacker.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "⚡ ¡ESQUIVE INSTINTIVO! " + ChatColor.GRAY + "(Ultra Instinct)");
    }

    private double applyLoadBalancer(JackInstance inst, double incomingDamage, Player attacker) {
        ArmorStand stand = inst.stand;
        List<Player> party = new ArrayList<>();
        for (Player p : stand.getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
            if (p.getLocation().distanceSquared(stand.getLocation()) <= 14 * 14) {
                party.add(p);
            }
        }

        if (party.size() > 1) {
            double sharedPart = incomingDamage * 0.35;
            double directPart = incomingDamage * 0.65;
            double perPlayer = sharedPart / party.size();

            for (Player p : party) {
                p.damage(perPlayer, stand);
                p.spawnParticle(Particle.CRIT, p.getLocation().add(0, 1.0, 0), 5, 0.2, 0.2, 0.2, 0.05);
                p.sendActionBar(ChatColor.GOLD + "[LOAD BALANCER] " + ChatColor.YELLOW + "Workload shared (-" + String.format("%.1f", perPlayer) + " HP)");
            }
            return directPart;
        }
        return incomingDamage;
    }

    // --- BUILDER DEFENSE SYSTEM ---
    public void placeTemporaryBlock(JackInstance inst, Block block, Material newMat, int durationTicks) {
        if (block == null) return;
        if (block.getType() != Material.AIR && block.getType() != Material.CAVE_AIR) {
            return; // Zero damage guarantee: never replace existing terrain or player builds
        }
        Material orig = block.getType();
        inst.activeTemporaryBlocks.put(block, orig);
        block.setType(newMat);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (inst.activeTemporaryBlocks.containsKey(block)) {
                    block.setType(inst.activeTemporaryBlocks.remove(block));
                    block.getWorld().spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5), 8, 0.2, 0.2, 0.2, newMat.createBlockData());
                }
            }
        }.runTaskLater(plugin, durationTicks);
    }

    public void buildFirewallBarrier(JackInstance inst, Player target) {
        Location mid = inst.stand.getLocation().clone().add(inst.stand.getLocation().getDirection().multiply(1.8));
        Vector right = new Vector(-inst.stand.getLocation().getDirection().getZ(), 0, inst.stand.getLocation().getDirection().getX()).normalize();
        World world = inst.stand.getWorld();

        for (int h = 0; h < 2; h++) {
            for (int w = -1; w <= 1; w++) {
                Location bLoc = mid.clone().add(right.clone().multiply(w)).add(0, h, 0);
                placeTemporaryBlock(inst, bLoc.getBlock(), (w == 0 ? Material.CRYING_OBSIDIAN : Material.POLISHED_BLACKSTONE_BRICKS), 100);
            }
        }
        world.playSound(mid, Sound.BLOCK_STONE_PLACE, 1.5f, 0.8f);
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, mid.clone().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);
        broadcastToArena(inst, ChatColor.DARK_AQUA + "[SYS] JackStar: " + ChatColor.AQUA + "\"Desplegando muro cortafuegos temporal...\"");
    }

    public void buildFirejailCage(JackInstance inst, Player target) {
        Location pLoc = target.getLocation().getBlock().getLocation();
        World world = target.getWorld();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 2; dy++) {
                    if (dx == 0 && dz == 0 && (dy == 0 || dy == 1)) continue;
                    Location bLoc = pLoc.clone().add(dx, dy, dz);
                    placeTemporaryBlock(inst, bLoc.getBlock(), (dy == 2 ? Material.CRYING_OBSIDIAN : Material.IRON_BARS), 90);
                }
            }
        }
        target.sendMessage(ChatColor.DARK_AQUA + "[FIREJAIL] " + ChatColor.AQUA + "¡JackStar te ha aislado dentro de un sandbox de procesos!");
        world.playSound(pLoc, Sound.BLOCK_IRON_DOOR_CLOSE, 1.6f, 0.7f);
    }

    public void buildStickyCobwebs(JackInstance inst, Player target) {
        Location bLoc = target.getLocation().getBlock().getLocation();
        placeTemporaryBlock(inst, bLoc.getBlock(), Material.COBWEB, 80);
        placeTemporaryBlock(inst, bLoc.clone().add(0, 1, 0).getBlock(), Material.COBWEB, 80);
        target.getWorld().playSound(bLoc, Sound.BLOCK_WOOL_PLACE, 1.2f, 1.4f);
        target.sendMessage(ChatColor.DARK_PURPLE + "[SNARE] " + ChatColor.GRAY + "Búfer atascado con telaraña de datos.");
    }

    private void triggerWatchdog(JackInstance inst) {
        inst.livesRemaining--;
        int rebootNum = 3 - inst.livesRemaining; // 1, 2, or 3
        inst.inFailoverRecovery = true;
        inst.failoverTicks = 55;

        ArmorStand stand = inst.stand;
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        world.playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 1.8f, 0.6f);
        world.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.5f);
        world.spawnParticle(Particle.FLASH, loc.clone().add(0, 1.5, 0), 3);
        world.spawnParticle(Particle.DUST, loc.clone().add(0, 1.5, 0), 80, 1.0, 1.5, 1.0, 0,
                new Particle.DustOptions(Color.fromRGB(0x00CCCC), 2.2f));

        String rebootTitle = switch (rebootNum) {
            case 1 -> ChatColor.RED + "[WATCHDOG 1/3] Nexus Inoperante";
            case 2 -> ChatColor.RED + "[WATCHDOG 2/3] Falla en Hyperion";
            default -> ChatColor.DARK_RED + "[WATCHDOG 3/3] ¡TODOS LOS NODOS CAÍDOS!";
        };
        String rebootSubtitle = switch (rebootNum) {
            case 1 -> ChatColor.YELLOW + "Conmutando al nodo secundario 'Hyperion'...";
            case 2 -> ChatColor.LIGHT_PURPLE + "Migrando a 'StarCluster Quantum'...";
            default -> ChatColor.RED + "Modo Singularidad: KERNEL PANIC FORZADO";
        };

        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(loc) <= 50 * 50) {
                p.sendTitle(rebootTitle, rebootSubtitle, 5, 45, 10);
            }
        }

        for (UUID id : inst.partDisplays.values()) {
            Entity e = world.getEntity(id);
            if (e != null) e.teleport(loc.clone().add(0, -50, 0));
        }
    }

    private void completeFailoverRecovery(JackInstance inst) {
        inst.inFailoverRecovery = false;
        ArmorStand stand = inst.stand;
        World world = stand.getWorld();
        Location loc = stand.getLocation();

        int rebootNum = 3 - inst.livesRemaining;
        double maxHealth = MscEntityUtils.getVirtualMaxHealth(stand);
        double restoredHp = (rebootNum == 3) ? (maxHealth * 0.40) : (maxHealth * 0.50);
        MscEntityUtils.setVirtualHealth(stand, restoredHp);

        world.strikeLightningEffect(loc);
        world.playSound(loc, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.2f);
        world.playSound(loc, Sound.ITEM_TRIDENT_THUNDER, 1.6f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, loc.clone().add(0, 1.5, 0), 1);

        if (rebootNum == 1) {
            inst.currentPhase = 3;
            if (inst.bossBar != null) inst.bossBar.setColor(BarColor.PURPLE);
            broadcastToArena(inst, ChatColor.AQUA + "[SYS] Nodo 'Hyperion' activado. JackStar ha revivido (Vidas restantes: 2)");
        } else if (rebootNum == 2) {
            inst.currentPhase = 4;
            if (inst.bossBar != null) inst.bossBar.setColor(BarColor.YELLOW);
            broadcastToArena(inst, ChatColor.GOLD + "[SYS] Nodo 'StarCluster' en línea. JackStar ha revivido (Vidas restantes: 1)");
        } else {
            inst.currentPhase = 5;
            inst.isKernelPanic = true;
            if (inst.bossBar != null) inst.bossBar.setColor(BarColor.RED);
            broadcastToArena(inst, ChatColor.RED + "[SYS] ¡ÚLTIMA VIDA! Modo Kernel Panic activado. ¡Destrucción total!");
        }

        syncDisplays(inst);
    }

    private Player findTarget(ArmorStand stand) {
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        for (Player p : stand.getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
            double d = stand.getLocation().distanceSquared(p.getLocation());
            if (d <= aggroRange * aggroRange && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    private void syncDisplays(JackInstance inst) {
        ArmorStand stand = inst.stand;
        Location root = stand.getLocation().clone();
        root.setYaw(stand.getLocation().getYaw() + 180);
        root.setPitch(0);

        for (JackPart part : JackPart.values()) {
            UUID id = inst.partDisplays.get(part);
            Entity e = (id != null) ? root.getWorld().getEntity(id) : null;
            if (e instanceof ItemDisplay display && display.isValid()) {
                display.teleport(root);
                display.setTransformation(buildTransformation(part, inst));
            } else {
                ItemDisplay display = spawnPart(root, part, inst.stand.getUniqueId());
                inst.partDisplays.put(part, display.getUniqueId());
            }
        }
    }

    private void enterCreativeModeAndSummonBoss(JackInstance inst) {
        if (inst.observedBossId != null || inst.creativeInvocations >= 5) return;

        ArmorStand stand = inst.stand;
        World world = stand.getWorld();
        Location origin = stand.getLocation();
        int initial = random.nextInt(6);
        for (int attempt = 0; attempt < 6; attempt++) {
            int candidate = (initial + attempt) % 6;
            double angle = Math.toRadians(random.nextInt(360));
            Location spawn = origin.clone().add(Math.cos(angle) * 14.0, 0, Math.sin(angle) * 14.0);
            snapToGround(spawn);
            Entity summoned = spawnObservedBoss(candidate, spawn);
            if (summoned == null) continue;

            summoned.addScoreboardTag(OBSERVED_BOSS_TAG);
            inst.observedBossId = summoned.getUniqueId();
            inst.creativeInvocations++;
            inst.creativeTicks = 0;
            stand.setGravity(false);
            spawnFloatingCommandBlocks(inst);
            broadcastToArena(inst, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD
                    + "[CREATIVE MODE] " + ChatColor.AQUA
                    + "JackStar ha entrado en modo creativo. Ejecutando ritual de "
                    + ChatColor.WHITE + summoned.getName() + ChatColor.AQUA + ".");
            world.playSound(origin, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 2.0f, 0.65f);
            for (int bolt = 0; bolt < 5; bolt++) {
                double boltAngle = Math.toRadians(bolt * 72.0);
                world.strikeLightningEffect(origin.clone().add(Math.cos(boltAngle) * 5.0, 0, Math.sin(boltAngle) * 5.0));
            }
            world.spawnParticle(Particle.ENCHANT, spawn.clone().add(0, 1.0, 0), 100, 2.5, 1.3, 2.5, 0.1);
            return;
        }
    }

    private Entity spawnObservedBoss(int candidate, Location spawn) {
        World world = spawn.getWorld();
        if (world == null) return null;
        String tag = switch (candidate) {
            case 0 -> "MSC_Garou";
            case 1 -> "MSC_Mahoraga";
            case 2 -> "MSC_ChaosMage";
            case 3 -> "MSC_ObsidianGuard";
            case 4 -> "MSC_SoulReaper";
            default -> NixBoss.TAG;
        };
        boolean created = switch (candidate) {
            case 0 -> plugin.getGarouBoss().trySpawn(spawn);
            case 1 -> plugin.getMahoraga().trySpawn(spawn);
            case 2 -> plugin.getChaosMage().trySpawn(spawn);
            case 3 -> plugin.getObsidianGuard().trySpawn(spawn);
            case 4 -> plugin.getSoulReaper().trySpawn(spawn);
            default -> plugin.getNixBoss().trySpawn(spawn);
        };
        if (!created) return null;
        for (Entity entity : world.getNearbyEntities(spawn, 3.0, 4.0, 3.0)) {
            if (entity.getScoreboardTags().contains(tag)) return entity;
        }
        return null;
    }

    private void tickCreativeObservation(JackInstance inst) {
        ArmorStand stand = inst.stand;
        Entity observed = stand.getWorld().getEntity(inst.observedBossId);
        if (observed == null || observed.isDead() || !observed.isValid()) {
            exitCreativeMode(inst);
            return;
        }

        inst.creativeTicks++;
        double angle = inst.creativeTicks * 0.075;
        Location center = observed.getLocation();
        Location orbit = center.clone().add(Math.cos(angle) * 11.5, 2.2 + Math.sin(angle * 0.5) * 0.35, Math.sin(angle) * 11.5);
        orbit.setDirection(center.toVector().subtract(orbit.toVector()));
        stand.teleport(orbit);
        inst.moving = true;
        inst.animTicks += 0.28f;
        updateCreativeDisplays(inst, orbit);
        syncDisplays(inst);

        if (inst.creativeTicks % 20 == 0) {
            stand.getWorld().spawnParticle(Particle.ENCHANT, orbit.clone().add(0, 1.2, 0), 18, 0.55, 0.7, 0.55, 0.05);
            stand.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, orbit.clone().add(0, 1.2, 0), 10, 0.4, 0.5, 0.4, 0.02);
        }
        if (inst.creativeTicks % 100 == 0) {
            stand.getWorld().strikeLightningEffect(observed.getLocation());
            broadcastToArena(inst, ChatColor.DARK_AQUA + "[SYS] " + ChatColor.GRAY
                    + "JackStar observa. El subproceso debe terminar antes de reanudar el combate.");
        }
    }

    private void spawnFloatingCommandBlocks(JackInstance inst) {
        Location root = inst.stand.getLocation();
        for (int i = 0; i < 6; i++) {
            ItemDisplay display = (ItemDisplay) root.getWorld().spawnEntity(root, EntityType.ITEM_DISPLAY);
            display.setItemStack(new ItemStack(Material.COMMAND_BLOCK));
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);
            display.setBillboard(Display.Billboard.FIXED);
            display.setBrightness(new Display.Brightness(15, 15));
            display.setGlowColorOverride(Color.AQUA);
            display.setGlowing(true);
            display.setGravity(false);
            display.setPersistent(false);
            display.addScoreboardTag(CREATIVE_DISPLAY_TAG);
            inst.creativeDisplays.add(display.getUniqueId());
        }
    }

    private void updateCreativeDisplays(JackInstance inst, Location root) {
        World world = root.getWorld();
        for (int i = 0; i < inst.creativeDisplays.size(); i++) {
            Entity entity = world.getEntity(inst.creativeDisplays.get(i));
            if (!(entity instanceof ItemDisplay display) || !display.isValid()) continue;
            double angle = inst.creativeTicks * 0.12 + (Math.PI * 2.0 * i / inst.creativeDisplays.size());
            display.teleport(root.clone().add(Math.cos(angle) * 1.45, 1.1 + Math.sin(angle * 2.0) * 0.24, Math.sin(angle) * 1.45));
        }
    }

    private void exitCreativeMode(JackInstance inst) {
        for (UUID displayId : inst.creativeDisplays) {
            Entity entity = inst.stand.getWorld().getEntity(displayId);
            if (entity != null) entity.remove();
        }
        inst.creativeDisplays.clear();
        inst.observedBossId = null;
        inst.stand.setGravity(true);
        inst.stand.getWorld().playSound(inst.stand.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.6f, 0.9f);
        broadcastToArena(inst, ChatColor.RED + "[SYS] " + ChatColor.GRAY
                + "JackStar reanuda el combate directo. El sistema vuelve a ser hostil.");
    }

    private void throwScoobySnack(JackInstance inst, Player target) {
        Location source = inst.stand.getLocation().clone().add(0, 1.25, 0);
        org.bukkit.entity.Item snack = source.getWorld().dropItem(source, ScoobyCookie.SCOOBY_COOKIE.clone());
        snack.setPickupDelay(Short.MAX_VALUE);
        snack.setVelocity(target.getEyeLocation().toVector().subtract(source.toVector()).normalize().multiply(0.65).setY(0.18));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (snack.isValid()) snack.remove();
        }, 50L);
        broadcastToArena(inst, ChatColor.GOLD + "[SCOOBY PACKET] " + ChatColor.GRAY
                + "JackStar lanzó una galleta de depuración. No alimentes procesos desconocidos.");
    }

    static String partOwnerTag(UUID ownerId) {
        return PART_OWNER_TAG_PREFIX + ownerId.toString().replace("-", "");
    }

    private void restorePartDisplays(JackInstance inst) {
        String ownerTag = partOwnerTag(inst.stand.getUniqueId());
        for (ItemDisplay display : inst.stand.getWorld().getEntitiesByClass(ItemDisplay.class)) {
            if (!display.getScoreboardTags().contains(PART_TAG) || !display.getScoreboardTags().contains(ownerTag)) continue;
            for (JackPart part : JackPart.values()) {
                if (display.getScoreboardTags().contains(PART_TAG + "_" + part.name())) {
                    inst.partDisplays.put(part, display.getUniqueId());
                    break;
                }
            }
        }
    }

    private ItemDisplay spawnPart(Location root, JackPart part, UUID ownerId) {
        ItemStack head = createHead(part.profileName, part.texture);
        ItemDisplay display = (ItemDisplay) root.getWorld().spawnEntity(root, EntityType.ITEM_DISPLAY);
        display.setItemStack(head);
        display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
        display.setBillboard(Display.Billboard.FIXED);
        display.setTransformation(buildTransformation(part, null));
        display.setTeleportDuration(1);
        display.setInterpolationDuration(2);
        display.setInterpolationDelay(0);
        display.setBrightness(new Display.Brightness(15, 15));
        display.setInvulnerable(false);
        display.setGravity(false);
        display.setSilent(true);
        display.setPersistent(true);
        display.addScoreboardTag(PART_TAG);
        display.addScoreboardTag(PART_TAG + "_" + part.name());
        display.addScoreboardTag(partOwnerTag(ownerId));
        return display;
    }

    private ItemStack createHead(String profileName, String base64Texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            try {
                String json = new String(Base64.getDecoder().decode(base64Texture));
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                String url = obj.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
                PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), profileName);
                PlayerTextures textures = profile.getTextures();
                textures.setSkin(new URL(url));
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to set Jack head texture for " + profileName + ": " + e.getMessage());
            }
            head.setItemMeta(meta);
        }
        return head;
    }

    public Transformation buildTransformation(JackPart part, JackInstance inst) {
        Quaternionf limbRot = (inst != null) ? computeLimbQuat(part.group, inst) : new Quaternionf();
        float currentScale = (inst != null) ? inst.currentScale : 1.0f;

        Vector3f pivot = getPivot(part.group);
        Vector3f localBase = new Vector3f(
                part.offset.x - JackPart.CENTER.x,
                part.offset.y,
                part.offset.z - JackPart.CENTER.z
        );

        Vector3f relToPivot = new Vector3f(localBase).sub(pivot);
        Vector3f rotatedRel = new Vector3f(relToPivot);
        limbRot.transform(rotatedRel);

        Vector3f finalTranslation = new Vector3f(pivot).add(rotatedRel).mul(currentScale);
        Quaternionf finalRotation = new Quaternionf(limbRot).mul(part.rotation);
        Vector3f finalScale = new Vector3f(part.scale).mul(currentScale);

        return new Transformation(finalTranslation, finalRotation, finalScale, new Quaternionf());
    }

    private Vector3f getPivot(LimbGroup group) {
        return switch (group) {
            case ARM_RIGHT -> PIVOT_SHOULDER_RIGHT;
            case ARM_LEFT -> PIVOT_SHOULDER_LEFT;
            case LEG_RIGHT -> PIVOT_HIP_RIGHT;
            case LEG_LEFT -> PIVOT_HIP_LEFT;
            case HEAD -> PIVOT_NECK;
            case TORSO_UPPER, TORSO_LOWER -> PIVOT_TORSO;
        };
    }

    private Quaternionf computeLimbQuat(LimbGroup group, JackInstance inst) {
        Quaternionf q = new Quaternionf();
        float s = inst.animTicks;
        boolean walking = inst.moving;

        switch (group) {
            case LEG_RIGHT -> {
                if (walking) {
                    float angle = (float) (Math.sin(s) * 0.32);
                    q.rotateX(angle);
                }
            }
            case LEG_LEFT -> {
                if (walking) {
                    float angle = (float) (Math.sin(s) * -0.32);
                    q.rotateX(angle);
                }
            }
            case ARM_RIGHT -> {
                if (inst.slashAnimTicks > 0) {
                    float prog = 1f - (float) inst.slashAnimTicks / 18f;
                    q.rotateY((float) (-Math.sin(prog * Math.PI) * 1.6));
                    q.rotateZ((float) (Math.sin(prog * Math.PI) * 0.5));
                } else if (walking) {
                    q.rotateX((float) (Math.sin(s) * -0.28));
                }
            }
            case ARM_LEFT -> {
                if (inst.slashAnimTicks > 0) {
                    float prog = 1f - (float) inst.slashAnimTicks / 18f;
                    q.rotateY((float) (Math.sin(prog * Math.PI) * 1.6));
                    q.rotateZ((float) (-Math.sin(prog * Math.PI) * 0.5));
                } else if (walking) {
                    q.rotateX((float) (Math.sin(s) * 0.28));
                }
            }
            case HEAD -> {
                if (inst.targetId != null) {
                    q.rotateY((float) (Math.sin(s * 0.5) * 0.08));
                }
            }
            case TORSO_UPPER, TORSO_LOWER -> {
                if (inst.slashAnimTicks > 0) {
                    float prog = 1f - (float) inst.slashAnimTicks / 18f;
                    q.rotateY((float) (Math.sin(prog * Math.PI) * 0.25));
                }
            }
        }
        return q;
    }

    private void snapToGround(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;
        int bx = loc.getBlockX(), by = loc.getBlockY(), bz = loc.getBlockZ();
        for (int y = by; y > by - 6 && y > world.getMinHeight(); y--) {
            if (world.getBlockAt(bx, y, bz).getType().isSolid()) {
                loc.setY(y + 1);
                return;
            }
        }
    }

    public boolean trySpawn(Location location) {
        World world = location.getWorld();
        if (world == null) return false;

        Location spawnLoc = location.clone();
        snapToGround(spawnLoc);

        ArmorStand stand = (ArmorStand) world.spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(true);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setSmall(false);
        stand.setInvulnerable(false);
        stand.setCollidable(true);
        stand.setCanPickupItems(false);
        stand.setCustomName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "JackStar — El Arquitecto del Sistema");
        stand.setCustomNameVisible(true);
        stand.addScoreboardTag(TAG);

        MscEntityUtils.initVirtualHealth(stand, health);

        JackInstance inst = new JackInstance(stand);
        activeInstances.put(stand.getUniqueId(), inst);

        setupBossBar(inst);
        syncDisplays(inst);

        world.playSound(spawnLoc, Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.7f);
        world.playSound(spawnLoc, Sound.BLOCK_BEACON_ACTIVATE, 2.0f, 1.0f);
        world.spawnParticle(Particle.PORTAL, spawnLoc.clone().add(0, 1.5, 0), 80, 1.0, 1.5, 1.0, 0.05);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (activeInstances.get(stand.getUniqueId()) == inst && stand.isValid()) {
                enterCreativeModeAndSummonBoss(inst);
            }
        }, 60L);

        return true;
    }

    private void setupBossBar(JackInstance inst) {
        inst.bossBar = Bukkit.createBossBar(
                ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "JackStar — El Arquitecto del Sistema",
                BarColor.BLUE,
                BarStyle.SEGMENTED_10,
                BarFlag.CREATE_FOG
        );
        new BukkitRunnable() {
            @Override
            public void run() {
                if (inst.stand.isDead() || !inst.stand.isValid()) {
                    if (inst.bossBar != null) inst.bossBar.removeAll();
                    cancel();
                    return;
                }
                Location loc = inst.stand.getLocation();
                for (Player p : loc.getWorld().getPlayers()) {
                    if (p.getLocation().distanceSquared(loc) <= aggroRange * aggroRange) {
                        inst.bossBar.addPlayer(p);
                    } else {
                        inst.bossBar.removePlayer(p);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public boolean isBossActiveIn(World world) {
        if (world == null) return false;
        for (JackInstance inst : activeInstances.values()) {
            if (inst.stand.getWorld().equals(world) && inst.stand.isValid()) {
                return true;
            }
        }
        return false;
    }

    public void cleanup(JackInstance inst) {
        if (inst.bossBar != null) {
            inst.bossBar.removeAll();
        }
        World world = inst.stand.getWorld();
        for (UUID id : inst.partDisplays.values()) {
            Entity e = world.getEntity(id);
            if (e != null) e.remove();
        }
        inst.partDisplays.clear();

        // Clear arena HUD glitches and potion effects
        for (Player p : world.getPlayers()) {
            p.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
            p.removePotionEffect(PotionEffectType.DARKNESS);
        }

        // Cleanup summoned minions
        for (UUID minionId : inst.summonedMinions) {
            Entity minion = world.getEntity(minionId);
            if (minion != null) minion.remove();
        }
        inst.summonedMinions.clear();

        for (UUID displayId : inst.creativeDisplays) {
            Entity display = world.getEntity(displayId);
            if (display != null) display.remove();
        }
        inst.creativeDisplays.clear();

        if (inst.observedBossId != null) {
            Entity observed = world.getEntity(inst.observedBossId);
            if (observed != null) observed.remove();
            inst.observedBossId = null;
        }

        // Cleanup active temporary builder blocks (guarantee zero world griefing)
        for (Map.Entry<Block, Material> entry : inst.activeTemporaryBlocks.entrySet()) {
            entry.getKey().setType(entry.getValue());
        }
        inst.activeTemporaryBlocks.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        ArmorStand stand = null;
        if (victim instanceof ArmorStand as && as.getScoreboardTags().contains(TAG)) {
            stand = as;
        } else if (victim instanceof ItemDisplay display && display.getScoreboardTags().contains(PART_TAG)) {
            stand = findOwner(display);
        }

        if (stand == null) return;

        JackInstance inst = activeInstances.get(stand.getUniqueId());
        if (inst == null || inst.inFailoverRecovery) {
            event.setCancelled(true);
            return;
        }

        Player player = null;
        if (event.getDamager() instanceof Player p) {
            player = p;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            player = p;
        }

        if (inst.observedBossId != null) {
            event.setCancelled(true);
            if (player != null && inst.creativeTicks % 40 == 0) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "[CREATIVE MODE] " + ChatColor.GRAY
                        + "JackStar es invulnerable mientras su invocación siga activa.");
            }
            return;
        }

        // Projectile Packet Loss
        if (event.getDamager() instanceof Projectile projectile) {
            double effPacketLoss = (inst.currentScale < 0.8f) ? 0.45 : packetLossChance;
            if (random.nextDouble() < effPacketLoss) {
                event.setCancelled(true);
                projectile.remove();
                Location loc = stand.getLocation().clone().add(0, 1.5, 0);
                stand.getWorld().playSound(loc, Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 2.0f);
                stand.getWorld().spawnParticle(Particle.PORTAL, loc, 12, 0.3, 0.3, 0.3, 0.05);
                if (player != null) {
                    player.sendMessage(ChatColor.DARK_AQUA + "[PACKET LOSS] " + ChatColor.GRAY + "Tu proyectil fue descartado en el buffer de red.");
                }
                return;
            }

            if (inst.firewallActiveTicks > 0) {
                event.setCancelled(true);
                projectile.remove();
                stand.getWorld().playSound(stand.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.2f, 1.0f);
                return;
            }
        }

        if (player != null) {
            event.setCancelled(true);

            double effDodge = (inst.currentScale < 0.8f) ? 0.45 : dodgeChance;
            if (random.nextDouble() < effDodge) {
                triggerMuiDodge(stand, player);
                return;
            }

            double damage = Math.max(1.0, event.getFinalDamage());
            damage = applyLoadBalancer(inst, damage, player);

            reduceHealth(stand, damage);
            hitEffect(stand);

            // Reactive builder defense
            if (inst.buildCooldown <= 0 && random.nextDouble() < 0.35) {
                inst.buildCooldown = (inst.currentPhase == 5) ? 90 : 150;
                if (event.getDamager() instanceof Projectile || random.nextBoolean()) {
                    buildFirewallBarrier(inst, player);
                } else {
                    buildFirejailCage(inst, player);
                }
            }
        }
    }

    private void hitEffect(ArmorStand stand) {
        Location loc = stand.getLocation().clone().add(0, 1.5, 0);
        stand.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 10, 0.4, 0.6, 0.4, 0.1);
        stand.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.9f, 0.9f);
    }

    private void reduceHealth(ArmorStand stand, double damage) {
        stand.setNoDamageTicks(0);
        double currentHealth = MscEntityUtils.getVirtualHealth(stand);
        double newHealth = currentHealth - damage;

        JackInstance inst = activeInstances.get(stand.getUniqueId());

        if (newHealth <= 0 && inst != null && inst.livesRemaining > 0) {
            newHealth = 1.0;
            MscEntityUtils.setVirtualHealth(stand, newHealth);
            triggerWatchdog(inst);
            return;
        }

        newHealth = Math.max(0, newHealth);
        MscEntityUtils.setVirtualHealth(stand, newHealth);

        if (inst != null && inst.bossBar != null) {
            double maxHealth = MscEntityUtils.getVirtualMaxHealth(stand);
            inst.bossBar.setProgress(MscEntityUtils.calculateVirtualProgress(newHealth, maxHealth));
        }

        if (newHealth <= 0) {
            triggerFinalDeath(stand);
        }
    }

    private ArmorStand findOwner(Entity entity) {
        ArmorStand best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : entity.getNearbyEntities(4, 4, 4)) {
            if (!(e instanceof ArmorStand stand)) continue;
            if (!stand.getScoreboardTags().contains(TAG)) continue;
            double d = e.getLocation().distanceSquared(entity.getLocation());
            if (d < bestDist) {
                bestDist = d;
                best = stand;
            }
        }
        return best;
    }

    private void triggerFinalDeath(ArmorStand stand) {
        JackInstance inst = activeInstances.remove(stand.getUniqueId());
        if (inst != null) cleanup(inst);

        Location loc = stand.getLocation();
        World world = stand.getWorld();
        world.playSound(loc, Sound.ENTITY_WITHER_DEATH, 1.4f, 0.8f);
        world.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.5f, 0.9f);
        world.strikeLightningEffect(loc);

        world.spawnParticle(Particle.PORTAL, loc.clone().add(0, 1.5, 0), 120, 1.5, 2.0, 1.5, 0.1);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, loc.clone().add(0, 1.5, 0), 2);

        world.spawn(loc, org.bukkit.entity.ExperienceOrb.class).setExperience(950);
        world.dropItemNaturally(loc.clone().add(0, 0.5, 0), ArchitectKernel.ARCHITECT_KERNEL.clone());

        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(loc) <= 60 * 60) {
                p.sendTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "JACKSTAR",
                        ChatColor.GRAY + "El sistema ha finalizado su ejecución con éxito.", 10, 70, 20);
            }
        }
        stand.remove();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof ArmorStand stand)) return;
        if (!stand.getScoreboardTags().contains(TAG)) return;
        List<String> messages = plugin.getConfig().getStringList("entities.jackstar-architect.death-messages");
        if (!messages.isEmpty()) {
            String raw = messages.get(random.nextInt(messages.size()));
            event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', raw.replace("%player%", event.getEntity().getName())));
        }
    }

    public static class JackInstance {
        public final ArmorStand stand;
        public final Map<JackPart, UUID> partDisplays = new EnumMap<>(JackPart.class);
        public BossBar bossBar;
        public UUID targetId;
        public int currentPhase = 1;
        public float currentScale = 1.0f;
        public float targetScale = 1.0f;
        public boolean isLevitating;
        public double flightYOffset;
        public int scaleShiftTimer;
        public int sonicBoomCooldown;
        public int minionCooldown;
        public int buildCooldown;
        public int livesRemaining = 3;
        public final List<UUID> summonedMinions = new ArrayList<>();
        public final Map<Block, Material> activeTemporaryBlocks = new HashMap<>();

        public int meleeCooldown;
        public int vectorSlamCooldown;
        public int sigkillCooldown;
        public int firewallCooldown;
        public int elasticDashCooldown;
        public int firewallActiveTicks;
        public int slashAnimTicks;
        public int slamAnimTicks;
        public boolean moving;
        public boolean isKernelPanic;
        public boolean watchdogTriggered;
        public boolean inFailoverRecovery;
        public int failoverTicks;
        public float animTicks;
        public int tickCount;
        public double snapshotHp;
        public Location snapshotLoc;
        public UUID observedBossId;
        public int creativeInvocations;
        public int creativeTicks;
        public final List<UUID> creativeDisplays = new ArrayList<>();

        public JackInstance(ArmorStand stand) {
            this.stand = stand;
        }
    }
}
