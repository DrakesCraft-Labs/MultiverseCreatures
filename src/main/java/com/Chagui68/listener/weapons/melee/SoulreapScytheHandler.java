package com.Chagui68.listener.weapons.melee;

import com.Chagui68.items.weapons.melee.SoulreapScythe;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SoulreapScytheHandler implements Listener {

    private final Plugin plugin;
    private final Map<UUID, Integer> soulCounts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> reapEnds = new ConcurrentHashMap<>();
    private final Set<UUID> inHit = ConcurrentHashMap.newKeySet();

    public SoulreapScytheHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private boolean isScythe(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_HOE) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(SoulreapScythe.SCYTHE_KEY, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player p)) return;
        ItemStack main = p.getInventory().getItemInMainHand();
        if (!isScythe(main)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        // Only the scythe's own melee hits apply its mechanics. Re-dealt or
        // secondary damage (e.g. thorns-style re-deals with the player as the
        // direct entity, which fires this same event type)
        // must not trigger the drain again.
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        UUID uuid = p.getUniqueId();
        if (!inHit.add(uuid)) return;

        try {
            long now = System.currentTimeMillis();
            boolean inReap = reapEnds.getOrDefault(uuid, 0L) > now;

            int hpDrain = inReap ? SoulreapScythe.LIFESTEAL_HIT * 2 : SoulreapScythe.LIFESTEAL_HIT;
            // Deal the drain through the real damage pipeline instead of
            // setHealth() so protective systems (e.g. the Eight-Handled Wheel)
            // can adapt to it. THORNS bypasses armor, same as the old drain.
            // The inHit guard above stops the resulting event from re-entering.
            DamageSource drainSource = DamageSource.builder(DamageType.THORNS)
                    .withCausingEntity(p)
                    .withDirectEntity(p)
                    .build();
            target.damage(hpDrain, drainSource);

            double maxHealth = p.getAttribute(Attribute.MAX_HEALTH).getValue();
            double newHealth = Math.min(maxHealth, p.getHealth() + hpDrain);
            p.setHealth(newHealth);

            soulCounts.merge(uuid, 1, Integer::sum);

            if (target.getHealth() <= 0) {
                soulCounts.merge(uuid, 3, Integer::sum);
            }

            if (inReap) {
                event.setDamage(event.getDamage() * SoulreapScythe.REAP_DAMAGE_MULTIPLIER);
                target.getWorld().spawnParticle(Particle.SOUL, target.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.05);
            }

            int souls = soulCounts.get(uuid);
            if (souls >= SoulreapScythe.SOULS_REQUIRED && !inReap) {
                reapEnds.put(uuid, now + (SoulreapScythe.REAP_DURATION_TICKS * 50L));
                soulCounts.put(uuid, 0);
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, SoulreapScythe.REAP_DURATION_TICKS, 1, false, false));
                p.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "REAP ACTIVATED!");
                p.getWorld().spawnParticle(Particle.SOUL, p.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.1);
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.5f, 0.5f);
            }
        } finally {
            inHit.remove(uuid);
        }
    }
}