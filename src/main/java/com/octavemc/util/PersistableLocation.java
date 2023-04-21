package com.octavemc.util;

import com.google.common.base.Preconditions;
import com.octavemc.Apollo;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import dev.morphia.annotations.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Embedded
@NoArgsConstructor
public class PersistableLocation implements Cloneable, ConfigurationSerializable {

    // Lazy loaded
    @Transient
    private Location location;
    @Transient
    private World world;
    @Property
    private UUID worldUID;
    @Property
    private double x, y, z;
    @Property
    private float yaw;
    @Property
    private float pitch;

    public PersistableLocation(Location location) {
        Preconditions.checkNotNull(location, "Location cannot be null");
        Preconditions.checkNotNull(location.getWorld(), "Locations' world cannot be null");

        this.world = location.getWorld();
        this.worldUID = world.getUID();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public PersistableLocation(World world, double x, double y, double z) {
        this.world = world;
        this.worldUID = world.getUID();
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = this.yaw = 0.0F;
    }

    public PersistableLocation(String worldName, double x, double y, double z) {
        this.worldUID = this.world.getUID();
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = this.yaw = 0.0F;
    }

    public PersistableLocation(Map<String, Object> map) {
        this.world = Apollo.getInstance().getServer().getWorld((String) map.get("world"));
        this.worldUID = world.getUID();
        this.x = (double) map.get("x");
        this.y = (double) map.get("y");
        this.z = (double) map.get("z");
        /*
        this.yaw = (double) map.get("yaw");
        this.pitch = (double) map.get("pitch");
         */
    }

    /**
     * Gets the {@link World} this is in.
     *
     * @return the containing world
     */
    public World getWorld() {
        Preconditions.checkNotNull(this.worldUID, "World UUID cannot be null");

        if (world == null) world = Bukkit.getWorld(this.worldUID);
        return world;
    }

    /**
     * Sets the {@link World} this is in.
     *
     * @param world the world to set
     */
    public void setWorld(World world) {
        this.worldUID = world.getUID();
        this.world = world;
    }

    /**
     * Converts this to a {@link Location}.
     *
     * @return the location instance
     */
    public Location getLocation() {
        if (this.location == null) {
            this.location = new Location(getWorld(), this.x, this.y, this.z, this.yaw, this.pitch);
        }

        return this.location;
    }

    @Override
    public Map<String, Object> serialize() {
        var map = new HashMap<String, Object>();
        map.put("world", world.getName());
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("yaw", yaw);
        map.put("pitch", pitch);
        return map;
    }

    @Override
    public PersistableLocation clone() {
        try {
            return (PersistableLocation) super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public String toString() {
        return "PersistableLocation [worldUID=" + this.worldUID +
                ", x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", yaw=" + this.yaw + ", pitch=" + this.pitch + ']';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistableLocation)) return false;

        PersistableLocation that = (PersistableLocation) o;

        if (Double.compare(that.x, x) != 0) return false;
        if (Double.compare(that.y, y) != 0) return false;
        if (Double.compare(that.z, z) != 0) return false;
        if (Float.compare(that.yaw, yaw) != 0) return false;
        if (Float.compare(that.pitch, pitch) != 0) return false;
        if (world != null ? !world.equals(that.world) : that.world != null) return false;
        return !(worldUID != null ? !worldUID.equals(that.worldUID) : that.worldUID != null);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = world != null ? world.hashCode() : 0;
        result = 31 * result + (worldUID != null ? worldUID.hashCode() : 0);
        temp = Double.doubleToLongBits(x);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (yaw != +0.0f ? Float.floatToIntBits(yaw) : 0);
        result = 31 * result + (pitch != +0.0f ? Float.floatToIntBits(pitch) : 0);
        return result;
    }
}
