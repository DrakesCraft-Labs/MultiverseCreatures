package com.Chagui68.listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


import javax.management.ListenerNotFoundException;

public class EntitiesIAHandler implements Listener {



        @EventHandler
        public void alEntrar(PlayerJoinEvent event) {
            event.getPlayer().sendMessage("¡Bienvenido al servidor!");
        }
    }


