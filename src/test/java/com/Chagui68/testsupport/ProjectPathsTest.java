package com.Chagui68.testsupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Self-test for the harness the source-reading guards share.
 *
 * <p>Those guards all fail open the same way: if the paths they read are wrong, a scan simply finds
 * nothing and the assertions that list offenders stay empty. This proves the shared lookup actually
 * reaches the project, so a moved working directory breaks here, with a readable message, instead of
 * quietly disarming every guard at once.
 */
class ProjectPathsTest {

    @Test
    @DisplayName("The lookup finds the project and both of the trees the guards read")
    void theLookupFindsTheProject() {
        assertTrue(Files.isRegularFile(ProjectPaths.root().resolve("pom.xml")),
                "root() must be the project, not a parent or a child: found " + ProjectPaths.root());
        assertTrue(Files.isDirectory(ProjectPaths.mainJava()), "src/main/java is missing under the root");
        assertTrue(Files.isRegularFile(ProjectPaths.resource("config.yml")),
                "the shipped config.yml must be reachable the same way the guards reach it");
        assertTrue(Files.isRegularFile(ProjectPaths.resource("plugin.yml")));
    }

    @Test
    @DisplayName("A file is named by segments and read whole")
    void filesAreNamedBySegmentsAndReadWhole() {
        String plugin = ProjectPaths.read(ProjectPaths.source("com", "Chagui68", "MultiverseCreatures.java"));
        assertTrue(plugin.contains("class MultiverseCreatures"), "the helper read the wrong file");

        assertTrue(ProjectPaths.read(ProjectPaths.resource("config.yml")).contains("armor-stand-boss"),
                "the resources tree must resolve to the files the config guard parses");
    }

    @Test
    @DisplayName("The scan reaches the whole source set, not just the folder it starts in")
    void theScanReachesTheWholeSourceSet() {
        List<Path> sources = ProjectPaths.javaFiles(ProjectPaths.mainJava());
        // The same floor the scanning guards use: far below the real count, far above an empty scan.
        assertTrue(sources.size() > 100, "only " + sources.size() + " sources were found");
        assertTrue(sources.stream().allMatch(path -> path.toString().endsWith(".java")));
        assertTrue(sources.stream().anyMatch(path -> path.toString().endsWith("MultiverseCreatures.java")));
    }

    @Test
    @DisplayName("A missing file throws instead of reading as empty")
    void aMissingFileThrows() {
        assertThrows(UncheckedIOException.class,
                () -> ProjectPaths.read(ProjectPaths.source("com", "Chagui68", "DoesNotExist.java")),
                "a guard must fail loudly when its file moves, not scan nothing");
    }
}
