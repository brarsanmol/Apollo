package com.octavemc.timer.type;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.event.FactionClaimChangedEvent;
import com.octavemc.faction.event.PlayerClaimEnterEvent;
import com.octavemc.faction.event.cause.ClaimChangeCause;
import com.octavemc.faction.type.ClaimableFaction;
import com.octavemc.faction.type.Faction;
import com.octavemc.faction.type.PlayerFaction;
import com.octavemc.faction.type.RoadFaction;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.timer.TimerCooldown;
import com.octavemc.timer.event.TimerClearEvent;
import com.octavemc.util.BukkitUtils;
import com.octavemc.util.DurationFormatter;
import com.octavemc.visualise.VisualType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Timer used to apply PVP Protection to {@link Player}s.
 */
public class InvincibilityTimer extends PlayerTimer implements Listener {

    private static final String PVP_COMMAND = "/invincibility remove";

    public InvincibilityTimer() {
        super("Invincibility", TimeUnit.MINUTES.toMillis(30L));
    }

    @Override
    public void handleExpiry(@Nullable Player player, UUID playerUUID) {
        if (player != null)
            Apollo.getInstance().getVisualiseHandler().clearVisualBlocks(player, VisualType.CLAIM_BORDER, null);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTimerClear(TimerClearEvent event) {
        if (event.getTimer() == this)
            event.getPlayer().ifPresent(player -> Apollo.getInstance().getVisualiseHandler().clearVisualBlocks(player, VisualType.CLAIM_BORDER, null));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onClaimChange(FactionClaimChangedEvent event) {
        if (event.getCause() == ClaimChangeCause.CLAIM) {
            for (var claim : event.getAffectedClaims()) {
                if (claim.getPlayers().isEmpty()) continue;
                Location location = new Location(claim.getWorld(), claim.getMinimumX() - 1, 0, claim.getMinimumZ() - 1);
                location = BukkitUtils.getHighestLocation(location, location);
                for (var player : claim.getPlayers()) {
                    if (getRemaining(player) > 0L && player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN))
                        player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Someone claimed land where you were standing, and you were teleported away because of your invincibility timer.");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (setCooldown(event.getPlayer(), event.getPlayer().getUniqueId(), defaultCooldown, true)) {
            setPaused(event.getPlayer().getUniqueId(), true);
            event.getPlayer().sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You now have invincibility.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        clearCooldown(event.getEntity());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        long remaining = getRemaining(player);
        if (remaining > 0L) {
            event.setCancelled(true);
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + "You can not perform this action for another " + ChatColor.AQUA + DurationFormatter.getRemaining(remaining, true, false)
                    + ChatColor.GRAY + "because you have " + ChatColor.AQUA + "invincibility" + ChatColor.GRAY + '.');
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        long remaining = getRemaining(player);
        if (remaining > 0L) {
            event.setCancelled(true);
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + "You can not perform this action for another " + ChatColor.AQUA + DurationFormatter.getRemaining(remaining, true, false)
                    + ChatColor.GRAY + "because you have " + ChatColor.AQUA + "invincibility" + ChatColor.GRAY + '.');
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        TimerCooldown runnable = cooldowns.get(event.getPlayer().getUniqueId());
        if (runnable != null && runnable.getRemaining() > 0L) {
            runnable.setPaused(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            if (canApply() && setCooldown(player, player.getUniqueId(), defaultCooldown, true)) {
                setPaused(player.getUniqueId(), true);
                player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You now have invincibility.");
            }
        } else {
            // If a player has their timer paused and they are not in a safezone, un-pause for them.
            // We do this because disconnection pauses PVP Protection.
            if (isPaused(player) && getRemaining(player) > 0L && !Apollo.getInstance().getFactionDao().getFactionAt(event.getSpawnLocation()).get().isSafezone()) {
                setPaused(player.getUniqueId(), false);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerClaimEnterMonitor(PlayerClaimEnterEvent event) {
        Player player = event.getPlayer();
        if (event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
            clearCooldown(player);
            return;
        }

        boolean flag = getRemaining(player.getUniqueId()) > 0L;
        if (flag) {
            Faction toFaction = event.getToFaction();
            Faction fromFaction = event.getFromFaction();
            if (fromFaction.isSafezone() && !toFaction.isSafezone()) {
                setPaused(player.getUniqueId(), false);
            } else if (!fromFaction.isSafezone() && toFaction.isSafezone()) {
                setPaused(player.getUniqueId(), true);
            }

            if (event.getEnterCause() == PlayerClaimEnterEvent.EnterCause.TELEPORT) {
                // Allow player to enter own claim, but just remove PVP Protection when teleporting.
                PlayerFaction playerFaction; // lazy-load
                if (toFaction instanceof PlayerFaction && (playerFaction = Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).get()) != null && playerFaction == toFaction) {
                    player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You no longer have invincibility because you entered your own territory.");
                    clearCooldown(player);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerClaimEnter(PlayerClaimEnterEvent event) {
        Player player = event.getPlayer();
        Faction toFaction = event.getToFaction();
        long remaining; // lazy load
        if (toFaction instanceof ClaimableFaction && (remaining = getRemaining(player)) > 0L) {
            if (!toFaction.isSafezone() && !(toFaction instanceof RoadFaction)) {
                event.setCancelled(true);
                player.sendMessage(new String[] {
                        Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not perform this action for another " + ChatColor.AQUA + DurationFormatter.getRemaining(remaining, true, false)
                        + ChatColor.GRAY + "because you have " + ChatColor.AQUA + "invincibility" + ChatColor.GRAY + '.',
                        Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Use the command " + ChatColor.AQUA + PVP_COMMAND + ChatColor.GRAY + " to remove your invincibility."
                        });
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player attacker = BukkitUtils.getFinalAttacker(event, true);
            if (attacker == null) {
                return;
            }

            long remaining;
            Player player = (Player) entity;
            if ((remaining = getRemaining(player)) > 0L) {
                event.setCancelled(true);
                player.sendMessage(
                        Configuration.DANGER_MESSAGE_PREFIX + "You can not perform this action for another " + ChatColor.AQUA + DurationFormatter.getRemaining(remaining, true, false)
                                + ChatColor.GRAY + "because " + ChatColor.AQUA + player.getName() + ChatColor.AQUA + " has " + ChatColor.AQUA + "invincibility" + ChatColor.GRAY + '.');

                return;
            }

            if ((remaining = getRemaining(attacker)) > 0L) {
                event.setCancelled(true);
                player.sendMessage(new String[] {
                        Configuration.DANGER_MESSAGE_PREFIX + "You can not perform this action for another " + ChatColor.AQUA + DurationFormatter.getRemaining(remaining, true, false)
                                + ChatColor.GRAY + "because you have " + ChatColor.AQUA + "invincibility" + ChatColor.GRAY + '.',
                        Configuration.PRIMARY_MESSAGE_PREFIX + "Use the command " + ChatColor.AQUA + PVP_COMMAND + ChatColor.GRAY + " to remove your invincibility."
                });
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.getPotion().getShooter() instanceof Player && BukkitUtils.isDebuff(event.getPotion()))
            event.getAffectedEntities().stream()
                    .filter(entity -> entity instanceof Player player && getRemaining(player) > 0L)
                    .forEach(entity -> event.setIntensity(entity, 0));
    }

    @Override
    public boolean setCooldown(@Nullable Player player, UUID playerUUID, long duration, boolean overwrite, @Nullable Predicate<Long> callback) {
        return canApply() && super.setCooldown(player, playerUUID, duration, overwrite, callback);
    }

    private boolean canApply() {
        return !Apollo.getInstance().getEotwHandler().isEndOfTheWorld() && Apollo.getInstance().getSotwTimer().getSotwRunnable() == null;
    }
}
