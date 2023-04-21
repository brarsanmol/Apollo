package com.octavemc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import com.octavemc.Apollo;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SpeedCommand extends BaseCommand {

    public SpeedCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @CommandAlias("speed")
    @CommandPermission("apollo.commands.speed")
    public static void onSpeed(Player player, float amount) {
        if (!(amount >= 0F && amount <= 1F)) {
            player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" + ChatColor.DARK_RED + ChatColor.BOLD +  "!" + ChatColor.DARK_GRAY + ChatColor.BOLD + ") "+ ChatColor.GRAY + "The speed amount must be between " + ChatColor.AQUA + "0" + ChatColor.GRAY + " to " + ChatColor.AQUA + "1" + ChatColor.GRAY + ".");
            return;
        } else if (player.isFlying()) {
            player.setFlySpeed(amount);
        } else {
            player.setWalkSpeed(amount);
        }

        player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" + ChatColor.AQUA + ChatColor.BOLD +  "!" + ChatColor.DARK_GRAY + ChatColor.BOLD + ") " + ChatColor.GRAY + "Set " + ChatColor.AQUA + (player.isFlying() ? "flying speed" : "walking speed") + ChatColor.GRAY + " to " + ChatColor.AQUA + amount + ChatColor.GRAY + ".");
    }

}
