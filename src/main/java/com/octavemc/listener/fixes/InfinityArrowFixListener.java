package com.octavemc.listener.fixes;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

/**
 * Listener that removes {@link org.bukkit.entity.Arrow}s from bows with Infinity when they land.
 */
public class InfinityArrowFixListener implements Listener {

    public InfinityArrowFixListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (Configuration.REMOVE_INFINITY_ARROWS_ON_LAND
                && event.getEntity() instanceof Arrow arrow
                && !(arrow.getShooter() instanceof Player)) {
            arrow.remove();
        }
    }
}
