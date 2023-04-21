package com.octavemc.faction.claim;

import com.octavemc.faction.FactionMember;
import com.octavemc.faction.type.Faction;
import com.octavemc.util.cuboid.Cuboid;
import dev.morphia.annotations.Property;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A implementation of {@link Claim} that denies other {@link FactionMember}s to access specific areas.
 */
@NoArgsConstructor
public class Subclaim extends Claim implements Cloneable {

    @Property
    private final Set<UUID> accessibleMembers = new HashSet<>();

    public Subclaim(Faction faction, Location location) {
        super(faction, location, location);
    }

    public Subclaim(Faction faction, Location location1, Location location2) {
        super(faction, location1, location2);
    }

    public Subclaim(Faction faction, World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        super(faction, world, x1, y1, z1, x2, y2, z2);
    }

    public Subclaim(Faction faction, Cuboid cuboid) {
        super(faction, cuboid);
    }

    /**
     * Gets the member {@link UUID}s that have access to this {@link Subclaim}.
     *
     * @return set of accessible member {@link UUID}s
     */
    public Set<UUID> getAccessibleMembers() {
        return accessibleMembers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subclaim)) return false;
        if (!super.equals(o)) return false;

        Subclaim blocks = (Subclaim) o;

        return !(accessibleMembers != null ? !accessibleMembers.equals(blocks.accessibleMembers) : blocks.accessibleMembers != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (accessibleMembers != null ? accessibleMembers.hashCode() : 0);
        return result;
    }

    @Override
    public Subclaim clone() {
        return (Subclaim) super.clone();
    }
}
