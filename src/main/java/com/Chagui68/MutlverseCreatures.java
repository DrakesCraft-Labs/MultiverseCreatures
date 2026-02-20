package com.Chagui68;

import com.Chagui68.entities.MobHandler;
import com.Chagui68.listener.EntitiesIAHandler;
import com.Chagui68.listener.ItemFoodHandler;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public class MutlverseCreatures extends JavaPlugin {

    private static MutlverseCreatures instance;

    public MutlverseCreatures() {
        instance = this;
    }

    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new MobHandler(this), this);
        getServer().getPluginManager().registerEvents(new ItemFoodHandler(), this);
        getServer().getPluginManager().registerEvents(new EntitiesIAHandler(),this);
    }
}
