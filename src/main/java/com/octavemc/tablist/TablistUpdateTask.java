package com.octavemc.tablist;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.DateTimeFormats;
import com.octavemc.faction.FactionMember;
import com.octavemc.util.BukkitUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TablistUpdateTask extends BukkitRunnable {

    public TablistUpdateTask() {
        this.runTaskTimerAsynchronously(Apollo.getInstance(), 20L, 5L);
    }

    @Override
    public void run() {
        Apollo.getInstance().getTablistManager().getTablists().values().forEach(tablist -> {
            tablist.getSlot(0, 0).setText(ChatColor.AQUA  + "" + ChatColor.BOLD + "Octave");

            tablist.getSlot(0, 2).setText(ChatColor.AQUA + "Location:");
            tablist.getSlot(0, 3).setText(Apollo.getInstance().getFactionDao().getFactionAt(tablist.getPlayer().getLocation()).get().getDisplayName(tablist.getPlayer()));
            tablist.getSlot(0, 4).setText(tablist.getPlayer().getLocation().getBlockX() + ", " + tablist.getPlayer().getLocation().getBlockZ() + ChatColor.AQUA + " [" + BukkitUtils.getCardinalDirection(tablist.getPlayer().getLocation().getYaw()) + "]");

            tablist.getSlot(0, 6).setText(ChatColor.AQUA + "Faction Info");

            Apollo.getInstance().getFactionDao().getByPlayer(tablist.getPlayer().getUniqueId()).ifPresentOrElse(faction -> {
                tablist.getSlot(0, 7).setText("Online: " + faction.getOnlineMembers().size() + " / " + faction.getMembers().size());
                tablist.getSlot(0, 8).setText("DTR: " + faction.getDtrColour() + faction.getDeathsUntilRaidable() + faction.getRegenStatus().getSymbol());
                tablist.getSlot(0, 9).setText("Home: " + (!faction.hasHome()
                        ? "Not Set"
                        : faction.getHome().getBlockX() + ", " + faction.getHome().getBlockY() + ", " + faction.getHome().getBlockZ()));
                tablist.getSlot(0, 10).setText("Balance: " + Configuration.ECONOMY_SYMBOL + faction.getBalance());

                int index = 3;
                tablist.getSlot(1, 2).setText(faction.getDisplayName(tablist.getPlayer()));
                for (FactionMember member : faction.getOnlineMembers().values()) {
                    if (index == 20) return;
                    tablist.getSlot(1, index).setText(ChatColor.AQUA + member.getRole().getAstrix() + ChatColor.WHITE + member.getName());
                    tablist.getSlot(1, index).setLatency(((CraftPlayer) member.toOnlinePlayer()).getHandle().ping);
                    index++;
                }
            }, () -> {
                tablist.getSlot(0, 7).setText("Get started by");
                tablist.getSlot(0, 8).setText("starting a faction");
                tablist.getSlot(0, 9).setText("using the command");
                tablist.getSlot(0, 10).setText(ChatColor.AQUA + "/f create <name>");
            });

            tablist.getSlot(0, 12).setText(ChatColor.AQUA + "Player Info");
            tablist.getSlot(0, 13).setText("Balance: " + Configuration.ECONOMY_SYMBOL + Apollo.getInstance().getUserDao().get(tablist.getPlayer().getUniqueId()).get().getBalance());
            tablist.getSlot(0, 14).setText("Kills: " + tablist.getPlayer().getStatistic(Statistic.PLAYER_KILLS));
            tablist.getSlot(0, 15).setText("Deaths: " + tablist.getPlayer().getStatistic(Statistic.DEATHS));

            tablist.getSlot(2, 2).setText(ChatColor.AQUA + "Map Kit:");
            tablist.getSlot(2, 3).setText("Prot 1, Sharp 1");

            tablist.getSlot(2, 5).setText(ChatColor.AQUA + "End Portals:");
            tablist.getSlot(2, 6).setText("±1000, ±1000");

            tablist.getSlot(2, 8).setText(ChatColor.AQUA + "World Border:");
            tablist.getSlot(2, 9).setText("±3000, ±3000");

            tablist.getSlot(2, 11).setText(ChatColor.AQUA + "Online Players:");
            tablist.getSlot(2, 12).setText(Apollo.getInstance().getServer().getOnlinePlayers().size() + " / " + Apollo.getInstance().getServer().getMaxPlayers());

            Apollo.getInstance().getEventScheduler().getSchedule().entrySet()
                    .stream()
                    .filter(entry -> LocalDateTime.now(ZoneId.systemDefault()).isAfter(entry.getKey())).forEach(entry -> {
                    tablist.getSlot(2, 14).setText(ChatColor.AQUA + "Upcoming Event:");
                    tablist.getSlot(2, 15).setText(WordUtils.capitalize(entry.getValue()));
                    tablist.getSlot(2, 16).setText(entry.getKey().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + entry.getKey().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + entry.getKey().getDayOfMonth() + " - " + DateTimeFormats.HR_MIN_AMPM.format(TimeUnit.HOURS.toMillis(entry.getKey().getHour()) + TimeUnit.MINUTES.toMillis(entry.getKey().getMinute())));
            });

            tablist.getSlot(3, 2).setText(ChatColor.AQUA + "Store:");
            tablist.getSlot(3, 3).setText("store.octavemc.com");

            tablist.getSlot(3, 5).setText(ChatColor.AQUA + "Discord:");
            tablist.getSlot(3, 6).setText("discord.octavemc.com");

            tablist.getSlot(3, 8).setText(ChatColor.AQUA + "Voting:");
            tablist.getSlot(3, 9).setText("vote.octavemc.com");

            tablist.getSlot(3, 11).setText(ChatColor.AQUA + "Twitter:");
            tablist.getSlot(3, 12).setText("twitter.octavemc.com");

            tablist.getSlot(3, 14).setText(ChatColor.AQUA + "Website:");
            tablist.getSlot(3, 15).setText("octavemc.com");
        });
    }
}
