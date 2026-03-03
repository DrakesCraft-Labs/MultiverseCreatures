package com.Chagui68;

import com.Chagui68.commands.AdminSpawnCommand;
import com.Chagui68.entities.MobHandler;
import com.Chagui68.items.ItemCombatTrades;
import com.Chagui68.listener.EntitiesIAHandler;
import com.Chagui68.listener.ItemCombatHandler;
import com.Chagui68.listener.ItemFoodHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class MutlverseCreatures extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        MobHandler mobHandler = new MobHandler(this);

        getServer().getPluginManager().registerEvents(new ItemCombatHandler(), this);
        ItemCombatHandler.inicializarReloj(this);
        getServer().getPluginManager().registerEvents(mobHandler, this);
        getServer().getPluginManager().registerEvents(new ItemFoodHandler(), this);
        getServer().getPluginManager().registerEvents(new EntitiesIAHandler(), this);

        getCommand("msc").setExecutor(new AdminSpawnCommand(mobHandler));
    }
}
