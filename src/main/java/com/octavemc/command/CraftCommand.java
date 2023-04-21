package com.octavemc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.octavemc.Apollo;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

public class CraftCommand extends BaseCommand {

    public CraftCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @CommandAlias("craft")
    @CommandPermission("apollo.commands.craft")
    @Description("Open a Crafting Table.")
    public static void onCommand(Player player) {
        player.openInventory(Apollo.getInstance().getServer().createInventory(player, InventoryType.WORKBENCH));
    }

}
