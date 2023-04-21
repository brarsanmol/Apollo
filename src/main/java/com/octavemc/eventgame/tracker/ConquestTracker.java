package com.octavemc.eventgame.tracker;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.eventgame.CaptureZone;
import com.octavemc.eventgame.EventTimer;
import com.octavemc.eventgame.EventType;
import com.octavemc.eventgame.faction.ConquestFaction;
import com.octavemc.eventgame.faction.EventFaction;
import com.octavemc.faction.event.FactionRemoveEvent;
import com.octavemc.faction.type.PlayerFaction;
import com.octavemc.util.ConcurrentValueOrderedMap;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Tracker used for handling the Conquest points.
 */
@Deprecated
public class ConquestTracker implements EventTracker, Listener {

    /**
     * Minimum time the KOTH has to be controlled before this tracker will announce when control has been lost.
     */
    private static final long MINIMUM_CONTROL_TIME_ANNOUNCE = TimeUnit.SECONDS.toMillis(5L);
    public static final long DEFAULT_CAP_MILLIS = TimeUnit.SECONDS.toMillis(30L);

    @Getter
    private final ConcurrentValueOrderedMap<PlayerFaction, Integer> factionPointsMap = new ConcurrentValueOrderedMap<>();

    public ConquestTracker() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRemove(FactionRemoveEvent event) {
        if (event.getFaction() instanceof PlayerFaction faction) this.factionPointsMap.remove(faction);
    }

    /**
     * Gets the amount of points a {@link PlayerFaction} has
     * gained for this {@link ConquestTracker}.
     *
     * @param faction the faction to get for
     * @return the new points of the {@link PlayerFaction}.
     */
    public int getPoints(PlayerFaction faction) {
        return this.factionPointsMap.getOrDefault(faction, 0);
    }

    /**
     * Sets the points a {@link PlayerFaction} has gained for this {@link ConquestTracker}.
     *
     * @param faction the faction to set for
     * @param amount  the amount to set
     * @return the new points of the {@link PlayerFaction}
     */
    public int setPoints(PlayerFaction faction, int amount) {
        this.factionPointsMap.put(faction, amount);
        return amount;
    }

    /**
     * Takes points from a {@link PlayerFaction} gained from this {@link ConquestTracker}.has
     *
     * @param faction the faction to take from
     * @param amount  the amount to take
     * @return the new points of the {@link PlayerFaction}
     */
    public int takePoints(PlayerFaction faction, int amount) {
        return setPoints(faction, getPoints(faction) - amount);
    }

    /**
     * Adds points to a {@link PlayerFaction} gained from this {@link ConquestTracker}.has
     *
     * @param faction the faction to add from
     * @param amount  the amount to add
     * @return the new points of the {@link PlayerFaction}
     */
    public int addPoints(PlayerFaction faction, int amount) {
        return setPoints(faction, getPoints(faction) + amount);
    }

    @Override
    public EventType getEventType() {
        return EventType.CONQUEST;
    }

