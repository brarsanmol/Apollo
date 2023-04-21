package com.octavemc.timer;

import com.octavemc.timer.event.TimerClearEvent;
import com.octavemc.timer.event.TimerExtendEvent;
import com.octavemc.timer.event.TimerPauseEvent;
import com.octavemc.timer.event.TimerStartEvent;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Represents a {@link Player} {@link Timer} countdown.
 */
public abstract class PlayerTimer extends Timer {

    protected final boolean persistable;
    protected final Map<UUID, TimerCooldown> cooldowns = new ConcurrentHashMap<>();

    public PlayerTimer(String name, long defaultCooldown) {
        this(name, defaultCooldown, true);
    }

    public PlayerTimer(String name, long defaultCooldown, boolean persistable) {
        super(name, defaultCooldown);
        this.persistable = persistable;
    }

    /**
     * Handles what happens when this {@link PlayerTimer} expires for a user.
     * <p>Extending this requires calling the super first</p>
     *
     * @param player     the {@link Player} if online, otherwise null
     * @param playerUUID the UUID of user to handle for
     */
    protected void handleExpiry(@Nullable Player player, UUID playerUUID) {
        cooldowns.remove(playerUUID);
    }

    public TimerCooldown clearCooldown(UUID uuid) {
        return clearCooldown(null, uuid);
    }

    public TimerCooldown clearCooldown(@NonNull Player player) {
        return clearCooldown(player, player.getUniqueId());
    }

    public TimerCooldown clearCooldown(@Nullable Player player, UUID playerUUID) {
        TimerCooldown runnable = cooldowns.remove(playerUUID);
        if (runnable != null) {
            runnable.cancel();
            if (player == null) Bukkit.getPluginManager().callEvent(new TimerClearEvent(playerUUID, this));
            else Bukkit.getPluginManager().callEvent(new TimerClearEvent(player, this));
        }
        return runnable;
    }

    public boolean isPaused(Player player) {
        return isPaused(player.getUniqueId());
    }

    public boolean isPaused(UUID playerUUID) {
        TimerCooldown runnable = cooldowns.get(playerUUID);
        return runnable != null && runnable.isPaused();
    }

    public void setPaused(UUID playerUUID, boolean paused) {
        TimerCooldown runnable = cooldowns.get(playerUUID);
        if (runnable != null && runnable.isPaused() != paused) {
            TimerPauseEvent event = new TimerPauseEvent(playerUUID, this, paused);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) runnable.setPaused(paused);
        }
    }

    public long getRemaining(Player player) {
        return getRemaining(player.getUniqueId());
    }

    public long getRemaining(UUID playerUUID) {
        TimerCooldown runnable = cooldowns.get(playerUUID);
        return runnable == null ? 0L : runnable.getRemaining();
    }

    public boolean setCooldown(@Nullable Player player, UUID playerUUID) {
        return setCooldown(player, playerUUID, defaultCooldown, false);
    }

    public boolean setCooldown(@Nullable Player player, UUID playerUUID, boolean overwrite) {
        return setCooldown(player, playerUUID, defaultCooldown, overwrite, null);
    }

    public boolean setCooldown(@Nullable Player player, UUID playerUUID, long duration, boolean overwrite) {
        return setCooldown(player, playerUUID, duration, overwrite, null);
    }

    /**
     * @return true if cooldown was set or changed
     */
    public boolean setCooldown(@Nullable Player player, UUID playerUUID, long duration, boolean overwrite, @Nullable Predicate<Long> currentCooldownPredicate) {
        TimerCooldown runnable = duration > 0L ? cooldowns.get(playerUUID) : clearCooldown(player, playerUUID);
        if (runnable != null) {
            long remaining = runnable.getRemaining();
            if (!overwrite && remaining > 0L && duration <= remaining) return false;

            TimerExtendEvent event = new TimerExtendEvent(player, playerUUID, this, remaining, duration);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;

            boolean flag = currentCooldownPredicate != null ? currentCooldownPredicate.test(remaining) : true;
            if (flag) runnable.setRemaining(duration);

            return flag;
        } else {
            Bukkit.getPluginManager().callEvent(new TimerStartEvent(player, playerUUID, this, duration));
            runnable = new TimerCooldown(this, playerUUID, duration);
        }

        cooldowns.put(playerUUID, runnable);
        return true;
    }

}