package com.octavemc.eventgame;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.claim.ClaimHandler;
import com.octavemc.faction.claim.ClaimSelection;
import com.octavemc.visualise.VisualType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EventClaimWandListener implements Listener {

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL || !event.hasItem() || !isEventClaimingWand(event.getItem())) {
            return;
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            Apollo.getInstance().getClaimHandler().clearClaimSelection(event.getPlayer());
            event.getPlayer().setItemInHand(new ItemStack(Material.AIR, 1));
            event.getPlayer().sendMessage(Configuration.WARNING_MESSAGE_PREFIX + ChatColor.GRAY + "You have cleared your claim selection.");
        } else if (event.getAction().name().endsWith("BLOCK")) {
            ClaimSelection selection = Apollo.getInstance().getClaimHandler().getClaimSelectionMap().putIfAbsent(event.getPlayer().getUniqueId(), new ClaimSelection(event.getClickedBlock().getWorld())) == null
                    ? Apollo.getInstance().getClaimHandler().getClaimSelectionMap().get(event.getPlayer().getUniqueId())
                    : Apollo.getInstance().getClaimHandler().getClaimSelectionMap().get(event.getPlayer().getUniqueId());

            if (!event.getClickedBlock().equals(selection.getAppropriatePos(event.getAction()))
                    && (System.currentTimeMillis() - selection.getLastUpdateMillis()) >= ClaimHandler.PILLAR_BUFFER_DELAY_MILLIS) {
                if (selection.getAppropriatePos(event.getAction()) != null) {
                    Apollo.getInstance().getVisualiseHandler().clearVisualBlocks(event.getPlayer(), VisualType.CREATE_CLAIM_SELECTION, block ->
                            block.getLocation().getBlockX() == selection.getAppropriatePos(event.getAction()).getBlockX() && block.getLocation().getBlockZ() == selection.getAppropriatePos(event.getAction()).getBlockZ()
                    );
                }

                selection.setAppropriatePos(event.getAction(), event.getClickedBlock().getLocation());
                event.getPlayer().sendMessage(Configuration.WARNING_MESSAGE_PREFIX + ChatColor.GRAY + "The location of selection "
                        + ChatColor.AQUA + (event.getAction() == Action.LEFT_CLICK_BLOCK ? '1' : '2') + ChatColor.GRAY + " has been set to: ("
                        + ChatColor.AQUA + selection.getAppropriatePos(event.getAction()).getBlockX()
                        + ChatColor.GRAY + ", " + ChatColor.AQUA + selection.getAppropriatePos(event.getAction()).getBlockZ() + ChatColor.GRAY + ").");

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Apollo.getInstance().getVisualiseHandler().generate(event.getPlayer(), IntStream.range(event.getClickedBlock().getY(), event.getPlayer().getWorld().getMaxHeight()).mapToObj(y -> {
                            Location location = event.getClickedBlock().getLocation().clone();
                            location.setY(y);
                            return location;
                        }).collect(Collectors.toList()), VisualType.CREATE_CLAIM_SELECTION, true);
                    }
                }.runTaskAsynchronously(Apollo.getInstance());
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isEventClaimingWand(event.getPlayer().getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (isEventClaimingWand(player.getItemInHand())) {
                player.setItemInHand(new ItemStack(Material.AIR, 1));
                Apollo.getInstance().getClaimHandler().clearClaimSelection(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerKick(PlayerKickEvent event) {
        event.getPlayer().getInventory().remove(ClaimHandler.EVENT_CLAIM_WAND);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().getInventory().remove(ClaimHandler.EVENT_CLAIM_WAND);
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        if (isEventClaimingWand(item.getItemStack())) {
            item.remove();
            Apollo.getInstance().getClaimHandler().clearClaimSelection(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        if (isEventClaimingWand(item.getItemStack())) {
            item.remove();
            Apollo.getInstance().getClaimHandler().clearClaimSelection(event.getPlayer());
        }
    }

    // Prevents dropping Claiming Wands on death.
    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getDrops().remove(ClaimHandler.EVENT_CLAIM_WAND)) {
            Apollo.getInstance().getClaimHandler().clearClaimSelection(event.getEntity());
        }
    }

    // Doesn't get called when opening own inventory.
    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onInventoryOpen(InventoryOpenEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            if (player.getInventory().contains(ClaimHandler.EVENT_CLAIM_WAND)) {
                player.getInventory().remove(ClaimHandler.EVENT_CLAIM_WAND);
                Apollo.getInstance().getClaimHandler().clearClaimSelection(player);
            }
        }
    }

    /**
     * Checks if an {@link ItemStack} is a Claiming Wand.
     *
     * @param stack the {@link ItemStack} to check
     * @return true if the {@link ItemStack} is a claiming wand
     */
    public boolean isEventClaimingWand(ItemStack stack) {
        return stack != null && stack.isSimilar(ClaimHandler.EVENT_CLAIM_WAND);
    }
}
