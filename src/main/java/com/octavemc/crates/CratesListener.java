package com.octavemc.crates;

import com.octavemc.Apollo;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class CratesListener implements Listener {

    public CratesListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getClickedBlock().getType() == Material.ENDER_CHEST) {
            switch (event.getAction()) {
                case RIGHT_CLICK_BLOCK ->
                        Apollo.getInstance().getCrateDao().getByKey(event.getItem())
                            .ifPresentOrElse(crate -> {
                                if (event.getPlayer().getInventory().firstEmpty() == -1) {
                                    event.getPlayer().sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" + ChatColor.DARK_RED + ChatColor.BOLD +  "!" + ChatColor.DARK_GRAY + ChatColor.BOLD + ") " + ChatColor.GRAY + "Your inventory is full, empty out a slot and try again.");
                                    return;
                                }
                                event.getClickedBlock().getLocation().getWorld().spawnEntity(event.getClickedBlock().getLocation().add(0, 1, 0), EntityType.FIREWORK);
                                event.getPlayer().setItemInHand(null);
                                event.getPlayer().getInventory().addItem(crate.next());
                                event.getPlayer().sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" + ChatColor.AQUA + ChatColor.BOLD +  "!" + ChatColor.DARK_GRAY + ChatColor.BOLD + ") " + ChatColor.GRAY + "You have redeemed a " + ChatColor.AQUA + crate.getIdentifier() + ChatColor.GRAY + " crate key.");
                            }, () -> event.getPlayer().sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" + ChatColor.DARK_RED + ChatColor.BOLD +  "!" + ChatColor.DARK_GRAY + ChatColor.BOLD + ") " + ChatColor.GRAY + "You must have a " + ChatColor.AQUA + "crate key" + ChatColor.GRAY + " in your hand."));
                case LEFT_CLICK_BLOCK -> Apollo.getInstance().getCrateDao().getByLocation(event.getClickedBlock().getLocation()).ifPresent(crate -> event.getPlayer().openInventory(crate.getInventory()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (Apollo.getInstance().getCrateDao().get(event.getInventory().getName()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDragEvent(InventoryDragEvent event) {
        if (Apollo.getInstance().getCrateDao().get(event.getInventory().getName()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event) {
        if(!event.getRightClicked().isVisible()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (Apollo.getInstance().getCrateDao().getByKey(event.getItemInHand()).isPresent()) {
            event.setCancelled(true);
        }
    }

}
