package com.octavemc.eventgame.eotw;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.event.FactionClaimChangeEvent;
import com.octavemc.faction.event.FactionCreateEvent;
import com.octavemc.faction.event.cause.ClaimChangeCause;
import com.octavemc.faction.type.PlayerFaction;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener used to handle events for if EOTW is active.
 */
public final class EotwListener implements Listener {

    public EotwListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        EotwHandler.EotwRunnable runnable = Apollo.getInstance().getEotwHandler().getRunnable();
        if (runnable != null) runnable.handleDisconnect(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        EotwHandler.EotwRunnable runnable = Apollo.getInstance().getEotwHandler().getRunnable();
        if (runnable != null) runnable.handleDisconnect(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        EotwHandler.EotwRunnable runnable = Apollo.getInstance().getEotwHandler().getRunnable();
        if (runnable != null) runnable.handleDisconnect(event.getEntity());
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionCreate(FactionCreateEvent event) {
        if (Apollo.getInstance().getEotwHandler().isEndOfTheWorld() && event.getSender() instanceof PlayerFaction) {
            event.setCancelled(true);
            event.getSender().sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not create factions during the end of the world!");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionClaimChange(FactionClaimChangeEvent event) {
        if (Apollo.getInstance().getEotwHandler().isEndOfTheWorld() && event.getCause() == ClaimChangeCause.CLAIM && event.getClaimableFaction() instanceof PlayerFaction) {
            event.setCancelled(true);
            event.getSender().sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not claim land during EOTW.");
        }
    }
}
