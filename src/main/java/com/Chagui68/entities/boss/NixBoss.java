package com.Chagui68.entities.boss;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.entities.boss.fx.Fx;
import com.Chagui68.entities.boss.fx.LiveStage;
import com.Chagui68.entities.boss.fx.Palette;
import com.Chagui68.entities.boss.fx.Sfx;
import com.Chagui68.entities.boss.fx.Shapes;
import com.Chagui68.utils.DisplaySuit;
import com.Chagui68.utils.MscBossBar;
import com.Chagui68.utils.MscEntityUtils;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * NIX - The Executioner
 * Custom boss constructed from 27 ItemDisplay player head parts with hierarchical
 * joint-pivot procedural animations, brutal execution cleaves, chain pulls, and adaptive AI.
 */
public class NixBoss implements Listener {

    public enum LimbGroup {
        HEAD,
        TORSO_UPPER,
        TORSO_LOWER,
        LEG_RIGHT,
        LEG_LEFT,
        ARM_RIGHT,
        ARM_LEFT
    }

    public enum NixPart {
        HEAD("Gultro",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjIzMzcwMywKICAicHJvZmlsZUlkIiA6ICI0OThjYTc2ZGYwODM0NzhmOGY0NjdjOGY1OTQwMjk1MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJHdWx0cm8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjkyYTQ0NjMwNjkwOTk2YTNmZTk4MjkwNDUyMjFlNDRlYWU1NjBhOTljMzJjMjc2ZGI2NzBmZmZiNGIzN2I2ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.937f, 0f, 0f, 0.0663665625f, 0f, 0.937f, 0f, 1.8735078125f, 0f, 0f, 0.937f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.HEAD),

        TORSO_UPPER("NiteCave",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjIzNTUxMywKICAicHJvZmlsZUlkIiA6ICI4OGMyOGYwOTY3MjU0NmNkYTg3ZjVhMTc0ODU0MzExYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJOaXRlQ2F2ZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81ZWJkNTVhYWI1ODYzZmJmYjMzYWQ4MmJhZTRlZTk2MzI3ZjY2YzA4YWE5NWY2OWExZjIxYjRmMmRjZWNjNWEwIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.937f, 0f, 0f, 0.0663665625f, 0f, 0.4685f, 0f, 1.4050078125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.TORSO_UPPER),

        TORSO_LOWER("CyberMinny",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjIzODc2NSwKICAicHJvZmlsZUlkIiA6ICI4ZjllYTBhNWJhOGE0NTNkYTgzNTBmYjRmNzVmOTJiOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJDeWJlck1pbm55IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk0ZjVhYzlhNzIwZTIyMTFjMzc1Mjk1ODY4Njc4Y2I3ZGIwNjZhYmNlNzE1NTA0NTMzNWEwZTRhMWY0ZDU4NWUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.937f, 0f, 0f, 0.0663665625f, 0f, 0.937f, 0f, 1.1707578125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.TORSO_LOWER),

        // Leg Right (6 parts)
        LEG_R_1("CeilingBird",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI0MjA4NiwKICAicHJvZmlsZUlkIiA6ICJjNWE4ZTZmZGIyYmI0NmJiYjE3NjVhYjE2NmMwNTZlOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDZWlsaW5nQmlyZCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zZTE5MDJhNzg4MjMzMWY3ZDM1MWM5NzNkZGIzZTBkMzQzMjE2MTRiZWQxYjY4ZjgzZmRjZTFlYTA5YjQyNWQxIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.466626f, 0f, 0f, -0.0490015625f, 0f, 0.1780296178f, -0.2898695861f, 0.3649378125f, 0f, 0.1780296178f, 0.2898695861f, 0.026601875f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_RIGHT),

        LEG_R_2("AirplaneGoBrr",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI0NTUxNCwKICAicHJvZmlsZUlkIiA6ICI3YjA5ZDg5NWQyYjc0NTU3YmM0YTkzNWYyNjU0NWNjNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBaXJwbGFuZUdvQnJyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzlhZDZkOTkxZDYwY2RiNzYwZmI0ZDg1YWZhN2E2Y2VmNjNiZjcxNTk5Yzg3OTVlZWQ3NDJhMzdiZjZiZjIxNDciLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.4685f, 0f, 0f, -0.0519296875f, 0f, 0.23425f, 0f, 0.3508828125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_RIGHT),

