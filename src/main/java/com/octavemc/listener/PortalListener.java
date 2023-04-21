package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.util.DurationFormatter;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PortalListener implements Listener {

    private static final long PORTAL_MESSAGE_DELAY_THRESHOLD = 2500L;

    private final TObjectLongMap<UUID> messageDelays;

    public PortalListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
        this.messageDelays = new TObjectLongHashMap<>();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onEntityPortal(EntityPortalEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            if (event.getTo().getWorld() != null
                    && event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
                event.useTravelAgent(false);
                event.setTo(event.getTo().getWorld().getSpawnLocation().clone().add(0.5, 0, 0.5));
                if (Configuration.END_EXTINGUISH_FIRE_ON_EXIT) event.getPlayer().setFireTicks(0);
            } else if (event.getFrom().getWorld() != null
                    && event.getFrom().getWorld().getEnvironment() == World.Environment.THE_END) {
                    event.useTravelAgent(false);
                    event.setTo(Configuration.END_EXIT_LOCATION.getLocation());
            }
        }
    }

    // Prevent players jumping the End with Strength.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        if (event.getFrom().getEnvironment() != World.Environment.THE_END
                && event.getPlayer().getWorld().getEnvironment() == World.Environment.THE_END
                && event.getPlayer().hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            event.getPlayer().removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            event.getPlayer().sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your strength effect has been removed because you entered the end.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPortalEnter(PlayerPortalEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            return;
        }

        Location to = event.getTo();
        World toWorld = to.getWorld();
        if (toWorld == null) return; // safe-guard if the End or Nether is disabled

        if (toWorld.getEnvironment() == World.Environment.THE_END) {
            Player player = event.getPlayer();

            // Prevent entering the end if it's closed.
            if (!Configuration.END_OPEN) {
                message(player, Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The end is currently closed.");
                event.setCancelled(true);
                return;
            }

            // Prevent entering the end if the player is Spawn Tagged.
            PlayerTimer timer = Apollo.getInstance().getTimerManager().getCombatTimer();
            long remaining;
            if ((remaining = timer.getRemaining(player)) > 0L) {
                message(player, Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not enter the end for another " + ChatColor.AQUA + DurationFormatter.getRemaining(remaining, true, false) + ChatColor.GRAY + ", because you are combat tagged.");
                event.setCancelled(true);
                return;
            }

            // Prevent entering the end if the player is PVP Protected.
            timer = Apollo.getInstance().getTimerManager().getInvincibilityTimer();
            if ((remaining = timer.getRemaining(player)) > 0L) {
                message(player, Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not enter the end for another " + ChatColor.AQUA + DurationFormatter.getRemaining(remaining, true, false) + ChatColor.GRAY + ", because you have invincibility.");
                event.setCancelled(true);
                return;
            }

            event.useTravelAgent(false);
            event.setTo(toWorld.getSpawnLocation().add(0.5, 0, 0.5));
        }
    }

    private void message(Player player, String message) {
        if (messageDelays.get(player.getUniqueId()) != messageDelays.getNoEntryValue()
                && (messageDelays.get(player.getUniqueId()) + PORTAL_MESSAGE_DELAY_THRESHOLD) - System.currentTimeMillis() > 0L) return;
        messageDelays.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(message);
    }
}
