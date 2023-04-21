package com.octavemc.faction.claim;

import com.octavemc.Apollo;
import com.octavemc.faction.type.ClaimableFaction;
import com.octavemc.faction.type.Faction;
import com.octavemc.util.cuboid.Cuboid;
import com.octavemc.util.cuboid.NamedCuboid;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Transient;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * An implementation of a {@link NamedCuboid} that represents land for a {@link Faction} can own.
 */
@Entity
@NoArgsConstructor
public class Claim extends NamedCuboid implements Cloneable {

    private static final Random RANDOM = new Random();

    @Transient
    private final Map<String, Subclaim> subclaims = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    @Property
    private ObjectId identifier;
    @Property
    private ObjectId faction;

    public Claim(Faction faction, Location location) {
        super(location, location);
        this.name = generateName();
        this.faction = faction.getIdentifier();
        this.identifier = new ObjectId();
    }

    public Claim(Faction faction, Location location1, Location location2) {
        super(location1, location2);
        this.name = generateName();
        this.faction = faction.getIdentifier();
        this.identifier = new ObjectId();
    }

    public Claim(Faction faction, World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        super(world, x1, y1, z1, x2, y2, z2);
        this.name = generateName();
        this.faction = faction.getIdentifier();
        this.identifier = new ObjectId();
    }

    public Claim(Faction faction, Cuboid cuboid) {
        super(cuboid);
        this.name = generateName();
        this.faction = faction.getIdentifier();
        this.identifier = new ObjectId();
    }

    private String generateName() {
        return String.valueOf(RANDOM.nextInt(899) + 100);
    }

    public ObjectId getIdentifier() {
        return identifier;
    }

    private boolean loaded = false;

    public ClaimableFaction getFaction() {
        return (ClaimableFaction) Apollo.getInstance().getFactionDao().get(this.faction).get();
    }

    /**
     * Gets the {@link Subclaim}s registered to this {@link Claim}.
     *
     * @return set of registered {@link Subclaim}s
     */
    public Collection<Subclaim> getSubclaims() {
        return this.subclaims.values();
    }

    /**
     * Gets a {@link Subclaim} this {@link Claim} has registered with
     * a specific name.
     *
     * @param name the name to search for
     * @return the {@link Subclaim}, null if not found
     */
    public Subclaim getSubclaim(String name) {
        return subclaims.get(name);
    }

    /**
     * Gets the formatted name for this {@link Claim}.
     *
     * @return the {@link Claim} formatted name
     */
    public String getFormattedName() {
        return getName() + ": (" + worldName + ", " + x1 + ", " + y1 + ", " + z1 + ") - (" + worldName + ", " + x2 + ", " + y2 + ", " + z2 + ')';
    }

    @Override
    public Claim clone() {
        return (Claim) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Claim blocks = (Claim) o;

        if (loaded != blocks.loaded) return false;
        if (subclaims != null ? !subclaims.equals(blocks.subclaims) : blocks.subclaims != null) return false;
        if (identifier != null ? !identifier.equals(blocks.identifier) : blocks.identifier != null) return false;
        if (faction != null ? !faction.equals(blocks.faction) : blocks.faction != null) return false;
        return !(faction != null ? !faction.equals(blocks.faction) : blocks.faction != null);
    }

    @Override
    public int hashCode() {
        int result = subclaims != null ? subclaims.hashCode() : 0;
        result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
        result = 31 * result + (faction != null ? faction.hashCode() : 0);
        result = 31 * result + (faction != null ? faction.hashCode() : 0);
        result = 31 * result + (loaded ? 1 : 0);
        return result;
    }
}
