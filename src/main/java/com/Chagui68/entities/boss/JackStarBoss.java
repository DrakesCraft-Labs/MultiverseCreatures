package com.Chagui68.entities.boss;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.LiveStage;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.items.components.ArchitectKernel;
import com.Chagui68.items.food.ScoobyCookie;
import com.Chagui68.utils.DisplaySuit;
import com.Chagui68.utils.MscBossBar;
import com.Chagui68.utils.MscEntityUtils;
import com.Chagui68.utils.MscText;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static net.kyori.adventure.text.format.NamedTextColor.*;

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

        // The left arm wears the right arm's textures: its own skins were deleted from Mojang's
        // texture server (404), and a skull whose skin is gone renders as a default Steve/Alex face.
        ARM_L_UPPER("elnadXB",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI4NTc1MywKICAicHJvZmlsZUlkIiA6ICI5MTU1ZmYzNTNlMzc0ZmZlYjE0MmE5NmU2MzU2ZjA4NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJlbG5hZFhCIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I3OGU1NzM2ODQzODNjMTkwMGY3YzBmNTViYzdmNGYzYmNiYjAxMDViMjRiZjdmMjFjYTNjYzI2ZWEzM2YwODEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.4685f, 0f, 0f, -0.3482909375f, 0f, 0.4685f, 0f, 1.404515625f, 0f, 0f, 0.4685f, -0.0164649875f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_LEFT),

        ARM_L_LOWER("vexlehaha",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTc3NDI4NzYwNSwKICAicHJvZmlsZUlkIiA6ICJkMTNmODljZmRiMmY0OTUxOWE4YjgxMTUzN2FmZWU2ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ2ZXhsZWhhaGEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjBlY2I4NjA0MDBmOWUwNTNhNGRmOGQ1N2FmN2YwMjgzY2FlNTVkNmVlNGE0MGE3NDg3MTFiNGE5MTRhOTE4MiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
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

    /**
     * Scale of the invisible armour stand that carries the hitbox: the model is about 2.1 blocks
     * tall, so a vanilla 1.975-block stand leaves the top of the head and the shoulders unhittable.
     */
    public static final double MODEL_HITBOX_SCALE = 1.2;

    /** Joints, rest pose and limb maths live in {@link JackModel}, testable without a server. */

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
    /**
     * Source and intended amount of the hit currently being applied, read back by the damage
     * listener so {@code /msc debug} can name the attack. Set and cleared by {@link #dealToPlayer}
     * around the synchronous damage call.
     */
    private String outgoingSource;
    private double outgoingIntended;
    private double dodgeChance;
    /** Size of the invisible stand the suit is hit through; {@link #MODEL_HITBOX_SCALE} is the default. */
    private double hitboxScale = MODEL_HITBOX_SCALE;
    private double packetLossChance;
    private double forkBombDamage;
    private double binaryRainDamage;
    private double stackOverflowDamage;
    /** Ticks between two signature moves (fork(), Binary Rain, Stack Overflow); shorter from phase 4. */
    private int specialCooldownTicks;

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
        hitboxScale = MscEntityUtils.clampHitboxScale(
                config.getDouble("entities.jackstar-architect.hitbox-scale", MODEL_HITBOX_SCALE));
        packetLossChance = config.getDouble("entities.jackstar-architect.packet-loss-chance", 0.25);
        forkBombDamage = config.getDouble("entities.jackstar-architect.fork-bomb-damage", 10.0);
        binaryRainDamage = config.getDouble("entities.jackstar-architect.binary-rain-damage", 8.0);
        stackOverflowDamage = config.getDouble("entities.jackstar-architect.stack-overflow-damage", 14.0);
        specialCooldownTicks = Math.max(20, config.getInt("entities.jackstar-architect.special-cooldown-ticks", 200));
    }

    /** Takes over a stand a previous run left behind, wearing the parts it already has. */
    private void adopt(ArmorStand stand) {
        if (activeInstances.containsKey(stand.getUniqueId())) return;
        if (!stand.getPersistentDataContainer().has(MscEntityUtils.KEY_VIRTUAL_MAX_HEALTH, org.bukkit.persistence.PersistentDataType.DOUBLE)) {
            MscEntityUtils.initVirtualHealth(stand, health);
        }
        stand.setGravity(false);
        JackInstance inst = new JackInstance(stand);
        restorePartDisplays(inst);
        activeInstances.put(stand.getUniqueId(), inst);
        setupBossBar(inst);
    }

    /**
     * A stand whose chunk loads after startup: without this it stood there with no AI, and its
     * body was swept as an orphan.
     */
    @EventHandler
    public void onEntitiesLoad(org.bukkit.event.world.EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity instanceof ArmorStand stand && stand.getScoreboardTags().contains(TAG)) adopt(stand);
        }
    }

    /** Removes body parts no live boss wears: spare copies, and the bodies of bosses that are gone. */
    private void sweepParts() {
        java.util.Map<UUID, java.util.Collection<UUID>> worn = new java.util.HashMap<>();
        for (JackInstance inst : activeInstances.values()) worn.put(inst.stand.getUniqueId(), inst.partDisplays.values());
        DisplaySuit.sweep(PART_TAG, PART_OWNER_TAG_PREFIX, worn);
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (stand.getScoreboardTags().contains(TAG)) adopt(stand);
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

    /**
     * The loop that walks this boss's instances. Held so {@link #stopTasks()} can end it: a task
     * nobody holds cannot be cancelled, and a second start would leave two loops walking the same
     * state.
     */
    private BukkitTask ticker;
    private int sweepClock;
    /** Ticks between two sweeps for stray body parts. */
    private static final int SWEEP_INTERVAL = 40;

    private void startTicker() {
        if (ticker != null) ticker.cancel();
        ticker = new BukkitRunnable() {
            @Override
            public void run() {
                for (JackInstance inst : new ArrayList<>(activeInstances.values())) {
                    tick(inst);
                }
                if (++sweepClock % SWEEP_INTERVAL == 0) sweepParts();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /** Stops the tick loop; the plugin calls this from its own {@code onDisable}. */
    public void stopTasks() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
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

        // A creative-mode invocation no longer freezes the fight: JackStar keeps fighting and keeps
        // taking damage while the summoned subprocess is alive, instead of hovering out of reach
        // behind an immunity flag.
        if (inst.observedBossId != null) {
            tickCreativeObservation(inst);
        }

        double currentHealth = MscEntityUtils.getVirtualHealth(stand);
        double maxHealth = MscEntityUtils.getVirtualMaxHealth(stand);
        double ratio = currentHealth / Math.max(1.0, maxHealth);

        // Update 5-Phase State Machine. A phase never walks back: a reboot restores health, and
        // re-deriving the phase from it used to replay every transition (the summons and the
        // broadcasts) on the way back up.
        int prevPhase = inst.currentPhase;
        inst.currentPhase = Math.max(prevPhase, phaseFor(ratio, inst.isKernelPanic));

        if (inst.currentPhase != prevPhase) {
            handlePhaseTransition(inst, prevPhase, inst.currentPhase);
        }

        Player target = findTarget(stand);
        inst.targetId = (target != null) ? target.getUniqueId() : null;

        Location loc = stand.getLocation();

        // Scale interpolation; the hitbox grows and shrinks with the model it stands for.
        inst.currentScale += (inst.targetScale - inst.currentScale) * 0.08f;
        syncHitboxScale(inst);

        // Phase 3 & 5 "levitation" is an aura, not altitude. It used to add a growing Y offset to
        // the stand's own location every tick, so the offsets piled up and JackStar rose out of
        // reach of every melee weapon: he flew instead of walking, and could not be hit.
        if (inst.isLevitating && inst.tickCount % 2 == 0) {
            Location vortex = loc.clone().add(0, 0.3, 0);
            stand.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, vortex, 3, 0.4, 0.1, 0.4, 0.03);
            stand.getWorld().spawnParticle(Particle.PORTAL, vortex, 3, 0.3, 0.2, 0.3, 0.05);
        }

        // Aura particles: Core beacon / plasma effect
        if (inst.tickCount % 5 == 0) {
            Location coreLoc = loc.clone().add(0, 1.2, 0);
            stand.getWorld().spawnParticle(inst.isKernelPanic ? Particle.SOUL_FIRE_FLAME : Particle.END_ROD, coreLoc, 2, 0.15, 0.25, 0.15, 0.01);
            if (inst.isKernelPanic) {
                stand.getWorld().spawnParticle(Particle.PORTAL, coreLoc, 5, 0.3, 0.5, 0.3, 0.05);
            }
        }

        // Update Arena Glitch HUD during Phase 5
        if (inst.tickCount % 20 == 0) {
            updateArenaHUD(inst);
        }

        double targetDist = target == null ? Double.MAX_VALUE
                : Math.hypot(target.getLocation().getX() - loc.getX(), target.getLocation().getZ() - loc.getZ());
        // A signature move owns the body while it plays; the regular routine below waits for it.
        boolean performing = inst.move != JackMoves.Move.NONE || (target != null && performMove(inst, target, targetDist));
        if (target != null && !performing) {
            Vector toTarget = target.getLocation().toVector().subtract(loc.toVector());
            toTarget.setY(0);
            double dist = toTarget.length();
            inst.moving = dist > 2.2;

            double currentSpeed = inst.isKernelPanic ? (moveSpeed * 1.45) : (inst.currentScale < 0.8f ? moveSpeed * 1.5 : moveSpeed);

            // Face target smoothly
            if (dist > 0.05) {
                loc.setDirection(toTarget);
            }

            // Movement toward target, on foot: up a step at most, never through a wall.
            if (inst.moving && dist <= aggroRange && inst.slashAnimTicks <= 3 && inst.slamAnimTicks <= 3) {
                Vector dir = toTarget.clone().normalize();
                double step = Math.min(currentSpeed, dist);
                BossArena.walk(loc, dir.multiply(step), true);
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
        if (inst.moveCooldown > 0 && inst.move == JackMoves.Move.NONE) inst.moveCooldown--;

        inst.animTicks += JackModel.WALK_RATE;
        inst.tickCount++;

        // Feet on the floor every tick, whether he walked or not: a dash, a dodge or a block
        // vanishing under him must never leave him standing on air.
        if (inst.move != JackMoves.Move.NONE) {
            inst.moving = false;
            runMove(inst, target, loc);
        }
        BossArena.settle(loc);
        stand.teleport(loc);
        syncDisplays(inst);
    }

    /** Keeps the stand's hitbox as big as the model, which phase 4 grows to 220% and shrinks to 60%. */
    private void syncHitboxScale(JackInstance inst) {
        double wanted = MscEntityUtils.clampHitboxScale(hitboxScale * inst.currentScale);
        if (Math.abs(wanted - inst.appliedHitboxScale) < 0.05) return;
        AttributeInstance scale = inst.stand.getAttribute(Attribute.SCALE);
        if (scale == null) return;
        scale.setBaseValue(wanted);
        inst.appliedHitboxScale = wanted;
    }

    /** The phase a health fraction puts JackStar in; kernel panic is always the last one. */
    static int phaseFor(double healthRatio, boolean kernelPanic) {
        if (kernelPanic || healthRatio <= 0.15) return 5;
        if (healthRatio <= 0.40) return 4;
        if (healthRatio <= 0.60) return 3;
        if (healthRatio <= 0.80) return 2;
        return 1;
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
        Location eye = stand.getLocation().clone().add(0, 1.6 * inst.currentScale, 0);
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
                                dealToPlayer(stand, p, 24.0, "Sonic Boom");
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
                dealToPlayer(stand, p, 18.0, "Giant Stomp");
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

            dealToPlayer(stand, p, meleeDamage * (scale > 1.5f ? 1.4 : 1.0), "Three-Slash");
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 1, false, true));
            Vector knock = MscEntityUtils.horizontalDirection(stand.getLocation(), p.getLocation()).multiply(0.7 * scale).setY(0.25);
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
                    dealToPlayer(stand, target, slamDamage, "Vector Slam");
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
                            dealToPlayer(stand, p, sigkillDamage, "Sigkill -9");
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
        // Behind the player on the horizontal: with the look pitch left in, a player looking down
        // put him up in the air and one looking up put him inside the floor.
        Location dest = target.getLocation().clone().subtract(horizontalLook(target).multiply(1.5));
        dest.setDirection(target.getLocation().toVector().subtract(dest.toVector()).setY(0));
        BossArena.settle(dest);

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

        Location behind = attacker.getLocation().clone().subtract(horizontalLook(attacker).multiply(1.8));
        behind.setDirection(attacker.getLocation().toVector().subtract(behind.toVector()).setY(0));
        BossArena.settle(behind);

        world.spawnParticle(Particle.CLOUD, loc.clone().add(0, 1.0, 0), 16, 0.3, 0.5, 0.3, 0.03);
        world.spawnParticle(Particle.FIREWORK, loc.clone().add(0, 1.0, 0), 12, 0.2, 0.4, 0.2, 0.05);
        world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 1.6f);
        world.playSound(behind, Sound.BLOCK_BEACON_POWER_SELECT, 0.9f, 2.0f);

        stand.teleport(behind);
        attacker.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "⚡ ¡ESQUIVE INSTINTIVO! " + ChatColor.GRAY + "(Ultra Instinct)");
    }

    /** Where a player is looking, flattened onto the ground; straight up or down falls back to their yaw. */
    private static Vector horizontalLook(Player player) {
        Vector look = player.getLocation().getDirection().setY(0);
        if (look.lengthSquared() < 1e-4) {
            double yaw = Math.toRadians(player.getLocation().getYaw());
            look = new Vector(-Math.sin(yaw), 0, Math.cos(yaw));
        }
        return look.normalize();
    }

    /** The players counted as part of the fight: everyone in range that can still take damage. */
    private List<Player> partyOf(ArmorStand stand) {
        List<Player> party = new ArrayList<>();
        for (Player p : stand.getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
            if (p.getLocation().distanceSquared(stand.getLocation())
                    <= JackResilience.PARTY_RADIUS * JackResilience.PARTY_RADIUS) {
                party.add(p);
            }
        }
        return party;
    }

    /** Makes the party pay its share of an incoming hit. The split itself lives in {@link JackResilience}. */
    private void shareWithParty(ArmorStand stand, List<Player> party, JackResilience.Split split) {
        if (party.size() <= 1) return;
        for (Player p : party) {
            dealToPlayer(stand, p, split.perPartyMember(), "Load Balancer");
            p.spawnParticle(Particle.CRIT, p.getLocation().add(0, 1.0, 0), 5, 0.2, 0.2, 0.2, 0.05);
            p.sendActionBar(ChatColor.GOLD + "[LOAD BALANCER] " + ChatColor.YELLOW + "Workload shared (-" + String.format("%.1f", split.perPartyMember()) + " HP)");
        }
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

        // The phase is left as it is: setting it back to 3 or 4 here was undone on the next tick,
        // and replayed that phase's transition on the way.
        if (rebootNum == 1) {
            if (inst.bossBar != null) inst.bossBar.setColor(BarColor.PURPLE);
            broadcastToArena(inst, ChatColor.AQUA + "[SYS] Nodo 'Hyperion' activado. JackStar ha revivido (Vidas restantes: 2)");
        } else if (rebootNum == 2) {
            if (inst.bossBar != null) inst.bossBar.setColor(BarColor.YELLOW);
            broadcastToArena(inst, ChatColor.GOLD + "[SYS] Nodo 'StarCluster' en línea. JackStar ha revivido (Vidas restantes: 1)");
        } else {
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
                // A reload with the part's chunk unloaded hides it from restorePartDisplays; adopting
                // the one still tagged for this boss avoids a second, overlapping body.
                ItemDisplay adopted = findPartDisplay(inst, part);
                if (adopted != null) {
                    inst.partDisplays.put(part, adopted.getUniqueId());
                } else {
                    ItemDisplay display = spawnPart(root, part, inst.stand.getUniqueId());
                    inst.partDisplays.put(part, display.getUniqueId());
                }
            }
        }
    }

    private ItemDisplay findPartDisplay(JackInstance inst, JackPart part) {
        return DisplaySuit.find(inst.stand.getWorld(), inst.stand.getLocation(),
                tags(part, inst.stand.getUniqueId()));
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
            ObservedSpawn result = spawnObservedBoss(candidate, spawn);
            // Refused by its own density / world rules: nothing was created, try another candidate.
            if (!result.created()) continue;
            // Created but not found where it should be: summoning again would stack bosses.
            if (result.entity() == null) return;
            Entity summoned = result.entity();

            summoned.addScoreboardTag(OBSERVED_BOSS_TAG);
            inst.observedBossId = summoned.getUniqueId();
            inst.creativeInvocations++;
            inst.creativeTicks = 0;
            spawnFloatingCommandBlocks(inst);
            broadcastToArena(inst, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD
                    + "[SUBPROCESO] " + ChatColor.AQUA
                    + "JackStar despliega a " + ChatColor.WHITE + summoned.getName()
                    + ChatColor.AQUA + ". El Arquitecto sigue en pie y sigue siendo golpeable.");
            world.playSound(origin, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 2.0f, 0.65f);
            for (int bolt = 0; bolt < 5; bolt++) {
                double boltAngle = Math.toRadians(bolt * 72.0);
                world.strikeLightningEffect(origin.clone().add(Math.cos(boltAngle) * 5.0, 0, Math.sin(boltAngle) * 5.0));
            }
            world.spawnParticle(Particle.ENCHANT, spawn.clone().add(0, 1.0, 0), 100, 2.5, 1.3, 2.5, 0.1);
            return;
        }
    }

    /** One summon attempt: whether the call created anything, and the boss now standing there. */
    private record ObservedSpawn(Entity entity, boolean created) {
    }

    private ObservedSpawn spawnObservedBoss(int candidate, Location spawn) {
        World world = spawn.getWorld();
        if (world == null) return new ObservedSpawn(null, false);
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
        if (!created) return new ObservedSpawn(null, false);
        for (Entity entity : world.getNearbyEntities(spawn, 5.0, 6.0, 5.0)) {
            if (entity.getScoreboardTags().contains(tag)) return new ObservedSpawn(entity, true);
        }
        plugin.getLogger().warning("[JackStar] Subjefe invocado sin localizar junto a "
                + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ()
                + "; no se encadena otro ritual.");
        return new ObservedSpawn(null, true);
    }

    private void tickCreativeObservation(JackInstance inst) {
        ArmorStand stand = inst.stand;
        Entity observed = stand.getWorld().getEntity(inst.observedBossId);
        if (observed == null || observed.isDead() || !observed.isValid()) {
            exitCreativeMode(inst);
            return;
        }

        inst.creativeTicks++;
        // JackStar stays where the fight is: the ritual is extra pressure, not an escape. He never
        // leaves the ground, so the subprocess can be cleared without losing the boss off-screen.
        Location anchor = stand.getLocation().clone();
        updateCreativeDisplays(inst, anchor);

        if (inst.creativeTicks % 20 == 0) {
            stand.getWorld().spawnParticle(Particle.ENCHANT, anchor.clone().add(0, 1.2, 0), 18, 0.55, 0.7, 0.55, 0.05);
            stand.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, anchor.clone().add(0, 1.2, 0), 10, 0.4, 0.5, 0.4, 0.02);
        }
        if (inst.creativeTicks % 100 == 0) {
            stand.getWorld().strikeLightningEffect(observed.getLocation());
            broadcastToArena(inst, ChatColor.DARK_AQUA + "[SYS] " + ChatColor.GRAY
                    + "Subproceso activo: " + observed.getName() + ". JackStar sigue en ejecución.");
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
        inst.stand.getWorld().playSound(inst.stand.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.6f, 0.9f);
        broadcastToArena(inst, ChatColor.RED + "[SYS] " + ChatColor.GRAY
                + "Subproceso cerrado. JackStar vuelve a ejecutarse en solitario.");
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

    /**
     * Spawns one piece of the suit. Everything a display piece needs — the head, the rest transform,
     * the zeroed interpolation and box, and the ownership tags — lives in {@link DisplaySuit}, so it
     * stays identical for every dressed boss instead of drifting apart per file.
     */
    private ItemDisplay spawnPart(Location root, JackPart part, UUID ownerId) {
        return DisplaySuit.spawn(root, headOf(part), buildTransformation(part, null), tags(part, ownerId));
    }

    private ItemStack headOf(JackPart part) {
        return DisplaySuit.head(part.profileName, part.texture, "JackStar");
    }

    private static DisplaySuit.SuitTags tags(JackPart part, UUID ownerId) {
        return new DisplaySuit.SuitTags(PART_TAG, PART_TAG + "_" + part.name(), partOwnerTag(ownerId));
    }

    public Transformation buildTransformation(JackPart part, JackInstance inst) {
        Quaternionf limbRot = (inst != null) ? computeLimbQuat(part.group, inst) : new Quaternionf();
        Quaternionf lowerRot = (inst != null) ? computeLowerQuat(part, inst) : new Quaternionf();
        float currentScale = (inst != null) ? inst.currentScale : 1.0f;
        return JackModel.compose(part, limbRot, lowerRot, currentScale);
    }

    /**
     * The rotation of the segment below a limb's joint. Folds during walking and during a slash:
     * elbows bend into the sword arc and knees flex into an athletic combat stance.
     */
    private Quaternionf computeLowerQuat(JackPart part, JackInstance inst) {
        if (inst == null) return new Quaternionf();
        if (inst.move != JackMoves.Move.NONE) return JackMoves.lower(inst.move, part, inst.moveTick);
        if (inst.slashAnimTicks > 0) {
            float prog = 1f - (float) inst.slashAnimTicks / 18f;
            return JackModel.slashLowerRotation(part, prog);
        }
        if (inst.moving) {
            return JackModel.lowerRotation(part, inst.animTicks);
        }
        return new Quaternionf();
    }

    private static boolean isArm(LimbGroup group) {
        return group == LimbGroup.ARM_RIGHT || group == LimbGroup.ARM_LEFT;
    }

    private Quaternionf computeLimbQuat(LimbGroup group, JackInstance inst) {
        if (inst.move != JackMoves.Move.NONE) return JackMoves.limb(inst.move, group, inst.moveTick);
        Quaternionf q = new Quaternionf();
        float s = inst.animTicks;
        boolean walking = inst.moving;

        switch (group) {
            case LEG_RIGHT, LEG_LEFT -> {
                if (walking) {
                    q.rotateX(JackModel.walkSwing(group, s));
                }
            }
            case ARM_RIGHT -> {
                if (inst.slashAnimTicks > 0) {
                    float prog = 1f - (float) inst.slashAnimTicks / 18f;
                    q.rotateY((float) (-Math.sin(prog * Math.PI) * 1.6));
                    q.rotateZ((float) (Math.sin(prog * Math.PI) * 0.5));
                } else if (walking) {
                    q.rotateX(JackModel.walkSwing(group, s));
                }
            }
            case ARM_LEFT -> {
                if (inst.slashAnimTicks > 0) {
                    float prog = 1f - (float) inst.slashAnimTicks / 18f;
                    q.rotateY((float) (Math.sin(prog * Math.PI) * 1.6));
                    q.rotateZ((float) (-Math.sin(prog * Math.PI) * 0.5));
                } else if (walking) {
                    q.rotateX(JackModel.walkSwing(group, s));
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

    // ------------------------------------------------------------------ signature moves

    private static final Color CODE = Color.fromRGB(0x39FF6A);
    private static final Color CODE_DIM = Color.fromRGB(0x0F7A2C);
    private static final Color GLITCH_CYAN = Color.fromRGB(0x00F0FF);
    private static final Color GLITCH_MAGENTA = Color.fromRGB(0xFF2BD6);

    /** One hop of a fork() process: it lands at {@code to} after {@code flight} ticks and splits. */
    record Hop(Vector from, Vector to, int start, int flight, int generation) {}

    /** One falling bit of Binary Rain, landing {@link #RAIN_WARN} + {@link #RAIN_FALL} ticks after {@code born}. */
    record Bit(Vector at, int born, boolean one) {}

    private static final int RAIN_WARN = 12;
    private static final int RAIN_FALL = 6;

    /**
     * Starts or continues a signature move.
     *
     * @return whether a move owns JackStar this tick, so the regular combat routine stays out
     */
    private boolean performMove(JackInstance inst, Player target, double dist) {
        if (inst.move == JackMoves.Move.NONE) {
            if (inst.moveCooldown > 0 || inst.slashAnimTicks > 0 || inst.slamAnimTicks > 0 || dist > aggroRange) {
                return false;
            }
            JackMoves.Move pick = pickMove(inst, dist);
            if (pick == null) return false;
            inst.move = pick;
            inst.moveTick = 0;
            inst.hops.clear();
            inst.bits.clear();
            inst.frames.clear();
            inst.struck.clear();
            broadcastToArena(inst, switch (pick) {
                case FORK_BOMB -> ChatColor.AQUA + "> " + ChatColor.WHITE + "while(true) fork();";
                case BINARY_RAIN -> ChatColor.GREEN + "> " + ChatColor.WHITE + "cat /dev/urandom > /arena";
                case STACK_OVERFLOW -> ChatColor.RED + "> " + ChatColor.WHITE + "recurse(jack, ∞);";
                default -> "";
            });
        }
        inst.moving = false;
        return true;
    }

    private JackMoves.Move pickMove(JackInstance inst, double dist) {
        int roll = random.nextInt(100);
        if (dist <= 6.0) return roll < 50 ? JackMoves.Move.STACK_OVERFLOW : JackMoves.Move.BINARY_RAIN;
        if (dist <= 16.0) {
            if (roll < 40) return JackMoves.Move.FORK_BOMB;
            return roll < 70 ? JackMoves.Move.STACK_OVERFLOW : JackMoves.Move.BINARY_RAIN;
        }
        return roll < 60 ? JackMoves.Move.FORK_BOMB : JackMoves.Move.BINARY_RAIN;
    }

    /** Plays one tick of the current move; moves the stand's location for the dashes. */
    private void runMove(JackInstance inst, Player target, Location loc) {
        Fx fx = LiveStage.fxIn(loc.getWorld());
        int t = inst.moveTick;
        switch (inst.move) {
            case FORK_BOMB -> forkTick(inst, fx, loc, target, t);
            case BINARY_RAIN -> rainTick(inst, fx, loc, target, t);
            case STACK_OVERFLOW -> stackTick(inst, fx, loc, target, t);
            default -> {
            }
        }
        inst.moveTick++;
        if (inst.moveTick >= inst.move.ticks) {
            inst.move = JackMoves.Move.NONE;
            inst.moveTick = 0;
            inst.hops.clear();
            inst.bits.clear();
            inst.frames.clear();
            inst.struck.clear();
            int base = inst.currentPhase >= 4 ? 140 : 200;
            inst.moveCooldown = Math.max(20, specialCooldownTicks * base / 200) + random.nextInt(40);
            inst.meleeCooldown = Math.max(inst.meleeCooldown, 10);
        }
    }

    /** fork(): a glitch cube thrown at the target; every landing bursts and forks into two. */
    private void forkTick(JackInstance inst, Fx fx, Location loc, Player target, int t) {
        float scale = inst.currentScale;
        Vector feet = loc.toVector();
        Vector right = rightOf(loc);
        Vector hand = feet.clone().add(new Vector(0, 1.5 * scale, 0)).add(right.clone().multiply(0.5 * scale))
                .subtract(loc.getDirection().setY(0).normalize().multiply(0.4 * scale));
        if (t == 0) fx.sound(feet, Sfx.BEACON_ACTIVATE, 1.5f, 1.8f);
        int release = JackMoves.FORK_WIND + JackMoves.FORK_RELEASE;
        if (t < release) {
            if (target != null && t < JackMoves.FORK_WIND) loc.setDirection(target.getLocation().toVector().subtract(feet).setY(0));
            float p = JackMoves.phase(t, 0, JackMoves.FORK_WIND);
            drawCube(fx, hand, 0.15 + 0.35 * p, t * 0.3, t);
            if (t % 3 == 0) fx.gather(hand, 1.6, 3, GLITCH_CYAN, 6);
            return;
        }
        if (t == release) {
            Vector to = target != null ? groundAt(target.getLocation()) : feet.clone().add(loc.getDirection().setY(0).normalize().multiply(10));
            inst.hops.add(new Hop(hand.clone(), to, t, JackMoves.FORK_FLIGHT, 0));
            fx.sound(hand, Sfx.TRIDENT_THROW, 1.8f, 1.4f);
            fx.sound(hand, Sfx.AMETHYST_CHIME, 2f, 0.6f);
        }
        for (Hop hop : new ArrayList<>(inst.hops)) {
            int age = t - hop.start();
            if (age < 0 || age > hop.flight()) continue;
            double p = age / (double) hop.flight();
            double arc = (hop.generation() == 0 ? 4.0 : 2.5) * 4 * p * (1 - p);
            Vector at = hop.from().clone().add(hop.to().clone().subtract(hop.from()).multiply(p)).add(new Vector(0, arc + 0.5, 0));
            drawCube(fx, at, 0.5 - hop.generation() * 0.1, t * 0.35, t + hop.generation());
            if (age % 2 == 0) {
                Vector landing = hop.to().clone().add(new Vector(0, 0.15, 0));
                fx.ring(landing, 2.5 - hop.generation() * 0.3, 0.5, t * 0.2,
                        fx.dust(Palette.mix(GLITCH_MAGENTA, Palette.WARNING_HOT, p), 1.2f));
            }
            if (age < hop.flight()) continue;
            // Landed: burst, then fork in two unless this is the last generation.
            Vector blast = hop.to().clone().add(new Vector(0, 0.6, 0));
            fx.impact(blast, hop.generation() % 2 == 0 ? GLITCH_CYAN : GLITCH_MAGENTA, 1.6);
            fx.burst(blast, Particle.END_ROD, 14, 0.25);
            fx.cloud(Particle.ELECTRIC_SPARK, blast, 20, 1.2, 0.1);
            fx.sound(blast, Sfx.GLASS_BREAK, 1.6f, 1.4f - hop.generation() * 0.2f);
            fx.sound(blast, Sfx.EXPLODE, 1.0f, 1.6f);
            double damage = forkBombDamage * (hop.generation() == 0 ? 1.0 : 0.6);
            for (Player p2 : arenaPlayers(loc.getWorld())) {
                if (p2.getLocation().toVector().distanceSquared(hop.to()) > 2.6 * 2.6) continue;
                dealToPlayer(inst.stand, p2, damage, "fork()");
                Vector away = p2.getLocation().toVector().subtract(hop.to()).setY(0);
                if (away.lengthSquared() > 0.01) away.normalize().multiply(0.4);
                p2.setVelocity(p2.getVelocity().add(away.setY(0.45)));
            }
            if (hop.generation() >= 2) continue;
            Vector dir = hop.to().clone().subtract(hop.from()).setY(0);
            if (dir.lengthSquared() < 0.01) dir = loc.getDirection().setY(0);
            dir.normalize();
            double spread = hop.generation() == 0 ? 0.9 : 0.7;
            double reach = hop.generation() == 0 ? 5.5 : 4.0;
            for (int side = -1; side <= 1; side += 2) {
                Vector way = rotateFlat(dir, side * spread).multiply(reach);
                Vector next = groundAt(hop.to().clone().add(way).toLocation(loc.getWorld()));
                inst.hops.add(new Hop(hop.to().clone(), next, t + 1, JackMoves.FORK_HOP - hop.generation(), hop.generation() + 1));
            }
        }
    }

    /** Binary Rain: Jack types at the sky and bits fall in telegraphed cells around the players. */
    private void rainTick(JackInstance inst, Fx fx, Location loc, Player target, int t) {
        Vector feet = loc.toVector();
        double sky = 12;
        if (t == 0) {
            fx.sound(feet, Sfx.BEACON_POWER, 1.6f, 1.6f);
            fx.sound(feet, Sfx.ENCHANT, 2f, 0.6f);
        }
        if (t < JackMoves.RAIN_END && t % 3 == 0) {
            // The source: a sheet of code over the arena, scrolling.
            double size = 4 + 8 * JackMoves.phase(t, 0, JackMoves.RAIN_RAISE);
            Vector center = feet.clone().add(new Vector(0, sky, 0));
            for (int i = 0; i < 18; i++) {
                double x = (random.nextDouble() * 2 - 1) * size;
                double z = (random.nextDouble() * 2 - 1) * size;
                fx.dust(random.nextBoolean() ? CODE : CODE_DIM, 1.4f).at(center.clone().add(new Vector(x, random.nextDouble() * 0.4, z)));
            }
            fx.ring(center, size, 1.2, t * 0.05, fx.dust(CODE_DIM, 1.0f));
        }
        if (t >= JackMoves.RAIN_RAISE && t < JackMoves.RAIN_END - RAIN_WARN - RAIN_FALL && t % 3 == 0) {
            List<Player> players = arenaPlayers(loc.getWorld());
            Vector spot;
            if (!players.isEmpty() && random.nextInt(100) < 75) {
                Player aim = players.get(random.nextInt(players.size()));
                Vector lead = aim.getVelocity().clone().setY(0).multiply(RAIN_WARN);
                spot = aim.getLocation().toVector().add(lead).add(new Vector(random.nextGaussian() * 1.5, 0, random.nextGaussian() * 1.5));
            } else {
                spot = feet.clone().add(new Vector(random.nextGaussian() * 7, 0, random.nextGaussian() * 7));
            }
            inst.bits.add(new Bit(groundAt(spot.toLocation(loc.getWorld())), t, random.nextBoolean()));
        }
        for (Bit bit : inst.bits) {
            int age = t - bit.born();
            if (age < 0 || age > RAIN_WARN + RAIN_FALL) continue;
            Vector cell = bit.at().clone().add(new Vector(0, 0.12, 0));
            if (age < RAIN_WARN) {
                if (age % 2 == 0) drawSquare(fx, cell, 1.6, fx.dust(Palette.mix(CODE_DIM, CODE, age / (double) RAIN_WARN), 1.2f));
                if (age % 4 == 0) fx.line(cell, cell.clone().add(new Vector(0, sky, 0)), 1.5, fx.dust(CODE_DIM, 0.7f).sometimes(0.5));
                continue;
            }
            double fall = (age - RAIN_WARN) / (double) RAIN_FALL;
            Vector glyph = cell.clone().add(new Vector(0, sky * (1 - fall) + 0.5, 0));
            drawBit(fx, glyph, bit.one(), loc);
            fx.line(glyph, glyph.clone().add(new Vector(0, 2.5, 0)), 0.4, fx.dust(CODE_DIM, 1.0f).sometimes(0.6));
            if (age < RAIN_WARN + RAIN_FALL) continue;
            fx.impact(cell.clone().add(new Vector(0, 0.4, 0)), CODE, 1.2);
            fx.flatBurst(cell, Particle.HAPPY_VILLAGER, 10, 0.2);
            fx.sound(cell, Sfx.AMETHYST_BREAK, 1.4f, bit.one() ? 1.6f : 1.1f);
            for (Player p : arenaPlayers(loc.getWorld())) {
                Vector to = p.getLocation().toVector().subtract(bit.at());
                if (Math.hypot(to.getX(), to.getZ()) > 1.8 || to.getY() < -1 || to.getY() > 3) continue;
                dealToPlayer(inst.stand, p, binaryRainDamage, "Binary Rain");
                p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0, false, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 30, 1, false, true));
            }
        }
        if (t >= JackMoves.RAIN_RAISE && t < JackMoves.RAIN_END && t % 6 == 0) {
            fx.sound(feet, Sfx.AMETHYST_CHIME, 0.8f, 1.5f + random.nextFloat() * 0.5f);
        }
    }

    /** Stack Overflow: four dashing cuts through the target, then every cut unwinds in reverse order. */
    private void stackTick(JackInstance inst, Fx fx, Location loc, Player target, int t) {
        float scale = inst.currentScale;
        Vector feet = loc.toVector();
        Vector waist = new Vector(0, 1.0 * scale, 0);
        if (t == 0) {
            fx.sound(feet, Sfx.WARDEN_SONIC_CHARGE, 1.5f, 1.8f);
            fx.sound(feet, Sfx.RESPAWN_ANCHOR_CHARGE, 1.5f, 1.4f);
        }
        if (t < JackMoves.STACK_READY) {
            if (target != null) loc.setDirection(target.getLocation().toVector().subtract(feet).setY(0));
            fx.cloud(Particle.ELECTRIC_SPARK, feet.clone().add(waist), 3, 0.5 * scale, 0.05);
            fx.ring(feet.clone().add(new Vector(0, 0.1, 0)), 1.2 * scale, 0.4, t * 0.4, fx.dust(GLITCH_MAGENTA, 1.1f));
            return;
        }
        int dashEnd = JackMoves.STACK_READY + JackMoves.STACK_DASHES * (JackMoves.STACK_DASH + JackMoves.STACK_GAP);
        if (t < dashEnd) {
            int local = (t - JackMoves.STACK_READY) % (JackMoves.STACK_DASH + JackMoves.STACK_GAP);
            int index = (t - JackMoves.STACK_READY) / (JackMoves.STACK_DASH + JackMoves.STACK_GAP);
            if (local == 0) {
                // A new frame: aim through the target and past it.
                Vector aim = target != null ? target.getLocation().toVector() : feet.clone().add(loc.getDirection().setY(0).multiply(6));
                Vector through = aim.clone().subtract(feet).setY(0);
                if (through.lengthSquared() < 0.25) through = rotateFlat(loc.getDirection().setY(0).normalize(), 2.0);
                Vector dir = through.clone().normalize();
                double length = Math.min(14, through.length() + 5);
                inst.frames.add(new Vector[]{feet.clone(), feet.clone().add(dir.clone().multiply(length))});
                inst.struck.clear();
                loc.setDirection(dir);
                drawBracket(fx, feet.clone().add(waist), dir, scale);
                fx.sound(feet, Sfx.BREEZE_WIND_BURST, 1.5f, 1.3f + index * 0.15f);
            }
            if (local < JackMoves.STACK_DASH && !inst.frames.isEmpty()) {
                Vector[] frame = inst.frames.get(inst.frames.size() - 1);
                Vector step = frame[1].clone().subtract(frame[0]).multiply(1.0 / JackMoves.STACK_DASH);
                Vector before = loc.toVector();
                BossArena.walk(loc, step, true);
                Vector after = loc.toVector();
                fx.line(before.clone().add(waist), after.clone().add(waist), 0.3,
                        fx.dust(index % 2 == 0 ? GLITCH_CYAN : GLITCH_MAGENTA, 1.5f).and(fx.particle(Particle.ELECTRIC_SPARK).sometimes(0.3)));
                fx.cloud(Particle.SWEEP_ATTACK, after.clone().add(waist), 1, 0.4, 0);
                for (Player p : arenaPlayers(loc.getWorld())) {
                    if (distanceToSegment(p.getLocation().toVector(), before, after) > 1.6 * scale) continue;
                    if (!inst.struck.add(p.getUniqueId())) continue;
                    dealToPlayer(inst.stand, p, stackOverflowDamage * 0.5, "Stack Overflow");
                    p.setVelocity(p.getVelocity().add(step.clone().normalize().multiply(0.5).setY(0.3)));
                    fx.impact(p.getLocation().toVector().add(new Vector(0, 1, 0)), GLITCH_MAGENTA, 1.2);
                }
                if (local == JackMoves.STACK_DASH - 1) fx.sound(after, Sfx.PLAYER_ATTACK_SWEEP, 1.6f, 1.2f + index * 0.1f);
            }
            // Frames already pushed stay on screen, flickering.
            if (t % 3 == 0) {
                for (Vector[] frame : inst.frames) {
                    fx.line(frame[0].clone().add(waist), frame[1].clone().add(waist), 0.6, fx.dust(CODE_DIM, 0.9f).sometimes(0.6));
                }
            }
            return;
        }
        if (t == dashEnd) {
            fx.sound(feet, Sfx.BEACON_DEACTIVATE, 2f, 1.6f);
            broadcastToArena(inst, ChatColor.RED + "Exception in thread \"arena\" " + ChatColor.WHITE + "java.lang.StackOverflowError");
        }
        // The unwind: newest frame first, one every two ticks.
        int popped = (t - JackMoves.STACK_UNWIND) / 2;
        boolean popTick = t >= JackMoves.STACK_UNWIND && (t - JackMoves.STACK_UNWIND) % 2 == 0;
        for (int i = 0; i < inst.frames.size(); i++) {
            int order = inst.frames.size() - 1 - i;
            Vector[] frame = inst.frames.get(i);
            if (order > popped || t < JackMoves.STACK_UNWIND) {
                if (t % 2 == 0) {
                    float heat = JackMoves.phase(t, dashEnd, JackMoves.STACK_UNWIND);
                    fx.line(frame[0].clone().add(waist), frame[1].clone().add(waist), 0.4,
                            fx.dust(Palette.mix(CODE, Palette.WARNING, heat), 1.3f));
                }
                continue;
            }
            if (!popTick || order != popped) continue;
            fx.line(frame[0].clone().add(waist), frame[1].clone().add(waist), 0.35,
                    fx.dust(Palette.WARNING_HOT, 2.0f).and(fx.particle(Particle.END_ROD, 1, 0.1, 0.05)));
            fx.burst(frame[1].clone().add(waist), Particle.EXPLOSION, 1, 0);
            fx.sound(frame[1], Sfx.EXPLODE, 1.4f, 1.3f + order * 0.1f);
            for (Player p : arenaPlayers(loc.getWorld())) {
                if (distanceToSegment(p.getLocation().toVector(), frame[0], frame[1]) > 1.8) continue;
                dealToPlayer(inst.stand, p, stackOverflowDamage, "Stack Overflow");
                p.setVelocity(p.getVelocity().add(new Vector(0, 0.6, 0)));
            }
        }
    }

    // ------------------------------------------------------------------ drawing helpers

    /** A spinning wireframe cube, its edges flickering between the two glitch colours. */
    private static void drawCube(Fx fx, Vector center, double half, double spin, int flicker) {
        double c = Math.cos(spin), s = Math.sin(spin);
        Vector[] corners = new Vector[8];
        for (int i = 0; i < 8; i++) {
            double x = (i & 1) == 0 ? -half : half;
            double y = (i & 2) == 0 ? -half : half;
            double z = (i & 4) == 0 ? -half : half;
            // Tumble: spin about Y, tilt about X.
            double rx = x * c - z * s, rz = x * s + z * c;
            double ty = y * Math.cos(spin * 0.6) - rz * Math.sin(spin * 0.6);
            double tz = y * Math.sin(spin * 0.6) + rz * Math.cos(spin * 0.6);
            corners[i] = center.clone().add(new Vector(rx, ty, tz));
        }
        Fx.Brush edge = fx.dust(flicker % 4 < 2 ? GLITCH_CYAN : GLITCH_MAGENTA, 1.0f);
        int[][] edges = {{0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        for (int[] e : edges) fx.line(corners[e[0]], corners[e[1]], 0.12 + half * 0.1, edge);
        fx.dust(Palette.ICE, 1.6f).at(center);
    }

    /** A flat square outline on the floor. */
    private static void drawSquare(Fx fx, Vector center, double half, Fx.Brush brush) {
        Vector a = center.clone().add(new Vector(-half, 0, -half));
        Vector b = center.clone().add(new Vector(half, 0, -half));
        Vector c = center.clone().add(new Vector(half, 0, half));
        Vector d = center.clone().add(new Vector(-half, 0, half));
        fx.line(a, b, 0.35, brush);
        fx.line(b, c, 0.35, brush);
        fx.line(c, d, 0.35, brush);
        fx.line(d, a, 0.35, brush);
    }

    /** A falling "1" or "0", drawn upright and turned towards JackStar so the fight can read it. */
    private static void drawBit(Fx fx, Vector at, boolean one, Location jack) {
        Vector across = rotateFlat(jack.getDirection().setY(0).normalize(), Math.PI / 2);
        Fx.Brush glow = fx.dust(CODE, 1.5f);
        if (one) {
            fx.line(at.clone().add(new Vector(0, -0.6, 0)), at.clone().add(new Vector(0, 0.6, 0)), 0.15, glow);
            fx.line(at.clone().add(new Vector(0, 0.6, 0)), at.clone().add(new Vector(0, 0.35, 0)).subtract(across.clone().multiply(0.3)), 0.15, glow);
            fx.line(at.clone().add(new Vector(0, -0.6, 0)).subtract(across.clone().multiply(0.3)),
                    at.clone().add(new Vector(0, -0.6, 0)).add(across.clone().multiply(0.3)), 0.15, glow);
        } else {
            fx.draw(Shapes.circle(at, 0.4, 14, across, new Vector(0, 1.5, 0), 0), glow);
        }
    }

    /** A "[ ]" pushed onto the stack where a dash starts. */
    private static void drawBracket(Fx fx, Vector at, Vector dir, float scale) {
        Vector across = rotateFlat(dir, Math.PI / 2).multiply(0.7 * scale);
        Vector up = new Vector(0, 0.9 * scale, 0);
        Fx.Brush brush = fx.dust(GLITCH_CYAN, 1.3f);
        for (int side = -1; side <= 1; side += 2) {
            Vector bar = at.clone().add(across.clone().multiply(side));
            Vector in = across.clone().multiply(-side * 0.35);
            fx.line(bar.clone().subtract(up), bar.clone().add(up), 0.15, brush);
            fx.line(bar.clone().add(up), bar.clone().add(up).add(in), 0.15, brush);
            fx.line(bar.clone().subtract(up), bar.clone().subtract(up).add(in), 0.15, brush);
        }
    }

    private static Vector rotateFlat(Vector dir, double angle) {
        double c = Math.cos(angle), s = Math.sin(angle);
        return new Vector(dir.getX() * c - dir.getZ() * s, 0, dir.getX() * s + dir.getZ() * c).normalize();
    }

    private static Vector rightOf(Location loc) {
        double yaw = Math.toRadians(loc.getYaw());
        return new Vector(-Math.cos(yaw), 0, -Math.sin(yaw));
    }

    private static double distanceToSegment(Vector p, Vector a, Vector b) {
        Vector ab = b.clone().subtract(a);
        double len = ab.lengthSquared();
        double t = len < 1e-6 ? 0 : Math.max(0, Math.min(1, p.clone().subtract(a).dot(ab) / len));
        return p.distance(a.clone().add(ab.multiply(t)));
    }

    /** The floor under a location, as a point on its top face. */
    private static Vector groundAt(Location at) {
        Location probe = at.clone().add(0, 3, 0);
        double y = BossArena.findFloorY(probe, 12);
        Vector out = at.toVector();
        if (!Double.isNaN(y)) out.setY(y);
        return out;
    }

    private List<Player> arenaPlayers(World world) {
        List<Player> out = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR || p.isDead()) continue;
            out.add(p);
        }
        return out;
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
        // Off on purpose: the tick loop walks him over the terrain and settles his feet itself,
        // and vanilla gravity fighting those teleports is how a stand ends up jittering in the air.
        stand.setGravity(false);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setSmall(false);
        // The stand is the only hitbox the model has (the displays must stay un-hittable), and the
        // body is ~2.1 blocks tall: a vanilla stand stops at 1.975, leaving the head top and the
        // shoulder line outside the box. Scaling the stand up keeps the visible body hittable.
        AttributeInstance scale = stand.getAttribute(Attribute.SCALE);
        if (scale != null) scale.setBaseValue(hitboxScale);
        stand.setInvulnerable(false);
        stand.setCollidable(true);
        stand.setCanPickupItems(false);
        stand.customName(MscText.title(DARK_AQUA, "JackStar — El Arquitecto del Sistema"));
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
        inst.bossBar = MscBossBar.create(
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
                // The bar shows to whoever is close to the boss; the helper also drops anyone who
                // logged out or left the world, which the old distance sweep could never notice.
                MscBossBar.showNear(inst.bossBar, inst.stand.getLocation(), aggroRange);
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
        DisplaySuit.remove(world, inst.partDisplays.values());
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

    // Not ignoreCancelled: the server fires the damage event of an invisible armour stand already
    // cancelled (vanilla stands that are invisible take no hits), so with ignoreCancelled this never
    // ran for the stand and JackStar could not be hurt at all. The hit is applied to his virtual
    // health here and the event stays cancelled.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();

        // A hit Jack lands on a player: pair the intended damage with what the player actually took.
        if (victim instanceof Player target && isJackAttacker(event.getDamager())) {
            plugin.getBossDamageLog().record(target.getUniqueId(), BossDamageSample.dealt(BossId.JACK_STAR,
                    outgoingSource == null ? "Attack" : outgoingSource,
                    outgoingSource == null ? event.getFinalDamage() : outgoingIntended,
                    event.getFinalDamage(), System.currentTimeMillis()));
            return;
        }

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

        // A summoned subprocess is extra pressure, never a shield: hits land on JackStar even while
        // one is alive, which is what makes the boss damageable at all times.
        // Projectile Packet Loss
        if (event.getDamager() instanceof Projectile projectile) {
            double effPacketLoss = JackResilience.effectiveChance(packetLossChance, inst.currentScale);
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

            double incoming = Math.max(1.0, event.getFinalDamage());
            List<Player> party = partyOf(stand);
            JackResilience.Resolution hit = JackResilience.resolve(incoming, random.nextDouble(),
                    JackResilience.effectiveChance(dodgeChance, inst.currentScale), party.size());
            if (hit.dodged()) {
                triggerMuiDodge(stand, player);
                recordIncoming(player, incoming, 0.0, "ultra instinct dodge");
                return;
            }

            double damage = hit.toBoss();
            shareWithParty(stand, party, hit.split());
            recordIncoming(player, incoming, damage,
                    damage < incoming ? "load balancer: " + (incoming - damage) + " shared" : "");

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

    /** Damages a player with this boss and remembers the attack so the listener can name it. */
    private void dealToPlayer(ArmorStand stand, Player target, double amount, String source) {
        // Saved and restored rather than cleared: a hit can trigger a nested one (the reflect
        // barrier hits back), and the outer call must find its own attack name again afterwards.
        String previousSource = outgoingSource;
        double previousIntended = outgoingIntended;
        outgoingSource = source;
        outgoingIntended = amount;
        try {
            target.damage(amount, stand);
        } finally {
            outgoingSource = previousSource;
            outgoingIntended = previousIntended;
        }
    }

    /** Records an incoming hit's split for {@code /msc debug}. */
    private void recordIncoming(Player player, double incoming, double applied, String note) {
        plugin.getBossDamageLog().record(player.getUniqueId(), BossDamageSample.taken(BossId.JACK_STAR,
                "Incoming hit", incoming, applied, note, System.currentTimeMillis()));
    }

    /** Whether a damage source is one of this boss's armor stands. */
    private static boolean isJackAttacker(Entity damager) {
        if (damager instanceof ArmorStand stand) return stand.getScoreboardTags().contains(TAG);
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof ArmorStand stand) {
            return stand.getScoreboardTags().contains(TAG);
        }
        return false;
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
        /** Hitbox scale last written to the stand, so the attribute is only touched when it changes. */
        public double appliedHitboxScale = -1;
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
        public JackMoves.Move move = JackMoves.Move.NONE;
        public int moveTick;
        /** The first signature move waits a few seconds into the fight. */
        public int moveCooldown = 160;
        public final List<Hop> hops = new ArrayList<>();
        public final List<Bit> bits = new ArrayList<>();
        public final List<Vector[]> frames = new ArrayList<>();
        public final java.util.Set<UUID> struck = new java.util.HashSet<>();
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
