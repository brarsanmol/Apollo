package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.eventgame.faction.ConquestFaction;
import com.octavemc.eventgame.faction.KothFaction;
import com.octavemc.faction.event.*;
import com.octavemc.faction.struct.RegenStatus;
import com.octavemc.faction.type.Faction;
import com.octavemc.faction.type.PlayerFaction;
import com.octavemc.tablist.Tablist;
import com.octavemc.util.ReflectionUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class FactionListener implements Listener {

    private static final long FACTION_JOIN_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    private static final String FACTION_JOIN_WAIT_WORDS = DurationFormatUtils.formatDurationWords(FACTION_JOIN_WAIT_MILLIS, true, true);

    private static final String LAND_CHANGED_META_KEY = "landChangedMessage";
    private static final long LAND_CHANGE_MSG_THRESHOLD = 225L;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoinedFactionEvent(PlayerJoinedFactionEvent event) {
        Apollo.getInstance().getFactionDao().getPlayerFactionCache().put(event.getPlayerUUID(), event.getFaction().getIdentifier());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLeftFactionEvent(PlayerLeftFactionEvent event) {
        Apollo.getInstance().getFactionDao().getPlayerFactionCache().remove(event.getPlayer().get().getUniqueId());
        event.getPlayer().ifPresent(player -> {
            Tablist tablist = Apollo.getInstance().getTablistManager().getTablist(player);
            IntStream.range(0, 20).forEach(index -> tablist.getSlot(1, index).setText(""));
        });
    }

    // Cache the claimed land locations
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionClaim(FactionClaimChangedEvent event) {
        event.getAffectedClaims().forEach(claim -> Apollo.getInstance().getFactionDao().indexClaim(claim, event.getCause()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRenameMonitor(FactionRenameEvent event) {
        Apollo.getInstance().getFactionDao().getNameFactionCache().remove(event.getOriginalName());
        Apollo.getInstance().getFactionDao().getNameFactionCache().put(event.getNewName(), event.getFaction().getIdentifier());
        if (event.getFaction() instanceof KothFaction) {
            ((KothFaction) event.getFaction()).getCaptureZone().setName(event.getNewName());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionCreate(FactionCreateEvent event) {
        Faction faction = event.getFaction();
        if (faction instanceof PlayerFaction) {
            Player player = (Player) event.getSender();

            long difference = (Apollo.getInstance().getUserDao().get(player.getUniqueId()).get().getLastFactionLeaveMillis() - System.currentTimeMillis()) + FACTION_JOIN_WAIT_MILLIS;
            if (difference > 0L && !player.hasPermission("apollo.faction.commands.staff.forcejoin")) {
                event.setCancelled(true);
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must wait another " + ChatColor.AQUA + DurationFormatUtils.formatDurationWords(difference, true, true) + ChatColor.GRAY + " before joining a faction.");
            } else {
                Apollo.getInstance().getServer()
                        .broadcastMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + event.getSender().getName() + ChatColor.GRAY + " created the faction " + ChatColor.AQUA + event.getFaction().getName() + ChatColor.GRAY + '.');
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRemove(FactionRemoveEvent event) {
        if (event.getFaction() instanceof PlayerFaction) {
            Apollo.getInstance().getServer()
                    .broadcastMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + event.getSender().getName() + ChatColor.GRAY + " disbanded the faction " + ChatColor.AQUA + event.getFaction().getName() + ChatColor.GRAY + '.');
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFactionRename(FactionRenameEvent event) {
        if (event.getFaction() instanceof PlayerFaction) {
            Apollo.getInstance().getServer()
                    .broadcastMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + event.getSender().getName() + ChatColor.GRAY + " has renamed the faction " + ChatColor.AQUA + event.getOriginalName() + ChatColor.GRAY + " to " + ChatColor.AQUA + event.getNewName() + ChatColor.GRAY + '.');
        }
    }

    private long getLastLandChangedMeta(Player player) {
        MetadataValue value = ReflectionUtils.getPlayerMetadata(player, LAND_CHANGED_META_KEY, Apollo.getInstance());

        long millis = System.currentTimeMillis();
        long remaining = value == null ? 0L : value.asLong() - millis;
        if (remaining <= 0L) { // update the metadata.
            player.setMetadata(LAND_CHANGED_META_KEY, new FixedMetadataValue(Apollo.getInstance(), millis + LAND_CHANGE_MSG_THRESHOLD));
        }

        return remaining;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneEnter(CaptureZoneEnterEvent event) {
        if (getLastLandChangedMeta(event.getPlayer()) > 0L) return; // delay before re-messaging.
        event.getPlayer().sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You are now entering the capture zone: " + event.getCaptureZone().getDisplayName() + (event.getFaction() instanceof ConquestFaction ? ChatColor.GRAY + " (" + ChatColor.AQUA + event.getFaction().getName() + ChatColor.GRAY + ")." : '.'));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCaptureZoneLeave(CaptureZoneLeaveEvent event) {
        if (getLastLandChangedMeta(event.getPlayer()) > 0L) return; // delay before re-messaging.
        if (event.getFaction() instanceof ConquestFaction faction)
        event.getPlayer().sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You are now leaving the capture zone: " + event.getCaptureZone().getDisplayName() + (event.getFaction() instanceof ConquestFaction ? ChatColor.GRAY + " (" + ChatColor.AQUA + event.getFaction().getName() + ChatColor.GRAY + ")." : '.'));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onPlayerClaimEnter(PlayerClaimEnterEvent event) {
        if (event.getToFaction().isSafezone()) {
            Player player = event.getPlayer();
            player.setHealth((double) player.getMaxHealth());
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.setSaturation(4.0F);
        }

        Player player = event.getPlayer();
        if (this.getLastLandChangedMeta(player) <= 0L) { // delay before re-messaging.
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You are now entering: " + event.getToFaction().getDisplayName(player) + ChatColor.GRAY + " (" + (event.getToFaction().isDeathban() ? ChatColor.RED + "Deathban" : ChatColor.GREEN + "Safezone") + ChatColor.GRAY + ").");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerLeftFaction(PlayerLeftFactionEvent event) {
        event.getPlayer().ifPresent(player -> Apollo.getInstance().getUserDao().get(event.getPlayer().get().getUniqueId()).get().setLastFactionLeaveMillis(System.currentTimeMillis()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPreFactionJoin(PlayerJoinFactionEvent event) {
        PlayerFaction faction = event.getFaction();
        event.getPlayer().ifPresent(player -> {
            if (!Apollo.getInstance().getEotwHandler().isEndOfTheWorld() && faction.getRegenStatus() == RegenStatus.PAUSED) {
                event.setCancelled(true);
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not join a faction that has its DTR frozen.");
                return;
            }

            long difference = (Apollo.getInstance().getUserDao().get(player.getUniqueId()).get().getLastFactionLeaveMillis() - System.currentTimeMillis()) + FACTION_JOIN_WAIT_MILLIS;
            if (difference > 0L && !player.hasPermission("apollo.faction.commands.staff.forcejoin")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot join factions after just leaving within " + FACTION_JOIN_WAIT_WORDS + ". " +
                        "You gotta wait another " + DurationFormatUtils.formatDurationWords(difference, true, true) + '.');
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionLeave(PlayerLeaveFactionEvent event) {
        if (event.isForce() || event.isKick()) {
            return;
        }

        event.getPlayer().ifPresent(player -> {
            if (Apollo.getInstance().getFactionDao().getFactionAt(player.getLocation()).get().equals(event.getFaction())) {
                event.setCancelled(true);
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not leave a faction while remaining in its territory.");
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Apollo.getInstance().getFactionDao().getByPlayer(event.getPlayer().getUniqueId()).ifPresent(faction ->
            faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + faction.getMember(event.getPlayer().getUniqueId()).getRole().getName() + " Online: " + ChatColor.AQUA + event.getPlayer().getName(),
                    event.getPlayer().getUniqueId())
        );
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Apollo.getInstance().getFactionDao().getByPlayer(event.getPlayer().getUniqueId()).ifPresent(faction -> {
            faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + faction.getMember(event.getPlayer().getUniqueId()).getRole().getName() + " Offline: " + ChatColor.AQUA + event.getPlayer().getName());
        });
    }
}
