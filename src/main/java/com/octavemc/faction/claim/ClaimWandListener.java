package com.octavemc.faction.claim;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.type.PlayerFaction;
import com.octavemc.visualise.VisualType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimWandListener implements Listener {

    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        /*
        if (event.getAction() == Action.PHYSICAL || !event.hasItem() || !isClaimingWand(event.getItem())) {
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
                        + ChatColor.AQUA + (event.getAction() == Action.LEFT_CLICK_BLOCK ? '1' : '2') + ChatColor.GRAY + " to: ("
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
        } else if (event.getAction().name().contains("LEFT") && event.getPlayer().isSneaking()) {

        }
         */
        Action action = event.getAction();

        // They didn't use a claiming wand for this action, so ignore.
        if (action == Action.PHYSICAL || !event.hasItem() || !isClaimingWand(event.getItem())) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Clearing the claim selection of player.
        if (action == Action.RIGHT_CLICK_AIR) {
            Apollo.getInstance().getClaimHandler().clearClaimSelection(player);
            player.setItemInHand(new ItemStack(Material.AIR, 1));
            event.getPlayer().sendMessage(Configuration.WARNING_MESSAGE_PREFIX + ChatColor.GRAY + "You have cleared your claim selection.");
            return;
        }

        PlayerFaction playerFaction = Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).get();

        // Purchasing the claim from the selections.
        if ((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && player.isSneaking()) {
            ClaimSelection claimSelection = Apollo.getInstance().getClaimHandler().claimSelectionMap.get(uuid);
            if (claimSelection == null || !claimSelection.hasBothPositionsSet()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You have not set both positions of this claim selection.");
                return;
            }

            if (Apollo.getInstance().getClaimHandler().tryPurchasing(player, claimSelection.toClaim(playerFaction))) {
                Apollo.getInstance().getClaimHandler().clearClaimSelection(player);
                player.setItemInHand(new ItemStack(Material.AIR, 1));
            }
            return;
        }

        // Setting the positions for the claim selection;
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            Location blockLocation = block.getLocation();

            // Don't hoe the soil block.
            if (action == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }

            if (Apollo.getInstance().getClaimHandler().canClaimHere(player, blockLocation)) {
                ClaimSelection revert;
                ClaimSelection claimSelection = Apollo.getInstance().getClaimHandler().claimSelectionMap.putIfAbsent(uuid, revert = new ClaimSelection(blockLocation.getWorld()));
                if (claimSelection == null) claimSelection = revert;

                Location oldPosition;
                Location opposite;
                int selectionId;
                switch (action) {
                    case LEFT_CLICK_BLOCK:
                        oldPosition = claimSelection.getPositionOne();
                        opposite = claimSelection.getPositionTwo();
                        selectionId = 1;
                        break;
                    case RIGHT_CLICK_BLOCK:
                        oldPosition = claimSelection.getPositionTwo();
                        opposite = claimSelection.getPositionOne();
                        selectionId = 2;
                        break;
                    default:
                        return; // This should never happen.
                }

                // Prevent players clicking in the same spot twice.
                int blockX = blockLocation.getBlockX();
                int blockZ = blockLocation.getBlockZ();
                if (oldPosition != null && blockX == oldPosition.getBlockX() && blockZ == oldPosition.getBlockZ()) {
                    return;
                }

                // Allow at least 1 tick before players can update one of the positions to prevent lag/visual glitches with delayed task below.
                if ((System.currentTimeMillis() - claimSelection.getLastUpdateMillis()) <= ClaimHandler.PILLAR_BUFFER_DELAY_MILLIS) {
                    return;
                }

                if (opposite != null) {
                    int xDiff = Math.abs(opposite.getBlockX() - blockX) + 1; // Add one as it gets a weird offset
                    int zDiff = Math.abs(opposite.getBlockZ() - blockZ) + 1; // Add one as it gets a weird offset
                    if (xDiff < ClaimHandler.MIN_CLAIM_RADIUS || zDiff < ClaimHandler.MIN_CLAIM_RADIUS) {
                        player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A claim must be at least " + ChatColor.AQUA + ClaimHandler.MIN_CLAIM_RADIUS + 'x' + ChatColor.AQUA + ClaimHandler.MIN_CLAIM_RADIUS + ChatColor.GRAY + " blocks.");
                        return;
                    }
                }

                if (oldPosition != null) {
                    Apollo.getInstance().getVisualiseHandler().clearVisualBlocks(player, VisualType.CREATE_CLAIM_SELECTION, visualBlock -> {
                        Location location = visualBlock.getLocation();
                        return location.getBlockX() == oldPosition.getBlockX() && location.getBlockZ() == oldPosition.getBlockZ();
                    });
                }

                //TODO: Clear this class up...
                if (selectionId == 1) claimSelection.setAppropriatePos(event.getAction(), blockLocation);
                if (selectionId == 2) claimSelection.setAppropriatePos(event.getAction(), blockLocation);

                event.getPlayer().sendMessage(Configuration.WARNING_MESSAGE_PREFIX + ChatColor.GRAY + "The location of selection "
                        + ChatColor.AQUA + (event.getAction() == Action.LEFT_CLICK_BLOCK ? '1' : '2') + ChatColor.GRAY + " to: ("
                        + ChatColor.AQUA + claimSelection.getAppropriatePos(event.getAction()).getBlockX()
                        + ChatColor.GRAY + ", " + ChatColor.AQUA + claimSelection.getAppropriatePos(event.getAction()).getBlockZ() + ChatColor.GRAY + ").");

                if (claimSelection.hasBothPositionsSet()) {
                    Claim claim = claimSelection.toClaim(playerFaction);
                    int selectionPrice = claimSelection.getPrice(playerFaction, false);
                    event.getPlayer().sendMessage(new String[] {
                            Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Claim Cost: " + (selectionPrice > playerFaction.getBalance() ? ChatColor.RED : ChatColor.AQUA) + Configuration.ECONOMY_SYMBOL + selectionPrice + ChatColor.GRAY + '.',
                            Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Current Size: [" + ChatColor.AQUA + claim.getWidth() + ChatColor.GRAY + ", " + ChatColor.AQUA + claim.getLength() + ChatColor.GRAY + ", " +
                            ChatColor.AQUA + claim.getArea() + ChatColor.GRAY + " blocks]."
                    });
                }

                final int blockY = block.getY();
                final int maxHeight = player.getWorld().getMaxHeight();
                final List<Location> locations = new ArrayList<>(maxHeight);
                for (int i = blockY; i < maxHeight; i++) {
                    Location other = blockLocation.clone();
                    other.setY(i);
                    locations.add(other);
                }

                // Generate the new claiming pillar a tick later as right clicking using this
                // event doesn't update the bottom block clicked occasionally.
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Apollo.getInstance().getVisualiseHandler().generate(player, locations, VisualType.CREATE_CLAIM_SELECTION, true);
                    }
                }.runTaskAsynchronously(Apollo.getInstance());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isClaimingWand(event.getPlayer().getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (isClaimingWand(player.getItemInHand())) {
                player.setItemInHand(null);
                Apollo.getInstance().getClaimHandler().clearClaimSelection(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerKick(PlayerKickEvent event) {
        event.getPlayer().getInventory().remove(ClaimHandler.CLAIM_WAND);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.getPlayer().getInventory().remove(ClaimHandler.CLAIM_WAND);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (isClaimingWand(event.getItemDrop().getItemStack())) {
            event.getItemDrop().remove();
            Apollo.getInstance().getClaimHandler().clearClaimSelection(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPickup(PlayerPickupItemEvent event) {
        if (isClaimingWand(event.getItem().getItemStack())) {
            event.getItem().remove();
            Apollo.getInstance().getClaimHandler().clearClaimSelection(event.getPlayer());
        }
    }

    // Prevents dropping Claiming Wands on death.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getDrops().remove(ClaimHandler.CLAIM_WAND)) {
            Apollo.getInstance().getClaimHandler().clearClaimSelection(event.getEntity());
        }
    }

    // Doesn't get called when opening own inventory.
    @EventHandler(ignoreCancelled = false, priority = EventPriority.NORMAL)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player && player.getInventory().contains(ClaimHandler.CLAIM_WAND)) {
            player.getInventory().remove(ClaimHandler.CLAIM_WAND);
            Apollo.getInstance().getClaimHandler().clearClaimSelection(player);
        }
    }

    /**
     * Checks if an {@link ItemStack} is a Claiming Wand.
     *
     * @param stack the {@link ItemStack} to check
     * @return true if the {@link ItemStack} is a claiming wand
     */
    public boolean isClaimingWand(ItemStack stack) {
        return stack != null && stack.isSimilar(ClaimHandler.CLAIM_WAND);
    }
}