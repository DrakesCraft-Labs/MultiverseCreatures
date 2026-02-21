package com.Chagui68.entities;

import com.Chagui68.items.ItemCombatTrades;
import com.Chagui68.items.ItemsFoodTrades;
import com.Chagui68.skin.PlayerSkinDisguiseService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MobHandler implements Listener {

    private static final double SHAGGY_CHANCE = 0.3;

    private final Random random = new Random();
    private final PlayerSkinDisguiseService disguiseService;

    public MobHandler(Plugin plugin) {
        this.disguiseService = new PlayerSkinDisguiseService(plugin);
    }

    @EventHandler
    public void OnSpawn(CreatureSpawnEvent entity) {
        if (entity.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL
            && entity.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
            && entity.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CHUNK_GEN
            && entity.getSpawnReason() != CreatureSpawnEvent.SpawnReason.REINFORCEMENTS
            && entity.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) {
            return;
        }

        double roll = random.nextDouble();
        if (entity.getEntityType() == EntityType.WANDERING_TRADER && roll < SHAGGY_CHANCE) {
            WanderingTrader wanderingTrader = (WanderingTrader) entity.getEntity();
            equipWanderingVillager(wanderingTrader);
            disguiseService.applyPlayerSkin(wanderingTrader);
        }
    }

    private void equipWanderingVillager(WanderingTrader trader) {
        List<MerchantRecipe> trades = new ArrayList<>();
        trader.setCustomName(ChatColor.GREEN + "Shaggy");
        trader.setCustomNameVisible(true);

        ItemStack cookies = ItemsFoodTrades.SCOOBY_COOKIES.clone();
        cookies.setAmount(5);
        MerchantRecipe cookiesTrade = new MerchantRecipe(cookies, 999);
        cookiesTrade.addIngredient(new ItemStack(Material.DIAMOND, 20));
        trades.add(cookiesTrade);

        ItemStack excalibur = ItemCombatTrades.EXCALIBUR_SWORD.clone();
        MerchantRecipe excaliburTrade = new MerchantRecipe(excalibur, 1);
        excaliburTrade.addIngredient(new ItemStack(Material.NETHERITE_INGOT, 8));
        excaliburTrade.addIngredient(new ItemStack(Material.DIAMOND_BLOCK, 16));
        trades.add(excaliburTrade);

        trader.setRecipes(trades);
        trader.addScoreboardTag("MSC_Shaggy");
    }
}
