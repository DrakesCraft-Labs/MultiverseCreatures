package com.Chagui68.testsupport;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * Locates the project from a test.
 *
 * <p>Every source-reading guard needs the same two things: the project's own files, and a way to
 * name one in a failure message. Each of them used to build {@code Path.of("src", "main", ...)}
 * itself, which quietly assumed Maven was started in the project root — start the suite from
 * anywhere else and every one of those scans finds nothing. Some of them then passed vacuously;
 * the rest failed with a message about the working directory instead of about the code.
 *
 * <p>This walks up from the working directory to the first folder that looks like the project, so
 * the paths are right wherever the suite is started, and the assumption lives in one place. Every
 * accessor is a pure path operation with one failure mode: a file that cannot be read throws rather
 * than returning something a guard could mistake for an empty file.
 */
public final class ProjectPaths {

    private static final Path ROOT = locateRoot();

    private ProjectPaths() {
    }

    /** The project root: the first ancestor of the working directory that has {@code src/main/java}. */
    public static Path root() {
        return ROOT;
    }

    /** The main source set, the tree the source-reading guards scan. */
    public static Path mainJava() {
        return ROOT.resolve("src").resolve("main").resolve("java");
    }

    /** The resources folder: {@code config.yml} and {@code plugin.yml} live here. */
    public static Path mainResources() {
        return ROOT.resolve("src").resolve("main").resolve("resources");
    }

    /**
     * A file under {@code src/main/java}, named by path segments so no separator is hardcoded:
     * {@code source("com", "Chagui68", "entities", "Kinger.java")}.
     */
    public static Path source(String... parts) {
        return resolve(mainJava(), parts);
    }

    /** A file under {@code src/main/resources}, named by path segments. */
    public static Path resource(String... parts) {
        return resolve(mainResources(), parts);
    }

    /** The whole text of a file; a read error is a test failure, never an empty string. */
    public static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + path, e);
        }
    }

    /** Every {@code .java} file under a folder, sorted so failure messages are stable. */
    public static List<Path> javaFiles(Path folder) {
        try (Stream<Path> walk = Files.walk(folder)) {
            return walk.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not scan " + folder, e);
        }
    }

    /** A path as the guards report it: relative to the project when it is inside it. */
    public static String relative(Path path) {
        Path absolute = path.toAbsolutePath();
        return absolute.startsWith(ROOT) ? ROOT.relativize(absolute).toString() : absolute.toString();
    }

    private static Path resolve(Path base, String... parts) {
        Path path = base;
        for (String part : parts) {
            path = path.resolve(part);
        }
        return path;
    }

    private static Path locateRoot() {
        Path working = Path.of("").toAbsolutePath();
        for (Path candidate = working; candidate != null; candidate = candidate.getParent()) {
            if (Files.isDirectory(candidate.resolve("src").resolve("main").resolve("java"))) {
                return candidate;
            }
        }
        // Nothing looked like the project. Keep the working directory: the guards report the paths
        // they could not read, which points at the real problem instead of a wrong root.
        return working;
    }
}
