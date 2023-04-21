package com.octavemc.timer;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.octavemc.Apollo;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

/**
 * Handles the execution and tab completion of the timer command.
 */
@CommandAlias("timer")
@CommandPermission("apollo.commands.timer")
public class TimerCommand extends BaseCommand {

    public TimerCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
        Apollo.getInstance().getCommandManager().getCommandCompletions().registerAsyncCompletion("player-timers", context -> Apollo.getInstance().getTimerManager().getTimers().stream().filter(timer ->  timer instanceof PlayerTimer).map(Timer::getName).collect(Collectors.toList()));
    }

    @HelpCommand
    public static void onHelpCommand(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("set")
    @CommandCompletion("player-timers")
    @Syntax("&c&lERROR! &c/timer set <identifier> <all|player> <duration>")
    @Description("Set remaining timer time")
    public static void onSetCommand(CommandSender sender, String identifier, String name, long duration) {
        OfflinePlayer target = Apollo.getInstance().getServer().getOfflinePlayer(identifier);
        PlayerTimer timer = ((PlayerTimer) Apollo.getInstance().getTimerManager().getTimer(identifier));

        if (duration < 1000L) {
            sender.sendMessage(ChatColor.RED + "Timer must last for at least 20 ticks.");
        } else if (timer == null) {
            sender.sendMessage(ChatColor.RED + "The timer with the identifier " + identifier + " cannot be found.");
        } else if (name.equalsIgnoreCase("all")) {
            Apollo.getInstance().getServer().getOnlinePlayers().forEach(player -> timer.setCooldown(player, player.getUniqueId(), duration, true, null));
            sender.sendMessage(ChatColor.BLUE + "Set timer " + timer.getName() + " for all to " + DurationFormatUtils.formatDurationWords(duration, true, true) + '.');
        } else if (target == null) {
            sender.sendMessage(ChatColor.GOLD + "Player '" + ChatColor.WHITE + name + ChatColor.GOLD + "' not found.");
        } else {
            timer.setCooldown(target.getPlayer(), target.getUniqueId(), duration, true, null);
            sender.sendMessage(ChatColor.BLUE + "Set timer " + timer.getName() + " duration to " + DurationFormatUtils.formatDurationWords(duration, true, true) + " for " + timer.getName() + '.');
        }
    }

    @Subcommand("check")
    @CommandCompletion("player-timers")
    @Syntax("&c&lERROR! &c/timer check <identifier> <player>")
    @Description("Check remaining timer time")
    public static void onCheckCommand(CommandSender sender, String identifier, String name) {
        OfflinePlayer player = Apollo.getInstance().getServer().getOfflinePlayer(identifier);
        PlayerTimer timer = ((PlayerTimer) Apollo.getInstance().getTimerManager().getTimer(identifier));

        if (timer == null) {
            sender.sendMessage(ChatColor.RED + "The timer with the identifier " + identifier + " cannot be found.");
        } else if (!player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.GOLD + "Player '" + ChatColor.WHITE + name + ChatColor.GOLD + "' not found.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + player.getName() + " has timer " + timer.getName() + " for another " + DurationFormatUtils.formatDurationWords(timer.getRemaining(player.getPlayer()), true, true));
        }
    }
}