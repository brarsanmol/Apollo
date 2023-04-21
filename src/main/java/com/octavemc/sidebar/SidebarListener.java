package com.octavemc.sidebar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;

public class SidebarListener implements Listener {

    public SidebarListener() {
        Zeus.getInstance().getPlugin().getServer().getPluginManager().registerEvents(this, Zeus.getInstance().getPlugin());
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Zeus.getInstance().getSidebarManager().addSidebar(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        event.getPlayer().getScoreboard().getTeams().forEach(team -> team.unregister());
        event.getPlayer().getScoreboard().getObjectives().forEach(objective -> objective.unregister());
        Zeus.getInstance().getSidebarManager().removeSidebar(event.getPlayer());
    }

    @EventHandler
    public void onPluginDisableEvent(PluginDisableEvent event) {
        Zeus.getInstance().getSidebarEntryGenerator().clearEntries();
        Zeus.getInstance().getSidebarTask().getBukkitTask().cancel();
    }

}