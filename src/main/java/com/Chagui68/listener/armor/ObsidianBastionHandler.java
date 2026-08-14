package com.Chagui68.listener.armor;

import com.Chagui68.items.armor.ObsidianBastion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ObsidianBastionHandler implements Listener {

    private static final NamespacedKey HP_MOD_KEY =
            new NamespacedKey("multiversecreatures", "obsidian_bastion_health_modifier");
    private static final NamespacedKey SPEED_MOD_KEY =
            new NamespacedKey("multiversecreatures", "obsidian_bastion_speed_modifier");
    private static final NamespacedKey KB_MOD_KEY =
            new NamespacedKey("multiversecreatures", "obsidian_bastion_kb_modifier");

    private final Set<UUID> hasSetBonus = ConcurrentHashMap.newKeySet();

    public ObsidianBastionHandler(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                checkSetBonus(p);
            }
        }, 20L, 20L);
    }

    private boolean isBastionPiece(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }

    private boolean isFullSet(Player p) {
        return isBastionPiece(p.getInventory().getHelmet(), ObsidianBastion.HELMET_KEY)
                && isBastionPiece(p.getInventory().getChestplate(), ObsidianBastion.CHEST_KEY)
                && isBastionPiece(p.getInventory().getLeggings(), ObsidianBastion.LEGS_KEY)
                && isBastionPiece(p.getInventory().getBoots(), ObsidianBastion.BOOTS_KEY);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        removeSetBonus(p);
        if (isFullSet(p)) {
            hasSetBonus.add(p.getUniqueId());
            applySetBonus(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (hasSetBonus.remove(event.getPlayer().getUniqueId())) {
            removeSetBonus(event.getPlayer());
        }
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        if (event.getBrokenItem().getType() == Material.NETHERITE_HELMET
                || event.getBrokenItem().getType() == Material.NETHERITE_CHESTPLATE
                || event.getBrokenItem().getType() == Material.NETHERITE_LEGGINGS
                || event.getBrokenItem().getType() == Material.NETHERITE_BOOTS) {
            Bukkit.getScheduler().runTaskLater(
                    Bukkit.getPluginManager().getPlugin("MultiverseCreatures"),
                    () -> checkSetBonus(event.getPlayer()), 1L);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (!hasSetBonus.contains(p.getUniqueId())) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE
                || event.getCause() == EntityDamageEvent.DamageCause.LAVA
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                || event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
            event.setCancelled(true);
        }
    }

    private void checkSetBonus(Player p) {
        boolean nowHas = isFullSet(p);
        UUID id = p.getUniqueId();
        boolean had = hasSetBonus.contains(id);

        if (!nowHas) {
            removeSetBonus(p);
            if (had) {
                hasSetBonus.remove(id);
                p.sendMessage(ChatColor.GRAY + "Obsidian Bastion set bonus lost.");
            }
            return;
        }

        if (!had) {
            hasSetBonus.add(id);
            applySetBonus(p);
            p.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "Obsidian Bastion set bonus activated!");
        }
    }

    private void applySetBonus(Player p) {
        AttributeInstance health = p.getAttribute(Attribute.MAX_HEALTH);
        if (health != null && health.getModifier(HP_MOD_KEY) == null) {
            AttributeModifier mod = new AttributeModifier(HP_MOD_KEY,
                    ObsidianBastion.MAX_HEALTH_BONUS, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
            double oldMax = health.getValue();
            health.addModifier(mod);
            p.setHealth(Math.min(p.getHealth() * (health.getValue() / oldMax), health.getValue()));
        }

        AttributeInstance kb = p.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (kb != null && kb.getModifier(KB_MOD_KEY) == null) {
            kb.addModifier(new AttributeModifier(KB_MOD_KEY, 1.0, AttributeModifier.Operation.ADD_NUMBER));
        }

        AttributeInstance speed = p.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(SPEED_MOD_KEY) == null) {
            speed.addModifier(new AttributeModifier(SPEED_MOD_KEY,
                    -ObsidianBastion.SPEED_PENALTY, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        }
    }

    private void removeSetBonus(Player p) {
        AttributeInstance health = p.getAttribute(Attribute.MAX_HEALTH);
        if (health != null) {
            AttributeModifier mod = health.getModifier(HP_MOD_KEY);
            if (mod != null) {
                double oldMax = health.getValue();
                health.removeModifier(mod);
                p.setHealth(Math.min(p.getHealth(), health.getValue()));
            }
        }

        AttributeInstance speed = p.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null) {
            AttributeModifier mod = speed.getModifier(SPEED_MOD_KEY);
            if (mod != null) {
                speed.removeModifier(mod);
            }
        }

        AttributeInstance kb = p.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (kb != null) {
            AttributeModifier mod = kb.getModifier(KB_MOD_KEY);
            if (mod != null) {
                kb.removeModifier(mod);
            }
        }
    }
}
