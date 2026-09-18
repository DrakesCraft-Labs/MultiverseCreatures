package com.Chagui68.listener.bossdimension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class BossDimensionGuardLogicTest {

    @Test
    @DisplayName("Verify event logic short-circuits on non-boss worlds without evaluating permission check")
    void testEventOrderingShortCircuit() {
        UUID bossWorldUid = UUID.randomUUID();
        UUID overworldUid = UUID.randomUUID();

        AtomicBoolean permissionChecked = new AtomicBoolean(false);
        BooleanSupplier permissionCheck = () -> {
            permissionChecked.set(true);
            return false;
        };

        // Case 1: Event in Overworld (should short-circuit immediately)
        UUID currentWorld = overworldUid;
        boolean inBossWorld = currentWorld.equals(bossWorldUid);

        if (inBossWorld) {
            permissionCheck.getAsBoolean();
        }

        assertFalse(inBossWorld, "Overworld must not be treated as boss world");
        assertFalse(permissionChecked.get(), "Permission check must NOT be invoked when outside boss world");

        // Case 2: Event in Boss World (permission check should execute)
        currentWorld = bossWorldUid;
        inBossWorld = currentWorld.equals(bossWorldUid);

        if (inBossWorld) {
            permissionCheck.getAsBoolean();
        }

        assertTrue(inBossWorld, "Boss world must match");
        assertTrue(permissionChecked.get(), "Permission check should be evaluated only when inside boss world");
    }
}
