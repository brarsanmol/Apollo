package com.octavemc.sotw;

import com.octavemc.Apollo;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class SotwListener implements Listener {

    public SotwListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player
                && event.getCause() != EntityDamageEvent.DamageCause.SUICIDE
                && Apollo.getInstance().getSotwTimer().getSotwRunnable() != null) {
            event.setCancelled(true);
        }
    }
}
