package com.octavemc.timer.type;

import com.octavemc.Apollo;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.util.DurationFormatter;
import com.octavemc.util.imagemessage.ImageChar;
import com.octavemc.util.imagemessage.ImageMessage;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

/**
 * Timer used to prevent {@link Player}s from using Notch Apples too often.
 */
public class SuperGoldenAppleTimer extends PlayerTimer implements Listener {

    private ImageMessage goppleArtMessage;

    public SuperGoldenAppleTimer() {
        super("Super Golden Apple", TimeUnit.HOURS.toMillis(1L));

        if (Apollo.getInstance().getImageFolder().getGopple() != null) {
            goppleArtMessage = ImageMessage.newInstance(Apollo.getInstance().getImageFolder().getGopple(), 13, ImageChar.BLOCK.getChar()).appendText("", "",
                    ChatColor.GOLD.toString() + ChatColor.BOLD + ' ' + name + ':',
                    ChatColor.GRAY + "  Consumed",
                    ChatColor.GOLD + " Cooldown Remaining:",
                    ChatColor.GRAY + "  " + DurationFormatUtils.formatDurationWords(defaultCooldown, true, true)
            );
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        ItemStack stack = event.getItem();
        if (stack != null && stack.getType() == Material.GOLDEN_APPLE && stack.getDurability() == 1) {
            Player player = event.getPlayer();
            if (setCooldown(player, player.getUniqueId(), defaultCooldown, false, value -> false)) {

                if (goppleArtMessage != null) {
                    goppleArtMessage.sendToPlayer(player);
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Consumed " + ChatColor.GOLD + "Super Golden Apple" + ChatColor.YELLOW + ", now on a cooldown for " +
                            DurationFormatUtils.formatDurationWords(defaultCooldown, true, true));
                }
                //TODO: Testing Lunar Client (should remove).
                //LunarClientAPI.getInstance().(event.getPlayer(), new LCCooldown("Super Golden Apple", (int) this.defaultCooldown, TimeUnit.MILLISECONDS, Material.GOLDEN_APPLE));
            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You still have a " + getName() + ChatColor.RED + " cooldown for another " +
                        ChatColor.BOLD + DurationFormatter.getRemaining(getRemaining(player), true, false) + ChatColor.RED + '.');
            }
        }
    }
}
