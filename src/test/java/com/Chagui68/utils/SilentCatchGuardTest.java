package com.Chagui68.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Forbids catch blocks that swallow their exception.
 *
 * Twenty-one of them had accumulated: a rejected attribute change, a failed world purge, a music
 * track that never started — all invisible, so the feature just stopped working and the console stayed
 * quiet. They now report through {@link MscLog}, and this guard keeps the next one from being added:
 * the body of a catch block must do something with the failure, or name its context, instead of being
 * empty.
 *
 * Comments do not count as a body — an empty catch with an explanation above it is still a failure
 * nobody can see in the console.
 */
class SilentCatchGuardTest {

    private static final Path SOURCES = Path.of("src", "main", "java");

    /**
     * Catches that are allowed to stay silent. Keep it empty: a failure worth catching but not worth
     * reporting is a failure worth reporting at {@code MscLog.debug}.
     */
    private static final List<String> ALLOWED = List.of();

    /** Sanity floor: the scan must keep finding the plugin's catch blocks instead of passing empty. */
    private static final int MINIMUM_CATCHES = 40;

    @Test
    @DisplayName("No catch block swallows its exception silently")
    void everyCatchReportsOrExplains() {
        List<String> silent = new ArrayList<>();
        int total = 0;

        for (Path file : sources()) {
            String source = withoutCommentsAndStrings(read(file));
            int index = 0;
            while ((index = source.indexOf("catch", index)) >= 0) {
                if (!isKeyword(source, index)) {
                    index += "catch".length();
                    continue;
                }
                int paren = source.indexOf('(', index);
                int open = paren < 0 ? -1 : source.indexOf('{', paren);
                int close = open < 0 ? -1 : matchingBrace(source, open);
                if (close < 0) break;

                total++;
                String body = source.substring(open + 1, close);
                if (body.isBlank()) {
                    String where = SOURCES.relativize(file) + ":" + lineOf(source, index);
                    if (!ALLOWED.contains(where)) silent.add(where);
                }
                index = close;
            }
        }

        assertTrue(total >= MINIMUM_CATCHES,
                "only " + total + " catch blocks were found; the scanner stopped matching the sources");
        assertTrue(silent.isEmpty(),
                "these catch blocks swallow their exception; report it with MscLog.debug/warn instead: " + silent);
    }

    // ------------------------------------------------------------------ helpers

    private static List<Path> sources() {
        try (Stream<Path> walk = Files.walk(SOURCES)) {
            return walk.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** {@code catch} is only a catch clause when it stands alone in front of its parentheses. */
    private static boolean isKeyword(String source, int index) {
        boolean cleanBefore = index == 0 || !Character.isJavaIdentifierPart(source.charAt(index - 1));
        int after = index + "catch".length();
        boolean cleanAfter = after >= source.length() || !Character.isJavaIdentifierPart(source.charAt(after));
        return cleanBefore && cleanAfter;
    }

    private static int matchingBrace(String source, int open) {
        int depth = 0;
        for (int i = open; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private static int lineOf(String source, int index) {
        int line = 1;
        for (int i = 0; i < index && i < source.length(); i++) {
            if (source.charAt(i) == '\n') line++;
        }
        return line;
    }

    /**
     * Blanks out comments, string literals and char literals, keeping the line count so the reported
     * line numbers still point at the real code. Without this the javadoc that documents this guard
     * would be scanned as if it were code.
     */
    private static String withoutCommentsAndStrings(String source) {
        StringBuilder out = new StringBuilder(source.length());
        int i = 0;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (c == '/' && i + 1 < source.length() && source.charAt(i + 1) == '/') {
                while (i < source.length() && source.charAt(i) != '\n') {
                    out.append(' ');
                    i++;
                }
            } else if (c == '/' && i + 1 < source.length() && source.charAt(i + 1) == '*') {
                out.append("  ");
                i += 2;
                while (i < source.length()
                        && !(source.charAt(i) == '*' && i + 1 < source.length() && source.charAt(i + 1) == '/')) {
                    out.append(source.charAt(i) == '\n' ? '\n' : ' ');
                    i++;
                }
                if (i < source.length()) {
                    out.append("  ");
                    i += 2;
                }
            } else if (c == '"' || c == '\'') {
                out.append(' ');
                i++;
                while (i < source.length() && source.charAt(i) != c) {
                    if (source.charAt(i) == '\\') {
                        out.append(' ');
                        i++;
                    }
                    if (i < source.length()) {
                        out.append(source.charAt(i) == '\n' ? '\n' : ' ');
                        i++;
                    }
                }
                if (i < source.length()) {
                    out.append(' ');
                    i++;
                }
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }
}