        LEG_R_3("MrScarySpaceCat",
                "ewogICJ0aW1lc3RhbXAiIDogMTc0ODE3ODM1MjcxNywKICAicHJvZmlsZUlkIiA6ICI0ZWE3NGM1ZGUyZGI0OGY2YjViOTk1YTVhNTYzMmU0NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNclNjYXJ5U3BhY2VDYXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTEzNmQwNzRmOTFkMTk1NTNhODVkOWVlNDRhYzAwY2YzYTA5OWNlY2NmODA4NTI5MjNjNWQ3ZjQwYjlmMmQyNiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.4685f, 0f, 0f, -0.0519296875f, 0f, 0.4685f, 0f, 0.2337578125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_RIGHT),

        LEG_R_4("XenokratesRitva",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI0ODIwNSwKICAicHJvZmlsZUlkIiA6ICJlNzM4MTYzZTYwM2M0MTFkOTg4MzNiYzkyZTI4Y2IyYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJYZW5va3JhdGVzUml0dmEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmUyNTRlZTI3YzAzOTNhYTg4ZTcwNmJkMWU3ZTkyYzk2YzRmMWM4YmVkOWM5MzY3YjdkYjc0NWZmYTY2N2U0MSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.4685f, 0f, 0f, -0.0507584375f, 0f, 0.4685f, 0f, 0.7022578125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_RIGHT),

        LEG_R_5("MLK15",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI1MDU5OSwKICAicHJvZmlsZUlkIiA6ICJkM2VmYjJmMThjMDU0YmRhYTE0OTVlZTY0ZDgxZjdlOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNTEsxNSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mNTZjOTBjYzBmMzkyNTVhN2M2Y2QxYjRkMmMyMWNiMDU5NjA4ZmZlMjRmZDM4NzI0MDFlNTIxNmMzMzFjYjdkIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.4685f, 0f, 0f, -0.0507584375f, 0f, 0.23425f, 0f, 0.4680078125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_RIGHT),

        LEG_R_6("MolesLLC",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI1MjQ2MCwKICAicHJvZmlsZUlkIiA6ICJhOGMzZGQ3OGVmZmI0NmYyYjlhNjcyOTE4OWU4OTI0ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNb2xlc0xMQyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yYjBhMWM3YTYzYzI3MDk3NDcxYWY4M2ZiZDEyMGQ2NGVlNzQ1ZWFhYjA3ZWI0MTZhOWI4YTRiY2RhMDc0MTJkIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.466626f, 0f, 0f, -0.0507584375f, 0f, 0.1739217517f, 0.294838779f, 0.4223290625f, 0f, -0.1739217517f, 0.294838779f, -0.061241875f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_RIGHT),

        // Leg Left (6 parts)
        LEG_L_1("HarolotWarlot",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI1ODk2NiwKICAicHJvZmlsZUlkIiA6ICJkM2Y4NTQ5YmUwZGY0NGUyOTlmZjkwMDQwZWZjNTQyMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJIYXJvbG90V2FybG90IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M4ZjBmMTUxYmM4N2M5OTY2NjQ1ZjU5MTY2NjM4NjdiYmI5MTQ5NTU0MTBhMjExNTY4YzIwM2UzMzVhNWJlOWQiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.466626f, 0f, 0f, 0.1817346875f, 0f, 0.1780296178f, -0.2898695861f, 0.3649378125f, 0f, 0.1780296178f, 0.2898695861f, 0.026601875f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_LEFT),

        LEG_L_2("Stoic_Sigma",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI2MTA5MiwKICAicHJvZmlsZUlkIiA6ICI4ZTYwMzY1MWQyZTQ0MmVhYjk1OGM4YThjYmFiYThmNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9pY19TaWdtYSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84ODFkYjIwZWMzYmE5Nzk0YTU0ZDg3OTk3MGFjYzJlNDRhYzJjMTVhMzkzODhjZDdiODAwNGJlZDEwNTlmZWUwIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.4685f, 0f, 0f, 0.1846628125f, 0f, 0.23425f, 0f, 0.3508828125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_LEFT),

        LEG_L_3("nabe4975",
                "ewogICJ0aW1lc3RhbXAiIDogMTc0ODE3ODM2MzI4MSwKICAicHJvZmlsZUlkIiA6ICIxN2I5ZDBmOWYxYzE0OTE5ODRkY2Y5ZGM2YzczZDYzYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJuYWJlNDk3NSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mZjc3YjEzNTQ0OTgyMGNmMDYwYmIzNGRmMjcxMzQ3M2ZiMzBkOTJmZjhmN2ZhMDQ3OGRhOTkxYzk0NzEzZmQ2IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.4685f, 0f, 0f, 0.1846628125f, 0f, 0.4685f, 0f, 0.2337578125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_LEFT),

        LEG_L_4("Praxibetl",
                "ewogICJ0aW1lc3RhbXAiIDogMTc0NDM4NjI0MTU1MCwKICAicHJvZmlsZUlkIiA6ICJkNDAwODgyZmY3OGQ0ZGVhYjliMGNlMTc2YmQ1ZTQyMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJQcmF4aWJldGwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmU2Mzk3ZDU3NWIzNjIyMTViOWRkZWJhYjM3NTg5ZjU2MmZhMWJhOTZlZTg3NDQ3MmQ4YWUwYTcxZGVkNDYxNiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.4685f, 0f, 0f, 0.1834915625f, 0f, 0.4685f, 0f, 0.7022578125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_LEFT),

        LEG_L_5("Omegaplex",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI2NTI2MiwKICAicHJvZmlsZUlkIiA6ICJhOWIyMmRjZTI0YmM0NmZkOTg5ZDYzNmI3YzkzZDFmOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJPbWVnYXBsZXgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmRiNTgzZjQ1NjM2ODgyODNkYjk1MTU1YzUzYjE0ZGRjZTg4OTdiYWRmNWIwNGZiMWJhMzFkYTY3NjFiN2Y0ZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.4685f, 0f, 0f, 0.1834915625f, 0f, 0.23425f, 0f, 0.4680078125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_LEFT),

        LEG_L_6("DelusionSketch",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI2ODE5OCwKICAicHJvZmlsZUlkIiA6ICIyM2RjZjc3NWQ5YzQ0MGE1ODc3MjE4ZjU3NzNlMTUzNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJEZWx1c2lvblNrZXRjaCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84NjM2YzA3NDc4NjM1Nzc5M2I3NjgwODNkOTQ0NmFkYzQ5NTNlMGJmZjZhMDg2MmM4Mjg3Mzk2YTMxNWUwNTA2IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.466626f, 0f, 0f, 0.1834915625f, 0f, 0.1739217517f, 0.294838779f, 0.4223290625f, 0f, -0.1739217517f, 0.294838779f, -0.061241875f, 0f, 0f, 0f, 1f},
                LimbGroup.LEG_LEFT),

        // Arm Right (6 parts)
        ARM_R_1("Th3m1s",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI3MDM1NSwKICAicHJvZmlsZUlkIiA6ICI2NDU4Mjc0MjEyNDg0MDY0YTRkMDBlNDdjZWM4ZjcyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaDNtMXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTQyNWE4YmY5Yjg0MTdkMDY5ZjdjNzdmYzJkNzhjNTUyZDVlM2YxZGIyZTE5NGU1NWZiMmQzMTk0MmViODYxNSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.466626f, 0f, 0f, 0.4159846875f, 0f, 0.1780296178f, 0.2898695861f, 1.0676878125f, 0f, -0.1780296178f, 0.2898695861f, -0.060070625f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_RIGHT),

        ARM_R_2("Th3m1s",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI3MDM1NSwKICAicHJvZmlsZUlkIiA6ICI2NDU4Mjc0MjEyNDg0MDY0YTRkMDBlNDdjZWM4ZjcyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaDNtMXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTQyNWE4YmY5Yjg0MTdkMDY5ZjdjNzdmYzJkNzhjNTUyZDVlM2YxZGIyZTE5NGU1NWZiMmQzMTk0MmViODYxNSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.4685f, 0f, 0f, 0.4189128125f, 0f, 0.23425f, 0f, 1.0536328125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_RIGHT),

        ARM_R_3("AlexisMadd",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI3MzM2NywKICAicHJvZmlsZUlkIiA6ICJhZmEwYmMzZTA1MjQ0MTM4YjkxMDIxY2Y3ODE2YjRkZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbGV4aXNNYWRkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk5MWVkY2U2MTc4NzliMDRiM2NiMThkYWUzMWNhNjVlYjNmNGVlMDkxMWViYmJkM2ViOTYxZDA5ODQ1YTMzNzYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.4685f, 0f, 0f, 0.4189128125f, 0f, 0.4685f, 0f, 0.9365078125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_RIGHT),

        ARM_R_4("meoweddd",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI3ODI3NiwKICAicHJvZmlsZUlkIiA6ICI5NmYyMmIwNmI2YWM0OGNkYTE5OWEzMjFkZDY4NTFmNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJtZW93ZWRkZCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hZmE1NzM3YzI0NmQwNGVjNDhjODk1MTUwMmJlN2U5M2Q5YTJhYmUyMmE3NDc5M2M4OTZiYTZiZTJmNjg3MTczIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.4685f, 0f, 0f, 0.4177415625f, 0f, 0.4685f, 0f, 1.4050078125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_RIGHT),

        ARM_R_5("PatatjeMC",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI4MDk4MywKICAicHJvZmlsZUlkIiA6ICIxMjE4YWNiNDJiYzA0MzY4YjIxOTU4ZTZiYWU2NDMyMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJQYXRhdGplTUMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjQ3YzNiZmM5OTg5ZTlmYWZmOTY0MTQwYmMxOGI4MGI4MzMyZDZlYWIwNWI1MDcwY2Q1NTU4MmM1OTY1ZGU2NiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.4685f, 0f, 0f, 0.4177415625f, 0f, 0.23425f, 0f, 1.1707578125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_RIGHT),

        ARM_R_6("PatatjeMC",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI4MDk4MywKICAicHJvZmlsZUlkIiA6ICIxMjE4YWNiNDJiYzA0MzY4YjIxOTU4ZTZiYWU2NDMyMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJQYXRhdGplTUMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjQ3YzNiZmM5OTg5ZTlmYWZmOTY0MTQwYmMxOGI4MGI4MzMyZDZlYWIwNWI1MDcwY2Q1NTU4MmM1OTY1ZGU2NiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.466626f, 0f, 0f, 0.4177415625f, 0f, 0.1739217517f, -0.294838779f, 1.1250790625f, 0f, 0.1739217517f, 0.294838779f, 0.027773125f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_RIGHT),

        // Arm Left (6 parts)
        ARM_L_1("spifftopia1",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI4NzU5NiwKICAicHJvZmlsZUlkIiA6ICI4MTFkNjM0NzUwY2Y0ZDI0OTJmZDcxYTViZjZhZjI3MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcGlmZnRvcGlhMSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zNzcxMDIxNmViZTVkY2ZhZTY2NTk1MDI0MjJiYTAyYWY0OTk3ZmY4YzVjZTIwNTZlYmQwN2IxYzMxZGJiNGI2IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.466626f, 0f, 0f, -0.2832515625f, 0f, 0.1780296178f, 0.2898695861f, 1.0676878125f, 0f, -0.1780296178f, 0.2898695861f, -0.060070625f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_LEFT),

        ARM_L_2("spifftopia1",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI4NzU5NiwKICAicHJvZmlsZUlkIiA6ICI4MTFkNjM0NzUwY2Y0ZDI0OTJmZDcxYTViZjZhZjI3MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcGlmZnRvcGlhMSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zNzcxMDIxNmViZTVkY2ZhZTY2NTk1MDI0MjJiYTAyYWY0OTk3ZmY4YzVjZTIwNTZlYmQwN2IxYzMxZGJiNGI2IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                new float[]{0.4685f, 0f, 0f, -0.2861796875f, 0f, 0.23425f, 0f, 1.0536328125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_LEFT),

        ARM_L_3("SmugFoodie",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjI5NDI4MiwKICAicHJvZmlsZUlkIiA6ICIwMzBlMDA1OWQwY2M0YTZhODY3N2RkZWU3MjEzMjg1MyIsCiAgInByb2ZpbGVOYW1lIiA6ICJTbXVnRm9vZGllIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzFmZWRlY2E0MzVjMzk4M2Q4Y2U3ZGQ0OTE2OWFmMGVhYWNmNmMxY2I2MTIwNjViZDIwZmMwODA2ZjFhYzk2YTMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.4685f, 0f, 0f, -0.2861796875f, 0f, 0.4685f, 0f, 0.9365078125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_LEFT),

        ARM_L_4("GR1ZZLY180",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjMwMTc4NiwKICAicHJvZmlsZUlkIiA6ICJmYWU5NzYzY2FmMDU0OWI2YjlmZTM0MmNjM2E3YzNlMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJHUjFaWkxZMTgwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NlYjUxMzViNTAwMTQ3MmMxZGJkYWYxZTVmM2ZiZDljYjk3ODQ2ZWM5ZjQ0OWYxZmQxODNmMzVlN2M4ZDY5OTUiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==",
                new float[]{0.4685f, 0f, 0f, -0.2850084375f, 0f, 0.4685f, 0f, 1.4050078125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_LEFT),

        ARM_L_5("Lord_of_xiaoyao",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjMwNzU1MSwKICAicHJvZmlsZUlkIiA6ICI5YWRmNmRlNTJkNjE0MzA2YmEzNzQ2NWU1ZDIyYThjMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJMb3JkX29mX3hpYW95YW8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQyODNmOWU4OTc4MWM5MzljYjcwN2FlNGFlZDVlYTZhNjdhZjhlMTBlYWE2YWM0ZjU2MDE1MGZiMzg2MmIzYSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.4685f, 0f, 0f, -0.2850084375f, 0f, 0.23425f, 0f, 1.1707578125f, 0f, 0f, 0.4685f, -0.016734375f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_LEFT),

        ARM_L_6("Lord_of_xiaoyao",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4OTMzNjMwNzU1MSwKICAicHJvZmlsZUlkIiA6ICI5YWRmNmRlNTJkNjE0MzA2YmEzNzQ2NWU1ZDIyYThjMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJMb3JkX29mX3hpYW95YW8iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQyODNmOWU4OTc4MWM5MzljYjcwN2FlNGFlZDVlYTZhNjdhZjhlMTBlYWE2YWM0ZjU2MDE1MGZiMzg2MmIzYSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
                new float[]{0.466626f, 0f, 0f, -0.2850084375f, 0f, 0.1739217517f, -0.294838779f, 1.1250790625f, 0f, 0.1739217517f, 0.294838779f, 0.027773125f, 0f, 0f, 0f, 1f},
                LimbGroup.ARM_LEFT);

        public final String profileName;
        public final String texture;
        public final float[] matrix;
        public final LimbGroup group;
        public final Vector3f offset;
        public final Quaternionf rotation;
        public final Vector3f scale;

        NixPart(String profileName, String texture, float[] matrix, LimbGroup group) {
            this.profileName = profileName;
            this.texture = texture;
            this.matrix = matrix;
            this.group = group;

            Matrix4f m = new Matrix4f().set(
                    matrix[0], matrix[4], matrix[8], matrix[12],
                    matrix[1], matrix[5], matrix[9], matrix[13],
                    matrix[2], matrix[6], matrix[10], matrix[14],
                    matrix[3], matrix[7], matrix[11], matrix[15]);
            this.offset = new Vector3f();
            this.scale = new Vector3f();
            this.rotation = new Quaternionf();
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
            float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
            float minZ = Float.POSITIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;

            for (NixPart p : values()) {
                minX = Math.min(minX, p.offset.x);
                maxX = Math.max(maxX, p.offset.x);
                minY = Math.min(minY, p.offset.y);
                maxY = Math.max(maxY, p.offset.y);
                minZ = Math.min(minZ, p.offset.z);
                maxZ = Math.max(maxZ, p.offset.z);
            }
            CENTER = new Vector3f((minX + maxX) * 0.5f, (minY + maxY) * 0.5f, (minZ + maxZ) * 0.5f);
        }
    }

    public static final String TAG = "MSC_NixBoss";
    public static final String PART_TAG = "MSC_NixPart";
    public static final String BAR_TITLE = ChatColor.DARK_RED + "" + ChatColor.BOLD + "NIX - The Executioner";
    /** Default ceiling on a single hit taken by Nix. */
    public static final double DEFAULT_MAX_DAMAGE_PER_HIT = 100.0;

    /** Tags make display ownership survive a plugin reload without duplicating the model. */
    static final String PART_OWNER_TAG_PREFIX = "MSC_NixOwner_";

    /**
     * Scale of the invisible armour stand that carries the hitbox.
     *
     * <p>The model reaches ±0.47 blocks around the spine and 2.11 blocks up, so a vanilla
     * 1.975-block stand would leave the head and the swinging arms outside the box, where a swing
     * aimed at them hits nothing. 1.9 is the smallest scale whose uniform box (0.95 wide, 3.75
     * tall) covers the whole rest pose; the old literal 2.0 did too, but kept 1.8 blocks of empty
     * box above the head.
     */
    public static final double MODEL_HITBOX_SCALE = 1.9;

    /** Height of his eyes above his feet, for the head to look at a player from. */
    static final double EYE_HEIGHT = 1.9;
    /** The executioner's axe is one more piece of the suit, with its own tag. */
    static final String AXE_TAG = PART_TAG + "_AXE";

    /** Joints, rest pose and limb maths live in {@link NixModel}, testable without a server. */

    private final MultiverseCreatures plugin;
    private final Random random = new Random();
    private final Map<UUID, NixInstance> activeInstances = new java.util.HashMap<>();

    private double health;
    private double aggroRange;
    private double moveSpeed;
    private double meleeRange;
    private double meleeDamage;
    private double cleaveDamage;
    private double chainRange;
    /** Ceiling on a single hit, so no burst source can one-shot the boss. */
    private double maxDamagePerHit;
    /** Size of the invisible stand the suit is hit through; {@link #MODEL_HITBOX_SCALE} is the default. */
    private double hitboxScale = MODEL_HITBOX_SCALE;
    /**
     * Source and intended amount of the hit currently being applied, read back by the damage
     * listener so {@code /msc debug} can name the attack. Set and cleared by {@link #dealToPlayer}
     * around the synchronous damage call.
     */
    private String outgoingSource;
    private double outgoingIntended;
    private int meleeCooldownTicks;
    private int chainCooldownTicks;
    private int cleaveAnimTicks = 16;
    private int chainAnimTicks = 12;
    private double harvestDamage;
    private double gallowsDamage;
    private double condemnDamage;
    /** Ticks between two signature moves (Blood Harvest, Gallows Leap, Condemnation). */
    private int specialCooldownTicks;

    public NixBoss(MultiverseCreatures plugin) {
        this.plugin = plugin;
        reloadConfig();
        if (!plugin.isEnabled("entities.nix-executioner")) return;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        reloadExisting();
        startTicker();
    }

    public void reloadConfig() {
        var config = plugin.getConfig();
        health = config.getDouble("entities.nix-executioner.health", 450.0);
        aggroRange = config.getDouble("entities.nix-executioner.aggro-range", 28.0);
        moveSpeed = config.getDouble("entities.nix-executioner.move-speed", 0.30);
        meleeRange = config.getDouble("entities.nix-executioner.melee-range", 3.5);
        meleeDamage = config.getDouble("entities.nix-executioner.melee-damage", 14.0);
        cleaveDamage = config.getDouble("entities.nix-executioner.cleave-damage", 22.0);
        chainRange = config.getDouble("entities.nix-executioner.chain-range", 24.0);
        maxDamagePerHit = config.getDouble("entities.nix-executioner.max-damage-per-hit", DEFAULT_MAX_DAMAGE_PER_HIT);
        hitboxScale = MscEntityUtils.clampHitboxScale(
                config.getDouble("entities.nix-executioner.hitbox-scale", MODEL_HITBOX_SCALE));
        meleeCooldownTicks = config.getInt("entities.nix-executioner.melee-cooldown-ticks", 24);
        chainCooldownTicks = config.getInt("entities.nix-executioner.chain-cooldown-ticks", 80);
        cleaveAnimTicks = config.getInt("entities.nix-executioner.cleave-anim-ticks", 16);
        harvestDamage = config.getDouble("entities.nix-executioner.harvest-damage", 9.0);
        gallowsDamage = config.getDouble("entities.nix-executioner.gallows-damage", 18.0);
        condemnDamage = config.getDouble("entities.nix-executioner.condemn-damage", 20.0);
        specialCooldownTicks = Math.max(20, config.getInt("entities.nix-executioner.special-cooldown-ticks", 160));
    }

    /** Takes over a stand a previous run left behind, wearing the parts it already has. */
    private void adopt(ArmorStand stand) {
        if (activeInstances.containsKey(stand.getUniqueId())) return;
        if (!stand.getPersistentDataContainer().has(MscEntityUtils.KEY_VIRTUAL_MAX_HEALTH, org.bukkit.persistence.PersistentDataType.DOUBLE)) {
            MscEntityUtils.initVirtualHealth(stand, health);
        }
        NixInstance inst = new NixInstance(stand);
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
        for (NixInstance inst : activeInstances.values()) {
            java.util.List<UUID> pieces = new ArrayList<>(inst.partDisplays.values());
            if (inst.axeDisplay != null) pieces.add(inst.axeDisplay);
            worn.put(inst.stand.getUniqueId(), pieces);
        }
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
                // Removing only those untagged parts avoids a second overlapping body.
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
     * Reattaches the parts a previous run already spawned for this stand, so enabling the plugin
     * over a live Nix continues his body instead of building a second one on top.
     */
    private void restorePartDisplays(NixInstance inst) {
        String ownerTag = partOwnerTag(inst.stand.getUniqueId());
        for (ItemDisplay display : inst.stand.getWorld().getEntitiesByClass(ItemDisplay.class)) {
            if (!display.getScoreboardTags().contains(PART_TAG) || !display.getScoreboardTags().contains(ownerTag)) continue;
            if (display.getScoreboardTags().contains(AXE_TAG)) {
                inst.axeDisplay = display.getUniqueId();
                continue;
            }
            for (NixPart part : NixPart.values()) {
                if (display.getScoreboardTags().contains(partTag(part))) {
                    inst.partDisplays.put(part, display.getUniqueId());
                    break;
                }
            }
        }
    }

    /** Every part of a boss carries its own tag, so an adoption can tell the parts apart. */
    static String partTag(NixPart part) {
        return PART_TAG + "_" + part.name();
    }

    static String partOwnerTag(UUID ownerId) {
        return PART_OWNER_TAG_PREFIX + ownerId.toString().replace("-", "");
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
                for (NixInstance inst : new ArrayList<>(activeInstances.values())) {
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

    private void tick(NixInstance inst) {
        ArmorStand stand = inst.stand;
        if (stand.isDead() || !stand.isValid()) {
            cleanup(inst);
            activeInstances.remove(stand.getUniqueId());
            return;
        }
        if (!stand.getWorld().isChunkLoaded(stand.getLocation().getChunk())) return;

        Player target = findTarget(stand);
        inst.targetId = (target != null) ? target.getUniqueId() : null;

        Location loc = stand.getLocation();

        if (target != null) {
            Vector toTarget = target.getLocation().toVector().subtract(loc.toVector());
            toTarget.setY(0);
            double dist = toTarget.length();
            inst.moving = dist > 2.0;

            // Check if target is executing threshold (< 25% health)
            double targetHpPercent = target.getHealth() / (target.getAttribute(Attribute.MAX_HEALTH) != null
                    ? target.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0);
            inst.bloodlust = targetHpPercent <= 0.25;

            double currentSpeed = inst.bloodlust ? (moveSpeed * 1.35) : moveSpeed;

            // A signature move between swings; while one plays it owns the body, the heading and the feet.
            if (inst.move == NixMoves.Move.NONE && inst.moveCooldown <= 0 && inst.cleaveAnim <= 0
                    && inst.chainAnim <= 0 && dist <= aggroRange) {
                startMove(inst, pickMove(dist));
            }
            boolean performing = inst.move != NixMoves.Move.NONE;
            if (performing) inst.moving = false;

            // Smooth face toward target
            if (dist > 0.05 && !performing) {
                loc.setDirection(toTarget);
            }

            // Move toward target
            if (!performing && inst.moving && dist <= aggroRange && inst.cleaveAnim <= 4) {
                Vector dir = toTarget.clone().normalize();
                double step = Math.min(currentSpeed, dist);
                BossArena.walk(loc, dir.multiply(step), true);
            }

            // Combat triggers
            if (!performing && dist <= meleeRange && inst.meleeCooldown <= 0 && inst.cleaveAnim <= 0) {
                // Initiate Cleave windup
                inst.cleaveAnim = cleaveAnimTicks;
                inst.meleeCooldown = meleeCooldownTicks;
                stand.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.3f, 0.6f);
            } else if (!performing && dist > 5.0 && dist <= chainRange && inst.chainCooldown <= 0 && inst.cleaveAnim <= 0) {
                inst.chainAnim = chainAnimTicks;
                inst.chainCooldown = chainCooldownTicks;
                castExecutionChains(stand, target);
            }
        } else {
            inst.moving = false;
            inst.bloodlust = false;
        }

        // Single ground settle & teleport for the anchor stand (not mid-leap)
        boolean airborne = inst.move != NixMoves.Move.NONE && runMove(inst, target, loc);
        if (!airborne) BossArena.settle(loc);
        stand.teleport(loc);

        // Impact moment of the cleave: when the axe comes down in front of him, at 60% of the swing.
        // A fixed tick 9 landed before the chop had even started with the configured 14-tick swing.
        if (inst.cleaveAnim == NixModel.cleaveImpactTick(cleaveAnimTicks) && target != null) {
            executeGuillotineCleaveImpact(stand, target);
        }

        // Timers & stride progression
        float speedMultiplier = inst.bloodlust ? 0.40f : NixModel.WALK_RATE;
        if (inst.moving) inst.animTicks += speedMultiplier;
        if (inst.cleaveAnim > 0) inst.cleaveAnim--;
        if (inst.chainAnim > 0) inst.chainAnim--;
        if (inst.meleeCooldown > 0) inst.meleeCooldown--;
        if (inst.chainCooldown > 0) inst.chainCooldown--;
        if (inst.moveCooldown > 0 && inst.move == NixMoves.Move.NONE) inst.moveCooldown--;

        // Bloodlust eye particle aura
        if (inst.bloodlust) {
            Location eye = stand.getEyeLocation().clone().add(stand.getLocation().getDirection().multiply(0.2));
            stand.getWorld().spawnParticle(Particle.DUST, eye, 2, 0.2, 0.15, 0.2, 0,
                    new Particle.DustOptions(Color.fromRGB(0xAA0000), 1.2f));
        }

        inst.tickCount++;

        // Synchronize all 27 display entities locked to stand location (throttled when stationary)
        if (DisplaySuit.shouldSync(inst.moving || inst.cleaveAnim != 0 || inst.chainAnim != 0
                || inst.move != NixMoves.Move.NONE, inst.tickCount)) {
            syncDisplays(inst);
        }

        // Update BossBar progress
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

    private void executeGuillotineCleaveImpact(ArmorStand stand, Player primaryTarget) {
        World world = stand.getWorld();
        Location front = stand.getLocation().clone().add(stand.getLocation().getDirection().multiply(1.8));
        front.add(0, 0.8, 0);

        // Heavy impact sound and blood explosion
        world.playSound(front, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.8f, 0.5f);
        world.playSound(front, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.2f, 0.6f);
        world.playSound(front, Sound.ITEM_MACE_SMASH_GROUND, 1.2f, 0.8f);
        // The arc the axe just cut, from over his head down to the floor in front of him.
        Fx fx = LiveStage.fxIn(world);
        Vector forward = stand.getLocation().getDirection().setY(0).normalize();
        Vector shoulder = stand.getLocation().toVector().add(new Vector(0, 1.4, 0));
        fx.draw(Shapes.arc(shoulder, 1.9, -0.7, 1.5, 26, forward, new Vector(0, 1, 0)), fx.fade(Palette.BLOOD, Palette.ASH, 1.6f));
        fx.draw(Shapes.arc(shoulder, 1.5, -0.5, 1.2, 18, forward, new Vector(0, 1, 0)), fx.dust(Palette.BLOOD, 1.1f));
        world.spawnParticle(Particle.SWEEP_ATTACK, front, 3, 0.5, 0.3, 0.5, 0);
        world.spawnParticle(Particle.DUST, front, 45, 1.2, 0.8, 1.2, 0,
                new Particle.DustOptions(Color.fromRGB(0x880000), 2.2f));
        world.spawnParticle(Particle.BLOCK, front, 30, 0.8, 0.8, 0.8, 0.1, Material.REDSTONE_BLOCK.createBlockData());

        for (Entity e : world.getNearbyEntities(front, 3.2, 2.5, 3.2)) {
            if (!(e instanceof Player p)) continue;
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) continue;

            dealToPlayer(stand, p, cleaveDamage, "Guillotine Cleave");
            p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1, false, true));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, true));

            Vector knock = p.getLocation().toVector().subtract(stand.getLocation().toVector());
            if (knock.lengthSquared() < 0.01) knock = new Vector(0, 0, -1);
            knock.normalize();
            p.setVelocity(knock.multiply(0.85).setY(0.3));
        }
    }

    private void castExecutionChains(ArmorStand stand, Player target) {
        World world = stand.getWorld();
        Location hand = stand.getLocation().clone().add(0, 1.4, 0);
        Location targetLoc = target.getEyeLocation();

        world.playSound(hand, Sound.BLOCK_CHAIN_PLACE, 1.4f, 0.8f);
        world.playSound(hand, Sound.ITEM_TRIDENT_THROW, 1.2f, 0.5f);

        // Draw particle chain
        Vector line = targetLoc.toVector().subtract(hand.toVector());
        double dist = line.length();
        line.normalize();
        for (double d = 0; d < dist; d += 0.5) {
            Location pt = hand.clone().add(line.clone().multiply(d));
            world.spawnParticle(Particle.CRIT, pt, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.DUST, pt, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.fromRGB(0x666666), 1.0f));
        }

        // Pull player toward Nix
        Vector pull = stand.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(1.35);
        pull.setY(0.35);
        target.setVelocity(pull);
        target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 50, 0, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 2, false, true));
        target.sendMessage(ChatColor.DARK_RED + "⛓ You cannot escape the Executioner's sentence!");
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

