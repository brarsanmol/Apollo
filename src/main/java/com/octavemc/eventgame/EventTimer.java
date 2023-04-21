package com.octavemc.eventgame;

import com.destroystokyo.paper.Title;
import com.google.common.collect.Iterables;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.eventgame.faction.ConquestFaction;
import com.octavemc.eventgame.faction.EventFaction;
import com.octavemc.eventgame.faction.KothFaction;
import com.octavemc.faction.event.CaptureZoneEnterEvent;
import com.octavemc.faction.event.CaptureZoneLeaveEvent;
import com.octavemc.faction.type.Faction;
import com.octavemc.listener.EventSignListener;
import com.octavemc.timer.GlobalTimer;
import com.octavemc.util.InventoryUtils;
import lombok.Getter;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Timer that handles the cooldown for KingOfTheHill events.
 */
public class EventTimer extends GlobalTimer implements Listener {

    private static final long RESCHEDULE_FREEZE_MILLIS = TimeUnit.SECONDS.toMillis(15L);
    private static final String RESCHEDULE_FREEZE_WORDS = DurationFormatUtils.formatDurationWords(RESCHEDULE_FREEZE_MILLIS, true, true);

    private long startStamp;                 // the milliseconds at when the current event started.
    private long lastContestedEventMillis;   // the milliseconds at when the last event was contested.
    @Getter
    private EventFaction eventFaction;

