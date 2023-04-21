package com.octavemc.voting;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class VoteListener implements Listener {

    public VoteListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVotifier(VotifierEvent event) {
        //TODO: Make this configurable.
        var player = Apollo.getInstance().getServer().getPlayer(event.getVote().getUsername());
        Apollo.getInstance().getCrateDao().get("Vote").ifPresentOrElse(crate -> {
            if (player.getInventory().firstEmpty() == -1) player.getWorld().dropItemNaturally(player.getLocation(), crate.getKey());
            else player.getInventory().addItem(crate.getKey());
            player.sendMessage(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "Thanks for voting! You have recieved a " + ChatColor.AQUA + crate.getKey().getItemMeta().getDisplayName() + ChatColor.GRAY + '.');
        }, () -> player.sendMessage(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "The voting crate does not exist, please contact an administrator."));
    }

}
