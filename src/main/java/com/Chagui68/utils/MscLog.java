package com.Chagui68.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The plugin's front door for the logging inside catch blocks.
 *
 * WHY IT EXISTS
 *
 * Failures used to disappear. Twenty-one catch blocks swallowed their exception
 * ({@code catch (Exception ignored) { }}), so a rejected attribute change, a failed world purge or a
 * music track that never started left no trace at all: the feature simply stopped working and nobody
 * could tell why. Every one of those blocks now reports here, which keeps the wording uniform,
 * separates the tolerated failures from the actionable ones, and — because the logger can be swapped
 * — lets a unit test assert that a failure is actually reported.
 *
 * The plugin hands its own logger over on enable so the messages keep the plugin prefix in the
 * console. The fallback is plain JUL and never touches Bukkit, so the class also works from tests
 * and from anything running before {@code onEnable}.
 */
public final class MscLog {

    private static final String FALLBACK_LOGGER = "MultiverseCreatures";

    private static Logger logger = Logger.getLogger(FALLBACK_LOGGER);

    private MscLog() {
    }

    /** Called by the plugin on enable; a null logger leaves the fallback in place. */
    public static void init(Logger pluginLogger) {
        if (pluginLogger != null) {
            logger = pluginLogger;
        }
    }

    /**
     * A tolerated failure: something that is expected to fail sometimes (a defensive API call on an
     * exotic entity, an argument the player typed wrong) and that only matters when debugging.
     */
    public static void debug(String context, Throwable error) {
        logger.log(Level.FINE, report(context, error));
    }

    /** An actionable failure: a feature that will not work as intended until someone looks at it. */
    public static void warn(String context, Throwable error) {
        logger.log(Level.WARNING, report(context, error));
    }

    /** One reported failure, as it appears in the console: what was attempted, then what went wrong. */
    static String report(String context, Throwable error) {
        return "[" + context + "] " + describe(error);
    }

    /** Names the failure even when the exception carries no message of its own. */
    static String describe(Throwable error) {
        if (error == null) return "unknown error";
        String message = error.getMessage();
        if (message == null || message.isBlank()) return error.getClass().getName();
        return error.getClass().getSimpleName() + ": " + message;
    }
}
