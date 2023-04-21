package com.octavemc.combatlogging.event;

import com.octavemc.combatlogging.LoggerEntityVillager;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LoggerRemovedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final LoggerEntityVillager loggerEntity;

    public LoggerRemovedEvent(LoggerEntityVillager loggerEntity) {
        this.loggerEntity = loggerEntity;
    }

    public LoggerEntityVillager getLoggerEntity() {
        return loggerEntity;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
