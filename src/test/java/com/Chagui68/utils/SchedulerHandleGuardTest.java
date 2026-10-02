package com.Chagui68.utils;

import com.Chagui68.testsupport.ProjectPaths;
import com.Chagui68.testsupport.SourceText;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every repeating task in the plugin has to be one of two things: a runnable that cancels itself, or
 * a task whose handle something still holds and can cancel.
 *
 * <p>The third kind — a bare {@code .runTaskTimer(...)} with no handle — runs until the server
 * stops. Twenty-four of them were live when this guard was written: a tick loop per custom mob, the
 * two slime tickers, one per item handler that keeps a player aura alive, and the population
 * recount. The shape of the bug is always the same: a permanent ticker with no handle cannot be
 * stopped by whoever starts it a second time, so a reload or a second {@code startTicker()} call
 * leaves two loops walking the same state. Bukkit does cancel a plugin's tasks when the plugin is
 * disabled, so the leak is not "it survives forever": it is that nobody inside the plugin can end
 * it, which is exactly what every other part of this codebase does (boss instances cancel their
 * attack tasks when the fight ends, the music manager cancels on {@code stopAll}).
 *
 * <p>Both counts are asserted non-zero: a scanner that mistook every site for one of the two shapes
 * would otherwise pass without checking anything.
 */
class SchedulerHandleGuardTest {

    private static final Path SOURCES = ProjectPaths.mainJava();

    /** The two ways this codebase schedules a repeating task. */
    private static final Pattern SCHEDULE = Pattern.compile(
            "\\.\\s*(?:runTaskTimer|scheduleSyncRepeatingTask)\\s*\\(");

    /** The anonymous class a call hangs off, under either spelling of the class name. */
    private static final Pattern RUNNABLE_NEW = Pattern.compile(
            "new\\s+(?:org\\.bukkit\\.scheduler\\.)?BukkitRunnable\\s*\\(\\s*\\)");

    /** A name that can carry a handle: {@code task}, {@code instance.flyTask}. */
    private static final Pattern NAME = Pattern.compile(
            "[A-Za-z_$][A-Za-z0-9_$]*(?:\\s*\\.\\s*[A-Za-z_$][A-Za-z0-9_$]*)*");

    /** A {@code cancel(} that ends the runnable itself, not one it happens to hold. */
    private static final Pattern SELF_CANCEL = Pattern.compile("(?<![\\w.])cancel\\s*\\(");

    /**
     * Floor for the site count: the scan has to see the whole project, not a corner of it. The
     * Sentinel's attacks all play through one choreography runner now, so the project holds about
     * sixty repeating tasks instead of the hundred-odd it had when every attack scheduled its own.
     */
    private static final int MIN_SITES = 45;

    /** Floor for the handle count, so a scanner that calls everything self-cancelling fails. */
    private static final int MIN_OWNED_TASKS = 10;

    @Test
    @DisplayName("Every repeating task cancels itself, or keeps a handle the plugin can cancel")
    void everyRepeatingTaskIsOwned() {
        int sites = 0;
        int selfCancelling = 0;
        int owned = 0;
        List<String> unowned = new ArrayList<>();

        for (Path file : ProjectPaths.javaFiles(SOURCES)) {
            String relative = ProjectPaths.relative(file);
            for (Site site : sitesIn(relative, SourceText.codeOnly(ProjectPaths.read(file)))) {
                sites++;
                if (site.selfCancelling()) {
                    selfCancelling++;
                    continue;
                }
                if (site.handle() != null) {
                    owned++;
                    continue;
                }
                unowned.add(site.file() + ":" + site.line() + "  " + site.text());
            }
        }

        assertTrue(sites >= MIN_SITES,
                "the scan found only " + sites + " repeating tasks, so it is not looking at the whole project");
        assertTrue(selfCancelling > 0 && owned > 0,
                "the scan classified nothing as self-cancelling (" + selfCancelling + ") or as keeping a handle ("
                        + owned + "), so it is not telling the two shapes apart");
        assertTrue(unowned.isEmpty(),
                "These repeating tasks neither cancel themselves nor keep a handle, so nothing inside the "
                        + "plugin can stop them:\n  " + String.join("\n  ", unowned));
    }

    @Test
    @DisplayName("Every kept handle has a cancel path")
    void everyKeptHandleCanBeCancelled() {
        Map<String, String> sources = new LinkedHashMap<>();
        for (Path file : ProjectPaths.javaFiles(SOURCES)) {
            sources.put(ProjectPaths.relative(file), SourceText.codeOnly(ProjectPaths.read(file)));
        }
        String project = String.join("\n", sources.values());

        int checked = 0;
        List<String> decoration = new ArrayList<>();
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            for (Site site : sitesIn(entry.getKey(), entry.getValue())) {
                if (site.selfCancelling() || site.handle() == null) continue;
                checked++;
                if (canBeCancelled(entry.getValue(), project, site.handle())) continue;
                decoration.add(site.file() + ":" + site.line() + " keeps " + site.handle()
                        + ", which no cancel() reaches");
            }
        }

