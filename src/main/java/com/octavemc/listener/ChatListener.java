package com.octavemc.listener;

import com.octavemc.Apollo;
import com.octavemc.faction.event.FactionChatEvent;
import com.octavemc.faction.struct.ChatChannel;
import com.octavemc.faction.type.Faction;
import com.octavemc.faction.type.PlayerFaction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Collection;
import java.util.Optional;

public final class ChatListener implements Listener {

    public ChatListener() {
        Apollo.getInstance().getServer().getPluginManager().registerEvents(this, Apollo.getInstance());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Optional<PlayerFaction> faction = Apollo.getInstance().getFactionDao().getByPlayer(event.getPlayer().getUniqueId());
        ChatChannel channel = faction.isEmpty() ? ChatChannel.PUBLIC : faction.get().getMember(event.getPlayer()).getChatChannel();

        // Handle faction or alliance chat modes.
        if (channel == ChatChannel.FACTION || channel == ChatChannel.ALLIANCE) {
            if (isGlobalChannel(event.getMessage())) { // allow players to use '!' to bypass friendly chat.
                event.setMessage(event.getMessage().substring(1).trim());
            } else {
                Collection<Player> online = faction.get().getOnlinePlayers();
                if (channel == ChatChannel.ALLIANCE) {
                    Collection<PlayerFaction> allies = faction.get().getAlliedFactions();
                    for (PlayerFaction ally : allies) {
                        online.addAll(ally.getOnlinePlayers());
                    }
                }
                event.getRecipients().retainAll(online);
                event.setFormat(channel.getRawFormat(event.getPlayer()));

                Apollo.getInstance().getServer().getPluginManager().callEvent(new FactionChatEvent(true, faction.get(), event.getPlayer(), channel, event.getRecipients(), event.getMessage()));
                return;
            }
        }

        //TODO: Do we really need to send console formatted messages?
        String displayName = event.getPlayer().getDisplayName();
        ConsoleCommandSender console = Bukkit.getConsoleSender();

        // Handle the custom messaging here.
        event.setFormat(this.getChatFormat(event.getPlayer(), faction, console));
        event.setCancelled(true);
        console.sendMessage(String.format(this.getChatFormat(event.getPlayer(), faction, console), displayName, event.getMessage()));
        event.getRecipients().forEach(recipient -> recipient.sendMessage(String.format(this.getChatFormat(event.getPlayer(), faction, recipient), displayName, event.getMessage())));
    }

    private String getChatFormat(Player player, Optional<PlayerFaction> playerFaction, CommandSender viewer) {
        String factionTag = playerFaction.isEmpty() ? ChatColor.RED + Faction.FACTIONLESS_PREFIX : playerFaction.get().getDisplayName(viewer);
        String prefix = Apollo.getInstance().getLuckPerms().getUserManager().getUser(player.getUniqueId()).getCachedData().getMetaData().getPrefix();
        return ChatColor.DARK_GRAY + "[" + factionTag + ChatColor.DARK_GRAY + "] " + (prefix == null ? "" : ChatColor.translateAlternateColorCodes('&', prefix)) + ChatColor.GRAY + " %1$s: " + ChatColor.WHITE + "%2$s";
    }

    /**
     * Checks if a message should be posted in {@link ChatChannel#PUBLIC}.
     *
     * @param input the message to check
     * @return true if the message should be posted in {@link ChatChannel#PUBLIC}
     */
    private boolean isGlobalChannel(String input) {
        return input.length() > 2 && input.startsWith("!") && !Character.isWhitespace(input.charAt(1));
    }
}
