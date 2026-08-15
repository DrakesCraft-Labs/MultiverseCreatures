package com.Chagui68.integration;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Contrato opcional con DrakesBosses/Odysseia sin enlazado binario entre plugins.
 *
 * La integración sólo usa Bukkit: funciona aunque DrakesBosses no esté instalado y evita que el
 * miniboss invada `boss_arena`. UltraGod publica la invulnerabilidad nativa del jugador, que es
 * suficiente para que Mahoraga nunca fuerce un daño administrativo.
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
}
