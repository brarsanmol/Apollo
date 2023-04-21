package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.DateTimeFormats;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SwordLoreListener implements Listener {

    public SwordLoreListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null && event.getEntity().getKiller().hasPermission("apollo.toolstats")
                && event.getEntity().getKiller().getItemInHand().getType().name().endsWith("SWORD")) {
            ItemMeta meta = event.getEntity().getKiller().getItemInHand().getItemMeta();
            List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
            lore.add(ChatColor.AQUA + event.getEntity().getName() + ChatColor.WHITE + " was slain by " + ChatColor.AQUA + event.getEntity().getKiller().getName() + ChatColor.WHITE + " at " + ChatColor.AQUA + DateTimeFormats.DAY_MTH_YR_HR_MIN_AMPM.format(System.currentTimeMillis()) + ChatColor.WHITE + ".");
            meta.setLore(lore);
            event.getEntity().getKiller().getItemInHand().setItemMeta(meta);
        }
    }

}
