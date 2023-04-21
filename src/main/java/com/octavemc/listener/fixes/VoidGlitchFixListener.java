package com.octavemc.listener.fixes;

import com.octavemc.Apollo;
import com.octavemc.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Listener that prevents players being killed by the void in the Overworld.
 */
public class VoidGlitchFixListener implements Listener {

    public VoidGlitchFixListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerDamageEvent(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID
                && event.getEntity() instanceof Player
                && event.getEntity().getWorld().getEnvironment() != World.Environment.THE_END) {
            Location destination = BukkitUtils.getHighestLocation(event.getEntity().getLocation());
            if (destination != null && event.getEntity().teleport(destination, PlayerTeleportEvent.TeleportCause.PLUGIN)) {
                event.setCancelled(true);
                event.getEntity().sendMessage(ChatColor.YELLOW + "You were saved from the void.");
            }
        }
    }
}
