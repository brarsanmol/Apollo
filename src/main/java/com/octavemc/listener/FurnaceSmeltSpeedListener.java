package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class FurnaceSmeltSpeedListener implements Listener {

    public FurnaceSmeltSpeedListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Furnace furnace) {
            furnace.setCookTime((short) Configuration.FURNACE_COOK_SPEED_MULTIPLIER);
        }
    }
}
