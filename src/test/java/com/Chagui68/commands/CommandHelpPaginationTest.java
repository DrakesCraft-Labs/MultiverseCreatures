package com.Chagui68.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pagination of the help menus.
 *
 * This used to re-implement the clamping and the page titles locally, so it proved the test's own
 * arithmetic rather than the command's. It now drives the real {@link CommandMenu} helpers and the
 * real catalogue titles, which means moving a menu to a different page cannot slip through.
 */
class CommandHelpPaginationTest {

    @ParameterizedTest(name = "Input page {0} with total {1} clamps to {2}")
    @CsvSource({
            "-5, 3, 1",
            "0,  3, 1",
            "1,  3, 1",
            "2,  3, 2",
            "3,  3, 3",
            "4,  3, 3",
            "99, 3, 3",
            "-1, 4, 1",
            "1,  4, 1",
            "4,  4, 4",
            "5,  4, 4"
    })
    @DisplayName("Page numbers are clamped strictly between 1 and the page count")
    void testPaginationClamping(int inputPage, int totalPages, int expectedPage) {
        assertEquals(expectedPage, CommandMenu.clampPage(inputPage, totalPages));
    }

    @ParameterizedTest(name = "{0} lines need {1} pages of {2}")
    @CsvSource({
            "0,   1, 12",
            "1,   1, 12",
            "12,  1, 12",
            "13,  2, 12",
            "24,  2, 12",
            "25,  3, 12",
            "144, 12, 12",
            "19,  1, 19"
    })
    @DisplayName("Page count always covers every line, and never drops below one page")
    void testPageCount(int lineCount, int expectedPages, int perPage) {
        assertEquals(expectedPages, CommandMenu.pageCount(lineCount, perPage));
    }

    @Test
    @DisplayName("A page slice shows exactly its own window and stays inside the list")
    void testPageSlices() {
        List<String> lines = IntStream.range(0, 30).mapToObj(i -> "line-" + i).toList();

        assertEquals(List.of("line-0", "line-11"), List.of(CommandMenu.pageSlice(lines, 1, 12).get(0),
                CommandMenu.pageSlice(lines, 1, 12).get(11)));
        assertEquals(12, CommandMenu.pageSlice(lines, 1, 12).size());
        assertEquals(12, CommandMenu.pageSlice(lines, 2, 12).size());
        assertEquals(List.of("line-24", "line-25", "line-26", "line-27", "line-28", "line-29"),
                CommandMenu.pageSlice(lines, 3, 12));
        assertEquals(CommandMenu.pageSlice(lines, 3, 12), CommandMenu.pageSlice(lines, 99, 12),
                "pages past the end must clamp to the last page");
        assertEquals(CommandMenu.pageSlice(lines, 1, 12), CommandMenu.pageSlice(lines, -3, 12),
                "pages before the start must clamp to the first page");
    }

    @Test
    @DisplayName("Page count and slicing agree for every page of every menu")
    void testSlicesCoverEverythingWithoutOverlap() {
        List<String> lines = IntStream.range(0, 37).mapToObj(i -> "l" + i).toList();
        int pages = CommandMenu.pageCount(lines.size(), CommandMenu.LINES_PER_PAGE);
        List<String> seen = new ArrayList<>();
        for (int page = 1; page <= pages; page++) {
            List<String> slice = CommandMenu.pageSlice(lines, page, CommandMenu.LINES_PER_PAGE);
            assertFalse(slice.isEmpty(), "page " + page + " is empty");
            assertTrue(slice.size() <= CommandMenu.LINES_PER_PAGE);
            assertTrue(seen.stream().noneMatch(slice::contains), "page " + page + " repeats a line");
            seen.addAll(slice);
        }
        assertEquals(lines, seen);
    }

    @Test
    @DisplayName("Category prefixes keep the exact legacy formatting")
    void testCategoryFormatting() {
        List<String> lines = CommandMenu.category("Patterns", List.of("pentagram", "storm"));
        assertEquals(List.of(" &6&lPatterns&8:", "   &e• &fpentagram", "   &e• &fstorm"), lines);
    }

    @Test
    @DisplayName("Every menu advertises the pages the command actually serves")
    void testSubheadersExistForAllValidPages() {
        for (int page = 1; page <= SpawnCatalogue.pages(); page++) {
            assertFalse(SpawnCatalogue.pageTitle(page).isBlank());
            assertFalse(SpawnCatalogue.helpLines(page).isEmpty(), "spawn page " + page + " has no entries");
        }
        for (int page = 1; page <= GiveCatalogue.pages(); page++) {
            assertFalse(GiveCatalogue.pageTitle(page).isBlank());
            assertFalse(GiveCatalogue.helpLines(page).isEmpty(), "give page " + page + " has no entries");
        }
        for (int page = 1; page <= AttackCatalogue.pages(); page++) {
            assertFalse(AttackCatalogue.pageTitle(page).isBlank());
            assertFalse(AttackCatalogue.helpLines(page).isEmpty(), "attack page " + page + " has no entries");
        }
    }

    @Test
    @DisplayName("Sub-menus that paginate themselves fit their content in the fixed page size")
    void testSelfPaginatedMenus() {
        assertEquals(2, CommandMenu.pageCount(DummyStudio.helpLines().size(), CommandMenu.LINES_PER_PAGE),
                "the dummy help is two pages long");
        assertEquals(2, CommandMenu.pageCount(SealStudio.helpLines().size(), CommandMenu.LINES_PER_PAGE),
                "the seal help is two pages long");
    }
}
