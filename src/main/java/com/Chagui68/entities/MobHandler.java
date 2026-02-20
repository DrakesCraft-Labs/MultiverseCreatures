package com.Chagui68.entities;

import com.Chagui68.items.ItemCombatTrades;
import com.Chagui68.items.ItemsFoodTrades;
import com.Chagui68.listener.ItemFoodHandler;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MobHandler implements Listener {

    private final Random random = new Random();

    private static final double SHAGGY_CHANCE = 0.3;

    public MobHandler(Plugin plugin) {

    }

    @EventHandler
    public void OnSpawn(CreatureSpawnEvent entity) {
        if (entity.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL &&
                entity.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG &&
                entity.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CHUNK_GEN &&
                entity.getSpawnReason() != CreatureSpawnEvent.SpawnReason.REINFORCEMENTS &&
                entity.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER)
            return;
        double roll = random.nextDouble();

// Manejo de aldeanos ambulantes
        if (entity.getEntityType() == EntityType.WANDERING_TRADER) {
            WanderingTrader wanderingTrader = (WanderingTrader) entity.getEntity();

// Shaggy
            if (roll < SHAGGY_CHANCE) {
                equipWanderingVillager(wanderingTrader);
            }
        }
    }


    private static void equipWanderingVillager(WanderingTrader trader) {
        List<MerchantRecipe> tradeos = new ArrayList<>();
        trader.setCustomName("§aShaggy");
        trader.setCustomNameVisible(true);

        ItemStack resultado1 = ItemsFoodTrades.SCOOBY_COOKIES.clone();
        resultado1.setAmount(5);
        MerchantRecipe precio1 = new MerchantRecipe(resultado1,999);
        precio1.addIngredient(new ItemStack(Material.DIAMOND,20));
        tradeos.add(precio1);

        ItemStack resultado2 = ItemCombatTrades.

        trader.setRecipes(tradeos);

        trader.addScoreboardTag("MSC_Shaggy");
    }

    private static void equipSnoopy(Wolf wolf){

    }
}
