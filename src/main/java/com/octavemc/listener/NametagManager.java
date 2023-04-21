package com.octavemc.listener;

import com.google.common.collect.Iterables;
import com.octavemc.Apollo;
import com.octavemc.faction.event.FactionRelationCreateEvent;
import com.octavemc.faction.event.FactionRelationRemoveEvent;
import com.octavemc.faction.event.PlayerJoinedFactionEvent;
import com.octavemc.faction.event.PlayerLeftFactionEvent;
import com.octavemc.faction.type.PlayerFaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.Optional;

public class NametagManager implements Listener {

    public NametagManager() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Apollo.getInstance().getServer().getOnlinePlayers().forEach(player -> this.addUpdates(player, Collections.singleton(event.getPlayer())));
        this.addUpdates(event.getPlayer(), Apollo.getInstance().getServer().getOnlinePlayers());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        //TODO: Remove player from teams.
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoinedFaction(PlayerJoinedFactionEvent event) {
        event.getPlayer().ifPresent(player -> {
            this.addUpdates(player, event.getFaction().getOnlinePlayers());
            event.getFaction().getOnlinePlayers().forEach(target -> this.addUpdates(target, Collections.singleton(player)));
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLeftFaction(PlayerLeftFactionEvent event) {
        event.getPlayer().ifPresent(player -> {
            this.addUpdates(player, event.getFaction().getOnlinePlayers());
            event.getFaction().getOnlinePlayers().forEach(target -> this.addUpdates(target, Collections.singleton(player)));
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionAllyCreate(FactionRelationCreateEvent event) {
        Iterable<Player> updates = Iterables.concat(
                event.getSenderFaction().getOnlinePlayers(),
                event.getTargetFaction().getOnlinePlayers()
        );

        Apollo.getInstance().getServer().getOnlinePlayers().forEach(player -> addUpdates(player, updates));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionAllyRemove(FactionRelationRemoveEvent event) {
        Iterable<Player> updates = Iterables.concat(
                event.getSenderFaction().getOnlinePlayers(),
                event.getTargetFaction().getOnlinePlayers()
        );

        Apollo.getInstance().getServer().getOnlinePlayers().forEach(player -> addUpdates(player, updates));
    }

    public void addUpdates(Player player, Iterable<? extends Player> updates) {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Lazy load - don't lookup this in every iteration
                Optional<PlayerFaction> playerFaction = null;
                boolean firstExecute = false;

                for (Player update : updates) {
                    if (player.equals(update)) {
                        player.getScoreboard().getTeam("members").addPlayer(update);
                        continue;
                    }

                    if (!firstExecute) {
                        playerFaction = Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId());
                        firstExecute = true;
                    }

                    // Lazy loading for performance increase.
                    Optional<PlayerFaction> targetFaction;
                    if (playerFaction == null
                            || playerFaction.isEmpty()
                            || (targetFaction = Apollo.getInstance().getFactionDao().getByPlayer(update.getUniqueId())).isEmpty()) {
                        player.getScoreboard().getTeam("neutrals").addPlayer(update);
                    } else if (playerFaction.get() == targetFaction.get()) {
                        player.getScoreboard().getTeam("members").addPlayer(update);
                    } else if (playerFaction.get().getAllied().contains(targetFaction.get().getIdentifier())) {
                        player.getScoreboard().getTeam("allies").addPlayer(update);
                    } else {
                        player.getScoreboard().getTeam("neutrals").addPlayer(update);
                    }
                }
            }
        }.runTaskAsynchronously(Apollo.getInstance());
    }

}
