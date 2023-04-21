package com.octavemc.timer.type;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.event.PlayerClaimEnterEvent;
import com.octavemc.faction.event.PlayerJoinFactionEvent;
import com.octavemc.faction.event.PlayerLeaveFactionEvent;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.timer.TimerCooldown;
import com.octavemc.timer.event.TimerStartEvent;
import com.octavemc.util.BukkitUtils;
import com.octavemc.util.DurationFormatter;
import com.octavemc.visualise.VisualType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Timer used to tag {@link Player}s in combat to prevent entering safe-zones.
 */
public final class CombatTimer extends PlayerTimer implements Listener {

    public CombatTimer() {
        super("Combat Tag", TimeUnit.SECONDS.toMillis(35L));
    }

    @Override
    public TimerCooldown clearCooldown(@Nullable Player player, UUID playerUUID) {
        TimerCooldown cooldown = super.clearCooldown(player, playerUUID);
        if (cooldown != null && player != null) {
            Apollo.getInstance().getVisualiseHandler().clearVisualBlocks(player, VisualType.SPAWN_BORDER, null);
        }

        return cooldown;
    }

    @Override
    public void handleExpiry(@Nullable Player player, UUID playerUUID) {
        super.handleExpiry(player, playerUUID);
        if (player != null) {
            Apollo.getInstance().getVisualiseHandler().clearVisualBlocks(player, VisualType.SPAWN_BORDER, null);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionJoin(PlayerJoinFactionEvent event) {
        event.getPlayer()
                .filter(player -> getRemaining(player) > 0L)
                .ifPresent(player -> {
                    event.setCancelled(true);
                    player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not join a faction as you are currently combat tagged for another " + ChatColor.AQUA + DurationFormatter.getRemaining(getRemaining(player), true, false) + ChatColor.GRAY + '.');
                });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onFactionLeave(PlayerLeaveFactionEvent event) {
        if (event.isForce()) return;
        event.getPlayer()
                .filter(player -> getRemaining(player) > 0L)
                .ifPresent(player -> {
                    event.setCancelled(true);
                    if (event.getSender() == player) event.getSender().sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not kick " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + " as they are currently combat tagged for another " + ChatColor.AQUA + DurationFormatter.getRemaining(getRemaining(player), true, false) + ChatColor.GRAY + '.');
                    else event.getSender().sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not leave " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + " as you are currently combat tagged for another " + ChatColor.AQUA + DurationFormatter.getRemaining(getRemaining(player), true, false) + ChatColor.GRAY + '.');
                });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPreventClaimEnter(PlayerClaimEnterEvent event) {
        if (event.getEnterCause() == PlayerClaimEnterEvent.EnterCause.TELEPORT) return;
        if (!event.getFromFaction().isSafezone() && event.getToFaction().isSafezone() && getRemaining(event.getPlayer()) > 0L) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not enter the territory of "
                    + ChatColor.AQUA + event.getToFaction().getDisplayName(event.getPlayer())
                    + ChatColor.GRAY + " while combat tagged for another "
                    + ChatColor.AQUA + DurationFormatter.getRemaining(getRemaining(event.getPlayer()), true, false) + ChatColor.GRAY + '.');
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player attacker = BukkitUtils.getFinalAttacker(event, true);
        if (attacker != null && event.getEntity() instanceof Player attacked) {
            setCooldown(attacker, attacker.getUniqueId(), defaultCooldown, false);
            setCooldown(attacked, attacked.getUniqueId(), defaultCooldown, false);
            //TODO: Testing Lunar Client (should remove).
            //LunarClientAPI.getInstance().sendCooldown(attacker, new LCCooldown("Combat Tag", this.defaultCooldown, TimeUnit.MILLISECONDS, Material.DIAMOND_SWORD));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTimerStart(TimerStartEvent event) {
        if (event.getTimer() == this)
            event.getPlayer().ifPresent(player -> player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You are now combat tagged."));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        clearCooldown(event.getPlayer(), event.getPlayer().getUniqueId());
    }

    @Override
    public boolean setCooldown(@Nullable Player player, UUID playerUUID, long duration, boolean overwrite, @Nullable Predicate<Long> currentCooldownPredicate) {
        if (player != null && Apollo.getInstance().getFactionDao().getFactionAt(player.getLocation()).get().isSafezone()) return false;
        return super.setCooldown(player, playerUUID, duration, overwrite, currentCooldownPredicate);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPreventClaimEnterMonitor(PlayerClaimEnterEvent event) {
        if (event.getEnterCause() == PlayerClaimEnterEvent.EnterCause.TELEPORT
                && (!event.getFromFaction().isSafezone() && event.getToFaction().isSafezone())) {
            clearCooldown(event.getPlayer(), event.getPlayer().getUniqueId());
        }
    }
}
