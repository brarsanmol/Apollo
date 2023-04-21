package com.octavemc.combatlogging;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.combatlogging.event.LoggerRemovedEvent;
import com.octavemc.combatlogging.event.LoggerSpawnEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * Listener that prevents {@link Player}s from combat-logging.
 */
public final class CombatLogListener implements Listener {

    private static final int NEARBY_SPAWN_RADIUS = 64;

    private final Set<UUID> safelyDisconnected;
    private final Map<UUID, LoggerEntityVillager> loggers;

    public CombatLogListener() {
        this.safelyDisconnected = new HashSet<>();
        this.loggers = new HashMap<>();
    }

    public void onNPCDeathEvent(NPCDeathEvent event) {
        event.getNPC().
    }

    /**
     * Disconnects a {@link Player} without a {@link LoggerEntityVillager} spawning.
     *
     * @param player the {@link Player} to disconnect
     * @param reason the reason for disconnecting
     */
    public void safelyDisconnect(Player player, String reason) {
        if (safelyDisconnected.add(player.getUniqueId())) player.kickPlayer(reason);
    }

    /**
     * Removes all the {@link LoggerEntityVillager} instances from the server.
     */
    public void removeCombatLoggers() {
        Iterator<LoggerEntityVillager> iterator = loggers.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().destroy();
            iterator.remove();
        }
        safelyDisconnected.clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLoggerRemoved(LoggerRemovedEvent event) {
        loggers.remove(event.getLoggerEntity().getUniqueID());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        LoggerEntityVillager logger = loggers.remove(event.getPlayer().getUniqueId());
        if (logger != null) logger.destroy();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!Configuration.HANDLE_COMBAT_LOGGING) return;

        if (player.getGameMode() != GameMode.CREATIVE && !player.isDead() && !safelyDisconnected.remove(player.getUniqueId())) {
            // If the player has PVP protection, don't spawn a logger
            if (Apollo.getInstance().getTimerManager().getInvincibilityTimer().getRemaining(player.getUniqueId()) > 0L) return;

            // There is no enemies near the player, so don't spawn a logger.
            if (Apollo.getInstance().getTimerManager().getTeleportTimer().getNearbyEnemies(player, NEARBY_SPAWN_RADIUS) <= 0
                    || Apollo.getInstance().getSotwTimer().getSotwRunnable() != null) return;

            // Make sure the player is not in a safezone.
            if (Apollo.getInstance().getFactionDao().getFactionAt(player.getLocation()).isPresent()
                    && Apollo.getInstance().getFactionDao().getFactionAt(player.getLocation()).get().isSafezone()) return;

            // Make sure the player hasn't already spawned a logger.
            if (loggers.containsKey(player.getUniqueId())) return;

            var entity = new LoggerEntityVillager(player, ((CraftWorld) player.getLocation().getWorld()).getHandle());
            var calledEvent = new LoggerSpawnEvent(entity);
            Apollo.getInstance().getServer().getPluginManager().callEvent(calledEvent);
            if (!calledEvent.isCancelled()) {
                loggers.put(player.getUniqueId(), entity);
                Apollo.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Apollo.getInstance(), entity, Configuration.COMBAT_LOG_DESPAWN_DELAY_TICKS);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Villager villager && villager.hasMetadata("combat-logger")) event.setCancelled(true);
    }
}
