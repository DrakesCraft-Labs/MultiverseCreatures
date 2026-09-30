package com.Chagui68.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the /msc permission rule.
 *
 * plugin.yml declared {@code msc.admin} and config.yml exposed {@code commands.permission} and
 * {@code commands.op-only}, while the executor gated on a bare {@code sender.isOp()}. A
 * LuckPerms administrator holding the node but not OP could not run the command at all. These
 * tests describe the rule the command now follows.
 */
class CommandPermissionTest {

    @Test
    @DisplayName("Verify holding the permission node is enough, even without OP")
    void testPermissionNodeIsEnough() {
        assertTrue(MSCCommand.canUseCommands(false, false, true));
        assertTrue(MSCCommand.canUseCommands(true, false, true));
    }

    @Test
    @DisplayName("Verify op-only keeps operators working when they lack the node")
    void testOpOnlyFallback() {
        assertTrue(MSCCommand.canUseCommands(true, true, false));
    }

    @Test
    @DisplayName("Verify op-only false stops an operator without the node")
    void testOpOnlyDisabledRequiresTheNode() {
        assertFalse(MSCCommand.canUseCommands(false, true, false));
    }

    @Test
    @DisplayName("Verify a plain player with no node and no OP is denied")
    void testPlainPlayerIsDenied() {
        assertFalse(MSCCommand.canUseCommands(true, false, false));
        assertFalse(MSCCommand.canUseCommands(false, false, false));
    }

    @Test
    @DisplayName("Verify a subcommand with no configured node stays open to whoever passed the gate")
    void testUnrestrictedSubCommandIsOpen() {
        assertTrue(MSCCommand.canUseSubCommand(false, false));
        assertTrue(MSCCommand.canUseSubCommand(false, true));
    }

    @Test
    @DisplayName("Verify a subcommand pinned to its own node needs that node on top of the main one")
    void testRestrictedSubCommandNeedsItsNode() {
        assertTrue(MSCCommand.canUseSubCommand(true, true));
        assertFalse(MSCCommand.canUseSubCommand(true, false));
    }
}
