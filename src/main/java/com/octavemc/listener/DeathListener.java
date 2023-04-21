package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    public DeathListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Apollo.getInstance().getFactionDao().getByPlayer(event.getEntity().getUniqueId()).ifPresent(faction -> {
            faction.setRemainingRegenerationTime(Configuration.FACTION_DTR_REGEN_FREEZE_BASE_MILLIS + (faction.getOnlinePlayers().size() * Configuration.FACTION_DTR_REGEN_FREEZE_MILLIS_PER_MEMBER));
            faction.broadcast(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + faction.getMember(event.getEntity()).getRole().getName() + " Death: " + Configuration.RELATION_COLOUR_TEAMMATE + event.getEntity().getName() + ChatColor.GRAY + ". " +
                    ChatColor.GRAY + "DTR: (" + ChatColor.WHITE +
                    String.format("%.2f", faction.setDeathsUntilRaidable(faction.getDeathsUntilRaidable()
                            - (1.0D * Apollo.getInstance().getFactionDao().getFactionAt(event.getEntity().getLocation()).get().getDtrLossMultiplier())), 2)
                    + ChatColor.GRAY + '/' + ChatColor.WHITE + String.format("%.2f", faction.getMaximumDeathsUntilRaidable()) + ChatColor.GRAY + ").");
        });
    }

}
