package com.Chagui68.utils;

import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The Spanish messages of version 3 give way to the English ones, unless an operator edited them. */
class MscRetiredDefaultsTest {

    @Test
    @DisplayName("An untouched Spanish list takes the English default; an edited one is kept")
    void replacesOnlyUntouchedLists() {
        String path = "vampire.sun-death-messages";
        List<String> english = List.of("&6%player% &cturned to ashes under the sun");
        MemoryConfiguration defaults = new MemoryConfiguration();
        defaults.set(path, english);
        defaults.set("entities.dio-brando.death-messages", List.of("english"));

        MemoryConfiguration config = new MemoryConfiguration();
        config.set(path, MscConfigMigration.RETIRED_DEFAULTS.get(path));
        List<String> edited = List.of("&6%player% &cmi propio mensaje");
        config.set("entities.dio-brando.death-messages", edited);

        Set<String> replaced = MscConfigMigration.replaceRetiredDefaults(config, defaults);

        assertEquals(Set.of(path), replaced);
        assertEquals(english, config.getStringList(path));
        assertEquals(edited, config.getStringList("entities.dio-brando.death-messages"));
    }
}
