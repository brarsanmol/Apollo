package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public final class CoreListener implements Listener {

    private static final String DEFAULT_WORLD_NAME = "world";

    public CoreListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(Bukkit.getWorld(CoreListener.DEFAULT_WORLD_NAME).getSpawnLocation().add(0.5, 0, 0.5));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            Apollo.getInstance().getUserDao().get(player.getUniqueId()).ifPresent(user -> user.setBalance(Configuration.ECONOMY_STARTING_BALANCE));
            event.setSpawnLocation(Bukkit.getWorld(CoreListener.DEFAULT_WORLD_NAME).getSpawnLocation().add(0.5, 0, 0.5));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!Configuration.SPAWNERS_PREVENT_PLACING_NETHER) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getWorld().getEnvironment() == World.Environment.NETHER && event.getBlock().getState() instanceof CreatureSpawner &&
                !player.hasPermission(ProtectionListener.PROTECTION_BYPASS_PERMISSION)) {

            event.setCancelled(true);
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not break spawners in the nether.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (Configuration.PREVENT_PLACING_BEDS_IN_NETHER) {
            if (player.getWorld().getEnvironment() == World.Environment.NETHER && event.getItemInHand() != null && event.getItemInHand().getType() == Material.BED) {
                event.setCancelled(true);
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not place beds in the nether.");
            }
        }
        if (Configuration.SPAWNERS_PREVENT_PLACING_NETHER) {
            if (player.getWorld().getEnvironment() == World.Environment.NETHER && event.getBlock().getState() instanceof CreatureSpawner &&
                    !player.hasPermission(ProtectionListener.PROTECTION_BYPASS_PERMISSION)) {

                event.setCancelled(true);
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not place spawners in the nether.");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Apollo.getInstance().getVisualiseHandler().clearVisualBlocks(player, null, null, false);
        Apollo.getInstance().getUserDao().get(event.getPlayer().getUniqueId()).ifPresent(user -> user.setShowClaimMap(false));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Apollo.getInstance().getVisualiseHandler().clearVisualBlocks(player, null, null, false);
        Apollo.getInstance().getUserDao().get(event.getPlayer().getUniqueId()).ifPresent(user -> user.setShowClaimMap(false));    }

    /*@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        plugin.getVisualiseHandler().clearVisualBlocks(event.getChunk());
    }*/
}
