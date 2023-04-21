package com.octavemc.deathban;

import com.octavemc.util.PersistableLocation;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Location;

@Entity
@NoArgsConstructor
public class Deathban {

    @Getter
    @Property
    private String reason;

    @Getter
    @Property
    private long creationMillis;

    @Property
    private long expiryMillis;

    @Property
    private PersistableLocation deathPoint;

    @Getter
    @Property
    private boolean eotwDeathban;

    public Deathban(String reason, long duration, PersistableLocation deathPoint, boolean eotwDeathban) {
        this.reason = reason;
        this.creationMillis = System.currentTimeMillis();
        this.expiryMillis = this.creationMillis + duration;
        this.deathPoint = deathPoint;
        this.eotwDeathban = eotwDeathban;
    }

    /**
     * Gets the initial duration of this {@link Deathban} in milliseconds.
     *
     * @return the initial duration
     */
    public long getInitialDuration() {
        return expiryMillis - creationMillis;
    }

    /**
     * Checks if this {@link Deathban} is active.
     *
     * @return true if is active
     */
    public boolean isActive() {
        return !eotwDeathban && getRemaining() > 0L;
    }

    /**
     * Gets the remaining time in milliseconds until this {@link Deathban}
     * is no longer active.
     *
     * @return the remaining time until expired
     */
    public long getRemaining() {
        return expiryMillis - System.currentTimeMillis();
    }

    /**
     * Gets the {@link Location} where this player died during {@link Deathban}.
     *
     * @return death {@link Location}
     */
    public Location getDeathPoint() {
        return deathPoint == null ? null : deathPoint.getLocation();
    }
}
