package com.Chagui68.entities.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class MobHandlerRecountTest {

    @Test
    void allowsIndependentWorldAndRespectsFailureCooldown() throws Exception {
        Map<String, Long> retryAt = new HashMap<>();
        retryAt.put("world", 160_000L);

        assertFalse(MobHandler.puedeRecontar(retryAt, "world", 159_999L));
        assertTrue(MobHandler.puedeRecontar(retryAt, "world", 160_000L));
        assertTrue(MobHandler.puedeRecontar(retryAt, "world_nether", 159_999L));
    }
}