        assertTrue(checked >= MIN_OWNED_TASKS,
                "the scan only found " + checked + " handles that outlive their own runnable, so this is not "
                        + "checking the project");
        assertTrue(decoration.isEmpty(),
                "These files keep a task handle whose task is never cancelled, so the handle is "
                        + "decoration:\n  " + String.join("\n  ", decoration));
    }

    @Test
    @DisplayName("The plugin stops its long-lived tasks when it is disabled")
    void thePluginStopsItsLongLivedTasks() {
        String plugin = SourceText.codeOnly(
                ProjectPaths.read(ProjectPaths.source("com", "Chagui68", "MultiverseCreatures.java")));
        String onDisable = SourceText.methodBody(plugin, "public void onDisable(");

        // The long-lived subsystems and the dimension the plugin itself created: onDisable is the
        // only place that can end them, since nothing else outlives a fight.
        for (String stop : List.of("stopAll()", "unloadBossDimension()")) {
            assertTrue(onDisable.contains(stop),
                    "onDisable no longer calls " + stop + ", so that subsystem keeps ticking after the plugin "
                            + "is disabled");
        }
        assertTrue(onDisable.contains("stopTasks") || onDisable.contains("stopAll"),
                "onDisable must stop the tickers that outlive a fight");
    }

    // ------------------------------------------------------------------ scanning

    /** One schedule call, with the runnable body it starts and the handle that survives it. */
    private record Site(String file, int line, String text, String handle, String body) {
        boolean selfCancelling() {
            return body != null && SELF_CANCEL.matcher(body).find();
        }
    }

    private static List<Site> sitesIn(String file, String code) {
        List<Site> sites = new ArrayList<>();
        Matcher schedule = SCHEDULE.matcher(code);
        while (schedule.find()) {
            int call = schedule.start();
            sites.add(new Site(file, lineOf(code, call), textOf(code, call),
                    handleOf(code, call), bodyOf(code, call)));
        }
        return sites;
    }

    /**
     * The runnable a schedule call starts, as source text, or null when the call does not start a
     * runnable object at all (a lambda or a method reference, whose handle can only be the task the
     * scheduler returns).
     *
     * <p>Two shapes: the call hangs off the anonymous class it schedules
     * ({@code new BukkitRunnable() { … }.runTaskTimer(…)}), or it is made on a name that was
     * assigned a runnable earlier in the file, including a field of another object
     * ({@code instance.flyTask.runTaskTimer(…)}). Both are matched by their braces, so a runnable
     * with nested one-shot tasks inside is followed to its own closing brace.
     */
    private static String bodyOf(String code, int call) {
        int before = previousNonSpace(code, call);
        if (before >= 0 && code.charAt(before) == '}') {
            int open = matchingOpen(code, before);
            return open >= 0 && opensRunnable(code, open) ? code.substring(open + 1, before) : null;
        }
        String name = lastNameOf(trailingName(statementBefore(code, call)));
        if (name == null) return null;
        int after = lastDeclarationEnd(code, call, name);
        if (after < 0) return null;
        int open = code.indexOf('{', after);
        if (open < 0) return null;
        int close = matchingClose(code, open);
        return close < 0 ? null : code.substring(open + 1, close);
    }

    /**
     * The name that still holds the runnable after the statement ends, or null when the call
     * schedules something anonymous that the statement does not assign.
     */
    private static String handleOf(String code, int call) {
        int before = previousNonSpace(code, call);
        if (before >= 0 && code.charAt(before) == '}') {
            int open = matchingOpen(code, before);
            if (open < 0 || !opensRunnable(code, open)) return null;
            return assignedNameBefore(code, lastRunnableStartBefore(code, open));
        }
        String statement = statementBefore(code, call);
        String chain = trailingName(statement);
        if (chain != null) return lastNameOf(chain);
        return assignedNameBefore(code, call);
    }

    /**
     * Whether the project cancels the task a handle keeps.
     *
     * <p>Three paths, all text-level: the name is cancelled directly ({@code task.cancel()},
     * {@code instance.defenseTask.cancel()}), the method returns the handle to its caller, or the
     * handle is handed to another name that is cancelled in turn ({@code instance.aiTask = ai;},
     * then {@code instance.aiTask.cancel()}).
     */
    private static boolean canBeCancelled(String file, String project, String handle) {
        String name = lastNameOf(handle);
        if (name == null) return false;
        if (Pattern.compile("\\b" + Pattern.quote(name) + "\\s*\\.\\s*cancel\\s*\\(").matcher(project).find()) {
            return true;
        }
        if (Pattern.compile("\\breturn\\s+" + Pattern.quote(name) + "\\s*;").matcher(file).find()) {
            return true;
        }
        Matcher handoff = Pattern.compile(
                "([A-Za-z_$][A-Za-z0-9_$]*(?:\\s*\\.\\s*[A-Za-z_$][A-Za-z0-9_$]*)*)\\s*=\\s*"
                        + Pattern.quote(name) + "\\s*;").matcher(file);
        while (handoff.find()) {
            String owner = lastNameOf(handoff.group(1));
            if (owner != null && Pattern.compile("\\b" + Pattern.quote(owner) + "\\s*\\.\\s*cancel\\s*\\(")
                    .matcher(project).find()) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ text plumbing

    private static int lineOf(String code, int index) {
        int line = 1;
        for (int position = 0; position < index; position++) {
            if (code.charAt(position) == '\n') line++;
        }
        return line;
    }

    private static String textOf(String code, int index) {
        int start = code.lastIndexOf('\n', index) + 1;
        int end = code.indexOf('\n', index);
        return code.substring(start, end < 0 ? code.length() : end).trim();
    }

    private static int previousNonSpace(String code, int index) {
        int position = index - 1;
        while (position >= 0 && Character.isWhitespace(code.charAt(position))) position--;
        return position;
    }

    /** The expression before {@code index}, cut at the statement boundary the receiver starts from. */
    private static String statementBefore(String code, int index) {
        int start = index - 1;
        while (start >= 0 && ";{}".indexOf(code.charAt(start)) < 0) start--;
        return code.substring(start + 1, index).stripTrailing();
    }

    /** The `{` that opens the block closed by the `}` at {@code close}. */
    private static int matchingOpen(String code, int close) {
        int depth = 0;
        for (int index = close; index >= 0; index--) {
            char character = code.charAt(index);
            if (character == '}') depth++;
            if (character == '{') {
                depth--;
                if (depth == 0) return index;
            }
        }
        return -1;
    }

    private static int matchingClose(String code, int open) {
        int depth = 0;
        for (int index = open; index < code.length(); index++) {
            char character = code.charAt(index);
            if (character == '{') depth++;
            if (character == '}') {
                depth--;
                if (depth == 0) return index;
            }
        }
        return -1;
    }

    private static int lastRunnableStartBefore(String code, int open) {
        Matcher matcher = RUNNABLE_NEW.matcher(code);
        int start = -1;
        while (matcher.find() && matcher.end() <= open) start = matcher.start();
        return start;
    }

    /** Whether the `{` at {@code open} is the body of an anonymous runnable. */
    private static boolean opensRunnable(String code, int open) {
        Matcher matcher = RUNNABLE_NEW.matcher(code);
        int end = -1;
        while (matcher.find() && matcher.end() <= open) end = matcher.end();
        return end >= 0 && code.substring(end, open).isBlank();
    }

    /** The name an assignment puts the runnable under, for `name = new …` declarations. */
    private static String assignedNameBefore(String code, int expression) {
        if (expression < 0) return null;
        String statement = statementBefore(code, expression);
        int equals = assignmentIndex(statement);
        return equals < 0 ? null : lastNameOf(statement.substring(0, equals));
    }

    /** The identifier chain a statement ends with, or null when it is not a plain name. */
    private static String trailingName(String statement) {
        Matcher matcher = Pattern.compile(NAME.pattern() + "$").matcher(statement);
        if (!matcher.find()) return null;
        int start = matcher.start();
        if (start > 0 && ")]\"'".indexOf(statement.charAt(start - 1)) >= 0) return null;
        return matcher.group();
    }

    /** The last name in a chunk of text ({@code instance.flyTask} -> {@code flyTask}). */
    private static String lastNameOf(String text) {
        if (text == null) return null;
        Matcher matcher = NAME.matcher(text);
        String last = null;
        while (matcher.find()) last = matcher.group();
        if (last == null) return null;
        String compact = last.replace(" ", "");
        int dot = compact.lastIndexOf('.');
        return dot < 0 ? compact : compact.substring(dot + 1);
    }

    /** The first plain `=` of a statement, skipping comparisons and compound operators. */
    private static int assignmentIndex(String statement) {
        for (int index = 0; index < statement.length(); index++) {
            if (statement.charAt(index) != '=') continue;
            char before = index > 0 ? statement.charAt(index - 1) : ' ';
            char after = index + 1 < statement.length() ? statement.charAt(index + 1) : ' ';
            if (before == '=' || after == '=' || "!<>+-*/%".indexOf(before) >= 0) continue;
            return index;
        }
        return -1;
    }

    /** The last `name = new …BukkitRunnable()` declared before the call, as its end offset. */
    private static int lastDeclarationEnd(String code, int call, String name) {
        Matcher declaration = Pattern.compile(
                "\\b" + Pattern.quote(name) + "\\s*=\\s*" + RUNNABLE_NEW.pattern())
                .matcher(code.substring(0, call));
        int after = -1;
        while (declaration.find()) after = declaration.end();
        return after;
    }
}
