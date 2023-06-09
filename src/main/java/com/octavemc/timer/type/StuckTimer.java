package com.octavemc.timer.type;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.LandMap;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.timer.TimerCooldown;
import com.octavemc.util.DurationFormatter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class StuckTimer extends PlayerTimer implements Listener {

    // The maximum distance a player can move before
    // this timer will self-cancel
    public static final int MAX_MOVE_DISTANCE = 5;

    private final Map<UUID, Location> startedLocations = new HashMap<>();

    public StuckTimer() {
        super("Stuck", TimeUnit.MINUTES.toMillis(2L) + TimeUnit.SECONDS.toMillis(45L), false);
    }

    @Override
    public TimerCooldown clearCooldown(@Nullable Player player, UUID uuid) {
        TimerCooldown runnable = super.clearCooldown(player, uuid);

        if (runnable != null) startedLocations.remove(uuid);
        return runnable;
    }

    @Override
    public boolean setCooldown(@Nullable Player player, UUID playerUUID, long millis, boolean force, @Nullable Predicate<Long> callback) {
        if (player != null && super.setCooldown(player, playerUUID, millis, force, callback)) {
            startedLocations.put(playerUUID, player.getLocation());
            return true;
        }

        return false;
    }

    private void checkMovement(Player player, Location from, Location to) {
        if (getRemaining(player.getUniqueId()) > 0L) {
            if (from == null) {
                clearCooldown(player, player.getUniqueId());
                return;
            }

            int xDiff = Math.abs(from.getBlockX() - to.getBlockX());
            int yDiff = Math.abs(from.getBlockY() - to.getBlockY());
            int zDiff = Math.abs(from.getBlockZ() - to.getBlockZ());
            if (xDiff > MAX_MOVE_DISTANCE || yDiff > MAX_MOVE_DISTANCE || zDiff > MAX_MOVE_DISTANCE) {
                clearCooldown(player, player.getUniqueId());
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You moved more than " + ChatColor.AQUA + MAX_MOVE_DISTANCE
                        + ChatColor.GRAY + " blocks, your " + ChatColor.AQUA + "stuck timer" + ChatColor.GRAY + " has been cancelled!");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (getRemaining(uuid) > 0L) {
            Location from = startedLocations.get(uuid);
            checkMovement(player, from, event.getTo());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (getRemaining(uuid) > 0L) {
            Location from = startedLocations.get(uuid);
            checkMovement(player, from, event.getTo());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (getRemaining(event.getPlayer().getUniqueId()) > 0L) {
            clearCooldown(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (getRemaining(event.getPlayer().getUniqueId()) > 0L) {
            clearCooldown(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (getRemaining(player) > 0L) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You were damaged, your "
                        + ChatColor.AQUA + "stuck timer" + ChatColor.GRAY + " has been cancelled!");
                clearCooldown(player);
            }
        }
    }

    private static final int NEAR_SEARCH_DISTANCE_BLOCKS = 24;

    @Override
    public void handleExpiry(@Nullable Player player, UUID userUUID) {
        if (player != null) {
            Location nearest = LandMap.getNearestSafePosition(player, player.getLocation(), NEAR_SEARCH_DISTANCE_BLOCKS);
            if (nearest == null) {
                Apollo.getInstance().getCombatLogListener().safelyDisconnect(player, Configuration.DANGER_MESSAGE_PREFIX
                        + ChatColor.GRAY + "Unable to find a safe area near you, you have been safely logged out.");
            } else if (player.teleport(nearest, PlayerTeleportEvent.TeleportCause.PLUGIN)) {
                player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You have been teleported to the nearest safe area.");
            }
        }
    }

    public void run(Player player) {
        if (getRemaining(player) > 0L) player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Teleporting to a safe area in "
                + ChatColor.AQUA + DurationFormatter.getRemaining(getRemaining(player), true, false) + ChatColor.GRAY + '.');
    }
}
