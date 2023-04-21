package com.octavemc.tablist;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TablistManager {

    @Getter
    private Map<UUID, Tablist> tablists;

    public TablistManager() {
        this.tablists = new ConcurrentHashMap<>();
    }

    public void addTablist(@NonNull Player player) {
        this.tablists.put(player.getUniqueId(), new Tablist(player));
    }

    public void removeTablist(@NonNull Player player) {
        this.tablists.remove(player.getUniqueId());
    }

    public Tablist getTablist(@NonNull Player player) {
        return this.tablists.get(player.getUniqueId());
    }
}
