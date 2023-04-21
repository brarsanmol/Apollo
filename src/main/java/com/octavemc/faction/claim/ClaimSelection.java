package com.octavemc.faction.claim;

import com.octavemc.Apollo;
import com.octavemc.faction.type.Faction;
import com.octavemc.faction.type.PlayerFaction;
import com.octavemc.util.cuboid.Cuboid;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.block.Action;

import java.util.UUID;

/**
 * Wrapper for a {@link Claim} selection.
 * <p>Cannot be a {@link Cuboid} as the implementation doesn't allow null locations.</p>
 */
public class ClaimSelection implements Cloneable {

    private final UUID uuid;
    private final World world;

    private long lastUpdateMillis;
    private Location positionOne;
    private Location positionTwo;

    /**
     * Constructs a {@link ClaimSelection} from a given {@link World}.
     *
     * @param world the {@link World} to construct from
     */
    public ClaimSelection(World world) {
        this.uuid = UUID.randomUUID();
        this.world = world;
    }

    /**
     * Constructs a new {@link ClaimSelection} from a
     * given {@link World} and two {@link Location}s.
     *
     * @param pos1  the first {@link Location}
     * @param pos2  the second {@link Location}
     * @param world the {@link World}
     */
    public ClaimSelection(World world, Location pos1, Location pos2) {
        super();
        this.uuid = UUID.randomUUID();
        this.world = world;
        this.positionOne = pos1;
        this.positionTwo = pos2;
    }

    /**
     * Gets the {@link UUID} of {@link ClaimSelection}.
     *
     * @return the {@link UUID} of this {@link ClaimSelection}
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Gets the {@link World} of this {@link ClaimSelection}.
     *
     * @return the {@link World}
     */
    public World getWorld() {
        return world;
    }

    /**
     * Gets the price of this {@link ClaimSelection}.
     *
     * @param faction the {@link Faction} looking up
     * @param selling       if the {@link Faction} is selling this {@link ClaimSelection}
     * @return the price of this {@link ClaimSelection}
     */
    public int getPrice(@NonNull PlayerFaction faction, boolean selling) {
        return positionOne == null || positionTwo == null
                ? 0
                : Apollo.getInstance().getClaimHandler().calculatePrice(new Cuboid(positionOne, positionTwo), faction.getClaims().size(), selling);
    }

    /**
     * Converts this {@link ClaimSelection} to a {@link Claim}.
     *
     * @param faction the faction this {@link Claim} is for
     * @return the converted {@link Claim} instance
     */
    public Claim toClaim(@NonNull Faction faction) {
        return positionOne == null || positionTwo == null ? null : new Claim(faction, positionOne, positionTwo);
    }

    /**
     * Gets the time in milliseconds the first or second position was last updated.
     *
     * @return time in milliseconds at last update
     */
    public long getLastUpdateMillis() {
        return lastUpdateMillis;
    }

    /**
     * Gets the first {@link Location} of this {@link ClaimSelection}.
     *
     * @return the first {@link ClaimSelection} {@link Location}
     */
    public Location getPositionOne() {
        return positionOne;
    }

    /**
     * Sets the first {@link Location} of the {@link ClaimSelection}.
     *
     * @param location the {@link Location} to set
     */
    public void setPositionOne(@NonNull Location location) {
        this.positionOne = location;
        this.lastUpdateMillis = System.currentTimeMillis();
    }

    /**
     * Gets the second {@link Location} of this {@link ClaimSelection}.
     *
     * @return the second {@link ClaimSelection} {@link Location}
     */
    public Location getPositionTwo() {
        return positionTwo;
    }

    /**
     * Sets the second {@link Location} of the {@link ClaimSelection}.
     *
     * @param location the {@link Location} to set
     */
    public void setPositionTwo(@NonNull Location location) {
        this.positionTwo = location;
        this.lastUpdateMillis = System.currentTimeMillis();
    }

    public Location getAppropriatePos(Action action) {
        return action == Action.LEFT_CLICK_BLOCK ? positionOne : positionTwo;
    }

    public void setAppropriatePos(@NonNull Action action, Location location) {
        if (action == Action.LEFT_CLICK_BLOCK) this.setPositionOne(location);
        else this.setPositionTwo(location);
    }

    /**
     * Checks if the {@link ClaimSelection} has both {@link Location}s set.
     *
     * @return true if both positions are set
     */
    public boolean hasBothPositionsSet() {
        return positionOne != null && positionTwo != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClaimSelection)) return false;

        ClaimSelection that = (ClaimSelection) o;

        if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) return false;
        if (world != null ? !world.equals(that.world) : that.world != null) return false;
        if (positionOne != null ? !positionOne.equals(that.positionOne) : that.positionOne != null) return false;
        return !(positionTwo != null ? !positionTwo.equals(that.positionTwo) : that.positionTwo != null);
    }

    @Override
    public int hashCode() {
        int result = uuid != null ? uuid.hashCode() : 0;
        result = 31 * result + (world != null ? world.hashCode() : 0);
        result = 31 * result + (positionOne != null ? positionOne.hashCode() : 0);
        result = 31 * result + (positionTwo != null ? positionTwo.hashCode() : 0);
        return result;
    }

    @Override
    public ClaimSelection clone() {
        try {
            return (ClaimSelection) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }
}