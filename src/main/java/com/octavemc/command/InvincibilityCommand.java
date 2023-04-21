package com.octavemc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.timer.type.InvincibilityTimer;
import com.octavemc.util.DurationFormatter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Command used to manage the {@link InvincibilityTimer} of {@link Player}s.
 */
@CommandAlias("invincibility")
public class InvincibilityCommand extends BaseCommand {

    public InvincibilityCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @HelpCommand
    public static void onHelp(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("give|g")
    @CommandCompletion("@players")
    public static void onGive(Player player, Player target) {
        InvincibilityTimer timer = Apollo.getInstance().getTimerManager().getInvincibilityTimer();

        if (timer.getRemaining(target) > 0L) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "That player already has invincibility.");
        } else if (timer.setCooldown(target, target.getUniqueId())) {
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You have given " + ChatColor.AQUA + target.getName() + ChatColor.GRAY + " invincibility.");
            target.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You have been given invincibility by " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + '.');
        }
    }

    @Subcommand("remove|r")
    public static void onRemove(Player player) {
        InvincibilityTimer timer = Apollo.getInstance().getTimerManager().getInvincibilityTimer();

        if (timer.getRemaining(player) <= 0L) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You currently do not have invincibility.");
        } else {
            timer.clearCooldown(player);
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You invincibility has been removed.");
        }
    }

    @Subcommand("left|l")
    @Description("Check the remaining duration of your invincibility.")
    public static void onLeft(Player player) {
        InvincibilityTimer timer = Apollo.getInstance().getTimerManager().getInvincibilityTimer();

        if (timer.getRemaining(player) <= 0L) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You currently do not have invincibility.");
        } else {
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You have " + ChatColor.AQUA + DurationFormatter.getRemaining(timer.getRemaining(player), true, false) + ChatColor.GRAY + " remaining on your " + ChatColor.AQUA + "invincibility" + ChatColor.AQUA + (timer.isPaused(player) ? " and it is currently " + ChatColor.AQUA + "paused" + ChatColor.GRAY: "") + '.');
        }
    }
}
