package com.Chagui68;

import com.Chagui68.entities.MobHandler;
import com.Chagui68.listener.EntitiesIAHandler;
import com.Chagui68.listener.ItemFoodHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class MutlverseCreatures extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new MobHandler(this), this);
        getServer().getPluginManager().registerEvents(new ItemFoodHandler(), this);
        getServer().getPluginManager().registerEvents(new EntitiesIAHandler(), this);
    }
}
