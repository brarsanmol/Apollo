package com.octavemc.faction.type;

import com.google.common.base.Preconditions;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.event.FactionRenameEvent;
import com.octavemc.faction.struct.Relation;
import com.octavemc.util.BukkitUtils;
import dev.morphia.annotations.*;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@NoArgsConstructor
@Entity
@Indexes(@Index(fields = { @Field("name") }, options = @IndexOptions(unique = true)))
public abstract class Faction {

    public static final String FACTIONLESS_PREFIX = "*";

    @Id
    protected ObjectId identifier;
    @Property
    protected String name;

    @Property
    protected long creationMillis = System.currentTimeMillis();  // the system millis when the faction was created
    public long lastRenameMillis;  // the system millis when the faction was last renamed

    @Property
    protected double dtrLossMultiplier = 1.0;
    @Property
    protected double deathbanMultiplier = 1.0;
    @Property
    protected boolean safezone;

    public Faction(String name) {
        this.identifier = new ObjectId();
        this.name = name;
    }

    /**
     * Gets the object ID of this {@link Faction}.
     *
     * @return the {@link ObjectId}
     */
    public ObjectId getIdentifier() {
        return identifier;
    }

    /**
     * Gets the name of this {@link Faction}.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this {@link Faction}.
     *
     * @param name the name to set
     * @return true if the name was successfully set
     */
    public boolean setName(String name) {
        return setName(name, Bukkit.getConsoleSender());
    }

    /**
     * Sets the name of this {@link Faction}.
     *
     * @param name   the name to set
     * @param sender the setting {@link CommandSender}
     * @return true if the name was successfully set
     */
    public boolean setName(String name, CommandSender sender) {
        if (this.name.equals(name)) {
            return false;
        }

        FactionRenameEvent event = new FactionRenameEvent(this, sender, this.name, name);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        this.lastRenameMillis = System.currentTimeMillis();
        this.name = name;
        return true;
    }

    public Relation getFactionRelation(Faction faction) {
        if (faction instanceof PlayerFaction target) {
            if (target == this) return Relation.MEMBER;
            else if (target.getAllied().contains(this.identifier)) return Relation.ALLY;
        }

        return Relation.ENEMY;
    }

    public Relation getRelation(CommandSender sender) {
        if (sender instanceof Player && Apollo.getInstance().getFactionDao().getByPlayer(((Player) sender).getUniqueId()).isPresent()) {
            return getFactionRelation(Apollo.getInstance().getFactionDao().getByPlayer(((Player) sender).getUniqueId()).get());
        }
        return Relation.ENEMY;
    }

    /**
     * Gets the display name of this {@link Faction} to a viewing
     * {@link CommandSender}.
     *
     * @param sender the {@link CommandSender} to get for
     * @return the display name for the viewer
     */
    public String getDisplayName(CommandSender sender) {
        return (safezone ? Configuration.RELATION_COLOUR_ALLY : getRelation(sender).toChatColour()) + name;
    }

    /**
     * Gets the display name of this {@link Faction} to a viewing
     * {@link Faction}.
     *
     * @param other the target {@link Faction} to get for
     * @return the display name for the viewer
     */
    public String getDisplayName(Faction other) {
        return getFactionRelation(other).toChatColour() + name;
    }

    /**
     * Prints details about this {@link Faction} to a {@link CommandSender}.
     *
     * @param sender the sender to print to
     */
    public void printDetails(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(getDisplayName(sender));
        sender.sendMessage(ChatColor.AQUA + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    /**
     * Checks if a deathban will be applied when players are killed
     * in the territory of this {@link Faction}.
     *
     * @return true if will deathban
     */
    public boolean isDeathban() {
        return !safezone && deathbanMultiplier > 0.0D;
    }

    /**
     * Sets if a deathban will be applied when players are killed
     * in the territory of this {@link Faction}.
     * <p>Setting to true will set the DTR multiplier to 1.0</p>
     *
     * @param deathban the value to set
     */
    public void setDeathban(boolean deathban) {
        if (deathban != isDeathban()) {
            this.deathbanMultiplier = deathban ? 1.0D : 0.5D;
        }
    }

    /**
     * Gets the deathban multiplier when players are killed in the territory
     * of this {@link Faction}.
     * <p>To disable deathbans completely, set the multiplier to 0</p>
     *
     * @return the deathban multiplier.
     */
    public double getDeathbanMultiplier() {
        return deathbanMultiplier;
    }

    /**
     * Sets the deathban multiplier when players are killed in the territory
     * of this {@link Faction}.
     * <p>To disable deathbans completely, set the multiplier to 0</p>
     *
     * @param deathbanMultiplier the multiplier to set.
     */
    public void setDeathbanMultiplier(double deathbanMultiplier) {
        Preconditions.checkArgument(deathbanMultiplier >= 0, "Deathban multiplier may not be negative");
        this.deathbanMultiplier = deathbanMultiplier;
    }

    public double getDtrLossMultiplier() {
        return dtrLossMultiplier;
    }

    public void setDtrLossMultiplier(double dtrLossMultiplier) {
        this.dtrLossMultiplier = dtrLossMultiplier;
    }

    /**
     * Checks if this {@link Faction} is a safezone protecting {@link Player}s from PVP and PVE.
     *
     * @return true if is safezone
     */
    public boolean isSafezone() {
        return safezone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Faction faction = (Faction) o;

        if (creationMillis != faction.creationMillis) return false;
        if (lastRenameMillis != faction.lastRenameMillis) return false;
        if (Double.compare(faction.dtrLossMultiplier, dtrLossMultiplier) != 0) return false;
        if (Double.compare(faction.deathbanMultiplier, deathbanMultiplier) != 0) return false;
        if (safezone != faction.safezone) return false;
        if (identifier != null ? !identifier.equals(faction.identifier) : faction.identifier != null) return false;
        return !(name != null ? !name.equals(faction.name) : faction.name != null);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (creationMillis ^ (creationMillis >>> 32));
        result = 31 * result + (int) (lastRenameMillis ^ (lastRenameMillis >>> 32));
        temp = Double.doubleToLongBits(dtrLossMultiplier);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(deathbanMultiplier);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (safezone ? 1 : 0);
        return result;
    }
}
