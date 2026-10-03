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
 * /stand summon                  summon or send back (same as sneak + F)
 * /stand ability [1|2]           use an ability (same as F, or sneak + left click)
 * /stand sha                     Killer Queen: Sheer Heart Attack
 * /stand give &lt;player&gt; [stand]  (admin) awaken a Stand, random when not named
 * /stand remove &lt;player&gt;        (admin) take the Stand away
 * /stand vampire &lt;player&gt; &lt;on|off&gt; (admin) give or cure DIO's blood
 * /stand arrow &lt;player&gt;         (admin) pierce a player with the Arrow
 * </pre>
 */
public final class StandCommand implements TabExecutor {

    private static final String ADMIN = "msc.admin";
    private static final List<String> PLAYER_SUBS = List.of("summon", "ability", "sha");
    private static final List<String> ADMIN_SUBS = List.of("give", "remove", "vampire", "arrow");

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
            sender.sendMessage(Component.text("Usage: /stand <give|remove|vampire|arrow> <player>", RED));
            return true;
        }
        switch (sub) {
            case "summon" -> stands.toggle(player);
            case "ability" -> stands.ability(player, args.length > 1 && args[1].equals("2") ? 2 : 1);
            case "sha", "sheerheartattack" -> stands.openSheerHeartAttack(player);
            default -> info(player);
        }
        return true;
    }

    private void info(Player player) {
        StandType stand = StandData.stand(player);
        player.sendMessage(MscText.title(GOLD, "✦ Stand ✦"));
        if (stand == null) {
            player.sendMessage(MscText.line(GRAY, "You have no Stand."));
            player.sendMessage(MscText.rich(GRAY, "Drink the ", DARK_RED, "Bearer's Elixir", GRAY,
                    " (DIO's blood) and let the Archer's ", GOLD, "Arrow", GRAY, " choose you."));
        } else {
            player.sendMessage(MscText.rich(GRAY, "Your Stand: ", stand.textColor(), "「" + stand.displayName() + "」"));
            player.sendMessage(MscText.rich(YELLOW, "Sneak + F: ", WHITE, "summon or send back"));
            for (Component line : StandManager.describe(stand)) {
                player.sendMessage(line);
            }
        }
        player.sendMessage(MscText.rich(GRAY, "Bearer: ", StandData.isBearer(player) ? GREEN : RED,
                StandData.isBearer(player) ? "yes" : "no", GRAY, " · Vampire: ",
                StandData.isVampire(player) ? DARK_RED : GRAY, StandData.isVampire(player) ? "yes" : "no"));
        if (StandData.isUnworthy(player)) {
            player.sendMessage(MscText.rich(DARK_RED, "Unworthy: ", GRAY,
                    "the Arrow killed you once. Drink the Bearer's Elixir to become a bearer."));
        }
    }

    private boolean admin(CommandSender sender, String sub, String[] args) {
        if (!sender.hasPermission(ADMIN)) {
            sender.sendMessage(Component.text("You do not have permission.", RED));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /stand " + sub + " <player>", RED));
            return true;
        }
        Player target = plugin.getServer().getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not online: " + args[1], RED));
            return true;
        }
        StandManager stands = plugin.getStandManager();
        switch (sub) {
            case "give" -> {
                StandType type = args.length > 2 ? StandType.byKey(args[2]) : null;
                if (args.length > 2 && type == null) {
                    sender.sendMessage(Component.text("Unknown Stand: " + args[2], RED));
                    return true;
                }
                StandType given = stands.awaken(target, type);
                sender.sendMessage(Component.text(target.getName() + " now has "
                        + (given == null ? "no Stand" : given.displayName()) + ".", GREEN));
            }
            case "remove" -> {
                stands.strip(target);
                sender.sendMessage(Component.text(target.getName() + " no longer has a Stand.", GREEN));
            }
            case "vampire" -> {
                boolean on = args.length < 3 || args[2].equalsIgnoreCase("on") || args[2].equalsIgnoreCase("yes")
                        || args[2].equalsIgnoreCase("true");
                if (on) {
                    StandData.drinkBlood(target);
                } else {
                    StandData.cure(target);
                    target.setVisualFire(false);
                }
                sender.sendMessage(Component.text(target.getName() + (on ? " is now a vampire and a Stand bearer."
                        : " is no longer a vampire nor a Stand bearer."), GREEN));
            }
            case "arrow" -> {
                if (plugin.getArrowSkeleton() == null) {
                    sender.sendMessage(Component.text("The Archer of the Arrow is disabled.", RED));
                    return true;
                }
                plugin.getArrowSkeleton().pierce(target);
                sender.sendMessage(Component.text("The Arrow has pierced " + target.getName() + ".", GOLD));
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
        } else if (args.length == 2 && args[0].equalsIgnoreCase("ability")) {
            out.addAll(List.of("1", "2"));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            for (StandType type : StandType.values()) {
                out.add(type.key());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("vampire")) {
            out.addAll(List.of("on", "off"));
        }
        String typed = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        out.removeIf(option -> !option.toLowerCase(Locale.ROOT).startsWith(typed));
        return out;
    }
}
