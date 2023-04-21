package com.octavemc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.util.NameUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class RenameCommand extends BaseCommand {

    public RenameCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @CommandAlias("rename")
    @CommandPermission("apollo.commands.rename")
    @Description("Rename a item.")
    public static void onCommand(Player player, String name) {
        if (player.getItemInHand().getType() == Material.AIR) player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "You cannot name air!");
        else {
            ItemMeta meta = player.getItemInHand().getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            player.getItemInHand().setItemMeta(meta);
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You have named your " + ChatColor.AQUA + NameUtils.getPrettyName(player.getItemInHand().getType().name()) + ChatColor.GRAY + " to " + ChatColor.translateAlternateColorCodes('&', name) + ChatColor.GRAY + '.');
        }
    }

}
