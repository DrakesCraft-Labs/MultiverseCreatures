package com.Chagui68.skin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

public class PlayerSkinDisguiseService {

    private final Plugin plugin;
    private final Random random = new Random();
    private final boolean enabled;
    private final String source;
    private final String fixedSkinName;
    private final double maxDistance;
    private boolean warnedMissingDependency;

    public PlayerSkinDisguiseService(Plugin plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("skins.enabled", false);
        this.source = plugin.getConfig().getString("skins.source", "fixed");
        this.fixedSkinName = plugin.getConfig().getString("skins.fixed-name", "Notch");
        this.maxDistance = plugin.getConfig().getDouble("skins.nearest-player-max-distance", 64.0D);
    }

    public void applyPlayerSkin(LivingEntity entity) {
        if (!enabled) {
            return;
        }

        if (!isLibsDisguisesAvailable()) {
            if (!warnedMissingDependency) {
                warnedMissingDependency = true;
                plugin.getLogger().warning("skins.enabled=true pero falta LibsDisguises. Instala ese plugin para aplicar skins.");
            }
            return;
        }

        String skinName = resolveSkinName(entity);
        if (skinName == null || skinName.isBlank()) {
            return;
        }

        try {
            Class<?> disguiseClass = Class.forName("me.libraryaddict.disguise.disguisetypes.Disguise");
            Class<?> playerDisguiseClass = Class.forName("me.libraryaddict.disguise.disguisetypes.PlayerDisguise");
            Class<?> disguiseApiClass = Class.forName("me.libraryaddict.disguise.DisguiseAPI");

            Constructor<?> constructor = playerDisguiseClass.getConstructor(String.class);
            Object disguise = constructor.newInstance("MSC_" + entity.getEntityId());

            Method setSkin = playerDisguiseClass.getMethod("setSkin", String.class);
            setSkin.invoke(disguise, skinName);

            Method disguiseEntity = disguiseApiClass.getMethod("disguiseEntity", Entity.class, disguiseClass);
            disguiseEntity.invoke(null, entity, disguise);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("No se pudo aplicar skin '" + skinName + "' a entidad " + entity.getType()
                + ": " + exception.getMessage());
        }
    }

    private boolean isLibsDisguisesAvailable() {
        return plugin.getServer().getPluginManager().getPlugin("LibsDisguises") != null;
    }

    private String resolveSkinName(LivingEntity entity) {
        String sourceNormalized = source == null ? "fixed" : source.toLowerCase(Locale.ROOT);
        return switch (sourceNormalized) {
            case "nearest-player" -> resolveNearestOnlinePlayer(entity).orElse(fixedSkinName);
            case "random-online-player" -> resolveRandomOnlinePlayer().orElse(fixedSkinName);
            case "entity-name" -> {
                String clean = entity.getCustomName() == null ? null : ChatColor.stripColor(entity.getCustomName());
                yield (clean == null || clean.isBlank()) ? fixedSkinName : clean.trim();
            }
            default -> fixedSkinName;
        };
    }

    private Optional<String> resolveNearestOnlinePlayer(LivingEntity entity) {
        return entity.getWorld().getPlayers().stream()
            .filter(player -> player.getLocation().distanceSquared(entity.getLocation()) <= maxDistance * maxDistance)
            .min(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(entity.getLocation())))
            .map(Player::getName);
    }

    private Optional<String> resolveRandomOnlinePlayer() {
        List<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers().stream().toList();
        if (onlinePlayers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(onlinePlayers.get(random.nextInt(onlinePlayers.size())).getName());
    }
}
