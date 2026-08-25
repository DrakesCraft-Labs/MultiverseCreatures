package com.Chagui68.entities;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnderKnightWorldGuardTest {

    @Test
    void allowsDistanceOnlyInsideTheSameWorld() {
        UUID world = UUID.randomUUID();

        assertTrue(EnderKnight.sharesWorld(world, world));
        assertFalse(EnderKnight.sharesWorld(world, UUID.randomUUID()));
    }

    @Test
    void rejectsMissingWorldIdentity() {
        assertFalse(EnderKnight.sharesWorld(null, UUID.randomUUID()));
        assertFalse(EnderKnight.sharesWorld(UUID.randomUUID(), null));
    }
}
