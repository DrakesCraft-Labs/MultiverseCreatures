package com.Chagui68.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the reporting contract of {@link MscLog}.
 *
 * The twenty-one catch blocks that used to swallow their exception now report through this class, so
 * what matters is that a failure always leaves a line with enough information to act on: what was
 * attempted, which exception, and its message — or its class name when the exception carries no
 * message, which is the case that used to look like a clean run.
 */
class MscLogTest {

    /** Captures what the logger was asked to print, so the message can be inspected. */
    private static final class Recorder extends Handler {
        final List<LogRecord> records = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            records.add(record);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        LogRecord only() {
            assertEquals(1, records.size(), "expected exactly one reported failure");
            return records.get(0);
        }
    }

    private final Recorder recorder = new Recorder();

    private void capture() {
        Logger logger = Logger.getLogger("MscLogTest");
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        logger.addHandler(recorder);
        MscLog.init(logger);
    }

    @AfterEach
    void restoreFallback() {
        MscLog.init(Logger.getLogger("MultiverseCreatures"));
    }

    @Test
    @DisplayName("A tolerated failure is logged at FINE with the context and the exception")
    void debugReportsAtFine() {
        capture();
        MscLog.debug("the radius argument is not a number", new NumberFormatException("For input string: \"abc\""));

        LogRecord record = recorder.only();
        assertEquals(Level.FINE, record.getLevel());
        assertTrue(record.getMessage().contains("the radius argument is not a number"), record.getMessage());
        assertTrue(record.getMessage().contains("NumberFormatException"), record.getMessage());
        assertTrue(record.getMessage().contains("abc"), "the offending input should survive: " + record.getMessage());
    }

    @Test
    @DisplayName("An actionable failure is logged at WARNING")
    void warnReportsAtWarning() {
        capture();
        MscLog.warn("could not start the boss music for Steve", new IllegalStateException("no track loaded"));

        LogRecord record = recorder.only();
        assertEquals(Level.WARNING, record.getLevel());
        assertTrue(record.getMessage().contains("could not start the boss music for Steve"), record.getMessage());
        assertTrue(record.getMessage().contains("IllegalStateException: no track loaded"), record.getMessage());
    }

    @Test
    @DisplayName("An exception without a message is still named, and a null one does not blow up")
    void describeSurvivesMissingDetails() {
        assertEquals("java.lang.RuntimeException", MscLog.describe(new RuntimeException()));
        assertEquals("IllegalStateException: boom", MscLog.describe(new IllegalStateException("boom")));
        assertEquals("unknown error", MscLog.describe(null));

        capture();
        MscLog.warn("could not read the bundled music folder", new IllegalStateException());
        MscLog.warn("could not purge the world", null);

        assertEquals(2, recorder.records.size());
        assertTrue(recorder.records.get(0).getMessage().contains("java.lang.IllegalStateException"),
                recorder.records.get(0).getMessage());
        assertTrue(recorder.records.get(1).getMessage().contains("unknown error"),
                recorder.records.get(1).getMessage());
    }

    @Test
    @DisplayName("A null logger keeps the previous one, so startup order cannot silence the plugin")
    void nullLoggerIsIgnored() {
        capture();
        MscLog.init(null);
        MscLog.warn("could not finish purging MSC creatures", new IllegalStateException("halted"));

        assertEquals(1, recorder.records.size(), "the recorder is still the active logger");
        assertTrue(recorder.records.get(0).getMessage().contains("halted"));
    }
}
