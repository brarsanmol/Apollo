package com.octavemc.economy.merchants;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandAlias("merchants|merchant")
@CommandPermission("apollo.commands.merchants")
public class MerchantsCommand extends BaseCommand {

    public MerchantsCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
        Apollo.getInstance().getCommandManager().getCommandCompletions().registerAsyncCompletion("merchants", context -> Apollo.getInstance().getMerchantDao().getCache().values().stream().map(MerchantEntityVillager::getName).collect(Collectors.toList()));
    }


    @Subcommand("create|c")
    @Syntax("<name>")
    public static void onCreate(Player player, String name, MerchantType type) {
        if (Apollo.getInstance().getMerchantDao().get(name).isPresent()) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A merchant with the name " + ChatColor.AQUA + name + ChatColor.GRAY + " already exists!");
        } else {
            Apollo.getInstance().getMerchantDao().getCache().put(name, new MerchantEntityVillager(player, name, type));
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Created a merchant with the name " + ChatColor.AQUA + name + ChatColor.GRAY + ".");
        }
    }

    @Subcommand("delete")
    @Syntax("<name>")
    @CommandCompletion("@merchants")
    public static void onDelete(Player player, String name) {
        Apollo.getInstance().getMerchantDao().get(name).ifPresentOrElse(merchant -> {
            merchant.world.removeEntity(merchant);
            Apollo.getInstance().getMerchantDao().delete(merchant);
            Apollo.getInstance().getMerchantDao().getCache().remove(merchant.getName()); //TODO: Is this operation okay?
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Successfully deleted the merchant.");
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A merchant with that name was not found"));
    }

    @Subcommand("table add")
    @Syntax("<name> <price>")
    @CommandCompletion("@merchants")
    public static void onTableAdd(Player player, String name, double price) {
        Apollo.getInstance().getMerchantDao().get(name).ifPresentOrElse(merchant -> {
            if (merchant.getLootTable().containsKey(player.getItemInHand())) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "That item already exists in the loot table!");
            } else if (price < 0) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The price must be a positive value.");
            } else {
                merchant.put(player.getItemInHand().clone(), price);
                player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Added the item " + ChatColor.AQUA + player.getItemInHand().getType() + ChatColor.GRAY + " with a price of " + ChatColor.AQUA + price + ChatColor.GRAY + " to the loot table.");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A merchant with the name " + ChatColor.AQUA + name + ChatColor.GRAY + " does not exist"));
    }

    @Subcommand("table clear")
    @Syntax("<name>")
    @CommandCompletion("@merchants")
    public static void onTableClear(Player player, String name) {
        Apollo.getInstance().getCrateDao().get(name).ifPresentOrElse(crate -> {
            crate.getLootTable().clear();
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Cleared the loot table for " + ChatColor.AQUA + name + ChatColor.GRAY + ".");
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A merchant with the name " + ChatColor.AQUA + name + ChatColor.GRAY + " does not exist"));
    }

    @Subcommand("table view")
    @Syntax("<name>")
    @CommandCompletion("@merchants")
    public static void onTableView(Player player, String name) {
        Apollo.getInstance().getCrateDao().get(name).ifPresentOrElse(crate -> player.openInventory(crate.getInventory()),
                () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY +  "A crate with the name " + ChatColor.AQUA + name + ChatColor.GRAY + " does not exist"));
    }
}
