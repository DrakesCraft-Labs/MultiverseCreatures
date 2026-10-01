package com.Chagui68.listener.bossdimension;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.ritual.BossDimensionManager;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Set;

/**
 * What the ritual dimension locks down while a boss fights in it: building, breaking and most
 * commands, for everyone without {@code msc.admin.bypass}.
 *
 * <p>The block and command handlers each carried their own copy of this check, and both forgot
 * JackStar: during his fight players could build, break and run any command.
 */
final class BossFightGuard {

    static final String BYPASS_PERMISSION = "msc.admin.bypass";

    /** Commands a player may still run mid-fight, by label. */
    static final Set<String> ALLOWED_COMMANDS = Set.of("say", "me", "help", "?", "dimtp");

    private BossFightGuard() {
    }

    /** Whether the fight's restrictions apply to {@code player} right now. */
    static boolean restricts(MultiverseCreatures plugin, Player player) {
        BossDimensionManager dimension = plugin.getBossDimensionManager();
        if (dimension == null || dimension.getBossWorld() == null) return false;
        if (!player.getWorld().equals(dimension.getBossWorld())) return false;
        if (player.hasPermission(BYPASS_PERMISSION)) return false;
        return plugin.isBossFightIn(player.getWorld());
    }

    /**
     * Whether a typed command is one of {@link #ALLOWED_COMMANDS}. The label is matched whole and
     * without its namespace: a prefix test let {@code /menu} through as {@code /me} and
     * {@code /sayhi} as {@code /say}.
     */
    static boolean isAllowedCommand(String message) {
        if (message == null) return false;
        String text = message.trim();
        if (text.startsWith("/")) text = text.substring(1);
        int space = text.indexOf(' ');
        String label = (space < 0 ? text : text.substring(0, space)).toLowerCase(Locale.ROOT);
        int namespace = label.indexOf(':');
        if (namespace >= 0) label = label.substring(namespace + 1);
        return ALLOWED_COMMANDS.contains(label);
    }
}
