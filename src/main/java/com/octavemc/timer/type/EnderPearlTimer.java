package com.octavemc.timer.type;

import com.octavemc.Configuration;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.util.DurationFormatter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EnderPearlTimer extends PlayerTimer implements Listener {

    public EnderPearlTimer() {
        super("Enderpearl", TimeUnit.SECONDS.toMillis(12L));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearCooldown(event.getPlayer(), event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        clearCooldown(event.getPlayer(), event.getPlayer().getUniqueId());
    }

    public void refund(Player player) {
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
        clearCooldown(player, player.getUniqueId());
    }

    @EventHandler
    public void onProjectileLaunchEvent(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof EnderPearl && event.getEntity().getShooter() instanceof Player shooter) {
            if (this.getRemaining(shooter) > 0L) {
                shooter.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not throw a " + ChatColor.AQUA + "enderpearl"
                        + ChatColor.GRAY + " for another "
                        + ChatColor.AQUA + DurationFormatter.getRemaining(this.getRemaining(shooter), true, false) + ChatColor.GRAY + '.');
                shooter.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
                event.setCancelled(true);
            } else {
                this.setCooldown(shooter, shooter.getUniqueId(), defaultCooldown, true);
                //TODO: Testing Lunar Client (should remove).
                //LunarClientAPI.getInstance().sendCooldown(shooter, new LCCooldown("Enderpearl", this.defaultCooldown, TimeUnit.MILLISECONDS, Material.ENDER_PEARL));
            }
        }
    }

    @Override
    public void handleExpiry(@Nullable Player player, UUID playerUUID) {
        clearCooldown(player, playerUUID);
    }
}
