package com.octavemc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import com.octavemc.Apollo;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SpawnCommand extends BaseCommand {

    public SpawnCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @CommandAlias("spawn")
    @CommandPermission("apollo.commands.spawn")
    public static void onSpawn(Player player) {
        player.teleport(player.getWorld().getSpawnLocation());
        player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "(" + ChatColor.AQUA + ChatColor.BOLD +  "!" + ChatColor.DARK_GRAY + ChatColor.BOLD + ") " + ChatColor.GRAY + "You've been teleported to Spawn!");
    }

}
