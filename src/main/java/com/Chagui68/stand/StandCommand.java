package com.Chagui68.stand;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.utils.MscText;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * {@code /stand}: what a player's Stand is and how to use it, plus the Stand commands.
 *
 * <pre>
 * /stand                         your Stand, your blood and the abilities
 * /stand invocar                 summon or send back (same as sneak + F)
 * /stand habilidad [1|2]         use an ability (same as F, or sneak + left click)
 * /stand sha                     Killer Queen: Sheer Heart Attack
 * /stand dar &lt;jugador&gt; [stand]  (admin) awaken a Stand, random when not named
 * /stand quitar &lt;jugador&gt;        (admin) take the Stand away
 * /stand vampiro &lt;jugador&gt; &lt;si|no&gt; (admin) give or cure DIO's blood
 * /stand flecha &lt;jugador&gt;        (admin) pierce a player with the Arrow
 * </pre>
 */
public final class StandCommand implements TabExecutor {

    private static final String ADMIN = "msc.admin";
    private static final List<String> PLAYER_SUBS = List.of("invocar", "habilidad", "sha");
    private static final List<String> ADMIN_SUBS = List.of("dar", "quitar", "vampiro", "flecha");

    private final MultiverseCreatures plugin;

    public StandCommand(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
                             @NotNull String[] args) {
        StandManager stands = plugin.getStandManager();
        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        if (ADMIN_SUBS.contains(sub)) {
            return admin(sender, sub, args);
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Uso: /stand <dar|quitar|vampiro|flecha> <jugador>", RED));
            return true;
        }
        switch (sub) {
            case "invocar", "summon" -> stands.toggle(player);
            case "habilidad", "ability" -> stands.ability(player, args.length > 1 && args[1].equals("2") ? 2 : 1);
            case "sha", "sheerheartattack" -> stands.openSheerHeartAttack(player);
            default -> info(player);
        }
        return true;
    }

    private void info(Player player) {
        StandType stand = StandData.stand(player);
        player.sendMessage(MscText.title(GOLD, "✦ Stand ✦"));
        if (stand == null) {
            player.sendMessage(MscText.line(GRAY, "No tienes ningún Stand."));
            player.sendMessage(MscText.rich(GRAY, "Bebe el ", DARK_RED, "Elixir del Portador", GRAY,
                    " (sangre de DIO) y deja que la ", GOLD, "Flecha", GRAY, " del Arquero te elija."));
        } else {
            player.sendMessage(MscText.rich(GRAY, "Tu Stand: ", stand.textColor(), "「" + stand.displayName() + "」"));
            player.sendMessage(MscText.rich(YELLOW, "Agachado + F: ", WHITE, "invocar o retirar"));
            for (Component line : StandManager.describe(stand)) {
                player.sendMessage(line);
            }
        }
        player.sendMessage(MscText.rich(GRAY, "Portador: ", StandData.isBearer(player) ? GREEN : RED,
                StandData.isBearer(player) ? "sí" : "no", GRAY, " · Vampiro: ",
                StandData.isVampire(player) ? DARK_RED : GRAY, StandData.isVampire(player) ? "sí" : "no"));
    }

    private boolean admin(CommandSender sender, String sub, String[] args) {
        if (!sender.hasPermission(ADMIN)) {
            sender.sendMessage(Component.text("No tienes permiso.", RED));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Uso: /stand " + sub + " <jugador>", RED));
            return true;
        }
        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Jugador no conectado: " + args[1], RED));
            return true;
        }
        StandManager stands = plugin.getStandManager();
        switch (sub) {
            case "dar" -> {
                StandType type = args.length > 2 ? StandType.byKey(args[2]) : null;
                if (args.length > 2 && type == null) {
                    sender.sendMessage(Component.text("Stand desconocido: " + args[2], RED));
                    return true;
                }
                StandType given = stands.awaken(target, type);
                sender.sendMessage(Component.text(target.getName() + " ahora tiene "
                        + (given == null ? "ningún Stand" : given.displayName()) + ".", GREEN));
            }
            case "quitar" -> {
                stands.strip(target);
                sender.sendMessage(Component.text(target.getName() + " ya no tiene Stand.", GREEN));
            }
            case "vampiro" -> {
                boolean on = args.length < 3 || args[2].equalsIgnoreCase("si") || args[2].equalsIgnoreCase("sí")
                        || args[2].equalsIgnoreCase("on");
                if (on) {
                    StandData.drinkBlood(target);
                } else {
                    StandData.cure(target);
                    target.setVisualFire(false);
                }
                sender.sendMessage(Component.text(target.getName() + (on ? " ahora es vampiro y portador."
                        : " ya no es vampiro ni portador."), GREEN));
            }
            case "flecha" -> {
                if (plugin.getArrowSkeleton() == null) {
                    sender.sendMessage(Component.text("El Arquero de la Flecha está desactivado.", RED));
                    return true;
                }
                plugin.getArrowSkeleton().pierce(target);
                sender.sendMessage(Component.text("La Flecha ha atravesado a " + target.getName() + ".", GOLD));
            }
            default -> {
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
                                      @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.addAll(PLAYER_SUBS);
            if (sender.hasPermission(ADMIN)) {
                out.addAll(ADMIN_SUBS);
            }
        } else if (args.length == 2 && ADMIN_SUBS.contains(args[0].toLowerCase(Locale.ROOT))
                && sender.hasPermission(ADMIN)) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                out.add(player.getName());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("habilidad")) {
            out.addAll(List.of("1", "2"));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("dar")) {
            for (StandType type : StandType.values()) {
                out.add(type.key());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("vampiro")) {
            out.addAll(List.of("si", "no"));
        }
        String typed = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        out.removeIf(option -> !option.toLowerCase(Locale.ROOT).startsWith(typed));
        return out;
    }
}
