package com.octavemc.sidebar;

import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.DateTimeFormats;
import com.octavemc.eventgame.EventType;
import com.octavemc.eventgame.faction.ConquestFaction;
import com.octavemc.eventgame.tracker.ConquestTracker;
import com.octavemc.faction.type.PlayerFaction;
import com.octavemc.pvpclass.bard.BardClass;
import com.octavemc.timer.PlayerTimer;
import com.octavemc.util.DurationFormatter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SidebarAdapterImpl implements SidebarAdapter {

    @Override
    public String getTitle() {
        return Configuration.SCOREBOARD_SIDEBAR_TITLE;
    }

    @Override
    public List<String> getLines(Player player) {
        List<String> lines = new ArrayList<>();

        lines.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 24));
        if (Apollo.getInstance().getSotwTimer().getSotwRunnable() != null) {
            lines.add("SOTW: " + ChatColor.AQUA + DurationFormatter.getRemaining(Apollo.getInstance().getSotwTimer().getSotwRunnable().getRemaining(), true));
        }

        if (Apollo.getInstance().getEotwHandler().getRunnable() != null) {
            lines.add("EOTW: " + ChatColor.AQUA + DurationFormatter.getRemaining(Apollo.getInstance().getEotwHandler().getRunnable().getMillisUntilCappable(), true));
        }

        Apollo.getInstance().getTimerManager().getTimers().stream().filter(timer -> timer instanceof PlayerTimer).filter(timer -> ((PlayerTimer) timer).getRemaining(player) > 0L).forEach(timer -> {
            lines.add(timer.getName() + ": " + ChatColor.AQUA + DurationFormatter.getRemaining(((PlayerTimer) timer).getRemaining(player), true));
        });

        if (Apollo.getInstance().getPvpClassManager().getEquippedClass(player) instanceof BardClass) {
            lines.add("Energy: " + ChatColor.AQUA + handleBardFormat((((BardClass) Apollo.getInstance().getPvpClassManager().getEquippedClass(player)).getEnergyMillis(player)), true));
            if (((BardClass) Apollo.getInstance().getPvpClassManager().getEquippedClass(player)).getRemainingBuffDelay(player) > 0) {
                lines.add("Buff Cooldown: " + ChatColor.AQUA + DurationFormatter.getRemaining(((BardClass) Apollo.getInstance().getPvpClassManager().getEquippedClass(player)).getRemainingBuffDelay(player), true));
            }
        }

        if (Apollo.getInstance().getTimerManager().getEventTimer().getRemaining() > 0L) {
            if (Apollo.getInstance().getTimerManager().getEventTimer().getEventFaction() instanceof ConquestFaction faction) {
                if (lines.size() > 1) lines.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 24));
                lines.add(Apollo.getInstance().getTimerManager().getEventTimer().getEventFaction().getName() + ChatColor.GRAY + "" + ChatColor.ITALIC + " (" + EventType.CONQUEST.getDisplayName() + ')' + ChatColor.WHITE + ':');
                lines.add(StringUtils.repeat(" ", 24));
                lines.add("  " + ChatColor.RED.toString() + faction.getRed().getScoreboardRemaining() + ' ' + ChatColor.YELLOW.toString() + faction.getYellow().getScoreboardRemaining());
                lines.add("  " + ChatColor.GREEN.toString() + faction.getGreen().getScoreboardRemaining() + ' ' + ChatColor.RESET + ChatColor.AQUA.toString() + faction.getBlue().getScoreboardRemaining());
                lines.add(StringUtils.repeat(" ", 24));

                ConquestTracker conquestTracker = (ConquestTracker) Apollo.getInstance().getTimerManager().getEventTimer().getEventFaction().getEventType().getEventTracker();
                List<Map.Entry<PlayerFaction, Integer>> entries = conquestTracker.getFactionPointsMap().entrySet().stream().limit(3).collect(Collectors.toUnmodifiableList());
                for (Map.Entry<PlayerFaction, Integer> entry : entries) {
                    lines.add(" - " + entry.getKey().getDisplayName(player) + ": " + ChatColor.AQUA + entry.getValue());
                }
            } else {
                lines.add(Apollo.getInstance().getTimerManager().getEventTimer().getName() + ChatColor.GRAY + "" + ChatColor.ITALIC + " (" + EventType.KOTH.getDisplayName() + ')' + ChatColor.WHITE + ": " + ChatColor.AQUA +
                        DurationFormatter.getRemaining(Apollo.getInstance().getTimerManager().getEventTimer().getRemaining(), true));
            }
        }
        lines.add(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + StringUtils.repeat("-", 24));

        Collections.reverse(lines);
        return lines.size() == 2 ? Collections.emptyList() : lines;
    }

    private static String handleBardFormat(long millis, boolean trailingZero) {
        return (trailingZero ? DateTimeFormats.REMAINING_SECONDS_TRAILING : DateTimeFormats.REMAINING_SECONDS).get().format(millis * 0.001);
    }
}
