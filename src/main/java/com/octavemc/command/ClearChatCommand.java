package com.octavemc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.octavemc.Apollo;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ClearChatCommand extends BaseCommand {

    public static final String BLANK_LINE = StringUtils.repeat(" ", 256);

    public ClearChatCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @CommandAlias("clearchat|cc")
    @Description("Clear the player chat box.")
    @CommandPermission("apollo.commands.clearchat")
    public static void onClearChat(CommandSender sender) {
        for (int index = 0; index < 121; index++) {
            Apollo.getInstance().getServer().broadcastMessage(BLANK_LINE);
        }
        sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" +  ChatColor.BOLD + ChatColor.AQUA + "!" + ChatColor.BOLD + ChatColor.DARK_GRAY + ") " + ChatColor.GRAY + "Chat has been cleared by " + ChatColor.AQUA + sender.getName());
    }

}