    public EventTimer() {
        super("Event", 0L);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (eventFaction != null) {
                    eventFaction.getEventType().getEventTracker().tick(EventTimer.this, eventFaction);
                    return;
                }

                // There isn't an active event, find one!
                LocalDateTime now = LocalDateTime.now();
                int day = now.getDayOfYear();
                int hour = now.getHour();
                int minute = now.getMinute();
                for (Map.Entry<LocalDateTime, String> entry : Apollo.getInstance().getEventScheduler().getSchedule().entrySet()) {
                    // Compare now with the scheduled time..
                    LocalDateTime scheduledTime = entry.getKey();
                    if (day != scheduledTime.getDayOfYear() || hour != scheduledTime.getHour() || minute != scheduledTime.getMinute()) {
                        continue;
                    }

                    // Make sure the faction found in schedule exists.
                    Optional<Faction> faction = Apollo.getInstance().getFactionDao().getByName(entry.getValue());
                    if (faction.isPresent() && faction.get() instanceof EventFaction && tryContesting((EventFaction) faction.get(), Bukkit.getConsoleSender())) {
                        break;
                    }
                }
            }
        }.runTaskTimer(Apollo.getInstance(), 20L, 20L);
    }

    @Override
    public String getName() {
        return eventFaction != null ? eventFaction.getName() : "Event";
    }

    @Override
    public boolean clearCooldown() {
        boolean result = super.clearCooldown();
        if (eventFaction != null) {
            for (CaptureZone captureZone : eventFaction.getCaptureZones()) {
                captureZone.setCappingPlayer(null);
            }

            // Make sure to set the land back as Deathban.
            eventFaction.setDeathban(true);
            eventFaction.getEventType().getEventTracker().stopTiming();
            eventFaction = null;
            startStamp = -1L;
            result = true;
        }

        return result;
    }

    @Override
    public long getRemaining() {
        if (eventFaction == null) {
            return 0L;
        } else if (eventFaction instanceof KothFaction) {
            return ((KothFaction) eventFaction).getCaptureZone().getRemainingCaptureMillis();
        } else {
            return super.getRemaining();
        }
    }

    /**
     * Handles the winner for this event.
     *
     * @param winner the {@link Player} that won
     */
    public void handleWinner(Player winner) {
        if (eventFaction != null) {
            //TODO: Should we remove the broadcastMessage?
            Apollo.getInstance().getServer().broadcastMessage(
                    ChatColor.GRAY + "[" + ChatColor.AQUA + eventFaction.getEventType().getDisplayName() + ChatColor.GRAY + "] "
                            + ChatColor.AQUA + winner.getName() + ChatColor.GRAY + " has captured " + ChatColor.AQUA + eventFaction.getName() + ChatColor.GRAY + '.'
            );
            Apollo.getInstance().getServer().getOnlinePlayers().forEach(player -> player.sendTitle(new Title.Builder()
                    .title(ChatColor.AQUA + "KoTH")
                    .subtitle(ChatColor.AQUA + winner.getName() + ChatColor.GRAY + " has captured " + ChatColor.AQUA + eventFaction.getName())
                    .stay(3).build()));

            Apollo.getInstance().getCrateDao().get(eventFaction.getCrate()).ifPresentOrElse(crate -> {
                if (InventoryUtils.getEmptySlots(winner.getInventory()) >= 2) winner.getInventory().addItem(crate.getKey(), EventSignListener.getEventSign(eventFaction.getName(), winner.getName()));
                else {
                    winner.getWorld().dropItemNaturally(winner.getLocation(), crate.getKey());
                    winner.getWorld().dropItemNaturally(winner.getLocation(), EventSignListener.getEventSign(eventFaction.getName(), winner.getName()));
                }
            }, () -> winner.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The crate that this event is mapped to does not exist, please contact a staff member."));

            clearCooldown(); // must always be cooled last as this nulls some variables.
        }
    }

    /**
     * Tries contesting an {@link EventFaction}.
     *
     * @param faction the {@link EventFaction} to be contested
     * @param sender       the contesting {@link CommandSender}
     * @return true if the {@link EventFaction} was successfully contested
     */
    public boolean tryContesting(EventFaction faction, CommandSender sender) {
        if (this.eventFaction != null) {
            sender.sendMessage(ChatColor.RED + "There is already an active event, use /event cancel to end it.");
            return false;
        }

        if (faction instanceof KothFaction koth) {
            if (koth.getCaptureZone() == null) {
                sender.sendMessage(ChatColor.RED + "Cannot schedule " + faction.getName() + " as its' capture zone is not set.");
                return false;
            }
        } else if (faction instanceof ConquestFaction conquest) {
            Collection<ConquestFaction.ConquestZone> zones = conquest.getConquestZones();
            for (ConquestFaction.ConquestZone zone : ConquestFaction.ConquestZone.values()) {
                if (!zones.contains(zone)) {
                    sender.sendMessage(ChatColor.RED + "Cannot schedule " + faction.getName() + " as capture zone '" + zone.getDisplayName() + ChatColor.RED + "' is not set.");
                    return false;
                }
            }
        }

        // Don't allow events to reschedule their-self before they are allowed to.
        long millis = System.currentTimeMillis();
        if (this.lastContestedEventMillis + EventTimer.RESCHEDULE_FREEZE_MILLIS - millis > 0L) {
            sender.sendMessage(ChatColor.RED + "Cannot reschedule events within " + EventTimer.RESCHEDULE_FREEZE_WORDS + '.');
            return false;
        }

        lastContestedEventMillis = millis;
        startStamp = millis;
        this.eventFaction = faction;

        faction.getEventType().getEventTracker().onContest(faction, this);
        if (faction instanceof ConquestFaction) {
            setRemaining(1000L, true); //TODO: Add a unpredicated timer impl instead of this xD.
            setPaused(true);
        }

        faction.getCaptureZones().stream().filter(CaptureZone::isActive).forEach(zone -> {
            Player player = Iterables.getFirst(zone.getCuboid().getPlayers(), null);
            if (player != null && faction.getEventType().getEventTracker().onControlTake(player, zone, faction)) zone.setCappingPlayer(player);
        });
        faction.setDeathban(false); // the event should be lowered deathban whilst active.
        return true;
    }

    /**
     * Gets the total uptime in milliseconds since the event started.
     *
     * @return the time in milliseconds since event started
     */
    public long getUptime() {
        return System.currentTimeMillis() - startStamp;
    }

    /**
     * Gets the time in milliseconds since the event started.
     *
     * @return the time in milliseconds since event started
     */
    public long getStartStamp() {
        return startStamp;
    }

    private void handleDisconnect(Player player) {
        if (eventFaction != null) {
            Objects.requireNonNull(player);
            Collection<CaptureZone> captureZones = eventFaction.getCaptureZones();
            for (CaptureZone captureZone : captureZones) {
                if (Objects.equals(captureZone.getCappingPlayer(), player)) {
                    captureZone.setCappingPlayer(null);
                    eventFaction.getEventType().getEventTracker().onControlLoss(player, captureZone, eventFaction);
                    break;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        handleDisconnect(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLogout(PlayerQuitEvent event) {
        handleDisconnect(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        handleDisconnect(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneEnter(CaptureZoneEnterEvent event) {
        if (eventFaction != null) {
            CaptureZone captureZone = event.getCaptureZone();
            if (eventFaction.getCaptureZones().contains(captureZone)) {
                Player player = event.getPlayer();
                if (captureZone.getCappingPlayer() == null && eventFaction.getEventType().getEventTracker().onControlTake(player, captureZone, eventFaction)) {
                    captureZone.setCappingPlayer(player);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneLeave(CaptureZoneLeaveEvent event) {
        if (Objects.equals(event.getFaction(), eventFaction)) {
            Player player = event.getPlayer();
            CaptureZone captureZone = event.getCaptureZone();
            if (Objects.equals(player, captureZone.getCappingPlayer())) {
                captureZone.setCappingPlayer(null);
                eventFaction.getEventType().getEventTracker().onControlLoss(player, captureZone, eventFaction);

                // Try and find a new capper.
                for (Player target : captureZone.getCuboid().getPlayers()) {
                    if (target != null && !target.equals(player) && eventFaction.getEventType().getEventTracker().onControlTake(target, captureZone, eventFaction)) {
                        captureZone.setCappingPlayer(target);
                        break;
                    }
                }
            }
        }
    }
}