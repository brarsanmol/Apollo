package com.octavemc.tablist;

import com.octavemc.Apollo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TablistListener implements Listener {

    public TablistListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Apollo.getInstance().getTablistManager().addTablist(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Apollo.getInstance().getTablistManager().getTablist(event.getPlayer()).getSlots().clear();
        Apollo.getInstance().getTablistManager().removeTablist(event.getPlayer());
    }
}
