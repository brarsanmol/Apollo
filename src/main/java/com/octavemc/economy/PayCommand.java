package com.octavemc.economy;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Command used to pay other {@link Player}s some money.
 */
public final class PayCommand extends BaseCommand {


    public PayCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @CommandAlias("pay")
    @Description("Pay another player money.")
    public static void onCommand(Player player, OfflinePlayer target, int amount) {
        if (!target.hasPlayedBefore()) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A player with that name was not found.");
        } else if (target.getPlayer().equals(player)) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You can not send money to yourself.");
        } else if (amount <= 10) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must send at least " + ChatColor.AQUA + "$10" + ChatColor.GRAY + ".");
        } else if (Apollo.getInstance().getUserDao().get(player.getUniqueId()).get().getBalance() < amount) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You tried to pay " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + amount + ChatColor.GRAY + ", but you only have " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + Apollo.getInstance().getUserDao().get(player.getUniqueId()).get().getBalance() + ChatColor.AQUA + ".");
        } else {
            Apollo.getInstance().getUserDao().get(player.getUniqueId()).ifPresent(user -> user.setBalance(user.getBalance() - amount));
            Apollo.getInstance().getUserDao().get(target.getUniqueId()).ifPresent(user -> user.setBalance(user.getBalance() + amount));
            target.getPlayer().sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + player.getName() + ChatColor.GRAY + " has sent you " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + amount + ChatColor.GRAY + '.');
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You have sent " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + amount + ChatColor.GRAY + " to " + ChatColor.AQUA + target.getName() + ChatColor.GRAY + '.');
        }
    }
}
