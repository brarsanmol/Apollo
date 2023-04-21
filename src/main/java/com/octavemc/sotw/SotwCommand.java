package com.octavemc.sotw;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.octavemc.Apollo;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandAlias("sotw")
@CommandPermission("apollo.commands.sotw")
public class SotwCommand extends BaseCommand {

    public SotwCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @HelpCommand
    public static void onHelpCommand(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("start|s")
    @Description("Start the Start of the World Event.")
    public static void onStartCommand(CommandSender sender, long duration) {
        if (duration < 1000L) {
            sender.sendMessage(ChatColor.RED + "SOTW protection time must last for at least 20 ticks.");
        } else if (Apollo.getInstance().getSotwTimer().getSotwRunnable() != null) {
            sender.sendMessage(ChatColor.RED + "SOTW protection is already enabled, use /sotw end to end it.");
        } else {
            Apollo.getInstance().getSotwTimer().start(duration);
            sender.sendMessage(ChatColor.RED + "Started SOTW protection for " + DurationFormatUtils.formatDurationWords(duration, true, true) + ".");
        }
    }

    @Subcommand("end|e")
    @Description("End the Start of the World Event")
    public static void onEndCommand(CommandSender sender) {
        if (Apollo.getInstance().getSotwTimer().getSotwRunnable() == null) {
            sender.sendMessage(ChatColor.RED + "SOTW protection is not active.");
        } else {
            Apollo.getInstance().getSotwTimer().cancel();
            sender.sendMessage(ChatColor.RED + "Cancelled SOTW protection.");
        }
    }
}
