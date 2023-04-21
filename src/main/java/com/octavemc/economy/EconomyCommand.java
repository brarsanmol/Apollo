package com.octavemc.economy;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

/**
 * Command used to check a players' balance.
 */
@CommandAlias("economy|eco")
@CommandPermission("apollo.commands.economy")
public class EconomyCommand extends BaseCommand {

    public EconomyCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @HelpCommand
    public static void onHelpCommand(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("give|g")
    @CommandCompletion("@players")
    public static void onGiveCommand(CommandSender sender, OfflinePlayer target, int amount) {
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A player with that name was not found.");
        } else if (amount <= 0) {
            sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Money given must be in " + ChatColor.AQUA + "positive" + ChatColor.GRAY + " quantities.");
        } else {
            Apollo.getInstance().getUserDao().get(target.getUniqueId()).ifPresent(user -> user.setBalance(user.getBalance() + amount));
            target.getPlayer().sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + " has given you " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + amount + ChatColor.GRAY + '.');
            sender.sendMessage(new String[] {
                    Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Gave " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + amount + ChatColor.GRAY + " to balance of " + ChatColor.AQUA + target.getName() + ChatColor.GRAY + ".",
                    Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Balance of " + ChatColor.AQUA + target.getName() + ChatColor.GRAY + " is now " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + Apollo.getInstance().getUserDao().get(target.getUniqueId()).get().getBalance() + ChatColor.GRAY + "."
            });
        }
    }

    @Subcommand("take|t")
    @CommandCompletion("@players")
    public static void onTakeCommand(CommandSender sender, OfflinePlayer target, int amount) {
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A player with that name was not found.");
        } else if (amount <= 0) {
            sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Money taken must be in " + ChatColor.AQUA + "positive" + ChatColor.GRAY + " quantities.");
        } else {
            Apollo.getInstance().getUserDao().get(target.getUniqueId()).ifPresent(user -> user.setBalance(user.getBalance() - amount));
            target.getPlayer().sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + " has taken you " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + amount + ChatColor.GRAY + '.');
            sender.sendMessage(new String[] {
                    Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Took " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + amount + ChatColor.GRAY + " from balance of " + ChatColor.AQUA + target.getName() + ChatColor.GRAY + ".",
                    Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Balance of " + ChatColor.AQUA + target.getName() + ChatColor.GRAY + " is now " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + Apollo.getInstance().getUserDao().get(target.getUniqueId()).get().getBalance() + ChatColor.GRAY + "."
            });
        }
    }

    @Subcommand("set|s")
    @CommandCompletion("@players")
    public static void onSetCommand(CommandSender sender, OfflinePlayer target, int amount) {
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A player with that name could not be found.");
        } else if (amount <= 0) {
            sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Money set must be in " + ChatColor.AQUA + "positive" + ChatColor.GRAY + " quantities.");
        } else {
            Apollo.getInstance().getUserDao().get(target.getUniqueId()).ifPresent(user -> user.setBalance(amount));
            target.getPlayer().sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + " has set your balance to " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + amount + ChatColor.GRAY + '.');
            sender.sendMessage(ChatColor.YELLOW + "Set balance of " + target.getName() + " to " + Configuration.ECONOMY_SYMBOL + amount + '.');
        }
    }
}
