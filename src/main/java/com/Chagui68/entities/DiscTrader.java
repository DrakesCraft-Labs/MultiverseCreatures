package com.Chagui68.entities;

import com.Chagui68.MultiverseCreatures;
import com.Chagui68.music.MusicDisc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * The Disc Trader: a librarian villager that sells one music disc per song
 * loaded by the MusicManager. The discs work only as regular jukebox discs.
 */
public class DiscTrader {

    private final MultiverseCreatures plugin;

    public DiscTrader(MultiverseCreatures plugin) {
        this.plugin = plugin;
    }

    public boolean trySpawn(Location location) {
        if (!plugin.isEnabled("entities.disc-trader")) return false;
        if (location.getWorld() == null) return false;

        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setCustomName(ChatColor.GOLD + "Disc Trader");
        villager.setCustomNameVisible(true);
        villager.setAdult();
        villager.setAgeLock(true);
        villager.setVillagerType(Villager.Type.PLAINS);
        villager.setProfession(Profession.LIBRARIAN);
        villager.setVillagerLevel(2);
        villager.setVillagerExperience(10);
        villager.setAI(true);
        villager.setCollidable(false);
        villager.setInvulnerable(true);
        villager.setRemoveWhenFarAway(false);
        villager.setPersistent(true);
        villager.addScoreboardTag("MSC_DiscTrader");

        assignTrades(villager);
        return true;
    }

    public boolean isDiscTrader(Villager villager) {
        return villager.getScoreboardTags().contains("MSC_DiscTrader");
    }

    public void assignTrades(Villager villager) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!villager.isValid()) return;
                List<MerchantRecipe> trades = new ArrayList<>();
                for (String song : plugin.getMusicManager().getSongNames()) {
                    ItemStack disc = MusicDisc.create(song, plugin.getMusicManager());
                    MerchantRecipe trade = new MerchantRecipe(disc, 999);
                    trade.setExperienceReward(false);
                    trade.addIngredient(new ItemStack(Material.EMERALD, 16));
                    trades.add(trade);
                }
                villager.setRecipes(trades);
            }
        }.runTaskLater(plugin, 2L);
    }

    public void reloadTrades() {
        for (var world : Bukkit.getWorlds()) {
            for (Villager villager : world.getEntitiesByClass(Villager.class)) {
                if (isDiscTrader(villager)) {
                    assignTrades(villager);
                }
            }
        }
    }
}
