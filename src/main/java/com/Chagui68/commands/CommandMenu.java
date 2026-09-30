package com.Chagui68.commands;

import com.Chagui68.entities.boss.BossDamageSample;
import com.Chagui68.entities.boss.BossId;
import com.Chagui68.entities.boss.PenetratingHit;
import com.Chagui68.utils.MscLog;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.bukkit.ChatColor.*;

/**
 * Every piece of chat output of {@code /msc}: headers, footers, the paginated sub-command menus and
 * the page arithmetic behind them.
 *
 * <p>Extracted from {@code MSCCommand} so the command stays a dispatcher, all on-screen text lives
 * in one place, and the pagination (page counts, clamping, slicing) is testable without a server.
 */
final class CommandMenu {

    /** Help lines shown per page; also the divisor used to derive page counts. */
    static final int LINES_PER_PAGE = 12;

    /** Header of a menu, e.g. {@code header("MSC SPAWN - " + subHeader)}. */
    private static final String SEPARATOR = "&8&m" + "-".repeat(40);

    private final CommandSender sender;

    CommandMenu(CommandSender sender) {
        this.sender = sender;
    }

    // ------------------------------------------------------------------ primitives

    /** Sends one raw line, translating {@code &} colour codes. */
    void line(String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    void header(String title) {
        line("");
        line(SEPARATOR);
        line(" &6&l★ &e&l" + title + " &6&l★");
        line(SEPARATOR);
    }

    void footer() {
        line(SEPARATOR);
    }

    /** A "&6&lLabel&8:" heading followed by one "   &e• &fitem" line per entry. */
    static List<String> category(String label, List<String> items) {
        List<String> lines = new ArrayList<>();
        lines.add(" &6&l" + label + "&8:");
        for (String item : items) {
            lines.add("   &e• &f" + item);
        }
        return lines;
    }

    static int clampPage(int page, int totalPages) {
        return Math.max(1, Math.min(page, totalPages));
    }

    static int pageCount(int lineCount, int perPage) {
        return Math.max(1, (int) Math.ceil(lineCount / (double) perPage));
    }

    /** The slice of {@code lines} visible on {@code page}, 1-based. */
    static <T> List<T> pageSlice(List<T> lines, int page, int perPage) {
        int totalPages = pageCount(lines.size(), perPage);
        int start = (clampPage(page, totalPages) - 1) * perPage;
        return lines.subList(start, Math.min(lines.size(), start + perPage));
    }

    /**
     * Parses an optional page argument. Returns 1 when absent, and reports invalid input instead of
     * silently jumping to page 1.
     */
    int parsePage(String[] args, int index) {
        if (args.length > index) {
            try {
                int page = Integer.parseInt(args[index]);
                if (page >= 1) return page;
            } catch (NumberFormatException e) {
                MscLog.debug("the requested page is not a number", e);
            }
            sender.sendMessage(RED + "Invalid page. Use a number >= 1.");
        }
        return 1;
    }

    /** Header, optional usage line, one page of {@code lines}, footer and the page indicator. */
    void paginated(String title, String usage, List<String> lines, int page, String navCommand) {
        int totalPages = pageCount(lines.size(), LINES_PER_PAGE);
        page = clampPage(page, totalPages);
        header(title);
        if (usage != null) {
            line(" &7Usage: &e" + usage);
            line("");
        }
        for (String line : pageSlice(lines, page, LINES_PER_PAGE)) {
            line(line);
        }
        footer();
        if (totalPages > 1) {
            String next = page < totalPages ? " &8· &7Use &e/msc " + navCommand + " help " + (page + 1) : "";
            line(" &7Page &e" + page + "&7/&e" + totalPages + next);
        }
    }

    /** Closes a fixed-size menu with its own "Next: /msc &lt;cmd&gt; help N" hint. */
    private void pageIndicator(int page, int totalPages, String navCommand) {
        String next = page < totalPages ? " &8· &7Next: &e/msc " + navCommand + " help " + (page + 1) : "";
        line(" &7Page &e" + page + "&7/&e" + totalPages + next);
    }

    // ------------------------------------------------------------------ menus

    void help() {
        header("MULTIVERSE CREATURES");
        line(" &c&l⚔ COMBAT & BOSSES&8:");
        line("   &e/msc spawn <type> &8- &7Spawn bosses, strikes & creatures.");
        line("   &e/msc attack <attack> [range] &8- &7Force a boss attack.");
        line("   &e/msc kill [type|all] [radius] &8- &7Safely purge custom mobs.");
        line("");
        line(" &6&l📦 GEAR & ARTIFACTS&8:");
        line("   &e/msc give <item> [amt] [player] &8- &7Give custom items & catalysts.");
        line("");
        line(" &d&l🌌 DIMENSIONS & RITUALS&8:");
        line("   &e/msc dimtp <world> &8- &7Teleport to multiverse dimensions.");
        line("   &e/msc cleanstands [world] &8- &7Purge plugin armor stands.");
        line("");
        line(" &b&l🎵 AUDIO & VISUALS&8:");
        line("   &e/msc music <play|stop|list|disc> &8- &7Play .nbs music or get discs.");
        line("   &e/msc seal <pattern> [plane] &8- &7Summon magic particle seals.");
        line("");
        line(" &a&l🛠 TESTING & SYSTEM&8:");
        line("   &e/msc dummy [action] &8- &7Spawn & pose test dummies.");
        line("   &e/msc debug [player] &8- &7Break down the Sentinel's last penetrating hit.");
        line("   &e/msc reload &8- &7Reload config.yml & sync entities.");
        line("");
        line(" &7&oExplore subcommands: &e/msc <cmd> help &7(e.g. &e/msc spawn help&7)");
        footer();
    }

    void spawnHelp(int page) {
        int totalPages = SpawnCatalogue.pages();
        page = clampPage(page, totalPages);
        header("MSC SPAWN - " + SpawnCatalogue.pageTitle(page));
        line(" &7Usage: &e/msc spawn <type>");
        line("");
        for (String helpLine : SpawnCatalogue.helpLines(page)) {
            line(helpLine);
        }
        footer();
        pageIndicator(page, totalPages, "spawn");
    }

    void giveHelp(int page) {
        int totalPages = GiveCatalogue.pages();
        page = clampPage(page, totalPages);
        header("MSC GIVE - " + GiveCatalogue.pageTitle(page));
        line(" &7Usage: &e/msc give <item> [amount] [player|@a|@p|@r|@s]");
        line("");
        for (String helpLine : GiveCatalogue.helpLines(page)) {
            line(" &e• " + helpLine);
        }
        footer();
        pageIndicator(page, totalPages, "give");
    }

    void attackHelp(int page) {
        int totalPages = AttackCatalogue.pages();
        page = clampPage(page, totalPages);
        header("MSC ATTACK - " + AttackCatalogue.pageTitle(page));
        line(" &7Usage: &e/msc attack <attack> [range]");
        line("");
        for (String helpLine : AttackCatalogue.helpLines(page)) {
            line(helpLine);
        }
        footer();
        pageIndicator(page, totalPages, "attack");
    }

    void sealHelp(int page) {
        paginated("MSC SEAL", "/msc seal <pattern> [plane]", SealStudio.helpLines(), page, "seal");
    }

    void dummyHelp(int page) {
        paginated("MSC DUMMY", "/msc dummy <action> [args]", DummyStudio.helpLines(), page, "dummy");
    }

    void musicHelp() {
        header("MSC MUSIC");
        line(" &7Usage: &e/msc music <play|stop|list|disc> [name] [loop]");
        line("");
        line(" &6&lActions&8:");
        line("   &e• &fplay <name> [loop]");
        line("      &7Play a song from plugins/MultiverseCreatures/music/ (.nbs)");
        line("   &e• &fstop");
        line("      &7Stop the song currently playing");
        line("   &e• &flist");
        line("      &7List all available songs");
        line("   &e• &fdisc <name>");
        line("      &7Get the jukebox music disc of a song");
        footer();
    }

    void dimtpHelp(Player player) {
        header("MSC DIMTP");
        line(" &7Usage: &e/msc dimtp <world>");
        line("");
        line(" &6&lInfo&8:");
        line("   &e• &fworld");
        line("      &7Teleport to a loaded dimension, keeping coordinates");
        line("      &7Worlds: &f" + worldList());
        footer();
    }

    void cleanStandsHelp() {
        header("MSC CLEANSTANDS");
        line(" &7Usage: &e/msc cleanstands [world]");
        line("");
        line(" &6&lInfo&8:");
        line("   &e• &f[world]");
        line("      &7Remove all custom plugin armor stands only in that dimension.");
        line("      &7Without a world, removes them from every loaded dimension.");
        line("      &7Worlds: &f" + worldList());
        footer();
    }

    // ------------------------------------------------------------------ debug

    /**
     * Detail lines of one penetrating hit, without the section bullet, so the exact figures the
     * report prints can be asserted without a live server or a sender.
     */
    static List<String> penetratingLines(PenetratingHit hit) {
        List<String> lines = new ArrayList<>();
        lines.add("      &7Event &8: &f" + decimal(hit.eventDamage())
                + " &8· &7armour &8: &f" + decimal(hit.armorCreditedBack())
                + " &8· &7protection &8: &f" + decimal(hit.protectionCreditedBack())
                + " &8· &7resistance &8: &f" + decimal(hit.resistanceCreditedBack()));
        lines.add("      &7Through armour &8: &f" + decimal(hit.throughArmor())
                + " &8· &7cap &8: &f" + decimal(hit.cap())
                + " &8· &7pierce &8: &f" + percent(hit.pierce())
                + " &8· &7Resistance &8: &f" + resistanceLabel(hit));
        lines.add("      &c&lFinal damage dealt &8: &c&l" + decimal(hit.dealt()));
        return lines;
    }

    /**
     * One generic damage sample as a bullet plus its figures, without the boss section header.
     * {@code now} is passed in so the age line is deterministic under test.
     */
    static List<String> sampleLines(BossDamageSample sample, long now) {
        List<String> lines = new ArrayList<>();
        boolean dealt = sample.direction() == BossDamageSample.Direction.DEALT_TO_PLAYER;
        lines.add("   &e▸ " + (dealt ? "&cDEALT to player" : "&aTAKEN from player")
                + " &8· &7" + sample.source() + " &8· &7" + age(sample.ageMillis(now)) + " ago");
        StringBuilder detail = new StringBuilder("      &7" + (dealt ? "Intended" : "Hit")
                + " &8: &f" + decimal(sample.before()));
        if (!sample.note().isBlank()) {
            detail.append(" &8· &7").append(sample.note());
        }
        detail.append(" &8· &7Applied &8: &f").append(decimal(sample.after()));
        lines.add(detail.toString());
        return lines;
    }

    /**
     * Full {@code /msc debug} report: the target, then one section per boss listing the damage it
     * dealt to that player and the damage it took from them. Sections with nothing recorded are
     * skipped, and the detail lines can be asserted headlessly through the two static helpers above.
     */
    void debugReport(String targetName, PenetratingHit sentinelDealt, List<BossDamageSample> samples,
                     boolean penetratingEnabled) {
        header("MSC DEBUG - BOSS DAMAGE");
        line(" &7Target: &f" + targetName);
        long now = System.currentTimeMillis();

        for (BossId boss : BossId.values()) {
            List<String> section = new ArrayList<>();
            if (boss == BossId.SENTINEL && sentinelDealt != null) {
                section.add("   &e▸ &cDEALT to player &8· &7penetrating &8· &7"
                        + age(sentinelDealt.ageMillis(now)) + " ago");
                section.addAll(penetratingLines(sentinelDealt));
            }
            for (BossDamageSample sample : samples) {
                if (sample.boss() == boss) section.addAll(sampleLines(sample, now));
            }
            if (section.isEmpty()) continue;
            line("");
            line(" &6&l" + boss.displayName() + "&8:");
            for (String sectionLine : section) {
                line(sectionLine);
            }
        }

        if (!penetratingEnabled) {
            line("");
            line(" &cNote&8: &7penetrating damage is currently disabled in config.yml.");
        }
        footer();
    }

    void debugHelp() {
        header("MSC DEBUG - BOSS DAMAGE");
        line(" &7Usage: &e/msc debug [player]");
        line("");
        line(" &6&lInfo&8:");
        line("   &e• &fShows what the Obsidian Sentinel, Nix and Jack Star did to a");
        line("      &fplayer and what they took back, per boss.");
        line("   &e• &fTargets the player you are looking at, or a named player.");
        line("   &e• &fDEALT lists the attack, the intended damage and what the player");
        line("      &freally took; TAKEN lists the hit, the boss's cap or load-balancer");
        line("      &fsplit and the damage the boss applied.");
        footer();
    }

    private static String resistanceLabel(PenetratingHit hit) {
        return hit.resistanceLevel() == 0
                ? "none"
                : "level " + hit.resistanceLevel();
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    /** Clamped to [0, 1] so an out-of-range config value reads as the pierce the maths uses. */
    private static String percent(double fraction) {
        double clamped = Math.max(0.0, Math.min(1.0, fraction));
        return String.format(Locale.ROOT, "%.0f%%", clamped * 100.0);
    }

    /** Human-readable age of a hit, from milliseconds up to minutes. */
    private static String age(long millis) {
        if (millis < 1000) return millis + "ms";
        double seconds = millis / 1000.0;
        if (seconds < 60.0) return String.format(Locale.ROOT, "%.1fs", seconds);
        return String.format(Locale.ROOT, "%.1fmin", seconds / 60.0);
    }

    void killHelp() {
        line(GOLD + "Usage: " + YELLOW + "/msc kill [all|<mob_type>] [radius]");
        line(GRAY + "Examples:");
        line(GRAY + "  /msc kill               - Remove all nearby/world MSC mobs");
        line(GRAY + "  /msc kill mahoraga      - Remove only Mahoraga");
        line(GRAY + "  /msc kill all 50        - Remove all MSC mobs within 50 blocks");
        line(GRAY + "  /msc kill garou 100     - Remove Garou within 100 blocks");
    }

    private static String worldList() {
        StringBuilder worlds = new StringBuilder();
        for (World world : Bukkit.getWorlds()) {
            if (worlds.length() > 0) worlds.append("&8, &f");
            worlds.append(world.getName());
        }
        return worlds.toString();
    }
}
