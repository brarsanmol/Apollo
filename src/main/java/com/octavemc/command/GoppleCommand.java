package com.octavemc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import com.octavemc.Apollo;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.util.DurationFormatter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Command used to check remaining Notch Apple cooldown time for {@link Player}.
 */
public final class GoppleCommand extends BaseCommand {

    public GoppleCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @CommandAlias("apple")
    public static void onCommand(Player player) {
        PlayerTimer timer = Apollo.getInstance().getTimerManager().getSuperGoldenAppleTimer();
        if (timer.getRemaining(player) <= 0L) {
            player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" + ChatColor.DARK_RED + ChatColor.BOLD +  "!" + ChatColor.DARK_GRAY + ChatColor.BOLD + ") " + ChatColor.GRAY + "You currently do not have a super golden apple timer.");
        } else {
            player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" + ChatColor.AQUA + ChatColor.BOLD +  "!" + ChatColor.DARK_GRAY + ChatColor.BOLD + ") " + ChatColor.GRAY + "You have " + ChatColor.AQUA + DurationFormatter.getRemaining(timer.getRemaining(player), true, false) + ChatColor.GRAY + " remaining on your " + ChatColor.AQUA + "super golden apple" + ChatColor.GRAY + " timer.");
        }
    }
}
