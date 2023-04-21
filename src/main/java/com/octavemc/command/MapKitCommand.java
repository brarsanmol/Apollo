package com.octavemc.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.util.InventoryUtils;
import com.octavemc.util.ItemBuilder;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MapKitCommand extends BaseCommand implements Listener {

    private Inventory mapkitInventory;

    public MapKitCommand() {
        Apollo.getInstance().getCommandManager().registerCommand(this);
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
        reloadMapKitInventory();
    }

    private void reloadMapKitInventory() {
        //TODO: Fix this...
        List<ItemStack> items = new ArrayList<>();

        for (Enchantment enchantment : Enchantment.values()) {
            int maxLevel = Configuration.getEnchantmentLimit(enchantment);
            ItemBuilder builder = new ItemBuilder(Material.ENCHANTED_BOOK);
            builder.displayName(ChatColor.YELLOW + enchantment.getName() + ": " + ChatColor.GREEN + (maxLevel == 0 ? "Disabled" : maxLevel));
            items.add(builder.build());
        }

        for (PotionType potionType : PotionType.values()) {
            int maxLevel = Configuration.getPotionLimit(potionType);
            ItemBuilder builder = new ItemBuilder(new Potion(potionType).toItemStack(1));
            builder.displayName(ChatColor.YELLOW + WordUtils.capitalizeFully(potionType.name().replace('_', ' ')) + ": " + ChatColor.GREEN + (maxLevel == 0 ? "Disabled" : maxLevel));
            //builder.lore(SEPARATOR_LINE, ChatColor.WHITE + "  No Extra Data", SEPARATOR_LINE);
            items.add(builder.build());
        }

        mapkitInventory = Bukkit.createInventory(null, InventoryUtils.getSafestInventorySize(items.size()), "Map " + Configuration.MAP_NUMBER + " Kit");
        for (ItemStack item : items) {
            mapkitInventory.addItem(item);
        }
    }

    @CommandAlias("mapkit")
    public boolean onMapKitCommand(Player player) {
        player.openInventory(mapkitInventory);
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (mapkitInventory.equals(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPluginDisable(PluginDisableEvent event) {
        for (HumanEntity viewer : new HashSet<>(mapkitInventory.getViewers())) { // copy to prevent co-modification
            viewer.closeInventory();
        }
    }
}
