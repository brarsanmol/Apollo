package com.octavemc.timer.type;

import com.google.common.base.Preconditions;
import com.octavemc.Apollo;
import com.octavemc.pvpclass.PvpClass;
import com.octavemc.pvpclass.event.PlayerArmorEquipEvent;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.timer.TimerCooldown;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Timer that handles {@link PvpClass} warmups.
 */
public class PvpClassWarmupTimer extends PlayerTimer implements Listener {

    protected final Map<UUID, PvpClass> classWarmups = new HashMap<>();

    public PvpClassWarmupTimer() {
        super("Class Warmup", TimeUnit.SECONDS.toMillis(5L), false);

        // Re-equip the applicable class for every player during reloads.
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    attemptEquip(player);
                }
            }
        }.runTaskLater(Apollo.getInstance(), 10L);
    }

    public void onDisable() {
        classWarmups.clear();
    }

    @Override
    public TimerCooldown clearCooldown(@Nullable Player player, UUID playerUUID) {
        TimerCooldown runnable = super.clearCooldown(player, playerUUID);

        if (runnable != null) classWarmups.remove(playerUUID);
        return runnable;
    }

    @Override
    public void handleExpiry(@Nullable Player player, UUID userUUID) {
        PvpClass pvpClass = classWarmups.remove(userUUID);
        if (player != null) {
            Preconditions.checkNotNull(pvpClass, "Attempted to equip a class for %s, but nothing was added", player.getName());
            Apollo.getInstance().getPvpClassManager().setEquippedClass(player, pvpClass);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerQuitEvent event) {
        Apollo.getInstance().getPvpClassManager().setEquippedClass(event.getPlayer(), null);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        attemptEquip(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEquipmentSet(PlayerArmorEquipEvent event) {
        attemptEquip(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onArmorEquipmentEvent(PlayerArmorEquipEvent event) {
        attemptEquip(event.getPlayer());
    }

    private void attemptEquip(Player player) {
        PvpClass current = Apollo.getInstance().getPvpClassManager().getEquippedClass(player);
        if (current != null) {
            if (current.isApplicableFor(player)) return;
            Apollo.getInstance().getPvpClassManager().setEquippedClass(player, null);
        } else if ((current = classWarmups.get(player.getUniqueId())) != null) {
            if (current.isApplicableFor(player)) return;
            clearCooldown(player);
        }
        Apollo.getInstance().getPvpClassManager().getPvpClasses().stream()
                .filter(clazz -> clazz.isApplicableFor(player)).findFirst()
                .ifPresent(clazz -> {
                    classWarmups.put(player.getUniqueId(), clazz);
                    setCooldown(player, player.getUniqueId(), clazz.getWarmupDelay(), false);
                });
    }
}