    private void syncDisplays(NixInstance inst) {
        ArmorStand stand = inst.stand;
        Location root = stand.getLocation().clone();
        root.setYaw(stand.getLocation().getYaw() + 180);
        root.setPitch(0);

        for (NixPart part : NixPart.values()) {
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
        Entity axe = inst.axeDisplay != null ? root.getWorld().getEntity(inst.axeDisplay) : null;
        if (axe instanceof ItemDisplay display && display.isValid()) {
            display.teleport(root);
            display.setTransformation(axeTransformation(inst));
        } else {
            DisplaySuit.SuitTags tags = axeTags(stand.getUniqueId());
            ItemDisplay adopted = DisplaySuit.find(stand.getWorld(), stand.getLocation(), tags);
            inst.axeDisplay = (adopted != null ? adopted : spawnAxe(root, stand.getUniqueId())).getUniqueId();
        }
    }

    private ItemDisplay spawnAxe(Location root, UUID ownerId) {
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
        return DisplaySuit.spawn(root, axe, NixModel.axe(new Quaternionf(), new Quaternionf()), axeTags(ownerId));
    }

    private static DisplaySuit.SuitTags axeTags(UUID ownerId) {
        return new DisplaySuit.SuitTags(PART_TAG, AXE_TAG, partOwnerTag(ownerId));
    }

    /** The axe rides the right forearm: the arm's swing and the elbow's fold, as its lowest piece. */
    private Transformation axeTransformation(NixInstance inst) {
        return NixModel.axe(computeLimbQuat(LimbGroup.ARM_RIGHT, inst), computeLowerQuat(NixPart.ARM_R_1, inst));
    }

    private ItemDisplay findPartDisplay(NixInstance inst, NixPart part) {
        return DisplaySuit.find(inst.stand.getWorld(), inst.stand.getLocation(),
                tags(part, inst.stand.getUniqueId()));
    }

    /**
     * Spawns one piece of the suit. Everything a display piece needs — the head, the rest transform,
     * the zeroed interpolation and box, and the ownership tags — lives in {@link DisplaySuit}, so it
     * stays identical for every dressed boss instead of drifting apart per file.
     */
    private ItemDisplay spawnPart(Location root, NixPart part, UUID ownerId) {
        return DisplaySuit.spawn(root, headOf(part), buildTransformation(part, null), tags(part, ownerId));
    }

    private ItemStack headOf(NixPart part) {
        return DisplaySuit.head(part.profileName, part.texture, "Nix");
    }

    private static DisplaySuit.SuitTags tags(NixPart part, UUID ownerId) {
        return new DisplaySuit.SuitTags(PART_TAG, partTag(part), partOwnerTag(ownerId));
    }

    /**
     * Builds the transformation for a part: rigid-body rotation around its limb's joint and, for a
     * part below the elbow or knee, the fold of that joint on top of it.
     */
    private Transformation buildTransformation(NixPart part, NixInstance inst) {
        Quaternionf limbRot = (inst != null) ? computeLimbQuat(part.group, inst) : new Quaternionf();
        Quaternionf lowerRot = (inst != null) ? computeLowerQuat(part, inst) : new Quaternionf();
        return NixModel.compose(part, limbRot, lowerRot);
    }

    /**
     * The rotation of the segment below a limb's joint. Folds during walking and during a cleave
     * attack: elbows bend dynamically during the overhead windup and snap straight on the chop, while
     * knees flex into a slight squat to absorb the blow.
     */
    private Quaternionf computeLowerQuat(NixPart part, NixInstance inst) {
        if (inst == null) return new Quaternionf();
        if (inst.move != NixMoves.Move.NONE) return NixMoves.lower(inst.move, part, inst.moveTick);
        if (inst.cleaveAnim > 0) {
            float prog = 1f - (float) inst.cleaveAnim / cleaveAnimTicks;
            return NixModel.cleaveLowerRotation(part, prog);
        }
        if (inst.moving && (inst.chainAnim <= 0 || !isArm(part.group))) {
            return NixModel.lowerRotation(part, inst.animTicks);
        }
        return new Quaternionf();
    }

    private static boolean isArm(LimbGroup group) {
        return group == LimbGroup.ARM_RIGHT || group == LimbGroup.ARM_LEFT;
    }

    /**
     * Computes clean, realistic, solid rotations for each limb group.
     */
    private Quaternionf computeLimbQuat(LimbGroup group, NixInstance inst) {
        if (inst.move != NixMoves.Move.NONE) return NixMoves.limb(inst.move, group, inst.moveTick);
        Quaternionf q = new Quaternionf();
        float s = inst.animTicks;
        boolean walking = inst.moving;

        switch (group) {
            case LEG_RIGHT, LEG_LEFT -> {
                if (walking) {
                    // Natural stride: the angle lives in the model so the knee below it can follow it.
                    q.rotateX(NixModel.walkSwing(group, s));
                }
            }
            case ARM_RIGHT -> {
                if (inst.cleaveAnim > 0) {
                    q.rotateX(NixModel.cleaveSwing(1f - (float) inst.cleaveAnim / cleaveAnimTicks, 2.7f));
                } else if (inst.chainAnim > 0) {
                    float p = 1f - (float) inst.chainAnim / chainAnimTicks;
                    float angle = (float) (Math.sin(p * Math.PI) * 1.0);
                    q.rotateX(angle);
                } else if (walking) {
                    q.rotateX(NixModel.walkSwing(group, s));
                }
            }
            case ARM_LEFT -> {
                if (inst.cleaveAnim > 0) {
                    q.rotateX(NixModel.cleaveSwing(1f - (float) inst.cleaveAnim / cleaveAnimTicks, 2.5f));
                } else if (walking) {
                    q.rotateX(NixModel.walkSwing(group, s));
                }
            }
            case TORSO_UPPER, TORSO_LOWER -> {
                if (inst.cleaveAnim > 0) {
                    float prog = 1f - (float) inst.cleaveAnim / cleaveAnimTicks;
                    float angle;
                    if (prog < 0.4f) {
                        float p = prog / 0.4f;
                        angle = (float) (-0.12 * Math.sin(p * Math.PI / 2));
                    } else if (prog < 0.7f) {
                        float p = (prog - 0.4f) / 0.3f;
                        angle = (float) (-0.12 + 0.32 * Math.sin(p * Math.PI / 2));
                    } else {
                        float p = (prog - 0.7f) / 0.3f;
                        angle = (float) (0.20 * (1.0 - Math.sin(p * Math.PI / 2)));
                    }
                    q.rotateX(angle);
                } else if (walking) {
                    q.rotateZ((float) (Math.sin(s * 0.5) * 0.025));
                }
            }
            case HEAD -> {
                // He looks at the target's eyes from his own. The angle used to be taken from his feet
                // and with the sign of a nod, so a player in front of him always had him staring at
                // the floor, and the cleave forced the same nod on top of that.
                Player target = inst.targetId != null ? Bukkit.getPlayer(inst.targetId) : null;
                if (target != null && target.isOnline()) {
                    Vector to = target.getEyeLocation().toVector()
                            .subtract(inst.stand.getLocation().toVector().add(new Vector(0, EYE_HEIGHT, 0)));
                    double horiz = Math.sqrt(to.getX() * to.getX() + to.getZ() * to.getZ());
                    if (horiz > 0.5) {
                        float pitch = (float) Math.toDegrees(Math.atan2(to.getY(), horiz));
                        pitch = Math.max(-25, Math.min(25, pitch));
                        q.rotateX((float) Math.toRadians(pitch));
                    }
                }
            }
        }
        return q;
    }

    // ------------------------------------------------------------------ signature moves

    /** Which signature move fits the distance to the target; null keeps to the cleave and the chains. */
    private NixMoves.Move pickMove(double dist) {
        int roll = random.nextInt(100);
        if (dist <= 5.0) return roll < 65 ? NixMoves.Move.HARVEST : NixMoves.Move.CONDEMN;
        if (dist <= 18.0) return roll < 55 ? NixMoves.Move.GALLOWS : NixMoves.Move.CONDEMN;
        return NixMoves.Move.CONDEMN;
    }

    private void startMove(NixInstance inst, NixMoves.Move move) {
        if (move == null || move == NixMoves.Move.NONE) return;
        inst.move = move;
        inst.moveTick = 0;
        inst.marks.clear();
        inst.struck.clear();
        inst.leapFrom = null;
        inst.leapTo = null;
        inst.cleaveAnim = 0;
        inst.chainAnim = 0;
    }

    /**
     * Plays one tick of the current signature move.
     *
     * @return whether Nix is in the air this tick, so the caller must not settle it on the floor
     */
    private boolean runMove(NixInstance inst, Player target, Location loc) {
        Fx fx = LiveStage.fxIn(loc.getWorld());
        int t = inst.moveTick;
        boolean airborne = switch (inst.move) {
            case HARVEST -> {
                harvestTick(inst, fx, loc, t);
                yield false;
            }
            case GALLOWS -> gallowsTick(inst, fx, loc, target, t);
            case CONDEMN -> {
                condemnTick(inst, fx, loc, target, t);
                yield false;
            }
            default -> false;
        };
        inst.moveTick++;
        if (inst.moveTick >= inst.move.ticks) {
            inst.move = NixMoves.Move.NONE;
            inst.moveTick = 0;
            inst.marks.clear();
            inst.struck.clear();
            inst.moveCooldown = specialCooldownTicks + random.nextInt(40);
            inst.meleeCooldown = Math.max(inst.meleeCooldown, 10);
        }
        return airborne;
    }

    /** Blood Harvest: arms flung wide, three whirling revolutions of blood blades. */
    private void harvestTick(NixInstance inst, Fx fx, Location loc, int t) {
        final double radius = 4.5;
        Vector feet = loc.toVector();
        Vector floor = feet.clone().add(new Vector(0, 0.15, 0));
        if (t == 0) {
            fx.sound(feet, Sfx.RAVAGER_ROAR, 1.8f, 1.3f);
            fx.sound(feet, Sfx.WARDEN_HEARTBEAT, 2f, 0.6f);
        }
        if (t < NixMoves.HARVEST_WIND) {
            float p = NixMoves.phase(t, 0, NixMoves.HARVEST_WIND);
            if (t % 2 == 0) fx.ring(floor, radius, 0.6, t * 0.2, fx.dust(Palette.mix(Palette.WARNING, Palette.WARNING_HOT, p), 1.4f));
            Vector right = rightOf(loc);
            for (int side = -1; side <= 1; side += 2) {
                Vector hand = feet.clone().add(new Vector(0, 1.45, 0)).add(right.clone().multiply(side * 1.2 * p));
                if (t % 3 == 0) fx.gather(hand, 2.2, 3, Palette.BLOOD, 8);
                fx.dust(Palette.BLOOD, 1.2f, 0.15, 1).at(hand);
            }
            if (t == NixMoves.HARVEST_WIND - 3) fx.sound(feet, Sfx.PLAYER_ATTACK_SWEEP, 2f, 0.5f);
            return;
        }
        if (t < NixMoves.HARVEST_SPIN_END) {
            int s = t - NixMoves.HARVEST_WIND;
            loc.setYaw(loc.getYaw() + 360f / NixMoves.HARVEST_REVOLUTION);
            Vector right = rightOf(loc);
            Vector blades = feet.clone().add(new Vector(0, 1.25, 0));
            double angle = Math.atan2(right.getZ(), right.getX());
            for (int side = -1; side <= 1; side += 2) {
                Vector hand = feet.clone().add(new Vector(0, 1.45, 0)).add(right.clone().multiply(side * 1.2));
                Vector tip = blades.clone().add(right.clone().multiply(side * radius));
                fx.line(hand, tip, 0.35, fx.dust(Palette.BLOOD, 1.7f));
                double a = side > 0 ? angle : angle + Math.PI;
                fx.draw(Shapes.arc(blades, radius - 0.4, a - 1.1, a, 16, Shapes.FLAT_U, Shapes.FLAT_V),
                        fx.fade(Palette.BLOOD, Palette.ASH, 1.4f));
                fx.draw(Shapes.arc(blades, radius * 0.6, a - 0.7, a, 8, Shapes.FLAT_U, Shapes.FLAT_V),
                        fx.dust(Palette.mix(Palette.BLOOD, Palette.EMBER, 0.3), 1.1f).sometimes(0.7));
            }
            if (s % 3 == 0) fx.cloud(Particle.SWEEP_ATTACK, blades, 2, radius * 0.5, 0.2, 0);
            fx.crumble(groundUnder(loc), 3, radius * 0.4).at(floor);
            if (s % 4 == 0) fx.sound(feet, Sfx.PLAYER_ATTACK_SWEEP, 1.6f, 0.7f + s * 0.02f);
            if (s % NixMoves.HARVEST_REVOLUTION == NixMoves.HARVEST_REVOLUTION / 2) {
                for (Player p : victims(loc.getWorld())) {
                    Vector to = p.getLocation().toVector().subtract(feet);
                    double dy = to.getY();
                    to.setY(0);
                    if (to.length() > radius + 0.5 || dy < -1.5 || dy > 3) continue;
                    dealToPlayer(inst.stand, p, harvestDamage, "Blood Harvest");
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 50, 0, false, true));
                    Vector in = to.lengthSquared() < 0.01 ? new Vector() : to.normalize().multiply(-0.35);
                    p.setVelocity(p.getVelocity().add(in.setY(0.2)));
                    fx.impact(p.getLocation().toVector().add(new Vector(0, 1, 0)), Palette.BLOOD, 1.2);
                }
            }
            return;
        }
        if (t == NixMoves.HARVEST_SPIN_END) {
            for (Vector p : Shapes.ring(floor, radius, 0.5, 0)) fx.dust(Palette.BLOOD, 2f, 0.2, 2).at(p.add(new Vector(0, 0.4, 0)));
            fx.flatBurst(floor, Particle.DAMAGE_INDICATOR, 20, 0.3);
            fx.sound(feet, Sfx.PLAYER_ATTACK_STRONG, 2f, 0.5f);
        }
    }

    /** Gallows Leap: crouch, leap onto the target, slam down with both arms. */
    private boolean gallowsTick(NixInstance inst, Fx fx, Location loc, Player target, int t) {
        final double radius = 4.0;
        final double height = 7.0;
        Vector feet = loc.toVector();
        if (t == 0) {
            inst.leapFrom = feet.clone();
            fx.sound(feet, Sfx.RAVAGER_ROAR, 1.8f, 0.8f);
        }
        if (t < NixMoves.GALLOWS_CROUCH) {
            float p = NixMoves.phase(t, 0, NixMoves.GALLOWS_CROUCH);
            Vector want = target != null ? target.getLocation().toVector() : feet.clone().add(loc.getDirection().setY(0).multiply(8));
            Vector reach = want.clone().subtract(feet).setY(0);
            if (reach.length() > 18) reach.normalize().multiply(18);
            Vector landing = feet.clone().add(reach);
            double floorY = BossArena.findFloorY(landing.toLocation(loc.getWorld()).add(0, 4, 0), 16);
            landing.setY(Double.isNaN(floorY) ? feet.getY() : floorY);
            inst.leapTo = landing;
            if (reach.lengthSquared() > 0.01) loc.setDirection(reach);
            if (t % 2 == 0) {
                Vector mark = landing.clone().add(new Vector(0, 0.15, 0));
                fx.ring(mark, radius, 0.6, t * 0.1, fx.dust(Palette.mix(Palette.WARNING, Palette.WARNING_HOT, p), 1.5f));
                fx.ring(mark, radius * 0.5, 0.6, -t * 0.1, fx.dust(Palette.BLOOD, 1.1f));
            }
            fx.cloud(Particle.CLOUD, feet, 2, 0.6, 0.02);
            if (t % 4 == 0) fx.crumble(groundUnder(loc), 6, 0.6).at(feet.clone().add(new Vector(0, 0.2, 0)));
            if (t == NixMoves.GALLOWS_CROUCH - 1) {
                fx.sound(feet, Sfx.WIND_CHARGE_BURST, 2f, 0.6f);
                fx.flatBurst(feet, Particle.CLOUD, 25, 0.35);
            }
            return false;
        }
        if (t < NixMoves.GALLOWS_LAND) {
            if (inst.leapFrom == null || inst.leapTo == null) return false;
            double p = (t - NixMoves.GALLOWS_CROUCH + 1) / (double) (NixMoves.GALLOWS_LAND - NixMoves.GALLOWS_CROUCH);
            Vector at = inst.leapFrom.clone().add(inst.leapTo.clone().subtract(inst.leapFrom).multiply(p))
                    .add(new Vector(0, height * 4 * p * (1 - p), 0));
            loc.setX(at.getX());
            loc.setY(at.getY());
            loc.setZ(at.getZ());
            Vector body = at.clone().add(new Vector(0, 1.2, 0));
            fx.dust(Palette.BLOOD, 1.8f, 0.4, 3).at(body);
            fx.cloud(Particle.LARGE_SMOKE, body, 2, 0.3, 0.01);
            if (t % 2 == 0) {
                Vector mark = inst.leapTo.clone().add(new Vector(0, 0.15, 0));
                fx.ring(mark, radius, 0.5, t * 0.2, fx.dust(Palette.WARNING_HOT, 1.6f));
                fx.line(body, mark, 1.0, fx.dust(Palette.BLOOD, 0.8f).sometimes(0.5));
            }
            return p < 1;
        }
        Vector center = feet.clone();
        if (t == NixMoves.GALLOWS_LAND) {
            Vector mid = center.clone().add(new Vector(0, 0.5, 0));
            fx.impact(mid, Palette.BLOOD, 2.5);
            fx.flatBurst(center, Particle.CLOUD, 40, 0.5);
            fx.crumble(groundUnder(loc), 40, radius * 0.5).at(mid);
            for (int i = 0; i < 8; i++) {
                Vector way = Shapes.heading(i * Math.PI / 4 + 0.3);
                fx.line(center.clone().add(new Vector(0, 0.1, 0)), center.clone().add(way.multiply(radius + 2)).add(new Vector(0, 0.1, 0)),
                        0.4, fx.dust(Palette.ASH, 1.4f).and(fx.dust(Palette.BLOOD, 1f).sometimes(0.3)));
            }
            fx.sound(center, Sfx.MACE_SMASH_GROUND, 2.5f, 0.6f);
            fx.sound(center, Sfx.ANVIL_LAND, 1.5f, 0.5f);
            fx.sound(center, Sfx.EXPLODE, 1.5f, 0.8f);
            for (Player p : victims(loc.getWorld())) {
                Vector to = p.getLocation().toVector().subtract(center);
                double dy = to.getY();
                if (Math.hypot(to.getX(), to.getZ()) > radius || dy < -1.5 || dy > 3) continue;
                inst.struck.add(p.getUniqueId());
                dealToPlayer(inst.stand, p, gallowsDamage, "Gallows Leap");
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 50, 2, false, true));
                p.setVelocity(p.getVelocity().add(new Vector(0, 0.9, 0)));
            }
        } else if (t < NixMoves.GALLOWS_LAND + 10) {
            double r = radius + (t - NixMoves.GALLOWS_LAND) * 0.6;
            Vector floor = center.clone().add(new Vector(0, 0.25, 0));
            fx.ring(floor, r, 0.7, t, fx.dust(Palette.ASH, 1.8f).and(fx.crumble(groundUnder(loc), 1, 0.2).sometimes(0.5)));
            for (Player p : victims(loc.getWorld())) {
                Vector to = p.getLocation().toVector().subtract(center);
                double flat = Math.hypot(to.getX(), to.getZ());
                if (Math.abs(flat - r) > 1.0 || Math.abs(to.getY()) > 1.5 || !inst.struck.add(p.getUniqueId())) continue;
                dealToPlayer(inst.stand, p, gallowsDamage * 0.5, "Gallows Leap");
                p.setVelocity(p.getVelocity().add(new Vector(0, 0.5, 0)));
            }
        }
        return false;
    }

    /** Condemnation: a guillotine over every player, falling when the raised arm chops down. */
    private void condemnTick(NixInstance inst, Fx fx, Location loc, Player target, int t) {
        final double radius = 1.8;
        final double top = 5.5;
        Vector feet = loc.toVector();
        Vector hand = feet.clone().add(new Vector(0, 3.0, 0));
        if (t == 0) {
            fx.sound(feet, Sfx.ELDER_GUARDIAN_CURSE, 1.2f, 0.7f);
            fx.sound(feet, Sfx.EVOKER_PREPARE_SUMMON, 1.6f, 0.6f);
        }
        if (t < NixMoves.CONDEMN_RAISE) {
            if (target != null) loc.setDirection(target.getLocation().toVector().subtract(feet).setY(0));
            fx.gather(hand, 1.5, 2, Palette.BLOOD, 6);
            return;
        }
        if (t == NixMoves.CONDEMN_RAISE) {
            for (Player p : victims(loc.getWorld())) {
                if (inst.marks.size() >= 4) break;
                if (p.getLocation().distanceSquared(loc) > aggroRange * aggroRange) continue;
                Vector at = p.getLocation().toVector();
                double floorY = BossArena.findFloorY(p.getLocation().add(0, 0.5, 0), 8);
                if (!Double.isNaN(floorY)) at.setY(floorY);
                inst.marks.add(at);
                fx.sound(at, Sfx.BELL_RESONATE, 1.5f, 0.6f);
                p.sendMessage(ChatColor.DARK_RED + "⚖ You have been condemned. Step out of the circle!");
            }
        }
        if (t < NixMoves.CONDEMN_CHOP) {
            float p = NixMoves.phase(t, NixMoves.CONDEMN_RAISE, NixMoves.CONDEMN_CHOP);
            double shake = Math.sin(t * 1.9) * 0.08;
            for (Vector m : inst.marks) {
                Vector side = sideOf(feet, m);
                Vector floor = m.clone().add(new Vector(0, 0.15, 0));
                if (t % 2 == 0) {
                    fx.ring(floor, radius, 0.35, t * 0.15, fx.dust(Palette.mix(Palette.WARNING, Palette.WARNING_HOT, p), 1.3f));
                    drawGallows(fx, m, side, top, 0.5);
                }
                Vector blade = m.clone().add(new Vector(0, top - 0.6 + shake, 0));
                fx.line(blade.clone().subtract(side.clone().multiply(1.3)), blade.clone().add(side.clone().multiply(1.3)).add(new Vector(0, -0.45, 0)),
                        0.2, fx.dust(Palette.ICE, 1.1f));
                if (t % 5 == 0) fx.line(hand, m.clone().add(new Vector(0, top, 0)), 0.8, fx.dust(Palette.BLOOD, 0.8f).sometimes(0.6));
                if (t % 10 == 0) fx.sound(m, Sfx.WARDEN_HEARTBEAT, 1.2f + p, 1.0f + p * 0.5f);
            }
            if (t == NixMoves.CONDEMN_CHOP - 2) {
                for (Vector m : inst.marks) fx.sound(m, Sfx.CHAIN_BREAK, 2f, 0.6f);
            }
            return;
        }
        if (t < NixMoves.CONDEMN_CHOP + NixMoves.CONDEMN_FALL) {
            double fall = (t - NixMoves.CONDEMN_CHOP + 1) / (double) NixMoves.CONDEMN_FALL;
            for (Vector m : inst.marks) {
                Vector side = sideOf(feet, m);
                drawGallows(fx, m, side, top, 0.5);
                Vector blade = m.clone().add(new Vector(0, (top - 0.6) * (1 - fall) + 0.2, 0));
                fx.line(blade.clone().subtract(side.clone().multiply(1.3)), blade.clone().add(side.clone().multiply(1.3)).add(new Vector(0, -0.45, 0)),
                        0.15, fx.dust(Palette.ICE, 1.5f).and(fx.particle(Particle.CRIT).sometimes(0.4)));
            }
            if (t != NixMoves.CONDEMN_CHOP + NixMoves.CONDEMN_FALL - 1) return;
            fx.sound(feet, Sfx.PLAYER_ATTACK_CRIT, 2f, 0.5f);
            for (Vector m : inst.marks) {
                Vector mid = m.clone().add(new Vector(0, 0.6, 0));
                fx.impact(mid, Palette.BLOOD, 1.8);
                fx.flatBurst(m, Particle.DAMAGE_INDICATOR, 12, 0.25);
                fx.sound(m, Sfx.ANVIL_LAND, 1.6f, 1.4f);
                fx.sound(m, Sfx.BONE_BREAK, 2f, 0.6f);
                for (Player p : victims(loc.getWorld())) {
                    Vector to = p.getLocation().toVector().subtract(m);
                    if (Math.hypot(to.getX(), to.getZ()) > radius || to.getY() < -1 || to.getY() > 3) continue;
                    if (!inst.struck.add(p.getUniqueId())) continue;
                    dealToPlayer(inst.stand, p, condemnDamage, "Condemnation");
                    p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1, false, true));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, true));
                }
            }
        }
    }

    /** A gallows frame in particles: two posts and a crossbar, the blade's rails. */
    private static void drawGallows(Fx fx, Vector base, Vector side, double top, double spacing) {
        Vector left = base.clone().subtract(side.clone().multiply(1.6));
        Vector right = base.clone().add(side.clone().multiply(1.6));
        Fx.Brush wood = fx.dust(Palette.ASH, 1.3f);
        fx.line(left, left.clone().add(new Vector(0, top, 0)), spacing, wood);
        fx.line(right, right.clone().add(new Vector(0, top, 0)), spacing, wood);
        fx.line(left.clone().add(new Vector(0, top, 0)), right.clone().add(new Vector(0, top, 0)), spacing, fx.dust(Palette.BLOOD, 1.3f));
    }

    /** Horizontal unit vector across the line from Nix to a mark, so a gallows faces him. */
    private static Vector sideOf(Vector from, Vector mark) {
        Vector d = mark.clone().subtract(from).setY(0);
        if (d.lengthSquared() < 0.01) d = new Vector(0, 0, 1);
        d.normalize();
        return new Vector(-d.getZ(), 0, d.getX());
    }

    /** The stand's right-hand side, flat. */
    private static Vector rightOf(Location loc) {
        double yaw = Math.toRadians(loc.getYaw());
        return new Vector(-Math.cos(yaw), 0, -Math.sin(yaw));
    }

    private static Material groundUnder(Location loc) {
        Material type = loc.clone().add(0, -0.5, 0).getBlock().getType();
        return type.isSolid() ? type : Material.STONE;
    }

    private static List<Player> victims(World world) {
        List<Player> out = new ArrayList<>();
        for (Player p : world.getPlayers()) {
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR || p.isDead()) continue;
            out.add(p);
        }
        return out;
    }

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.nix-executioner")) return false;
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

        NixInstance inst = new NixInstance(stand);
        activeInstances.put(stand.getUniqueId(), inst);
        setupBossBar(inst);

        Location root = stand.getLocation().clone();
        root.setYaw(stand.getLocation().getYaw() + 180);
        root.setPitch(0);

        for (NixPart part : NixPart.values()) {
            ItemDisplay display = spawnPart(root, part, stand.getUniqueId());
            inst.partDisplays.put(part, display.getUniqueId());
        }

        location.getWorld().playSound(location, Sound.ENTITY_WITHER_SPAWN, 1.2f, 0.6f);
        location.getWorld().spawnParticle(Particle.DUST, location.clone().add(0, 1.5, 0), 80, 1.5, 2.0, 1.5, 0,
                new Particle.DustOptions(Color.fromRGB(0x880000), 2.5f));
        return true;
    }

    private void cleanup(NixInstance inst) {
        World world = (inst.stand != null) ? inst.stand.getWorld() : null;
        DisplaySuit.remove(world, inst.partDisplays.values());
        inst.partDisplays.clear();
        if (inst.axeDisplay != null) DisplaySuit.remove(world, List.of(inst.axeDisplay));
        inst.axeDisplay = null;
        if (inst.bossBar != null) {
            inst.bossBar.removeAll();
            inst.bossBar = null;
        }
    }

    private void setupBossBar(NixInstance inst) {
        BossBar bar = MscBossBar.create(BAR_TITLE, BarColor.RED, BarStyle.SEGMENTED_12, BarFlag.DARKEN_SKY, BarFlag.CREATE_FOG);
        MscBossBar.showInWorld(bar, inst.stand.getWorld());
        inst.bossBar = bar;
    }

    public boolean isBossActive() {
        return !activeInstances.isEmpty();
    }

    public boolean isBossActiveIn(World world) {
        if (world == null) return false;
        for (NixInstance inst : activeInstances.values()) {
            if (inst.stand != null && inst.stand.isValid() && world.equals(inst.stand.getWorld())) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();

        // A hit Nix lands on a player: pair the intended damage with what the player actually took.
        if (damaged instanceof Player victim && isNixAttacker(event.getDamager())) {
            plugin.getBossDamageLog().record(victim.getUniqueId(), BossDamageSample.dealt(BossId.NIX,
                    outgoingSource == null ? "Attack" : outgoingSource,
                    outgoingSource == null ? event.getFinalDamage() : outgoingIntended,
                    event.getFinalDamage(), System.currentTimeMillis()));
            return;
        }

        if (damaged instanceof ArmorStand stand && stand.getScoreboardTags().contains(TAG)) {
            Player player = attackerPlayer(event.getDamager());
            if (player != null) {
                event.setCancelled(true);
                recordIncoming(player, stand, Math.max(1.0, event.getFinalDamage()));
                hitEffect(stand);
            }
            return;
        }

        if (damaged instanceof ItemDisplay display && display.getScoreboardTags().contains(PART_TAG)) {
            ArmorStand stand = findOwner(display);
            if (stand != null && !stand.isDead() && stand.isValid()) {
                event.setCancelled(true);
                double damage = Math.max(1.0, event.getDamage());
                Player player = attackerPlayer(event.getDamager());
                if (player != null) {
                    recordIncoming(player, stand, damage);
                } else {
                    reduceHealth(stand, damage);
                }
                hitEffect(stand);
            }
            return;
        }

        // Friendly fire protection between MSC entities
        boolean damagerMsc = false;
        boolean damagedMsc = false;
        for (String tag : event.getDamager().getScoreboardTags()) {
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

    private void hitEffect(ArmorStand stand) {
        Location loc = stand.getLocation().clone().add(0, 1.5, 0);
        stand.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, loc, 10, 0.4, 0.6, 0.4, 0.1);
        stand.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.9f, 0.8f);
    }

    /**
     * Clamps one incoming hit to the configured ceiling. A {@code cap <= 0} disables the limit.
     * Exposed for tests: the damage path itself needs a live server.
     */
    static double capIncomingDamage(double damage, double cap) {
        return cap > 0 ? Math.min(damage, cap) : damage;
    }

    /** Damages a player with this boss and remembers the attack so the listener can name it. */
    private void dealToPlayer(ArmorStand stand, Player target, double amount, String source) {
        // Saved and restored rather than cleared: a hit can trigger a nested one, and the outer call
        // must find its own attack name again afterwards.
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

    /** Records the incoming hit's cap for {@code /msc debug}, then applies it as usual. */
    private void recordIncoming(Player player, ArmorStand stand, double damage) {
        double applied = capIncomingDamage(damage, maxDamagePerHit);
        plugin.getBossDamageLog().record(player.getUniqueId(), BossDamageSample.taken(BossId.NIX,
                "Incoming hit", damage, applied,
                applied < damage ? "cap " + maxDamagePerHit : "", System.currentTimeMillis()));
        reduceHealth(stand, damage);
    }

    /** The player behind a melee or projectile hit, or {@code null} for anything else. */
    private static Player attackerPlayer(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player p) {
            return p;
        }
        return null;
    }

    /** Whether a damage source is one of this boss's armor stands. */
    private static boolean isNixAttacker(Entity damager) {
        if (damager instanceof ArmorStand stand) return stand.getScoreboardTags().contains(TAG);
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof ArmorStand stand) {
            return stand.getScoreboardTags().contains(TAG);
        }
        return false;
    }

    private void reduceHealth(ArmorStand stand, double damage) {
        stand.setNoDamageTicks(0);
        double currentHealth = MscEntityUtils.getVirtualHealth(stand);
        double newHealth = Math.max(0, currentHealth - capIncomingDamage(damage, maxDamagePerHit));
        MscEntityUtils.setVirtualHealth(stand, newHealth);

        NixInstance inst = activeInstances.get(stand.getUniqueId());
        if (inst != null && inst.bossBar != null) {
            double maxHealth = MscEntityUtils.getVirtualMaxHealth(stand);
            inst.bossBar.setProgress(MscEntityUtils.calculateVirtualProgress(newHealth, maxHealth));
        }
    }

    private ArmorStand findOwner(Entity entity) {
        ArmorStand best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : entity.getNearbyEntities(3, 3, 3)) {
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

        NixInstance inst = activeInstances.remove(stand.getUniqueId());
        if (inst != null) cleanup(inst);

        Location loc = stand.getLocation();
        World world = stand.getWorld();
        world.playSound(loc, Sound.ENTITY_WITHER_DEATH, 1.2f, 0.6f);
        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.7f);
        world.spawnParticle(Particle.DUST, loc.clone().add(0, 1.5, 0), 100, 1.5, 2.0, 1.5, 0,
                new Particle.DustOptions(Color.fromRGB(0x880000), 2.5f));
        world.spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 1.5, 0), 12, 1.5, 1.5, 1.5, 0);

        // Experience and title announcement
        world.spawn(loc, org.bukkit.entity.ExperienceOrb.class).setExperience(450);
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(loc) <= 60 * 60) {
                p.sendTitle(ChatColor.DARK_RED + "" + ChatColor.BOLD + "NIX",
                        ChatColor.GRAY + "The sentence has ended.", 10, 60, 20);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof ArmorStand stand)) return;
        if (!stand.getScoreboardTags().contains(TAG)) return;
        List<String> messages = plugin.getConfig().getStringList("entities.nix-executioner.death-messages");
        if (!messages.isEmpty()) {
            String raw = messages.get(random.nextInt(messages.size()));
            event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', raw.replace("%player%", event.getEntity().getName())));
        }
    }

    public static class NixInstance {
        public final ArmorStand stand;
        public final Map<NixPart, UUID> partDisplays = new EnumMap<>(NixPart.class);
        public UUID axeDisplay;
        public BossBar bossBar;
        public UUID targetId;
        public int meleeCooldown;
        public int chainCooldown;
        public int cleaveAnim;
        public int chainAnim;
        public boolean moving;
        public boolean bloodlust;
        public float animTicks;
        public int tickCount;
        public NixMoves.Move move = NixMoves.Move.NONE;
        public int moveTick;
        /** The first signature move waits a few seconds into the fight. */
        public int moveCooldown = 100;
        public Vector leapFrom;
        public Vector leapTo;
        public final List<Vector> marks = new ArrayList<>();
        public final java.util.Set<UUID> struck = new java.util.HashSet<>();

        public NixInstance(ArmorStand stand) {
            this.stand = stand;
        }
    }
}
