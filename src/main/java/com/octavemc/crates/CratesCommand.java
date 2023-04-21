package com.octavemc.crates;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.util.BukkitUtils;
import com.octavemc.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.stream.Collectors;

@CommandAlias("crates|crate")
@CommandPermission("apollo.commands.crates")
public class CratesCommand extends BaseCommand {

    public CratesCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
        Apollo.getInstance().getCommandManager().getCommandCompletions().registerAsyncCompletion("crates", context -> Apollo.getInstance().getCrateDao().getCache().values().stream().map(Crate::getIdentifier).collect(Collectors.toList()));
    }

    @Subcommand("create")
    @Syntax("<name> <key_item>")
    public static void onCreate(Player player, String name, String key) {
        if (Apollo.getInstance().getCrateDao().get(name).isPresent()) {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A crate with the name " + ChatColor.AQUA + name + ChatColor.GRAY + " already exists!");
        } else if (player.getTargetBlock((Set<Material>) null, 5).getType() == Material.ENDER_CHEST) {
            Apollo.getInstance().getCrateDao().getCache().put(name, new Crate(name, Material.matchMaterial(key), player.getTargetBlock((Set<Material>) null, 5).getLocation()));
            BukkitUtils.createHologram(player.getTargetBlock((Set<Material>) null, 5).getLocation().add(0.5, -0.25, 0.5), ChatColor.AQUA + "" + ChatColor.BOLD + name + ChatColor.WHITE + "" + ChatColor.BOLD + " Crate");
            BukkitUtils.createHologram(player.getTargetBlock((Set<Material>) null, 5).getLocation().add(0.5, -0.5, 0.5), ChatColor.AQUA + "Right Click " + ChatColor.WHITE + "To Redeem");
            BukkitUtils.createHologram(player.getTargetBlock((Set<Material>) null, 5).getLocation().add(0.5, -0.75, 0.5), ChatColor.AQUA + "Left Click " + ChatColor.WHITE + "To View Rewards");
            BukkitUtils.createHologram(player.getTargetBlock((Set<Material>) null, 5).getLocation().add(0.5, -1, 0.5), ChatColor.WHITE + "Purchase at " + ChatColor.AQUA + "store.octavemc.com");
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Created a crate with the name " + ChatColor.AQUA + name + ChatColor.GRAY + ".");
        } else {
            player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Please ensure that you are looking at crate enderchest (5 blocks) when creating a crate, use the " + ChatColor.AQUA + "/crate chest" + ChatColor.GRAY + " command to receive one");
        }
    }

    @Subcommand("delete")
    @Syntax("<name>")
    @CommandCompletion("@crates")
    public static void onDelete(Player player, String name) {
        Apollo.getInstance().getCrateDao().get(name).ifPresentOrElse(crate -> {
            crate.getLocation().getLocation().getWorld().getNearbyEntities(crate.getLocation().getLocation(), 0, 1, 0).stream().filter(entity -> entity instanceof ArmorStand).forEach(entity -> ((ArmorStand) entity).setHealth(0));
            Apollo.getInstance().getCrateDao().delete(crate);
            Apollo.getInstance().getCrateDao().getCache().remove(crate); //TODO: Is this operation okay?
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Successfully deleted the crate.");
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A crate with that name was not found"));
    }

    @Subcommand("chest")
    public static void onChest(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.ENDER_CHEST, 1)
                .lore(ChatColor.AQUA + "1) Place down this chest...", ChatColor.AQUA + "2) Look at it while performing the " + ChatColor.WHITE + "/crate create <name> " + ChatColor.AQUA + " command.")
                .build());
    }

    @Subcommand("key give")
    @Syntax("<name> <player>")
    @CommandCompletion("@players @crates")
    public static void onKeyGive(Player player, String name, Player target) {
        Apollo.getInstance().getCrateDao().get(name).ifPresentOrElse(crate -> {
            target.getInventory().addItem(crate.getKey());
            target.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You were given a crate key for " + ChatColor.AQUA + name + ChatColor.GRAY + " by " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + ".");
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You gave a crate key for " + ChatColor.AQUA + name + ChatColor.GRAY + " to " + ChatColor.AQUA + player.getName() + ChatColor.GRAY + ".");
        }, () -> player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "A crate with the name " + ChatColor.AQUA + name + ChatColor.GRAY + " does not exist."));
    }

    @Subcommand("key all")
    @CommandCompletion("@crates")
    public static void onKeyAll(Player player, String name) {
        Apollo.getInstance().getCrateDao().get(name).ifPresentOrElse(crate -> {
            Apollo.getInstance().getServer().getOnlinePlayers().forEach(target -> target.getInventory().addItem(crate.getKey()));
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "You were given a crate key for " + ChatColor.AQUA + name + ChatColor.GRAY + " by " + ChatColor.AQUA + player.getName());
        }, () -> player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "A crate with the name " + ChatColor.AQUA + name + ChatColor.GRAY + " does not exist."));
    }

    @Subcommand("table add")
    @Syntax("<name> <weight>")
    @CommandCompletion("@crates")
    public static void onTableAdd(Player player, String name, double weight) {
        Apollo.getInstance().getCrateDao().get(name).ifPresentOrElse(crate -> {
            if (crate.getLootTable().containsKey(weight)) {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A item with the weight " + ChatColor.AQUA + weight + ChatColor.GRAY + " already exists.");
            } else if (crate.put(weight, player.getItemInHand().clone()) == null) {
                player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Added the item " + ChatColor.AQUA + player.getItemInHand().getType() + ChatColor.GRAY +  " with a weight of " + ChatColor.AQUA + weight + ChatColor.GRAY + " to the loot table for " + ChatColor.AQUA + name);
            } else {
                player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Failed to add the item " + ChatColor.AQUA + player.getItemInHand().getType() + ChatColor.GRAY + "with a weight of " + ChatColor.AQUA + weight + ChatColor.GRAY + " to the loot table for " + ChatColor.AQUA + name + ChatColor.GRAY + ".");
            }
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A crate with the name " + ChatColor.AQUA + name + ChatColor.GRAY + " does not exist"));
    }

    @Subcommand("table clear")
    @Syntax("<name>")
    @CommandCompletion("@crates")
    public static void onTableClear(Player player, String name) {
        Apollo.getInstance().getCrateDao().get(name).ifPresentOrElse(crate -> {
            crate.getLootTable().clear();
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Cleared the loot table for " + ChatColor.AQUA + name + ChatColor.GRAY + ".");
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "A crate with the name " + ChatColor.AQUA + name + ChatColor.GRAY + " does not exist"));
    }

    @Subcommand("table view")
    @Syntax("<name>")
    @CommandCompletion("@crates")
    public static void onTableView(Player player, String name) {
        Apollo.getInstance().getCrateDao().get(name).ifPresentOrElse(crate -> player.openInventory(crate.getInventory()),
                () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY +  "A crate with the name " + ChatColor.AQUA + name + ChatColor.GRAY + " does not exist"));
    }
}
