package com.octavemc.deathban;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.user.User;
import com.octavemc.util.DelayedMessageRunnable;
import com.octavemc.util.DurationFormatter;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DeathbanListener implements Listener {

    private static final long LIFE_USE_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(30L);
    private static final String LIFE_USE_DELAY_WORDS = DurationFormatUtils.formatDurationWords(DeathbanListener.LIFE_USE_DELAY_MILLIS, true, true);
    private static final String DEATH_BAN_BYPASS_PERMISSION = "apollo.deathban.bypass";

    private final TObjectIntMap<UUID> respawnTickTasks = new TObjectIntHashMap<>();
    private final TObjectLongMap<UUID> lastAttemptedJoinMap = new TObjectLongHashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().spigot().respawn(); // Method already checks if player is dead first
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Apollo.getInstance().getUserDao().get(event.getPlayer().getUniqueId())
                .filter(User::hasDeathban)
                .ifPresent(user -> {
                    if (event.getPlayer().hasPermission(DEATH_BAN_BYPASS_PERMISSION)) {
                        informAboutDeathbanBypass(event.getPlayer(), user.getDeathban(), true);
                        user.setDeathban(null);
                        return;
                    }

                    if (user.getDeathban().isEotwDeathban()) {
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.AQUA + "" + ChatColor.BOLD + "DEATHBANNED!" + "\n"
                                + ChatColor.AQUA + "Remaining: " + ChatColor.WHITE + DurationFormatter.getRemaining(user.getDeathban().getRemaining(), true, false) + "\n"
                                + ChatColor.AQUA + "Reason: " + ChatColor.WHITE + ChatColor.stripColor(user.getDeathban().getReason()) + "\n"
                                + ChatColor.AQUA + "Location: " + ChatColor.WHITE + user.getDeathban().getDeathPoint().getBlockX() + ", " + user.getDeathban().getDeathPoint().getBlockY() + ", " + user.getDeathban().getDeathPoint().getBlockZ() + "\n"
                                + ChatColor.AQUA + "Thanks for playing! You have been deathbanned permanently for this the remainder of the map, because of EOTW!"
                        );
                    } else if (user.getLives() <= 0) {
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.AQUA + "" + ChatColor.BOLD + "DEATHBANNED!\n\n"
                                + ChatColor.AQUA + "Remaining: " + ChatColor.WHITE + DurationFormatter.getRemaining(user.getDeathban().getRemaining(), true, false) + "\n"
                                + ChatColor.AQUA + "Reason: " + ChatColor.WHITE + ChatColor.stripColor(user.getDeathban().getReason()) + "\n"
                                + ChatColor.AQUA + "Location: " + ChatColor.WHITE + user.getDeathban().getDeathPoint().getBlockX() + ", " + user.getDeathban().getDeathPoint().getBlockY() + ", " + user.getDeathban().getDeathPoint().getBlockZ() + "\n"
                                + ChatColor.AQUA + "Want to skip the deathban? Revive yourself by purchasing lives at " + ChatColor.WHITE + "https://www.octavemc.com/store"
                        );
                    } else if (lastAttemptedJoinMap.get(event.getPlayer().getUniqueId()) != lastAttemptedJoinMap.getNoEntryValue()
                            && lastAttemptedJoinMap.get(event.getPlayer().getUniqueId()) - System.currentTimeMillis() < LIFE_USE_DELAY_MILLIS) {
                        lastAttemptedJoinMap.remove(event.getPlayer().getUniqueId());
                        user.setDeathban(null);
                        user.setLives(user.getLives() - 1);

                        event.setResult(PlayerLoginEvent.Result.ALLOWED);
                        new DelayedMessageRunnable(event.getPlayer(),
                                Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You have used a life, you now have " + ChatColor.AQUA + user.getLives() + ChatColor.GRAY + " remaining.");
                    } else {
                        // The user has lives, but just in case they didn't want them to use, tell them to join again in the next 30 seconds.
                        lastAttemptedJoinMap.put(event.getPlayer().getUniqueId(), System.currentTimeMillis() + LIFE_USE_DELAY_MILLIS);
                        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ChatColor.AQUA + "" + ChatColor.BOLD + "DEATHBANNED!\n\n"
                                + ChatColor.AQUA + "Remaining: " + ChatColor.WHITE + DurationFormatter.getRemaining(user.getDeathban().getRemaining(), true, false) + "\n"
                                + ChatColor.AQUA + "Reason: " + ChatColor.WHITE + ChatColor.stripColor(user.getDeathban().getReason()) + "\n"
                                + ChatColor.AQUA + "Location: " + ChatColor.WHITE + user.getDeathban().getDeathPoint().getBlockX() + ", " + user.getDeathban().getDeathPoint().getBlockY() + ", " + user.getDeathban().getDeathPoint().getBlockZ() + "\n"
                                + ChatColor.AQUA + "You have " + user.getLives() + " lives\n"
                                + ChatColor.AQUA + "Reconnect within " + ChatColor.BOLD + LIFE_USE_DELAY_WORDS + ChatColor.AQUA + " to revive yourself.");
                    }
                });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Deathban deathban = Apollo.getInstance().getDeathbanManager().applyDeathBan(player, event.getDeathMessage());
        long remaining = deathban.getRemaining();
        if (remaining <= 0L || player.hasPermission(DeathbanListener.DEATH_BAN_BYPASS_PERMISSION)) {
            return;
        }

        long ticks = Configuration.DEATHBAN_RESPAWN_SCREEN_TICKS_BEFORE_KICK;

        if (ticks <= 0L || remaining < ticks) {
            handleKick(player, deathban);
            return;
        }

        // Let the player see the death screen for x seconds
        respawnTickTasks.put(player.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                respawnTickTasks.remove(player.getUniqueId());
                handleKick(player, deathban);
            }
        }.runTaskLater(Apollo.getInstance(), ticks).getTaskId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRequestRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Apollo.getInstance().getUserDao().get(player.getUniqueId()).ifPresent(user -> {
            if (user.getDeathban() != null
                    && user.getDeathban().getRemaining() > 0L
                    && !player.hasPermission(DEATH_BAN_BYPASS_PERMISSION)) {
                handleKick(player, user.getDeathban());
            } else {
                cancelRespawnKickTask(player);
                informAboutDeathbanBypass(player, user.getDeathban(), false);
                user.setDeathban(null);
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelRespawnKickTask(event.getPlayer());
    }

    private static void informAboutDeathbanBypass(Player player, Deathban deathban, boolean later) {
        var message = Configuration.WARNING_MESSAGE_PREFIX + ChatColor.GRAY + "You have bypassed your deathban.";
        if (later) {
            new DelayedMessageRunnable(player, message);
        } else {
            player.sendMessage(message);
        }
    }

    private void cancelRespawnKickTask(Player player) {
        int taskId = respawnTickTasks.remove(player.getUniqueId());
        if (taskId != respawnTickTasks.getNoEntryValue()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    private void handleKick(Player player, Deathban deathban) {
        if (Apollo.getInstance().getEotwHandler().isEndOfTheWorld()) {
            player.kickPlayer(ChatColor.AQUA + "" + ChatColor.BOLD + "DEATHBANNED!\n\n"
                    + ChatColor.AQUA + "Remaining: " + ChatColor.WHITE + DurationFormatter.getRemaining(deathban.getRemaining(), true) + "\n"
                    + ChatColor.AQUA + "Reason: " + ChatColor.WHITE + ChatColor.stripColor(deathban.getReason()) + "\n"
                    + ChatColor.AQUA + "Location: " + ChatColor.WHITE + deathban.getDeathPoint().getBlockX() + ", " + deathban.getDeathPoint().getBlockY() + ", " + deathban.getDeathPoint().getBlockZ() + "\n"
                    + ChatColor.AQUA + "Thanks for playing! You have been deathbanned permanently for this the remainder of the map, because of EOTW!"
            );
        } else {
            player.kickPlayer(ChatColor.AQUA + "" + ChatColor.BOLD + "DEATHBANNED!\n\n"
                    + ChatColor.AQUA + "Remaining: " + ChatColor.WHITE + DurationFormatter.getRemaining(deathban.getRemaining(), true) + "\n"
                    + ChatColor.AQUA + "Reason: " + ChatColor.WHITE + ChatColor.stripColor(deathban.getReason()) + "\n"
                    + ChatColor.AQUA + "Location: " + ChatColor.WHITE + deathban.getDeathPoint().getBlockX() + ", " + deathban.getDeathPoint().getBlockY() + ", " + deathban.getDeathPoint().getBlockZ() + "\n"
            );
        }
    }
}
