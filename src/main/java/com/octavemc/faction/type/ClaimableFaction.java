package com.octavemc.faction.type;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.octavemc.Apollo;
import com.octavemc.Configuration;
import com.octavemc.faction.claim.Claim;
import com.octavemc.faction.event.FactionClaimChangeEvent;
import com.octavemc.faction.event.FactionClaimChangedEvent;
import com.octavemc.faction.event.cause.ClaimChangeCause;
import com.octavemc.util.BukkitUtils;
import dev.morphia.annotations.Property;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a {@link Faction} that can claim land.
 */
@NoArgsConstructor
public class ClaimableFaction extends Faction {

    @Property
    protected List<Claim> claims = new ArrayList();

    public ClaimableFaction(String name) {
        super(name);
    }

    protected static final ImmutableMap<World.Environment, String> ENVIRONMENT_MAPPINGS = Maps.immutableEnumMap(ImmutableMap.of(
            World.Environment.NETHER, "Nether",
            World.Environment.NORMAL, "Overworld",
            World.Environment.THE_END, "The End"
    ));

    /**
     * Prints details about this {@link Faction} to a {@link CommandSender}.
     *
     * @param sender the sender to print to
     */
    @Override
    public void printDetails(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + BukkitUtils.STRAIGHT_LINE_DEFAULT);
        sender.sendMessage(getDisplayName(sender));
        for (Claim claim : claims) {
            Location location = claim.getCenter();
            sender.sendMessage(ChatColor.AQUA + "Location: " + ChatColor.WHITE +
                    '(' + ENVIRONMENT_MAPPINGS.get(location.getWorld().getEnvironment()) + ", " + location.getBlockX() + ChatColor.GRAY + " | " +  ChatColor.WHITE + location.getBlockZ() + ')');
        }

        sender.sendMessage(ChatColor.AQUA + BukkitUtils.STRAIGHT_LINE_DEFAULT);
    }

    /**
     * Gets the {@link Claim}s owned by this {@link ClaimableFaction}.
     *
     * @return an immutable list of {@link Claim}s
     */
    public List<Claim> getClaims() {
        return ImmutableList.copyOf(this.claims);
    }

    /**
     * Gets the {@link Claim}s owned by this {@link ClaimableFaction}
     * in a specific world.
     *
     * @return an immutable list of {@link Claim}s
     */
    public List<Claim> getClaims(World world) {
        List<Claim> ret = new ArrayList<>();
        for (Claim claim : this.claims) {
            if (world.equals(claim.getWorld())) {
                ret.add(claim);
            }
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Adds a {@link Claim} for this {@link Faction}.
     *
     * @param claim  the {@link Claim} to add
     * @param sender the {@link CommandSender} adding claim
     * @return true if the {@link Claim} was successfully added
     */
    public boolean addClaim(Claim claim, @Nullable CommandSender sender) {
        return addClaims(Collections.singleton(claim), sender);
    }

    /**
     * Adds a collection of {@link Claim}s to this {@link ClaimableFaction}.
     *
     * @param adding the {@link Claim}s to add
     * @param sender the {@link CommandSender} adding the {@link Claim}s
     * @return true if the {@link Claim}s were successfully added
     */
    public boolean addClaims(Collection<Claim> adding, @Nullable CommandSender sender) {
        if (sender == null) sender = Bukkit.getConsoleSender();

        var event = new FactionClaimChangeEvent(sender, ClaimChangeCause.CLAIM, adding, this);
        Apollo.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled() || !claims.addAll(adding)) return false;

        Apollo.getInstance().getServer().getPluginManager().callEvent(new FactionClaimChangedEvent(sender, ClaimChangeCause.CLAIM, adding));
        return true;
    }

    /**
     * Removes a {@link Claim} for this {@link Faction}.
     *
     * @param claim  the {@link Claim} to remove
     * @param sender the {@link CommandSender} removing {@link Claim}
     * @return true if the {@link Claim} was successfully removed
     */
    public boolean removeClaim(Claim claim, @Nullable CommandSender sender) {
        return removeClaims(Collections.singleton(claim), sender);
    }

    /**
     * Removes a collection of {@link Claim}s for this {@link Faction}.
     *
     * @param toRemove the {@link Claim}s to remove
     * @param sender   the {@link CommandSender} removing {@link Claim}s
     * @return true if the {@link Claim}s were successfully removed
     */
    public boolean removeClaims(Collection<Claim> toRemove, @Nullable CommandSender sender) {
        if (sender == null) sender = Bukkit.getConsoleSender();

        int expected = this.claims.size() - toRemove.size();

        var event = new FactionClaimChangeEvent(sender, ClaimChangeCause.UNCLAIM, new ArrayList<>(toRemove), this);
        Apollo.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled() || !this.claims.removeAll(toRemove)) return false; // we clone the collection so we can show what we removed to the event.

        if (expected != this.claims.size()) return false;

        if (this instanceof PlayerFaction faction) {
            int refund = 0;
            for (Claim claim : toRemove) {
                refund += Apollo.getInstance().getClaimHandler().calculatePrice(claim, expected, true);
                if (expected > 0) expected--;

                if (faction.getHome() != null && claim.contains(faction.getHome())) {
                    faction.setHome(null);
                    faction.broadcast(Configuration.DANGER_MESSAGE_PREFIX + ChatColor.GRAY + "Your faction home has been unset, as the claim it resided in was removed.");
                    break;
                }
            }
            int finalRefund = refund;
            Apollo.getInstance().getUserDao().get(faction.getLeader().getUniqueID()).ifPresent(user -> user.setBalance(user.getBalance() + finalRefund));
            faction.broadcast(Configuration.PRIMARY_MESSAGE_PREFIX + ChatColor.GRAY + "The faction leader was refunded " + ChatColor.AQUA + Configuration.ECONOMY_SYMBOL + refund + ChatColor.GRAY + " because land was unclaimed.");
        }

        Apollo.getInstance().getServer().getPluginManager().callEvent(new FactionClaimChangedEvent(sender, ClaimChangeCause.UNCLAIM, toRemove));
        return true;
    }
}
