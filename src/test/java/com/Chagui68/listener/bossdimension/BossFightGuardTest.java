package com.Chagui68.listener.bossdimension;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BossFightGuardTest {

    @ParameterizedTest(name = "\"{0}\" is allowed")
    @ValueSource(strings = {"/say hi", "/me waves", "/help", "/?", "/dimtp", "/HELP 2", "/minecraft:say hi", "  /me  "})
    @DisplayName("The documented commands still run mid-fight, in any case and namespace")
    void allowedCommands(String message) {
        assertTrue(BossFightGuard.isAllowedCommand(message));
    }

    @ParameterizedTest(name = "\"{0}\" is blocked")
    @ValueSource(strings = {"/menu", "/meteor", "/sayhi", "/helpop", "/home", "/spawn", "/tp Steve", "/msg a b"})
    @DisplayName("A command that merely starts like an allowed one is blocked")
    void blockedCommands(String message) {
        assertFalse(BossFightGuard.isAllowedCommand(message));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("No command is not an allowed command")
    void emptyIsBlocked(String message) {
        assertFalse(BossFightGuard.isAllowedCommand(message));
    }
}
