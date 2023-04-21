package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.util.NameUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class PickaxeLoreListener implements Listener {

    public PickaxeLoreListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (event.getPlayer().hasPermission("apollo.toolstats")
                && event.getBlock().getType().name().endsWith("ORE")
                && event.getPlayer().getItemInHand().getType().name().endsWith("PICKAXE")
                && !event.getBlock().getDrops(event.getPlayer().getItemInHand()).isEmpty()) {
            ItemMeta meta = event.getPlayer().getItemInHand().getItemMeta();
            List<String> lore = !meta.hasLore() ? new ArrayList<>(Arrays.asList(
                    ChatColor.WHITE + NameUtils.getPrettyName(Material.EMERALD_ORE.name()) + ": " + ChatColor.AQUA + "0",
                    ChatColor.WHITE + NameUtils.getPrettyName(Material.DIAMOND_ORE.name()) + ": " + ChatColor.AQUA + "0",
                    ChatColor.WHITE + NameUtils.getPrettyName(Material.REDSTONE_ORE.name()) + ": " + ChatColor.AQUA + "0",
                    ChatColor.WHITE + NameUtils.getPrettyName(Material.GOLD_ORE.name()) + ": " + ChatColor.AQUA + "0",
                    ChatColor.WHITE + NameUtils.getPrettyName(Material.LAPIS_ORE.name()) + ": " + ChatColor.AQUA + "0",
                    ChatColor.WHITE + NameUtils.getPrettyName(Material.IRON_ORE.name()) + ": " + ChatColor.AQUA + "0",
                    ChatColor.WHITE + NameUtils.getPrettyName(Material.COAL_ORE.name()) + ": " + ChatColor.AQUA + "0",
                    ChatColor.WHITE + NameUtils.getPrettyName(Material.QUARTZ_ORE.name()) + ": " + ChatColor.AQUA + "0"
            )): meta.getLore();
            IntStream.range(0, lore.size()).forEach(index -> {
                String line = lore.get(index);
                if (ChatColor.stripColor(line).charAt(0) == event.getBlock().getType().name().charAt(0)) {
                    int count = Integer.valueOf(ChatColor.stripColor(line.substring(line.lastIndexOf(' ') + 1))) + 1;
                    lore.set(index, ChatColor.WHITE + NameUtils.getPrettyName(event.getBlock().getType().name()) + ": " + ChatColor.AQUA + count);
                }
            });
            meta.setLore(lore);
            event.getPlayer().getItemInHand().setItemMeta(meta);
        }
    }

}
