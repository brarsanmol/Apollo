package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BookDisenchantListener implements Listener {

    public BookDisenchantListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (Configuration.BOOKS_DISENCHANTING
                && event.getAction() == Action.LEFT_CLICK_BLOCK
                && event.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE
                && event.getPlayer().getItemInHand().getType() == Material.ENCHANTED_BOOK) {
            event.setCancelled(true);
            event.getPlayer().setItemInHand(new ItemStack(Material.BOOK, 1));
            event.getPlayer().sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You have disenchanted this book.");
        }
    }

}
