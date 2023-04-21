package com.octavemc.combatlogging.event;

import com.octavemc.combatlogging.LoggerEntityVillager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LoggerSpawnEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final LoggerEntityVillager loggerEntity;

    public LoggerSpawnEvent(LoggerEntityVillager loggerEntity) {
        this.loggerEntity = loggerEntity;
    }

    public LoggerEntityVillager getLoggerEntity() {
        return loggerEntity;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
