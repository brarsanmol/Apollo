package com.octavemc.timer.type;

import com.octavemc.Configuration;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.util.DurationFormatter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.concurrent.TimeUnit;

public class GoldenAppleTimer extends PlayerTimer implements Listener {

    public GoldenAppleTimer() {
        super("Golden Apple", TimeUnit.SECONDS.toMillis(6L));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (event.getItem() != null && event.getItem().getType() == Material.GOLDEN_APPLE && event.getItem().getDurability() == 0) {
            Player player = event.getPlayer();
            if (getRemaining(player) > 0L) {
                event.setCancelled(true);
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not consume a " + ChatColor.AQUA + "golden apple"
                        + ChatColor.GRAY + " for another "
                        + ChatColor.AQUA + DurationFormatter.getRemaining(this.getRemaining(player), true, false) + ChatColor.GRAY + '.');
            } else {
                setCooldown(player, player.getUniqueId(), defaultCooldown, false, value -> false);
                //TODO: Testing Lunar Client (should remove).
                //LunarClientAPI.getInstance().sendCooldown(event.getPlayer(), new LCCooldown("Golden Apple", this.defaultCooldown, TimeUnit.MILLISECONDS, Material.GOLDEN_APPLE));
            }
        }
    }

}
