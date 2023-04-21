package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.DateTimeFormats;
import com.octavemc.util.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.stream.IntStream;

public class EventSignListener implements Listener {

    public EventSignListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    private final static String EVENT_SIGN_ITEM_NAME = ChatColor.GOLD + "Event Sign";

    public static ItemStack getEventSign(String playerName, String kothName) {
        return new ItemBuilder(Material.SIGN)
                .displayName(EVENT_SIGN_ITEM_NAME)
                .lore(ChatColor.GREEN + playerName,
                        ChatColor.WHITE + "captured by",
                        ChatColor.GREEN + kothName,
                        DateTimeFormats.DAY_MTH_HR_MIN_SECS.format(System.currentTimeMillis())).build();
    }

    private boolean isEventSign(Block block) {
        return block.getState() instanceof Sign sign
                && sign.getLines().length > 0
                && sign.getLines()[1] != null
                && sign.getLines()[1].equals(ChatColor.WHITE + "captured by");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        if (isEventSign(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    // Triggered when a event sign is broken.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isEventSign(event.getBlock())) {
            BlockState state = event.getBlock().getState();
            Sign sign = (Sign) state;

            ItemStack stack = new ItemBuilder(Material.SIGN)
                    .displayName(EVENT_SIGN_ITEM_NAME)
                    .lore(sign.getLines())
                    .build();

            if (event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getPlayer().getWorld().isGameRule("doTileDrops")) {
                event.getPlayer().getWorld().dropItemNaturally(event.getBlock().getLocation(), stack);
            }

            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            state.update();
        }
    }

    // Triggered when a event sign is placed.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getState() instanceof Sign sign
                && event.getItemInHand().hasItemMeta()
                && event.getItemInHand().getItemMeta().hasDisplayName()
                && event.getItemInHand().getItemMeta().getDisplayName().equals(EVENT_SIGN_ITEM_NAME)) {
            IntStream.range(0, 4).forEach(line -> sign.setLine(line, event.getItemInHand().getItemMeta().getLore().get(line)));
            sign.update();
        }
    }
}
