package com.octavemc.deathban.lives;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.struct.Relation;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Handles the execution and tab completion of the lives command.
 */
@CommandAlias("lives")
public class LivesCommand extends BaseCommand {

    public LivesCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @HelpCommand
    public static void onHelpCommand(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("check|c")
    @Description("Check the amount of lives a player has.")
    @CommandCompletion("@players")
    public static void onCheckCommand(Player player, @Optional String name) {
        OfflinePlayer target = Apollo.getInstance().getServer().getOfflinePlayer(name) == null ? player : Apollo.getInstance().getServer().getOfflinePlayer(name);
        if (!target.hasPlayedBefore()) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A player with that name was not found.");
        } else {
            int lives = Apollo.getInstance().getUserDao().get(player.getUniqueId()).get().getLives();
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + target.getName() + ChatColor.GRAY + " has " + ChatColor.AQUA + lives + ChatColor.GRAY + ' ' + (lives == 1 ? "life" : "lives") + '.');
        }
    }

    @Subcommand("set|s")
    @Description("Set the amount of lives a player has.")
    @CommandCompletion("@players")
    @CommandPermission("apollo.commands.lives.set")
    public static void onSetCommand(Player player, OfflinePlayer target, int amount) {
        if (amount < 0) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You must set lives in positive quantities.");
        } else if (!target.hasPlayedBefore()) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A player with that name was not found.");
        } else {
            Apollo.getInstance().getUserDao().get(player.getUniqueId()).ifPresent(user -> user.setLives(amount));
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.AQUA + target.getName() + ChatColor.GRAY + " now has " + ChatColor.AQUA + amount + ChatColor.GRAY + ' ' + (amount == 1 ? "life" : "lives") + '.');
        }
    }

    @Subcommand("revive|r")
    @CommandCompletion("@players")
    @Description("Revive a deathbanned player.")
    public static void onReviveCommand(Player player, OfflinePlayer target) {
        Apollo.getInstance().getUserDao().get(player.getUniqueId()).ifPresent(user -> {
            if (user.getLives() <= 0) {
                player.sendMessage(ChatColor.RED + "You do not have any lives.");
            } else if (!target.hasPlayedBefore()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A player with that name was not found.");
            } else if (Apollo.getInstance().getUserDao().get(target.getUniqueId()).get().getDeathban() == null
                    || !Apollo.getInstance().getUserDao().get(target.getUniqueId()).get().getDeathban().isActive()) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "That player is not currently death-banned.");
            } else if (Apollo.getInstance().getEotwHandler().isEndOfTheWorld() && !player.hasPermission("apollo.commands.lives.revive.bypass")){
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You cannot revive players during EOTW.");
            } else {
                user.setLives(user.getLives() - 1);
                Apollo.getInstance().getUserDao().get(target.getUniqueId()).get().setDeathban(null);
                player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You have revived " + (Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).isEmpty()
                        ? Relation.ENEMY
                        : Apollo.getInstance().getFactionDao().getByPlayer(player.getUniqueId()).get().getFactionRelation(Apollo.getInstance().getFactionDao().getByPlayer(target.getUniqueId()).get())) + target.getName() + ChatColor.GRAY + '.');
            }
        });
    }


}