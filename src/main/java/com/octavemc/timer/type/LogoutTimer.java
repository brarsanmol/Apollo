package com.octavemc.timer.type;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.util.DurationFormatter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LogoutTimer extends PlayerTimer implements Listener {

    public LogoutTimer() {
        super("Logout", TimeUnit.SECONDS.toMillis(30L), false);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        if (getRemaining(event.getPlayer()) > 0L) {
            event.getPlayer().sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You moved, your " + ChatColor.AQUA + "logout timer" + ChatColor.GRAY + " has been cancelled!");
            clearCooldown(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        onPlayerMove(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        if (getRemaining(event.getPlayer().getUniqueId()) > 0L) clearCooldown(event.getPlayer(), event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (getRemaining(event.getPlayer().getUniqueId()) > 0L) clearCooldown(event.getPlayer(), event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && getRemaining(player) > 0L) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You were damaged, your " + ChatColor.AQUA + "logout timer" + ChatColor.GRAY + " has been cancelled!");
            clearCooldown(player);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent cast
                && cast.getDamager() instanceof Player attacker
                && event.getEntity() instanceof Player
                && getRemaining(attacker) > 0L) {
            attacker.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You attacked a player, your " + ChatColor.AQUA + "logout timer" + ChatColor.GRAY + " has been cancelled!");
            clearCooldown(attacker);
        }
    }

    @Override
    public void handleExpiry(@Nullable Player player, UUID userUUID) {
        if (player != null) Apollo.getInstance().getCombatLogListener().safelyDisconnect(player, Configuration.PRIMARY_MESSAGE_PREFIX + "You have been safely disconnected.");
    }

    public void run(Player player) {
        if (getRemaining(player) > 0L)
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "The " + ChatColor.AQUA + "logout timer" + ChatColor.GRAY + " is disconnecting you in "
                    + ChatColor.AQUA + DurationFormatter.getRemaining(getRemaining(player), true, false) + ChatColor.GRAY + '.');
    }
}
