package com.Chagui68.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the suit every dressed boss wears. Two things have to hold: an adoption must recognise a
 * piece only when all three tags agree (a piece claimed by another owner would leave two overlapping
 * bodies), and the code that builds and configures a piece must stay in this one class, because the
 * three bosses had already drifted apart when each carried its own copy.
 */
class DisplaySuitTest {

    @Test
    @DisplayName("A piece is recognised only when its suit, its name and its owner all agree")
    void allThreeTagsHaveToAgree() {
        DisplaySuit.SuitTags tags = new DisplaySuit.SuitTags("suit", "piece_a", "owner_x");

        assertTrue(tags.matches(display(Set.of("suit", "piece_a", "owner_x", "other"))));
        assertFalse(tags.matches(display(Set.of("piece_a", "owner_x"))), "the suit tag is missing");
        assertFalse(tags.matches(display(Set.of("suit", "owner_x"))), "the piece tag is missing");
        assertFalse(tags.matches(display(Set.of("suit", "piece_a"))), "the owner tag is missing");
        assertFalse(tags.matches(display(Set.of("suit", "piece_b", "owner_x"))), "another piece of the same boss");
        assertFalse(tags.matches(display(Set.of("suit", "piece_a", "owner_y"))), "the same piece of another boss");
        assertFalse(tags.matches(display(Set.of())));
    }

    @Test
    @DisplayName("Looking for a piece takes the one that matches and ignores the rest")
    void theSearchTakesTheMatchingPiece() {
        DisplaySuit.SuitTags tags = new DisplaySuit.SuitTags("suit", "piece_a", "owner_x");
        Location center = new Location(null, 0, 0, 0);

        Entity foreignOwner = display(Set.of("suit", "piece_a", "owner_y"));
        Entity wrongPiece = display(Set.of("suit", "piece_b", "owner_x"));
        Entity notADisplay = entity(Set.of("suit", "piece_a", "owner_x"));
        Entity wanted = display(Set.of("suit", "piece_a", "owner_x"));

        assertSame(wanted, DisplaySuit.find(world(List.of(foreignOwner, wrongPiece, notADisplay, wanted)), center, tags));
        assertNull(DisplaySuit.find(world(List.of(foreignOwner, wrongPiece)), center, tags),
                "a piece of another boss or another piece of this suit must not be adopted");
        assertNull(DisplaySuit.find(world(List.of()), center, tags), "nothing to adopt yet");
        assertNull(DisplaySuit.find(null, center, tags));
        assertNull(DisplaySuit.find(world(List.of(wanted)), null, tags));
    }

    @Test
    @DisplayName("The suit is built in one place, not copied into every dressed boss")
    void theSuitIsNotCopiedPerBoss() throws IOException {
        for (String boss : List.of("entities/Kinger.java", "entities/boss/NixBoss.java", "entities/boss/JackStarBoss.java")) {
            String source = Files.readString(Path.of("src", "main", "java", "com", "Chagui68", boss));
            assertTrue(source.contains("DisplaySuit."), boss + " must wear the shared suit");
            assertFalse(source.contains("Base64.getDecoder()"),
                    boss + " must not build its own head from the skin texture");
            assertFalse(source.contains("setTeleportDuration"),
                    boss + " must not configure its pieces by hand; that is DisplaySuit's job");
            assertTrue(source.contains("DisplaySuit.find("),
                    boss + " must adopt an orphaned piece instead of spawning a second one");
            assertTrue(source.contains("DisplaySuit.remove("),
                    boss + " must take its suit off the world when it dies");
        }
    }

    // --- fakes -------------------------------------------------------------------------------------

    private static World world(List<Entity> nearby) {
        return (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[]{World.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getNearbyEntities" -> nearby;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "world";
                    default -> null;
                });
    }

    private static Entity entity(Set<String> tags) {
        return (Entity) Proxy.newProxyInstance(Entity.class.getClassLoader(), new Class<?>[]{Entity.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getScoreboardTags" -> tags;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "entity";
                    default -> null;
                });
    }

    private static ItemDisplay display(Set<String> tags) {
        return (ItemDisplay) Proxy.newProxyInstance(ItemDisplay.class.getClassLoader(), new Class<?>[]{ItemDisplay.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getScoreboardTags" -> tags;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "display";
                    default -> null;
                });
    }
}
