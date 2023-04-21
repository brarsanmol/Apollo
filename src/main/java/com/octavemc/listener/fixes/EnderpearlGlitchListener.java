package com.octavemc.listener.fixes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.type.ClaimableFaction;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.InventoryHolder;

public class EnderpearlGlitchListener implements Listener {

    //TODO: Move this to Configuration class.
    private static final ImmutableSet<Material> BLOCKED_PEARL_TYPES = Sets.immutableEnumSet(
            Material.THIN_GLASS,
            Material.IRON_FENCE,
            Material.FENCE,
            Material.NETHER_FENCE,
            Material.FENCE_GATE,
            Material.ACACIA_STAIRS,
            Material.BIRCH_WOOD_STAIRS,
            Material.BRICK_STAIRS,
            Material.COBBLESTONE_STAIRS,
            Material.DARK_OAK_STAIRS,
            Material.JUNGLE_WOOD_STAIRS,
            Material.NETHER_BRICK_STAIRS,
            Material.QUARTZ_STAIRS,
            Material.SANDSTONE_STAIRS,
            Material.SMOOTH_STAIRS,
            Material.SPRUCE_WOOD_STAIRS,
            Material.WOOD_STAIRS,
            Material.WOOD_STEP,
            Material.WOOD_DOUBLE_STEP,
            Material.STEP,
            Material.DOUBLE_STEP
    );

    public EnderpearlGlitchListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && event.hasItem()
                && event.getItem().getType() == Material.ENDER_PEARL && event.getClickedBlock().getType().isSolid()
                && !(event.getClickedBlock().getState() instanceof InventoryHolder)
                && Apollo.getInstance().getFactionDao().getFactionAt(event.getClickedBlock().getLocation()).get() instanceof ClaimableFaction) {
            event.setCancelled(true);
            event.getPlayer().setItemInHand(event.getItem());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPearlClip(PlayerTeleportEvent event) {
        if (Configuration.ENDERPEARL_GLITCHING_ENABLED
                && event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            if (BLOCKED_PEARL_TYPES.contains(event.getTo().getBlock().getType())) {
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Pearl glitching detected" + (Configuration.ENDERPEARL_GLITCHING_REFUND ? ", used Enderpearl has been refunded" : "") + ".");
                Apollo.getInstance().getTimerManager().getEnderpearlTimer().refund(event.getPlayer());
                event.setCancelled(true);
            } else {
                event.getTo().setX(event.getTo().getBlockX() + 0.5);
                event.getTo().setZ(event.getTo().getBlockZ() + 0.5);
                event.setTo(event.getTo());
            }
        }
    }
}
