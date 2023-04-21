package com.octavemc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.timer.type.LogoutTimer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class LogoutCommand extends BaseCommand {

    public LogoutCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @CommandAlias("logout|l")
    public static void onLogout(Player player) {
        LogoutTimer timer = Apollo.getInstance().getTimerManager().getLogoutTimer();

        if (!timer.setCooldown(player, player.getUniqueId(), false)) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your " + ChatColor.AQUA + "logout" + ChatColor.GRAY + " timer is already active!");
        } else {
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Your " + ChatColor.AQUA + "logout" + ChatColor.GRAY + " timer has started!");
        }
    }
}
