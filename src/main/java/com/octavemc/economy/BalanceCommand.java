package com.octavemc.economy;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class BalanceCommand extends BaseCommand {

    public BalanceCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @CommandAlias("balance|bal")
    @Description("")
    public static void onCommand(Player player, @Optional OfflinePlayer target) {
        if (target == null) {
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + "Your Balance: " + ChatColor.WHITE + Configuration.ECONOMY_SYMBOL + Apollo.getInstance().getUserDao().get(player.getUniqueId()).get().getBalance());
        } else if (!target.hasPlayedBefore()) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A player with that name was not found.");
        } else {
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + target.getName() + "'s Balance: " + ChatColor.WHITE + Configuration.ECONOMY_SYMBOL + Apollo.getInstance().getUserDao().get(target.getUniqueId()).get().getBalance());
        }
    }

}
