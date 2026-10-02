package com.Chagui68.integration;

import com.Chagui68.utils.MscLog;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

/**
 * Optional contract with DrakesBosses/Odysseia without hard plugin dependency.
 *
 * Integration uses only Bukkit: it works even if DrakesBosses is not installed and prevents
 * the miniboss from invading `boss_arena`. UltraGod exposes native player invulnerability,
 * which is enough for Mahoraga to never force administrative damage.
 *
 * <p>Summoning goes through DrakesBosses' public API by reflection
 * ({@code DrakesBosses#getBossManager().spawnBoss(String, Location)}), so this jar neither
 * compiles against nor ships DrakesBosses: without it the altar simply stays silent.
 */
public final class DrakesBossesIntegration {

    private static final String DRAKES_BOSSES = "DrakesBosses";

    private DrakesBossesIntegration() {
    }

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled(DRAKES_BOSSES);
    }

    public static boolean isUltraGod(Player player) {
        return player.isInvulnerable();
    }

    public static boolean isArenaWorld(World world) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(DRAKES_BOSSES);
        if (!(plugin instanceof JavaPlugin drakesBosses) || !plugin.isEnabled()) return false;
        String arenaWorld = drakesBosses.getConfig().getString("boss-arena.world-name", "drakes_bosses");
        return world.getName().equalsIgnoreCase(arenaWorld);
    }

    /**
     * Asks DrakesBosses to spawn the boss {@code type} at {@code location}.
     *
     * @return the boss's entity, or null when DrakesBosses is missing, refuses the type (an unknown
     *         id, or Jax switched off in its config) or fails
     */
    public static LivingEntity spawnBoss(String type, Location location) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(DRAKES_BOSSES);
        if (plugin == null || !plugin.isEnabled()) return null;
        try {
            Object manager = plugin.getClass().getMethod("getBossManager").invoke(plugin);
            if (manager == null) return null;
            Method spawn = manager.getClass().getMethod("spawnBoss", String.class, Location.class);
            Object boss = spawn.invoke(manager, type, location);
            if (boss == null) return null;
            // Looked up on the declared return type (OdysseyBoss, public) rather than on the
            // instance's class, which may be one DrakesBosses does not export.
            Object entity = spawn.getReturnType().getMethod("getEntity").invoke(boss);
            return entity instanceof LivingEntity living ? living : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            Throwable cause = e instanceof java.lang.reflect.InvocationTargetException wrapped
                    && wrapped.getCause() != null ? wrapped.getCause() : e;
            MscLog.warn("DrakesBosses could not spawn " + type, cause);
            return null;
        }
    }
}
