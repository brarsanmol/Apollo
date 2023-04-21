package com.octavemc.timer.type;

import com.octavemc.Apollo;
import com.octavemc.faction.type.PlayerFaction;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.timer.TimerCooldown;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Timer that handles teleportation warmups for {@link Player}s.
 */
public class TeleportTimer extends PlayerTimer implements Listener {

    private final Map<UUID, Location> destinationMap = new HashMap<>();

    public TeleportTimer() {
        super("Teleport", TimeUnit.SECONDS.toMillis(10L), false);
    }

    @Override
    public void handleExpiry(@Nullable Player player, UUID userUUID) {
        if (player != null) {
            Location destination = destinationMap.remove(userUUID);
            if (destination != null) {
                destination.getChunk(); // pre-load the chunk before teleport.
                player.teleport(destination, PlayerTeleportEvent.TeleportCause.COMMAND);
            }
        }
    }

    /**
     * Gets the {@link Location} this {@link TeleportTimer} will send to.
     *
     * @param player the {@link Player} to get for
     * @return the {@link Location}
     */
    public Location getDestination(Player player) {
        return destinationMap.get(player.getUniqueId());
    }

    @Override
    public TimerCooldown clearCooldown(UUID uuid) {
        TimerCooldown runnable = super.clearCooldown(uuid);
        if (runnable != null) {
            destinationMap.remove(uuid);
            return runnable;
        }

        return null;
    }

    /**
     * Gets the amount of enemies nearby a {@link Player}.
     *
     * @param player   the {@link Player} to get for
     * @param distance the radius to get within
     * @return the amount of players within enemy distance
     */
    public int getNearbyEnemies(Player player, int distance) {
        Optional<PlayerFaction> playerFaction = Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId());
        int count = 0;
        for (Entity entity : player.getNearbyEntities(distance, distance, distance)) {
            if (entity instanceof Player target) {
                Optional<PlayerFaction> targetFaction = Apollo.getInstance().getFactionDao().getByPlayer(target.getUniqueId());
                // If the nearby player or sender cannot see each-other, continue.
                if (!target.canSee(player) || !player.canSee(target)) continue;
                if (playerFaction.isEmpty() || targetFaction.isEmpty() || (targetFaction.isPresent() && targetFaction.get() != playerFaction.get())) count++;
            }
        }

        return count;
    }

    /**
     * Teleports a {@link Player} to a {@link Location} with a delay.
     *
     * @param player        the {@link Player} to teleport
     * @param location      the {@link Location} to teleport to
     * @param millis        the time in milliseconds until teleport
     * @param warmupMessage the message to send whilst waiting
     * @param cause         the cause for teleporting
     * @return true if {@link Player} was successfully teleported
     */
    public boolean teleport(Player player, Location location, long millis, String warmupMessage, PlayerTeleportEvent.TeleportCause cause) {
        cancelTeleport(player, null); // cancels any previous teleport for the player.

        boolean result;
        if (millis <= 0) { // if there is no delay, just instantly teleport.
            result = player.teleport(location, cause);
            clearCooldown(player.getUniqueId());
        } else {
            player.sendMessage(warmupMessage);
            destinationMap.put(player.getUniqueId(), location.clone());
            setCooldown(player, player.getUniqueId(), millis, true, null);
            result = true;
        }

        return result;
    }

    /**
     * Cancels a {@link Player}s' teleport process for a given reason.
     *
     * @param player the {@link Player} to cancel for
     * @param reason the reason for cancelling
     */
    public void cancelTeleport(Player player, String reason) {
        if (getRemaining(player.getUniqueId()) > 0L) {
            clearCooldown(player.getUniqueId());
            if (reason != null && !reason.isEmpty()) player.sendMessage(reason);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Player didn't move a block.
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;
        cancelTeleport(event.getPlayer(), ChatColor.YELLOW + "You moved a block, therefore cancelling your teleport.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            cancelTeleport(player, ChatColor.YELLOW + "You took damage, therefore cancelling your teleport.");
        }
    }
}
