package com.octavemc.deathban;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.google.common.base.Strings;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.DateTimeFormats;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;

@CommandAlias("deathbans")
@CommandPermission("apollo.commands.deathbans")
public class DeathbansCommand extends BaseCommand {

    public DeathbansCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @HelpCommand
    public static void onHelpCommand(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("clear")
    @CommandPermission("apollo.commands.deathbans.clear")
    @Description("Clear all active deathbans.")
    public static void onClear(CommandSender sender) {
        CompletableFuture.runAsync(() -> {
            Apollo.getInstance().getUserDao().getAll().forEach(user -> user.setDeathban(null));
            Apollo.getInstance().getUserDao().getCache().values().forEach(user -> user.setDeathban(null));
        });
        Apollo.getInstance().getServer().broadcast(Configuration.WARNING_MESSAGE_PREFIX + ChatColor.GRAY + "All deathbans have been cleared by " + sender.getName(), "apollo.commands.deathbans.clear");
    }

    @Subcommand("check")
    @CommandCompletion("@players")
    @CommandPermission("apollo.commands.deathbans.check")
    @Description("Check the information for a player's deathban.")
    public static void onCheck(CommandSender sender, OfflinePlayer target) {
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A player with that name was not found.");
            return;
        }
        Apollo.getInstance().getUserDao().get(target.getUniqueId()).ifPresent(user -> {
            if (user.getDeathban() != null && user.getDeathban().isActive()) {
                sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.AQUA + target.getName() + ChatColor.GRAY + " does not have active deathban.");
            } else {
                Deathban deathban = user.getDeathban();
                sender.sendMessage(ChatColor.GRAY + "Deathban Information: " + ChatColor.AQUA + target.getName());
                sender.sendMessage(ChatColor.GRAY + "Time: " + ChatColor.AQUA + DateTimeFormats.HR_MIN.format(deathban.getCreationMillis()));
                sender.sendMessage(ChatColor.GRAY + "Duration: " + ChatColor.AQUA + DurationFormatUtils.formatDurationWords(deathban.getInitialDuration(), true, true));
                sender.sendMessage(ChatColor.GRAY + "Location: (" + ChatColor.AQUA + deathban.getDeathPoint().getBlockX() + ", " + deathban.getDeathPoint().getBlockY() + ", " + deathban.getDeathPoint().getBlockZ() + ") - " + deathban.getDeathPoint().getWorld().getName());
                sender.sendMessage(ChatColor.GRAY + "Reason: ["  + ChatColor.AQUA + Strings.nullToEmpty(deathban.getReason()) + ChatColor.GRAY + "]");
            }
        });
    }

    @Subcommand("remove")
    @CommandPermission("apollo.commands.deathbans.remove")
    @Description("Remove a player's deathban.")
    public static void onRemove(CommandSender sender, OfflinePlayer target) {
        if (!target.hasPlayedBefore()) {
            sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A player with that name was not found.");
            return;
        }
        Apollo.getInstance().getUserDao().get(target.getUniqueId()).ifPresent(user -> {
            if (user.getDeathban() != null && user.getDeathban().isActive()) {
                sender.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.AQUA + target.getName() + ChatColor.GRAY + " does not have active deathban.");
            } else {
                Apollo.getInstance().getUserDao().get(target.getUniqueId()).get().setDeathban(null);
                Apollo.getInstance().getServer().broadcast(Configuration.WARNING_MESSAGE_PREFIX + ChatColor.AQUA + sender.getName() + ChatColor.GRAY + " has removed the deathban for " + ChatColor.AQUA + target.getName() + ChatColor.GRAY + '.', "apollo.commands.deathbans.remove");
            }
        });
    }

}
