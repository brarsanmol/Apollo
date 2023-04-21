package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.util.ExperienceManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener that stores experience into a Exp Bottle.
 */
public class BottledExpListener implements Listener {

    public static final String BOTTLED_EXP_DISPLAY_NAME = ChatColor.AQUA.toString() + "Bottled Experience";

    public BottledExpListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    // Don't ignore cancelled as AIR interactions are cancelled
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (Configuration.BOTTLED_EXP
                && event.hasItem()
                && (event.getAction() == Action.RIGHT_CLICK_AIR
                || (event.getAction() == Action.RIGHT_CLICK_BLOCK
                && !event.isCancelled()))
                && isBottledExperience(event.getItem())) {
            event.setCancelled(true);
            int amount = Integer.valueOf(
                    ChatColor.stripColor(event.getItem().getItemMeta().getLore().get(0).substring(event.getItem().getItemMeta().getLore().get(0).lastIndexOf(' '))));
            new ExperienceManager(event.getPlayer()).changeExp(amount);
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ORB_PICKUP, 1.0F, 1.0F);
            event.getItem().setAmount(event.getItem().getAmount() - 1);
        }
    }

    /**
     * Checks if an {@link ItemStack} is bottled exp.
     *
     * @param stack the {@link ItemStack} to check
     * @return true if is bottled exp
     */
    private boolean isBottledExperience(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return false;
        return stack.getItemMeta().hasDisplayName() && stack.getItemMeta().getDisplayName().equals(BOTTLED_EXP_DISPLAY_NAME);
    }
}