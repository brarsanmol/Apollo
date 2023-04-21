package com.octavemc.sidebar;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.stream.IntStream;

public class SidebarTask implements Runnable {

    @Getter
    private BukkitTask bukkitTask;

    public SidebarTask() {
        this.bukkitTask = Zeus.getInstance().getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(Zeus.getInstance().getPlugin(), this, 20L, 2L);
    }

    @Override
    public void run() {
        Zeus.getInstance().getSidebarManager().getSidebars().values().forEach(sidebar -> {
            if (Zeus.getInstance().getSidebarAdapter().getLines(sidebar.getPlayer()) != null) {
                if (sidebar.getScoreboard().getEntries().size() != Zeus.getInstance().getSidebarAdapter().getLines(sidebar.getPlayer()).size())
                    sidebar.getScoreboard().getEntries().forEach(entry -> sidebar.getScoreboard().resetScores(entry));
                IntStream.range(0, Zeus.getInstance().getSidebarAdapter().getLines(sidebar.getPlayer()).size()).forEach(number -> {
                    String line = Zeus.getInstance().getSidebarAdapter().getLines(sidebar.getPlayer()).get(number);
                    if (line.length() > 16) {
                        String prefix = line.substring(0, line.charAt(15) == ChatColor.COLOR_CHAR ? 15 : 16);
                        String suffix = ChatColor.getLastColors(prefix) + (prefix.length() != 16 ? ChatColor.COLOR_CHAR + "" : "") + line.substring(16);
                        sidebar.getScoreboard().getTeam("zeus" + number).setPrefix(prefix);
                        sidebar.getScoreboard().getTeam("zeus" + number).setSuffix(suffix);
                    } else {
                        sidebar.getScoreboard().getTeam("zeus" + number).setPrefix(line);
                        sidebar.getScoreboard().getTeam("zeus" + number).setSuffix("");
                    }
                    sidebar.getScoreboard().getObjective("zeus").getScore(Zeus.getInstance().getSidebarEntryGenerator().getNextEntry(number)).setScore(number + 1);
                });
                sidebar.getPlayer().setScoreboard(sidebar.getScoreboard());
            }
        });
    }
}