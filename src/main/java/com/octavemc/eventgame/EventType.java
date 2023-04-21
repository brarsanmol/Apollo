package com.octavemc.eventgame;

import com.octavemc.Apollo;
import com.octavemc.eventgame.tracker.ConquestTracker;
import com.octavemc.eventgame.tracker.EventTracker;
import com.octavemc.eventgame.tracker.KothTracker;

public enum EventType {

    CONQUEST("Conquest", new ConquestTracker()),
    //TODO: Remove DI injection requirement.
    KOTH("KoTH", new KothTracker(Apollo.getInstance()));

    private final EventTracker eventTracker;
    private final String displayName;

    EventType(String displayName, EventTracker eventTracker) {
        this.displayName = displayName;
        this.eventTracker = eventTracker;
    }

    public EventTracker getEventTracker() {
        return eventTracker;
    }

    public String getDisplayName() {
        return displayName;
    };

    @Deprecated
    public static EventType getByDisplayName(String name) {
        for (EventType type : values()) {
            if (type.getDisplayName().equals(name)) {
                return type;
            }
        }
        return null;
    }
}
