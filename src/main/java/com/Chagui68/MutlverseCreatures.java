package com.Chagui68;

import com.Chagui68.entities.MobHandler;
import com.Chagui68.entities.dragon.DragonCombatHandler;
import com.Chagui68.entities.dragon.DragonSpawnHandler;
import com.Chagui68.listener.EntitiesIAHandler;
import com.Chagui68.listener.ItemCombatHandler;
import com.Chagui68.listener.ItemFoodHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class MutlverseCreatures extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        MobHandler mobHandler = new MobHandler(this);
        getServer().getPluginManager().registerEvents(mobHandler, this);
        getServer().getPluginManager().registerEvents(new ItemFoodHandler(), this);
        getServer().getPluginManager().registerEvents(new EntitiesIAHandler(), this);
        getServer().getPluginManager().registerEvents(new ItemCombatHandler(this), this);

        // Dragon Handlers
        DragonCombatHandler dragonCombatHandler = new DragonCombatHandler(this);
        getServer().getPluginManager().registerEvents(dragonCombatHandler, this);
        getServer().getPluginManager().registerEvents(new DragonSpawnHandler(dragonCombatHandler), this);

        // Commands
        com.Chagui68.commands.MSCCommand mscCommand = new com.Chagui68.commands.MSCCommand(mobHandler,
                dragonCombatHandler);
        getCommand("msc").setExecutor(mscCommand);
    }

}
