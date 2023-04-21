package com.octavemc.listener;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LunarClientListener implements Listener {

    public LunarClientListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        LunarClientAPI.getInstance().sendWaypoint(event.getPlayer(), new LCWaypoint("Spawn", Apollo.getInstance().getServer().getWorld("world").getSpawnLocation(), Configuration.RELATION_COLOUR_SAFEZONE.getChar(), true));
        LunarClientAPI.getInstance().sendWaypoint(event.getPlayer(), new LCWaypoint("Nether Spawn", Apollo.getInstance().getServer().getWorld("world_nether").getSpawnLocation(), Configuration.RELATION_COLOUR_SAFEZONE.getChar(), true));
        LunarClientAPI.getInstance().sendWaypoint(event.getPlayer(), new LCWaypoint("End Spawn", Apollo.getInstance().getServer().getWorld("world_the_end").getSpawnLocation(), Configuration.RELATION_COLOUR_WARZONE.getChar(), true));
        //TODO: Send waypoints for KoTH's and Conquests (If Active)
    }

}
