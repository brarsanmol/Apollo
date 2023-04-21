package com.octavemc.sidebar;

import com.google.common.base.Preconditions;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SidebarEntryGenerator {

    private final List<String> entries;

    public SidebarEntryGenerator() {
        this.entries = new ArrayList<>();
        IntStream.range(0, ChatColor.values().length).forEach(index -> this.entries.add(ChatColor.values()[index] + "" + ChatColor.values()[index] + ChatColor.RESET));
    }

    public String getNextEntry(int index) {
        Preconditions.checkNotNull(index, "Index cannot be null");
        Preconditions.checkArgument(index > -1, "Index cannot be negative.");
        Preconditions.checkArgument(index < ChatColor.values().length, "Index cannot greater than " + ChatColor.values().length + ".");
        return this.entries.get(index);
    }

    public void clearEntries() {
        this.entries.clear();
    }
}