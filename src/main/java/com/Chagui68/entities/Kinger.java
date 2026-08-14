package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.entity.Display;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Kinger implements Listener {

    public enum KingerPart {
        BASE_LEFT("StormStormy",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ0ODExNCwKICAicHJvZmlsZUlkIiA6ICI5MWYwNGZlOTBmMzY0M2I1OGYyMGUzMzc1Zjg2ZDM5ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9ybVN0b3JteSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82NTAwNmQxMmZlOGM3YWNhOTBhOGU4NzEwMDI4ZjZkOWVhODVmNDE2OGZhOWEyNmQxYWVlYTZiOTZhYzZlOWEyIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.1276971324f, 0.3221145439f, 0.0041503481f, 0.3886122987f, -0.2949017661f, 0.1617527436f, 0.002580528f, 0.6775875205f, 0.0005826184f, -0.0078345397f, 0.3280719816f, 0.501853708f, 0f, 0f, 0f, 1f}),
        BASE_RIGHT("PatatjeMC",
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
        NECK("PrinceCR",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ2MzUwMiwKICAicHJvZmlsZUlkIiA6ICI4MDQ2MzdjMTA1ZGY0MzM0ODE3YTNmMDcxMTMyOTYyMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJQcmluY2VDUiIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84NDAwNDk0MjNjNzlmNTcyYzRmZDZkZDMxNmQ0NmY4NjQ3ODRiNTRmMzJmMDI5Nzg0ZWNjZWRkZjBhNWE2MTM1IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.380859375f, 0f, 0f, 0.5052224005f, 0f, 0.6698198427f, 0.0066809993f, 1.5827280655f, 0f, -0.0143164272f, 0.4686786071f, 0.4711609839f, 0f, 0f, 0f, 1f}),
        BELT("_pakman_",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ2NTI5MywKICAicHJvZmlsZUlkIiA6ICIwNDg2YWUwMWI4Y2I0OWUzODMyZDcwOTNmMWJlNzI3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJfcGFrbWFuXyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jYmUzZDJlY2ViNzk1ZDQxZTFjZTU5ZTZmNjdkNzM0ZGUzYWNjMjU3MTYzYWY5MzcyYjQxOGVlMGM4NWViYzg0IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.2380371094f, 0f, 0f, 0.5052224005f, 0f, 0.4784427448f, 0.0041756246f, 1.3913218155f, 0f, -0.0102260194f, 0.2929241294f, 0.4711609839f, 0f, 0f, 0f, 1f}),
        ARM_RIGHT("MineSkin_14",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ2NzcxMCwKICAicHJvZmlsZUlkIiA6ICI5MDczN2E1N2RlYjk0MWYxYTEyMzE1MmJkZmZjMTBmYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5lU2tpbl8xNCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iNzBlNTIxZWZlOTI2Yzk2MjVmNTEyNTAxZTFiMzU0MTk4OTdmNjc0ODU3MTdhOGJkZDEzOTAyNjQyN2ZlMmMyIgogICAgfQogIH0KfQ==",
                new float[]{0.2344292468f, -0.0246395067f, -0.0265026901f, 0.5764186975f, 0.0272583744f, 0.2371247105f, 0.013221886f, 1.511015625f, 0.0383150384f, -0.0245761095f, 0.2315287091f, 0.34375f, 0f, 0f, 0f, 1f}),
        COLLAR("_pakman_",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ2NTI5MywKICAicHJvZmlsZUlkIiA6ICIwNDg2YWUwMWI4Y2I0OWUzODMyZDcwOTNmMWJlNzI3NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJfcGFrbWFuXyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jYmUzZDJlY2ViNzk1ZDQxZTFjZTU5ZTZmNjdkNzM0ZGUzYWNjMjU3MTYzYWY5MzcyYjQxOGVlMGM4NWViYzg0IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.1904296875f, 0f, 0f, 0.5052224005f, 0f, 0.5741312937f, 0.0033404997f, 1.619095253f, 0f, -0.0122712233f, 0.2343393036f, 0.4711609839f, 0f, 0f, 0f, 1f}),
        HEAD("8b2ca111504dde50",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ3MDAxNywKICAicHJvZmlsZUlkIiA6ICIzOTg5OGFiODFmMjU0NmQxOGIyY2ExMTE1MDRkZGU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICI4YjJjYTExMTUwNGRkZTUwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q5YmIyNWI4ZjcyMDMyYWM1MDc2MzAxMzM2YjhjNDcxY2FmNTZiOTNhM2MyYzNmNmFhMDQzYzg2MDI5ZGM4MGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.0476074219f, 0f, 0f, 0.5050075647f, 0f, 0.3349099213f, 0.0016702498f, 1.7741992188f, 0f, -0.0071582136f, 0.1171696518f, 0.4708984375f, 0f, 0f, 0f, 1f}),
        ORNAMENT_RIGHT("8b2ca111504dde50",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ3MDAxNywKICAicHJvZmlsZUlkIiA6ICIzOTg5OGFiODFmMjU0NmQxOGIyY2ExMTE1MDRkZGU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICI4YjJjYTExMTUwNGRkZTUwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q5YmIyNWI4ZjcyMDMyYWM1MDc2MzAxMzM2YjhjNDcxY2FmNTZiOTNhM2MyYzNmNmFhMDQzYzg2MDI5ZGM4MGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0f, 0.2856010262f, 0.0016617282f, 0.5764186975f, -0.0123848957f, -0.0048400124f, 0.0924280407f, 1.7048144531f, 0.0565972164f, -0.0015880131f, 0.0303257374f, 0.4603515625f, 0f, 0f, 0f, 1f}),
        ARM_LEFT("ThadomInator478",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NTI2OTQ3MTY2NSwKICAicHJvZmlsZUlkIiA6ICIzMzU3MWJiY2UyMDE0MTRiYmNkMDYyMjEyZTI4MjBlMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGFkb21JbmF0b3I0NzgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjcyMjQ5NDc3ODNlYjNkMjA3ZTZlZjM2M2JiOTAyMmU5ZmYwMWZjNDc5ZDg4NDRjZDQ4MmIwNjc5MWNkNTczYyIKICAgIH0KICB9Cn0=",
                new float[]{0.2338185397f, 0.0370332185f, 0.0199053226f, 0.4335964318f, -0.035613916f, 0.2360094161f, -0.0132786824f, 1.4631640625f, -0.0333698349f, 0.0154059474f, 0.2325232711f, 0.34375f, 0f, 0f, 0f, 1f});

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
            Vector3f sum = new Vector3f();
            KingerPart[] parts = values();
            for (KingerPart p : parts) {
                sum.x += p.offset.x;
                sum.z += p.offset.z;
            }
            sum.x /= parts.length;
            sum.z /= parts.length;
            CENTER = sum;
        }
    }

    public static final String TAG = "MSC_Kinger";
    public static final String PART_TAG = "MSC_KingerPart";
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

    public Kinger(MultiverseCreatures plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        reloadConfig();
        reloadExisting();
        startTicker();
    }

    public void reloadConfig() {
        var config = plugin.getConfig();
        health = config.getDouble("kinger.health", 120.0);
        aggroRange = config.getDouble("kinger.aggro-range", 25.0);
        moveSpeed = config.getDouble("kinger.move-speed", 0.32);
        meleeRange = config.getDouble("kinger.melee-range", 3.0);
        meleeDamage = config.getDouble("kinger.melee-damage", 8.0);
        meleeRadius = config.getDouble("kinger.melee-radius", 3.5);
        rangedRange = config.getDouble("kinger.ranged-range", 30.0);
        rangedDamage = config.getDouble("kinger.ranged-damage", 6.0);
        meleeCooldownTicks = config.getInt("kinger.melee-cooldown-ticks", 25);
        rangedCooldownTicks = config.getInt("kinger.ranged-cooldown-ticks", 45);
        meleeAnimTicks = config.getInt("kinger.melee-anim-ticks", 12);
        rangedAnimTicks = config.getInt("kinger.ranged-anim-ticks", 20);
        armorStandChance = config.getDouble("kinger.spawn-on-armorstand-chance", 1.0);
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandPlace(EntityPlaceEvent event) {
        if (event.getEntityType() != EntityType.ARMOR_STAND) return;
        if (event.getPlayer() == null) return;
        if (!plugin.getConfig().getBoolean("kinger.enabled", true)) return;
        if (random.nextDouble() >= armorStandChance) return;
        Location loc = event.getEntity().getLocation();
        event.setCancelled(true);
        trySpawn(loc);
    }

    private void reloadExisting() {
        Set<UUID> validStands = new HashSet<>();
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {
                if (!stand.getScoreboardTags().contains(TAG)) continue;
                activeKingers.put(stand.getUniqueId(), new KingerInstance(stand));
                validStands.add(stand.getUniqueId());
            }
            for (ItemDisplay display : world.getEntitiesByClass(ItemDisplay.class)) {
                if (!display.getScoreboardTags().contains(PART_TAG)) continue;
                boolean nearStand = false;
                for (Entity e : display.getNearbyEntities(3, 3, 3)) {
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
                for (KingerInstance inst : new ArrayList<>(activeKingers.values())) {
                    tick(inst);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
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
            double dist = stand.getLocation().distance(target.getLocation());
            inst.moving = dist > 1.8;
            if (inst.moving && dist <= aggroRange) {
                moveTowards(stand, target.getLocation());
            }
            faceTarget(stand, target);
            if (dist <= meleeRange && inst.meleeCooldown <= 0) {
                meleeAttack(stand);
                inst.meleeAnim = meleeAnimTicks;
                inst.meleeCooldown = meleeCooldownTicks;
            } else if (dist > meleeRange && dist <= rangedRange && inst.rangedCooldown <= 0) {
                rangedAttack(stand, target);
                inst.rangedAnim = rangedAnimTicks;
                inst.rangedCooldown = rangedCooldownTicks;
            }
        } else {
            inst.moving = false;
        }

        snapToGround(stand);

        if (inst.moving) inst.animTicks += 0.3;
        if (inst.meleeAnim > 0) inst.meleeAnim--;
        if (inst.rangedAnim > 0) inst.rangedAnim--;
        if (inst.meleeCooldown > 0) inst.meleeCooldown--;
        if (inst.rangedCooldown > 0) inst.rangedCooldown--;

        syncDisplays(inst);

        if (inst.bossBar != null) {
            double maxHealth = stand.getAttribute(Attribute.MAX_HEALTH) != null
                    ? stand.getAttribute(Attribute.MAX_HEALTH).getValue() : health;
            inst.bossBar.setProgress(Math.max(0.0, stand.getHealth() / maxHealth));
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
        loc.add(dir.multiply(step));
        loc.setPitch(0);
        stand.teleport(loc);
    }

    private void faceTarget(ArmorStand stand, Player target) {
        Location loc = stand.getLocation();
        loc.setDirection(target.getLocation().toVector().subtract(loc.toVector()).setY(0));
        stand.teleport(loc);
    }

    private void snapToGround(ArmorStand stand) {
        Location loc = stand.getLocation();
        World world = stand.getWorld();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        int y = loc.getBlockY();
        for (int i = y; i > y - 8; i--) {
            if (world.getBlockAt(x, i, z).getType().isSolid()) {
                double groundY = i + 1.0;
                if (Math.abs(groundY - loc.getY()) > 0.001) {
                    loc.setY(groundY);
                    stand.teleport(loc);
                }
                return;
            }
        }
    }

    private void hitEffect(ArmorStand stand) {
        Location loc = stand.getLocation().clone().add(0, 1, 0);
        stand.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 8, 0.4, 0.6, 0.4, 0.1);
        stand.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.8f, 1.1f);
    }

    private void meleeAttack(ArmorStand stand) {
        World world = stand.getWorld();
        Location loc = stand.getLocation();
        world.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 0.6f);
        world.spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 40, 1.5, 1.0, 1.5, 0,
                new Particle.DustOptions(Color.fromRGB(0xAA00FF), 1.6f));
        world.spawnParticle(Particle.LARGE_SMOKE, loc.clone().add(0, 0.5, 0), 20, 1.2, 0.8, 1.2, 0.02);

        for (Entity e : world.getNearbyEntities(loc, meleeRadius, meleeRadius, meleeRadius)) {
            if (!(e instanceof Player p)) continue;
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;
            p.damage(meleeDamage, stand);
            Vector away = p.getLocation().toVector().subtract(loc.toVector());
            if (away.lengthSquared() < 0.01) away = new Vector(0, 0, -1);
            away.normalize();
            p.setVelocity(away.multiply(1.3).setY(0.45));
        }
    }

    private void rangedAttack(ArmorStand stand, Player target) {
        World world = stand.getWorld();
        Location hand = partWorldLocation(stand, KingerPart.ARM_RIGHT);
        world.playSound(hand, Sound.ENTITY_SHULKER_SHOOT, 1.0f, 1.2f);
        world.spawnParticle(Particle.DUST, hand, 12, 0.3, 0.3, 0.3, 0,
                new Particle.DustOptions(Color.fromRGB(0xBB66FF), 1.2f));
        ShulkerBullet bullet = (ShulkerBullet) world.spawnEntity(hand, EntityType.SHULKER_BULLET);
        bullet.addScoreboardTag(BULLET_TAG);
        bullet.setTarget(target);
        Vector vel = target.getLocation().toVector().subtract(hand.toVector()).normalize().multiply(1.5);
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
        for (KingerPart part : KingerPart.values()) {
            UUID id = inst.partDisplays.get(part);
            Entity e = (id != null) ? Bukkit.getEntity(id) : null;
            if (e instanceof ItemDisplay display && display.isValid()) {
                display.teleport(partWorldLocation(stand, part));
                display.setTransformation(buildTransformation(part, inst));
            } else {
                ItemDisplay display = spawnPart(stand, part);
                inst.partDisplays.put(part, display.getUniqueId());
            }
        }
    }

    private Location partWorldLocation(ArmorStand stand, KingerPart part) {
        Location base = stand.getLocation().clone();
        base.setYaw(stand.getLocation().getYaw() + 180);
        base.setPitch(0);
        double yawRad = Math.toRadians(base.getYaw());
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);
        Vector3f off = centered(part.offset);
        base.add(off.x * cos - off.z * sin, off.y, off.x * sin + off.z * cos);
        return base;
    }

    private ItemDisplay spawnPart(ArmorStand stand, KingerPart part) {
        ItemStack head = createHead(part.profileName, part.texture);
        ItemDisplay display = (ItemDisplay) stand.getWorld().spawnEntity(stand.getLocation(), EntityType.ITEM_DISPLAY);
        display.setItemStack(head);
        display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
        display.setBillboard(Display.Billboard.FIXED);
        display.setTransformation(toTransformation(part));
        display.setBrightness(new Display.Brightness(15, 15));
        display.setInvulnerable(false);
        display.setGravity(false);
        display.setSilent(true);
        display.setPersistent(true);
        display.addScoreboardTag(PART_TAG);
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
                plugin.getLogger().warning("Failed to set Kinger head texture for " + profileName + ": " + e.getMessage());
            }
            head.setItemMeta(meta);
        }
        return head;
    }

    private Transformation toTransformation(KingerPart part) {
        return new Transformation(new Vector3f(), part.rotation, part.scale, new Quaternionf());
    }

    private Transformation buildTransformation(KingerPart part, KingerInstance inst) {
        Quaternionf anim = computeAnimQuat(part, inst);
        if (anim.x == 0 && anim.y == 0 && anim.z == 0) {
            return new Transformation(new Vector3f(), part.rotation, part.scale, new Quaternionf());
        }
        Quaternionf left = new Quaternionf(anim).mul(part.rotation);
        return new Transformation(new Vector3f(), left, part.scale, new Quaternionf());
    }

    private Vector3f centered(Vector3f offset) {
        return new Vector3f(offset.x - KingerPart.CENTER.x, offset.y, offset.z - KingerPart.CENTER.z);
    }

    private Quaternionf computeAnimQuat(KingerPart part, KingerInstance inst) {
        Quaternionf q = new Quaternionf();
        float s = inst.animTicks;
        boolean walking = inst.moving;
        switch (part) {
            case LEG_RIGHT_UPPER:
            case LEG_RIGHT_LOWER:
                if (walking) q.rotateX((float) (Math.sin(s) * 0.3));
                break;
            case LEG_LEFT_UPPER:
            case LEG_LEFT_LOWER:
                if (walking) q.rotateX((float) (Math.sin(s) * -0.3));
                break;
            case ARM_RIGHT:
                if (inst.meleeAnim > 0) {
                    float prog = 1f - (float) inst.meleeAnim / meleeAnimTicks;
                    q.rotateX((float) (Math.sin(prog * Math.PI) * -3.0));
                } else if (inst.rangedAnim > 0) {
                    q.rotateX(3.0f);
                } else if (walking) {
                    q.rotateX((float) (Math.sin(s) * -0.35));
                }
                break;
            case ARM_LEFT:
                if (inst.meleeAnim > 0) {
                    float prog = 1f - (float) inst.meleeAnim / meleeAnimTicks;
                    q.rotateX((float) (Math.sin(prog * Math.PI) * 3.0));
                } else if (inst.rangedAnim > 0) {
                    q.rotateX(3.0f);
                } else if (walking) {
                    q.rotateX((float) (Math.sin(s) * 0.35));
                }
                break;
            case TORSO_UPPER:
            case TORSO_LOWER:
            case NECK:
            case BELT:
            case COLLAR:
                if (inst.meleeAnim > 0) {
                    float prog = 1f - (float) inst.meleeAnim / meleeAnimTicks;
                    q.rotateX((float) (Math.sin(prog * Math.PI) * -0.5));
                } else if (inst.rangedAnim > 0) {
                    q.rotateX(0.4f);
                } else if (walking) {
                    q.rotateX((float) (Math.sin(s) * 0.05));
                }
                break;
            case HEAD:
                Player target = inst.targetId != null ? Bukkit.getPlayer(inst.targetId) : null;
                if (inst.meleeAnim > 0) {
                    q.rotateX((float) Math.toRadians(-15));
                } else if (inst.rangedAnim > 0) {
                    q.rotateX((float) Math.toRadians(-55));
                } else if (target != null && target.isOnline()) {
                    Vector to = target.getEyeLocation().toVector().subtract(inst.stand.getLocation().toVector());
                    double horiz = Math.sqrt(to.getX() * to.getX() + to.getZ() * to.getZ());
                    if (horiz > 0.5) {
                        float pitch = (float) Math.toDegrees(Math.atan2(-to.getY(), horiz));
                        pitch = Math.max(-35, Math.min(35, pitch));
                        q.rotateX((float) Math.toRadians(pitch));
                    }
                }
                break;
        }
        return q;
    }

    public boolean trySpawn(Location location) {
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
        stand.setRemoveWhenFarAway(false);
        stand.setPersistent(true);
        stand.setMaximumNoDamageTicks(0);
        stand.addScoreboardTag(TAG);

        AttributeInstance maxHealthAttr = stand.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) maxHealthAttr.setBaseValue(health);
        stand.setHealth(health);

        AttributeInstance scaleAttr = stand.getAttribute(Attribute.SCALE);
        if (scaleAttr != null) scaleAttr.setBaseValue(2.0);

        KingerInstance inst = new KingerInstance(stand);
        activeKingers.put(stand.getUniqueId(), inst);
        setupBossBar(inst);

        for (KingerPart part : KingerPart.values()) {
            ItemDisplay display = spawnPart(stand, part);
            inst.partDisplays.put(part, display.getUniqueId());
        }

        location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
        return true;
    }

    private void cleanup(KingerInstance inst) {
        for (UUID id : inst.partDisplays.values()) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) e.remove();
        }
        inst.partDisplays.clear();
        if (inst.bossBar != null) {
            inst.bossBar.removeAll();
            inst.bossBar = null;
        }
    }

    private void setupBossBar(KingerInstance inst) {
        BossBar bar = Bukkit.createBossBar(BAR_TITLE, BarColor.PURPLE, BarStyle.SEGMENTED_10, BarFlag.DARKEN_SKY);
        bar.setProgress(1.0);
        bar.setVisible(true);
        for (Player p : inst.stand.getWorld().getPlayers()) {
            bar.addPlayer(p);
        }
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
        if (damager instanceof ShulkerBullet bullet && bullet.getScoreboardTags().contains(BULLET_TAG)) {
            if (damaged instanceof Player p) {
                event.setDamage(rangedDamage);
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, false));
            }
        }

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
            event.setCancelled(true);
        }
    }

    private void reduceHealth(ArmorStand stand, double damage) {
        stand.setNoDamageTicks(0);
        double newHealth = Math.max(0, stand.getHealth() - damage);
        stand.setHealth(newHealth);

        KingerInstance inst = activeKingers.get(stand.getUniqueId());
        if (inst != null && inst.bossBar != null) {
            double maxHealth = stand.getAttribute(Attribute.MAX_HEALTH) != null
                    ? stand.getAttribute(Attribute.MAX_HEALTH).getValue() : health;
            inst.bossBar.setProgress(Math.max(0.0, newHealth / maxHealth));
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
        List<String> messages = plugin.getConfig().getStringList("kinger.death-messages");
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
        public boolean moving;
        public float animTicks;

        public KingerInstance(ArmorStand stand) {
            this.stand = stand;
        }
    }
}
