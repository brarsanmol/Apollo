package com.octavemc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.listener.BottledExpListener;
import com.octavemc.util.ExperienceManager;
import com.octavemc.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BottleCommand extends BaseCommand {

    public BottleCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @CommandAlias("bottle")
    @CommandPermission("apollo.commands.bottle")
    @Description("Create a bottle o'enchanting to save your experience for later!")
    public static void onBottleCommand(Player player) {
        int exp = new ExperienceManager(player).getCurrentExp();
        if (exp < 1) player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + "You must have some experience to create a bottle.");
        else player.getInventory().addItem(new ItemBuilder(Material.EXP_BOTTLE).displayName(BottledExpListener.BOTTLED_EXP_DISPLAY_NAME).lore(ChatColor.WHITE.toString() + exp + ChatColor.GOLD + " Experience").build());
    }

}
