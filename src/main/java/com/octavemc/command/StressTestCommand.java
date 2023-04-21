package com.octavemc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import com.octavemc.Apollo;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class StressTestCommand extends BaseCommand {

    public StressTestCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
    }

    @CommandAlias("stressteststacker")
    @CommandPermission("apollo.stressteststacker")
    public void onCommand(Player player, int amount, EntityType type) {
        player.sendMessage("Spawning in " + amount + type.getName() + "'s.");
        for (int index = 0; index < amount; index++) {
            player.getWorld().spawnEntity(player.getLocation(), type);
        }
    }

}
