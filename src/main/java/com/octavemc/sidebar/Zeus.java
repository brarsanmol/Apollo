package com.octavemc.sidebar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.plugin.java.JavaPlugin;

public class Zeus {

    @Getter(AccessLevel.PROTECTED)
    private static Zeus instance;
    @Getter(AccessLevel.PROTECTED)
    private final JavaPlugin plugin;
    @Getter(AccessLevel.PROTECTED)
    private final SidebarAdapter sidebarAdapter;
    @Getter
    private final SidebarManager sidebarManager;
    @Getter(AccessLevel.PROTECTED)
    private final SidebarEntryGenerator sidebarEntryGenerator;
    @Getter(AccessLevel.PROTECTED)
    private final SidebarTask sidebarTask;

    public Zeus(@NonNull JavaPlugin plugin, @NonNull SidebarAdapter sidebarAdapter) {
        instance = this;
        this.plugin = plugin;
        this.sidebarAdapter = sidebarAdapter;
        this.sidebarManager = new SidebarManager();
        this.sidebarEntryGenerator = new SidebarEntryGenerator();
        this.sidebarTask = new SidebarTask();
        new SidebarListener();
    }
}
