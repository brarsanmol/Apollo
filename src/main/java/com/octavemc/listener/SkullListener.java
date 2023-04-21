package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SkullListener implements Listener {

    public SkullListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null && event.getEntity().hasPermission("apollo.kill.behead")) {
            event.getDrops().add(new ItemBuilder(Material.SKULL_ITEM)
                    .data((byte) 3)
                    .owner(event.getEntity())
                    .build());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.getClickedBlock().getState() instanceof Skull skull
                && skull.getSkullType() == SkullType.PLAYER) {
            event.getPlayer().sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Skull Of: " + ChatColor.AQUA + skull.getOwner());
        }
    }
}
