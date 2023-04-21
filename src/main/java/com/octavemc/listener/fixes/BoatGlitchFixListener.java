package com.octavemc.listener.fixes;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;

/**
 * Listener that prevents boats from being placed on land.
 */
public class BoatGlitchFixListener implements Listener {

    public BoatGlitchFixListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleCreate(VehicleCreateEvent event) {
        if (Configuration.DISABLE_BOAT_PLACEMENT_ON_LAND && event.getVehicle() instanceof Boat boat) {
            Block belowBlock = boat.getLocation().add(0, -1, 0).getBlock();
            if (belowBlock.getType() != Material.WATER && belowBlock.getType() != Material.STATIONARY_WATER) {
                boat.remove();
            }
        }
    }
}