    @Override
    public void tick(EventTimer eventTimer, EventFaction eventFaction) {
        ConquestFaction conquestFaction = (ConquestFaction) eventFaction;
        List<CaptureZone> captureZones = conquestFaction.getCaptureZones();
        for (CaptureZone captureZone : captureZones) {
            captureZone.updateScoreboardRemaining();
            Player cappingPlayer = captureZone.getCappingPlayer();
            if (cappingPlayer == null) continue;

            if (!captureZone.getCuboid().contains(cappingPlayer)) {
                onControlLoss(cappingPlayer, captureZone, eventFaction);
                continue;
            }

            // The capture zone has been controlled.
            long remainingMillis = captureZone.getRemainingCaptureMillis();
            if (remainingMillis <= 0L) {
                UUID uuid = cappingPlayer.getUniqueId();

                Apollo.getInstance().getFactionDao().getByPlayer(uuid).ifPresent(playerFaction -> {
                    int newPoints = addPoints(playerFaction, 1);
                    if (newPoints < Configuration.CONQUEST_REQUIRED_VICTORY_POINTS) {
                        // Reset back to the default for this tracker.
                        captureZone.setRemainingCaptureMillis(captureZone.getDefaultCaptureMillis());
                        Apollo.getInstance().getServer().broadcastMessage(
                            ChatColor.GRAY + "[" + ChatColor.AQUA + eventFaction.getName() + ChatColor.GRAY + "] "
                                    + ChatColor.AQUA + playerFaction.getName() + ChatColor.GRAY + " has gained 1 point for capturing zone " + captureZone.getDisplayName()
                                    + ChatColor.GRAY + " (" + ChatColor.AQUA + newPoints + ChatColor.GRAY + '/' + ChatColor.AQUA + Configuration.CONQUEST_REQUIRED_VICTORY_POINTS + ChatColor.GRAY + ')'
                        );
                    } else {
                        // Clear all the points for the next Conquest event.
                        this.factionPointsMap.clear();
                        Apollo.getInstance().getTimerManager().getEventTimer().handleWinner(cappingPlayer);
                        return;
                    }
                });
                return;
            }

            int remainingSeconds = (int) Math.round((double) remainingMillis / 1000L);
            if (remainingSeconds % 5 == 0) {
                cappingPlayer.sendMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + eventFaction.getName() + ChatColor.GRAY + "] Attempting to capture "
                        + ChatColor.AQUA + captureZone.getDisplayName() + ChatColor.GRAY + ", "
                        + ChatColor.AQUA + remainingSeconds + ChatColor.GRAY + " seconds remaining.");
            }
        }
    }

    @Override
    public void onContest(EventFaction eventFaction, EventTimer eventTimer) {
        Apollo.getInstance().getServer().broadcastMessage(ChatColor.GRAY + (eventFaction instanceof ConquestFaction ? "" : "[" + ChatColor.AQUA + eventFaction.getName() + ChatColor.GRAY + "] ") + ChatColor.AQUA + eventFaction.getName() + ChatColor.GRAY + " can now be contested.");
    }

    @Override
    public boolean onControlTake(Player player, CaptureZone captureZone, EventFaction eventFaction) {
        if (!Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).isPresent()) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must be in a faction to capture zones for " + eventFaction.getName());
            return false;
        }
        return true;
    }

    @Override
    public void onControlLoss(Player player, CaptureZone captureZone, EventFaction eventFaction) {
        if (captureZone.getRemainingCaptureMillis() > 0L
                && (captureZone.getDefaultCaptureMillis() - captureZone.getRemainingCaptureMillis()) > MINIMUM_CONTROL_TIME_ANNOUNCE)
            Apollo.getInstance().getServer().broadcastMessage(ChatColor.GRAY + "[" + ChatColor.AQUA + eventFaction.getName() + ChatColor.GRAY + "] "
                            + ChatColor.AQUA + player.getName() + ChatColor.GRAY + " has been knocked off " + captureZone.getDisplayName() + ChatColor.GRAY + '.');
    }

    @Override
    public void stopTiming() {
        factionPointsMap.clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (Apollo.getInstance().getTimerManager().getEventTimer().getEventFaction() instanceof ConquestFaction conquest)
            Apollo.getInstance().getFactionDao().getByPlayer(event.getEntity().getUniqueId())
                    .filter(faction -> Configuration.CONQUEST_POINT_LOSS_PER_DEATH > 0)
                    .ifPresent(faction -> {
                        int oldPoints = getPoints(faction);
                        if (oldPoints == 0) return;
                        int newPoints = takePoints(faction, Configuration.CONQUEST_POINT_LOSS_PER_DEATH);
                        event.setDeathMessage(null); // for some reason if it isn't handled manually, weird colour coding happens
                        Apollo.getInstance().getServer().broadcastMessage(
                                ChatColor.GRAY + "[" + ChatColor.AQUA + conquest.getName() + ChatColor.GRAY + "] "
                                        + ChatColor.AQUA + faction.getName() + ChatColor.GRAY + " has lost " + ChatColor.AQUA + Configuration.CONQUEST_POINT_LOSS_PER_DEATH + ChatColor.GRAY + " points because "
                                        + ChatColor.AQUA + event.getEntity().getName() + ChatColor.GRAY + " has died."
                                        + ChatColor.GRAY + '(' + ChatColor.AQUA + newPoints + ChatColor.GRAY + '/' + ChatColor.AQUA + Configuration.CONQUEST_REQUIRED_VICTORY_POINTS + ChatColor.GRAY + ')'
                        );
                    });
    }
}
