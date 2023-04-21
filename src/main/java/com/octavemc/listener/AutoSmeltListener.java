package com.octavemc.listener;

import com.octavemc.Apollo;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class AutoSmeltListener implements Listener {

    public AutoSmeltListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (event.getPlayer().hasPermission("apollo.autosmelt")
                && (event.getBlock().getType() == Material.IRON_ORE || event.getBlock().getType() == Material.GOLD_ORE)
                && !event.getBlock().getDrops(event.getPlayer().getItemInHand()).isEmpty()) {
            ItemStack stack = new ItemStack(Material.valueOf(event.getBlock().getType().name().replace("ORE", "INGOT")));
            event.getBlock().setType(Material.AIR);
            event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation(), stack);
        }
    }

}
