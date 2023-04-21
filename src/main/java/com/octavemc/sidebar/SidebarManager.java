package com.octavemc.sidebar;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SidebarManager {

    @Getter
    private final Map<UUID, Sidebar> sidebars;

    public SidebarManager() {
        this.sidebars = new HashMap<>();
    }

    public void addSidebar(@NonNull Player player) {
        this.sidebars.put(player.getUniqueId(), new Sidebar(player));
    }

    public void removeSidebar(@NonNull Player player) {
        this.sidebars.remove(player.getUniqueId());
    }

    public Sidebar getSidebar(@NonNull Player target) {
        return this.sidebars.get(target.getUniqueId());
    }
}