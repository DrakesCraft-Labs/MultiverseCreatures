package com.Chagui68.entities;

import com.Chagui68.entities.boss.BossArena;
import com.Chagui68.utils.DisplaySuit;
import com.Chagui68.utils.MscBossBar;
import com.Chagui68.utils.MscEntityUtils;
import com.Chagui68.MultiverseCreatures;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Kinger implements Listener {

    /**
     * A rigid limb: every piece in a group revolves around that group's one joint, so a shin follows
     * its thigh and a boot plate follows its leg instead of each piece swinging from its own anchor.
     */
    public enum LimbGroup {
        HEAD, TORSO, ARM_RIGHT, ARM_LEFT, LEG_RIGHT, LEG_LEFT
    }

    public enum KingerPart {
        ARM_LEFT("StormStormy",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ0ODExNCwKICAicHJvZmlsZUlkIiA6ICI5MWYwNGZlOTBmMzY0M2I1OGYyMGUzMzc1Zjg2ZDM5ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9ybVN0b3JteSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82NTAwNmQxMmZlOGM3YWNhOTBhOGU4NzEwMDI4ZjZkOWVhODVmNDE2OGZhOWEyNmQxYWVlYTZiOTZhYzZlOWEyIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.1276971324f, 0.3221145439f, 0.0041503481f, 0.3886122987f, -0.2949017661f, 0.1617527436f, 0.002580528f, 0.6775875205f, 0.0005826184f, -0.0078345397f, 0.3280719816f, 0.501853708f, 0f, 0f, 0f, 1f}),
        ARM_RIGHT("PatatjeMC",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ1MDYzNCwKICAicHJvZmlsZUlkIiA6ICIxMjE4YWNiNDJiYzA0MzY4YjIxOTU4ZTZiYWU2NDMyMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJQYXRhdGplTUMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg0YmIxN2I5MmE1MzBkMjI3NDlmMGM0ODg3ZmVkNmI4OTg5NjMyNTUwYTE5NWMxNzQzMTk2NmExMWE0ZWQ2OCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.1201950133f, -0.289781228f, 0.0080174534f, 0.634953709f, 0.277548711f, 0.1455017004f, -0.0049849465f, 0.6814326334f, 0.0010592712f, 0.0136209663f, 0.3279271088f, 0.4967771821f, 0f, 0f, 0f, 1f}),
        LEG_RIGHT_LOWER("Roco_cop",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ1Mjk3OCwKICAicHJvZmlsZUlkIiA6ICIyOWM2MTQyM2MwMTc0YWI4ODgwZWM2MzBlYzIyYmZjMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSb2NvX2NvcCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lMDExM2ZlNTkyMjhlNmIyZjhjYzM3YzUxZDZmYzBhNWQ1NjhkMThhM2UyNzQ3MmZjOGFjNmFmNzI5ZmMxMDk1IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.3618164063f, 0f, 0f, 0.5950490686f, 0f, 0.451171875f, 0f, 0.232635498f, 0f, 0f, 0.5535f, 0.5025390625f, 0f, 0f, 0f, 1f}),
        LEG_RIGHT_UPPER("raxitocl",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ1NDUxNywKICAicHJvZmlsZUlkIiA6ICJkMTQ4NjFiM2UwZmM0Njk5OTFlMTcyNTllMzdiZjZhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJyYXhpdG9jbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80ZjI0NDBhMWFkNjA1MDMzYzJiYmU1MTkxOGU3NmU0NWZjOTQwYzA4NmUwZWVjZjM1ZjY3NDdiNTYyZDFhYjQ3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.3618164063f, 0f, 0f, 0.5950490686f, 0f, 0.90234375f, 0f, 0.683807373f, 0f, 0f, 0.5535f, 0.5025390625f, 0f, 0f, 0f, 1f}),
        LEG_LEFT_LOWER("SloppierPawJob",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ1NjEzMywKICAicHJvZmlsZUlkIiA6ICIwYmEyOTY4NDc3ZTc0NjYwODAzYThlOWIxMmQwNGU3NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTbG9wcGllclBhd0pvYiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81OGEwYjBjMWY0ZjVlNDFlNTg0NGFkYjI4YTQwOGRhZTIwYjBmODJmNGNhYjk2ZmJkY2M5NTFjNDA0ZjI1NmJmIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.3618164063f, 0f, 0f, 0.409919674f, 0f, 0.451171875f, 0f, 0.2333874512f, 0f, 0f, 0.5535f, 0.5025390625f, 0f, 0f, 0f, 1f}),
        LEG_LEFT_UPPER("EggyButton2411",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ1NzI1NiwKICAicHJvZmlsZUlkIiA6ICJjYmYxNGIxMGJhNWU0NzgwYjIyNmFiNmQzOTUxODk4YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJFZ2d5QnV0dG9uMjQxMSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hODUwZjE0N2Y2OTZlNmUyMTQzMTYxOTVjNGFkMGIxZDNmNjc1NmJlMTRjNzJjNDVhNzQ5ZmVjZWQzZWYzYTJhIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.3618164063f, 0f, 0f, 0.409919674f, 0f, 0.90234375f, 0f, 0.6845593262f, 0f, 0f, 0.5535f, 0.5025390625f, 0f, 0f, 0f, 1f}),
        TORSO_UPPER("LEATHER_LEGGINGS",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ1OTg4OCwKICAicHJvZmlsZUlkIiA6ICIyZDFhMzI0YjRhNDE0ODJmODNjYzk3YTA2NzY5YjI2ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJMRUFUSEVSX0xFR0dJTkdTIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM1MmEyZmM0N2RkMzgwZjZmYjE2ZThkZmFkOWI2ZDM3N2UzNzgzZGNhMThiNDgyYzQyOTc1NWRhNDg5ZWZiNjQiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.73328125f, 0f, 0f, 0.5022069787f, 0f, 0.78203125f, 0f, 1.2012714355f, 0f, 0f, 0.5625f, 0.5028765625f, 0f, 0f, 0f, 1f}),
        TORSO_LOWER("xentany",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ2MTQ5MCwKICAicHJvZmlsZUlkIiA6ICI3MjU1MDA3NjQzYzQ0YTZiYjM3MjJlNzc3OTk5OTFkOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4ZW50YW55IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQwNGY4OWZiYmE4Nzc4MGM1NzYxZDIyZDIyMDlkYWNlODIwMzdkMmVjNWM4MzQyOTM1ZDUzYjNhYjI3NmZkMmYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.73328125f, 0f, 0f, 0.5021828576f, 0f, 0.391015625f, 0f, 0.810135498f, 0f, 0f, 0.5625f, 0.5025390625f, 0f, 0f, 0f, 1f}),
        HEAD("PrinceCR",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ2MzUwMiwKICAicHJvZmlsZUlkIiA6ICI4MDQ2MzdjMTA1ZGY0MzM0ODE3YTNmMDcxMTMyOTYyMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJQcmluY2VDUiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84NDAwNDk0MjNjNzlmNTcyYzRmZDZkZDMxNmQ0NmY4NjQ3ODRiNTRmMzJmMDI5Nzg0ZWNjZWRkZjBhNWE2MTM1IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.380859375f, 0f, 0f, 0.5052224005f, 0f, 0.6698198427f, 0.0066809993f, 1.5827280655f, 0f, -0.0143164272f, 0.4686786071f, 0.4711609839f, 0f, 0f, 0f, 1f}),
        NECK("_pakman_",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ2NTI5MywKICAicHJvZmlsZUlkIiA6ICIwNDg2YWUwMWI4Y2I0OWUzODMyZDcwOTNmMWJlNzI3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJfcGFrbWFuXyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jYmUzZDJlY2ViNzk1ZDQxZTFjZTU5ZTZmNjdkNzM0ZGUzYWNjMjU3MTYzYWY5MzcyYjQxOGVlMGM4NWViYzg0IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.2380371094f, 0f, 0f, 0.5052224005f, 0f, 0.4784427448f, 0.0041756246f, 1.3913218155f, 0f, -0.0102260194f, 0.2929241294f, 0.4711609839f, 0f, 0f, 0f, 1f}),
        EYE_RIGHT("MineSkin_14",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ2NzcxMCwKICAicHJvZmlsZUlkIiA6ICI5MDczN2E1N2RlYjk0MWYxYTEyMzE1MmJkZmZjMTBmYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5lU2tpbl8xNCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iNzBlNTIxZWZlOTI2Yzk2MjVmNTEyNTAxZTFiMzU0MTk4OTdmNjc0ODU3MTdhOGJkZDEzOTAyNjQyN2ZlMmMyIgogICAgfQogIH0KfQ==",
                new float[]{0.2344292468f, -0.0246395067f, -0.0265026901f, 0.5764186975f, 0.0272583744f, 0.2371247105f, 0.013221886f, 1.511015625f, 0.0383150384f, -0.0245761095f, 0.2315287091f, 0.34375f, 0f, 0f, 0f, 1f}),
        CROWN("_pakman_",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ2NTI5MywKICAicHJvZmlsZUlkIiA6ICIwNDg2YWUwMWI4Y2I0OWUzODMyZDcwOTNmMWJlNzI3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJfcGFrbWFuXyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jYmUzZDJlY2ViNzk1ZDQxZTFjZTU5ZTZmNjdkNzM0ZGUzYWNjMjU3MTYzYWY5MzcyYjQxOGVlMGM4NWViYzg0IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.1904296875f, 0f, 0f, 0.5052224005f, 0f, 0.5741312937f, 0.0033404997f, 1.619095253f, 0f, -0.0122712233f, 0.2343393036f, 0.4711609839f, 0f, 0f, 0f, 1f}),
        CROSS("8b2ca111504dde50",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ3MDAxNywKICAicHJvZmlsZUlkIiA6ICIzOTg5OGFiODFmMjU0NmQxOGIyY2ExMTE1MDRkZGU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICI4YjJjYTExMTUwNGRkZTUwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q5YmIyNWI4ZjcyMDMyYWM1MDc2MzAxMzM2YjhjNDcxY2FmNTZiOTNhM2MyYzNmNmFhMDQzYzg2MDI5ZGM4MGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.0476074219f, 0f, 0f, 0.5050075647f, 0f, 0.3349099213f, 0.0016702498f, 1.7741992188f, 0f, -0.0071582136f, 0.1171696518f, 0.4708984375f, 0f, 0f, 0f, 1f}),
        CROSS_BAR("8b2ca111504dde50",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ3MDAxNywKICAicHJvZmlsZUlkIiA6ICIzOTg5OGFiODFmMjU0NmQxOGIyY2ExMTE1MDRkZGU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICI4YjJjYTExMTUwNGRkZTUwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q5YmIyNWI4ZjcyMDMyYWM1MDc2MzAxMzM2YjhjNDcxY2FmNTZiOTNhM2MyYzNmNmFhMDQzYzg2MDI5ZGM4MGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0f, 0.2856010262f, 0.0016617282f, 0.5764186975f, -0.0123848957f, -0.0048400124f, 0.0924280407f, 1.7048144531f, 0.0565972164f, -0.0015880131f, 0.0303257374f, 0.4603515625f, 0f, 0f, 0f, 1f}),
        EYE_LEFT("ThadomInator478",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ3MTY2NSwKICAicHJvZmlsZUlkIiA6ICIzMzU3MWJiY2UyMDE0MTRiYmNkMDYyMjEyZTI4MjBlMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGFkb21JbmF0b3I0NzgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjcyMjQ5NDc3ODNlYjNkMjA3ZTZlZjM2M2JiOTAyMmU5ZmYwMWZjNDc5ZDg4NDRjZDQ4MmIwNjc5MWNkNTczYyIKICAgIH0KICB9Cn0=",
                new float[]{0.2338185397f, 0.0370332185f, 0.0199053226f, 0.4335964318f, -0.035613916f, 0.2360094161f, -0.0132786824f, 1.4631640625f, -0.0333698349f, 0.0154059474f, 0.2325232711f, 0.34375f, 0f, 0f, 0f, 1f});

        /**
         * The limb this piece belongs to. The whole wooden head — neck, face, crown, cross and both
         * eyes — turns as one; the two robe pieces are the torso; each sleeve is an arm.
         *
         * <p>The names follow the skins, not the old guesses: the two pieces once called the arms are
         * the eyes (white, blue iris), which is why Kinger used to "attack with his eyes", and the
         * two once called boot plates are the purple sleeves with white hands.
         */
        public LimbGroup group() {
            return switch (this) {
                case NECK, HEAD, CROWN, CROSS, CROSS_BAR, EYE_RIGHT, EYE_LEFT -> LimbGroup.HEAD;
                case TORSO_UPPER, TORSO_LOWER -> LimbGroup.TORSO;
                case ARM_RIGHT -> LimbGroup.ARM_RIGHT;
                case ARM_LEFT -> LimbGroup.ARM_LEFT;
                case LEG_RIGHT_UPPER, LEG_RIGHT_LOWER -> LimbGroup.LEG_RIGHT;
                case LEG_LEFT_UPPER, LEG_LEFT_LOWER -> LimbGroup.LEG_LEFT;
            };
        }

        public final String profileName;
        public final String texture;
        public final float[] matrix;
        public final Vector3f offset;
        public final Quaternionf rotation;
        public final Vector3f scale;

        KingerPart(String profileName, String texture, float[] matrix) {
            this.profileName = profileName;
            this.texture = texture;
            this.matrix = matrix;
            Matrix4f m = new Matrix4f().set(
                    matrix[0], matrix[4], matrix[8], matrix[12],
                    matrix[1], matrix[5], matrix[9], matrix[13],
                    matrix[2], matrix[6], matrix[10], matrix[14],
                    matrix[3], matrix[7], matrix[11], matrix[15]);
            offset = new Vector3f();
            scale = new Vector3f();
            rotation = new Quaternionf();
            m.getTranslation(offset);
            m.getScale(scale);
            Matrix4f normalized = new Matrix4f(m);
            if (scale.x != 0 && scale.y != 0 && scale.z != 0) {
                normalized.scale(1f / scale.x, 1f / scale.y, 1f / scale.z);
            }
            normalized.getUnnormalizedRotation(rotation);
        }

        public static final Vector3f CENTER;

        static {
            // The torso is the axis the whole suit hangs from: both robe pieces and the four leg
            // pieces share its x/z to within a millimetre, so centring on it is what puts the visible
            // body over the invisible armour stand that carries the hitbox. The average of all fifteen
            // anchors would be dragged forward by the eyes, which sit on the front of the face.
            Vector3f axis = new Vector3f(TORSO_UPPER.offset).add(TORSO_LOWER.offset).mul(0.5f);
            CENTER = new Vector3f(axis.x, 0f, axis.z);
        }
    }

    public static final String TAG = "MSC_Kinger";
    public static final String PART_TAG = "MSC_KingerPart";

    /**
     * Part ownership, so a reload continues the suit it already has instead of building a second,
     * overlapping one: every piece carries its own name tag plus the tag of its stand.
     */
    static final String PART_OWNER_TAG_PREFIX = "MSC_KingerOwner_";

    /**
     * Scale of the invisible armour stand that carries the hitbox.
     *
     * <p>The body — robe, legs and head — reaches under 0.2 blocks from the torso axis and 1.78
     * blocks up, so a plain unscaled armour stand (0.5 wide, 1.975 tall) covers it; only the two
     * small hands poke a few centimetres out of the sides. The old literal 2.0 doubled the box in
     * every direction, so a swing aimed a block clear of the chess piece still landed on it.
     */
    public static final double MODEL_HITBOX_SCALE = 1.0;

    private static final String BAR_TITLE = ChatColor.DARK_PURPLE + "Kinger";
    private static final String BULLET_TAG = "MSC_KingerBullet";

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, KingerInstance> activeKingers = new java.util.HashMap<>();

    private double health;
    private double aggroRange;
    private double moveSpeed;
    private double meleeRange;
    private double meleeDamage;
    private double meleeRadius;
    private double rangedRange;
    private double rangedDamage;
    private int meleeCooldownTicks;
    private int rangedCooldownTicks;
    private int meleeAnimTicks = 12;
    private int rangedAnimTicks = 20;
    private double armorStandChance;
    /** Size of the invisible stand the suit is hit through; {@link #MODEL_HITBOX_SCALE} is the default. */
    private double hitboxScale = MODEL_HITBOX_SCALE;

    public Kinger(MultiverseCreatures plugin) {
        this.plugin = plugin;
        reloadConfig();
        if (!plugin.isEnabled("entities.kinger")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        reloadExisting();
        startTicker();
    }

    public void reloadConfig() {
        var config = plugin.getConfig();
        health = config.getDouble("entities.kinger.health", 120.0);
        aggroRange = config.getDouble("entities.kinger.aggro-range", 25.0);
        moveSpeed = config.getDouble("entities.kinger.move-speed", 0.32);
        meleeRange = config.getDouble("entities.kinger.melee-range", 3.0);
        meleeDamage = config.getDouble("entities.kinger.melee-damage", 8.0);
        meleeRadius = config.getDouble("entities.kinger.melee-radius", 3.5);
        rangedRange = config.getDouble("entities.kinger.ranged-range", 30.0);
        rangedDamage = config.getDouble("entities.kinger.ranged-damage", 6.0);
        meleeCooldownTicks = config.getInt("entities.kinger.melee-cooldown-ticks", 25);
        rangedCooldownTicks = config.getInt("entities.kinger.ranged-cooldown-ticks", 45);
        meleeAnimTicks = config.getInt("entities.kinger.melee-anim-ticks", 12);
        rangedAnimTicks = config.getInt("entities.kinger.ranged-anim-ticks", 20);
        armorStandChance = config.getDouble("entities.kinger.spawn-on-armorstand-chance", 0.01);
        hitboxScale = MscEntityUtils.clampHitboxScale(
                config.getDouble("entities.kinger.hitbox-scale", MODEL_HITBOX_SCALE));
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandPlace(EntityPlaceEvent event) {
        if (event.getEntityType() != EntityType.ARMOR_STAND) return;
        if (event.getPlayer() == null) return;
        if (!plugin.getConfig().getBoolean("entities.kinger.enabled", true)) return;
        if (random.nextDouble() >= armorStandChance) return;
        Location loc = event.getEntity().getLocation();
        event.setCancelled(true);
        trySpawn(loc);
    }

    private void reloadExisting() {
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (!stand.getScoreboardTags().contains(TAG)) continue;
                // A boss from a build that never wrote its virtual health would otherwise get a bar
                // that can never move off empty, because progress comes from that stored number.
                if (!stand.getPersistentDataContainer().has(MscEntityUtils.KEY_VIRTUAL_MAX_HEALTH,
                        PersistentDataType.DOUBLE)) {
                    MscEntityUtils.initVirtualHealth(stand, health);
                }
                KingerInstance inst = new KingerInstance(stand);
                restorePartDisplays(inst);
                activeKingers.put(stand.getUniqueId(), inst);
                setupBossBar(inst);
            }
            for (ItemDisplay display : world.getEntitiesByClass(ItemDisplay.class)) {
                if (!display.getScoreboardTags().contains(PART_TAG)) continue;
                boolean hasOwner = display.getScoreboardTags().stream()
                        .anyMatch(tag -> tag.startsWith(PART_OWNER_TAG_PREFIX));
                // Pieces made by pre-ownership builds cannot be reattached to a stand, and pieces of an
                // older suit carry names that mean something else now: removing both is the only way
                // to avoid a second suit standing inside the real one.
                if (!hasOwner || !isCurrentPiece(display.getScoreboardTags())) {
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
     * Reattaches the pieces a previous run already spawned for this stand, so enabling the plugin
     * over a live Kinger continues his suit instead of building a second one on top of it.
     */
    private void restorePartDisplays(KingerInstance inst) {
        String ownerTag = partOwnerTag(inst.stand.getUniqueId());
        for (ItemDisplay display : inst.stand.getWorld().getEntitiesByClass(ItemDisplay.class)) {
            if (!display.getScoreboardTags().contains(PART_TAG)
                    || !display.getScoreboardTags().contains(ownerTag)) continue;
            for (KingerPart part : KingerPart.values()) {
                if (display.getScoreboardTags().contains(partTag(part))) {
                    inst.partDisplays.put(part, display.getUniqueId());
                    break;
                }
            }
        }
    }

    /**
     * Version of the suit's piece tags. Bumped when the pieces were renamed after what they really
     * are: a piece spawned by an older build carries an old name — an "arm" that is really an eye —
     * so it is never adopted under the new one; {@link #reloadExisting} removes it and the next sync
     * dresses the stand again.
     */
    static final String SUIT_VERSION = "v2";

    /** Every piece carries its own tag, so an adoption can tell the pieces apart. */
    static String partTag(KingerPart part) {
        return PART_TAG + "_" + SUIT_VERSION + "_" + part.name();
    }

    /** Whether a piece was spawned by this build's suit, under one of the current piece names. */
    static boolean isCurrentPiece(java.util.Set<String> tags) {
        for (KingerPart part : KingerPart.values()) {
            if (tags.contains(partTag(part))) return true;
        }
        return false;
    }

    static String partOwnerTag(UUID ownerId) {
        return PART_OWNER_TAG_PREFIX + ownerId.toString().replace("-", "");
    }

    /**
     * The loop that walks this mob's instances. Held so {@link #stopTasks()} can end it: a task
     * nobody holds cannot be cancelled, and a second start would leave two loops walking the same
     * state.
     */
    private BukkitTask ticker;

    private void startTicker() {
        if (ticker != null) ticker.cancel();
        ticker = new BukkitRunnable() {
            @Override
            public void run() {
                for (KingerInstance inst : new ArrayList<>(activeKingers.values())) {
                    tick(inst);
                }
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

    private void tick(KingerInstance inst) {
        ArmorStand stand = inst.stand;
        if (stand.isDead() || !stand.isValid()) {
            cleanup(inst);
            activeKingers.remove(stand.getUniqueId());
            return;
        }
        if (!stand.getWorld().isChunkLoaded(stand.getLocation().getChunk())) return;

        Player target = findTarget(stand);
        inst.targetId = (target != null) ? target.getUniqueId() : null;

        if (target != null) {
            double distSq = stand.getLocation().distanceSquared(target.getLocation());
            inst.moving = distSq > 3.24; // 1.8^2
            if (inst.moving && distSq <= (aggroRange * aggroRange)) {
                moveTowards(stand, target.getLocation());
            }
            faceTarget(stand, target);
            double meleeRangeSq = meleeRange * meleeRange;
            boolean swinging = inst.meleeAnim > 0;
            if (distSq <= meleeRangeSq && inst.meleeCooldown <= 0 && !swinging) {
                // Wind-up only: the hit lands at the top of the swing, below.
                inst.meleeAnim = meleeAnimTicks;
                inst.meleeCooldown = meleeCooldownTicks;
                inst.meleePending = true;
            } else if (distSq > meleeRangeSq && distSq <= (rangedRange * rangedRange) && inst.rangedCooldown <= 0
                    && !swinging && inst.rangedAnim <= 0 && stand.hasLineOfSight(target)) {
                // He raises his hand first; the shot leaves it once the arm points at the target.
                inst.rangedAnim = rangedAnimTicks;
                inst.rangedCooldown = rangedCooldownTicks;
                inst.rangedPending = true;
            }
        } else {
            inst.moving = false;
        }

        // The damage used to be dealt on the tick the swing started, before the arms had moved, so a
        // player was hit by a pose that had not happened yet. It lands at the peak now, on whoever
        // is in front of him at that moment.
        if (inst.meleePending && isMeleeImpactTick(inst.meleeAnim, meleeAnimTicks)) {
            inst.meleePending = false;
            meleeAttack(stand);
        }
        if (inst.rangedPending && progress(inst.rangedAnim, rangedAnimTicks) >= KingerModel.RANGED_FIRE_PROGRESS) {
            inst.rangedPending = false;
            if (target != null) rangedAttack(inst, target);
        }

        snapToGround(stand);

        if (inst.moving) inst.animTicks += KingerModel.WALK_RATE;
        if (inst.meleeAnim > 0) inst.meleeAnim--;
        if (inst.rangedAnim > 0) inst.rangedAnim--;
        if (inst.meleeCooldown > 0) inst.meleeCooldown--;
        if (inst.rangedCooldown > 0) inst.rangedCooldown--;

        inst.tickCount++;

        // Synchronize displays locked to stand location (throttled when idle)
        if (DisplaySuit.shouldSync(inst.moving || inst.meleeAnim != 0 || inst.rangedAnim != 0, inst.tickCount)) {
            syncDisplays(inst);
        }

        if (inst.bossBar != null) {
            double current = MscEntityUtils.getVirtualHealth(stand);
            double max = MscEntityUtils.getVirtualMaxHealth(stand);
            inst.bossBar.setProgress(MscEntityUtils.calculateVirtualProgress(current, max));
            // A bar is a packet per player, not a world object: taking a fresh look once a second is
            // what makes a login, a logout or a world change correct itself.
            if (inst.tickCount % 20 == 0) {
                MscBossBar.showInWorld(inst.bossBar, stand.getWorld());
            }
        }
    }

    private void moveTowards(ArmorStand stand, Location target) {
        Location loc = stand.getLocation();
        Vector dir = target.toVector().subtract(loc.toVector());
        dir.setY(0);
        double dist = dir.length();
        if (dist < 0.01) return;
        dir.normalize();
        double step = Math.min(moveSpeed, dist);
        if (!BossArena.walk(loc, dir.multiply(step), true)) return;
        loc.setPitch(0);
        stand.teleport(loc);
    }

    private void faceTarget(ArmorStand stand, Player target) {
        Location loc = stand.getLocation();
        Vector to = target.getLocation().toVector().subtract(loc.toVector()).setY(0);
        // Standing inside him: a zero direction would tip the stand's pitch to the vertical.
        if (to.lengthSquared() < 1.0e-4) return;
        loc.setDirection(to);
        stand.teleport(loc);
    }

    /** Keeps his feet on the floor: falls off ledges, never hangs over a drop deeper than a few blocks. */
    private void snapToGround(ArmorStand stand) {
        Location loc = stand.getLocation();
        double before = loc.getY();
        BossArena.settle(loc);
        if (Math.abs(loc.getY() - before) > 0.001) stand.teleport(loc);
    }

    private void hitEffect(ArmorStand stand) {
        Location loc = stand.getLocation().clone().add(0, 1, 0);
        stand.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 8, 0.4, 0.6, 0.4, 0.1);
        stand.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.1f);
    }

    /**
     * Whether the swing has reached its peak this tick. The arms follow {@code sin(progress * PI)},
     * which tops out halfway through the animation; {@code remaining} counts down from {@code total}.
     */
    static boolean isMeleeImpactTick(int remaining, int total) {
        if (total <= 0) return true;
        return remaining <= (total + 1) / 2;
    }

    /**
     * Whether a player stands where the swing reaches: within the radius as a sphere (the old cube
     * reached 40% further along its diagonals) and not behind him.
     */
    static boolean inMeleeArc(Vector facing, Vector toPlayer, double radius) {
        if (toPlayer.lengthSquared() > radius * radius) return false;
        Vector flat = toPlayer.clone().setY(0);
        if (flat.lengthSquared() < 0.25) return true; // standing on top of him
        Vector forward = facing.clone().setY(0);
        if (forward.lengthSquared() < 1e-6) return true;
        return forward.normalize().dot(flat.normalize()) >= MELEE_ARC_COS;
    }

    /** Cosine of the half-angle the swing covers: 0.0 is the whole front half. */
    static final double MELEE_ARC_COS = 0.0;

    private void meleeAttack(ArmorStand stand) {
        World world = stand.getWorld();
        Location loc = stand.getLocation();
        world.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.6f);
        world.spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 40, 1.5, 1.0, 1.5, 0,
                new Particle.DustOptions(Color.fromRGB(0xAA00FF), 1.6f));
        world.spawnParticle(Particle.LARGE_SMOKE, loc.clone().add(0, 0.5, 0), 20, 1.2, 0.8, 1.2, 0.02);

        Vector facing = loc.getDirection();
        for (Entity e : world.getNearbyEntities(loc, meleeRadius, meleeRadius, meleeRadius)) {
            if (!(e instanceof Player p)) continue;
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
            if (!inMeleeArc(facing, p.getLocation().toVector().subtract(loc.toVector()), meleeRadius)) continue;
            p.damage(meleeDamage, stand);
            Vector away = p.getLocation().toVector().subtract(loc.toVector());
            if (away.lengthSquared() < 0.01) away = new Vector(0, 0, -1);
            away.normalize();
            p.setVelocity(away.multiply(1.3).setY(0.45));
        }
    }

    private void rangedAttack(KingerInstance inst, Player target) {
        ArmorStand stand = inst.stand;
        World world = stand.getWorld();
        Location hand = modelToWorld(stand, KingerModel.rightHand(pose(inst)));
        world.playSound(hand, Sound.ENTITY_SHULKER_SHOOT, 1.0f, 1.2f);
        world.spawnParticle(Particle.DUST, hand, 12, 0.3, 0.3, 0.3, 0,
                new Particle.DustOptions(Color.fromRGB(0xBB66FF), 1.2f));
        ShulkerBullet bullet = (ShulkerBullet) world.spawnEntity(hand, EntityType.SHULKER_BULLET);
        bullet.addScoreboardTag(BULLET_TAG);
        bullet.setShooter(stand);
        bullet.setTarget(target);
        Vector vel = MscEntityUtils.direction(hand.toVector(), target.getEyeLocation().toVector(),
                stand.getLocation().getDirection()).multiply(1.5);
        bullet.setVelocity(vel);
        bullet.setSilent(true);
        bullet.setGlowing(true);
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

    private void syncDisplays(KingerInstance inst) {
        ArmorStand stand = inst.stand;
        Location root = standRoot(stand);
        KingerModel.Pose pose = pose(inst);
        boolean dressed = false;
        for (KingerPart part : KingerPart.values()) {
            UUID id = inst.partDisplays.get(part);
            Entity e = (id != null) ? root.getWorld().getEntity(id) : null;
            if (e instanceof ItemDisplay display && display.isValid()) {
                display.teleport(root);
                display.setTransformation(KingerModel.compose(part, pose));
            } else {
                // A reload with the piece's chunk unloaded hides it from restorePartDisplays;
                // adopting the piece still tagged for this stand avoids a second, overlapping suit.
                ItemDisplay adopted = findPartDisplay(inst, part);
                if (adopted != null) {
                    inst.partDisplays.put(part, adopted.getUniqueId());
                } else {
                    ItemDisplay display = spawnPart(root, part, stand.getUniqueId());
                    inst.partDisplays.put(part, display.getUniqueId());
                    dressed = true;
                }
            }
        }
        // A stand dressed again after its chunk came back may still wear the pieces of an older
        // suit that the startup sweep could not see.
        if (dressed) removeStalePieces(stand);
    }

    /** Removes this stand's pieces that an older build spawned under names that mean something else now. */
    private void removeStalePieces(ArmorStand stand) {
        String ownerTag = partOwnerTag(stand.getUniqueId());
        for (Entity entity : stand.getWorld().getNearbyEntities(stand.getLocation(), 6.0, 8.0, 6.0)) {
            if (entity instanceof ItemDisplay display && display.getScoreboardTags().contains(ownerTag)
                    && !isCurrentPiece(display.getScoreboardTags())) {
                display.remove();
            }
        }
    }

    /** The spot every piece hangs from: the stand's own location, facing the stand's own heading. */
    private Location standRoot(ArmorStand stand) {
        Location loc = stand.getLocation().clone();
        loc.setYaw(stand.getLocation().getYaw() + 180);
        loc.setPitch(0);
        return loc;
    }

    /** Looks for this piece of this boss near the stand, without loading anything new. */
    private ItemDisplay findPartDisplay(KingerInstance inst, KingerPart part) {
        return DisplaySuit.find(inst.stand.getWorld(), inst.stand.getLocation(),
                tags(part, inst.stand.getUniqueId()));
    }

    /** The world point a model-space point of the suit is drawn at: where the right hand fires from. */
    private Location modelToWorld(ArmorStand stand, Vector3f point) {
        Location base = standRoot(stand);
        double yawRad = Math.toRadians(base.getYaw());
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);
        base.add(point.x * cos - point.z * sin, point.y, point.x * sin + point.z * cos);
        return base;
    }

    /**
     * Spawns one piece of the suit. Everything a display piece needs — the head, the rest transform,
     * the zeroed interpolation and box, and the ownership tags — lives in {@link DisplaySuit}, so it
     * stays identical for every dressed boss instead of drifting apart per file.
     */
    private ItemDisplay spawnPart(Location root, KingerPart part, UUID ownerId) {
        return DisplaySuit.spawn(root, headOf(part), buildTransformation(part, null), tags(part, ownerId));
    }

    private ItemStack headOf(KingerPart part) {
        return DisplaySuit.head(part.profileName, part.texture, "Kinger");
    }

    private static DisplaySuit.SuitTags tags(KingerPart part, UUID ownerId) {
        return new DisplaySuit.SuitTags(PART_TAG, partTag(part), partOwnerTag(ownerId));
    }

    /** The transform of one piece in the pose Kinger is in this tick, or at rest before he has one. */
    private Transformation buildTransformation(KingerPart part, KingerInstance inst) {
        return KingerModel.compose(part, inst != null ? pose(inst) : KingerModel.Pose.rest());
    }

    /**
     * The pose for this tick: the walk, the swing or the shot, and the head following his target.
     * Built once per sync; the head looks from his eyes, not from his feet, so a player in front of
     * him is looked at instead of the floor at his feet.
     */
    private KingerModel.Pose pose(KingerInstance inst) {
        float melee = inst.meleeAnim > 0 ? progress(inst.meleeAnim, meleeAnimTicks) : -1f;
        float ranged = inst.rangedAnim > 0 ? progress(inst.rangedAnim, rangedAnimTicks) : -1f;
        float look = 0f;
        Player target = inst.targetId != null ? Bukkit.getPlayer(inst.targetId) : null;
        if (target != null && target.isOnline() && target.getWorld().equals(inst.stand.getWorld())) {
            Vector to = target.getEyeLocation().toVector()
                    .subtract(inst.stand.getLocation().toVector().add(new Vector(0, KingerModel.EYE_HEIGHT, 0)));
            look = KingerModel.lookPitch(to.getY(), Math.hypot(to.getX(), to.getZ()));
        }
        return KingerModel.pose(inst.animTicks, inst.moving, melee, ranged, look);
    }

    /** How far through an animation of {@code total} ticks one with {@code remaining} left is, in [0, 1]. */
    static float progress(int remaining, int total) {
        if (total <= 0) return 1f;
        return Math.max(0f, Math.min(1f, 1f - (float) remaining / total));
    }

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.kinger")) return false;
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        if (stand == null) return false;

        stand.setVisible(false);
        stand.setMarker(false);
        stand.setSmall(false);
        stand.setArms(false);
        stand.setBasePlate(false);
        stand.setGravity(false);
        stand.setInvulnerable(false);
        stand.setCollidable(true);
        stand.setCanPickupItems(false);
        stand.setSilent(true);
        stand.setAI(false);
        MscEntityUtils.applyAmbientPersistence(plugin, stand);
        stand.setMaximumNoDamageTicks(0);
        stand.addScoreboardTag(TAG);

        MscEntityUtils.initVirtualHealth(stand, health);

        AttributeInstance scaleAttr = stand.getAttribute(Attribute.SCALE);
        if (scaleAttr != null) scaleAttr.setBaseValue(hitboxScale);

        KingerInstance inst = new KingerInstance(stand);
        activeKingers.put(stand.getUniqueId(), inst);
        setupBossBar(inst);

        Location root = standRoot(stand);
        for (KingerPart part : KingerPart.values()) {
            ItemDisplay display = spawnPart(root, part, stand.getUniqueId());
            inst.partDisplays.put(part, display.getUniqueId());
        }

        location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
        return true;
    }

    private void cleanup(KingerInstance inst) {
        World world = (inst.stand != null) ? inst.stand.getWorld() : null;
        DisplaySuit.remove(world, inst.partDisplays.values());
        inst.partDisplays.clear();
        if (inst.bossBar != null) {
            inst.bossBar.removeAll();
            inst.bossBar = null;
        }
    }

    private void setupBossBar(KingerInstance inst) {
        BossBar bar = MscBossBar.create(BAR_TITLE, BarColor.PURPLE, BarStyle.SEGMENTED_10, BarFlag.DARKEN_SKY);
        MscBossBar.showInWorld(bar, inst.stand.getWorld());
        inst.bossBar = bar;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();

        if (damaged instanceof ArmorStand stand && stand.getScoreboardTags().contains(TAG)) {
            Player player = null;
            if (event.getDamager() instanceof Player p) {
                player = p;
            } else if (event.getDamager() instanceof Projectile projectile
                    && projectile.getShooter() instanceof Player p) {
                player = p;
            }
            if (player != null) {
                event.setCancelled(true);
                double damage = Math.max(1.0, event.getFinalDamage());
                reduceHealth(stand, damage);
                hitEffect(stand);
            }
            return;
        }

        if (damaged instanceof ItemDisplay display && display.getScoreboardTags().contains(PART_TAG)) {
            ArmorStand stand = findOwner(display);
            if (stand != null && !stand.isDead() && stand.isValid()) {
                event.setCancelled(true);
                reduceHealth(stand, Math.max(1.0, event.getDamage()));
                hitEffect(stand);
            }
            return;
        }

        Entity damager = event.getDamager();
        boolean damagerMsc = false;
        boolean damagedMsc = false;
        for (String tag : damager.getScoreboardTags()) {
            if (tag.startsWith("MSC_")) {
                damagerMsc = true;
                break;
            }
        }
        for (String tag : damaged.getScoreboardTags()) {
            if (tag.startsWith("MSC_")) {
                damagedMsc = true;
                break;
            }
        }
        if (damagerMsc && damagedMsc) {
            boolean involvesFrostGolem = damager.getScoreboardTags().contains("MSC_FrostGolem")
                    || damaged.getScoreboardTags().contains("MSC_FrostGolem");
            if (!involvesFrostGolem) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Resolves Kinger's bullet itself. A vanilla shulker bullet that hits an entity also gives it ten
     * seconds of Levitation, which no event before this one can strip, so every ranged hit used to
     * float the player off the ground; the old damage override never touched that. The impact is
     * cancelled here and the hit Kinger means — damage and a short Darkness — is applied instead.
     */
    @EventHandler(ignoreCancelled = true)
    public void onBulletHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof ShulkerBullet bullet)) return;
        if (!bullet.getScoreboardTags().contains(BULLET_TAG)) return;
        Entity hit = event.getHitEntity();
        if (hit == null) return; // a wall: the bullet just bursts, as usual

        event.setCancelled(true);
        Location at = bullet.getLocation();
        bullet.remove();
        at.getWorld().spawnParticle(Particle.DUST, at, 10, 0.2, 0.2, 0.2, 0,
                new Particle.DustOptions(Color.fromRGB(0xBB66FF), 1.2f));

        if (!(hit instanceof Player p)) return;
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;
        if (bullet.getShooter() instanceof ArmorStand stand && stand.isValid()) {
            p.damage(rangedDamage, stand);
        } else {
            p.damage(rangedDamage);
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, false));
    }

    private void reduceHealth(ArmorStand stand, double damage) {
        stand.setNoDamageTicks(0);
        double currentHealth = MscEntityUtils.getVirtualHealth(stand);
        double newHealth = Math.max(0, currentHealth - damage);
        MscEntityUtils.setVirtualHealth(stand, newHealth);

        KingerInstance inst = activeKingers.get(stand.getUniqueId());
        if (inst != null && inst.bossBar != null) {
            double maxHealth = MscEntityUtils.getVirtualMaxHealth(stand);
            inst.bossBar.setProgress(MscEntityUtils.calculateVirtualProgress(newHealth, maxHealth));
        }
    }

    private ArmorStand findOwner(Entity entity) {
        ArmorStand best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : entity.getNearbyEntities(2, 2, 2)) {
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

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        if (!stand.getScoreboardTags().contains(TAG)) return;

        event.getDrops().clear();
        event.setDroppedExp(0);

        KingerInstance inst = activeKingers.remove(stand.getUniqueId());
        if (inst != null) cleanup(inst);

        Location loc = stand.getLocation();
        World world = stand.getWorld();
        world.playSound(loc, Sound.ENTITY_WITHER_DEATH, 1.0f, 0.8f);
        world.spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 60, 1.0, 1.5, 1.0, 0,
                new Particle.DustOptions(Color.PURPLE, 2f));
        world.spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 1, 0), 8, 1.5, 1.5, 1.5, 0);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof ArmorStand stand)) return;
        if (!stand.getScoreboardTags().contains(TAG)) return;
        List<String> messages = plugin.getConfig().getStringList("entities.kinger.death-messages");
        if (!messages.isEmpty()) {
            String raw = messages.get(random.nextInt(messages.size()));
            event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', raw.replace("%player%", event.getEntity().getName())));
        }
    }

    public static class KingerInstance {
        public final ArmorStand stand;
        public final Map<KingerPart, UUID> partDisplays = new EnumMap<>(KingerPart.class);
        public BossBar bossBar;
        public UUID targetId;
        public int meleeCooldown;
        public int rangedCooldown;
        public int meleeAnim;
        public int rangedAnim;
        /** A swing has started and its hit has not landed yet. */
        public boolean meleePending;
        /** A shot has been wound up and has not left his hand yet. */
        public boolean rangedPending;
        public boolean moving;
        public float animTicks;
        public int tickCount;

        public KingerInstance(ArmorStand stand) {
            this.stand = stand;
        }
    }
}
